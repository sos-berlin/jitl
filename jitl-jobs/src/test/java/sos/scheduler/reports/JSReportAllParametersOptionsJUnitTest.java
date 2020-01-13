package sos.scheduler.reports;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

/** \class JSReportAllParametersOptionsJUnitTest - Report all Parameters
 *
 * \brief
 *
 *
 * 
 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\
 * JSReportAllParameters.xml for (more) details.
 * 
 * \verbatim ; mechanicaly created by
 * C:\Users\KB\eclipse\sos.scheduler.xsl\JSJobDoc2JSJUnitOptionSuperClass.xsl
 * from http://www.sos-berlin.com at 20110516150438 \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um f�r einen Test eine HashMap
 * mit sinnvollen Werten f�r die einzelnen Optionen zu erzeugen.
 *
 * \verbatim private HashMap <String, String> SetJobSchedulerSSHJobOptions
 * (HashMap <String, String> pobjHM) { pobjHM.put
 * ("		JSReportAllParametersOptionsJUnitTest.auth_file", "test"); // This
 * parameter specifies the path and name of a user's pr return pobjHM; } //
 * private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim */
public class JSReportAllParametersOptionsJUnitTest extends JSToolBox {

    private final String conClassName = "JSReportAllParametersOptionsJUnitTest"; //$NON-NLS-1$
    private JSReportAllParameters objE = null;

    protected JSReportAllParametersOptions objOptions = null;

    public JSReportAllParametersOptionsJUnitTest() {
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

    /** \brief testReportFileName : The Name of the Report-File. The names and
     * values of all parameters a
     * 
     * \details The Name of the Report-File. The names and values of all
     * parameters are written to this file if a file- (and path)name ist
     * specified. The format of the report is specified by the parameter
     * reportFormat. */
    @Test
    public void testReportFileName() {  // SOSOptionString
        objOptions.ReportFileName.setValue("++----++");
        assertEquals("The Name of the Report-File. The names and values of all parameters a", objOptions.ReportFileName.getValue(), "++----++");

    }

    /** \brief testReportFormat : The Format of the report is specified with this
     * parameter. possbile V
     * 
     * \details The Format of the report is specified with this parameter.
     * possbile Values are 'text', 'xml', ... */
    @Test
    public void testReportFormat() {  // SOSOptionString
        objOptions.ReportFormat.setValue("++text++");
        assertEquals("The Format of the report is specified with this parameter. possbile V", objOptions.ReportFormat.getValue(), "++text++");

    }

} // public class JSReportAllParametersOptionsJUnitTest