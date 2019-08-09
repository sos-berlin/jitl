package com.sos.jitl.eventing;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import org.apache.log4j.Logger;
import org.junit.*;

import static org.junit.Assert.assertEquals;


public class JSEventsClientJUnitTest extends JSToolBox {

    @SuppressWarnings("unused")
    private final static String conClassName = "JSEventsClientJUnitTest";						//$NON-NLS-1$
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JSEventsClientJUnitTest.class);

    protected JSEventsClientOptions objOptions = null;
    private JSEventsClient objE = null;

    public JSEventsClientJUnitTest() {
        //
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        objE = new JSEventsClient();
        objOptions = objE.getOptions();

        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testExecute() throws Exception {

        objOptions.scheduler_event_class.setValue("kb-TestClass");
        objOptions.scheduler_event_id.setValue("kb-Event1");

        objE.execute();

        assertEquals("auth_file", "test", "test"); //$NON-NLS-1$

    }

    @Test
    public void testExecuteLocalHost() throws Exception {

        objOptions.scheduler_event_class.setValue("kb-TestClass");
        objOptions.scheduler_event_id.setValue("kb-Event1");

        objOptions.scheduler_event_handler_host.setValue("8of9.sos");
        objOptions.scheduler_event_handler_port.value(4210);

        objE.execute();

        assertEquals("auth_file", "test", "test"); //$NON-NLS-1$
        //		assertEquals ("user", objOptions.user.Value(),"test"); //$NON-NLS-1$

    }

    @Test
    public void testExecuteMultipleEvents() throws Exception {

        objOptions.scheduler_event_class.setValue("kb-TestClass");
        objOptions.scheduler_event_id.setValue("kb-Event1;kb-Event2;kb-Event3");
        objOptions.del_events.setValue("kb-Event1;kb-Event2;kb-Event3");

        objOptions.scheduler_event_handler_host.setValue("8of9.sos");
        objOptions.scheduler_event_handler_port.value(4210);

        objE.execute();

        assertEquals("auth_file", "test", "test"); //$NON-NLS-1$
        //		assertEquals ("user", objOptions.user.Value(),"test"); //$NON-NLS-1$

    }

} // class JSEventsClientJUnitTest