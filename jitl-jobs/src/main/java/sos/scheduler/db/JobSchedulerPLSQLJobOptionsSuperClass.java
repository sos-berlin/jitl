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
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.Options.SOSOptionUrl;
import com.sos.JSHelper.Options.SOSOptionUserName;

@JSOptionClass(name = "JobSchedulerPLSQLJobOptionsSuperClass", description = "JobSchedulerPLSQLJobOptionsSuperClass")
public class JobSchedulerPLSQLJobOptionsSuperClass extends JSOptionsClass implements ISOSCredentialStoreOptionsBridge {

    protected SOSCredentialStoreImpl objCredentialStore = null;
    private static final long serialVersionUID = 1L;
    private final String conClassName = "JobSchedulerPLSQLJobOptionsSuperClass";

    @JSOptionDefinition(name = "command", description = "Database Commands for the Job. It is possible to define m", key = "command",
            type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString command = new SOSOptionCommandString(this, conClassName + ".command",
            "Database Commands for the Job. It is possible to define m", "", "", false);
    public SOSOptionCommandString sql_command = (SOSOptionCommandString) command.setAlias("sql_command");

    public SOSOptionCommandString getcommand() {
        return command;
    }

    public void setcommand(final SOSOptionCommandString p_command) {
        command = p_command;
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

    @JSOptionDefinition(name = "db_password", description = "database password", key = "db_password", type = "SOSOptionString", mandatory = false)
    public SOSOptionString db_password = new SOSOptionString(this, conClassName + ".db_password", "database password", " ", " ", false);

    public SOSOptionString getdb_password() {
        return db_password;
    }

    public void setdb_password(final SOSOptionString p_db_password) {
        db_password = p_db_password;
    }

    @JSOptionDefinition(name = "db_url", description = "jdbc url (e.g. jdbc:oracle:thin:@localhost:1521:XE)", key = "db_url",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString db_url = new SOSOptionString(this, conClassName + ".db_url", "jdbc url (e.g. jdbc:oracle:thin:@localhost:1521:XE)", " ",
            " ", false);

    public SOSOptionString getdb_url() {
        return db_url;
    }

    public void setdb_url(final SOSOptionString p_db_url) {
        db_url = p_db_url;
    }

    @JSOptionDefinition(name = "db_user", description = "database user", key = "db_user", type = "SOSOptionString", mandatory = false)
    public SOSOptionString db_user = new SOSOptionString(this, conClassName + ".db_user", "database user", " ", " ", false);

    public SOSOptionString getdb_user() {
        return db_user;
    }

    public void setdb_user(final SOSOptionString p_db_user) {
        db_user = p_db_user;
    }
    
    
    @JSOptionDefinition(name = "credential_store_file", description = "", key = "credential_store_file",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString credential_store_file = new SOSOptionString(this, conClassName + ".credential_store_file", "", "",
            "", false);

    public SOSOptionString getcredential_store_file() {
        return credential_store_file;
    }

    public void setcredential_store_file(final SOSOptionString p_credential_store_file) {
        credential_store_file = p_credential_store_file;
    }
    
    @JSOptionDefinition(name = "credential_store_key_file", description = "", key = "credential_store_key_file",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString credential_store_key_file = new SOSOptionString(this, conClassName + ".credential_store_key_file", "", "",
            "", false);

    public SOSOptionString getcredential_store_key_file() {
        return credential_store_key_file;
    }

    public void setcredential_store_key_file(final SOSOptionString p_credential_store_key_file) {
        credential_store_key_file = p_credential_store_key_file;
    }    
    
    @JSOptionDefinition(name = "credential_store_password", description = "", key = "credential_store_password",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString credential_store_password = new SOSOptionString(this, conClassName + ".credential_store_password", "", "",
            "", false);

    public SOSOptionString getcredential_store_password() {
        return credential_store_password;
    }

    public void setcredential_store_password(final SOSOptionString p_credential_store_password) {
        credential_store_password = p_credential_store_password;
    }    
        
    @JSOptionDefinition(name = "credential_store_entry_path", description = "", key = "credential_store_entry_path",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString credential_store_entry_path = new SOSOptionString(this, conClassName + ".credential_store_entry_path", "", "",
            "", false);

    public SOSOptionString getcredential_store_entry_path() {
        return credential_store_entry_path;
    }

    public void setcredential_store_entry_path(final SOSOptionString p_credential_store_entry_path) {
        credential_store_entry_path = p_credential_store_entry_path;
    }    

    @JSOptionDefinition(name = "exec_returns_resultset", description = "If stored procedures are called which return a result set",
            key = "exec_returns_resultset", type = "SOSOptionString", mandatory = false)
    public SOSOptionString exec_returns_resultset = new SOSOptionString(this, conClassName + ".exec_returns_resultset",
            "If stored procedures are called which return a result set", "false", "false", false);

    public SOSOptionString getexec_returns_resultset() {
        return exec_returns_resultset;
    }

    public void setexec_returns_resultset(final SOSOptionString p_exec_returns_resultset) {
        exec_returns_resultset = p_exec_returns_resultset;
    }

    @JSOptionDefinition(name = "resultset_as_parameters", description = "false No output parameters are generated.", key = "resultset_as_parameters",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString resultset_as_parameters = new SOSOptionString(this, conClassName + ".resultset_as_parameters",
            "false No output parameters are generated.", "false", "false", false);

    public SOSOptionString getresultset_as_parameters() {
        return resultset_as_parameters;
    }

    public void setresultset_as_parameters(final SOSOptionString p_resultset_as_parameters) {
        resultset_as_parameters = p_resultset_as_parameters;
    }

    @JSOptionDefinition(name = "resultset_as_warning", description = "If set to true, a warning will be issued, if the statemen",
            key = "resultset_as_warning", type = "SOSOptionString", mandatory = false)
    public SOSOptionString resultset_as_warning = new SOSOptionString(this, conClassName + ".resultset_as_warning",
            "If set to true, a warning will be issued, if the statemen", "false", "false", false);

    public SOSOptionString getresultset_as_warning() {
        return resultset_as_warning;
    }

    public void setresultset_as_warning(final SOSOptionString p_resultset_as_warning) {
        resultset_as_warning = p_resultset_as_warning;
    }

    public JobSchedulerPLSQLJobOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JobSchedulerPLSQLJobOptionsSuperClass(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerPLSQLJobOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    @Override
    public void setAllOptions(HashMap<String, String> settings) {
        super.setAllOptions(settings);
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
        this.setAllOptions(super.getSettings());
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