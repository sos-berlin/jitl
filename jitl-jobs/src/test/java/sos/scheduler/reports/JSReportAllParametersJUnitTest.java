package sos.scheduler.reports;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

/** \class JSReportAllParametersJUnitTest - JUnit-Test for
 * "Report all Parameters"
 *
 * \brief MainClass to launch JSReportAllParameters as an executable
 * command-line program
 *
 * 
 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\
 * JSReportAllParameters.xml for (more) details.
 *
 * \verbatim ; mechanicaly created by
 * C:\Users\KB\eclipse\sos.scheduler.xsl\JSJobDoc2JSJUnitClass.xsl from
 * http://www.sos-berlin.com at 20110516150429 \endverbatim */
public class JSReportAllParametersJUnitTest extends JSToolBox {

    protected JSReportAllParametersOptions objOptions = null;
    private JSReportAllParameters objE = null;

    public JSReportAllParametersJUnitTest() {
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
        objE = new JSReportAllParameters();
        objE.registerMessageListener(this);
        objOptions = objE.Options();
        objOptions.registerMessageListener(this);

        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testExecute() throws Exception {

        objE.Execute();

        //		assertEquals ("auth_file", objO.auth_file.Value(),"test"); //$NON-NLS-1$
        //		assertEquals ("user", objO.user.Value(),"test"); //$NON-NLS-1$

    }
}  // class JSReportAllParametersJUnitTest