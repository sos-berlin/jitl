package com.sos.jitl.checkblacklist;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionString;

@JSOptionClass(name = "JobSchedulerCheckBlacklistOptionsSuperClass", description = "JobSchedulerCheckBlacklistOptionsSuperClass")
public class JobSchedulerCheckBlacklistOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private final String conClassName = "JobSchedulerCheckBlacklistOptionsSuperClass";

    @JSOptionDefinition(name = "granuality", description = "", key = "granuality", type = "SOSOptionString", mandatory = false)
    public SOSOptionString granuality = new SOSOptionString(this, conClassName + ".granuality", "", "blacklist", "blacklist", false);

    public SOSOptionString getgranuality() {
        return granuality;
    }

    public void setgranuality(SOSOptionString p_granuality) {
        this.granuality = p_granuality;
    }

    @JSOptionDefinition(name = "job", description = "", key = "job", type = "SOSOptionString", mandatory = false)
    public SOSOptionString job = new SOSOptionString(this, conClassName + ".job", "", "", "", false);

    public SOSOptionString getjob() {
        return job;
    }

    public void setjob(SOSOptionString p_job) {
        this.job = p_job;
    }

    @JSOptionDefinition(name = "job_chain", description = "The name of the job chain that should be startet Paramet", key = "job_chain",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString job_chain = new SOSOptionString(this, conClassName + ".job_chain",
            "The name of the job chain that should be startet Paramet", "", "", false);

    public SOSOptionString getjob_chain() {
        return job_chain;
    }

    public void setjob_chain(SOSOptionString p_job_chain) {
        this.job_chain = p_job_chain;
    }

    @JSOptionDefinition(name = "jobscheduler_answer", description = "", key = "jobscheduler_answer", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jobscheduler_answer = new SOSOptionString(this, conClassName + ".jobscheduler_answer", "", " ", " ", false);

    public SOSOptionString getjobscheduler_answer() {
        return jobscheduler_answer;
    }

    public void setjobscheduler_answer(SOSOptionString p_jobscheduler_answer) {
        this.jobscheduler_answer = p_jobscheduler_answer;
    }

    @JSOptionDefinition(name = "level", description = "", key = "level", type = "SOSOptionString", mandatory = false)
    public SOSOptionString level = new SOSOptionString(this, conClassName + ".level", "", "info", "info", false);

    public SOSOptionString getlevel() {
        return level;
    }

    public void setlevel(SOSOptionString p_level) {
        this.level = p_level;
    }

    public JobSchedulerCheckBlacklistOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JobSchedulerCheckBlacklistOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerCheckBlacklistOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
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