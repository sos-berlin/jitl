package com.sos.jitl.notification.jobs.notifier;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.jitl.notification.helper.NotificationMail;
import com.sos.jitl.notification.helper.settings.MailSettings;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Mail;
import sos.util.SOSString;

public class SystemNotifierJobJSAdapterClass extends JobSchedulerJobAdapter {

    private SystemNotifierJob job;

    @Override
    public boolean spooler_init() {
        try {
            job = new SystemNotifierJob();
            SystemNotifierJobOptions options = job.getOptions();
            options.setCurrentNodeName(this.getCurrentNodeName());
            options.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
            job.setJSJobUtilites(this);
            job.setJSCommands(this);

            if (SOSString.isEmpty(options.hibernate_configuration_file_reporting.getValue())) {
                options.hibernate_configuration_file_reporting.setValue(getHibernateConfigurationReporting().toString());
            }

            Mail mail = spooler_log.mail();

            MailSettings mailSettings = new MailSettings();
            mailSettings.setIniPath(spooler.ini_path());
            mailSettings.setSmtp(mail.smtp());
            mailSettings.setQueueDir(mail.queue_dir());
            mailSettings.setFrom(mail.from());
            mailSettings.setTo(mail.to());
            mailSettings.setCc(mail.cc());
            mailSettings.setBcc(mail.bcc());

            options.scheduler_mail_settings.setValue(NotificationMail.getSchedulerMailOptions(mailSettings));
            job.init(spooler);
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.toString(), e);
        }
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() throws Exception {

        try {
            super.spooler_process();

            SystemNotifierJobOptions options = job.getOptions();
            options.setCurrentNodeName(this.getCurrentNodeName());
            options.setAllOptions(getSchedulerParameterAsProperties(getParameters()));

            job.openSession();
            job.execute();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.toString(), e);
        } finally {
            job.closeSession();
        }
        return signalSuccess();

    }

    @Override
    public void spooler_close() throws Exception {
        if (job != null) {
            job.exit();
        }
        super.spooler_close();
    }
}
