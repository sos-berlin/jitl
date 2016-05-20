package sos.scheduler.db;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.JSHelper.io.Files.JSTextFile;

// oh 16.04.13 test hangs [SP]
@Ignore("Test set to Ignore for later examination")
public class SOSSQLPlusJobJUnitTest extends JSJobUtilitiesClass<SOSSQLPlusJobOptions> {

    protected SOSSQLPlusJobOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(SOSSQLPlusJobJUnitTest.class);
    private SOSSQLPlusJob objE = null;
    private HashMap<String, String> paramMap = null;
    final String conNL = System.getProperty("line.separator");

    public SOSSQLPlusJobJUnitTest() {
        super(new SOSSQLPlusJobOptions());
    }

    @Before
    public void setUp() throws Exception {
        objE = new SOSSQLPlusJob();
        objE.setJSJobUtilites(this);
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
        objT.put("ConfigurationBaseMonitor.conf_", "check");
        return objT;
    }

    @Ignore
    public void testExecute() throws Exception {
        setOptions();
        String strCmdScript =
                "Set Echo on" + conNL + "WHENEVER SQLERROR EXIT SQL.SQLCODE" + conNL + "WHENEVER OSERROR EXIT FAILURE" + conNL
                        + "selct &SELECT from dual1;" + conNL + "prompt fertig;" + conNL + "prompt db_user = &DB_USER;" + conNL
                        + "prompt SET varname IS varWert;" + conNL + "exit;" + conNL;
        setScript(strCmdScript);
        objOptions.setAllOptions(createMap());
        objE.Execute();
        assertEquals("Variable as expected", "0", paramMap.get("sql_Error"));
    }

    @Test
    public void testExecute2() throws Exception {
        setOptions();
        String strCmdScript =
                "-- initialize the varaible of out parameters" + conNL + "WHENEVER SQLERROR EXIT SQL.SQLCODE" + conNL
                        + "WHENEVER OSERROR EXIT FAILURE" + conNL + "column end_date new_value BCY_DATE" + conNL
                        + "column period new_value PN_YEAR_PERIOD" + conNL + "column period_prev new_value PN_YEAR_PERIOD_PREV" + conNL
                        + "select '0' as end_date from dual;" + conNL + "prompt SET end_date IS &BCY_DATE;" + conNL + "/" + conNL
                        + "select '0' as period from dual;" + conNL + "prompt SET period IS &PN_YEAR_PERIOD;" + conNL + "/" + conNL
                        + "select '0' as period_prev from dual;" + conNL + "prompt SET period_prev IS &PN_YEAR_PERIOD_PREV;" + conNL + "/" + conNL
                        + "prompt SET end_date IS &BCY_DATE;" + conNL + "exit;" + conNL;
        setScript(strCmdScript);
        objOptions.setAllOptions(createMap());
        objE.Execute();
        assertEquals("Variable as expected", "0", paramMap.get("period"));
    }

    @Test
    public void testExecute3() throws Exception {
        setOptions();
        objOptions.ignore_sp2_messages.setValue("0734");
        String strCmdScript =
                "Set Echo on" + conNL + "WHENEVER SQLERROR EXIT SQL.SQLCODE" + conNL + "WHENEVER OSERROR EXIT FAILURE" + conNL
                        + "select &SELECT from dual;" + conNL + "prompt fertig;" + conNL + "prompt set db_user is &DB_USER;" + conNL
                        + "prompt set huhu is &SELECT;" + conNL + "prompt SET varname IS varWert;" + conNL + "exit;" + conNL;
        setScript(strCmdScript);
        objOptions.setAllOptions(createMap());
        objE.Execute();
    }

    @Test
    public void testExecute4() throws Exception {
        setOptions();
        objOptions.ignore_sp2_messages.setValue("0734");
        String strCmdScript =
                "Set Echo on" + conNL + "WHENEVER SQLERROR EXIT SQL.SQLCODE" + conNL + "WHENEVER OSERROR EXIT FAILURE" + conNL
                        + "select &SELECT from dual;" + conNL + "prompt fertig;" + conNL + "prompt set db_user is &DB_USER;" + conNL
                        + "prompt set huhu is &SELECT;" + conNL + "prompt huhu = &SELECT;" + conNL + "prompt SET varname IS varWert;" + conNL
                        + "exit;" + conNL;
        setScript(strCmdScript);
        objOptions.variable_parser_reg_expr.setValue("^\\s*([^=]+)\\s*=\\s*(.*)$");
        objOptions.setAllOptions(createMap());
        objE.Execute();
    }

