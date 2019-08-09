package com.sos.jitl.notification.model;

import java.io.File;
import java.util.Locale;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.helper.RegExFilenameFilter;

public class NotificationModel {

    final Logger logger = LoggerFactory.getLogger(NotificationModel.class);
    DBLayerSchedulerMon dbLayer = null;

    public static final String OPERATION_ACKNOWLEDGE = "acknowledge";
    public static final String OPERATION_RESET_SERVICES = "reset_services";

    public enum NotificationType {
        ERROR, SUCCESS, RECOVERY, CHECK
    }

    public NotificationModel() {
    }

    public NotificationModel(SOSHibernateSession sess) throws Exception {
        if (sess == null) {
            throw new Exception("connection is NULL");
        }
        dbLayer = new DBLayerSchedulerMon(sess);
    }

    public void setConnection(SOSHibernateSession conn) throws Exception {
        if (conn == null) {
            throw new Exception("connection is NULL");
        }
        dbLayer = new DBLayerSchedulerMon(conn);
    }

    public DBLayerSchedulerMon getDbLayer() {
        return dbLayer;
    }

    public static File[] getFiles(File dir, String regex) {

        return dir.listFiles(new RegExFilenameFilter(regex));
    }

    public static File[] getAllConfigurationFiles(File dir) {
        String regex = "^SystemMonitorNotificationTimers\\.xml$|(^SystemMonitorNotification_){1}(.)*\\.xml$";

        return getFiles(dir, regex);
    }

    public static File[] getConfigurationFiles(File dir) {
        String regex = "(^SystemMonitorNotification_){1}(.)*\\.xml$";

        return getFiles(dir, regex);
    }

    public static File getTimerConfigurationFileX(File dir) {
        File f = new File(dir, "SystemMonitorNotificationTimers.xml");
        return f.exists() ? f : null;
    }

    public static File getConfigurationSchemaFile(File dir) {
        String regex = "(^SystemMonitorNotification_){1}(.)*\\.xsd$";

        File[] result = getFiles(dir, regex);
        if (result.length > 0) {
            return result[0];
        }
        return null;
    }

    public static String getDuration(DateTime startTime, DateTime endTime) {
        Duration duration = new Duration(startTime, endTime);
        Period period = duration.toPeriod().normalizedStandard(PeriodType.time());
        return PeriodFormat.wordBased(Locale.ENGLISH).print(period);
    }

    public static String toString(Object o) {
        if (o == null) {
            return null;
        }
        try {
            return ReflectionToStringBuilder.toString(o, ToStringStyle.SHORT_PREFIX_STYLE);
        } catch (Throwable t) {
        }
        return o.toString();
    }

}
