package com.sos.jitl.housekeeping.rotatelog;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionTime;

@JSOptionClass(name = "JobSchedulerRotateLogOptionsSuperClass", description = "JobSchedulerRotateLogOptionsSuperClass")
public class JobSchedulerRotateLogOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = -6542456636928445160L;
    private final String conClassName = "JobSchedulerRotateLogOptionsSuperClass";

    @JSOptionDefinition(name = "JobSchedulerID", description = "The ID of the JobScheduler", key = "JobSchedulerID", type = "SOSOptionString",
            mandatory = true)
    public SOSOptionString jobSchedulerID = new SOSOptionString(this, conClassName + ".JobSchedulerID", "The ID of the JobScheduler", "",
            "scheduler", true);

    public SOSOptionString getJobSchedulerID() {
        return jobSchedulerID;
    }

    public JobSchedulerRotateLogOptionsSuperClass setJobSchedulerID(final SOSOptionString pstrValue) {
        jobSchedulerID = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "delete_file_age", description = "", key = "delete_file_age", type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime delete_file_age = new SOSOptionTime(this, conClassName + ".delete_file_age", "", "0", "0", false);

    public SOSOptionTime getdelete_file_age() {
        return delete_file_age;
    }

    public SOSOptionTime delete_file_age() {
        return delete_file_age;
    }

    public void setdelete_file_age(SOSOptionTime p_delete_file_age) {
        this.delete_file_age = p_delete_file_age;
    }

    public void delete_file_age(SOSOptionTime p_delete_file_age) {
        this.delete_file_age = p_delete_file_age;
    }

    @JSOptionDefinition(name = "delete_file_specification", description = "", key = "delete_file_specification", type = "SOSOptionRegExp",
            mandatory = false)
    public SOSOptionRegExp delete_file_specification = new SOSOptionRegExp(this, conClassName + ".delete_file_specification", "",
            "^(scheduler)([0-9\\-]+).*(\\.log)(\\.gz)?$", "^(scheduler)([0-9\\-]+).*(\\.log)(\\.gz)?$", false);

    public SOSOptionRegExp getdelete_file_specification() {
        return delete_file_specification;
    }

    public SOSOptionRegExp delete_file_specification() {
        return delete_file_specification;
    }

    public void setdelete_file_specification(SOSOptionRegExp p_delete_file_specification) {
        this.delete_file_specification = p_delete_file_specification;
    }

    public void delete_file_specification(SOSOptionRegExp p_delete_file_specification) {
        this.delete_file_specification = p_delete_file_specification;
    }

    @JSOptionDefinition(name = "file_age", description = "", key = "file_age", type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime file_age = new SOSOptionTime(this, conClassName + ".file_age", "", "14d", "14d", false);
    public SOSOptionTime compressFileAge = (SOSOptionTime) file_age.SetAlias(conClassName + ".compress_file_age");
    public SOSOptionTime compress_file_age = (SOSOptionTime) file_age.SetAlias(conClassName + ".compress_file_age");

    public SOSOptionTime getfile_age() {
        return file_age;
    }

    public SOSOptionTime file_age() {
        return file_age;
    }

    public void setfile_age(SOSOptionTime p_file_age) {
        this.file_age = p_file_age;
    }

    public void file_age(SOSOptionTime p_file_age) {
        this.file_age = p_file_age;
    }

    @JSOptionDefinition(name = "file_path", description = "", key = "file_path", type = "SOSOptionFolderName", mandatory = true)
    public SOSOptionFolderName file_path = new SOSOptionFolderName(this, conClassName + ".file_path", "directory for the JobScheduler log files",
            "${SCHEDULER_DATA}/logs", "${SCHEDULER_DATA}/logs", true);
    public SOSOptionFolderName jobSchedulerLogFilesPath = (SOSOptionFolderName) file_path.SetAlias(".JobScheduler_LogFiles_Path");

    public SOSOptionFolderName getfile_path() {
        return file_path;
    }

    public SOSOptionFolderName file_path() {
        return file_path;
    }

    public SOSOptionFolderName JobSchedulerLogFilesPath() {
        return file_path;
    }

    public void setfile_path(SOSOptionFolderName p_file_path) {
        this.file_path = p_file_path;
    }

    public void file_path(SOSOptionFolderName p_file_path) {
        this.file_path = p_file_path;
    }

    @JSOptionDefinition(name = "file_specification", description = "", key = "file_specification", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp file_specification = new SOSOptionRegExp(this, conClassName + ".file_specification", "",
            "^(scheduler).*([0-9\\-]+).*(\\.log)$", "^(scheduler).*([0-9\\-]+).*(\\.log)$", false);

    public SOSOptionRegExp getfile_specification() {
        return file_specification;
    }

    public SOSOptionRegExp file_specification() {
        return file_specification;
    }

    public void setfile_specification(SOSOptionRegExp p_file_specification) {
        this.file_specification = p_file_specification;
    }

    public void file_specification(SOSOptionRegExp p_file_specification) {
        this.file_specification = p_file_specification;
    }

    public SOSOptionRegExp compress_file_spec = (SOSOptionRegExp) file_specification.SetAlias(conClassName + ".compress_file_spec");

    public JobSchedulerRotateLogOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JobSchedulerRotateLogOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerRotateLogOptionsSuperClass(HashMap<String, String> JSSettings) {
        this();
        this.setAllOptions(JSSettings);
    }

    @Override
    public void setAllOptions(HashMap<String, String> pobjJSSettings) {
        objSettings = pobjJSSettings;
        super.setAllOptions(pobjJSSettings);
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