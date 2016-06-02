package com.sos.jitl.textprocessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import sos.util.SOSStandardLogger;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

/** \class JobSchedulerTextProcessorOptionsJUnitTest - Diverse Funktionen auf
 * Textdateien
 *
 * \brief */

public class JobSchedulerTextProcessorOptionsJUnitTest extends JSToolBox {

    private final String conClassName = "JobSchedulerTextProcessorOptionsJUnitTest";
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JobSchedulerTextProcessorOptionsJUnitTest.class);
    private JobSchedulerTextProcessor objE = null;

    protected JobSchedulerTextProcessorOptions objOptions = null;

    public JobSchedulerTextProcessorOptionsJUnitTest() {
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
        objE = new JobSchedulerTextProcessor();
        objE.registerMessageListener(this);
        objOptions = objE.getOptions();
        objOptions.registerMessageListener(this);

        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;

    }

    @After
    public void tearDown() throws Exception {
    }

    /** \brief testcommand :
     * 
     * \details Command: count: counts the hits of a string add: adds a string
     * at the end of the file. read: reads line -n. Possible value for n are
     * numbers and first/last The command can contain the param. Samples: count
     * test add xxxx read 6 read last */
    @Test
    public void testcommand() {  // SOSOptionString
        objOptions.command.setValue("++----++");
        assertEquals("", objOptions.command.getValue(), "++----++");

    }

    /** \brief testfilename :
     * 
     * \details Name of the file. */
    @Test
    public void testfilename() {  // SOSOptionString
        objOptions.filename.setValue("++----++");
        assertEquals("", objOptions.filename.getValue(), "++----++");

    }

    /** \brief testparam :
     * 
     * \details */
    @Test
    public void testparam() {  // SOSOptionString
        objOptions.param.setValue("++----++");
        assertEquals("", objOptions.param.getValue(), "++----++");

    }

    /** \brief testscheduler_textprocessor_result :
     * 
     * \details Command: Return value: count: counted number of char
     * countCaseSensitive: counted number of char add: param read: the readed
     * line insert: param */
    @Test
    public void testscheduler_textprocessor_result() {  // SOSOptionString
        objOptions.scheduler_textprocessor_result.setValue("++----++");
        assertEquals("", objOptions.scheduler_textprocessor_result.getValue(), "++----++");

    }

} // public class JobSchedulerTextProcessorOptionsJUnitTest