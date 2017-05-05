package com.sos.jitl.inventory.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
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
    private static final String SHOW_STATE_COMMAND = "<show_state what=\"cluster source job_chains job_chain_orders schedules operations\" />";
    private String hibernateCfgFile = "C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT2/sp_41110x2/config/reporting.hibernate.cfg.xml";
    private String answerXml =
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><spooler><answer time=\"2017-01-19T08:10:21.017Z\"><state "
                    + "config_file=\"C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT1/sp_41110x1/config/scheduler.xml\" "
                    + "db=\"jdbc -id=spooler -class=org.postgresql.Driver jdbc:postgresql://localhost:5432/scheduler -user=scheduler\" host=\"SP\" "
                    + "http_port=\"40119\" id=\"sp_41110x1\" "
                    + "log_file=\"C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT3/sp_41110x1/logs/scheduler-2017-01-19-080427.sp_41110x1.log\" "
                    + "loop=\"382\" pid=\"27112\" spooler_id=\"sp_41110x1\" spooler_running_since=\"2017-01-19T08:04:27Z\" state=\"running\" tcp_port=\"4119\" "
                    + "time=\"2017-01-19T08:10:21.017Z\" time_zone=\"Europe/Berlin\" udp_port=\"4117\" version=\"1.11.0-SNAPSHOT\" wait_until=\"2017-01-19T09:12:00.000Z\" "
                    + "waits=\"99\"><order_id_spaces/><subprocesses/><remote_schedulers active=\"0\" count=\"0\"/><http_server/><connections/></state></answer></spooler>";
    private String msSQLAnswerXml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><spooler><answer time=\"2017-04-11T11:58:43.177Z\">"
            + "<state config_file=\"C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT4/sp_41110x4/config/scheduler.xml\" db=\"jdbc "
            + "-id=spooler -class=net.sourceforge.jtds.jdbc.Driver jdbc:jtds:sqlserver://SP:1433;instance=SQLEXPRESS;"
            + "sendStringParametersAsUnicode=false;selectMethod=cursor;databaseName=scheduler -user=scheduler\" host=\"SP\" "
            + "http_port=\"40116\" id=\"sp_41110x4\" log_file=\"C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT4/sp_41110x4"
            + "/logs/scheduler-2017-04-11-115351.sp_41110x4.log\" loop=\"216\" pid=\"25768\" spooler_id=\"sp_41110x4\" "
            + "spooler_running_since=\"2017-04-11T11:53:51Z\" state=\"running\" tcp_port=\"4116\" time=\"2017-04-11T11:58:43.178Z\" "
            + "time_zone=\"Europe/Berlin\" udp_port=\"4116\" version=\"1.11.1\" wait_until=\"2017-04-11T22:00:00.000Z\" waits=\"52\">"
            + "</state></answer></spooler>";
    private String showJobAnswerXml1 = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><spooler><answer time=\"2017-03-29T12:01:59.146Z\">"
            + "<job all_steps=\"0\" all_tasks=\"0\" enabled=\"yes\" in_period=\"yes\" job=\"shell_worker\" job_chain_priority=\"1\" "
            + "log_file=\"C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT1/sp_41110x1/logs/job.shell_worker,shell_worker.log\" "
            + "name=\"shell_worker\" order=\"yes\" path=\"/shell_worker/shell_worker\" state=\"pending\" tasks=\"1\">"
            + "<file_based file=\"C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT1/sp_41110x1/config/live/shell_worker/shell_worker."
            + "job.xml\" last_write_time=\"2017-02-22T11:18:20.000Z\" state=\"active\"><requisites/></file_based><run_time>\r\n</run_time>"
            + "<tasks count=\"0\"/><queued_tasks length=\"0\"/><order_queue length=\"1\"/><log highest_level=\"info\" "
            + "last_info=\"SCHEDULER-893  Job is 'active' now\" level=\"info\" mail_from=\"scheduler_mySQL@SP\" "
            + "mail_on_error=\"yes\" mail_on_warning=\"yes\" mail_to=\"sp@sos-berlin.com\" smtp=\"mail.sos-berlin.com\"/>"
            + "</job></answer></spooler>";
    private String showStateAnswerXmlForLiveFolder =
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><spooler><answer time=\"2017-04-19T07:26:08.124Z\">"
                    + "<state config_file=\"C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT1/sp_41110x1/config/scheduler.xml\" db=\"jdbc "
                    + "-id=spooler -class=org.postgresql.Driver jdbc:postgresql://localhost:5432/scheduler -user=scheduler\" host=\"SP\" "
                    + "http_port=\"40119\" id=\"SP_41110x1\" log_file=\"C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT1/sp_41110x1/logs"
                    + "/scheduler-2017-04-19-060556.SP_41110x1.log\" loop=\"1536\" pid=\"9960\" spooler_id=\"SP_41110x1\" "
                    + "spooler_running_since=\"2017-04-19T06:05:56Z\" state=\"running\" tcp_port=\"4119\" time=\"2017-04-19T07:26:08.126Z\" "
                    + "time_zone=\"Europe/Berlin\" udp_port=\"4119\" version=\"1.11.2-SNAPSHOT\" "
                    + "version_commit_hash=\"95eb53f5ba7d39af41b6a487e9437f71450e975d\" wait_until=\"2017-04-19T22:00:00.000Z\" waits=\"720\">"
                    + "<operations>"
                    + "Socket_manager()\n"
                    + "Directory_observer(C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT1/sp_41110x1/config/live2), at 2017-04-19 07:27:06 UTC \n"
                    + "Directory_file_order_source(\"jade_history\", \"\\.csv$\"), at 2017-04-19 07:27:06 UTC \n"
                    + "Directory_file_order_source(\"C:/temp/file_watcher_test\",\"^abc.*.txt$\"), at 2017-04-19 07:27:06 UTC \n"
                    + "Directory_file_order_source(\"C:/sp/incoming/jobchain1\",\".*\"), at 2017-04-19 07:27:06 UTC \n"
                    + "Directory_file_order_source(\"C:/temp/file_watcher_test\",\"^test.*.txt$\"), at 2017-04-19 07:27:07 UTC\n" + "</operations>"
                    + "<java_subsystem DeleteGlobalRef=\"225491\" GlobalRef=\"2482\" NewGlobalRef=\"227972\"></java_subsystem></state></answer>"
                    + "</spooler>";
     private Path liveDirectory = Paths.get("C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT2/sp_41110x2/config/live");
     private Path configDirectory = Paths.get("C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT2/sp_41110x2/config");

    @Test
    public void testEventUpdateExecute() {
        InventoryEventUpdateUtil eventUpdates = null;
        try {
            SOSHibernateFactory factory = new SOSHibernateFactory(hibernateCfgFile);
            factory.setAutoCommit(false);
            factory.addClassMapping(DBLayer.getInventoryClassMapping());
            factory.build();
            eventUpdates = new InventoryEventUpdateUtil("SP", 40118, factory, null, Paths.get(configDirectory.toString(), "scheduler.xml"));
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
            // SOSHibernateSession session = factory.openStatelessSession();

            ProcessInitialInventoryUtil initialUtil = new ProcessInitialInventoryUtil(factory);
            initialUtil.process(new SOSXMLXPath(new StringBuffer(msSQLAnswerXml)), liveDirectory, Paths.get(hibernateCfgFile), "http://sp:40116");
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
            DBItemInventoryInstance instance = layer.getInventoryInstance("SP", 40118);
            InventoryModel inventoryModel = new InventoryModel(factory, instance, Paths.get(configDirectory.toString(), "scheduler.xml"));
            inventoryModel.setAnswerXml(getResponse());
            inventoryModel.process();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void testRuntimeParse() throws Exception {
        SOSXMLXPath xPath = new SOSXMLXPath(new StringBuffer(showJobAnswerXml1));
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
        SOSXMLXPath xPath = new SOSXMLXPath(new StringBuffer(showStateAnswerXmlForLiveFolder));
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
            File privateConf = Paths.get("C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT3/sp_41110x3/config/private/private.conf").toFile();
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
        StringBuilder connectTo = new StringBuilder();
        connectTo.append("http://").append(HOST).append(":").append(PORT);
        connectTo.append(MASTER_WEBSERVICE_URL_APPEND);
        URIBuilder uriBuilder = new URIBuilder(connectTo.toString());
        JobSchedulerRestApiClient client = new JobSchedulerRestApiClient();
        client.addHeader(CONTENT_TYPE_HEADER, APPLICATION_HEADER_VALUE);
        client.addHeader(ACCEPT_HEADER, APPLICATION_HEADER_VALUE);
        client.setSocketTimeout(5000);
        String response = client.postRestService(uriBuilder.build(), SHOW_STATE_COMMAND);
        return response;
    }
}