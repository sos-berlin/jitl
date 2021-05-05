package com.sos.jitl.mail.smtp;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Order;

public class JSSmtpMailClientBaseClass extends JobSchedulerJobAdapter {

    protected final boolean continue_with_spooler_process = true;
    protected final boolean continue_with_task = true;
    protected JSSmtpMailClient objR = null;
    protected JSSmtpMailOptions objO = null;

   
    protected void createOptions(Order order, final String pstrEntryPointName) throws Exception {
        objR = new JSSmtpMailClient();
        objO = objR.getOptions();
        objR.setJSJobUtilites(this);
        objR.setJSCommands(this);
        String strStepName = getCurrentNodeName(order, false);
        objO.setCurrentJobId(getJobId()).setCurrentJobName(getJobName()).setCurrentJobFolder(getJobFolder()).setCurrentNodeName(strStepName);
        objO.setAllOptions(getSchedulerParameterAsProperties(order));
    }

    protected void doProcessing(Order order) throws Exception {
        createOptions(order, "");
        objR.Execute();
    }

}
