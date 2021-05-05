package com.sos.jitl.operations.criticalpath.job;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionString;

/** @author Robert Ehrlich */
@JSOptionClass(name = "UncriticalJobNodesJobOptionsSuperClass", description = "UncriticalJobNodesJobOptionsSuperClass")
public class UncriticalJobNodesJobOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private final static String conClassName = UncriticalJobNodesJobOptionsSuperClass.class.getSimpleName();

    @JSOptionDefinition(name = "operation", description = "", key = "operation", type = "SOSOptionString", mandatory = true)
    public SOSOptionString operation = new SOSOptionString(this, conClassName + ".operation", "", "", "", true);

    public SOSOptionString getoperation() {
        return operation;
    }

    public void setoperation(SOSOptionString val) {
        operation = val;
    }

    @JSOptionDefinition(name = "processing_prefix", description = "", key = "processing_prefix", type = "SOSOptionString", mandatory = false)
    public SOSOptionString processing_prefix = new SOSOptionString(this, conClassName + ".processing_prefix", "", "-", "-", false);

    public SOSOptionString getprocessing_prefix() {
        return processing_prefix;
    }

    public void setprocessing_prefix(SOSOptionString val) {
        processing_prefix = val;
    }

    @JSOptionDefinition(name = "processing_recursive", description = "", key = "processing_recursive", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean processing_recursive = new SOSOptionBoolean(this, conClassName + ".processing_recursive", "", "true", "true", false);

    public SOSOptionBoolean getprocessing_recursive() {
        return processing_recursive;
    }

    public void setprocessing_recursive(SOSOptionBoolean val) {
        processing_recursive = val;
    }

    @JSOptionDefinition(name = "include_job_chains", description = "", key = "include_job_chains", type = "SOSOptionString", mandatory = false)
    public SOSOptionString include_job_chains = new SOSOptionString(this, conClassName + ".include_job_chains", "", "", "", false);

    public SOSOptionString getinclude_job_chains() {
        return include_job_chains;
    }

    public void setinclude_job_chains(SOSOptionString val) {
        include_job_chains = val;
    }

    @JSOptionDefinition(name = "exclude_job_chains", description = "", key = "exclude_job_chains", type = "SOSOptionString", mandatory = false)
    public SOSOptionString exclude_job_chains = new SOSOptionString(this, conClassName + ".exclude_job_chains", "", "/sos", "/sos", false);

    public SOSOptionString getexclude_job_chains() {
        return exclude_job_chains;
    }

    public void setexclude_job_chains(SOSOptionString val) {
        exclude_job_chains = val;
    }

    @JSOptionDefinition(name = "target_scheduler_host", description = "", key = "target_scheduler_host", type = "SOSOptionString", mandatory = false)
    public SOSOptionString target_scheduler_host = new SOSOptionString(this, conClassName + ".target_scheduler_host", "", "", "", false);

    public SOSOptionString gettarget_scheduler_host() {
        return target_scheduler_host;
    }

    public void settarget_scheduler_host(SOSOptionString val) {
        this.target_scheduler_host = val;
    }

    @JSOptionDefinition(name = "target_scheduler_port", description = "", key = "target_scheduler_port", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger target_scheduler_port = new SOSOptionInteger(this, conClassName + ".target_scheduler_port", "", "", "", false);

    public SOSOptionInteger gettarget_scheduler_port() {
        return target_scheduler_port;
    }

    public void settarget_scheduler_port(SOSOptionInteger val) {
        this.target_scheduler_port = val;
    }

    @JSOptionDefinition(name = "target_scheduler_timeout", description = "", key = "target_scheduler_timeout", type = "SOSOptionInteger",
            mandatory = false)
    public SOSOptionInteger target_scheduler_timeout = new SOSOptionInteger(this, conClassName + ".target_scheduler_timeout", "", "5", "5", false);

    public SOSOptionInteger gettarget_scheduler_timeout() {
        return target_scheduler_timeout;
    }

    public void settarget_scheduler_timeout(SOSOptionInteger val) {
        this.target_scheduler_timeout = val;
    }

    public UncriticalJobNodesJobOptionsSuperClass() {
        currentClass = this.getClass();
    }

    public UncriticalJobNodesJobOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public UncriticalJobNodesJobOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
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