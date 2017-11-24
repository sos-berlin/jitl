package com.sos.jitl.notification.model;

import java.util.HashMap;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Options.JSMailOptions;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.notification.db.DBLayer;
import com.sos.jitl.notification.helper.NotificationMail.MailHeaderKeyName;
import com.sos.jitl.notification.helper.NotificationMail.MailServerKeyName;
import com.sos.jitl.notification.jobs.notifier.SystemNotifierJobOptions;
import com.sos.jitl.notification.model.notifier.SystemNotifierModel;

import sos.settings.SOSProfileSettings;
import sos.settings.SOSSettings;

public class SystemNotifierModelTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemNotifierModelTest.class);

    private SOSHibernateFactory factory;
    private SOSHibernateSession session;

    private SystemNotifierJobOptions options;

    public SystemNotifierModelTest(SystemNotifierJobOptions opt) {
        options = opt;
    }

    public void init() throws Exception {
        try {
            factory = new SOSHibernateFactory(options.hibernate_configuration_file_reporting.getValue());
            factory.setIdentifier("notification");
            factory.setAutoCommit(options.connection_autocommit.value());
            factory.setTransactionIsolation(options.connection_transaction_isolation.value());
            factory.addClassMapping(DBLayer.getNotificationClassMapping());
            factory.build();

            session = factory.openStatelessSession();
        } catch (Exception ex) {
            throw new Exception(String.format("reporting connection: %s", ex.toString()));
        }
    }

    public void exit() {
        if (session != null) {
            session.close();
        }
        if (factory != null) {
            factory.close();
        }
    }

    public static JSMailOptions getMailOptions() throws Exception {
        SOSSettings settings = new SOSProfileSettings(Config.FACTORY_INI);
        Properties p = settings.getSection("spooler");

        JSMailOptions mo = new JSMailOptions();
        HashMap<String, String> ms = new HashMap<String, String>();
        ms.put(MailServerKeyName.SCHEDULER_INI_PATH, Config.FACTORY_INI);
        ms.put(MailServerKeyName.SMTP_HOST, p.getProperty("smtp"));
        ms.put(MailServerKeyName.QUEUE_DIR, p.getProperty("mail_queue_dir"));
        ms.put(MailHeaderKeyName.FROM, p.getProperty("log_mail_from"));
        ms.put(MailHeaderKeyName.TO, p.getProperty("log_mail_to"));
        ms.put(MailHeaderKeyName.CC, p.getProperty("log_mail_cc"));
        ms.put(MailHeaderKeyName.BCC, p.getProperty("log_mail_bcc"));
        mo.setAllOptions(ms);
        return mo;
    }

    public static void main(String[] args) throws Exception {

        SystemNotifierJobOptions opt = new SystemNotifierJobOptions();
        opt.hibernate_configuration_file_reporting.setValue(Config.HIBERNATE_CONFIGURATION_FILE);
        opt.schema_configuration_file.setValue(Config.SCHEMA_CONFIGURATION_FILE);
        opt.system_configuration_file.setValue(Config.SYSTEM_CONFIGURATION_FILE);
        opt.scheduler_mail_settings.setValue(getMailOptions());

        SystemNotifierModelTest t = new SystemNotifierModelTest(opt);

        try {
            LOGGER.info("START --");
            t.init();

            SystemNotifierModel model = new SystemNotifierModel(t.session, t.options, null);
            model.process();
            LOGGER.info("END --");

        } catch (Exception ex) {
            throw ex;
        } finally {
            t.exit();
        }

    }

}
