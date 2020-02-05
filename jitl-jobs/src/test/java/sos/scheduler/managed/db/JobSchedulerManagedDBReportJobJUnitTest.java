package sos.scheduler.managed.db;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

/** \class JobSchedulerManagedDBReportJobJUnitTest - JUnit-Test for
 * "Launch Database Report"
 *
 * \brief MainClass to launch JobSchedulerManagedDBReportJob as an executable
 * command-line program
 *
 * 
 *
 * see \see
 * R:\backup\sos\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc
 * \JobSchedulerManagedDBReportJob.xml for (more) details.
 *
 * \verbatim ; mechanicaly created by
 * C:\Users\KB\eclipse\xsl\JSJobDoc2JSJUnitClass.xsl from
 * http://www.sos-berlin.com at 20120830214522 \endverbatim */
public class JobSchedulerManagedDBReportJobJUnitTest extends JSToolBox {

    @SuppressWarnings("unused")//$NON-NLS-1$
    private final static String conClassName = "JobSchedulerManagedDBReportJobJUnitTest"; //$NON-NLS-1$

    protected JobSchedulerManagedDBReportJobOptions objOptions = null;
    private JobSchedulerManagedDBReportJob objE = null;

    public JobSchedulerManagedDBReportJobJUnitTest() {
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
        objE = new JobSchedulerManagedDBReportJob();
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
    @Ignore("Test set to Ignore for later examination")
    public void testExecute() throws Exception {

        objE.Execute();

        //		assertEquals ("auth_file", objOptions.auth_file.Value(),"test"); //$NON-NLS-1$
        //		assertEquals ("user", objOptions.user.Value(),"test"); //$NON-NLS-1$

    }
}  // class JobSchedulerManagedDBReportJobJUnitTest