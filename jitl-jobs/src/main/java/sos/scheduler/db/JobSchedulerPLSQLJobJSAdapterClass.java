package sos.scheduler.db;

import sos.scheduler.job.JobSchedulerJobAdapter;
import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JobSchedulerPLSQLJobJSAdapterClass extends JobSchedulerJobAdapter {

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
            return getSpoolerProcess().isOrderJob();
        } catch (Exception e) {
            e.printStackTrace();
            throw new JobSchedulerException("Fatal Error", e);
        }
    }

    private void doProcessing() throws Exception {
        JobSchedulerPLSQLJob jobSchedulerPLSQLJob = new JobSchedulerPLSQLJob();
        JobSchedulerPLSQLJobOptions jobSchedulerPLSQLJobOptions = jobSchedulerPLSQLJob.getOptions();
        jobSchedulerPLSQLJobOptions.setCurrentNodeName(getCurrentNodeName(getSpoolerProcess().getOrder(), true));
        jobSchedulerPLSQLJobOptions.setAllOptions(getSchedulerParameterAsProperties(getSpoolerProcess().getOrder()));
        setJobScript(jobSchedulerPLSQLJobOptions.command);
        jobSchedulerPLSQLJobOptions.checkMandatory();
        jobSchedulerPLSQLJob.setJSJobUtilites(this);
        jobSchedulerPLSQLJob.execute();
    }

}