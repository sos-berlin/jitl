package com.sos.jitl.mail.smtp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.jitl.mail.smtp.JSSmtpMailOptions.enuMailClasses;

public class SmtpMailMonitor extends JSSmtpMailClientBaseClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpMailMonitor.class);

    @Override
    public void spooler_task_after() throws Exception {
        try {
            super.spooler_process();
            createOptions(getSpoolerProcess().getOrder(), "task_after");
            if (spooler_task.job().order_queue() == null) {
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
            throw new JobSchedulerException(e.getMessage(), e);
        }
    }

    @Override
    public boolean spooler_task_before() throws Exception {
        try {
            super.spooler_process();
            createOptions(getSpoolerProcess().getOrder(), "task_before");
            if (spooler_task.job().order_queue() == null && objO.MailOnJobStart()) {
                objR.Execute(objO.getOptions(enuMailClasses.MailOnJobStart));
            }
        } catch (Exception e) {
            throw new JobSchedulerException(e.getMessage(), e);
        }
        return continue_with_task;
    }

    @Override
    public boolean spooler_process_after(final boolean spooler_process_return_code) throws Exception {
        try {
            super.spooler_process();
            createOptions(getSpoolerProcess().getOrder(), "process_after");
            if (spooler_task.job().order_queue() != null) {
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
            throw new JobSchedulerException(e.getMessage(), e);
        }
        return spooler_process_return_code;
    }

    @Override
    public boolean spooler_process_before() throws Exception {
        try {
            super.spooler_process();
            createOptions(getSpoolerProcess().getOrder(), "process_before");
            if (spooler_task.job().order_queue() != null && objO.MailOnJobStart()) {
                objR.Execute(objO.getOptions(enuMailClasses.MailOnJobStart));
            }
        } catch (Exception e) {
            throw new JobSchedulerException(e.getMessage(), e);
        }
        return continue_with_spooler_process;
    }

}