package com.sos.jitl.inventory.plugins;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sos.xml.SOSXMLXPath;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.inventory.data.InventoryEventUpdateUtil;
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
    private static final String FULL_COMMAND = "<show_state what=\"cluster source job_chains job_chain_orders schedules\" />";
    private static final String HIBERNATE_CONFIG_PATH_APPENDER = "hibernate.cfg.xml";
    private SchedulerXmlCommandExecutor xmlCommandExecutor;
    private SOSHibernateConnection connection;
    private String liveDirectory;
    private String configDirectory;
    private InventoryModel model;
    private InventoryEventUpdateUtil inventoryEventUpdate;
    private String answerXml;
    private VariableSet variables;
    private String proxyUrl;
    private ExecutorService fixedThreadPoolExecutor = Executors.newFixedThreadPool(1);
    private String masterUrl;
    private String hibernateConfigPath;

    @Inject
    public InitializeInventoryInstancePlugin(SchedulerXmlCommandExecutor xmlCommandExecutor, VariableSet variables){
        this.xmlCommandExecutor = xmlCommandExecutor;
        this.variables = variables;
    }

    @Override
    public void onPrepare() {
        proxyUrl = variables.apply("sos.proxy_url");
        try {
            Runnable inventoryInitThread = new Runnable() {
                @Override
                public void run() {
                    try {
                        initFirst();
                        executeInitialInventoryProcessing();
                    } catch (Exception e) {
                        LOGGER.error(e.toString(), e);
                    }
                }
            };
            fixedThreadPoolExecutor.submit(inventoryInitThread);
        } catch (Exception e) {
            closeConnections();
            LOGGER.error("Fatal Error in InventoryPlugin @OnPrepare:" + e.toString(), e);
        }
        super.onPrepare();
    }
    
    @Override
    public void onActivate() {
        try {
            Runnable inventoryEventThread = new Runnable() {
                @Override
                public void run() {
                    try {
                        executeEventBasedInventoryProcessing();
                    } catch (Exception e) {
                        LOGGER.error(e.toString(), e);
                    }
                }
            };
            fixedThreadPoolExecutor.submit(inventoryEventThread);
        } catch (Exception e) {
            closeConnections();
            LOGGER.error("Fatal Error in InventoryPlugin @OnActivate:" + e.toString(), e);
        }
        super.onActivate();
    }
    
    public void executeInitialInventoryProcessing() {
        try {
            if (answerXml != null && !answerXml.isEmpty()) {
                ProcessInitialInventoryUtil dataUtil = new ProcessInitialInventoryUtil(hibernateConfigPath, connection);
                dataUtil.setLiveDirectory(liveDirectory);
                if(proxyUrl != null && !proxyUrl.isEmpty()) {
                    dataUtil.setProxyUrl(proxyUrl);
                }
                DBItemInventoryInstance jsInstanceItem = dataUtil.getDataFromJobscheduler(answerXml);
                DBItemInventoryOperatingSystem osItem = dataUtil.getOsData(jsInstanceItem);
                dataUtil.insertOrUpdateDB(jsInstanceItem, osItem);
            } else {
                LOGGER.error("No answer from JobScheduler received!");
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        } finally {
            try {
                initInitialInventoryProcessing();
            } catch (Exception e1) {
                LOGGER.error(e1.toString(), e1);
            }
            if (model != null) {
                try {
                    model.process();
                } catch (Exception e) {
                    LOGGER.error(e.toString(), e);
                }
            }
        }
    }
    
    private void initFirst(){
        for (int i=0; i < 10; i++) {
            InputStream inStream = null;
            try {
                Thread.sleep(1000);
                answerXml = executeXML(COMMAND); 
                if (answerXml != null && !answerXml.isEmpty()) {
                    inStream = new ByteArrayInputStream(answerXml.getBytes());
                    SOSXMLXPath xPathAnswerXml = new SOSXMLXPath(inStream);
                    String state = xPathAnswerXml.selectSingleNodeValue("/spooler/answer/state/@state");
                    if ("running,waiting_for_activation,paused".contains(state)) {
                       break; 
                    }
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            } finally {
                try {
                    inStream.close(); 
                } catch (Exception e) {}
            }
        }
        if (answerXml != null && !answerXml.isEmpty()) {
            liveDirectory = getLiveDirectory(answerXml);
            configDirectory = getConfigDirectory(answerXml);
            if(!configDirectory.endsWith("/")) {
                configDirectory = configDirectory + "/";
            }
            hibernateConfigPath = configDirectory + HIBERNATE_CONFIG_PATH_APPENDER;
            init(hibernateConfigPath);
            try {
                masterUrl = getUrlFromJobScheduler();
            } catch (Exception e) {
                LOGGER.error(String.format("Problem getting url from JobScheduler: %1$s", e.toString()), e);
            }
        } else {
            LOGGER.error("JobScheduler doesn't response the state");
        }
    }
    
    private void init(String hibernateConfigPath) {
        try {
            connection = new SOSHibernateConnection(hibernateConfigPath);
            connection.setAutoCommit(false);
            connection.addClassMapping(DBLayer.getInventoryClassMapping());
            connection.connect();
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }
    
    private void initInitialInventoryProcessing() throws Exception {
        InventoryJobOptions options = new InventoryJobOptions();
        options.current_scheduler_configuration_directory.setValue(liveDirectory);
        String fullAnswerXml =executeXML(FULL_COMMAND);
        model = new InventoryModel(connection, options);
        model.setAnswerXml(fullAnswerXml);
    }
    
    private void executeEventBasedInventoryProcessing() {
        inventoryEventUpdate = new InventoryEventUpdateUtil(masterUrl, connection);
        inventoryEventUpdate.execute();
    }
    
    private String executeXML(String xmlCommand) {
        if (xmlCommandExecutor != null) {
//            return xmlCommandExecutor.executeXml(COMMAND);
            return xmlCommandExecutor.executeXml(xmlCommand);
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
            LOGGER.error(e.toString(), e);
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
            LOGGER.error(e.toString(), e);
        }
        return null;
    }
    
    @Override
    public void close() {
        closeConnections();
        try {
            fixedThreadPoolExecutor.shutdownNow();
            boolean shutdown = fixedThreadPoolExecutor.awaitTermination(1L, TimeUnit.SECONDS);
            if(shutdown) {
                LOGGER.debug("Thread has been shut down correctly.");
            } else {
                LOGGER.debug("Thread has ended due to timeout on shutdown. Doesn´t wait for answer from thread.");
            }
        } catch (InterruptedException e) {
            LOGGER.error(e.toString(), e);
        }
        super.close();
    }
    
    private String getUrlFromJobScheduler() throws Exception {
        StringBuilder strb = new StringBuilder();
        strb.append("http://");
        SOSXMLXPath xPath = new SOSXMLXPath(new StringBuffer(answerXml));
        Node stateNode = xPath.selectSingleNode("/spooler/answer/state");
        Element stateElement = (Element) stateNode;
        strb.append(InetAddress.getLocalHost().getCanonicalHostName().toLowerCase());
        strb.append(":");
        String httpPort = stateElement.getAttribute("http_port");
        if(httpPort != null && !httpPort.isEmpty()) {
            strb.append(httpPort);
        } else {
            strb.append("4444");
        }
        return strb.toString();
    }
    
    private void closeConnections(){
        if(inventoryEventUpdate != null) {
            try {
                inventoryEventUpdate.getHttpClient().close();
            } catch (IOException e) {
                LOGGER.error(e.toString(), e);
            }
        }
        if (connection != null) {
            connection.disconnect();
        }
    }
}