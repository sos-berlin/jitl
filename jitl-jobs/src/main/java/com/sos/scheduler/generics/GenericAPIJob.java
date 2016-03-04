package com.sos.scheduler.generics;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;

public class GenericAPIJob extends JSJobUtilitiesClass<GenericAPIJobOptions> {

    private static final Logger LOGGER = Logger.getLogger(GenericAPIJob.class);

    public GenericAPIJob() {
        super(new GenericAPIJobOptions());
    }

    @Override
    public GenericAPIJobOptions getOptions() {
        if (objOptions == null) {
            objOptions = new GenericAPIJobOptions();
        }
        return objOptions;
    }

    public GenericAPIJob Execute() throws Exception {
        final String methodName = "GenericAPIJob::Execute";
        LOGGER.debug(String.format(Messages.getMsg("JSJ-I-110"), methodName));
        try {
            getOptions().CheckMandatory();
            LOGGER.debug(getOptions().toString());
        } catch (Exception e) {
            LOGGER.error(String.format(Messages.getMsg("JSJ-I-107"), methodName) + " " + e.getMessage(), e);
        } finally {
            LOGGER.debug(String.format(Messages.getMsg("JSJ-I-111"), methodName));
        }
        return this;
    }

    public void init() {
        doInitialize();
    }

    private void doInitialize() {
        // doInitialize
    } 

    @Override
    public String myReplaceAll(final String pstrSourceString, final String pstrReplaceWhat, final String pstrReplaceWith) {
        String newReplacement = pstrReplaceWith.replaceAll("\\$", "\\\\\\$");
        return pstrSourceString.replaceAll("(?m)" + pstrReplaceWhat, newReplacement);
    }

}