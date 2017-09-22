package com.sos.jitl.checkhistory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

import sos.scheduler.job.JobSchedulerJobAdapter;

public class JobSchedulerCheckHistoryJSAdapterClass extends JobSchedulerJobAdapter {

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (JobSchedulerException e) {
            return false;
        }
        return this.signalSuccess();
    }

    protected void doProcessing() throws Exception {
        JobSchedulerCheckHistory jobSchedulerCheckRunHistory = new JobSchedulerCheckHistory();
        JobSchedulerCheckHistoryOptions jobSchedulerCheckRunHistoryOptions = jobSchedulerCheckRunHistory.options();
        jobSchedulerCheckRunHistoryOptions.setCurrentNodeName(getCurrentNodeName());
        jobSchedulerCheckRunHistoryOptions.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
        jobSchedulerCheckRunHistory.setJSJobUtilites(this);
        jobSchedulerCheckRunHistory.setJSCommands(this);
        jobSchedulerCheckRunHistory.setPathOfJob(spooler_job.folder_path());

        jobSchedulerCheckRunHistory.Execute();
        if (this.isOrderJob()) {
            spooler_task.order().params().set_var("check_run_history_result", jobSchedulerCheckRunHistoryOptions.result.getValue());
        } else {
            spooler_task.params().set_var("check_run_history_result", jobSchedulerCheckRunHistoryOptions.result.getValue());
        }
    }

}