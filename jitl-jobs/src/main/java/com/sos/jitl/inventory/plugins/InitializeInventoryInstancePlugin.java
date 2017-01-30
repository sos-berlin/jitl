package com.sos.jitl.inventory.plugins;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.xml.SOSXMLXPath;

import com.sos.exception.InvalidDataException;
import com.sos.exception.NoResponseException;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.jitl.inventory.data.InventoryEventUpdateUtil;
import com.sos.jitl.inventory.data.ProcessInitialInventoryUtil;
import com.sos.jitl.inventory.model.InventoryModel;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.scheduler.engine.kernel.plugin.AbstractPlugin;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public class InitializeInventoryInstancePlugin extends AbstractPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitializeInventoryInstancePlugin.class);
    private static final String COMMAND = 
            "<show_state subsystems=\"folder\" what=\"folders cluster no_subfolders\" path=\"/any/path/that/does/not/exists\" />";
    private static final String HIBERNATE_CONFIG_PATH_APPENDER = "hibernate.cfg.xml";
    private SchedulerXmlCommandExecutor xmlCommandExecutor;
    private SOSHibernateFactory prepareFactory;
    private SOSHibernateFactory activateFactory;
    private Path liveDirectory;
    private InventoryModel model;
    private InventoryEventUpdateUtil inventoryEventUpdate;
    private SOSXMLXPath xPathAnswerXml;
    private VariableSet variables;
    private ExecutorService fixedThreadPoolExecutor = Executors.newFixedThreadPool(1);
    private String masterUrl;
    private Path hibernateConfigPath;
    private Path schedulerXmlPath;
    private String proxyUrl;
    private Path configDirectory;
    private String host;
    private Integer port;
    

    @Inject
    public InitializeInventoryInstancePlugin(SchedulerXmlCommandExecutor xmlCommandExecutor, VariableSet variables){
        this.xmlCommandExecutor = xmlCommandExecutor;
        this.variables = variables;
    }

    @Override
    public void onPrepare() {
        if (variables.apply("sos.proxy_url") != null && !variables.apply("sos.proxy_url").isEmpty()) {
            proxyUrl = variables.apply("sos.proxy_url");
        }
        try {
            Runnable inventoryInitThread = new Runnable() {
                @Override
                public void run() {
                    try {
                        initFirst();
                        LOGGER.info("*** initial inventory instance update started ***");
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
                        LOGGER.info("*** event based inventory update started ***");
                        initActivate(hibernateConfigPath);
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
    
    public void executeInitialInventoryProcessing() throws Exception {
        ProcessInitialInventoryUtil dataUtil = new ProcessInitialInventoryUtil(prepareFactory);
        dataUtil.setConfigDirectory(configDirectory);
        DBItemInventoryInstance jsInstanceItem = dataUtil.process(xPathAnswerXml, liveDirectory, hibernateConfigPath, masterUrl);
        InventoryModel model = initInitialInventoryProcessing(jsInstanceItem, schedulerXmlPath);
        if (model != null) {
            LOGGER.info("*** initial inventory configuration update started ***");
            model.process();
        }
    }
    
    private void initFirst() throws Exception{
        String schedulerXmlPathname = null;
        String answerXml = null;
        for (int i=0; i < 120; i++) {
            try {
                Thread.sleep(1000);
                answerXml = executeXML(COMMAND); 
                if (answerXml != null && !answerXml.isEmpty()) {
                    xPathAnswerXml = new SOSXMLXPath(new StringBuffer(answerXml));
                    String state = xPathAnswerXml.selectSingleNodeValue("/spooler/answer/state/@state");
                    if ("running,waiting_for_activation,paused".contains(state)) {
                        schedulerXmlPathname = xPathAnswerXml.selectSingleNodeValue("/spooler/answer/state/@config_file");
                        break; 
                    }
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
        if (schedulerXmlPathname == null) {
            throw new InvalidDataException("Couldn't determine path of scheduler.xml");
        }
        schedulerXmlPath = Paths.get(schedulerXmlPathname);
        if (!Files.exists(schedulerXmlPath)) {
            throw new IOException(String.format("Configuration file %1$s doesn't exist", schedulerXmlPathname));
        }
        
        if (answerXml == null || answerXml.isEmpty()) {
            throw new NoResponseException("JobScheduler doesn't response the state");
        }
        // TODO consider scheduler.xml to get "live" directory in /spooler/config/@configuration_directory
        configDirectory = schedulerXmlPath.getParent();
        if (configDirectory == null) {
            throw new InvalidDataException("Couldn't determine \"config\" directory.");
        }
        liveDirectory = configDirectory.resolve("live");
        hibernateConfigPath = configDirectory.resolve(HIBERNATE_CONFIG_PATH_APPENDER);
        initPrepare(hibernateConfigPath);
        try {
            masterUrl = getUrlFromJobScheduler(xPathAnswerXml);
        } catch (Exception e) {
            throw new InvalidDataException("Couldn't determine JobScheduler http url", e);
        }
    }
    
    private void initActivate(Path hibernateConfigPath) throws Exception {
        activateFactory = new SOSHibernateFactory(hibernateConfigPath);
        activateFactory.setAutoCommit(false);
        activateFactory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        activateFactory.setIgnoreAutoCommitTransactions(true);
        activateFactory.addClassMapping(DBLayer.getInventoryClassMapping());
        activateFactory.build();
    }
    
    private void initPrepare(Path hibernateConfigPath) throws Exception {
        prepareFactory = new SOSHibernateFactory(hibernateConfigPath);
        prepareFactory.setAutoCommit(false);
        prepareFactory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        prepareFactory.setIgnoreAutoCommitTransactions(true);
        prepareFactory.addClassMapping(DBLayer.getInventoryClassMapping());
        prepareFactory.build();
    }
    
    private InventoryModel initInitialInventoryProcessing(DBItemInventoryInstance jsInstanceItem, Path schedulerXmlPath) throws Exception {
        model = new InventoryModel(prepareFactory, jsInstanceItem, schedulerXmlPath);
        model.setXmlCommandExecutor(xmlCommandExecutor);
        return model;
    }
    
    private void executeEventBasedInventoryProcessing() {
        inventoryEventUpdate = new InventoryEventUpdateUtil(host, port, activateFactory);
        inventoryEventUpdate.execute();
    }
    
    private String executeXML(String xmlCommand) {
        if (xmlCommandExecutor != null) {
            return xmlCommandExecutor.executeXml(xmlCommand);
        } else {
            LOGGER.error("xmlCommandExecutor is null");
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
    
    private String getUrlFromJobScheduler(SOSXMLXPath xPath) throws Exception {
        // TODO consider plugin parameter "url"
//        if (variables.apply("sos.proxy_url") != null && !variables.apply("sos.proxy_url").isEmpty()) {
//            return variables.apply("sos.proxy_url");
//        }
        if (proxyUrl != null) {
            return proxyUrl;
        }
        StringBuilder strb = new StringBuilder();
        strb.append("http://");
        strb.append(InetAddress.getLocalHost().getCanonicalHostName().toLowerCase());
        strb.append(":");
        host = xPath.selectSingleNodeValue("/spooler/answer/state/@host");
        String httpPort = xPath.selectSingleNodeValue("/spooler/answer/state/@http_port", "40444"); 
        port = Integer.valueOf(httpPort);
        strb.append(httpPort);
        return strb.toString();
    }
    
    private void closeConnections(){
        if(inventoryEventUpdate != null) {
            inventoryEventUpdate.setClosed(true);
            try {
                inventoryEventUpdate.getHttpClient().close();
            } catch (IOException e) {}
        }
        if (prepareFactory != null) {
            prepareFactory.close();
        }
    }
}