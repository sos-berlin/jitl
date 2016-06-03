package sos.scheduler.db;

import sos.scheduler.job.JobSchedulerJobAdapter;
import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JobSchedulerPLSQLJobJSAdapterClass extends JobSchedulerJobAdapter {



    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error", e);
        } 
        return signalSuccess();
    }


    private void doProcessing() throws Exception {
        JobSchedulerPLSQLJob jobSchedulerPLSQLJob = new JobSchedulerPLSQLJob();
        JobSchedulerPLSQLJobOptions jobSchedulerPLSQLJobOptions = jobSchedulerPLSQLJob.getOptions();
        jobSchedulerPLSQLJobOptions.setCurrentNodeName(this.getCurrentNodeName());
        jobSchedulerPLSQLJobOptions.setAllOptions(getSchedulerParameterAsProperties());
        setJobScript(jobSchedulerPLSQLJobOptions.command);
        jobSchedulerPLSQLJobOptions.checkMandatory();
        jobSchedulerPLSQLJob.setJSJobUtilites(this);
        jobSchedulerPLSQLJob.Execute();
}
}
