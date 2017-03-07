package com.sos.jitl.notification.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.SOSHibernateStatelessSession;

import sos.util.SOSString;

public class DBLayerSchedulerMon extends DBLayer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerSchedulerMon.class);
    private static final String FROM_WITH_SPACES = " from ";
    private static final String FROM = "from ";
    private static final String WHERE_N2_EQUALS_N1 = "  where n2.orderHistoryId = n1.orderHistoryId ";
    private static final String DELETE_FROM = "delete from ";
    private static final String DELETE_COUNT = "deleted %s = %s";
    private static final String ORDER_HISTORY_ID = "orderHistoryId";
    private static final String SERVICE_NAME = "serviceName";
    private static final String SYSTEM_ID = "systemId";
    private static final String UPDATE = "update ";

    public DBLayerSchedulerMon(SOSHibernateStatelessSession conn) {
        super(conn);
    }

    public void cleanupNotifications(Date date) throws Exception {
        try {
            StringBuilder sql = new StringBuilder(DELETE_FROM);
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);
            sql.append(" where created <= :date");
            int count = getConnection().createQuery(sql.toString()).setTimestamp("date", date).executeUpdate();
            LOGGER.info(String.format(DELETE_COUNT, DBITEM_SCHEDULER_MON_NOTIFICATIONS, count));

            String whereNotificationIdNotIn = " where notificationId not in (select id from " + DBITEM_SCHEDULER_MON_NOTIFICATIONS + ")";

            sql = new StringBuilder(DELETE_FROM);
            sql.append(DBITEM_SCHEDULER_MON_RESULTS);
            sql.append(whereNotificationIdNotIn);
            count = getConnection().createQuery(sql.toString()).executeUpdate();
            LOGGER.info(String.format(DELETE_COUNT, DBITEM_SCHEDULER_MON_RESULTS, count));

            sql = new StringBuilder(DELETE_FROM);
            sql.append(DBITEM_SCHEDULER_MON_CHECKS);
            sql.append(whereNotificationIdNotIn);
            count = getConnection().createQuery(sql.toString()).executeUpdate();
            LOGGER.info(String.format(DELETE_COUNT, DBITEM_SCHEDULER_MON_CHECKS, count));

            sql = new StringBuilder(DELETE_FROM);
            sql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
            sql.append(whereNotificationIdNotIn);
            int countS1 = getConnection().createQuery(sql.toString()).executeUpdate();

            sql = new StringBuilder(DELETE_FROM);
            sql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
            sql.append(" where checkId > 0");
            sql.append(" and checkId not in (select id from " + DBITEM_SCHEDULER_MON_CHECKS + ")");
            int countS2 = getConnection().createQuery(sql.toString()).executeUpdate();
            count = countS1 + countS2;
            LOGGER.info(String.format(DELETE_COUNT, DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS, count));
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public int resetAcknowledged(String systemId, String serviceName) throws Exception {
        try {
            StringBuilder sql = new StringBuilder(UPDATE);
            sql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
            sql.append(" set acknowledged = 1");
            sql.append(" where lower(systemId) = :systemId");
            if (!SOSString.isEmpty(serviceName)) {
                sql.append(" and serviceName =:serviceName");
            }

            Query query = getConnection().createQuery(sql.toString());
            query.setParameter(SYSTEM_ID, systemId.toLowerCase());
            if (!SOSString.isEmpty(serviceName)) {
                query.setParameter(SERVICE_NAME, serviceName);
            }
            return query.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public List<DBItemSchedulerMonNotifications> getNotificationOrderSteps(Long notificationId) throws Exception {
        try {
            String method = "getNotificationOrderSteps";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n1");
            sql.append(" where exists (");
            sql.append("   select n2.orderHistoryId ");
            sql.append("   from ");
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n2");
            sql.append("   where n1.orderHistoryId = n2.orderHistoryId");
            sql.append("   and n2.id = :id ");
            sql.append(" )");
            sql.append(" order by n1.step");

            Query q = getConnection().createQuery(sql.toString());
            q.setParameter("id", notificationId);

            return executeQueryList(method, sql, q);
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public List<DBItemSchedulerMonResults> getNotificationResults(Long notificationId) throws Exception {
        try {
            String method = "getNotificationResults";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_RESULTS).append(" r");
            sql.append(" where r.notificationId = :id");

            Query q = getConnection().createQuery(sql.toString());
            q.setParameter("id", notificationId);

            return executeQueryList(method, sql, q);
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public List<DBItemSchedulerMonChecks> getSchedulerMonChecksForSetTimer(Optional<Integer> fetchSize) throws Exception {
        try {
            String method = "getSchedulerMonChecksForSetTimer";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_CHECKS);
            sql.append(" where checked = 0");

            Query q = getConnection().createQuery(sql.toString());
            q.setReadOnly(true);
            if (fetchSize.isPresent()) {
                q.setFetchSize(fetchSize.get());
            }

            return executeQueryList(method, sql, q);
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public void setNotificationCheck(DBItemSchedulerMonChecks check, Date stepFromStartTime, Date stepToEndTime, String text, String resultIds)
            throws Exception {
        try {
            check.setStepFromStartTime(stepFromStartTime);
            check.setStepToEndTime(stepToEndTime);
            check.setChecked(true);
            check.setCheckText(text);
            check.setResultIds(SOSString.isEmpty(resultIds) ? null : resultIds);
            check.setModified(DBLayer.getCurrentDateTime());
            getConnection().update(check);
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public void setNotificationCheckForRerun(DBItemSchedulerMonChecks check, Date stepFromStartTime, Date stepToEndTime, String text, String resultIds)
            throws Exception {
        try {
            check.setStepFromStartTime(stepFromStartTime);
            check.setStepToEndTime(stepToEndTime);
            check.setChecked(false);
            check.setCheckText("1");
            check.setResultIds(SOSString.isEmpty(resultIds) ? null : resultIds);
            check.setModified(DBLayer.getCurrentDateTime());
            getConnection().update(check);
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public int setRecovered(Long orderHistoryId,Long step, String state) throws Exception {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("from ");
            sb.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);
            sb.append("	where step <= :step");
            sb.append("	and orderHistoryId = :orderHistoryId");
            sb.append("	and orderStepState = :state");
            sb.append("	and error = true");
            
            Query query = getConnection().createQuery(sb.toString());
            query.setReadOnly(true);
            query.setParameter("orderHistoryId",orderHistoryId);
            query.setParameter("step",step);
            query.setParameter("state",state);
            
            List<DBItemSchedulerMonNotifications> r = query.list();
            if(r != null && r.size() > 0){
            	ArrayList<Long> ids = new ArrayList<Long>();
            	for(int i=0;i<r.size();i++){
            		ids.add(r.get(i).getId());
            	}
            	
            	sb = new StringBuilder("update ");
                sb.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);
                sb.append(" set recovered=true");
                sb.append(" where id in :ids");
                
                query = getConnection().createQuery(sb.toString());
                query.setParameterList("ids",ids);
                
                return query.executeUpdate();
            }
            return 0;
            
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public DBItemSchedulerMonNotifications getNotification(String schedulerId, boolean standalone, Long taskId, Long step, Long orderHistoryId, boolean checkDummyMaxStep)
            throws Exception {
        try {
            String method = "getNotification";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);
            sql.append(" where schedulerId = :schedulerId");
            sql.append(" and standalone = :standalone");
            sql.append(" and taskId = :taskId");
            if(checkDummyMaxStep){
            	sql.append(" and step in :steps");
            }
            else{
            	sql.append(" and step = :step");
            }
            sql.append(" and orderHistoryId = :orderHistoryId ");

            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("schedulerId", schedulerId);
            query.setParameter("standalone", standalone);
            query.setParameter("taskId", taskId);
            if(checkDummyMaxStep){
            	ArrayList<Long> steps = new ArrayList<Long>();
            	steps.add(step);
            	steps.add(DBLayer.NOTIFICATION_DUMMY_MAX_STEP);
             	query.setParameterList("steps", steps);
            }
            else{
            	query.setParameter("step", step);
            }
            query.setParameter(ORDER_HISTORY_ID, orderHistoryId);

            List<DBItemSchedulerMonNotifications> result = executeQueryList(method, sql, query);
            if (result != null && result.size() > 0) {
                int resultSize = result.size();
            	if(resultSize > 1){
                	for(int i=0;i<resultSize;i++){
                		DBItemSchedulerMonNotifications r = result.get(i);
                		if(r.getStep().equals(DBLayer.NOTIFICATION_DUMMY_MAX_STEP)){
                			return r;
                		}
                	}
                }
              	return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public List<DBItemSchedulerMonNotifications> getNotificationsByState(String schedulerId, boolean standalone, Long taskId, Long orderHistoryId, String state)
            throws Exception {
        try {
            String method = "getNotificationsByState";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);
            sql.append(" where schedulerId = :schedulerId");
            sql.append(" and standalone = :standalone");
            sql.append(" and taskId = :taskId");
            sql.append(" and orderHistoryId = :orderHistoryId ");
        	sql.append(" and orderStepState = :state");
            
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("schedulerId", schedulerId);
            query.setParameter("standalone", standalone);
            query.setParameter("taskId", taskId);
            query.setParameter(ORDER_HISTORY_ID, orderHistoryId);
            query.setParameter("state", state);
            
            return executeQueryList(method, sql, query);
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    
    @SuppressWarnings("unchecked")
    public DBItemSchedulerMonNotifications getNotification(Long id) throws Exception {
        try {
            String method = "getNotification";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);
            sql.append(" where id = :id ");

            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("id", id);

            List<DBItemSchedulerMonNotifications> result = executeQueryList(method, sql, query);
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public List<DBItemSchedulerMonSystemNotifications> getSystemNotifications4NotifyAgain(String systemId, Long objectType) throws Exception {
        try {
            String method = "getSystemNotifications4NotifyAgain";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
            sql.append(" where lower(systemId) = :systemId");
            sql.append(" and maxNotifications = false");
            sql.append(" and acknowledged = false");
            if (objectType != null) {
                sql.append(" and objectType = :objectType");
            }

            Query query = getConnection().createQuery(sql.toString());
            query.setParameter(SYSTEM_ID, systemId.toLowerCase());
            if (objectType != null) {
                query.setParameter("objectType", objectType);
            }

            return executeQueryList(method, sql, query);
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public List<DBItemSchedulerMonNotifications> getNotifications4NotifyNew(String systemId) throws Exception {
        try {
            String method = "getNotifications4NotifyNew";

            List<DBItemSchedulerMonNotifications> result = null;

            StringBuilder sql = new StringBuilder("select max(sn.notificationId) ");
            sql.append(FROM);
            sql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS).append(" sn");
            sql.append(" where lower(sn.systemId) = :systemId");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter(SYSTEM_ID, systemId.toLowerCase());

            Long maxNotificationId = (Long) query.uniqueResult();

            if (maxNotificationId == null || maxNotificationId.equals(new Long(0))) {
                sql = new StringBuilder(FROM).append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);
                query = getConnection().createQuery(sql.toString());
            } else {
                sql = new StringBuilder(FROM).append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n");
                sql.append(" where n.id > :maxNotificationId");
                query = getConnection().createQuery(sql.toString());
                query.setParameter("maxNotificationId", maxNotificationId);
            }
            result = executeQueryList(method, sql, query);
            return result;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public List<DBItemSchedulerMonSystemNotifications> getSystemNotifications(String systemId, String serviceName, Long notificationId) throws Exception {
        try {
            String method = "getSystemNotifications";

            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
            sql.append(" where notificationId = :notificationId");
            sql.append(" and serviceName = :serviceName ");
            sql.append(" and lower(systemId) = :systemId");

            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("notificationId", notificationId);
            query.setParameter(SYSTEM_ID, systemId.toLowerCase());
            query.setParameter(SERVICE_NAME, serviceName);

            return executeQueryList(method, sql, query);
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public DBItemSchedulerMonSystemNotifications getSystemNotification(String systemId, String serviceName, Long notificationId, Long checkId, Long objectType,
            boolean onSuccess, String stepFrom, String stepTo, String returnCodeFrom, String returnCodeTo) throws Exception {
        try {
            String method = "getSystemNotification";
            LOGGER.debug(String
                    .format("%s: systemId = %s, serviceName = %s, notificationId = %s, checkId = %s, objectType = %s, onSuccess = %s, stepFrom = %s, stepTo = %s, returnCodeFrom = %s, returnCodeTo = %s",
                            method, systemId, serviceName, notificationId, checkId, objectType, onSuccess, stepFrom, stepTo, returnCodeFrom, returnCodeTo));
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
            sql.append(" where notificationId = :notificationId");
            sql.append(" and checkId = :checkId");
            sql.append(" and objectType = :objectType");
            sql.append(" and serviceName = :serviceName");
            sql.append(" and lower(systemId) = :systemId");
            sql.append(" and success = :success");
            if (stepFrom != null) {
                sql.append(" and stepFrom = :stepFrom");
            }
            if (stepTo != null) {
                sql.append(" and stepTo = :stepTo");
            }
            if (returnCodeFrom != null) {
                sql.append(" and returnCodeFrom = :returnCodeFrom");
            }
            if (returnCodeTo != null) {
                sql.append(" and returnCodeTo = :returnCodeTo");
            }
            Query query = getConnection().createQuery(sql.toString());
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

            List<DBItemSchedulerMonSystemNotifications> result = executeQueryList(method, sql, query);
            if (!result.isEmpty()) {
                return result.get(0);
            } else {
                LOGGER.debug(String.format(
                        "%s: SystemNotification not found for systemId = %s, serviceName = %s, notificationId = %s, checkId = %s, objectType = %s, onSuccess = %s, "
                                + "stepFrom = %s, stepTo = %s, returnCodeFrom = %s, returnCodeTo = %s", method, systemId, serviceName, notificationId, checkId,
                        objectType, onSuccess, stepFrom, stepTo, returnCodeFrom, returnCodeTo));
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public DBItemSchedulerMonResults createResult(Long notificationId, String name, String value) {
        DBItemSchedulerMonResults dbItem = new DBItemSchedulerMonResults();
        dbItem.setNotificationId(notificationId);
        dbItem.setName(name);
        dbItem.setValue(value);
        dbItem.setCreated(DBLayer.getCurrentDateTime());
        dbItem.setModified(DBLayer.getCurrentDateTime());
        return dbItem;
    }

    @SuppressWarnings("unchecked")
    public List<DBItemSchedulerMonChecks> getChecksForNotifyTimer(Optional<Integer> fetchSize) throws Exception {
        try {
            String method = "getChecksForNotifyTimer";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_CHECKS);
            sql.append(" where checked = 1");

            Query q = getConnection().createQuery(sql.toString());
            q.setReadOnly(true);
            if (fetchSize.isPresent()) {
                q.setFetchSize(fetchSize.get());
            }

            return executeQueryList(method, sql, q);
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public DBItemSchedulerMonNotifications getNotificationFirstStep(DBItemSchedulerMonNotifications notification) throws Exception {
        try {
            String method = "getNotificationFirstStep";

            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n");
            sql.append(" where n.orderHistoryId = :orderHistoryId");
            sql.append(" and n.step = 1");

            Query query = getConnection().createQuery(sql.toString());
            query.setParameter(ORDER_HISTORY_ID, notification.getOrderHistoryId());

            List<DBItemSchedulerMonNotifications> result = executeQueryList(method, sql, query);
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public DBItemSchedulerMonNotifications getNotificationsLastStep(DBItemSchedulerMonNotifications notification,
            boolean orderCompleted) throws Exception {
        try {
            String method = "getNotificationsLastStep";
            LOGGER.debug(String.format("%s: orderHistoryId = %s, orderCompleted = %s", method, notification.getOrderHistoryId(), orderCompleted));
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n1");
            sql.append(" where n1.orderHistoryId = :orderHistoryId");
            sql.append(" and n1.step = ");
            sql.append(" (select max(n2.step) ");
            sql.append(FROM_WITH_SPACES);
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n2 ");
            sql.append(WHERE_N2_EQUALS_N1);
            sql.append(" ) ");
            if (orderCompleted) {
                sql.append(" and n1.orderEndTime is not null");
            }

            Query query = getConnection().createQuery(sql.toString());
            query.setParameter(ORDER_HISTORY_ID, notification.getOrderHistoryId());
            query.setReadOnly(true);
            
            List<DBItemSchedulerMonNotifications> result = executeQueryList(method, sql, query);
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    @SuppressWarnings("rawtypes")
    private List executeQueryList(String functionName, StringBuilder sql, Query q) throws Exception {
        List result = null;
        try {
            try {
                result = q.list();
            } catch (LockAcquisitionException ex) {
                LOGGER.debug(String.format("executeQueryList. try rerun %s again in %s. cause exception = %s, sql = %s", functionName,
                        RERUN_TRANSACTION_INTERVAL, ex.getMessage(), sql));
                Thread.sleep(RERUN_TRANSACTION_INTERVAL * 1000);
                result = q.list();
            } catch (Exception ex) {
                throw new Exception(String.format("%s: %s , sql = %s", functionName, ex.getMessage(), sql), ex);
            }
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s , sql = %s", functionName, ex.getMessage(), sql));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<DBItemSchedulerMonNotifications> getOrderNotifications(Optional<Integer> fetchSize, Long orderHistoryId) throws Exception {
        try {
            String method = "getOrderNotifications";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);
            sql.append(" where orderHistoryId = :orderHistoryId");
            sql.append(" order by step");

            Query q = getConnection().createQuery(sql.toString());
            q.setReadOnly(true);
            if (fetchSize.isPresent()) {
                q.setFetchSize(fetchSize.get());
            }
            q.setParameter(ORDER_HISTORY_ID, orderHistoryId);

            return executeQueryList(method, sql, q);
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public int removeCheck(Long checkId) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("delete ");
            sql.append(DBITEM_SCHEDULER_MON_CHECKS);
            sql.append(" where id = :id ");

            Query q = getConnection().createQuery(sql.toString());
            q.setParameter("id", checkId);

            return q.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }
    
    @SuppressWarnings("unchecked")
    public DBItemSchedulerMonChecks getCheck(Long notificationId) throws Exception {
        try {
            String method = "getCheck";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_CHECKS);
            sql.append(" where notificationId = :notificationId");

            Query q = getConnection().createQuery(sql.toString());
            q.setReadOnly(true);
            q.setParameter("notificationId",notificationId);

            List<DBItemSchedulerMonChecks> res =  executeQueryList(method, sql, q);
            if(res != null && res.size()> 0){
            	return res.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateSession.getException(ex));
        }
    }

    public DBItemSchedulerMonChecks createCheck(String name, DBItemSchedulerMonNotifications notification, String stepFrom, String stepTo,
            Date stepFromStartTime, Date stepToEndTime) throws Exception {
        
    	Long notificationId = notification.getId();
        // NULL wegen batch Insert bei den Datenbanken, die kein Autoincrement
        // haben (Oracle ...)
    	DBItemSchedulerMonChecks item = null;
        if (notificationId == null || notificationId.equals(new Long(0))) {
        	item = new DBItemSchedulerMonChecks();
            item.setName(name);
                    	
        	notificationId = new Long(0);
            item.setResultIds(notification.getSchedulerId() + ";" + (notification.getStandalone() ? "true" : "false") + ";" + notification.getTaskId() + ";"
                    + notification.getStep() + ";" + notification.getOrderHistoryId());
            item.setNotificationId(notificationId);
            item.setStepFrom(stepFrom);
            item.setStepTo(stepTo);
            item.setStepFromStartTime(stepFromStartTime);
            item.setStepToEndTime(stepToEndTime);
            item.setChecked(false);
            item.setCreated(DBLayer.getCurrentDateTime());
            item.setModified(DBLayer.getCurrentDateTime());
            
    		getConnection().save(item);
        }
        else{
        	item = getCheck(notificationId);
        	if(item == null){
        		item = new DBItemSchedulerMonChecks();
                item.setName(name);
                item.setNotificationId(notificationId);
                item.setStepFrom(stepFrom);
                item.setStepTo(stepTo);
                item.setStepFromStartTime(stepFromStartTime);
                item.setStepToEndTime(stepToEndTime);
                item.setChecked(false);
                item.setCreated(DBLayer.getCurrentDateTime());
                item.setModified(DBLayer.getCurrentDateTime());
                getConnection().save(item);
        	}
        	else{
        		item.setStepFrom(stepFrom);
                item.setStepTo(stepTo);
                item.setStepFromStartTime(stepFromStartTime);
                item.setStepToEndTime(stepToEndTime);
                item.setModified(DBLayer.getCurrentDateTime());
                getConnection().update(item);
        	}
        }
        return item;
    }

    public DBItemSchedulerMonSystemNotifications createSystemNotification(String systemId, String serviceName, Long notificationId, Long checkId,
            String returnCodeFrom, String returnCodeTo, Long objectType, String stepFrom, String stepTo, Date stepFromStartTime, Date stepToEndTime,
            Long currentNotification, Long notifications, boolean acknowledged, boolean recovered, boolean success) {
        DBItemSchedulerMonSystemNotifications dbItem = new DBItemSchedulerMonSystemNotifications();
        dbItem.setSystemId(systemId);
        dbItem.setServiceName(serviceName);
        dbItem.setNotificationId(notificationId);
        dbItem.setCheckId(checkId);
        dbItem.setReturnCodeFrom(returnCodeFrom);
        dbItem.setReturnCodeTo(returnCodeTo);
        dbItem.setObjectType(objectType);
        dbItem.setStepFrom(stepFrom);
        dbItem.setStepTo(stepTo);
        dbItem.setStepFromStartTime(stepFromStartTime);
        dbItem.setStepToEndTime(stepToEndTime);
        dbItem.setMaxNotifications(false);
        dbItem.setCurrentNotification(currentNotification);
        dbItem.setNotifications(notifications);
        dbItem.setAcknowledged(acknowledged);
        dbItem.setRecovered(recovered);
        dbItem.setSuccess(success);
        dbItem.setCreated(DBLayer.getCurrentDateTime());
        dbItem.setModified(DBLayer.getCurrentDateTime());
        return dbItem;
    }

    public void deleteDummySystemNotification(String systemId) throws Exception {
        StringBuffer sql = new StringBuffer(DELETE_FROM);
        sql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
        sql.append(" where objectType = :objectType");
        sql.append(" and lower(systemId) = :systemId");

        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("objectType", DBLayer.NOTIFICATION_OBJECT_TYPE_DUMMY);
        query.setParameter("systemId", systemId.toLowerCase());

        query.executeUpdate();
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

        DBItemSchedulerMonSystemNotifications sm = createSystemNotification(systemId, serviceName, notificationId, checkId, returnCodeFrom, returnCodeTo,
                objectType, stepFrom, stepTo, stepFromStartTime, stepToEndTime, currentNotification, notifications, acknowledged, recovered, success);
        sm.setMaxNotifications(true);
        return sm;
    }

    public DBItemSchedulerMonNotifications createNotification(String schedulerId, boolean standalone, Long taskId, Long step, Long orderHistoryId,
            String jobChainName, String jobChainTitle, String orderId, String orderTitle, Date orderStartTime, Date orderEndTime, String orderStepState,
            Date orderStepStartTime, Date orderStepEndTime, String jobName, String jobTitle, Date taskStartTime, Date taskEndTime, boolean recovered,
            Long returnCode, boolean error, String errorCode, String errorText) throws Exception {
        DBItemSchedulerMonNotifications dbItem = new DBItemSchedulerMonNotifications();
        // set unique key
        dbItem.setSchedulerId(schedulerId);
        dbItem.setStandalone(standalone);
        dbItem.setTaskId(taskId);
        dbItem.setStep(step);
        dbItem.setOrderHistoryId(orderHistoryId);
        // set others
        dbItem.setJobChainName(jobChainName);
        dbItem.setJobChainTitle(jobChainTitle);
        dbItem.setOrderId(orderId);
        dbItem.setOrderTitle(orderTitle);
        dbItem.setOrderStartTime(orderStartTime);
        dbItem.setOrderEndTime(orderEndTime);
        dbItem.setOrderStepState(orderStepState);
        dbItem.setOrderStepStartTime(orderStepStartTime);
        dbItem.setOrderStepEndTime(orderStepEndTime);
        dbItem.setJobName(jobName);
        dbItem.setJobTitle(jobTitle);
        dbItem.setTaskStartTime(taskStartTime);
        dbItem.setTaskEndTime(taskEndTime);
        dbItem.setRecovered(recovered);
        dbItem.setReturnCode(returnCode);
        dbItem.setError(error);
        dbItem.setErrorCode(errorCode);
        dbItem.setErrorText(errorText);
        dbItem.setCreated(DBLayer.getCurrentDateTime());
        dbItem.setModified(DBLayer.getCurrentDateTime());
        return dbItem;
    }
}
