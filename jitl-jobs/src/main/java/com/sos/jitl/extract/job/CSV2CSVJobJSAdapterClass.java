package com.sos.jitl.extract.job;

import sos.scheduler.job.JobSchedulerJobAdapter;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class CSV2CSVJobJSAdapterClass extends JobSchedulerJobAdapter {

    @Override
    public boolean spooler_process() throws Exception {

        CSV2CSVJob job = new CSV2CSVJob();
        try {
            super.spooler_process();

            CSV2CSVJobOptions options = job.getOptions();
            options.setCurrentNodeName(getCurrentNodeName(getSpoolerProcess().getOrder(), false));
            options.setAllOptions(getSchedulerParameterAsProperties(getSpoolerProcess().getOrder()));
            job.setJSJobUtilites(this);
            job.setJSCommands(this);

            job.execute();
            return getSpoolerProcess().getSuccess();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        }
    }
}
