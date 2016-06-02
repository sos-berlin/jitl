package com.sos.jitl.extract.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.jitl.extract.model.CSV2CSVModel;

public class CSV2CSVJob extends JSJobUtilitiesClass<CSV2CSVJobOptions> {

    private final String className = CSV2CSVJob.class.getSimpleName();
    private static Logger logger = LoggerFactory.getLogger(CSV2CSVJob.class);

    public CSV2CSVJob() {
        super(new CSV2CSVJobOptions());
    }

    public CSV2CSVJob execute() throws Exception {
        final String methodName = className + "::execute";

        logger.debug(methodName);

        try {
            getOptions().checkMandatory();
            logger.debug(getOptions().toString());

            CSV2CSVModel model = new CSV2CSVModel(getOptions());
            model.process();
        } catch (Exception e) {
            logger.error(String.format("%s: %s", methodName, e.toString()));
            throw e;
        }

        return this;
    }

    public CSV2CSVJobOptions getOptions() {
        if (objOptions == null) {
            objOptions = new CSV2CSVJobOptions();
        }
        return objOptions;
    }

}