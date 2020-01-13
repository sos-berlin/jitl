package com.sos.jitl.md5;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

/** \class JobSchedulerMD5FileOptionsJUnitTest - title
 *
 * \brief */

public class JobSchedulerMD5FileOptionsJUnitTest extends JSToolBox {

    private final String conClassName = "JobSchedulerMD5FileOptionsJUnitTest"; //$NON-NLS-1$
    private JobSchedulerMD5File objE = null;

    protected JobSchedulerMD5FileOptions objOptions = null;

    public JobSchedulerMD5FileOptionsJUnitTest() {
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
        objE = new JobSchedulerMD5File();
        objE.registerMessageListener(this);
        objOptions = objE.getOptions();
        objOptions.registerMessageListener(this);

        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    @After
    public void tearDown() throws Exception {
    }

    /** \brief testfile :
     * 
     * \details */
    @Test
    public void testfile() {  // SOSOptionString
        objOptions.file.setValue("++----++");
        assertEquals("", objOptions.file.getValue(), "++----++");

    }

    /** \brief testmd5_suffix :
     * 
     * \details */
    @Test
    public void testmd5_suffix() {  // SOSOptionString
        objOptions.md5_suffix.setValue("++----++");
        assertEquals("", objOptions.md5_suffix.getValue(), "++----++");

    }

    /** \brief testmode :
     * 
     * \details */
    @Test
    public void testmode() {  // SOSOptionString
        objOptions.mode.setValue("++----++");
        assertEquals("", objOptions.mode.getValue(), "++----++");

    }

    /** \brief testresult :
     * 
     * \details */
    @Test
    public void testresult() {  // SOSOptionString
        objOptions.result.setValue("++----++");
        assertEquals("", objOptions.result.getValue(), "++----++");

    }

} // public class JobSchedulerMD5FileOptionsJUnitTest