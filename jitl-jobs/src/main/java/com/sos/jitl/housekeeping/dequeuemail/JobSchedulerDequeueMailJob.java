package com.sos.jitl.housekeeping.dequeuemail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.scheduler.messages.JSMessages;

public class JobSchedulerDequeueMailJob extends JSJobUtilitiesClass<JobSchedulerDequeueMailJobOptions> {

    protected JobSchedulerDequeueMailJobOptions objOptions = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerDequeueMailJob.class);
    private JSJobUtilities objJSJobUtilities = this;
    private String hibernateConfigurationFile;
    private String configDir;
    
    public JobSchedulerDequeueMailJob() {
        super(new JobSchedulerDequeueMailJobOptions());
    }

    public JobSchedulerDequeueMailJobOptions getOptions() {
        if (objOptions == null) {
            objOptions = new JobSchedulerDequeueMailJobOptions();
        }
        return objOptions;
    }

    public void setOptions(final JobSchedulerDequeueMailJobOptions pobjOptions) {
        objOptions = pobjOptions;
     }

    public JobSchedulerDequeueMailJob execute() throws Exception {
        final String methodName = "JobSchedulerDequeueMailJob::Execute";
        LOGGER.debug(String.format(JSMessages.JSJ_I_110.get(), methodName));
        try {
            getOptions().checkMandatory();
            LOGGER.debug(getOptions().toString());
            DequeueMailExecuter dequeueMailExecuter = new DequeueMailExecuter(getOptions());
            dequeueMailExecuter.setHibernateConfigurationFile(hibernateConfigurationFile);
            dequeueMailExecuter.setConfigDir(configDir);

            if (getOptions().emailFileName.isDirty() || !getOptions().emailFileName.getValue().isEmpty()) {
                dequeueMailExecuter.execute();
            } else {
                dequeueMailExecuter.resendFailedMails();
            }
        } catch (Exception e) {
            LOGGER.error(String.format(JSMessages.JSJ_F_107.get(), methodName) + " " + e.getMessage(), e);
            throw e;
        } finally {
            LOGGER.debug(String.format(JSMessages.JSJ_I_111.get(), methodName));
        }
        return this;
    }

    public String myReplaceAll(String pstrSourceString, String pstrReplaceWhat, String pstrReplaceWith) {
        String newReplacement = pstrReplaceWith.replaceAll("\\$", "\\\\\\$");
        return pstrSourceString.replaceAll("(?m)" + pstrReplaceWhat, newReplacement);
    }

    @Override
    public String replaceSchedulerVars(String pstrString2Modify) {
        LOGGER.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
        return pstrString2Modify;
    }

    @Override
    public void setJSParam(String pstrKey, String pstrValue) {

    }

    @Override
    public void setJSParam(String pstrKey, StringBuffer pstrValue) {

    }

    public void setJSJobUtilites(JSJobUtilities pobjJSJobUtilities) {
        if (pobjJSJobUtilities == null) {
            objJSJobUtilities = this;
        } else {
            objJSJobUtilities = pobjJSJobUtilities;
        }
        LOGGER.debug("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
    }

    
    public void setHibernateConfigurationFile(String hibernateConfigurationFile) {
        this.hibernateConfigurationFile = hibernateConfigurationFile;
    }

    
    public void setConfigDir(String configDir) {
        this.configDir = configDir;
    }

}