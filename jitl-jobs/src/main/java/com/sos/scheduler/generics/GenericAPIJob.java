package com.sos.scheduler.generics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;

public class GenericAPIJob extends JSJobUtilitiesClass<GenericAPIJobOptions> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericAPIJob.class);

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
            getOptions().checkMandatory();
            LOGGER.debug(getOptions().toString());
        } catch (Exception e) {
            LOGGER.error(String.format(Messages.getMsg("JSJ-I-107"), methodName) + " " + e.getMessage(), e);
        } finally {
            LOGGER.debug(String.format(Messages.getMsg("JSJ-I-111"), methodName));
        }
        return this;
    }

}