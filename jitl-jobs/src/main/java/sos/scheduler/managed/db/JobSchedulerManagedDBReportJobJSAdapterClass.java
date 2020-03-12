package sos.scheduler.managed.db;

import sos.scheduler.job.JobSchedulerJobAdapter;

public class JobSchedulerManagedDBReportJobJSAdapterClass extends JobSchedulerJobAdapter {

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
            return getSpoolerProcess().getSuccess();
        } catch (Exception e) {
            return false;
        }
    }

    private void doProcessing() throws Exception {
        JobSchedulerManagedDBReportJob objR = new JobSchedulerManagedDBReportJob();
        JobSchedulerManagedDBReportJobOptions objO = objR.Options();
        objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters(getSpoolerProcess().getOrder())));
        objO.checkMandatory();
        objR.setJSJobUtilites(this);
        objR.Execute();
    }

}