package com.sos.jitl.mail.smtp;

import sos.scheduler.job.JobSchedulerJobAdapter;

public class JSSmtpMailClientBaseClass extends JobSchedulerJobAdapter {

    protected final boolean continue_with_spooler_process = true;
    protected final boolean continue_with_task = true;
    protected JSSmtpMailClient objR = null;
    protected JSSmtpMailOptions objO = null;

    protected void CreateOptions(final String pstrEntryPointName) throws Exception {
        initializeLog4jAppenderClass();
        objR = new JSSmtpMailClient();
        objO = objR.getOptions();
        objR.setJSJobUtilites(this);
        objR.setJSCommands(this);
        String strStepName = this.getCurrentNodeName();
        objO.CurrentNodeName(strStepName).CurrentJobName(this.getJobName()).CurrentJobId(this.getJobId()).CurrentJobFolder(this.getJobFolder());
<<<<<<< HEAD

        objO.setAllOptions(getSchedulerParameterAsProperties());
    } // doProcessing
=======
        objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
    }
>>>>>>> 64d9775fa1b10da33c929ee153b62bb86923b408

    protected void doProcessing() throws Exception {
        CreateOptions("");
        objR.Execute();
    }

}
