package com.sos.jitl.notification.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hibernate.query.Query;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernate;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.reporting.db.DBItemAuditLog;
import com.sos.jitl.reporting.db.DBItemReportExecution;
import com.sos.jitl.reporting.db.DBItemReportTask;
import com.sos.jitl.reporting.db.DBItemReportTrigger;

import sos.util.SOSString;

public class DBLayerSchedulerMon extends DBLayer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerSchedulerMon.class);
    private static final String FROM = "from ";
    private static final String SERVICE_NAME = "serviceName";
    private static final String SYSTEM_ID = "systemId";
    private static final String UPDATE = "update ";

    /** result rerun interval in seconds */
    private static final long RERUN_INTERVAL = 2;
    private static final int MAX_RERUNS = 3;

    public DBLayerSchedulerMon(SOSHibernateSession sess) {
        super(sess);
    }

    public void cleanupNotifications(Date date) throws Exception {
        String method = "cleanupNotifications";

        LOGGER.info(String.format("[%s]delete <= %s", method, date));

        String hql = String.format("delete from %s where created <= :date", DBITEM_SCHEDULER_MON_NOTIFICATIONS);
        Query<?> query = getSession().createQuery(hql).setParameter("date", date);
        int count = getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]%s", method, TABLE_SCHEDULER_MON_NOTIFICATIONS, count));

        String notificationIdNotIn = String.format("notificationId not in (select id from %s)", DBITEM_SCHEDULER_MON_NOTIFICATIONS);

        hql = String.format("delete from %s where %s", DBITEM_SCHEDULER_MON_RESULTS, notificationIdNotIn);
        query = getSession().createQuery(hql);
        count = getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]%s", method, TABLE_SCHEDULER_MON_RESULTS, count));

        hql = String.format("delete from %s where %s", DBITEM_SCHEDULER_MON_CHECKS, notificationIdNotIn);
        query = getSession().createQuery(hql);
        count = getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]%s", method, TABLE_SCHEDULER_MON_CHECKS, count));

        hql = String.format("delete from %s where objectType in (%s,%s) and %s", DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS,
                DBLayer.NOTIFICATION_OBJECT_TYPE_JOB, DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN, notificationIdNotIn);
        query = getSession().createQuery(hql);
        count = getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]%s", method, TABLE_SCHEDULER_MON_SYSNOTIFICATIONS, count));

        hql = String.format("delete from %s where sysNotificationId not in (select id from %s)", DBITEM_SCHEDULER_MON_SYSRESULTS,
                DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
        query = getSession().createQuery(hql);
        count = getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]%s", method, TABLE_SCHEDULER_MON_SYSRESULTS, count));

        cleanupInternalNotifications(date);
    }

    private void cleanupInternalNotifications(Date date) throws Exception {
        String method = "cleanupInternalNotifications";

        String hql = String.format("delete from %s where created <= :date", DBITEM_SCHEDULER_MON_INTERNAL_NOTIFICATIONS);
        Query<?> query = getSession().createQuery(hql).setParameter("date", date);
        int count = getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]%s", method, TABLE_SCHEDULER_MON_INTERNAL_NOTIFICATIONS, count));

        String notificationIdNotIn = String.format("notificationId not in (select id from %s)", DBITEM_SCHEDULER_MON_INTERNAL_NOTIFICATIONS);

        hql = String.format("delete from %s where objectType in (%s,%s,%s,%s) and %s", DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS,
                DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_TASK_IF_LONGER_THAN, DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_TASK_IF_SHORTER_THAN,
                DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_TASK_WARNING, DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_MASTER_MESSAGE,
                notificationIdNotIn);
        query = getSession().createQuery(hql);
        count = getSession().executeUpdate(query);
        LOGGER.info(String.format("[%s][%s]%s", method, TABLE_SCHEDULER_MON_SYSNOTIFICATIONS, count));
    }

    public int resetAcknowledged(String systemId, String serviceName) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder(UPDATE);
        hql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
        hql.append(" set acknowledged = 1");
        hql.append(" where lower(systemId) = :systemId");
        if (!SOSString.isEmpty(serviceName)) {
            hql.append(" and serviceName =:serviceName");
        }
        Query<?> query = getSession().createQuery(hql.toString());
        query.setParameter(SYSTEM_ID, systemId.toLowerCase());
        if (!SOSString.isEmpty(serviceName)) {
            query.setParameter(SERVICE_NAME, serviceName);
        }
        return getSession().executeUpdate(query);
    }

    @SuppressWarnings("deprecation")
    public List<DBItemReportingTaskAndOrder> getReportingTaskAndOrder(String schedulerId, Long taskHistoryId) throws SOSHibernateException {
        String method = "getReportingTaskAndOrder";
        StringBuilder hql = new StringBuilder("select rt.schedulerId as schedulerId");
        hql.append(",rt.historyId as taskId");
        hql.append(",rt.isOrder as isOrder");
        hql.append(",rt.name as jobName");
        hql.append(",rt.title as jobTitle");
        hql.append(",rt.startTime as taskStartTime");
        hql.append(",rt.endTime as taskEndTime");
        hql.append(",rt.exitCode as exitCode");
        hql.append(",rt.agentUrl as agentUrl");
        hql.append(",rt.clusterMemberId as clusterMemberId");

        hql.append(",rtr.parentName as jobChainName");
        hql.append(",rtr.parentTitle as jobChainTitle");
        hql.append(",rtr.historyId as orderHistoryId");
        hql.append(",rtr.name as orderId");
        hql.append(",rtr.title as orderTitle");
        hql.append(",rtr.startTime as orderStartTime");
        hql.append(",rtr.endTime as orderEndTime");

        hql.append(",re.step as orderStep");
        hql.append(",re.state as orderStepState");
        hql.append(",re.startTime as orderStepStartTime");
        hql.append(",re.endTime as orderStepEndTime ");

        hql.append("from ").append(DBItemReportTask.class.getSimpleName()).append(" rt ");
        hql.append("left outer join ").append(DBItemReportExecution.class.getSimpleName()).append(" re ");
        hql.append("on re.taskId=rt.id ");
        hql.append("left outer join ").append(DBItemReportTrigger.class.getSimpleName()).append(" rtr ");
        hql.append("on re.triggerId=rtr.id ");

        hql.append("where rt.historyId = :taskHistoryId ");
        hql.append("and rt.schedulerId = :schedulerId");

        Query<DBItemReportingTaskAndOrder> query = getSession().createQuery(hql.toString());
        query.setParameter("taskHistoryId", taskHistoryId);
        query.setParameter("schedulerId", schedulerId);
        query.setResultTransformer(Transformers.aliasToBean(DBItemReportingTaskAndOrder.class));

        return executeQueryList(method, query);
    }

    public DBItemSchedulerMonInternalNotifications getInternalNotificationByTaskId(String schedulerId, Long taskId, Long objectType)
            throws SOSHibernateException {
        String method = "getInternalNotification";

        StringBuilder hql = new StringBuilder(FROM);
        hql.append(DBITEM_SCHEDULER_MON_INTERNAL_NOTIFICATIONS);
        hql.append(" where schedulerId = :schedulerId");
        hql.append(" and taskId = :taskId");
        hql.append(" and objectType = :objectType");

        Query<DBItemSchedulerMonInternalNotifications> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("taskId", taskId);
        query.setParameter("objectType", objectType);

        List<DBItemSchedulerMonInternalNotifications> result = executeQueryList(method, query);
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public List<DBItemSchedulerMonNotifications> getOrderNotifications(Optional<Integer> fetchSize, DBItemSchedulerMonNotifications notification)
            throws SOSHibernateException {
        String method = "getOrderNotifications";
        StringBuilder hql = new StringBuilder(FROM);
        hql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);
        hql.append(" where schedulerId = :schedulerId");
        hql.append(" and orderHistoryId = :orderHistoryId");
        hql.append(" order by step");

        Query<DBItemSchedulerMonNotifications> query = getSession().createQuery(hql.toString());
        query.setReadOnly(true);
        if (fetchSize.isPresent()) {
            query.setFetchSize(fetchSize.get());
        }
        query.setParameter("schedulerId", notification.getSchedulerId());
        query.setParameter("orderHistoryId", notification.getOrderHistoryId());
        return executeQueryList(method, query);
    }

    public List<DBItemSchedulerMonNotifications> getOrderNotificationsByNotificationId(Long notificationId) throws SOSHibernateException {
        String method = "getNotificationOrderSteps";
        StringBuilder hql = new StringBuilder(FROM);
        hql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n1");
        hql.append(" where exists (");
        hql.append("   select n2.orderHistoryId ");
        hql.append("   from ");
        hql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n2");
        hql.append("   where n1.orderHistoryId = n2.orderHistoryId");
        hql.append("   and n2.id = :id ");
        hql.append(" )");
        hql.append(" order by n1.step");

        Query<DBItemSchedulerMonNotifications> query = getSession().createQuery(hql.toString());
        query.setParameter("id", notificationId);
        return executeQueryList(method, query);
    }

    public List<DBItemSchedulerMonResults> getNotificationResults(Long notificationId) throws SOSHibernateException {
        String method = "getNotificationResults";
        String hql = String.format("from %s r where r.notificationId = :id", DBITEM_SCHEDULER_MON_RESULTS);

        Query<DBItemSchedulerMonResults> query = getSession().createQuery(hql);
        query.setParameter("id", notificationId);
        return executeQueryList(method, query);
    }

    public int updateNotificationResults(Long newNotificationId, Long oldNotificationId) throws SOSHibernateException {
        String hql = String.format("update %s set notificationId = :newNotificationId where notificationId = :oldNotificationId",
                DBITEM_SCHEDULER_MON_RESULTS);

        Query<DBItemSchedulerMonResults> query = getSession().createQuery(hql);
        query.setParameter("newNotificationId", newNotificationId);
        query.setParameter("oldNotificationId", oldNotificationId);

        return getSession().executeUpdate(query);
    }

    public List<DBItemSchedulerMonChecks> getSchedulerMonChecksForSetTimer(Optional<Integer> fetchSize) throws SOSHibernateException {
        String method = "getSchedulerMonChecksForSetTimer";
        String hql = String.format("from %s where checked = 0", DBITEM_SCHEDULER_MON_CHECKS);

        Query<DBItemSchedulerMonChecks> query = getSession().createQuery(hql);
        query.setReadOnly(true);
        if (fetchSize.isPresent()) {
            query.setFetchSize(fetchSize.get());
        }
        return executeQueryList(method, query);
    }

    public DBItemSchedulerMonNotifications getNotification(String schedulerId, boolean standalone, Long taskId, Long step, Long orderHistoryId)
            throws SOSHibernateException {
        String method = "getNotification";
        StringBuilder hql = new StringBuilder(FROM);
        hql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);
        hql.append(" where schedulerId = :schedulerId");
        hql.append(" and standalone = :standalone");
        hql.append(" and taskId = :taskId");
        hql.append(" and step = :step");
        hql.append(" and orderHistoryId = :orderHistoryId ");

        Query<DBItemSchedulerMonNotifications> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("standalone", standalone);
        query.setParameter("taskId", taskId);
        query.setParameter("step", step);
        query.setParameter("orderHistoryId", orderHistoryId);

        List<DBItemSchedulerMonNotifications> result = executeQueryList(method, query);
        if (result != null && result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    public List<DBItemSchedulerMonNotifications> getNotificationsWithDummyStep(String schedulerId, boolean standalone, Long taskId, Long step,
            Long orderHistoryId) throws SOSHibernateException {
        String method = "getNotifications";
        StringBuilder hql = new StringBuilder(FROM);
        hql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);
        hql.append(" where schedulerId = :schedulerId");
        hql.append(" and standalone = :standalone");
        hql.append(" and taskId = :taskId");
        hql.append(" and step in :steps");
        hql.append(" and orderHistoryId = :orderHistoryId ");

        Query<DBItemSchedulerMonNotifications> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("standalone", standalone);
        query.setParameter("taskId", taskId);

        ArrayList<Long> steps = new ArrayList<Long>();
        steps.add(step);
        steps.add(DBLayer.NOTIFICATION_DUMMY_MAX_STEP);

        query.setParameterList("steps", steps);

        query.setParameter("orderHistoryId", orderHistoryId);
        return executeQueryList(method, query);
    }

    public List<DBItemSchedulerMonNotifications> getNotificationsByState(String schedulerId, boolean standalone, Long taskId, Long orderHistoryId,
            String state) throws SOSHibernateException {
        String method = "getNotificationsByState";
        StringBuilder hql = new StringBuilder(FROM);
        hql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);
        hql.append(" where schedulerId = :schedulerId");
        hql.append(" and standalone = :standalone");
        hql.append(" and taskId = :taskId");
        hql.append(" and orderHistoryId = :orderHistoryId ");
        hql.append(" and orderStepState = :state");

        Query<DBItemSchedulerMonNotifications> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("standalone", standalone);
        query.setParameter("taskId", taskId);
        query.setParameter("orderHistoryId", orderHistoryId);
        query.setParameter("state", state);
        return executeQueryList(method, query);
    }

    public DBItemSchedulerMonNotifications getNotificationByStep(String schedulerId, Long orderHistoryId, Long step) throws SOSHibernateException {
        String method = "getNotificationByStep";
        StringBuilder hql = new StringBuilder(FROM);
        hql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);
        hql.append(" where schedulerId = :schedulerId");
        hql.append(" and orderHistoryId = :orderHistoryId ");
        hql.append(" and step = :step");

        Query<DBItemSchedulerMonNotifications> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("orderHistoryId", orderHistoryId);
        query.setParameter("step", step);

        List<DBItemSchedulerMonNotifications> result = executeQueryList(method, query);
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public DBItemSchedulerMonNotifications getLastErrorNotificationByState(String schedulerId, Long orderHistoryId, String state, Long step)
            throws SOSHibernateException {
        String method = "getLastErrorNotificationByState";
        StringBuilder hql = new StringBuilder(FROM);
        hql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);
        hql.append(" where schedulerId = :schedulerId");
        hql.append(" and orderHistoryId = :orderHistoryId ");
        hql.append(" and orderStepState = :state");
        hql.append(" and step > :step");

        Query<DBItemSchedulerMonNotifications> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("orderHistoryId", orderHistoryId);
        query.setParameter("state", state);
        query.setParameter("step", step);

        List<DBItemSchedulerMonNotifications> result = executeQueryList(method, query);
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public DBItemSchedulerMonNotifications getNotification(Long id) throws SOSHibernateException {
        return getSession().get(DBItemSchedulerMonNotifications.class, id);
    }

    public DBItemSchedulerMonInternalNotifications getInternalNotification(Long id) throws SOSHibernateException {
        return getSession().get(DBItemSchedulerMonInternalNotifications.class, id);
    }

    public List<DBItemSchedulerMonSystemNotifications> getSystemNotifications4NotifyAgain(String systemId) throws SOSHibernateException {
        String method = "getSystemNotifications4NotifyAgain";
        StringBuilder hql = new StringBuilder(FROM);
        hql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
        hql.append(" where lower(systemId) = :systemId");
        hql.append(" and maxNotifications = false");
        hql.append(" and acknowledged = false");
        Query<DBItemSchedulerMonSystemNotifications> query = getSession().createQuery(hql.toString());
        query.setParameter(SYSTEM_ID, systemId.toLowerCase());
        return executeQueryList(method, query);
    }

    public DBItemSchedulerMonSystemNotifications getDummySystemNotification(String systemId) throws Exception {
        String method = "getDummySystemNotification";

        StringBuilder hql = new StringBuilder("from ");
        hql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS).append(" ");
        hql.append("where lower(systemId) = :systemId ");
        hql.append("and objectType = :objectType");

        Query<DBItemSchedulerMonSystemNotifications> query = getSession().createQuery(hql.toString());
        query.setParameter(SYSTEM_ID, systemId.toLowerCase());
        query.setParameter("objectType", DBLayer.NOTIFICATION_OBJECT_TYPE_DUMMY);
        List<DBItemSchedulerMonSystemNotifications> result = executeQueryList(method, query);
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public List<DBItemSchedulerMonNotifications> getNotifications4NotifyNew(String systemId, DBItemSchedulerMonSystemNotifications dummy)
            throws Exception {
        String method = "getNotifications4NotifyNew";

        Query<DBItemSchedulerMonNotifications> query = null;
        if (dummy == null || dummy.getNotificationId().equals(new Long(0))) {
            query = getSession().createQuery("from " + DBITEM_SCHEDULER_MON_NOTIFICATIONS);
        } else {
            StringBuilder hql = new StringBuilder(FROM).append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" ");
            hql.append("where id > :maxNotificationId");
            query = getSession().createQuery(hql.toString());
            query.setParameter("maxNotificationId", dummy.getNotificationId());
        }
        return executeQueryList(method, query);
    }

    public List<DBItemSchedulerMonSystemNotifications> getSystemNotifications(String systemId, String serviceName, Long notificationId)
            throws SOSHibernateException {
        String method = "getSystemNotifications";
        StringBuilder hql = new StringBuilder(FROM);
        hql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
        hql.append(" where notificationId = :notificationId");
        if (serviceName != null) {
            hql.append(" and serviceName = :serviceName ");
        }
        hql.append(" and lower(systemId) = :systemId");

        Query<DBItemSchedulerMonSystemNotifications> query = getSession().createQuery(hql.toString());
        query.setParameter("notificationId", notificationId);
        query.setParameter(SYSTEM_ID, systemId.toLowerCase());
        if (serviceName != null) {
            query.setParameter(SERVICE_NAME, serviceName);
        }
        return executeQueryList(method, query);
    }

    public DBItemSchedulerMonSystemNotifications getSystemNotification(String systemId, String serviceName, Long notificationId, Long checkId,
            Long objectType, boolean onSuccess, String stepFrom, String stepTo, String returnCodeFrom, String returnCodeTo)
            throws SOSHibernateException {
        String method = "getSystemNotification";

        StringBuilder hql = new StringBuilder(FROM);
        hql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
        hql.append(" where notificationId = :notificationId");
        hql.append(" and checkId = :checkId");
        hql.append(" and objectType = :objectType");
        hql.append(" and serviceName = :serviceName");
        hql.append(" and lower(systemId) = :systemId");
        hql.append(" and success = :success");
        if (stepFrom != null) {
            hql.append(" and stepFrom = :stepFrom");
        }
        if (stepTo != null) {
            hql.append(" and stepTo = :stepTo");
        }
        if (returnCodeFrom != null) {
            hql.append(" and returnCodeFrom = :returnCodeFrom");
        }
        if (returnCodeTo != null) {
            hql.append(" and returnCodeTo = :returnCodeTo");
        }
        Query<DBItemSchedulerMonSystemNotifications> query = getSession().createQuery(hql.toString());
        query.setParameter("notificationId", notificationId);
        query.setParameter("checkId", checkId);
        query.setParameter("objectType", objectType);
        query.setParameter(SERVICE_NAME, serviceName);
        query.setParameter(SYSTEM_ID, systemId.toLowerCase());
        query.setParameter("success", onSuccess);
        if (stepFrom != null) {
            query.setParameter("stepFrom", stepFrom);
        }
        if (stepTo != null) {
            query.setParameter("stepTo", stepTo);
        }
        if (returnCodeFrom != null) {
            query.setParameter("returnCodeFrom", returnCodeFrom);
        }
        if (returnCodeTo != null) {
            query.setParameter("returnCodeTo", returnCodeTo);
        }

        List<DBItemSchedulerMonSystemNotifications> result = executeQueryList(method, query);
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public DBItemSchedulerMonResults createResult(Long notificationId, String name, String value) {
        DBItemSchedulerMonResults item = new DBItemSchedulerMonResults();
        item.setNotificationId(notificationId);
        item.setName(name);
        item.setValue(value);
        item.setCreated(DBLayer.getCurrentDateTime());
        item.setModified(DBLayer.getCurrentDateTime());
        return item;
    }

    public List<DBItemSchedulerMonChecks> getChecksForNotifyTimer(Optional<Integer> fetchSize) throws SOSHibernateException {
        String method = "getChecksForNotifyTimer";
        String hql = String.format("from %s where checked = 1", DBITEM_SCHEDULER_MON_CHECKS);

        Query<DBItemSchedulerMonChecks> query = getSession().createQuery(hql);
        query.setReadOnly(true);
        if (fetchSize.isPresent()) {
            query.setFetchSize(fetchSize.get());
        }
        return executeQueryList(method, query);
    }

    public DBItemSchedulerMonNotifications getNotificationFirstStep(DBItemSchedulerMonNotifications notification) throws SOSHibernateException {
        String method = "getNotificationFirstStep";
        StringBuilder hql = new StringBuilder(FROM);
        hql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n ");
        hql.append("where n.schedulerId  = :schedulerId ");
        hql.append("and n.orderHistoryId = :orderHistoryId ");
        hql.append("and n.step = 1");

        Query<DBItemSchedulerMonNotifications> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", notification.getSchedulerId());
        query.setParameter("orderHistoryId", notification.getOrderHistoryId());
        List<DBItemSchedulerMonNotifications> result = executeQueryList(method, query);
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public DBItemSchedulerMonNotifications getNotificationMinStep(DBItemSchedulerMonNotifications notification) throws Exception {
        String method = "getNotificationMinStep";
        StringBuilder hql = new StringBuilder(FROM);
        hql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n1 ");
        hql.append("where n1.schedulerId = :schedulerId ");
        hql.append("and n1.orderHistoryId = :orderHistoryId ");
        hql.append("and n1.step = ");
        hql.append(" (select min(n2.step) from ");
        hql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n2 ");
        hql.append("where n2.schedulerId = n1.schedulerId ");
        hql.append("and n2.orderHistoryId = n1.orderHistoryId ");
        hql.append(" ) ");

        Query<DBItemSchedulerMonNotifications> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", notification.getSchedulerId());
        query.setParameter("orderHistoryId", notification.getOrderHistoryId());
        query.setReadOnly(true);

        List<DBItemSchedulerMonNotifications> result = executeQueryList(method, query);
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public DBItemSchedulerMonNotifications getNotificationMaxStep(DBItemSchedulerMonNotifications notification) throws Exception {
        String method = "getNotificationMaxStep";
        StringBuilder hql = new StringBuilder(FROM);
        hql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n1 ");
        hql.append("where n1.schedulerId = :schedulerId ");
        hql.append("and n1.orderHistoryId = :orderHistoryId ");
        hql.append("and n1.step = ");
        hql.append(" (select max(n2.step) from ");
        hql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n2 ");
        hql.append("where n2.schedulerId = n1.schedulerId ");
        hql.append("and n2.orderHistoryId = n1.orderHistoryId ");
        hql.append(" ) ");

        Query<DBItemSchedulerMonNotifications> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", notification.getSchedulerId());
        query.setParameter("orderHistoryId", notification.getOrderHistoryId());
        query.setReadOnly(true);

        List<DBItemSchedulerMonNotifications> result = executeQueryList(method, query);
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    private <T> List<T> executeQueryList(String callerMethodName, Query<T> query) throws SOSHibernateException {
        List<T> result = null;
        int count = 0;
        boolean run = true;
        while (run) {
            count++;
            try {
                result = getSession().getResultList(query);
                run = false;
            } catch (Exception e) {
                if (count >= MAX_RERUNS) {
                    throw e;
                } else {
                    Throwable te = SOSHibernate.findLockException(e);
                    if (te == null) {
                        throw e;
                    } else {
                        LOGGER.warn(String.format("%s: %s occured, wait %ss and try again (%s of %s) ...", callerMethodName, te.getClass().getName(),
                                RERUN_INTERVAL, count, MAX_RERUNS));
                        try {
                            Thread.sleep(RERUN_INTERVAL * 1000);
                        } catch (InterruptedException e1) {
                        }
                    }
                }
            }
        }
        return result;
    }

    public int removeCheck(Long checkId) throws SOSHibernateException {
        String hql = String.format("delete from %s where id = :id", DBITEM_SCHEDULER_MON_CHECKS);
        Query<?> query = getSession().createQuery(hql);
        query.setParameter("id", checkId);
        return getSession().executeUpdate(query);
    }

    public DBItemSchedulerMonChecks getCheck(String name, Long notificationId, Long objectType, String stepFrom, String stepTo)
            throws SOSHibernateException {
        String method = "getCheck";
        StringBuilder hql = new StringBuilder("from ").append(DBITEM_SCHEDULER_MON_CHECKS);
        hql.append(" where notificationId = :notificationId");
        hql.append(" and name = :name");
        hql.append(" and objectType = :objectType");
        hql.append(" and stepFrom = :stepFrom");
        hql.append(" and stepTo = :stepTo");

        Query<DBItemSchedulerMonChecks> query = getSession().createQuery(hql.toString());
        query.setReadOnly(true);
        query.setParameter("notificationId", notificationId);
        query.setParameter("name", name);
        query.setParameter("objectType", objectType);
        query.setParameter("stepFrom", stepFrom);
        query.setParameter("stepTo", stepTo);

        List<DBItemSchedulerMonChecks> results = executeQueryList(method, query);
        if (results != null && results.size() > 0) {
            return results.get(0);
        }
        return null;
    }

    public DBItemSchedulerMonSystemNotifications createSystemNotification(String systemId, String serviceName, Long notificationId, Long checkId,
            String returnCodeFrom, String returnCodeTo, Long objectType, String stepFrom, String stepTo, Date stepFromStartTime, Date stepToEndTime,
            Long currentNotification, Long notifications, boolean acknowledged, boolean recovered, boolean success) {
        DBItemSchedulerMonSystemNotifications item = new DBItemSchedulerMonSystemNotifications();
        item.setSystemId(systemId);
        item.setServiceName(serviceName);
        item.setNotificationId(notificationId);
        item.setCheckId(checkId);
        item.setReturnCodeFrom(returnCodeFrom);
        item.setReturnCodeTo(returnCodeTo);
        item.setObjectType(objectType);
        item.setStepFrom(stepFrom);
        item.setStepTo(stepTo);
        item.setStepFromStartTime(stepFromStartTime);
        item.setStepToEndTime(stepToEndTime);
        item.setMaxNotifications(false);
        item.setCurrentNotification(currentNotification);
        item.setNotifications(notifications);
        item.setAcknowledged(acknowledged);
        item.setRecovered(recovered);
        item.setSuccess(success);
        item.setCreated(DBLayer.getCurrentDateTime());
        item.setModified(DBLayer.getCurrentDateTime());
        return item;
    }

    public DBItemSchedulerMonSystemNotifications createDummySystemNotification(String systemId, Long notificationId) {
        String serviceName = DBLayer.DEFAULT_EMPTY_NAME;
        Long checkId = new Long(0);
        String returnCodeFrom = DBLayer.DEFAULT_EMPTY_NAME;
        String returnCodeTo = DBLayer.DEFAULT_EMPTY_NAME;
        Long objectType = DBLayer.NOTIFICATION_OBJECT_TYPE_DUMMY;
        String stepFrom = DBLayer.DEFAULT_EMPTY_NAME;
        String stepTo = DBLayer.DEFAULT_EMPTY_NAME;
        Date stepFromStartTime = null;
        Date stepToEndTime = null;
        Long currentNotification = new Long(0);
        Long notifications = new Long(0);
        boolean acknowledged = false;
        boolean recovered = false;
        boolean success = false;

        DBItemSchedulerMonSystemNotifications sm = createSystemNotification(systemId, serviceName, notificationId, checkId, returnCodeFrom,
                returnCodeTo, objectType, stepFrom, stepTo, stepFromStartTime, stepToEndTime, currentNotification, notifications, acknowledged,
                recovered, success);
        sm.setMaxNotifications(true);
        return sm;
    }

    public DBItemSchedulerMonNotifications createNotification(String schedulerId, boolean standalone, Long taskId, Long step, Long orderHistoryId,
            String jobChainName, String jobChainTitle, String orderId, String orderTitle, Date orderStartTime, Date orderEndTime,
            String orderStepState, Date orderStepStartTime, Date orderStepEndTime, String jobName, String jobTitle, Date taskStartTime,
            Date taskEndTime, boolean recovered, Long returnCode, String agentUrl, String clusterMemberId, String criticality, boolean error,
            String errorCode, String errorText) throws Exception {
        DBItemSchedulerMonNotifications item = new DBItemSchedulerMonNotifications();
        // set unique key
        item.setSchedulerId(schedulerId);
        item.setStandalone(standalone);
        item.setTaskId(taskId);
        item.setStep(step);
        item.setOrderHistoryId(orderHistoryId);
        // set others
        item.setJobChainName(jobChainName);
        item.setJobChainTitle(jobChainTitle);
        item.setOrderId(orderId);
        item.setOrderTitle(orderTitle);
        item.setOrderStartTime(orderStartTime);
        item.setOrderEndTime(orderEndTime);
        item.setOrderStepState(orderStepState);
        item.setOrderStepStartTime(orderStepStartTime);
        item.setOrderStepEndTime(orderStepEndTime);
        item.setJobName(jobName);
        item.setJobTitle(jobTitle);
        item.setTaskStartTime(taskStartTime);
        item.setTaskEndTime(taskEndTime);
        item.setRecovered(recovered);
        item.setReturnCode(returnCode);
        item.setAgentUrl(agentUrl);
        item.setClusterMemberId(clusterMemberId);
        item.setJobCriticality(criticality);
        item.setError(error);
        item.setErrorCode(errorCode);
        item.setErrorText(errorText);
        item.setCreated(DBLayer.getCurrentDateTime());
        item.setModified(DBLayer.getCurrentDateTime());
        return item;
    }

    public DBItemSchedulerMonSystemResults getSystemResult(Long sysNotificationId, Long notificationId) throws Exception {
        String method = "getSystemResult";
        StringBuilder sql = new StringBuilder(FROM);
        sql.append(DBITEM_SCHEDULER_MON_SYSRESULTS);
        sql.append(" where sysNotificationId = :sysNotificationId and ");
        sql.append(" notificationId = :notificationId");

        Query<DBItemSchedulerMonSystemResults> query = getSession().createQuery(sql.toString());
        query.setParameter("sysNotificationId", sysNotificationId);
        query.setParameter("notificationId", notificationId);

        List<DBItemSchedulerMonSystemResults> result = executeQueryList(method, query);
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public DBItemSchedulerMonSystemResults createSystemResult(DBItemSchedulerMonSystemNotifications sm,
            DBItemSchedulerMonNotifications notification) {
        DBItemSchedulerMonSystemResults dbItem = new DBItemSchedulerMonSystemResults();
        dbItem.setSysNotificationId(sm.getId());
        dbItem.setNotificationId(notification.getId());
        dbItem.setOrderStep(notification.getStep());
        dbItem.setOrderStepState(notification.getOrderStepState());
        dbItem.setOrderStepEndTime(notification.getOrderStepEndTime());
        dbItem.setRecovered(false);
        dbItem.setCurrentNotification(new Long(0));
        dbItem.setCreated(DBLayer.getCurrentDateTime());
        dbItem.setModified(DBLayer.getCurrentDateTime());
        return dbItem;
    }

    public DBItemSchedulerMonSystemResults getSystemResultMaxStep(Long sysNotificationId) throws Exception {
        String method = "getSystemResultLastStep";
        StringBuilder sql = new StringBuilder("from ");
        sql.append(DBITEM_SCHEDULER_MON_SYSRESULTS).append(" s1 ");
        sql.append("where s1.sysNotificationId = :sysNotificationId");
        sql.append(" and s1.orderStep = ");
        sql.append(" (select max(s2.orderStep) from ");
        sql.append(DBITEM_SCHEDULER_MON_SYSRESULTS).append(" s2 ");
        sql.append("where s2.sysNotificationId = s1.sysNotificationId)");

        Query<DBItemSchedulerMonSystemResults> query = getSession().createQuery(sql.toString());
        query.setParameter("sysNotificationId", sysNotificationId);
        query.setReadOnly(true);

        List<DBItemSchedulerMonSystemResults> result = executeQueryList(method, query);
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public int removeNotification(DBItemSchedulerMonNotifications notification) throws Exception {
        String hql = String.format("delete from %s where id = :id", DBITEM_SCHEDULER_MON_NOTIFICATIONS);
        Query<DBItemSchedulerMonNotifications> query = getSession().createQuery(hql);
        query.setParameter("id", notification.getId());
        return getSession().executeUpdate(query);
    }

    public int removeSystemNotification(DBItemSchedulerMonSystemNotifications sysNotification) throws Exception {
        String hql = String.format("delete from %s where id = :id", DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
        Query<DBItemSchedulerMonSystemNotifications> query = getSession().createQuery(hql);
        query.setParameter("id", sysNotification.getId());
        int count = getSession().executeUpdate(query);

        hql = String.format("delete from %s where sysNotificationId = :id", DBITEM_SCHEDULER_MON_SYSRESULTS);
        query = getSession().createQuery(hql);
        query.setParameter("id", sysNotification.getId());
        count += getSession().executeUpdate(query);
        return count;
    }

    public List<DBItemSchedulerMonSystemResults> getSystemResults(DBItemSchedulerMonNotifications notification, String systemId) throws Exception {
        String method = "getSystemResults";
        StringBuilder sql = new StringBuilder(FROM);
        sql.append(DBITEM_SCHEDULER_MON_SYSRESULTS);
        sql.append(" where sysNotificationId in ");
        sql.append("(");
        sql.append("select id from ");
        sql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
        sql.append(" where notificationId= :notificationId ");
        sql.append("and checkId = 0 ");
        sql.append("and objectType= :objectType ");
        sql.append("and lower(systemId) = :systemId ");
        sql.append(")");
        Query<DBItemSchedulerMonSystemResults> query = getSession().createQuery(sql.toString());
        query.setParameter("notificationId", notification.getId());
        query.setParameter("objectType", DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN);
        query.setParameter("systemId", systemId.toLowerCase());

        return executeQueryList(method, query);
    }

    public int setNotificationsOrderEndTime(String schedulerId, Long orderHistoryId, Date orderEndTime) throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("update ");
        hql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" ");
        hql.append("set orderEndTime=:orderEndTime");
        hql.append(",modified=:modified ");
        hql.append("where schedulerId=:schedulerId ");
        hql.append("and orderHistoryId=:orderHistoryId");

        Query<DBItemSchedulerMonResults> query = getSession().createQuery(hql.toString());
        query.setParameter("orderEndTime", orderEndTime);
        query.setParameter("modified", DBLayer.getCurrentDateTime());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("orderHistoryId", orderHistoryId);

        return getSession().executeUpdate(query);
    }

    public Long getMinAuditLogId(String schedulerId, String jobChain, String orderId, Date startTimeFrom, Date startTimeTo)
            throws SOSHibernateException {
        StringBuilder hql = new StringBuilder("select min(id) from ").append(DBItemAuditLog.class.getSimpleName()).append(" ");
        hql.append("where schedulerId=:schedulerId ");
        hql.append("and jobChain=:jobChain ");
        hql.append("and orderId=:orderId ");
        hql.append("and startTime >=:startTimeFrom ");
        if (startTimeTo != null) {
            hql.append("and startTime < :startTimeTo");
        }

        Query<Long> query = getSession().createQuery(hql.toString());
        query.setParameter("schedulerId", schedulerId);
        query.setParameter("jobChain", jobChain);
        query.setParameter("orderId", orderId);
        query.setParameter("startTimeFrom", startTimeFrom);
        if (startTimeTo != null) {
            query.setParameter("startTimeTo", startTimeTo);
        }

        return getSession().getSingleValue(query);
    }
}
