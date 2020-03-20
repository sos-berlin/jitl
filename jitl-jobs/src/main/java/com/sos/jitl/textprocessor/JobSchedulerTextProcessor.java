package com.sos.jitl.textprocessor;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.scheduler.messages.JSMessages;

public class JobSchedulerTextProcessor extends JSJobUtilitiesClass<JobSchedulerTextProcessorOptions> {

    protected JobSchedulerTextProcessorOptions objOptions = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerTextProcessor.class);
    private JSJobUtilities objJSJobUtilities = this;

    public JobSchedulerTextProcessor() {
        super(new JobSchedulerTextProcessorOptions());
    }

    public JobSchedulerTextProcessorOptions getOptions() {
        if (objOptions == null) {
            objOptions = new JobSchedulerTextProcessorOptions();
        }
        return objOptions;
    }

    public JobSchedulerTextProcessorOptions getOptions(final JobSchedulerTextProcessorOptions pobjOptions) {
        objOptions = pobjOptions;
        return objOptions;
    }

    public JobSchedulerTextProcessor Execute() throws Exception {
        final String methodName = "JobSchedulerTextProcessor::Execute";
        LOGGER.debug(String.format(JSMessages.JSJ_I_110.get(), methodName));
        File inputFile = new File(getOptions().filename.getValue());
        String command = getOptions().command.getValue();
        String param = getOptions().param.getValue();
        JobSchedulerTextProcessorExecuter jobSchedulerTextProcessorExecuter = new JobSchedulerTextProcessorExecuter(inputFile, command + " " + param);
        String result = jobSchedulerTextProcessorExecuter.execute();
        getOptions().result.setValue(result);
        try {
            getOptions().checkMandatory();
            LOGGER.debug(getOptions().toString());
        } catch (Exception e) {
            LOGGER.error(String.format(JSMessages.JSJ_F_107.get(), methodName) + " " + e.getMessage(), e);
            throw e;
        } finally {
            LOGGER.debug(String.format(JSMessages.JSJ_I_111.get(), methodName));
        }
        return this;
    }

    public void init() {
        doInitialize();
    }

    private void doInitialize() {
        // doInitialize
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
    public void setJSParam(String pstrKey, StringBuilder pstrValue) {

    }

    public void setJSJobUtilites(JSJobUtilities pobjJSJobUtilities) {
        if (pobjJSJobUtilities == null) {
            objJSJobUtilities = this;
        } else {
            objJSJobUtilities = pobjJSJobUtilities;
        }
        LOGGER.debug("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
    }

}