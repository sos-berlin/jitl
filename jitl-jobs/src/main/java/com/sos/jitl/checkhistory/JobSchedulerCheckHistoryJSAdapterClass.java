package com.sos.jitl.checkhistory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

import sos.scheduler.job.JobSchedulerJobAdapter;

public class JobSchedulerCheckHistoryJSAdapterClass extends JobSchedulerJobAdapter {

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
            return getSpoolerProcess().getSuccess();
        } catch (JobSchedulerException e) {
            return false;
        }
    }

    protected void doProcessing() throws Exception {
        JobSchedulerCheckHistory jobSchedulerCheckRunHistory = new JobSchedulerCheckHistory();
        JobSchedulerCheckHistoryOptions jobSchedulerCheckHistoryOptions = jobSchedulerCheckRunHistory.options();
        jobSchedulerCheckHistoryOptions.setCurrentNodeName(getCurrentNodeName(getSpoolerProcess().getOrder(), true));
        jobSchedulerCheckHistoryOptions.setAllOptions(getSchedulerParameterAsProperties(getSpoolerProcess().getOrder()));
        jobSchedulerCheckRunHistory.setJSJobUtilites(this);
        jobSchedulerCheckRunHistory.setJSCommands(this);
        jobSchedulerCheckRunHistory.setPathOfJob(spooler_job.folder_path());

        try {
            jobSchedulerCheckRunHistory.execute();
        } finally {
            if (spooler_task.job().order_queue() != null) {
                getSpoolerProcess().getOrder().params().set_var("check_run_history_result", jobSchedulerCheckHistoryOptions.result.getValue());
            } else {
                spooler_task.params().set_var("check_run_history_result", jobSchedulerCheckHistoryOptions.result.getValue());
            }
        }
    }

}