package com.sos.jitl.inventory.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.utils.URIBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.xml.SOSXMLXPath;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.inventory.db.DBLayerInventory;
import com.sos.jitl.inventory.model.InventoryModel;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.restclient.JobSchedulerRestApiClient;


public class InventoryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryTest.class);
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String APPLICATION_HEADER_VALUE = "application/xml";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String MASTER_WEBSERVICE_URL_APPEND = "/jobscheduler/master/api/command";
    private static final String HOST = "localhost";
    private static final String PORT = "40119";
//    private static final String PORT = "10111";
    private static final String SHOW_STATE_COMMAND =
            "<show_state what=\"cluster source job_chains job_chain_orders schedules operations\" />";
    private static final String SHOW_JOB_COMMAND = "<show_job job=\"/shell_worker/shell_worker\" />";
    private String hibernateCfgFile =
            "C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT1/sp_41110x1/config/reporting.hibernate.cfg.xml";
//    private String hibernateCfgFile =
//            "C:/sp/jobschedulers/approvals/jobscheduler_1.11.1-for_patches/sp_1111/config/reporting.hibernate.cfg.xml";
//    private Path liveDirectory = Paths.get("C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT1/sp_41110x1/config/live");
    private Path liveDirectory = Paths.get("C:/sp/jobschedulers/approvals/jobscheduler_1.11.1-for_patches/sp_1111/config/live");
    private Path configDirectory = Paths.get("C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT1/sp_41110x1/config");
    private Path schedulerXmlPath = 
            Paths.get("C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT1/sp_41110x1/config/scheduler.xml");
    private String supervisorHost = null;
    private String supervisorPort = null;
    
    @Test
    public void testEventUpdateExecute() {
        InventoryEventUpdateUtil eventUpdates = null;
        try {
            SOSHibernateFactory factory = new SOSHibernateFactory(hibernateCfgFile);
            factory.setAutoCommit(false);
            factory.addClassMapping(DBLayer.getInventoryClassMapping());
            factory.build();
            String answerXml = getResponse();
            eventUpdates = new InventoryEventUpdateUtil("SP", 40119, factory, null, schedulerXmlPath, "sp_41110x1", answerXml);
            eventUpdates.execute();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            eventUpdates.setClosed(true);
        }
    }

    @Test
    public void testInitialProcessingExecute() {
        try {
            SOSHibernateFactory factory = new SOSHibernateFactory(hibernateCfgFile);
            factory.setAutoCommit(false);
            factory.addClassMapping(DBLayer.getInventoryClassMapping());
            factory.build();
            ProcessInitialInventoryUtil initialUtil = new ProcessInitialInventoryUtil(factory);
            setSupervisorFromSchedulerXml();
            initialUtil.setSupervisorHost(supervisorHost);
            initialUtil.setSupervisorPort(supervisorPort);
            initialUtil.process(new SOSXMLXPath(new StringBuffer(getResponse())), liveDirectory, Paths.get(hibernateCfgFile),
                    "http://sp:10111");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void testInventoryModelExecute() {
        try {
            SOSHibernateFactory factory = new SOSHibernateFactory(hibernateCfgFile);
            factory.setAutoCommit(false);
            factory.addClassMapping(DBLayer.getInventoryClassMapping());
            factory.build();
            SOSHibernateSession session = factory.openStatelessSession();
            DBLayerInventory layer = new DBLayerInventory(session);
            DBItemInventoryInstance instance = layer.getInventoryInstance("SP", 40119);
            InventoryModel inventoryModel = new InventoryModel(factory, instance, schedulerXmlPath);
            inventoryModel.setAnswerXml(getResponse());
            inventoryModel.process();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void testRuntimeParse() throws Exception {
        SOSXMLXPath xPath = new SOSXMLXPath(new StringBuffer(getResponse(SHOW_JOB_COMMAND)));
        Node runTimeNode = xPath.selectSingleNode("spooler/answer/job/run_time[/* or @schedule]");
        boolean isRuntimeDefined = true;
        if (runTimeNode != null) {
            isRuntimeDefined = runTimeNode.hasChildNodes() || !((Element) runTimeNode).getAttribute("schedule").isEmpty();
        } else {
            isRuntimeDefined = false;
        }
        LOGGER.info("isRuntimeDefined = " + isRuntimeDefined);
    }

    @Test
    public void testExtractLiveFolderFromOperations() throws Exception {
        SOSXMLXPath xPath = new SOSXMLXPath(new StringBuffer(getResponse()));
        Node operations = xPath.selectSingleNode("/spooler/answer/state/operations");
        NodeList operationsTextChilds = operations.getChildNodes();
        for (int i = 0; i < operationsTextChilds.getLength(); i++) {
            String text = operationsTextChilds.item(i).getNodeValue();
            if(text.contains("Directory_observer")) {
                Matcher regExMatcher = Pattern.compile("Directory_observer\\((.*)\\)").matcher(text);
                String liveDirectory = null;
                if (regExMatcher.find()) {
                    liveDirectory = regExMatcher.group(1);
                }
                LOGGER.info("Live Directory: " + liveDirectory);
            }
        }
    }

    @Test
    public void testExtractSupervisorFromOperations() throws Exception {
        SOSXMLXPath xPath = new SOSXMLXPath(new StringBuffer(getResponse()));
        Node operations = xPath.selectSingleNode("/spooler/answer/state/operations");
        if (operations != null) {
            NodeList operationsTextChilds = operations.getChildNodes();
            for (int i = 0; i < operationsTextChilds.getLength(); i++) {
                String text = operationsTextChilds.item(i).getNodeValue();
                if (text.contains("Xml_client_connection")) {
                    Matcher regExMatcher = Pattern.compile("Xml_client_connection\\([^/]*/([^:]+):(\\d+)[^\\)]*\\)").matcher(text);
                    if (regExMatcher.find()) {
                        String supervisorHost = regExMatcher.group(1);
                        String supervisorPort = regExMatcher.group(2);
                        LOGGER.info("supervisor Host = " + supervisorHost);
                        LOGGER.info("supervisor Port = " + supervisorPort);
                    }
                }
            }
        }
    }

    @Test
    public void testOrderBaseName() {
        String baseName = "job_chain1,order_title_with_links";
        baseName = baseName.substring(baseName.lastIndexOf(",") + 1);
        Assert.assertEquals("order_title_with_links", baseName);
        LOGGER.info(baseName);
    }

    @Test
    public void testTokenizer() {
        try {
            String schedulerId = "sp_41110x3";
            boolean user = false;
            boolean configuration = false;
            String userVal = null;
            String phrase = null;
            File privateConf =
                    Paths.get("C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT3/sp_41110x3/config/private/private.conf")
                    .toFile();
            if (privateConf != null) {
                // StringBuilder strb = new StringBuilder();
                FileInputStream fis = new FileInputStream(privateConf);
                Reader reader = new BufferedReader(new InputStreamReader(fis));
                StreamTokenizer tokenizer = new StreamTokenizer(reader);
                tokenizer.resetSyntax();
                tokenizer.slashStarComments(true);
                tokenizer.slashSlashComments(true);
                tokenizer.eolIsSignificant(false);
                tokenizer.whitespaceChars(0, 8);
                tokenizer.whitespaceChars(10, 31);
                tokenizer.wordChars(9, 9);
                tokenizer.wordChars(32, 255);
                tokenizer.commentChar('#');
                tokenizer.quoteChar('"');
                tokenizer.quoteChar('\'');
                int ttype = 0;
                while (ttype != StreamTokenizer.TT_EOF) {
                    ttype = tokenizer.nextToken();
                    String sval = "";
                    switch (ttype) {
                    case StreamTokenizer.TT_WORD:
                        sval = tokenizer.sval;
                        if (sval.contains(schedulerId)) {
                            user = true;
                            userVal = sval;
                        } else {
                            user = false;
                        }
                        if (sval.contains("{")) {
                            if (sval.contains("jobscheduler.master.auth.users")) {
                                configuration = true;
                            } else {
                                configuration = false;
                            }
                        }
                        break;
                    case '"':
                        sval = "\"" + tokenizer.sval + "\"";
                        if (user && configuration) {
                            phrase = sval;
                        }
                        break;
                    }
                }
                phrase = phrase.trim();
                phrase = phrase.substring(1, phrase.length() - 1);
                String[] phraseSplit = phrase.split(":");
                if (userVal.replace("=", "").trim().equalsIgnoreCase(schedulerId) && "plain".equalsIgnoreCase(phraseSplit[0])) {
                    byte[] encoded = Base64.getEncoder().encode((schedulerId + ":" + phraseSplit[1]).getBytes());
                    StringBuilder encodedAsString = new StringBuilder();
                    for (byte me : encoded) {
                        encodedAsString.append((char) me);
                    }
                    LOGGER.info("userName: " + userVal.replace("=", "").trim() + " | phrase: " + phraseSplit[1] + " : encoded: "
                            + encodedAsString.toString());
                    byte[] decoded = Base64.getDecoder().decode(encoded);
                    StringBuilder decodedAsString = new StringBuilder();
                    for (byte me : decoded) {
                        decodedAsString.append((char) me);
                    }
                    LOGGER.info("decoded base64 String: " + decodedAsString.toString());
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found!");
        } catch (IOException e) {
            LOGGER.error("Cannot read from File !");
        }
    }

    private String getResponse() throws Exception {
        return getResponse(SHOW_STATE_COMMAND);
    }
    
    private String getResponse(String command) throws Exception {
        StringBuilder connectTo = new StringBuilder();
        connectTo.append("http://").append(HOST).append(":").append(PORT);
        connectTo.append(MASTER_WEBSERVICE_URL_APPEND);
        URIBuilder uriBuilder = new URIBuilder(connectTo.toString());
        JobSchedulerRestApiClient client = new JobSchedulerRestApiClient();
        client.addHeader(CONTENT_TYPE_HEADER, APPLICATION_HEADER_VALUE);
        client.addHeader(ACCEPT_HEADER, APPLICATION_HEADER_VALUE);
        client.setSocketTimeout(5000);
        String response = client.postRestService(uriBuilder.build(), command);
        return response;
    }

    @Test
    public void testGetSupervisorFromSchedulerXml() throws Exception {
        SOSXMLXPath xPathSchedulerXml = new SOSXMLXPath(schedulerXmlPath);
        String supervisorUrl =
                xPathSchedulerXml.selectSingleNodeValue("/spooler/config/@supervisor");
        String sp = "sp";
        String ipV4 = "192.168.0.51";
        String localhost = "localhost";
        String localhostIp = "127.0.0.1";
        String localhostIpV6 = "::1";
        String ipV6 = "fe80::840a:c2f6:ef11:c26b";
        String canonicalHost = "sp.sos";
        String host = null;
        String supervisorHost = null;
        String supervisorPort = null;
        if(supervisorUrl != null && !supervisorUrl.isEmpty()) {
            String[] supervisorSplit = supervisorUrl.split(":");
            host = supervisorSplit[0];
            LOGGER.info(String.format("Supervisor resolved Hostname for \"%1$s\" is %2$s", sp, getResolvedHostname(sp)));
            LOGGER.info(String.format("Supervisor resolved Hostname for \"%1$s\" is %2$s", localhost, 
                    getResolvedHostname(localhost)));
            LOGGER.info(String.format("Supervisor resolved Hostname for \"%1$s\" (without name resolution) is %2$s", ipV4, 
                    getResolvedHostname(ipV4)));
            LOGGER.info(String.format("Supervisor resolved Hostname for \"%1$s\" (without name resolution) is %2$s", ipV6, 
                    getResolvedHostname(ipV6)));
            LOGGER.info(String.format("Supervisor resolved Hostname for \"%1$s\" is %2$s", localhostIp,
                    getResolvedHostname(localhostIp)));
            LOGGER.info(String.format("Supervisor resolved Hostname for \"%1$s\" is %2$s", localhostIpV6,
                    getResolvedHostname(localhostIpV6)));
            LOGGER.info(String.format("Supervisor resolved Hostname for \"%1$s\" is %2$s", canonicalHost,
                    getResolvedHostname(canonicalHost)));
            supervisorPort = supervisorSplit[1];
        }
        LOGGER.info("Supervisor Port is " + supervisorPort);
    }
    
    private String getResolvedHostname(String hostname) throws UnknownHostException {
        String resolvedHost = null;
        if ("localhost".equalsIgnoreCase(hostname) || "127.0.0.1".equals(hostname)) {
            resolvedHost = InetAddress.getLocalHost().getCanonicalHostName();
        } else {
            resolvedHost = InetAddress.getByName(hostname).getCanonicalHostName();
        }
        if (!resolvedHost.equals(InetAddress.getByName(hostname).getHostAddress()) && resolvedHost.contains(".")) {
            String[] split = resolvedHost.split("\\.", 2);
            resolvedHost = split[0];
        }
        return resolvedHost;
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