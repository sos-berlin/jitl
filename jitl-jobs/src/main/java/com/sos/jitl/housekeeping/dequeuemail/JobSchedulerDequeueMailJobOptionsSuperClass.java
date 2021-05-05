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

    public SOSOptionString getDb() {
        return db;
    }

    public void setDb(SOSOptionString db) {
        this.db = db;
    }

    @JSOptionDefinition(name = "failed_prefix", description = "prefix for failed mail files", key = "failed_prefix", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString failedPrefix = new SOSOptionString(this, conClassName + ".failed_prefix", "prefix for failed mail files", "failed.",
            "failed.", false);

    public SOSOptionString getFailedPrefix() {
        return failedPrefix;
    }

    public void setFailedPrefix(SOSOptionString failedPrefix) {
        this.failedPrefix = failedPrefix;
    }

    @JSOptionDefinition(name = "file", description = "", key = "file", type = "SOSOptionString", mandatory = true)
    public SOSOptionString file = new SOSOptionString(this, conClassName + ".file", "", " ", " ", true);

    public SOSOptionString getFile() {
        return file;
    }

    public void setFile(SOSOptionString file) {
        this.file = file;
    }

    @JSOptionDefinition(name = "ini_path", description = "", key = "ini_path", type = "SOSOptionString", mandatory = false)
    public SOSOptionString iniPath = new SOSOptionString(this, conClassName + ".ini_path", "", "value from JobScheduler instance",
            "value from JobScheduler instance", false);

    public SOSOptionString getIniPath() {
        return iniPath;
    }

    public void setIniPath(SOSOptionString iniPath) {
        this.iniPath = iniPath;
    }

    @JSOptionDefinition(name = "log_directory", description = "", key = "log_directory", type = "SOSOptionString", mandatory = false)
    public SOSOptionString logDirectory = new SOSOptionString(this, conClassName + ".log_directory", "", "", "", false);

    public SOSOptionString getLogDirectory() {
        return logDirectory;
    }

    public void setLogDirectory(SOSOptionString logDirectory) {
        this.logDirectory = logDirectory;
    }

    @JSOptionDefinition(name = "log_only", description = "", key = "log_only", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean logOnly = new SOSOptionBoolean(this, conClassName + ".log_only", "", " ", " ", false);

    public SOSOptionBoolean getLogOnly() {
        return logOnly;
    }

    public void setLogOnly(SOSOptionBoolean logOnly) {
        this.logOnly = logOnly;
    }


    @JSOptionDefinition(name = "file_watching", description = "", key = "file_watching", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean fileWatching = new SOSOptionBoolean(this, conClassName + ".file_watching", "", " ", " ", false);

    public SOSOptionBoolean getFileWatching() {
        return fileWatching;
    }

    public void setFileWatching(SOSOptionBoolean fileWatching) {
        this.fileWatching = fileWatching;
    }

    
    @JSOptionDefinition(name = "max_delivery", description = "", key = "max_delivery", type = "SOSOptionString", mandatory = false)
    public SOSOptionInteger maxDelivery = new SOSOptionInteger(this, conClassName + ".max_delivery", "", "0", "0", false);

    public SOSOptionInteger getMaxDelivery() {
        return maxDelivery;
    }

    public void setMaxDelivery(SOSOptionInteger max_delivery) {
        this.maxDelivery = max_delivery;
    }

    @JSOptionDefinition(name = "queue_directory", description = "", key = "queue_directory", type = "SOSOptionString", mandatory = false)
    public SOSOptionString queueDirectory = new SOSOptionString(this, conClassName + ".queue_directory", "", "", "",
            false);

    public SOSOptionString getQueueDirectory() {
        return queueDirectory;
    }

    public void setQueueDirectory(SOSOptionString queueDirectory) {
        this.queueDirectory = queueDirectory;
    }

    @JSOptionDefinition(name = "email_file_name", description = "", key = "email_file_name", type = "SOSOptionString", mandatory = false)
    public SOSOptionString emailFileName = new SOSOptionString(this, conClassName + ".email_file_name", "", "", "",
            false);

    public SOSOptionString getEmailFileName() {
        return emailFileName;
    }

    public void setEmailFileName(SOSOptionString emailFileName) {
        this.emailFileName = emailFileName;
    }

    @JSOptionDefinition(name = "queue_pattern", description = "pattern for filenames of enqueued mails", key = "queue_pattern",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString queuePattern = new SOSOptionString(this, conClassName + ".queue_pattern", "pattern for filenames of enqueued mails",
            "yyyy-MM-dd.HHmmss.S", "yyyy-MM-dd.HHmmss.S", false);

    public SOSOptionString getQueuePattern() {
        return queuePattern;
    }

    public void setQueuePattern(SOSOptionString queuePattern) {
        this.queuePattern = queuePattern;
    }

    @JSOptionDefinition(name = "queue_prefix", description = "", key = "queue_prefix", type = "SOSOptionString", mandatory = false)
    public SOSOptionString queuePrefix = new SOSOptionString(this, conClassName + ".queue_prefix", "", "sos.", "sos.", false);

    public SOSOptionString getQueuePrefix() {
        return queuePrefix;
    }

    public void setQueuePrefix(SOSOptionString queuePprefix) {
        this.queuePrefix = queuePrefix;
    }

    @JSOptionDefinition(name = "queue_prefix_spec", description = "", key = "queue_prefix_spec", type = "SOSOptionString", mandatory = false)
    public SOSOptionString queuePrefixSpec = new SOSOptionString(this, conClassName + ".queue_prefix_spec", "", "^(sos.*)(?&lt;!\\~)$",
            "^(sos.*)(?&lt;!\\~)$", false);

    public SOSOptionString getQueuePrefixSpec() {
        return queuePrefixSpec;
    }

    public void setQueuePrefixSpec(SOSOptionString queuePrefixSpec) {
        this.queuePrefixSpec = queuePrefixSpec;
    }

    @JSOptionDefinition(name = "smtp_host", description = "", key = "smtp_host", type = "SOSOptionString", mandatory = false)
    public SOSOptionString smtpHost = new SOSOptionString(this, conClassName + ".smtp_host", "", "value from JobScheduler instance",
            "value from JobScheduler instance", false);

    public SOSOptionString getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(SOSOptionString smtpHost) {
        this.smtpHost = smtpHost;
    }

    public JobSchedulerDequeueMailJobOptionsSuperClass() {
        currentClass = this.getClass();
    }

    public JobSchedulerDequeueMailJobOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerDequeueMailJobOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
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