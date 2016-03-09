package com.sos.jitl.mail.smtp;

import java.util.HashMap;

import org.apache.log4j.Logger;

import sos.net.mail.options.SOSSmtpMailOptions;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSJobId;
import com.sos.JSHelper.Options.JSJobName;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionPortNumber;

@JSOptionClass(name = "JSSmtpMailOptions", description = "Launch and observe any given job or job chain")
public class JSSmtpMailOptions extends SOSSmtpMailOptions {

    private static final long serialVersionUID = 6441074884525254517L;
    private static final Logger LOGGER = Logger.getLogger(JSSmtpMailOptions.class);
    private String strAlternativePrefix = "";
    public JSSmtpMailOptions objMailOnError = null;
    public JSSmtpMailOptions objMailOnSuccess = null;
    public JSSmtpMailOptions objMailOnJobStart = null;

    public enum enuMailClasses {
        MailDefault, MailOnError, MailOnSuccess, MailOnJobStart;
    }

    public JSSmtpMailOptions() {
    }

    public JSSmtpMailOptions(final JSListener pobjListener) {
        this();
    }

    public JSSmtpMailOptions getOptions(final enuMailClasses penuMailClass) {
        JSSmtpMailOptions objO = objMailOnError;
        switch (penuMailClass) {
        case MailOnError:
            break;
        case MailOnJobStart:
            objO = objMailOnJobStart;
            break;
        case MailOnSuccess:
            objO = objMailOnSuccess;
            break;
        default:
            objO = this;
            break;
        }
        return objO;
    }

    public JSSmtpMailOptions(final HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
        objMailOnError = new JSSmtpMailOptions(JSSettings, "MailOnError_");
        objMailOnSuccess = new JSSmtpMailOptions(JSSettings, "MailOnSuccess_");
        objMailOnJobStart = new JSSmtpMailOptions(JSSettings, "MailOnJobStart_");
    }

    public boolean MailOnJobStart() {
        boolean flgR = false;
        if (objMailOnJobStart == null) {
            objMailOnJobStart = new JSSmtpMailOptions(Settings(), "MailOnJobStart_");
            mergeDefaultSettings(objMailOnJobStart);
        }
        flgR = objMailOnJobStart.to.isDirty();
        return flgR;
    }

    public boolean MailOnError() {
        boolean flgR = false;
        if (objMailOnError == null) {
            objMailOnError = new JSSmtpMailOptions(Settings(), "MailOnError_");
            mergeDefaultSettings(objMailOnError);
        }
        flgR = objMailOnError.to.isDirty();
        return flgR;
    }

    public boolean MailOnSuccess() {
        boolean flgR = false;
        if (objMailOnSuccess == null) {
            objMailOnSuccess = new JSSmtpMailOptions(Settings(), "MailOnSuccess_");
            mergeDefaultSettings(objMailOnSuccess);
        }
        flgR = objMailOnSuccess.to.isDirty();
        return flgR;
    }

    private void mergeDefaultSettings(final JSSmtpMailOptions pobjOpt) {
        if (pobjOpt.host.isNotDirty()) {
            pobjOpt.host.Value(host.Value());
        }
        if (pobjOpt.port.isNotDirty()) {
            pobjOpt.port.Value(port.Value());
        }
        if (pobjOpt.smtp_user.isNotDirty()) {
            pobjOpt.smtp_user.Value(smtp_user.Value());
        }
        if (pobjOpt.smtp_password.isNotDirty()) {
            pobjOpt.smtp_password.Value(smtp_password.Value());
        }
    }

    public JSSmtpMailOptions(final HashMap<String, String> JSSettings, final String pstrPrefix) {
        strAlternativePrefix = pstrPrefix;
        try {
            super.setAllOptions(JSSettings, strAlternativePrefix);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.trace(this.dirtyString());
    }

    @Override
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @JSOptionDefinition(name = "tasklog_to_body", description = "add task log to body", key = "tasklog_to_body", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean tasklog_to_body = new SOSOptionBoolean(this, "tasklog_to_body", "add task log to body", "false", "false", false);

    public SOSOptionBoolean gettasklog_to_body() {
        return tasklog_to_body;
    }

    public void settasklog_to_body(final SOSOptionBoolean p_tasklog_to_body) {
        tasklog_to_body = p_tasklog_to_body;
    }

    @JSOptionDefinition(name = "job_name", description = "job name", key = "job_name", type = "JSJobName", mandatory = false)
    public JSJobName job_name = new JSJobName(this, "job_name", "job name", "", "", false);

    public JSJobName getjob_name() {
        return job_name;
    }

    public void setjob_name(final JSJobName p_job_name) {
        job_name = p_job_name;
    }

    @JSOptionDefinition(name = "job_id", description = "task id of a job", key = "job_id", type = "JSJobId", mandatory = false)
    public JSJobId job_id = new JSJobId(this, "job_id", "task id of a job", "", "", false);

    public JSJobId task_id = (JSJobId) job_id.SetAlias("task_id");

    public JSJobId getjob_id() {
        return job_id;
    }

    public void setjob_id(final JSJobId p_job_id) {
        job_id = p_job_id;
    }

    @JSOptionDefinition(name = "scheduler_host", description = "jobscheduler hostname", key = "scheduler_host", type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName scheduler_host = new SOSOptionHostName(this, "scheduler_host", "jobscheduler hostname", "localhost", "localhost", false);

    public SOSOptionHostName getscheduler_host() {
        return scheduler_host;
    }

    public void setscheduler_host(final SOSOptionHostName p_scheduler_host) {
        scheduler_host = p_scheduler_host;
    }

    @JSOptionDefinition(name = "scheduler_port", description = "jobscheduler port", key = "scheduler_port", type = "SOSOptionPortNumber", mandatory = false)
    public SOSOptionPortNumber scheduler_port = new SOSOptionPortNumber(this, "scheduler_port", "jobscheduler port", "", "", false);

    public SOSOptionPortNumber getscheduler_port() {
        return scheduler_port;
    }

    public void setscheduler_port(final SOSOptionPortNumber p_scheduler_port) {
        scheduler_port = p_scheduler_port;
    }

}
