package com.sos.jitl.housekeeping.dequeuemail;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionString;

@JSOptionClass(name = "JobSchedulerDequeueMailJobOptionsSuperClass", description = "JobSchedulerDequeueMailJobOptionsSuperClass")
public class JobSchedulerDequeueMailJobOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private final String conClassName = "JobSchedulerDequeueMailJobOptionsSuperClass";

    @JSOptionDefinition(name = "db", description = "", key = "db", type = "SOSOptionString", mandatory = false)
    public SOSOptionString db = new SOSOptionString(this, conClassName + ".db", "", "false", "false", false);

    public SOSOptionString getdb() {
        return db;
    }

    public void setdb(SOSOptionString p_db) {
        this.db = p_db;
    }

    @JSOptionDefinition(name = "failed_prefix", description = "prefix for failed mail files", key = "failed_prefix", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString failed_prefix = new SOSOptionString(this, conClassName + ".failed_prefix", "prefix for failed mail files", "failed.",
            "failed.", false);

    public SOSOptionString getfailed_prefix() {
        return failed_prefix;
    }

    public void setfailed_prefix(SOSOptionString p_failed_prefix) {
        this.failed_prefix = p_failed_prefix;
    }

    @JSOptionDefinition(name = "file", description = "", key = "file", type = "SOSOptionString", mandatory = true)
    public SOSOptionString file = new SOSOptionString(this, conClassName + ".file", "", " ", " ", true);

    public SOSOptionString getfile() {
        return file;
    }

    public void setfile(SOSOptionString p_file) {
        this.file = p_file;
    }

    @JSOptionDefinition(name = "ini_path", description = "", key = "ini_path", type = "SOSOptionString", mandatory = false)
    public SOSOptionString ini_path = new SOSOptionString(this, conClassName + ".ini_path", "", "value from JobScheduler instance",
            "value from JobScheduler instance", false);

    public SOSOptionString getini_path() {
        return ini_path;
    }

    public void setini_path(SOSOptionString p_ini_path) {
        this.ini_path = p_ini_path;
    }

    @JSOptionDefinition(name = "log_directory", description = "", key = "log_directory", type = "SOSOptionString", mandatory = false)
    public SOSOptionString log_directory = new SOSOptionString(this, conClassName + ".log_directory", "", "", "", false);

    public SOSOptionString getlog_directory() {
        return log_directory;
    }

    public void setlog_directory(SOSOptionString p_log_directory) {
        this.log_directory = p_log_directory;
    }

    @JSOptionDefinition(name = "log_only", description = "", key = "log_only", type = "SOSOptionString", mandatory = false)
    public SOSOptionBoolean log_only = new SOSOptionBoolean(this, conClassName + ".log_only", "", " ", " ", false);

    public SOSOptionBoolean getlog_only() {
        return log_only;
    }

    public void setlog_only(SOSOptionBoolean p_log_only) {
        this.log_only = p_log_only;
    }

    @JSOptionDefinition(name = "max_delivery", description = "", key = "max_delivery", type = "SOSOptionString", mandatory = false)
    public SOSOptionInteger max_delivery = new SOSOptionInteger(this, conClassName + ".max_delivery", "", "0", "0", false);

    public SOSOptionInteger getmax_delivery() {
        return max_delivery;
    }

    public void setmax_delivery(SOSOptionInteger p_max_delivery) {
        this.max_delivery = p_max_delivery;
    }

    @JSOptionDefinition(name = "queue_directory", description = "", key = "queue_directory", type = "SOSOptionString", mandatory = false)
    public SOSOptionString queue_directory = new SOSOptionString(this, conClassName + ".queue_directory", "", "Mail.dequeue()", "Mail.dequeue()",
            false);

    public SOSOptionString getqueue_directory() {
        return queue_directory;
    }

    public void setqueue_directory(SOSOptionString p_queue_directory) {
        this.queue_directory = p_queue_directory;
    }

    @JSOptionDefinition(name = "queue_pattern", description = "pattern for filenames of enqueued mails", key = "queue_pattern",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString queue_pattern = new SOSOptionString(this, conClassName + ".queue_pattern", "pattern for filenames of enqueued mails",
            "yyyy-MM-dd.HHmmss.S", "yyyy-MM-dd.HHmmss.S", false);

    public SOSOptionString getqueue_pattern() {
        return queue_pattern;
    }

    public void setqueue_pattern(SOSOptionString p_queue_pattern) {
        this.queue_pattern = p_queue_pattern;
    }

    @JSOptionDefinition(name = "queue_prefix", description = "", key = "queue_prefix", type = "SOSOptionString", mandatory = false)
    public SOSOptionString queue_prefix = new SOSOptionString(this, conClassName + ".queue_prefix", "", "sos.", "sos.", false);

    public SOSOptionString getqueue_prefix() {
        return queue_prefix;
    }

    public void setqueue_prefix(SOSOptionString p_queue_prefix) {
        this.queue_prefix = p_queue_prefix;
    }

    @JSOptionDefinition(name = "queue_prefix_spec", description = "", key = "queue_prefix_spec", type = "SOSOptionString", mandatory = false)
    public SOSOptionString queue_prefix_spec = new SOSOptionString(this, conClassName + ".queue_prefix_spec", "", "^(sos.*)(?&lt;!\\~)$",
            "^(sos.*)(?&lt;!\\~)$", false);

    public SOSOptionString getqueue_prefix_spec() {
        return queue_prefix_spec;
    }

    public void setqueue_prefix_spec(SOSOptionString p_queue_prefix_spec) {
        this.queue_prefix_spec = p_queue_prefix_spec;
    }

    @JSOptionDefinition(name = "smtp_host", description = "", key = "smtp_host", type = "SOSOptionString", mandatory = false)
    public SOSOptionString smtp_host = new SOSOptionString(this, conClassName + ".smtp_host", "", "value from JobScheduler instance",
            "value from JobScheduler instance", false);

    public SOSOptionString getsmtp_host() {
        return smtp_host;
    }

    public void setsmtp_host(SOSOptionString p_smtp_host) {
        this.smtp_host = p_smtp_host;
    }

    public JobSchedulerDequeueMailJobOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JobSchedulerDequeueMailJobOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerDequeueMailJobOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    public void setAllOptions(HashMap<String, String> pobjJSSettings) {
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.setSettings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
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
        this.setAllOptions(super.objSettings);
    }

}