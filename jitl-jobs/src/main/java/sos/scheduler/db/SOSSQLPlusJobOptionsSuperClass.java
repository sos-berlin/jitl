package sos.scheduler.db;

import java.util.HashMap;

import com.sos.CredentialStore.SOSCredentialStoreImpl;
import com.sos.CredentialStore.Options.ISOSCredentialStoreOptionsBridge;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod;
import com.sos.JSHelper.Options.SOSOptionCommandString;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionStringValueList;
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.Options.SOSOptionUrl;
import com.sos.JSHelper.Options.SOSOptionUserName;
import com.sos.VirtualFileSystem.Interfaces.ISOSCmdShellOptions;

@JSOptionClass(name = "SOSSQLPlusJobOptionsSuperClass", description = "SOSSQLPlusJobOptionsSuperClass")
public class SOSSQLPlusJobOptionsSuperClass extends JSOptionsClass implements ISOSCmdShellOptions, ISOSCredentialStoreOptionsBridge {

    protected SOSCredentialStoreImpl objCredentialStore = null;
    private static final long serialVersionUID = 7532723066179760236L;
    private final String conClassName = "SOSSQLPlusJobOptionsSuperClass";

    @JSOptionDefinition(name = "Start_Shell_command", description = "Command to start a command shell", key = "Start_Shell_command",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString Start_Shell_command = new SOSOptionString(this, conClassName + ".Start_Shell_command", "Command to start a command shell",
            "", "", false);

    @Override
    public SOSOptionString getStartShellCommand() {
        return Start_Shell_command;
    }

    @Override
    public void setStartShellCommand(final SOSOptionString pstrValue) {
        Start_Shell_command = pstrValue;
    }

    @JSOptionDefinition(name = "OS_Name", description = "Name of Operating-System", key = "OS_Name", type = "SOSOptionString", mandatory = false)
    public SOSOptionString OS_Name = new SOSOptionString(this, conClassName + ".OS_Name", "Name of Operating-System", "", "", false);

    @Override
    public SOSOptionString getOSName() {
        return OS_Name;
    }

    @Override
    public void setOSName(final SOSOptionString pstrValue) {
        OS_Name = pstrValue;
    }

    @JSOptionDefinition(name = "Start_Shell_command_Parameter", description = "Additional Parameters for Shell command",
            key = "Start_Shell_command_Parameter", type = "SOSOptionString", mandatory = false)
    public SOSOptionString Start_Shell_command_Parameter = new SOSOptionString(this, conClassName + ".Start_Shell_command_Parameter",
            "Additional Parameters for Shell command", "", "", false);

    @Override
    public SOSOptionString getStartShellCommandParameter() {
        return Start_Shell_command_Parameter;
    }

    @Override
    public void setStartShellCommandParameter(final SOSOptionString pstrValue) {
        Start_Shell_command_Parameter = pstrValue;
    }

    @JSOptionDefinition(name = "Shell_command_Parameter", description = "Additional Parameters for Shell command", key = "Shell_command_Parameter",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString Shell_command_Parameter = new SOSOptionString(this, conClassName + ".Shell_command_Parameter",
            "Additional Parameters for Shell command", "", "", false);

    @Override
    public SOSOptionString getShellCommandParameter() {
        return Shell_command_Parameter;
    }

    @Override
    public void setShellCommandParameter(final SOSOptionString pstrValue) {
        Shell_command_Parameter = pstrValue;
    }

    @JSOptionDefinition(name = "ignore_ora_messages", description = "Ignore ORA MEssages", key = "ignore_ora_messages",
            type = "SOSOptionStringValueList", mandatory = false)
    public SOSOptionStringValueList ignore_ora_messages = new SOSOptionStringValueList(this, conClassName + ".ignore_ora_messages",
            "Ignore ORA MEssages", "", "", false);

    public String getignore_ora_messages() {
        return ignore_ora_messages.getValue();
    }

    public void setignore_ora_messages(final String pstrValue) {
        ignore_ora_messages.setValue(pstrValue);
    }

    @JSOptionDefinition(name = "ignore_sp2_messages", description = "List of messages to ignore or *all", key = "ignore_sp2_messages",
            type = "SOSOptionValueList", mandatory = false)
    public SOSOptionStringValueList ignore_sp2_messages = new SOSOptionStringValueList(this, conClassName + ".ignore_sp2_messages",
            "List of messages to ignore or *all", "", "", false);

    public String getignore_sp2_messages() {
        return ignore_sp2_messages.getValue();
    }

    public void setignore_sp2_messages(final String pstrValue) {
        ignore_sp2_messages.setValue(pstrValue);
    }

    @JSOptionDefinition(name = "db_url", description = "URL for connection to database jdbc url (e.g.", key = "db_url", type = "SOSOptionString",
            mandatory = true)
    public SOSOptionString db_url =
            new SOSOptionString(this, conClassName + ".db_url", "URL for connection to database jdbc url (e.g.", "", "", true);

    public SOSOptionString getdb_url() {
        return db_url;
    }

    public void setdb_url(final SOSOptionString p_db_url) {
        db_url = p_db_url;
    }

    @JSOptionDefinition(name = "command_script_file", description = "Script file name to Execute The va", key = "command_script_file",
            type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionCommandString command_script_file = new SOSOptionCommandString(this, conClassName + ".command_script_file",
            "Script file name to Execute The va", "", "", true);
    public SOSOptionCommandString sql_script_file = (SOSOptionCommandString) command_script_file.setAlias(conClassName + ".sql_script_file");

    @Override
    public SOSOptionCommandString getCommandScriptFile() {
        return command_script_file;
    }

    @Override
    public void setCommandScriptFile(final SOSOptionCommandString p_command_script_file) {
        command_script_file = p_command_script_file;
    }

    @JSOptionDefinition(name = "variable_parser_reg_expr", description = "variable_parser_reg_expr", key = "variable_parser_reg_expr",
            type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp variable_parser_reg_expr = new SOSOptionRegExp(this, conClassName + ".variable_parser_reg_expr",
            "variable_parser_reg_expr", "^SET\\s+([^\\s]+)\\s*IS\\s+(.*)$", "^SET\\s+([^\\s]+)\\s*IS\\s+(.*)$", false);
    public SOSOptionRegExp VariableParserRegExpr = (SOSOptionRegExp) variable_parser_reg_expr.setAlias(conClassName + ".VariableParserRegExpr");

    public SOSOptionRegExp getvariable_parser_reg_expr() {
        return variable_parser_reg_expr;
    }

    public void setvariable_parser_reg_expr(final SOSOptionRegExp p_variable_parser_reg_expr) {
        variable_parser_reg_expr = p_variable_parser_reg_expr;
    }

    @JSOptionDefinition(name = "Command_Line_options", description = "Command_Line_options", key = "Command_Line_options", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString CommandLineOptions = new SOSOptionString(this, conClassName + ".Command_Line_options", "Command_Line_options", "-S -L",
            "-S -L", false);

    @Override
    public SOSOptionString getCommandLineOptions() {
        return CommandLineOptions;
    }

    @Override
    public void setCommandLineOptions(final SOSOptionString p_Command_Line_options) {
        CommandLineOptions = p_Command_Line_options;
    }

    @JSOptionDefinition(name = "db_password", description = "database password", key = "db_password", type = "SOSOptionString", mandatory = false)
    public SOSOptionPassword db_password = new SOSOptionPassword(this, conClassName + ".db_password", "database password", "", "", false);

    public SOSOptionPassword getdb_password() {
        return db_password;
    }

    public void setdb_password(final SOSOptionPassword p_db_password) {
        db_password = p_db_password;
    }

    @JSOptionDefinition(name = "db_user", description = "database user", key = "db_user", type = "SOSOptionString", mandatory = false)
    public SOSOptionUserName db_user = new SOSOptionUserName(this, conClassName + ".db_user", "database user", "", "", false);

    public SOSOptionUserName getdb_user() {
        return db_user;
    }

    public void setdb_user(final SOSOptionUserName p_db_user) {
        db_user = p_db_user;
    }

    @JSOptionDefinition(name = "include_files", description = "IncludeFiles", key = "include_files", type = "SOSOptionString", mandatory = false)
    public SOSOptionString include_files = new SOSOptionString(this, conClassName + ".include_files", "IncludeFiles", "", "", false);

    public SOSOptionString getinclude_files() {
        return include_files;
    }

    public void setinclude_files(final SOSOptionString p_include_files) {
        include_files = p_include_files;
    }

    @JSOptionDefinition(name = "shell_command", description = "", key = "shell_command", type = "SOSOptionString", mandatory = false)
    public SOSOptionString shell_command = new SOSOptionString(this, conClassName + ".shell_command", "", "sqlplus", "sqlplus", false);

    @Override
    public SOSOptionString getShellCommand() {
        return shell_command;
    }

    @Override
    public void setShellCommand(final SOSOptionString p_shell_command) {
        shell_command = p_shell_command;
    }

    @JSOptionDefinition(name = "sql_error", description = "sql_error", key = "sql_error", type = "SOSOptionString", mandatory = false)
    public SOSOptionString sql_error = new SOSOptionString(this, conClassName + ".sql_error", "sql_error", "", "", false);

    public SOSOptionString getsql_error() {
        return sql_error;
    }

    public void setsql_error(final SOSOptionString p_sql_error) {
        sql_error = p_sql_error;
    }

    public SOSSQLPlusJobOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public SOSSQLPlusJobOptionsSuperClass(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public SOSSQLPlusJobOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    @Override
    public void setAllOptions(final HashMap<String, String> pobjJSSettings) {
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.setSettings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
    }

    @Override
    public void checkMandatory() throws JSExceptionMandatoryOptionMissing, Exception {
        try {
            getCredentialStore().checkCredentialStoreOptions();
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void commandLineArgs(final String[] pstrArgs) {
        super.commandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }

    public SOSCredentialStoreImpl getCredentialStore() {
        if (objCredentialStore == null) {
            objCredentialStore = new SOSCredentialStoreImpl(this);
        }
        return objCredentialStore;
    }

    public void setChildClasses(final HashMap<String, String> pobjJSSettings, final String pstrPrefix) throws Exception {
        getCredentialStore().setChildClasses(pobjJSSettings, pstrPrefix);
        objCredentialStore.checkCredentialStoreOptions();
    }

    @Override
    public SOSOptionUrl getUrl() {
        return null;
    }

    @Override
    public void setUrl(final SOSOptionUrl pstrValue) {
    }

    @Override
    public SOSOptionHostName getHost() {
        return null;
    }

    @Override
    public void setHost(final SOSOptionHostName p_host) {
    }

    @Override
    public SOSOptionPortNumber getPort() {
        return null;
    }

    @Override
    public void setPort(final SOSOptionPortNumber p_port) {
    }

    @Override
    public SOSOptionTransferType getProtocol() {
        return null;
    }

    @Override
    public void setProtocol(final SOSOptionTransferType p_protocol) {
    }

    @Override
    public SOSOptionUserName getUser() {
        return null;
    }

    @Override
    public SOSOptionPassword getPassword() {
        return null;
    }

    @Override
    public void setPassword(final SOSOptionPassword p_password) {

    }

    @Override
    public SOSOptionInFileName getAuthFile() {
        return null;
    }

    @Override
    public void setAuthFile(final SOSOptionInFileName p_ssh_auth_file) {

    }

    @Override
    public SOSOptionAuthenticationMethod getAuthMethod() {
        return null;
    }

    @Override
    public void setAuthMethod(final SOSOptionAuthenticationMethod p_ssh_auth_method) {

    }

    @Override
    public void setUser(final SOSOptionUserName pobjUser) {

    }

}