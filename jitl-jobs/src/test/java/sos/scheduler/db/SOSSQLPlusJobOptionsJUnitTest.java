package sos.scheduler.db;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

import org.apache.log4j.Logger;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/** \class SOSSQLPlusJobOptionsJUnitTest - Start SQL*Plus client and execute
 * sql*plus programs
 *
 * \brief
 *
 *
 * 
 *
 * see \see
 * R:\backup\sos\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc
 * \SOSSQLPlusJob.xml for (more) details.
 *
 * \verbatim ; mechanicaly created by C:\Users\KB\eclipse -
 * Kopie\xsl\JSJobDoc2JSJUnitOptionSuperClass.xsl from http://www.sos-berlin.com
 * at 20120927164224 \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim private HashMap <String, String> SetJobSchedulerSSHJobOptions
 * (HashMap <String, String> pobjHM) { pobjHM.put
 * ("		SOSSQLPlusJobOptionsJUnitTest.auth_file", "test"); // This parameter
 * specifies the path and name of a user's pr return pobjHM; } // private void
 * SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) \endverbatim */
public class SOSSQLPlusJobOptionsJUnitTest extends JSToolBox {

    @SuppressWarnings("unused")
    private final String conClassName = "SOSSQLPlusJobOptionsJUnitTest";							//$NON-NLS-1$
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(SOSSQLPlusJobOptionsJUnitTest.class);
    private SOSSQLPlusJob objE = null;

    protected SOSSQLPlusJobOptions objOptions = null;

    public SOSSQLPlusJobOptionsJUnitTest() {
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
        objE = new SOSSQLPlusJob();
        objE.registerMessageListener(this);
        objOptions = objE.getOptions();
        objOptions.registerMessageListener(this);

        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    @After
    public void tearDown() throws Exception {
    }

    /** \brief testdb_url : URL for connection to database jdbc url (e.g.
     *
     * \details jdbc url (e.g. jdbc:oracle:thin:@localhost:1521:XE) */
    @Test
    public void testdb_url() { // SOSOptionString
        objOptions.db_url.setValue("++----++");
        assertEquals("URL for connection to database jdbc url (e.g.", objOptions.db_url.getValue(), "++----++");

    }

    /** \brief testcommand_script_file : Script file name to Execute The va
     *
     * \details The value of this parameter contains the file-name (and
     * path-name, if needed) of a local (script-)file, which will be transferred
     * to the remote host and will then be executed there. The script can access
     * job- and order-parameters by environment variables. The names of the
     * environment variables are in upper case and have the string
     * "SCHEDULER_PARAM_" as a prefix. Order parameters with the same name
     * overwrite task parameters. This parameter can be used as an alternative
     * to command , command_delimiter and command_script . */
    @Test
    public void testcommand_script_file() { // SOSOptionInFileName
        objOptions.command_script_file.setValue("++----++");
        assertEquals("Script file name to Execute The va", objOptions.command_script_file.getValue(), "++----++");

    }

    /** \brief testvariable_parser_reg_expr : variable_parser_reg_expr
     *
     * \details */
    @Test
    public void testvariable_parser_reg_expr() { // SOSOptionRegExp
        objOptions.variable_parser_reg_expr.setValue("++----++");
        assertEquals("variable_parser_reg_expr", objOptions.variable_parser_reg_expr.getValue(), "++----++");

    }

    /** \brief testCommand_Line_options : Command_Line_options
     *
     * \details */
    @Test
    public void testCommand_Line_options() { // SOSOptionString
        objOptions.CommandLineOptions.setValue("++-S -L++");
        assertEquals("Command_Line_options", objOptions.CommandLineOptions.getValue(), "++-S -L++");

    }

    /** \brief testdb_password : database password
     *
     * \details database password */
    @Test
    public void testdb_password() { // SOSOptionString
        objOptions.db_password.setValue("++----++");
        assertEquals("database password", objOptions.db_password.getValue(), "++----++");

    }

    /** \brief testdb_user : database user
     *
     * \details database user */
    @Test
    public void testdb_user() { // SOSOptionString
        objOptions.db_user.setValue("++----++");
        assertEquals("database user", objOptions.db_user.getValue(), "++----++");

    }

    /** \brief testinclude_files : IncludeFiles
     *
     * \details */
    @Test
    public void testinclude_files() { // SOSOptionString
        objOptions.include_files.setValue("++----++");
        assertEquals("IncludeFiles", objOptions.include_files.getValue(), "++----++");
    }

    /** \brief testshell_command :
     *
     * \details */
    @Test
    public void testshell_command() { // SOSOptionString
        objOptions.shell_command.setValue("++----++");
        assertEquals("", objOptions.shell_command.getValue(), "++----++");

    }

    /** \brief testsql_error : sql_error
     *
     * \details */
    @Test
    public void testsql_error() { // SOSOptionString
        objOptions.sql_error.setValue("++----++");
        assertEquals("sql_error", objOptions.sql_error.getValue(), "++----++");

    }

    @Test
    public void testignore_sp2_messages() { // SOSOptionStringValueList
        String ignoreSp2MessageValue = "0743";
        objOptions.ignore_sp2_messages.setValue(ignoreSp2MessageValue);
        boolean containsIgnoreSp2MessageValue = objOptions.ignore_sp2_messages.contains(ignoreSp2MessageValue);
        assertEquals("ignore_sp2_messages contains " + ignoreSp2MessageValue + " is expected", true, containsIgnoreSp2MessageValue);
    }

} // public class SOSSQLPlusJobOptionsJUnitTest