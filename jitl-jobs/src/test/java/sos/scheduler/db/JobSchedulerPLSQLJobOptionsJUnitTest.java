package sos.scheduler.db;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

public class JobSchedulerPLSQLJobOptionsJUnitTest extends JSToolBox {

    private JobSchedulerPLSQLJob objE = null;
    protected JobSchedulerPLSQLJobOptions objOptions = null;

    public JobSchedulerPLSQLJobOptionsJUnitTest() {
        //
    }

    @Before
    public void setUp() throws Exception {
        objE = new JobSchedulerPLSQLJob();
        objE.registerMessageListener(this);
        objOptions = objE.getOptions();
        objOptions.registerMessageListener(this);
        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    @Test
    public void testcommand() {
        objOptions.command.Value("++----++");
        assertEquals("Database Commands for the Job. It is possible to define m", objOptions.command.Value(), "++----++");
    }

    @Test
    public void testdb_password() {
        objOptions.db_password.Value("++----++");
        assertEquals("database password", objOptions.db_password.Value(), "++----++");
    }

    @Test
    public void testdb_url() {
        objOptions.db_url.Value("++----++");
        assertEquals("jdbc url (e.g. jdbc:oracle:thin:@localhost:1521:XE)", objOptions.db_url.Value(), "++----++");
    }

    @Test
    public void testdb_user() {
        objOptions.db_user.Value("++----++");
        assertEquals("database user", objOptions.db_user.Value(), "++----++");
    }

    @Test
    public void testexec_returns_resultset() {
        objOptions.exec_returns_resultset.Value("++false++");
        assertEquals("If stored procedures are called which return a result set", objOptions.exec_returns_resultset.Value(), "++false++");
    }

    @Test
    public void testresultset_as_parameters() {
        objOptions.resultset_as_parameters.Value("++false++");
        assertEquals("false No output parameters are generated.", objOptions.resultset_as_parameters.Value(), "++false++");
    }

    @Test
    public void testresultset_as_warning() {
        objOptions.resultset_as_warning.Value("++false++");
        assertEquals("If set to true, a warning will be issued, if the statemen", objOptions.resultset_as_warning.Value(), "++false++");
    }

}