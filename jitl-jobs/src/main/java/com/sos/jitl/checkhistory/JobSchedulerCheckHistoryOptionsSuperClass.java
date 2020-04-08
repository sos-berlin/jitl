package com.sos.jitl.checkhistory;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSJobChainName;
import com.sos.JSHelper.Options.JSJobName;
import com.sos.JSHelper.Options.JSOptionMailOptions;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.JSOrderId;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionTimeHorizon;
import com.sos.i18n.annotation.I18NMessage;
import com.sos.i18n.annotation.I18NMessages;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
@JSOptionClass(name = "JobSchedulerCheckRunHistoryOptionsSuperClass", description = "JobSchedulerCheckRunHistoryOptionsSuperClass")
public class JobSchedulerCheckHistoryOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = -8442592876516710875L;
    private final String conClassName = "JobSchedulerCheckRunHistoryOptionsSuperClass";
    @I18NMessages(value = { @I18NMessage("The name of the job to check"),
            @I18NMessage(value = "The name of the job to check", locale = "en_UK", explanation = "The name of the job to check"),
            @I18NMessage(value = "Der Name des zu prüfenden Jobs", locale = "de", explanation = "The name of the job to check") }, msgnum = "JSJ_CRH_0030", msgurl = "msgurl")
    public static final String JSJ_CRH_0030 = "JSJ_CRH_0030";
    @I18NMessages(value = { @I18NMessage("Email blind carbon copy address of the recipient, see ./c"),
            @I18NMessage(value = "Email blind carbon copy address of the recipient, see ./c", locale = "en_UK", explanation = "Email blind carbon copy address of the recipient, see ./c"),
            @I18NMessage(value = "Email blind carbon copy address of the recipient, see ./c", locale = "de", explanation = "Email blind carbon copy address of the recipient, see ./c") }, msgnum = "JSJ_CRH_0040", msgurl = "msgurl")
    public static final String JSJ_CRH_0040 = "JSJ_CRH_0040";

    @JSOptionDefinition(name = "result", description = "The result of the check", key = "result", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean result = new SOSOptionBoolean(this, conClassName + ".result", "The result of the check", "false", "false", false);

    public SOSOptionBoolean getResult() {
        return result;
    }

    public void setResult(SOSOptionBoolean result) {
        this.result = result;
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

    @JSOptionDefinition(name = "job_chain_name", description = "The name of a job chain.", key = "job_chain_name", type = "JSJobChainName", mandatory = false)
    public JSJobChainName jobChainName = new JSJobChainName(this, conClassName + ".job_chain_name", "The name of a job chain.", "", "", false);

    public JSJobChainName getJobChainName() {
        return jobChainName;
    }

    public void setJobChainName(JSJobChainName p_JobChainName) {
        this.jobChainName = p_JobChainName;
    }

    @JSOptionDefinition(name = "joc_url", description = "The url of JOC Webservices.", key = "joc_url", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jocUrl = new SOSOptionString(this, conClassName + ".joc_url", "The url of JOC Webservices.", "", "", false);

    public SOSOptionString getJocUrl() {
        return jocUrl;
    }

    public void setJocUrl(SOSOptionString jocUrl) {
        this.jocUrl = jocUrl;
    }

    @JSOptionDefinition(name = "job_name", description = "The name of the job to check", key = "job_name", type = "JSJobName", mandatory = true)
    public JSJobName jobName = new JSJobName(this, conClassName + ".job_name", "The name of the job to check", "", "", false);

    public JSJobName getJobName() {
        return jobName;
    }

    public void setJobName(JSJobName jobName) {
        this.jobName = jobName;
    }

    @JSOptionDefinition(name = "mail_bcc", description = "Email blind carbon copy address of the recipient, see ./c", key = "mail_bcc", type = "JSOptionMailOptions", mandatory = false)
    public JSOptionMailOptions mail_bcc = new JSOptionMailOptions(this, conClassName + ".mail_bcc", "Email blind carbon copy address of the recipient", "", "", false);

    public JSOptionMailOptions getMailBcc() {
        return mail_bcc;
    }

    public void setmail_bcc(JSOptionMailOptions mailBcc) {
        this.mail_bcc =mailBcc;
    }

    @JSOptionDefinition(name = "mail_cc", description = "Email carbon copy address of the recipient, see ./config/", key = "mail_cc", type = "JSOptionMailOptions", mandatory = false)
    public JSOptionMailOptions mailCC = new JSOptionMailOptions(this, conClassName + ".mail_cc", "Email carbon copy address of the recipient, see ./config/", "", "", false);

    public JSOptionMailOptions getMailCC() {
        return mailCC;
    }

    public void setMail_cc(JSOptionMailOptions mailCC) {
        this.mailCC = mailCC;
    }

    @JSOptionDefinition(name = "mail_to", description = "Email address of the recipient, see ./config/factory.ini,", key = "mail_to", type = "JSOptionMailOptions", mandatory = false)
    public JSOptionMailOptions mailTo = new JSOptionMailOptions(this, conClassName + ".mail_to", "Email address of the recipient, see ./config/factory.ini,", "", "", false);

    public JSOptionMailOptions getMailTo() {
        return mailTo;
    }

    public void setmail_to(JSOptionMailOptions p_mail_to) {
        this.mailTo = p_mail_to;
    }

    @JSOptionDefinition(name = "message", description = "Text in the email subject and in the log.", key = "message", type = "SOSOptionString", mandatory = false)
    public SOSOptionString message = new SOSOptionString(this, conClassName + ".message", "Text in the email subject and in the log.", "", "", false);
    public SOSOptionString Subject = (SOSOptionString) message.setAlias(conClassName + ".Subject");

    public SOSOptionString getmessage() {
        return message;
    }

    public void setmessage(SOSOptionString p_message) {
        this.message = p_message;
    }

    @JSOptionDefinition(name = "user", description = "User for the Webservice", key = "user", type = "SOSOptionString", mandatory = false)
    public SOSOptionString user = new SOSOptionString(this, conClassName + ".user", "User for the Webservice", "", "", false);

    public SOSOptionString getUser() {
        return user;
    }

    public void setUser(SOSOptionString user) {
        this.user = user;
    }

    @JSOptionDefinition(name = "password", description = "Password for the Webservice", key = "password", type = "SOSOptionString", mandatory = false)
    public SOSOptionString password = new SOSOptionString(this, conClassName + ".password", "Password for the Webservice", "", "", false);

    public SOSOptionString getPassword() {
        return password;
    }

    public void setPassword(SOSOptionString password) {
        this.password = password;
    }

    @JSOptionDefinition(name = "accessToken", description = "Access Token for the Webservice", key = "accessToken", type = "SOSOptionString", mandatory = false)
    public SOSOptionString accessToken = new SOSOptionString(this, conClassName + ".accessToken", "'Access Token for the Webservice", "", "", false);

    public SOSOptionString getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(SOSOptionString accessToken) {
        this.accessToken = accessToken;
    }

    @JSOptionDefinition(name = "query", description = "Query to be executed", key = "query", type = "SOSOptionString", mandatory = true)
    public SOSOptionString query = new SOSOptionString(this, conClassName + ".query", "Query to be executed", "isCompletedAfter", "isCompletedAfter", false);

    public SOSOptionString getquery() {
        return query;
    }

    public void setquery(SOSOptionString p_query) {
        this.query = p_query;
    }

    @JSOptionDefinition(name = "fail_on_query_result_false", description = "How to handle false", key = "fail_on_query_result_false", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean failOnQueryResultFalse = new SOSOptionBoolean(this, conClassName + ".fail_on_query_result_false", "How to handle false", "true", "true", false);

    public SOSOptionBoolean getfailOnQueryResultFalse() {
        return failOnQueryResultFalse;
    }

    public void setfailOnQueryResultFalse(SOSOptionBoolean p_failOnQueryResultFalse) {
        this.failOnQueryResultFalse = p_failOnQueryResultFalse;
    }

    @JSOptionDefinition(name = "fail_on_query_result_true", description = "How to handle true", key = "fail_on_query_result_true", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean failOnQueryResultTrue = new SOSOptionBoolean(this, conClassName + ".fail_on_query_result_true", "How to handle true", "false", "false", false);

    public SOSOptionBoolean getfailOnQueryResultTrue() {
        return failOnQueryResultTrue;
    }

    public void setfailOnQueryResultTrue(SOSOptionBoolean p_failOnQueryResultTrue) {
        this.failOnQueryResultTrue = p_failOnQueryResultTrue;
    }

    @JSOptionDefinition(name = "OrderId", description = "The name or the identification of an order.", key = "OrderId", type = "JSOrderId", mandatory = false)
    public JSOrderId orderId = new JSOrderId(this, conClassName + ".OrderId", "The name or the identification of an order.", "", "", false);

    public JSOrderId getOrderId() {
        return orderId;
    }

    public void setOrderId(JSOrderId p_OrderId) {
        this.orderId = p_OrderId;
    }

    @JSOptionDefinition(name = "start_time", description = "The start time from which the parametrisized job is check", key = "start_time", type = "SOSOptionString", mandatory = false)
    public SOSOptionTimeHorizon start_time = new SOSOptionTimeHorizon(this, conClassName + ".start_time", "The start time from which the parametrisized job is check", "0:00:00:00",
            "0:00:00:00", false);

    public SOSOptionTimeHorizon getstart_time() {
        return start_time;
    }

    public void setstart_time(SOSOptionTimeHorizon p_start_time) {
        this.start_time = p_start_time;
    }

    @JSOptionDefinition(name = "end_time", description = "The end time from which the parametrisized job is check", key = "end_time", type = "SOSOptionString", mandatory = false)
    public SOSOptionTimeHorizon end_time = new SOSOptionTimeHorizon(this, conClassName + ".end_time", "The end time from which the parametrisized job is check", "0:00:00:00",
            "0:00:00:00", false);

    public SOSOptionTimeHorizon getend_time() {
        return end_time;
    }

    public void setend_time(SOSOptionTimeHorizon p_end_time) {
        this.end_time = p_end_time;
    }

    public JobSchedulerCheckHistoryOptionsSuperClass() {
        currentClass = this.getClass();
    }

    public JobSchedulerCheckHistoryOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerCheckHistoryOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    public void setAllOptions(HashMap<String, String> settings) {
        super.setAllOptions(settings);
    }

    @Override
    public void checkMandatory() throws JSExceptionMandatoryOptionMissing, Exception {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void commandLineArgs(String[] pstrArgs) {
        super.commandLineArgs(pstrArgs);
        this.setAllOptions(super.getSettings());
    }

}