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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.utils.URIBuilder;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.inventory.db.DBLayerInventory;
import com.sos.jitl.inventory.helper.HttpHelper;
import com.sos.jitl.inventory.helper.InventoryRuntimeHelper;
import com.sos.jitl.inventory.model.InventoryModel;
import com.sos.jitl.reporting.db.DBItemInventoryClusterCalendarUsage;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBItemInventoryJob;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.restclient.JobSchedulerRestApiClient;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;

import sos.xml.SOSXMLXPath;

public class InventoryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryTest.class);
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String APPLICATION_HEADER_VALUE = "application/xml";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String MASTER_WEBSERVICE_URL_APPEND = "/jobscheduler/master/api/command";
    private static final String HOST = "127.0.0.1";
    private static final String HTTP_PORT = "sp.sos:40012";
    private static final String PORT = "40012";
    private static final String SHOW_STATE_COMMAND =
            "<show_state what=\"cluster source job_chains job_chain_orders schedules operations\" />";
    private static final String SHOW_JOB_COMMAND = "<show_job job=\"/shell_worker/shell_worker\" />";
    private String hibernateCfgFile =
            "C:/sp/jobschedulers/approvals/jobscheduler_1.12-SNAPSHOT/sp_4012/config/reporting.hibernate.cfg.xml";
    private Path liveDirectory = Paths.get("C:/sp/jobschedulers/approvals/jobscheduler_1.12-SNAPSHOT/sp_4012/config/live");
    private Path configDirectory = Paths.get("C:/sp/jobschedulers/approvals/jobscheduler_1.12-SNAPSHOT/sp_4012/config");
    private Path schedulerXmlPath = 
            Paths.get("C:/sp/jobschedulers/approvals/jobscheduler_1.12-SNAPSHOT/sp_4012/config/scheduler.xml");
    private String supervisorHost = null;
    private String supervisorPort = null;
    
    @Test
    @Ignore
    public void testEventUpdateExecute() {
        InventoryEventUpdateUtil eventUpdates = null;
        try {
            SOSHibernateFactory factory = new SOSHibernateFactory(hibernateCfgFile);
            factory.setAutoCommit(false);
            factory.addClassMapping(DBLayer.getInventoryClassMapping());
            factory.addClassMapping(DBLayer.getReportingClassMapping());
            factory.build();
            String answerXml = getResponse();
            String httpPort = new SOSXMLXPath(new StringBuffer(getResponse())).selectSingleNodeValue("/spooler/answer/state/@http_port");
            String httpHost = HttpHelper.getHttpHost(httpPort, "127.0.0.1");
            eventUpdates = new InventoryEventUpdateUtil("SP", 40012, factory, schedulerXmlPath, "SP_4012", answerXml, httpPort);
            eventUpdates.execute();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            eventUpdates.setClosed(true);
        }
    }

    @Test
    @Ignore
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
            initialUtil.process(new SOSXMLXPath(new StringBuffer(getResponse())), liveDirectory, Paths.get(hibernateCfgFile), HTTP_PORT);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    @Ignore
    public void testInventoryModelExecute() {
        try {
            SOSHibernateFactory factory = new SOSHibernateFactory(hibernateCfgFile);
            factory.setAutoCommit(false);
            factory.addClassMapping(DBLayer.getInventoryClassMapping());
            factory.addClassMapping(DBLayer.getReportingClassMapping());
            factory.build();
            SOSHibernateSession session = factory.openStatelessSession();
            DBLayerInventory layer = new DBLayerInventory(session);
            DBItemInventoryInstance instance = layer.getInventoryInstance("SP", 40012);
            InventoryModel inventoryModel = new InventoryModel(factory, instance, schedulerXmlPath);
            inventoryModel.setLiveDirectory(liveDirectory);
            inventoryModel.setAnswerXml(getResponse());
            inventoryModel.process();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    @Ignore
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
    @Ignore
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
    @Ignore
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
    @Ignore
    public void testGetAuthFromPrivateConf(){
        Config config = null;
        String schedulerId = "scheduler.1.11.oh";
        // Only for debugging in UnitTest the path of the liveDirectory is needed, at runtime the correct working dir is set
//        Path path = liveDirectory.getParent().resolveSibling(Paths.get("config/private/private.conf")); 
        Path path = Paths.get("C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT1/sp_41110x1/config/private/private.conf"); 
        if (Files.exists(path)) {
            config = ConfigFactory.parseFile(path.toFile());
            String phrase = null;
            try {
                phrase = config.getString("jobscheduler.master.auth.users." + schedulerId);
            } catch (ConfigException e) {
                LOGGER.warn("[inventory] - An credential with the schedulerId as key is missing from configuration item \"jobscheduler.master.auth.users\"!");
                LOGGER.warn("[inventory] - see https://kb.sos-berlin.com/x/NwgCAQ for further details on how to setup a secure connection");
            }
            if (phrase != null && !phrase.isEmpty()) {
                String[] phraseSplit = phrase.split(":", 2);
                byte[] upEncoded = Base64.getEncoder().encode((schedulerId + ":" + phraseSplit[1]).getBytes());
                LOGGER.info(new String(upEncoded));
            }
        } else {
            LOGGER.warn(String.format("[inventory] file %1$s not found!", path.toString()));
        }
    }

    @Test
    @Ignore
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
        connectTo.append("http://").append(HttpHelper.getHttpHost(HTTP_PORT, "127.0.0.1")).append(":").append(HttpHelper.getHttpPort(HTTP_PORT));
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
    @Ignore
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
    
    @Test
    @Ignore
    public void testFilesExistsAndFilesNotExists () {
        Path pathWithoutReadOnlyFlag = Paths.get("C:\\tmp\\testfileohne.txt");
        boolean fileWithoutFlagExist= Files.exists(pathWithoutReadOnlyFlag);
        boolean fileWithoutFlagNotExist = Files.notExists(pathWithoutReadOnlyFlag);
        LOGGER.info("Return value of Files.exists(path of file without readOnly flag): " + fileWithoutFlagExist);
        LOGGER.info("Return value of Files.notExists(path of file without readOnly flag): " + fileWithoutFlagNotExist);

        Path pathWithReadOnlyFlag = Paths.get("C:\\tmp\\testfilemit.txt");
        boolean fileWithFlagExist= Files.exists(pathWithReadOnlyFlag);
        boolean fileWithFlagNotExist = Files.notExists(pathWithReadOnlyFlag);
        LOGGER.info("Return value of Files.exists(path of file with readOnly flag): " + fileWithFlagExist);
        LOGGER.info("Return value of Files.notExists(path of file with readOnly flag): " + fileWithFlagNotExist);
        
        try {
            BasicFileAttributes basicFileAttributes = Files.readAttributes(pathWithReadOnlyFlag, BasicFileAttributes.class);
            LOGGER.info("BasicFileAttributes - size: " + basicFileAttributes.size());
            LOGGER.info("BasicFileAttributes - creationTime: " + basicFileAttributes.creationTime().toString());
            LOGGER.info("BasicFileAttributes - fileKey: " + basicFileAttributes.fileKey());
            LOGGER.info("BasicFileAttributes - isDirectory: " + basicFileAttributes.isDirectory());
            LOGGER.info("BasicFileAttributes - isOther: " + basicFileAttributes.isOther());
            LOGGER.info("BasicFileAttributes - isRegularFile: " + basicFileAttributes.isRegularFile());
            LOGGER.info("BasicFileAttributes - isSymbolicLink: " + basicFileAttributes.isSymbolicLink());
            LOGGER.info("BasicFileAttributes - lastAccessTime: " + basicFileAttributes.lastAccessTime().toString());
            LOGGER.info("BasicFileAttributes - lastModifiedTime: " + basicFileAttributes.lastModifiedTime().toString());
            DosFileAttributes dosFileAttributes = Files.readAttributes(pathWithReadOnlyFlag, DosFileAttributes.class);
            LOGGER.info("DosFileAttributes - isArchive: " + dosFileAttributes.isArchive());
            LOGGER.info("DosFileAttributes - isHidden: " + dosFileAttributes.isHidden());
            LOGGER.info("DosFileAttributes - isReadOnly: " + dosFileAttributes.isReadOnly());
            LOGGER.info("DosFileAttributes - isSystem: " + dosFileAttributes.isSystem());
            FileOwnerAttributeView fileOwnerAttributesView = Files.getFileAttributeView(pathWithReadOnlyFlag, FileOwnerAttributeView.class);
            LOGGER.info("FileOwnerAttributes - Owner: " + fileOwnerAttributesView.getOwner().getName());
            AclFileAttributeView aclFileAttributesView = Files.getFileAttributeView(pathWithReadOnlyFlag, AclFileAttributeView.class);
            for(AclEntry acl : aclFileAttributesView.getAcl()) {
                LOGGER.info(String.format("AclFileAttributes - %1$s: %2$s", acl.principal().getName(), acl.toString()));
            }
            UserDefinedFileAttributeView userDefinedFileAttributesView = Files.getFileAttributeView(pathWithReadOnlyFlag,
                    UserDefinedFileAttributeView.class);
            for(String attribute : userDefinedFileAttributesView.list()) {
                ByteBuffer buf = ByteBuffer.allocate(userDefinedFileAttributesView.size(attribute));
                userDefinedFileAttributesView.read(attribute, buf);
                buf.flip();
                String value = Charset.defaultCharset().decode(buf).toString();
                LOGGER.info(String.format("UserDefinedFileAttributes - %1$s: %2$s", attribute, value));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        Path pathWithoutReadOnlyFlagHidden = Paths.get("C:\\tmp\\testfileohnehidden.txt");
        boolean fileWithoutFlagHiddenExist= Files.exists(pathWithoutReadOnlyFlagHidden);
        boolean fileWithoutFlagHiddenNotExist = Files.notExists(pathWithoutReadOnlyFlagHidden);
        LOGGER.info("Return value of Files.exists(path of file without readOnly flag but hidden): " + fileWithoutFlagHiddenExist);
        LOGGER.info("Return value of Files.notExists(path of file without readOnly flag but hidden): " + fileWithoutFlagHiddenNotExist);

        Path pathWithReadOnlyFlagHidden = Paths.get("C:\\tmp\\testfilemithidden.txt");
        boolean fileWithFlagHiddenExist= Files.exists(pathWithReadOnlyFlagHidden);
        boolean fileWithFlagHiddenNotExist = Files.notExists(pathWithReadOnlyFlagHidden);
        LOGGER.info("Return value of Files.exists(path of file with readOnly flag and hidden): " + fileWithFlagHiddenExist);
        LOGGER.info("Return value of Files.notExists(path of file with readOnly flag and hidden): " + fileWithFlagHiddenNotExist);
    }
    
    @Test
    @Ignore
    public void runTimeHelperTest() throws Exception {
        SOSHibernateSession connection = null;
        SOSHibernateFactory factory = null;
        Path hibernateConfigPath = Paths.get("C:\\ProgramData\\sos-berlin.com\\joc\\jetty_base\\resources\\joc\\reporting.hibernate.cfg.xml");
        factory = new SOSHibernateFactory(hibernateConfigPath);
        factory.setAutoCommit(true);
        factory.addClassMapping(DBLayer.getInventoryClassMapping());
        factory.build();
        connection = factory.openStatelessSession("TEST");
        DBLayerInventory dbLayer = new DBLayerInventory(connection);
        DBItemInventoryJob job = dbLayer.getInventoryJobCaseInsensitive(19L, "/test/echo1");
        
        List<DBItemInventoryClusterCalendarUsage> dbCalendarUsages = dbLayer.getAllCalendarUsagesForObject("scheduler.1.12.oh", "/test/echo1", "JOB");
        InventoryRuntimeHelper.recalculateRuntime(dbLayer, job, dbCalendarUsages, Paths.get("C:/tmp/a"), "Europe/Berlin");
        
        
    }
    
}