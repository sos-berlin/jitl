package com.sos.jitl.extract.job;

import sos.scheduler.job.JobSchedulerJobAdapter;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class ResultSet2CSVJobJSAdapterClass extends JobSchedulerJobAdapter {

    @Override
    public boolean spooler_process() throws Exception {

        ResultSet2CSVJob job = new ResultSet2CSVJob();
        try {
            super.spooler_process();

            ResultSet2CSVJobOptions options = job.getOptions();
            options.setCurrentNodeName(this.getCurrentNodeName(getSpoolerProcess().getOrder(), true));
            options.setAllOptions(getSchedulerParameterAsProperties(getSpoolerProcess().getOrder()));
            job.setJSJobUtilites(this);
            job.setJSCommands(this);

            job.init();
            job.execute();
            return getSpoolerProcess().isOrderJob();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        } finally {
            job.exit();
        }

    }
}
