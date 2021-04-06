package com.sos.jitl.mail.smtp;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Order;

public class JSSmtpMailClientBaseClass extends JobSchedulerJobAdapter {

    protected final boolean continue_with_spooler_process = true;
    protected final boolean continue_with_task = true;
    protected JSSmtpMailClient objR = null;
    protected JSSmtpMailOptions objO = null;

    private void addParameters() {
        if (spooler_task != null) {
            setJobId(spooler_task.id());
        }
        if (spooler_job != null) {
            String jobName = spooler_job.name();

            setJobName(jobName);
            setJobFolder(spooler_job.folder_path());
            setJobTitle(spooler_job.title());
        }
        this.getSchedulerParameters().put("SCHEDULER_JOB_NAME", spooler_job.name());
        this.getSchedulerParameters().put("SCHEDULER_JOB_TITLE", spooler_job.title());
        this.getSchedulerParameters().put("SCHEDULER_TASK_ID", String.valueOf(spooler_task.id()));

    }

    protected void createOptions(Order order, final String pstrEntryPointName) throws Exception {
        objR = new JSSmtpMailClient();
        objO = objR.getOptions();
        objR.setJSJobUtilites(this);
        objR.setJSCommands(this);
        String strStepName = getCurrentNodeName(order, false);
        objO.setCurrentJobId(getJobId()).setCurrentJobName(getJobName()).setCurrentJobFolder(getJobFolder()).setCurrentNodeName(strStepName);
        objO.setAllOptions(getSchedulerParameterAsProperties(order));
        addParameters();
    }

    protected void doProcessing(Order order) throws Exception {
        createOptions(order, "");
        objR.Execute();
    }

}
