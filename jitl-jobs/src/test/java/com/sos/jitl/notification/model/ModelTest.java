package com.sos.jitl.notification.model;

import java.util.HashMap;
import java.util.Properties;

import com.sos.JSHelper.Options.JSMailOptions;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.notification.db.DBLayer;
import com.sos.jitl.notification.helper.NotificationMail.MailHeaderKeyName;
import com.sos.jitl.notification.helper.NotificationMail.MailServerKeyName;

import sos.settings.SOSProfileSettings;
import sos.settings.SOSSettings;

public abstract class ModelTest {

    private SOSHibernateFactory factory;
    private SOSHibernateSession session;

    public void init(String hibernateConfigurationFile) throws Exception {
        factory = new SOSHibernateFactory(hibernateConfigurationFile);
        factory.setIdentifier("notification");
        factory.setAutoCommit(false);
        factory.addClassMapping(DBLayer.getNotificationClassMapping());
        factory.build();
        session = factory.openStatelessSession();
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
        SOSSettings settings = new SOSProfileSettings(ConfigTest.FACTORY_INI);
        Properties p = settings.getSection("spooler");

        JSMailOptions mo = new JSMailOptions();
        HashMap<String, String> ms = new HashMap<String, String>();
        ms.put(MailServerKeyName.SCHEDULER_INI_PATH, ConfigTest.FACTORY_INI);
        ms.put(MailServerKeyName.SMTP_HOST, p.getProperty("smtp"));
        ms.put(MailServerKeyName.QUEUE_DIR, p.getProperty("mail_queue_dir"));
        ms.put(MailHeaderKeyName.FROM, p.getProperty("log_mail_from"));
        ms.put(MailHeaderKeyName.TO, p.getProperty("log_mail_to"));
        ms.put(MailHeaderKeyName.CC, p.getProperty("log_mail_cc"));
        ms.put(MailHeaderKeyName.BCC, p.getProperty("log_mail_bcc"));
        mo.setAllOptions(ms);
        return mo;
    }

    public SOSHibernateSession getSession() {
        return session;
    }
}
