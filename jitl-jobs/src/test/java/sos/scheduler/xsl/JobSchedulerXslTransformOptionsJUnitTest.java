package sos.scheduler.xsl;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

/** \class JobSchedulerXslTransformOptionsJUnitTest - JobSchedulerXslTransform
 *
 * \brief
 *
 *
 * 
 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\
 * JobSchedulerXslTransform.xml for (more) details.
 * 
 * \verbatim ; mechanicaly created by
 * C:\Users\KB\eclipse\sos.scheduler.xsl\JSJobDoc2JSJUnitOptionSuperClass.xsl
 * from http://www.sos-berlin.com at 20110815114303 \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim private HashMap <String, String> SetJobSchedulerSSHJobOptions
 * (HashMap <String, String> pobjHM) { pobjHM.put
 * ("		JobSchedulerXslTransformOptionsJUnitTest.auth_file", "test"); // This
 * parameter specifies the path and name of a user's pr return pobjHM; } //
 * private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim */
public class JobSchedulerXslTransformOptionsJUnitTest extends JSToolBox {

    private final String conClassName = "JobSchedulerXslTransformOptionsJUnitTest"; //$NON-NLS-1$
    private JobSchedulerXslTransform objE = null;

    protected JobSchedulerXslTransformOptions objOptions = null;

    public JobSchedulerXslTransformOptionsJUnitTest() {
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
        objE = new JobSchedulerXslTransform();
        objE.registerMessageListener(this);
        objOptions = objE.getOptions();
        objOptions.registerMessageListener(this);

        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    @After
    public void tearDown() throws Exception {
    }

    /** \brief testFileName :
     * 
     * \details */
    @Test
    public void testFileName() {  // SOSOptionString
        objOptions.FileName.setValue("++----++");
        assertEquals("", objOptions.FileName.getValue(), "++----++");

    }

    /** \brief testOutputFileName :
     * 
     * \details */
    @Test
    public void testOutputFileName() {  // SOSOptionString
        objOptions.OutputFileName.setValue("++----++");
        assertEquals("", objOptions.OutputFileName.getValue(), "++----++");

    }

    /** \brief testXslFileName :
     * 
     * \details */
    @Test
    public void testXslFileName() {  // SOSOptionString
        objOptions.XslFileName.setValue("++----++");
        assertEquals("", objOptions.XslFileName.getValue(), "++----++");

    }

} // public class JobSchedulerXslTransformOptionsJUnitTest