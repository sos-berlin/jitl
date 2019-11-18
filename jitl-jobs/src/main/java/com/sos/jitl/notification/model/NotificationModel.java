package com.sos.jitl.notification.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import com.sos.jitl.xmleditor.common.JobSchedulerXmlEditor;

public class NotificationModel {

    public static final String OPERATION_ACKNOWLEDGE = "acknowledge";
    public static final String OPERATION_RESET_SERVICES = "reset_services";
    public static final String DEFAULT_SYSTEM_ID = "MonitorSystem";

    public enum NotificationType {
        ERROR, SUCCESS, RECOVERY, CHECK
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationModel.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private DBLayerSchedulerMon dbLayer = null;

    public NotificationModel() {
    }

    public NotificationModel(SOSHibernateSession sess) throws Exception {
        createDbLayer(sess);
    }

    public void createDbLayer(SOSHibernateSession sess) throws Exception {
        if (sess == null) {
            throw new Exception("SOSHibernateSession is NULL");
        }
        dbLayer = new DBLayerSchedulerMon(sess);
    }

    public DBLayerSchedulerMon getDbLayer() {
        return dbLayer;
    }

    public static File[] getFiles(File dir, String regex) {

        return dir.listFiles(new RegExFilenameFilter(regex));
    }

    public static File[] getDirectoryFiles(File dir) {
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

    public List<File> getAllConfigFiles(Path configDirectory) throws Exception {
        List<File> result = new ArrayList<File>();
        File defaultConfiguration = getDefaultNotificationXml();
        if (defaultConfiguration.exists()) {
            result.add(defaultConfiguration);
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s]default configuration not found", defaultConfiguration.toString()));
            }
        }
        Path notificationConfig = configDirectory.resolve("notification");
        File[] files = getDirectoryFiles(notificationConfig.toFile());
        if (files != null && files.length > 0) {
            result.addAll(Arrays.asList(files));
        }

        if (result.size() == 0) {
            throw new Exception(String.format("[%s][%s]missing configuration", normalizePath(notificationConfig.toFile()), normalizePath(
                    defaultConfiguration)));
        }
        return result;
    }

    public static File getDefaultNotificationXml() {
        return new File("config/live/" + JobSchedulerXmlEditor.getLivePathNotificationXml());
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

    public static String normalizePath(File f) {
        try {
            return f.getCanonicalPath().replaceAll("\\\\", "/");
        } catch (IOException e) {
            return f.getAbsolutePath().replaceAll("\\\\", "/");
        }
    }

}
