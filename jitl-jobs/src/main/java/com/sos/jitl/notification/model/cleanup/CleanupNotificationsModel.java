package com.sos.jitl.notification.model.cleanup;

import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.jobs.cleanup.CleanupNotificationsJobOptions;
import com.sos.jitl.notification.model.INotificationModel;
import com.sos.jitl.notification.model.NotificationModel;

import sos.util.SOSDate;

public class CleanupNotificationsModel extends NotificationModel implements INotificationModel {

    final Logger logger = LoggerFactory.getLogger(CleanupNotificationsModel.class);
    private CleanupNotificationsJobOptions options;

    public CleanupNotificationsModel(SOSHibernateSession sess, CleanupNotificationsJobOptions opt) throws Exception {
        super(sess);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        options = opt;
    }

    @Override
    public void process() throws Exception {
        try {
            Long minutes = SOSDate.resolveAge("m", options.age.getValue());
            Date date = DBLayerSchedulerMon.getCurrentDateTimeMinusMinutes(minutes.intValue());

            getDbLayer().getSession().beginTransaction();
            getDbLayer().cleanupNotifications(date);
            getDbLayer().getSession().commit();
        } catch (Exception ex) {
            getDbLayer().getSession().rollback();
            throw ex;
        }
    }

}
