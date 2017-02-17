package com.sos.jitl.inventory.plugins;

import static scala.collection.JavaConversions.mapAsJavaMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Map;
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
import com.sos.jitl.classes.plugin.PluginMailer;
import com.sos.jitl.inventory.data.InventoryEventUpdateUtil;
import com.sos.jitl.inventory.data.ProcessInitialInventoryUtil;
import com.sos.jitl.inventory.model.InventoryModel;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.scheduler.engine.eventbus.EventBus;
import com.sos.scheduler.engine.kernel.Scheduler;
import com.sos.scheduler.engine.kernel.plugin.AbstractPlugin;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public class InitializeInventoryInstancePlugin extends AbstractPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitializeInventoryInstancePlugin.class);
    private static final String COMMAND = 
            "<show_state subsystems=\"folder\" what=\"folders cluster no_subfolders\" path=\"/any/path/that/does/not/exists\" />";
    private static final String REPORTING_HIBERNATE_CONFIG_PATH_APPENDER = "reporting.hibernate.cfg.xml";
    private static final String DEFAULT_HIBERNATE_CONFIG_PATH_APPENDER = "hibernate.cfg.xml";
    private static final String HIBERNATE_CFG_REPORTING_KEY = "sos.hibernate_configuration_reporting";
    private SchedulerXmlCommandExecutor xmlCommandExecutor;
    private SOSHibernateFactory factory;
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
    private String host;
    private Integer port;
    private String hibernateConfigReporting;
    private Scheduler scheduler;
    private EventBus customEventBus;

    @Inject
    public InitializeInventoryInstancePlugin(Scheduler scheduler, SchedulerXmlCommandExecutor xmlCommandExecutor, VariableSet variables, EventBus eventBus){
        this.scheduler = scheduler;
        this.xmlCommandExecutor = xmlCommandExecutor;
        this.variables = variables;
        this.customEventBus = eventBus;
        
    }

    @Override
    public void onPrepare() {
        try {
            if(variables.apply(HIBERNATE_CFG_REPORTING_KEY) != null && !variables.apply(HIBERNATE_CFG_REPORTING_KEY).isEmpty()) {
                hibernateConfigReporting = variables.apply(HIBERNATE_CFG_REPORTING_KEY);
                if(Files.notExists(Paths.get(hibernateConfigReporting))) {
                    throw new FileNotFoundException("The file configured in scheduler.xml as 'sos.hibernate_configuration_reporting' could not be found!");
                }
            }
            if (variables.apply("sos.proxy_url") != null && !variables.apply("sos.proxy_url").isEmpty()) {
                proxyUrl = variables.apply("sos.proxy_url");
            }
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
        Map<String, String> mailDefaults = mapAsJavaMap(scheduler.mailDefaults());
        try {
            Runnable inventoryEventThread = new Runnable() {
                @Override
                public void run() {
                    PluginMailer mailer = new PluginMailer("inventory", mailDefaults);
                    try {
                        LOGGER.info("*** event based inventory update started ***");
                        executeEventBasedInventoryProcessing();
                    } catch (Exception e) {
                        LOGGER.error(e.toString(), e);
                        mailer.sendOnError("InitializeInventoryInstancePlugin", "onActivate", e);
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
        ProcessInitialInventoryUtil dataUtil = new ProcessInitialInventoryUtil(factory);
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
        liveDirectory = schedulerXmlPath.getParent().resolve("live");
        if(hibernateConfigReporting != null && !hibernateConfigReporting.isEmpty()) {
            hibernateConfigPath = Paths.get(hibernateConfigReporting);
        } else if (Files.exists(schedulerXmlPath.getParent().resolve(REPORTING_HIBERNATE_CONFIG_PATH_APPENDER))) {
            hibernateConfigPath = schedulerXmlPath.getParent().resolve(REPORTING_HIBERNATE_CONFIG_PATH_APPENDER);
        } else {
            hibernateConfigPath = schedulerXmlPath.getParent().resolve(DEFAULT_HIBERNATE_CONFIG_PATH_APPENDER);
        }
        if (hibernateConfigPath != null) {
            init(hibernateConfigPath);
        } else {
            throw new FileNotFoundException("No hibernate configuration file found!");
        }
        try {
            masterUrl = getUrlFromJobScheduler(xPathAnswerXml);
        } catch (Exception e) {
            throw new InvalidDataException("Couldn't determine JobScheduler http url", e);
        }
    }
    
    private void init(Path hibernateConfigPath) throws Exception {
        factory = new SOSHibernateFactory(hibernateConfigPath);
        factory.setConnectionIdentifier("inventory");
        factory.setAutoCommit(false);
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        factory.addClassMapping(DBLayer.getInventoryClassMapping());
        factory.build();
    }
    
    private InventoryModel initInitialInventoryProcessing(DBItemInventoryInstance jsInstanceItem, Path schedulerXmlPath) throws Exception {
        model = new InventoryModel(factory, jsInstanceItem, schedulerXmlPath);
        model.setXmlCommandExecutor(xmlCommandExecutor);
        return model;
    }
    
    private void executeEventBasedInventoryProcessing() {
        inventoryEventUpdate = new InventoryEventUpdateUtil(host, port, factory, customEventBus);
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
        if (factory != null) {
            factory.close();
        }
    }
}