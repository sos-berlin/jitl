package com.sos.scheduler.generics;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

/** \class GenericAPIJobJUnitTest - JUnit-Test for "A generic internal API job"
 *
 * \brief MainClass to launch GenericAPIJob as an executable command-line
 * program
 *
 * 
 *
 * see \see
 * C:\Users\KB\AppData\Local\Temp\scheduler_editor-2864692299059909179.html for
 * (more) details.
 *
 * \verbatim ; mechanicaly created by
 * C:\Users\KB\sos-berlin.com\jobscheduler\scheduler
 * \config\JOETemplates\java\xsl\JSJobDoc2JSJUnitClass.xsl from
 * http://www.sos-berlin.com at 20120611173607 \endverbatim */
public class GenericAPIJobJUnitTest extends JSToolBox {

    protected GenericAPIJobOptions objOptions = null;
    private GenericAPIJob objE = null;

    public GenericAPIJobJUnitTest() {
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
        objE = new GenericAPIJob();
        objE.registerMessageListener(this);
        objOptions = objE.getOptions();
        objOptions.registerMessageListener(this);

        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testExecute() throws Exception {

        objE.Execute();

        //		assertEquals ("auth_file", objOptions.auth_file.Value(),"test"); //$NON-NLS-1$
        //		assertEquals ("user", objOptions.user.Value(),"test"); //$NON-NLS-1$

    }
}  // class GenericAPIJobJUnitTest