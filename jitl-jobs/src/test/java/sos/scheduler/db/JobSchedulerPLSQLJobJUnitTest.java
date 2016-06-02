package sos.scheduler.db;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import org.apache.log4j.Logger;
import org.junit.*;

import java.util.HashMap;

public class JobSchedulerPLSQLJobJUnitTest extends JSToolBox {

    protected JobSchedulerPLSQLJobOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerPLSQLJobJUnitTest.class);
    private JobSchedulerPLSQLJob objE = null;

    public JobSchedulerPLSQLJobJUnitTest() {
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

    private HashMap<String, String> createMap() {
        HashMap<String, String> objT = new HashMap<String, String>();
        objT.put("db_user", objOptions.db_user.getValue());
        objT.put("hw", "Hello, world!");
        objT.put("count", "4711");
        objT.put("select", "4711");
        return objT;
    }

    @Test
    public void testExecute() throws Exception {
        objOptions.db_url.setValue("jdbc:oracle:thin:@8of9.sos:1521:test");
        objOptions.db_user.setValue("scheduler");
        objOptions.db_password.setValue("scheduler");
        String strSql =
                "declare " + "\n" + " howmany NUMBER;" + "\n" + "      p_id varchar2(20) := null; " + "\n" + "	  result varchar2(40) := null; "
                        + "\n" + "   v_line scheduler_variables%rowtype;" + "\n" + "begin " + "\n"
                        + "dbms_output.put_line('set variable1 is value1');" + "\n" + "    p_id := '12345'; " + "\n"
                        + " select count(*) into howmany from scheduler_variables;" + "\n"
                        + "dbms_output.put_line('This schema owns ' || howmany || ' tables.');" + "\n"
                        + "dbms_output.put_line('set howmany is ' || howmany);" + "\n" + "dbms_output.put_line('set variable1 is ' || p_id);" + "\n"
                        + "dbms_output.put_line('set variable2 is value2');" + "\n" + "end;" + "\n";
        objOptions.command.setValue(strSql);
        objOptions.setAllOptions(createMap());
        objE.Execute();
        LOGGER.info("objE.getOutput()" + objE.getOutput());
        LOGGER.info("objE.getSqlError()" + objE.getSqlError());
    }

    @Test
    public void testExecute2() throws Exception {
        objOptions.db_url.setValue("jdbc:oracle:thin:@8of9.sos:1521:test");
        objOptions.db_user.setValue("scheduler");
        objOptions.db_password.setValue("scheduler");
        String strSql =
                "declare " + "\n" + " howmany NUMBER;" + "\n" + "      p_id varchar2(20) := null; " + "\n" + "	  result varchar2(40) := null; "
                        + "\n" + "   v_line scheduler_variables%rowtype;" + "\n" + "begin " + "\n" + "dbms_output.put_line('set variable1=value1');"
                        + "\n" + "    p_id := '12345'; " + "\n" + " select count(*) into howmany from scheduler_variables;" + "\n"
                        + "dbms_output.put_line('This schema owns ' || howmany || ' tables.');" + "\n"
                        + "dbms_output.put_line('set howmany=' || howmany);" + "\n" + "dbms_output.put_line('set variable1=' || p_id);" + "\n"
                        + "dbms_output.put_line('set variable.2 = value2');" + "\n" + "dbms_output.put_line('variable_2 = value2');" + "\n" + "end;"
                        + "\n";
        objOptions.command.setValue(strSql);
        objOptions.variable_parser_reg_expr.setValue("^.*?([^= ]+?)\\s*=\\s*(.*)$");
        objOptions.setAllOptions(createMap());
        objE.Execute();
        LOGGER.info("objE.getOutput()" + objE.getOutput());
        LOGGER.info("objE.getSqlError()" + objE.getSqlError());
    }

    @Test
    public void testStringFormat() {
        LOGGER.info(String.format("%%SCHEDULER_PARAM_%1$s%%", "VarName"));
    }

}