package com.sos.jitl.eventing;

import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class JSEventsClientBaseClassTest extends JSEventsClientBaseClass {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JSEventsClientBaseClassTest.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        // BasicConfigurator.configure();
        objO = new JSEventsClientOptions();
        objO.scheduler_event_handler_host.Value("localhost");
        objO.scheduler_event_handler_port.value(4446);
        this.setOptions(objO);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    @Ignore("Not yet implemented")
    public void testSetOptions() {
        // TODO:
    }

    @Test
    @Ignore("Not yet implemented")
    public void testInitialize() {
        // TODO:
    }

    @Test
    @Ignore("Not yet implemented")
    public void testReadEventsFromDB() {
        // TODO:
    }

    @Test
    @Ignore("Not yet implemented")
    public void testGetEventsTableName() {
        // TODO:
    }

    @Test
    @Ignore("Not yet implemented")
    public void testCreateEventsDocument() {
        // TODO:
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testGetEventsFromSchedulerVar() throws Exception {
        Document objDoc = getEventsFromSchedulerVar();
    }

    @Test
    public void testSetEvent() throws Exception {
        objO.operation.Value("add");
        objO.EventClass.Value("kbtest");
        objO.id.Value("TestEvent");
        this.setOptions(objO);

        JSEventsClient objCl = new JSEventsClient();
        objCl.Options(objO);
        objCl.Execute();

        objO.id.Value("TestEvent2");
        objCl.Execute();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testCheckEvent() throws Exception {
        boolean rc = false;
        String eventSpec = "//event[@event_class='kbtest' and (@event_id='TestEvent' and @event_id='TestEvent')]";
        checkEvents(eventSpec);

        eventSpec = "//event[@event_class='kbtest']";
        checkEvents(eventSpec);

        eventSpec = "//event[@event_class='kbtest' and (@event_id='TestEvent' and not(@event_id='TestEvent'))]";
        checkEvents(eventSpec);

        eventSpec = "//event[@event_class='kbtest' and (@event_id='TestEvent' and not(@event_id='TestEvent'))]";
        checkEvents(eventSpec);

        eventSpec = "//event[@event_class='kbtest' and (@event_id='a' and @event_id='b') and  not(@event_id='c')]";
        checkEvents(eventSpec);
    }

    @Test
    public void testDeleteEvent() throws Exception {
        objO.operation.Value("remove");
        objO.EventClass.Value("kbtest");
        objO.id.Value("TestEvent");
        this.setOptions(objO);

        JSEventsClient objCl = new JSEventsClient();
        objCl.Options(objO);
        objCl.Execute();

        objO.id.Value("TestEvent2");
        objCl.Execute();
    }

    private int checkEvents(final String eventSpec) throws Exception {
        boolean rc = false;

        Document eventDocument = getEventsFromSchedulerVar();
        int intNoOfEvents = 0;

        NodeList nodes = XPathAPI.selectNodeList(eventDocument, eventSpec);
        if (nodes == null || nodes.getLength() == 0) {
            logger.info(String.format("*No* matching events '%1$s' were found.", eventSpec));
            rc = false;
        } else {
            intNoOfEvents = nodes.getLength();
            logger.info(String.format("%1$d Match found.", intNoOfEvents));
            rc = true;
        }
        return intNoOfEvents;
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testCreateXPath() throws Exception {
        String strR = "";
        String strExp = "a and b and c";
        strR = createXPath(strExp);
        Assert.assertEquals("", "@event_id='a' and @event_id='b' and @event_id='c'", strR);

        strR = createXPath("a or b or c");
        Assert.assertEquals("", "@event_id='a' or @event_id='b' or @event_id='c'", strR);

        strR = createXPath("(a and b) or c");
        Assert.assertEquals("", "(@event_id='a' and @event_id='b') or @event_id='c'", strR);

        strR = createXPath("(a and b) and not(c)");
        Assert.assertEquals("", "(@event_id='a' and @event_id='b') and  not(@event_id='c')", strR);

        strR = createXPath("((a or b) and c) and not(d)");
        Assert.assertEquals("", "((@event_id='a' or @event_id='b') and @event_id='c') and  not(@event_id='d')", strR);
        checkEvents("//event[@event_class='kbtest' and " + strR + "]");

        strR = createXPath("((a or b) and c) and (not(d) or e)");
        Assert.assertEquals("", "((@event_id='a' or @event_id='b') and @event_id='c') and ( not(@event_id='d') or @event_id='e')", strR);
        checkEvents("//event[@event_class='kbtest' and " + strR + "]");

        strR = createXPath("((a or b) and c) and (not(d or e))");
        Assert.assertEquals("", "((@event_id='a' or @event_id='b') and @event_id='c') and ( not(@event_id='d' or @event_id='e'))", strR);
        checkEvents("//event[@event_class='kbtest' and " + strR + "]");

    }

    private String createXPath(final String pstrString) {

        String[] strA = pstrString.split(" ");
        logger.debug(pstrString);
        boolean flgOpexp = false;
        String strX = "";
        String strKlaZu = "";
        for (String string : strA) {
            string = string.trim();
            if (flgOpexp == true) {
                flgOpexp = false;
                strX += " " + string + " ";
            } else {
                flgOpexp = true;
                while (string.startsWith("(")) {  // ... (not(a) and b)
                    strX += "(";
                    string = string.substring(1);
                }
                if (string.startsWith("not(")) {
                    strX += " not(";
                    string = string.substring(4);
                }
                if (string.startsWith("(")) {
                    while (string.startsWith("(")) {
                        strX += "(";
                        string = string.substring(1);
                    }
                }
                if (string.endsWith(")")) {
                    while (string.endsWith(")")) {
                        strKlaZu += ")";
                        string = string.substring(0, string.length() - 1);
                    }
                }
                strX += "@event_id='" + string + "'" + strKlaZu;
                strKlaZu = "";
            }
        }

        logger.debug(strX);
        return strX;
    }
}
