package com.sos.jitl.checkrunhistory;

import sos.scheduler.job.JobSchedulerJobAdapter;

public class JobSchedulerCheckRunHistoryJSAdapterClass extends JobSchedulerJobAdapter {

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
            return getSpoolerProcess().isOrderJob();
        } catch (Exception e) {
            return false;
        }
    }

    protected void doProcessing() throws Exception {
        JobSchedulerCheckRunHistory jobSchedulerCheckRunHistory = new JobSchedulerCheckRunHistory();
        JobSchedulerCheckRunHistoryOptions jobSchedulerCheckRunHistoryOptions = jobSchedulerCheckRunHistory.options();
        jobSchedulerCheckRunHistoryOptions.setCurrentNodeName(getCurrentNodeName(getSpoolerProcess().getOrder(), true));
        jobSchedulerCheckRunHistoryOptions.setAllOptions(getSchedulerParameterAsProperties(getSpoolerProcess().getOrder()));
        jobSchedulerCheckRunHistory.setJSJobUtilites(this);
        jobSchedulerCheckRunHistory.setJSCommands(this);
        jobSchedulerCheckRunHistory.setPathOfJob(spooler_job.folder_path());
        jobSchedulerCheckRunHistory.Execute();
        if (spooler_task.job().order_queue() != null) {
            spooler_task.order().params().set_var("check_run_history_result", jobSchedulerCheckRunHistoryOptions.result.getValue());
            spooler_task.order().params().set_var("check_run_history_number_of_starts", jobSchedulerCheckRunHistoryOptions.numberOfStarts.getValue());
            spooler_task.order().params().set_var("check_run_history_number_of_completed", jobSchedulerCheckRunHistoryOptions.numberOfCompleted
                    .getValue());
            spooler_task.order().params().set_var("check_run_history_number_of_completed_with_error",
                    jobSchedulerCheckRunHistoryOptions.numberOfCompletedWithError.getValue());
            spooler_task.order().params().set_var("check_run_history_number_of_completed_successful",
                    jobSchedulerCheckRunHistoryOptions.numberOfCompletedSuccessful.getValue());
        } else {
            spooler_task.params().set_var("check_run_history_result", jobSchedulerCheckRunHistoryOptions.result.getValue());
            spooler_task.params().set_var("check_run_history_number_of_starts", jobSchedulerCheckRunHistoryOptions.numberOfStarts.getValue());
            spooler_task.params().set_var("check_run_history_number_of_completed", jobSchedulerCheckRunHistoryOptions.numberOfCompleted.getValue());
            spooler_task.params().set_var("check_run_history_number_of_completed_with_error",
                    jobSchedulerCheckRunHistoryOptions.numberOfCompletedWithError.getValue());
            spooler_task.params().set_var("check_run_history_number_of_completed_successful",
                    jobSchedulerCheckRunHistoryOptions.numberOfCompletedSuccessful.getValue());
        }
    }

}