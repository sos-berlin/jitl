package com.sos.jitl.operations.criticalpath.job;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(UncriticalJobNodesJobOptionsSuperClass.class);

    /** \var operation : */
    @JSOptionDefinition(name = "operation", description = "", key = "operation", type = "SOSOptionString", mandatory = true)
    public SOSOptionString operation = new SOSOptionString(this, conClassName + ".operation", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue activate, deactivate
    true // isMandatory
    );

    /** \brief getoperation :
     * 
     * \details
     * 
     * 
     * \return */
    public SOSOptionString getoperation() {
        return operation;
    }

    /** \brief setoperation :
     * 
     * \details
     * 
     * 
     * @param operation : */
    public void setoperation(SOSOptionString val) {
        operation = val;
    }

    /** \var processing_prefix : */
    @JSOptionDefinition(name = "processing_prefix", description = "", key = "processing_prefix", type = "SOSOptionString", mandatory = false)
    public SOSOptionString processing_prefix = new SOSOptionString(this, conClassName + ".processing_prefix", // HashMap-Key
    "", // Titel
    "-", // InitValue
    "-", // DefaultValue
    false // isMandatory
    );

    /** \brief getprocessing_prefix :
     * 
     * \details
     * 
     * 
     * \return */
    public SOSOptionString getprocessing_prefix() {
        return processing_prefix;
    }

    /** \brief setprocessing_prefix :
     * 
     * \details
     * 
     * 
     * @param processing_prefix : */
    public void setprocessing_prefix(SOSOptionString val) {
        processing_prefix = val;
    }

    /** \var processing_recursive : */
    @JSOptionDefinition(name = "processing_recursive", description = "", key = "processing_recursive", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean processing_recursive = new SOSOptionBoolean(this, conClassName + ".processing_recursive", // HashMap-Key
    "", // Titel
    "true", // InitValue
    "true", // DefaultValue
    false // isMandatory
    );

    /** \brief getprocessing_recursive :
     * 
     * \details
     * 
     * 
     * \return */
    public SOSOptionBoolean getprocessing_recursive() {
        return processing_recursive;
    }

    /** \brief setprocessing_recursive :
     * 
     * \details
     * 
     * 
     * @param processing_recursive : */
    public void setprocessing_recursive(SOSOptionBoolean val) {
        processing_recursive = val;
    }

    /** \var include_job_chains : */
    @JSOptionDefinition(name = "include_job_chains", description = "", key = "include_job_chains", type = "SOSOptionString", mandatory = false)
    public SOSOptionString include_job_chains = new SOSOptionString(this, conClassName + ".include_job_chains", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    /** \brief getinclude_job_chains :
     * 
     * \details
     * 
     * 
     * \return */
    public SOSOptionString getinclude_job_chains() {
        return include_job_chains;
    }

    /** \brief setinclude_job_chains :
     * 
     * \details
     * 
     * 
     * @param include_job_chains : */
    public void setinclude_job_chains(SOSOptionString val) {
        include_job_chains = val;
    }

    /** \var exclude_job_chains : */
    @JSOptionDefinition(name = "exclude_job_chains", description = "", key = "exclude_job_chains", type = "SOSOptionString", mandatory = false)
    public SOSOptionString exclude_job_chains = new SOSOptionString(this, conClassName + ".exclude_job_chains", // HashMap-Key
    "", // Titel
    "/sos", // InitValue
    "/sos", // DefaultValue /sos
    false // isMandatory
    );

    /** \brief getexclude_job_chains :
     * 
     * \details
     * 
     * 
     * \return */
    public SOSOptionString getexclude_job_chains() {
        return exclude_job_chains;
    }

    /** \brief setexclude_job_chains :
     * 
     * \details
     * 
     * 
     * @param exclude_job_chains : */
    public void setexclude_job_chains(SOSOptionString val) {
        exclude_job_chains = val;
    }

    /** \var target_scheduler_host : */
    @JSOptionDefinition(name = "target_scheduler_host", description = "", key = "target_scheduler_host", type = "SOSOptionString", mandatory = false)
    public SOSOptionString target_scheduler_host = new SOSOptionString(this, conClassName + ".target_scheduler_host", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    /** \brief gettarget_scheduler_host :
     * 
     * \details
     * 
     * 
     * \return */
    public SOSOptionString gettarget_scheduler_host() {
        return target_scheduler_host;
    }

    /** \brief settarget_scheduler_host :
     * 
     * \details
     * 
     * 
     * @param target_scheduler_host : */
    public void settarget_scheduler_host(SOSOptionString val) {
        this.target_scheduler_host = val;
    }

    /** \var target_scheduler_port : */
    @JSOptionDefinition(name = "target_scheduler_port", description = "", key = "target_scheduler_port", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger target_scheduler_port = new SOSOptionInteger(this, conClassName + ".target_scheduler_port", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    /** \brief gettarget_scheduler_port :
     * 
     * \details
     * 
     * 
     * \return */
    public SOSOptionInteger gettarget_scheduler_port() {
        return target_scheduler_port;
    }

    /** \brief settarget_scheduler_port :
     * 
     * \details
     * 
     * 
     * @param target_scheduler_port : */
    public void settarget_scheduler_port(SOSOptionInteger val) {
        this.target_scheduler_port = val;
    }

    /** \var scheduler_timeout in Sekunden. Default 5 Sekunden: */
    @JSOptionDefinition(name = "target_scheduler_timeout", description = "", key = "target_scheduler_timeout", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger target_scheduler_timeout = new SOSOptionInteger(this, conClassName + ".target_scheduler_timeout", // HashMap-Key
    "", // Titel
    "5", // InitValue
    "5", // DefaultValue
    false // isMandatory
    );

    /** \brief gettarget_scheduler_timeout :
     * 
     * \details
     * 
     * 
     * \return */
    public SOSOptionInteger gettarget_scheduler_timeout() {
        return target_scheduler_timeout;
    }

    /** \brief settarget_scheduler_timeout :
     * 
     * \details
     * 
     * 
     * @param target_scheduler_timeout : */
    public void settarget_scheduler_timeout(SOSOptionInteger val) {
        this.target_scheduler_timeout = val;
    }

    /**
     * 
     */
    public UncriticalJobNodesJobOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    /** @param pobjListener */
    public UncriticalJobNodesJobOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    /** @param JSSettings
     * @throws Exception */
    public UncriticalJobNodesJobOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    /**
	 * 
	 */
    public void setAllOptions(HashMap<String, String> pobjJSSettings) {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::setAllOptions";
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.Settings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
    } // public void setAllOptions (HashMap <String, String> JSSettings)

    /**
	 * 
	 */
    @Override
    public void CheckMandatory() throws JSExceptionMandatoryOptionMissing //
            , Exception {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    /**
	 * 
	 */
    @Override
    public void CommandLineArgs(String[] pstrArgs) {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }
}