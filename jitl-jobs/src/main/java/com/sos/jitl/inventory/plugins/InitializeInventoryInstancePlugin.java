package com.sos.jitl.inventory.plugins;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sos.xml.SOSXMLXPath;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.inventory.data.ProcessInitialInventoryUtil;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBItemInventoryOperatingSystem;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.job.inventory.InventoryJobOptions;
import com.sos.jitl.reporting.model.inventory.InventoryModel;
import com.sos.scheduler.engine.kernel.plugin.AbstractPlugin;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public class InitializeInventoryInstancePlugin extends AbstractPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitializeInventoryInstancePlugin.class);
    private static final String COMMAND = 
            "<show_state subsystems=\"folder\" what=\"folders cluster no_subfolders\" path=\"/any/path/that/does/not/exists\" />";
    private static final String HIBERNATE_CONFIG_PATH_APPENDER = "hibernate.cfg.xml";
    private SchedulerXmlCommandExecutor xmlCommandExecutor;
    private SOSHibernateConnection connection;
    private String liveDirectory;
    private String configDirectory;
    private InventoryModel model;
    private String answerXml;
    private VariableSet variables;
    private String proxyUrl;
    private ExecutorService singleThreadExecutor;

    @Inject
    public InitializeInventoryInstancePlugin(SchedulerXmlCommandExecutor xmlCommandExecutor, VariableSet variables){
        this.xmlCommandExecutor = xmlCommandExecutor;
        this.variables = variables;
    }

    @Override
    public void onPrepare() {
        proxyUrl = variables.apply("sos.proxy_url");
        try {
            singleThreadExecutor = Executors.newSingleThreadExecutor();
            Runnable runnableThread = new Runnable() {
                @Override
                public void run() {
                    try {
                        execute();
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            };
            singleThreadExecutor.execute(runnableThread);
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        }
        super.onPrepare();
    }
    
    public void execute() {
        try {
            answerXml = executeXML();
            if (answerXml != null && !answerXml.isEmpty()) {
                liveDirectory = getLiveDirectory(answerXml);
                configDirectory = getConfigDirectory(answerXml);
                if(!configDirectory.endsWith("/")) {
                    configDirectory = configDirectory + "/";
                }
                String hibernateConfigPath = configDirectory + HIBERNATE_CONFIG_PATH_APPENDER;
                init(hibernateConfigPath);
                ProcessInitialInventoryUtil dataUtil = new ProcessInitialInventoryUtil(hibernateConfigPath, connection);
                dataUtil.setLiveDirectory(liveDirectory);
                if(proxyUrl != null && !proxyUrl.isEmpty()) {
                    dataUtil.setProxyUrl(proxyUrl);
                }
//                dataUtil.setSupervisorHost(supervisorHost);
//                dataUtil.setSupervisorPort(supervisorPort);
                DBItemInventoryInstance jsInstanceItem = dataUtil.getDataFromJobscheduler(answerXml);
                DBItemInventoryOperatingSystem osItem = dataUtil.getOsData(jsInstanceItem);
                dataUtil.insertOrUpdateDB(jsInstanceItem, osItem);
            } else {
                LOGGER.error("No answer from JobScheduler received! ");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {
                initInventoryJob();
            } catch (Exception e1) {
                LOGGER.error(e1.getMessage(), e1);
            }
            if (model != null) {
                try {
                    model.process();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            exit();
        }
    }
    
    private void init(String hibernateConfigPath) {
        try {
            connection = new SOSHibernateConnection(hibernateConfigPath);
            connection.setAutoCommit(true);
            connection.addClassMapping(DBLayer.getInventoryClassMapping());
            connection.connect();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    private void initInventoryJob() throws Exception {
        InventoryJobOptions options = new InventoryJobOptions();
        options.current_scheduler_configuration_directory.setValue(liveDirectory);
        model = new InventoryModel(connection, options);
        model.setAnswerXml(answerXml);
    }
    
    private String executeXML() {
        if (xmlCommandExecutor != null) {
            return xmlCommandExecutor.executeXml(COMMAND);
        } else {
            LOGGER.error("xmlCommandExecutor is null");
        }
        return null;
    }

    private String getLiveDirectory(String answerXml) {
        try {
            SOSXMLXPath xPath = new SOSXMLXPath(new StringBuffer(answerXml));
            Node stateNode = xPath.selectSingleNode("/spooler/answer/state");
            Element stateElement = (Element) stateNode;
            String schedulerXmlPathname = stateElement.getAttribute("config_file");
            Path schedulerXMLPath = Paths.get(schedulerXmlPathname);
            Path liveDirectory = Paths.get(schedulerXMLPath.getParent().toString(), "live");
            return liveDirectory.toString();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
    
    private String getConfigDirectory(String answerXml) {
        try {
            SOSXMLXPath xPath = new SOSXMLXPath(new StringBuffer(answerXml));
            Node stateNode = xPath.selectSingleNode("/spooler/answer/state");
            Element stateElement = (Element) stateNode;
            String schedulerXmlPathname = stateElement.getAttribute("config_file");
            Path schedulerXMLPath = Paths.get(schedulerXmlPathname);
            return schedulerXMLPath.getParent().toString();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
    
    private void exit() {
        if (connection != null) {
            connection.disconnect();
        }
    }

}