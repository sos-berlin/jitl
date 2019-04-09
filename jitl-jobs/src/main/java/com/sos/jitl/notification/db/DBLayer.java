package com.sos.jitl.notification.db;

import java.util.Date;

import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.ClassList;
import com.sos.hibernate.classes.SOSHibernateSession;

public class DBLayer {

    final Logger LOGGER = LoggerFactory.getLogger(DBLayer.class);

    public final static String SCHEDULER_VARIABLES_NOTIFICATION = "notification_date";
    public final static String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** Table SCHEDULER_MON_NOTIFICATIONS */
    public final static String DBITEM_SCHEDULER_MON_NOTIFICATIONS = DBItemSchedulerMonNotifications.class.getSimpleName();
    public final static String TABLE_SCHEDULER_MON_NOTIFICATIONS = "SCHEDULER_MON_NOTIFICATIONS";
    public final static String SEQUENCE_SCHEDULER_MON_NOTIFICATIONS = "SCHEDULER_MON_NOT_ID_SEQ";

    /** Table SCHEDULER_MON_INT_NOTIFICATIONS */
    public final static String DBITEM_SCHEDULER_MON_INTERNAL_NOTIFICATIONS = DBItemSchedulerMonInternalNotifications.class.getSimpleName();
    public final static String TABLE_SCHEDULER_MON_INTERNAL_NOTIFICATIONS = "SCHEDULER_MON_INTNOTIFICATIONS";
    public final static String SEQUENCE_SCHEDULER_MON_INTERNAL_NOTIFICATIONS = "SCHEDULER_MON_INTNOT_ID_SEQ";

    /** Table SCHEDULER_MON_RESULTS */
    public final static String DBITEM_SCHEDULER_MON_RESULTS = DBItemSchedulerMonResults.class.getSimpleName();
    public final static String TABLE_SCHEDULER_MON_RESULTS = "SCHEDULER_MON_RESULTS";
    public final static String SEQUENCE_SCHEDULER_MON_RESULTS = "SCHEDULER_MON_RES_ID_SEQ";

    /** Table SCHEDULER_MON_SYSNOTIFICATIONS */
    public final static String DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS = DBItemSchedulerMonSystemNotifications.class.getSimpleName();
    public final static String TABLE_SCHEDULER_MON_SYSNOTIFICATIONS = "SCHEDULER_MON_SYSNOTIFICATIONS";
    public final static String SEQUENCE_SCHEDULER_MON_SYSNOTIFICATIONS = "SCHEDULER_MON_SYSNOT_ID_SEQ";

    /** Table SCHEDULER_MON_SYSRESULTS */
    public final static String DBITEM_SCHEDULER_MON_SYSRESULTS = DBItemSchedulerMonSystemResults.class.getSimpleName();
    public final static String TABLE_SCHEDULER_MON_SYSRESULTS = "SCHEDULER_MON_SYSRESULTS";
    public final static String SEQUENCE_SCHEDULER_MON_SYSRESULTS = "SCHEDULER_MON_SYSR_ID_SEQ";

    /** Table SCHEDULER_MON_CHECKS */
    public final static String DBITEM_SCHEDULER_MON_CHECKS = DBItemSchedulerMonChecks.class.getSimpleName();
    public final static String TABLE_SCHEDULER_MON_CHECKS = "SCHEDULER_MON_CHECKS";
    public final static String SEQUENCE_SCHEDULER_MON_CHECKS = "SCHEDULER_MON_CHECKS_ID_SEQ";

    /** in seconds */
    public final static int RERUN_TRANSACTION_INTERVAL = 3;
    public final static String EMPTY_TEXT_VALUE = "";
    public final static String DEFAULT_EMPTY_NAME = "*";
    public final static Long DEFAULT_EMPTY_NUMERIC = new Long(0);

    public static final Long NOTIFICATION_OBJECT_TYPE_JOB_CHAIN = new Long(0);
    public static final Long NOTIFICATION_OBJECT_TYPE_JOB = new Long(1);
    public static final Long NOTIFICATION_OBJECT_TYPE_INTERNAL_TASK_IF_LONGER_THAN = new Long(10);
    public static final Long NOTIFICATION_OBJECT_TYPE_INTERNAL_TASK_IF_SHORTER_THAN = new Long(11);
    public static final Long NOTIFICATION_OBJECT_TYPE_INTERNAL_MASTER_MESSAGES = new Long(20);
    public static final Long NOTIFICATION_OBJECT_TYPE_DUMMY = new Long(100);
    public static final Long NOTIFICATION_DUMMY_MAX_STEP = new Long(999999);

    private SOSHibernateSession session;

    public DBLayer(SOSHibernateSession sess) {
        session = sess;
    }

    public SOSHibernateSession getSession() {
        return session;
    }

    public static ClassList getNotificationClassMapping() {
        ClassList cl = new ClassList();
        cl.add(DBItemSchedulerMonChecks.class);
        cl.add(DBItemSchedulerMonNotifications.class);
        cl.add(DBItemSchedulerMonResults.class);
        cl.add(DBItemSchedulerMonSystemNotifications.class);
        cl.add(DBItemSchedulerMonSystemResults.class);
        cl.add(DBItemSchedulerMonInternalNotifications.class);
        cl.add(DBItemReportingTaskAndOrder.class);
        return cl;
    }

    public String quote(String fieldName) {
        return session.getFactory().quoteColumn(fieldName);
    }

    public static Date getCurrentDateTime() {
        return new DateTime(DateTimeZone.UTC).toLocalDateTime().toDate();
    }

    public static Date getCurrentDateTimeMinusDays(int days) {
        return new DateTime(DateTimeZone.UTC).toLocalDateTime().minusDays(days).toDate();
    }

    public static Date getCurrentDateTimeMinusMinutes(int minutes) {
        return new DateTime(DateTimeZone.UTC).toLocalDateTime().minusMinutes(minutes).toDate();
    }

    public static String getDateAsString(Date d) throws Exception {
        DateTimeFormatter f = DateTimeFormat.forPattern(DATETIME_FORMAT);
        DateTime dt = new DateTime(d, DateTimeZone.UTC);
        return f.print(dt);
    }

    public static String getLocalDateAsString(Date d) throws Exception {
        DateTimeFormatter f = DateTimeFormat.forPattern(DATETIME_FORMAT);
        DateTime dt = new DateTime(d);
        return f.print(dt);
    }

    public static Date getDateFromString(String d) throws Exception {
        DateTimeFormatter f = DateTimeFormat.forPattern(DATETIME_FORMAT);
        return f.parseDateTime(d).toDate();
    }

    public void flushScrollableResults(int readCount) throws Exception {
        // Moreover if session cache is enabled,
        // you need to add explicit code to clear the session cache,
        // such as a code snippet here to clear cache every 100 rows:

        if (readCount % 100 == 0) {
            if (getSession().getCurrentSession() instanceof Session) {
                Session s = (Session) getSession().getCurrentSession();
                s.clear();
                s.flush();
            }
        }
    }

}