    @Test
    public void testExecute5() throws Exception {
        setOptions();
        objOptions.ignore_ora_messages.setValue("00942");
        String strCmdScript =
                "Set Echo on" + conNL + "WHENEVER SQLERROR EXIT SQL.SQLCODE" + conNL + "WHENEVER OSERROR EXIT FAILURE" + conNL
                        + "select &SELECT from dual1;" + conNL + "exit;" + conNL;
        setScript(strCmdScript);
        objOptions.setAllOptions(createMap());
        objE.Execute();
    }

    @Test
    public void testExecute6() throws Exception {
        setOptions();
        objOptions.ignore_sp2_messages.setValue("0734");
        String strCmdScript =
                "Set Echo on" + conNL + "WHENEVER SQLERROR EXIT SQL.SQLCODE" + conNL + "WHENEVER OSERROR EXIT FAILURE" + conNL
                        + "selct &SELECT from dual;" + conNL + "exit;" + conNL;
        setScript(strCmdScript);
        objOptions.setAllOptions(createMap());
        objE.Execute();
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    public void testExecute7() throws Exception {
        setOptions();
        objOptions.ignore_sp2_messages.setValue("0734");
        String strCmdScript =
                "Set Echo on" + conNL + "WHENEVER SQLERROR EXIT SQL.SQLCODE" + conNL + "WHENEVER OSERROR EXIT FAILURE" + conNL
                        + "select &SELECT from dual;" + conNL + "exit;" + conNL;
        setScript(strCmdScript);
        objOptions.command_script_file.setValue("file:" + "abcd.ef");
        objOptions.setAllOptions(createMap());
        objE.Execute();
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    public void testExecute8() throws Exception {
        setOptions();
        String strCmdScript =
                "Set Echo on" + conNL + "WHENEVER SQLERROR EXIT SQL.SQLCODE" + conNL + "WHENEVER OSERROR EXIT FAILURE" + conNL
                        + "selct &SELECT from dual;" + conNL + "exit;" + conNL;
        objOptions.command_script_file.setValue(strCmdScript);
        objOptions.setAllOptions(createMap());
        objE.Execute();
    }

    @Test
    public void testJunitSqlPlusIdentifier() throws Exception {
        String s = "012345678901234567890123456789TooLongForSqlPlus";
        s = objE.sqlPlusVariableName(s);
        assertEquals("testJunitSqlPlusIdentifier", "01234567890123456789012345678_", s);
        s = objE.sqlPlusVariableName(s);
        assertEquals("testJunitSqlPlusIdentifier", "01234567890123456789012345678_", s);
    }

    @Ignore
    public void testExecute9() throws Exception {
        setOptions();
        objOptions.Start_Shell_command.setValue("none");
        String strCmdScript =
                "Set Echo on" + conNL + "WHENEVER SQLERROR EXIT SQL.SQLCODE" + conNL + "WHENEVER OSERROR EXIT FAILURE" + conNL
                        + "select &SELECT from dual;" + conNL + "prompt fertig;" + conNL + "prompt set db_user is &DB_USER;" + conNL
                        + "prompt set huhu is &SELECT;" + conNL + "prompt huhu = &SELECT;" + conNL + "prompt SET varname IS varWert;" + conNL
                        + "exit;" + conNL;
        objOptions.command_script_file.setValue(strCmdScript);
        objOptions.setAllOptions(createMap());
        objE.Execute();
    }

    private void setOptions() {
        objOptions.CommandLineOptions.setValue("-S -L");
        objOptions.db_user.setValue("sys");
        objOptions.db_password.setValue("scheduler");
        objOptions.db_url.setValue("localhost as sysdba");
    }

    private void setScript(final String pstrScript) throws Exception {
        String strSQLFileName = File.createTempFile("SOS", "sql").getAbsolutePath();
        JSTextFile objSQL = new JSTextFile(strSQLFileName);
        objSQL.writeLine(pstrScript);
        objSQL.close();
        objOptions.command_script_file.setValue("file:" + strSQLFileName);
    }

    @Override
    public void setJSParam(final String pstrKey, final String pstrValue) {
        if (paramMap == null) {
            paramMap = new HashMap<String, String>();
        }
        paramMap.put(pstrKey, pstrValue);
        LOGGER.debug(String.format("*mock* set param '%1$s' to value '%2$s'", pstrKey, pstrValue));
    }

}