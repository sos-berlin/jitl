package com.sos.jitl.mail.smtp;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.jitl.mail.smtp.JSSmtpMailOptions.enuMailClasses;

public class SmtpMailMonitor extends JSSmtpMailClientBaseClass {

    private final String conClassName = this.getClass().getSimpleName();

    @Override
    public void spooler_task_after() throws Exception {
        try {
            super.spooler_process();
            CreateOptions("task_after");
            if (!isOrderJob()) {
                if (spooler_task.exit_code() != 0) {
                    if (objO.MailOnError()) {
                        objR.Execute(objO.getOptions(enuMailClasses.MailOnError));
                    }
                } else {
                    if (objO.MailOnSuccess()) {
                        objR.Execute(objO.getOptions(enuMailClasses.MailOnSuccess));
                    }
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public boolean spooler_task_before() throws Exception {
        try {
            super.spooler_process();
            CreateOptions("task_before");
            if (!isOrderJob() && objO.MailOnJobStart()) {
                objR.Execute(objO.getOptions(enuMailClasses.MailOnJobStart));
            }
        } catch (Exception e) {
            throw new JobSchedulerException(e.getLocalizedMessage(), e);
        }
        return continue_with_task;
    }

    @Override
    public boolean spooler_process_after(final boolean spooler_process_return_code) throws Exception {
        try {
            super.spooler_process();
            CreateOptions("process_after");
            if (isOrderJob()) {
                if (!spooler_process_return_code) {
                    if (objO.MailOnError() && objO.MailOnError()) {
                        objR.Execute(objO.getOptions(enuMailClasses.MailOnError));
                    }
                } else {
                    if (objO.MailOnSuccess() && objO.MailOnSuccess()) {
                        objR.Execute(objO.getOptions(enuMailClasses.MailOnSuccess));
                    }
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException(e.getLocalizedMessage(), e);
        }
        return spooler_process_return_code;
    }

    @Override
    public boolean spooler_process_before() throws Exception {
        try {
            super.spooler_process();
            CreateOptions("process_before");
            if (isOrderJob() && objO.MailOnJobStart()) {
                objR.Execute(objO.getOptions(enuMailClasses.MailOnJobStart));
            }
        } catch (Exception e) {
            throw new JobSchedulerException(e.getLocalizedMessage(), e);
        }
        return continue_with_spooler_process;
    }

}