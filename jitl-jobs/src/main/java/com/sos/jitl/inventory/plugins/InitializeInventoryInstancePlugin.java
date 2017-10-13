package com.sos.jitl.inventory.plugins;

import static scala.collection.JavaConversions.mapAsJavaMap;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.xml.SOSXMLXPath;

import com.sos.exception.SOSInvalidDataException;
import com.sos.exception.SOSNoResponseException;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.jitl.classes.plugin.PluginMailer;
import com.sos.jitl.inventory.data.InventoryEventUpdateUtil;
import com.sos.jitl.inventory.data.ProcessInitialInventoryUtil;
import com.sos.jitl.inventory.exceptions.SOSInventoryPluginException;
import com.sos.jitl.inventory.helper.HttpHelper;
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
    private static final String COMMAND = "<show_state subsystems=\"folder\" "
            + "what=\"folders cluster no_subfolders operations\" "
            + "path=\"/any/path/that/does/not/exists\" />";
    private static final String REPORTING_HIBERNATE_CONFIG_PATH_APPENDER = "reporting.hibernate.cfg.xml";
    private static final String DEFAULT_HIBERNATE_CONFIG_PATH_APPENDER = "hibernate.cfg.xml";
    private static final String HIBERNATE_CFG_REPORTING_KEY = "sos.hibernate_configuration_reporting";
    private static final String REG_EXP_PATTERN_FOR_LIVE_FOLDER = "Directory_observer\\((.*)\\)";
    private static final Long HTTP_CLIENT_RECONNECT_DELAY = 30000L;
    private SchedulerXmlCommandExecutor xmlCommandExecutor;
    private SOSHibernateFactory factory;
    private Path liveDirectory;
    private InventoryModel model;
    private InventoryEventUpdateUtil inventoryEventUpdate;
    private SOSXMLXPath xPathAnswerXml;
    private VariableSet variables;
    private ExecutorService fixedThreadPoolExecutor = Executors.newFixedThreadPool(1);
    private Path hibernateConfigPath;
    private Path schedulerXmlPath;
    private String host;
    private Integer port;
    private String hibernateConfigReporting;
    private Scheduler scheduler;
    private EventBus customEventBus;
    private String supervisorHost;
    private String supervisorPort;
    private String schedulerId;
    private String hostFromHttpPort;

    @Inject
    public InitializeInventoryInstancePlugin(Scheduler scheduler, SchedulerXmlCommandExecutor xmlCommandExecutor,
            VariableSet variables, EventBus eventBus) {
        this.scheduler = scheduler;
        this.xmlCommandExecutor = xmlCommandExecutor;
        this.variables = variables;
        this.customEventBus = eventBus;
    }

    @Override
    public void onPrepare() {
        try {
            if (variables.apply(HIBERNATE_CFG_REPORTING_KEY) != null && !variables.apply(HIBERNATE_CFG_REPORTING_KEY).isEmpty()) {
                hibernateConfigReporting = variables.apply(HIBERNATE_CFG_REPORTING_KEY);
                if (Files.notExists(Paths.get(hibernateConfigReporting))) {
                    LOGGER.warn("The file configured in scheduler.xml as 'sos.hibernate_configuration_reporting' could not be found!");
                }
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
                    } catch (Throwable t) {
                        LOGGER.error(t.toString(), t);
                    }
                }
            };
            fixedThreadPoolExecutor.submit(inventoryInitThread);
        } catch (Exception e) {
            closeConnections();
            LOGGER.error("Fatal Error in InventoryPlugin @OnPrepare:" + e.toString(), e);
        } catch (Throwable t) {
            LOGGER.error("Fatal Error in InventoryPlugin @OnPrepare:" + t.toString(), t);
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
                        LOGGER.warn("Restarting execution of events!");
                        try {
                            inventoryEventUpdate.restartExecution();
                        } catch (Exception e1) {
                        }
                    } catch (Throwable t) {
                        mailer.sendOnError("InitializeInventoryInstancePlugin", "onActivate", t);
                        try {
                            Thread.sleep(HTTP_CLIENT_RECONNECT_DELAY);
                        } catch (InterruptedException e1) {
                        }
                        LOGGER.warn("Restarting execution of events!");
                        try {
                            inventoryEventUpdate.restartExecution();
                        } catch (Exception e1) {
                        }
                    }
                }
            };
            fixedThreadPoolExecutor.submit(inventoryEventThread);
        } catch (Exception e) {
            closeConnections();
            LOGGER.error("Fatal Error in InventoryPlugin @OnActivate:" + e.toString(), e);
        } catch (Throwable t) {
            closeConnections();
            LOGGER.error("Fatal Error in InventoryPlugin @OnActivate:" + t.toString(), t);
        }
        super.onActivate();
    }

    public void executeInitialInventoryProcessing() throws Exception {
        ProcessInitialInventoryUtil dataUtil = new ProcessInitialInventoryUtil(factory);
        if(supervisorHost != null && supervisorPort != null) {
            dataUtil.setSupervisorHost(supervisorHost);
            dataUtil.setSupervisorPort(supervisorPort);
            LOGGER.debug("[InventoryPlugin] - supervisor host is " + supervisorHost);
            LOGGER.debug("[InventoryPlugin] - supervisor port is " + supervisorPort);
        }
        DBItemInventoryInstance jsInstanceItem = dataUtil.process(xPathAnswerXml, liveDirectory, hibernateConfigPath);
        InventoryModel model = initInitialInventoryProcessing(jsInstanceItem, schedulerXmlPath);
        if (model != null) {
            model.setLiveDirectory(liveDirectory);
            LOGGER.info("*** initial inventory configuration update started ***");
            model.process();
        }
    }

    private void initFirst() throws Exception {
        String schedulerXmlPathname = null;
        String answerXml = null;
        for (int i = 0; i < 120; i++) {
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
            throw new SOSInvalidDataException("Couldn't determine path of scheduler.xml");
        }
        schedulerXmlPath = Paths.get(schedulerXmlPathname);
        if (!Files.exists(schedulerXmlPath)) {
            throw new SOSInventoryPluginException(String.format("Configuration file %1$s doesn't exist", schedulerXmlPathname));
        }
        if (answerXml == null || answerXml.isEmpty()) {
            throw new SOSNoResponseException("JobScheduler doesn't response the state");
        }
        try {
            setGlobalProperties(xPathAnswerXml);
        } catch (Exception e) {
            throw new SOSInvalidDataException("Couldn't determine JobScheduler http url", e);
        }
        Node operations = xPathAnswerXml.selectSingleNode("/spooler/answer/state/operations");
        if (operations != null) {
            NodeList operationsTextChilds = operations.getChildNodes();
            for (int i = 0; i < operationsTextChilds.getLength(); i++) {
                String text = operationsTextChilds.item(i).getNodeValue();
                if (text.contains("Directory_observer")) {
                    Matcher regExMatcher = Pattern.compile(REG_EXP_PATTERN_FOR_LIVE_FOLDER).matcher(text);
                    if (regExMatcher.find()) {
                        liveDirectory = Paths.get(regExMatcher.group(1));
                    }
                } else {
                    liveDirectory = schedulerXmlPath.getParent().resolve("live");
                }
            }
        } else {
            liveDirectory = schedulerXmlPath.getParent().resolve("live");
        }
        setSupervisorFromSchedulerXml();
        if (hibernateConfigReporting != null && !hibernateConfigReporting.isEmpty()) {
            hibernateConfigPath = Paths.get(hibernateConfigReporting);
        } else if (Files.exists(schedulerXmlPath.getParent().resolve(REPORTING_HIBERNATE_CONFIG_PATH_APPENDER))) {
            hibernateConfigPath = schedulerXmlPath.getParent().resolve(REPORTING_HIBERNATE_CONFIG_PATH_APPENDER);
        } else {
            hibernateConfigPath = schedulerXmlPath.getParent().resolve(DEFAULT_HIBERNATE_CONFIG_PATH_APPENDER);
        }
        if (hibernateConfigPath != null) {
            init(hibernateConfigPath);
        } else {
            throw new SOSInventoryPluginException("No hibernate configuration file found!");
        }
    }

    private void init(Path hibernateConfigPath) throws Exception {
        factory = new SOSHibernateFactory(hibernateConfigPath);
        factory.setIdentifier("inventory");
        factory.setAutoCommit(false);
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        factory.addClassMapping(DBLayer.getInventoryClassMapping());
        factory.addClassMapping(DBLayer.getReportingClassMapping());
        factory.build();
    }

    private InventoryModel initInitialInventoryProcessing(DBItemInventoryInstance jsInstanceItem, Path schedulerXmlPath)
            throws Exception {
        model = new InventoryModel(factory, jsInstanceItem, schedulerXmlPath);
        model.setXmlCommandExecutor(xmlCommandExecutor);
        return model;
    }

    private void executeEventBasedInventoryProcessing() throws Exception {
        inventoryEventUpdate = new InventoryEventUpdateUtil(host, port, factory, customEventBus, schedulerXmlPath, schedulerId, hostFromHttpPort);
        inventoryEventUpdate.setXmlCommandExecutor(xmlCommandExecutor);
        inventoryEventUpdate.execute();
    }

    private String executeXML(String xmlCommand) {
        try {
            if (xmlCommandExecutor != null) {
                return xmlCommandExecutor.executeXml(xmlCommand);
            } else {
                LOGGER.error("xmlCommandExecutor is null");
            }
        } catch (Exception e) {}
        return null;
    }

    @Override
    public void close() {
        closeConnections();
        try {
            fixedThreadPoolExecutor.shutdownNow();
            boolean shutdown = fixedThreadPoolExecutor.awaitTermination(1L, TimeUnit.SECONDS);
            if (shutdown) {
                LOGGER.debug("Thread has been shut down correctly.");
            } else {
                LOGGER.debug("Thread has ended due to timeout on shutdown. Doesn´t wait for answer from thread.");
            }
        } catch (InterruptedException e) {
            LOGGER.error(e.toString(), e);
        }
        super.close();
    }
    
    private void setGlobalProperties(SOSXMLXPath xPath) throws Exception {
        schedulerId = xPath.selectSingleNodeValue("/spooler/answer/state/@id");
        host = xPath.selectSingleNodeValue("/spooler/answer/state/@host");
        String httpPort = xPath.selectSingleNodeValue("/spooler/answer/state/@http_port");
        hostFromHttpPort = HttpHelper.getHttpHost(httpPort, "localhost");
        port = HttpHelper.getHttpPort(httpPort);
    }
    
    private void closeConnections() {
        if (inventoryEventUpdate != null) {
            inventoryEventUpdate.setClosed(true);
            try {
                inventoryEventUpdate.getHttpClient().close();
            } catch (IOException e) {
            }
        }
        if (factory != null) {
            factory.close();
        }
    }
    
    private void setSupervisorFromSchedulerXml() throws Exception {
        SOSXMLXPath xPathSchedulerXml = new SOSXMLXPath(schedulerXmlPath);
        String supervisorUrl =
                xPathSchedulerXml.selectSingleNodeValue("/spooler/config/@supervisor");
        if(supervisorUrl != null && !supervisorUrl.isEmpty()) {
            String[] supervisorSplit = supervisorUrl.split(":");
            String determinedHost = supervisorSplit[0];
            supervisorPort = supervisorSplit[1];
            try {
                if ("localhost".equalsIgnoreCase(determinedHost) || "127.0.0.1".equals(determinedHost)) {
                    supervisorHost = InetAddress.getLocalHost().getCanonicalHostName();
                } else {
                    supervisorHost = InetAddress.getByName(determinedHost).getCanonicalHostName();
                }
                if (!supervisorHost.equals(InetAddress.getByName(determinedHost).getHostAddress())
                        && supervisorHost.contains(".")) {
                    String[] split = supervisorHost.split("\\.", 2);
                    supervisorHost = split[0];
                } else if (supervisorHost.equals(InetAddress.getByName(determinedHost).getHostAddress())) {
                    LOGGER.error("Could not determine supervisor host name from given IP address.");
                }
            } catch (UnknownHostException e) {
                LOGGER.error("Could not resolve supervisor host name.", e);
            }
        }
    }
    
}