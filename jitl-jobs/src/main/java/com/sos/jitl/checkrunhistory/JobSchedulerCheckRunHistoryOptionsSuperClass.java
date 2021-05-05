package com.sos.jitl.checkrunhistory;

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
public class JobSchedulerCheckRunHistoryOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = -8442592876516710875L;
    private final String conClassName = "JobSchedulerCheckRunHistoryOptionsSuperClass";
    @I18NMessages(value = { @I18NMessage("The name of the job to check"),
            @I18NMessage(value = "The name of the job to check", locale = "en_UK", explanation = "The name of the job to check"),
            @I18NMessage(value = "Der Name des zu prüfenden Jobs", locale = "de", explanation = "The name of the job to check") },
            msgnum = "JSJ_CRH_0030", msgurl = "msgurl")
    public static final String JSJ_CRH_0030 = "JSJ_CRH_0030";
    @I18NMessages(value = {
            @I18NMessage("Email blind carbon copy address of the recipient, see ./c"),
            @I18NMessage(value = "Email blind carbon copy address of the recipient, see ./c", locale = "en_UK",
                    explanation = "Email blind carbon copy address of the recipient, see ./c"),
            @I18NMessage(value = "Email blind carbon copy address of the recipient, see ./c", locale = "de",
                    explanation = "Email blind carbon copy address of the recipient, see ./c") }, msgnum = "JSJ_CRH_0040", msgurl = "msgurl")
    public static final String JSJ_CRH_0040 = "JSJ_CRH_0040";

    @JSOptionDefinition(name = "result", description = "The result of the check", key = "result", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean result = new SOSOptionBoolean(this, conClassName + ".result", "The result of the check", "false", "false", false);

    public SOSOptionBoolean getresult() {
        return result;
    }

    public void setresult(SOSOptionBoolean p_result) {
        this.result = p_result;
    }

    @JSOptionDefinition(name = "number_of_starts", description = "The number_of_starts of the check", key = "number_of_starts",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString numberOfStarts = new SOSOptionString(this, conClassName + ".number_of_starts", "The numberOfStarts of the check", "", "0",
            false);

    public SOSOptionString getnumberOfStarts() {
        return numberOfStarts;
    }

    public void setnumberOfStarts(SOSOptionString p_numberOfStarts) {
        this.numberOfStarts = p_numberOfStarts;
    }

    @JSOptionDefinition(name = "number_of_completed_successful", description = "The number_of_completed_successful of the check",
            key = "number_of_completed_successful", type = "SOSOptionString", mandatory = false)
    public SOSOptionString numberOfCompletedSuccessful = new SOSOptionString(this, conClassName + ".number_of_completed_successful",
            "The number_of_completed_successful of the check", "", "0", false);

    public SOSOptionString getnumberOfCompletedSuccessful() {
        return numberOfCompletedSuccessful;
    }

    public void setnumberOfCompletedSuccessful(SOSOptionString p_numberOfCompletedSuccessful) {
        this.numberOfCompletedSuccessful = p_numberOfCompletedSuccessful;
    }

    @JSOptionDefinition(name = "number_of_completed_with_error", description = "The number_of_completed_with_error of the check",
            key = "number_of_completed_with_error", type = "SOSOptionString", mandatory = false)
    public SOSOptionString numberOfCompletedWithError = new SOSOptionString(this, conClassName + ".number_of_completed_with_error",
            "The number_of_completed_with_error of the check", "", "0", false);

    public SOSOptionString getnumberOfCompletedWithError() {
        return numberOfCompletedWithError;
    }

    public void setnumberOfCompletedWithError(SOSOptionString p_numberOfCompletedWithError) {
        this.numberOfCompletedWithError = p_numberOfCompletedWithError;
    }

    @JSOptionDefinition(name = "number_of_completed", description = "The number_of_completed of the check", key = "number_of_completed",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString numberOfCompleted = new SOSOptionString(this, conClassName + ".number_of_completed",
            "The number_of_completed of the check", "", "0", false);

    public SOSOptionString getnumberOfCompleted() {
        return numberOfCompleted;
    }

    public void setnumberOfCompleted(SOSOptionString p_numberOfCompleted) {
        this.numberOfCompleted = p_numberOfCompleted;
    }

    @JSOptionDefinition(name = "job_chain_name", description = "The name of a job chain.", key = "job_chain_name", type = "JSJobChainName",
            mandatory = false)
    public JSJobChainName jobChainName = new JSJobChainName(this, conClassName + ".job_chain_name", "The name of a job chain.", "", "", false);

    public JSJobChainName getJobChainName() {
        return jobChainName;
    }

    public void setJobChainName(JSJobChainName p_JobChainName) {
        this.jobChainName = p_JobChainName;
    }

    @JSOptionDefinition(name = "job_name", description = "The name of the job to check", key = "job_name", type = "JSJobName", mandatory = true)
    public JSJobName jobName = new JSJobName(this, conClassName + ".job_name", "The name of the job to check", "", "", false);

    public JSJobName getJobName() {
        return jobName;
    }

    public void setJobName(JSJobName p_JobName) {
        this.jobName = p_JobName;
    }

    @JSOptionDefinition(name = "mail_bcc", description = "Email blind carbon copy address of the recipient, see ./c", key = "mail_bcc",
            type = "JSOptionMailOptions", mandatory = false)
    public JSOptionMailOptions mail_bcc = new JSOptionMailOptions(this, conClassName + ".mail_bcc",
            "Email blind carbon copy address of the recipient", "", "", false);

    public JSOptionMailOptions getmail_bcc() {
        return mail_bcc;
    }

    public void setmail_bcc(JSOptionMailOptions p_mail_bcc) {
        this.mail_bcc = p_mail_bcc;
    }

    @JSOptionDefinition(name = "mail_cc", description = "Email carbon copy address of the recipient, see ./config/", key = "mail_cc",
            type = "JSOptionMailOptions", mandatory = false)
    public JSOptionMailOptions mail_cc = new JSOptionMailOptions(this, conClassName + ".mail_cc",
            "Email carbon copy address of the recipient, see ./config/", "", "", false);

    public JSOptionMailOptions getmail_cc() {
        return mail_cc;
    }

    public void setmail_cc(JSOptionMailOptions p_mail_cc) {
        this.mail_cc = p_mail_cc;
    }

    @JSOptionDefinition(name = "mail_to", description = "Email address of the recipient, see ./config/factory.ini,", key = "mail_to",
            type = "JSOptionMailOptions", mandatory = false)
    public JSOptionMailOptions mail_to = new JSOptionMailOptions(this, conClassName + ".mail_to",
            "Email address of the recipient, see ./config/factory.ini,", "", "", false);

    public JSOptionMailOptions getmail_to() {
        return mail_to;
    }

    public void setmail_to(JSOptionMailOptions p_mail_to) {
        this.mail_to = p_mail_to;
    }

    @JSOptionDefinition(name = "message", description = "Text in the email subject and in the log.", key = "message", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString message = new SOSOptionString(this, conClassName + ".message", "Text in the email subject and in the log.", "", "", false);
    public SOSOptionString Subject = (SOSOptionString) message.setAlias(conClassName + ".Subject");

    public SOSOptionString getmessage() {
        return message;
    }

    public void setmessage(SOSOptionString p_message) {
        this.message = p_message;
    }

    @JSOptionDefinition(name = "query", description = "Query to be executed", key = "query", type = "SOSOptionString", mandatory = true)
    public SOSOptionString query = new SOSOptionString(this, conClassName + ".query", "Query to be executed", "isCompletedAfter", "isCompletedAfter",
            false);

    public SOSOptionString getquery() {
        return query;
    }

    public void setquery(SOSOptionString p_query) {
        this.query = p_query;
    }

    @JSOptionDefinition(name = "fail_on_query_result_false", description = "How to handle false", key = "fail_on_query_result_false",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean failOnQueryResultFalse = new SOSOptionBoolean(this, conClassName + ".fail_on_query_result_false", "How to handle false",
            "true", "true", false);

    public SOSOptionBoolean getfailOnQueryResultFalse() {
        return failOnQueryResultFalse;
    }

    public void setfailOnQueryResultFalse(SOSOptionBoolean p_failOnQueryResultFalse) {
        this.failOnQueryResultFalse = p_failOnQueryResultFalse;
    }

    @JSOptionDefinition(name = "fail_on_query_result_true", description = "How to handle true", key = "fail_on_query_result_true",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean failOnQueryResultTrue = new SOSOptionBoolean(this, conClassName + ".fail_on_query_result_true", "How to handle true",
            "false", "false", false);

    public SOSOptionBoolean getfailOnQueryResultTrue() {
        return failOnQueryResultTrue;
    }

    public void setfailOnQueryResultTrue(SOSOptionBoolean p_failOnQueryResultTrue) {
        this.failOnQueryResultTrue = p_failOnQueryResultTrue;
    }

    @JSOptionDefinition(name = "OrderId", description = "The name or the identification of an order.", key = "OrderId", type = "JSOrderId",
            mandatory = false)
    public JSOrderId orderId = new JSOrderId(this, conClassName + ".OrderId", "The name or the identification of an order.", "", "", false);

    public JSOrderId getOrderId() {
        return orderId;
    }

    public void setOrderId(JSOrderId p_OrderId) {
        this.orderId = p_OrderId;
    }

    @JSOptionDefinition(name = "start_time", description = "The start time from which the parametrisized job is check", key = "start_time",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionTimeHorizon start_time = new SOSOptionTimeHorizon(this, conClassName + ".start_time",
            "The start time from which the parametrisized job is check", "0:00:00:00", "0:00:00:00", false);

    public SOSOptionTimeHorizon getstart_time() {
        return start_time;
    }

    public void setstart_time(SOSOptionTimeHorizon p_start_time) {
        this.start_time = p_start_time;
    }

    @JSOptionDefinition(name = "end_time", description = "The end time from which the parametrisized job is check", key = "end_time",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionTimeHorizon end_time = new SOSOptionTimeHorizon(this, conClassName + ".end_time",
            "The end time from which the parametrisized job is check", "0:00:00:00", "0:00:00:00", false);

    public SOSOptionTimeHorizon getend_time() {
        return end_time;
    }

    public void setend_time(SOSOptionTimeHorizon p_end_time) {
        this.end_time = p_end_time;
    }

    public JobSchedulerCheckRunHistoryOptionsSuperClass() {
        currentClass = this.getClass();
    }

    public JobSchedulerCheckRunHistoryOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerCheckRunHistoryOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
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