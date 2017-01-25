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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.xml.SOSXMLXPath;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.inventory.db.DBLayerInventory;
import com.sos.jitl.inventory.model.InventoryModel;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBLayer;

public class InventoryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryTest.class);
    private String hibernateCfgFile = "C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT3/sp_41110x3/config/hibernate.cfg.xml"; 
    private String answerXml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><spooler><answer time=\"2017-01-19T08:10:21.017Z\"><state "
            + "config_file=\"C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT3/sp_41110x3/config/scheduler.xml\" "
            + "db=\"jdbc -id=spooler -class=org.postgresql.Driver jdbc:postgresql://localhost:5432/scheduler -user=scheduler\" host=\"SP\" "
            + "http_port=\"40117\" https_port=\"47117\" id=\"sp_41110x3\" "
            + "log_file=\"C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT3/sp_41110x3/logs/scheduler-2017-01-19-080427.sp_41110x3.log\" "
            + "loop=\"382\" pid=\"27112\" spooler_id=\"sp_41110x3\" spooler_running_since=\"2017-01-19T08:04:27Z\" state=\"running\" tcp_port=\"4117\" "
            + "time=\"2017-01-19T08:10:21.017Z\" time_zone=\"Europe/Berlin\" udp_port=\"4117\" version=\"1.11.0-RC3\" wait_until=\"2017-01-19T09:12:00.000Z\" "
            + "waits=\"99\"><order_id_spaces/><subprocesses/><remote_schedulers active=\"0\" count=\"0\"/><http_server/><connections/></state></answer></spooler>";
    private Path liveDirectory = Paths.get("C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT3/sp_41110x3/config/live");
    private Path configDirectory = Paths.get("C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT3/sp_41110x3/config");
    
    @Test
    public void testEventUpdateExecute() {
        try {
            SOSHibernateConnection connection = new SOSHibernateConnection(hibernateCfgFile);
            connection.setAutoCommit(true);
            connection.setIgnoreAutoCommitTransactions(true);
            connection.addClassMapping(DBLayer.getInventoryClassMapping());
            connection.connect();
            InventoryEventUpdateUtil eventUpdates = new InventoryEventUpdateUtil("SP", 40117, connection);
            eventUpdates.execute();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    @Test
    public void testInitialProcessingExecute() {
        try {
            SOSHibernateConnection connection = new SOSHibernateConnection(hibernateCfgFile);
            connection.setAutoCommit(true);
            connection.setIgnoreAutoCommitTransactions(true);
            connection.addClassMapping(DBLayer.getInventoryClassMapping());
            connection.setUseOpenStatelessSession(true);
            connection.connect();
            ProcessInitialInventoryUtil initialUtil = new ProcessInitialInventoryUtil(connection);
            initialUtil.setConfigDirectory(configDirectory);
            initialUtil.process(new SOSXMLXPath(new StringBuffer(answerXml)), liveDirectory, Paths.get(hibernateCfgFile), "http://sp.sos:40117");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    @Test
    public void testInventoryModelExecute() {
        try {
            SOSHibernateConnection connection = new SOSHibernateConnection(hibernateCfgFile);
            connection.setAutoCommit(true);
            connection.setIgnoreAutoCommitTransactions(true);
            connection.addClassMapping(DBLayer.getInventoryClassMapping());
            connection.setUseOpenStatelessSession(true);
            connection.connect();
            DBLayerInventory layer = new DBLayerInventory(connection);
            DBItemInventoryInstance instance = layer.getInventoryInstance("SP", 40117);
            InventoryModel inventoryModel = new InventoryModel(connection, instance, Paths.get(configDirectory.toString(), "scheduler.xml"));
            inventoryModel.process();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    
    @Test
    public void testTokenizer(){
        try {
            String schedulerId = "sp_41110x3";
            boolean user = false;
            boolean configuration = false;
            String userVal = null;
            String phrase = null;
            File privateConf = Paths.get("C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT3/sp_41110x3/config/private/private.conf").toFile();
            if(privateConf != null) {
//                StringBuilder strb = new StringBuilder();
                FileInputStream fis = new FileInputStream(privateConf);
                Reader reader = new BufferedReader(new InputStreamReader(fis));
                StreamTokenizer tokenizer = new StreamTokenizer(reader);
                tokenizer.resetSyntax();
                tokenizer.slashStarComments(true);
                tokenizer.slashSlashComments(true);
                tokenizer.eolIsSignificant(false);
                tokenizer.whitespaceChars(0,8);
                tokenizer.whitespaceChars(10,31);
                tokenizer.wordChars(9, 9);
                tokenizer.wordChars(32, 255);
                tokenizer.commentChar('#');
                tokenizer.quoteChar('"');
                tokenizer.quoteChar('\'');
                int ttype       = 0;
//                int lastline    = -1;
//                String s        = "";
                while (ttype != StreamTokenizer.TT_EOF) {
                    ttype = tokenizer.nextToken();
                    String sval = "";
                    switch(ttype) {
                        case StreamTokenizer.TT_WORD : 
                            sval    = tokenizer.sval;
                            if(sval.contains(schedulerId)) {
                                user = true;
                                userVal= sval;
                            } else {
                                user = false;
                            }
                            if(sval.contains("{")) {
                                if ( sval.contains("jobscheduler.master.auth.users")) {
                                    configuration = true;
                                } else {
                                    configuration = false;
                                }
                            }
                            break;
                        case '"':
                            sval    = "\"" + tokenizer.sval + "\"";
                            if(user && configuration) {
                                phrase = sval;
                            }
                            break;
                    }
                }
                phrase = phrase.trim();
                phrase = phrase.substring(1, phrase.length() -1);
                String[] phraseSplit = phrase.split(":");
                if(userVal.replace("=", "").trim().equalsIgnoreCase(schedulerId) && "plain".equalsIgnoreCase(phraseSplit[0])) {
                    byte[] encoded = Base64.getEncoder().encode((schedulerId + ":" + phraseSplit[1]).getBytes());
                    StringBuilder encodedAsString = new StringBuilder();
                    for (byte me : encoded){
                        encodedAsString.append((char)me);
                    }
                    LOGGER.info("userName: " + userVal.replace("=", "").trim() +" | phrase: " + phraseSplit[1] + " : encoded: " + encodedAsString.toString());
                    byte[] decoded = Base64.getDecoder().decode(encoded);
                    StringBuilder decodedAsString = new StringBuilder();
                    for (byte me : decoded){
                        decodedAsString.append((char)me);
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
}
