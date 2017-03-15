package com.sos.jitl.eventing.db;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.query.Query;
import org.joda.time.DateTime;

import com.sos.jitl.eventing.evaluate.BooleanExp;
import com.sos.hibernate.layer.SOSHibernateDBLayer;

/** @author Uwe Risse */
public class SchedulerEventDBLayer extends SOSHibernateDBLayer {

    private static final Logger LOGGER = Logger.getLogger(SchedulerEventDBLayer.class);
    private static final String EVENT_ID = "eventId";
    private static final String EVENT_CLASS = "eventClass";
    private SchedulerEventFilter filter = null;

    public SchedulerEventDBLayer(final String configurationFilename) throws Exception {
        super();
        this.setConfigurationFileName(configurationFilename);
        this.createStatefullConnection(this.getConfigurationFileName());
        resetFilter();
    }

    public SchedulerEventDBLayer(String configurationFilename, SchedulerEventFilter filter_) throws Exception {
        super();
        this.setConfigurationFileName(configurationFilename);
        this.createStatefullConnection(this.getConfigurationFileName());
        filter = filter_;
    }

    public SchedulerEventDBItem getEvent(final Long id) throws Exception {
        if (sosHibernateSession == null) {
            this.createStatefullConnection(this.getConfigurationFileName());
        }
        return (SchedulerEventDBItem) (sosHibernateSession.get(SchedulerEventDBItem.class, id));
    }

    public void resetFilter() {
        filter = new SchedulerEventFilter();
        filter.setEventClass("");
        filter.setEventId("");
        filter.setSchedulerId("");
        filter.setJobChain("");
        filter.setJobName("");
        filter.setExitCode("");
        filter.setExpires(new Date());
        filter.setSchedulerId("");
    }

    private Query getQuery(String hql) throws Exception {
        Query query = null;
        sosHibernateSession.beginTransaction();
        query = sosHibernateSession.createQuery(hql);
        if (filter.hasEvents()) {
            query.setParameterList(EVENT_ID, filter.getEventList());
        }
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getRemoteSchedulerHost() != null && !"".equals(filter.getRemoteSchedulerHost())) {
            query.setParameter("remoteSchedulerHost", filter.getRemoteSchedulerHost());
        }
        if (filter.getRemoteSchedulerPort() != null && !"".equals(filter.getRemoteSchedulerPort())) {
            query.setParameter("remoteSchedulerPort", filter.getRemoteSchedulerPort());
        }
        if (filter.getJobChain() != null && !"".equals(filter.getJobChain())) {
            query.setParameter("jobChain", filter.getJobChain());
        }
        if (filter.getJobName() != null && !"".equals(filter.getJobName())) {
            query.setParameter("jobName", filter.getJobName());
        }
        if (filter.getEventClass() != null && !"".equals(filter.getEventClass())) {
            query.setParameter("eventClass", filter.getEventClass());
        }
        if (filter.getEventId() != null && !"".equals(filter.getEventId())) {
            query.setParameter("eventId", filter.getEventId());
        }
        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            query.setParameter("orderId", filter.getOrderId());
        }
        if (filter.getExitCode() != null && !"".equals(filter.getExitCode())) {
            query.setParameter("exitCode", filter.getExitCode());
        }
        return query;
    }

    private Query setQueryParams(String hql) throws Exception {
        return getQuery(hql);
    }

    public int delete() throws Exception {
        String hql = "delete from SchedulerEventDBItem " + getWhere();
        Query query = null;
        int row = 0;
        query = setQueryParams(hql);
        sosHibernateSession.beginTransaction();
        row = query.executeUpdate();
        return row;
    }

    private String getWhere() {
        String where = "";
        String and = "";
        if (filter.hasEvents()) {
            where += and + " eventId in ( :eventId )";
            and = " and ";
        }
        if (filter.getRemoteSchedulerPort() != null && !"".equals(filter.getRemoteSchedulerPort())) {
            where += and + " remoteSchedulerPort = :remoteSchedulerPort";
            and = " and ";
        }
        if (filter.getRemoteSchedulerHost() != null && !"".equals(filter.getRemoteSchedulerHost())) {
            where += and + " remoteSchedulerHost = :remoteSchedulerHost";
            and = " and ";
        }
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            where += and + " schedulerId = :schedulerId";
            and = " and ";
        }
        if (filter.getJobChain() != null && !"".equals(filter.getJobChain())) {
            where += and + " jobChain = :jobChain";
            and = " and ";
        }
        if (filter.getJobName() != null && !"".equals(filter.getJobName())) {
            where += and + " jobName = :jobName";
            and = " and ";
        }
        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            where += and + " orderId = :orderId";
            and = " and ";
        }
        if (filter.getEventId() != null && !"".equals(filter.getEventId())) {
            where += and + " eventId = :eventId";
            and = " and ";
        }
        if (filter.getEventClass() != null && !"".equals(filter.getEventClass())) {
            where += and + " eventClass = :eventClass";
            and = " and ";
        }
        if (filter.getExitCode() != null && !"".equals(filter.getExitCode())) {
            where += and + " exitCode = :exitCode";
            and = " and ";
        }
        if (!"".equals(where.trim())) {
            where = "where " + where;
        }
        return where;
    }

    public List<SchedulerEventDBItem> getScheduleEventList(final int limit) throws Exception {
        String hql = "from SchedulerEventDBItem " + getWhere() + filter.getOrderCriteria() + filter.getSortMode();
        Query query = null;
        List<SchedulerEventDBItem> scheduleEventList = null;
        sosHibernateSession.beginTransaction();
        query = setQueryParams(hql);
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        scheduleEventList = query.list();
        return scheduleEventList;
    }

    public boolean checkEventExists() throws Exception {
        return !getScheduleEventList(1).isEmpty();
    }

    public boolean checkEventExists(SchedulerEventDBItem event) throws Exception {
        resetFilter();
        filter.setEventClass(event.getEventClass());
        filter.setEventId(event.getEventId());
        return !getScheduleEventList(1).isEmpty();
    }

    public boolean checkEventExists(SchedulerEventFilter filter) throws Exception {
        resetFilter();
        this.filter = filter;
        return !getScheduleEventList(1).isEmpty();
    }

    public boolean checkEventExists(String condition) throws Exception {
        resetFilter();
        List<SchedulerEventDBItem> listOfActiveEvents = getEventsFromDb();
        boolean result = false;
        Iterator<SchedulerEventDBItem> iExit = listOfActiveEvents.iterator();
        BooleanExp exp = new BooleanExp(condition);
        while (iExit.hasNext()) {
            SchedulerEventDBItem e = iExit.next();
            exp.replace(e.getEventName() + ":" + e.getExitCode(), "true");
            exp.replace(e.getEventId() + ":" + e.getExitCode(), "true");
            LOGGER.debug(exp.getBoolExp());
        }
        Iterator<SchedulerEventDBItem> iClass = listOfActiveEvents.iterator();
        while (iClass.hasNext()) {
            SchedulerEventDBItem e = iClass.next();
            exp.replace(e.getEventName(), "true");
            LOGGER.debug(exp.getBoolExp());
        }
        Iterator<SchedulerEventDBItem> iEventId = listOfActiveEvents.iterator();
        while (iEventId.hasNext()) {
            SchedulerEventDBItem e = iEventId.next();
            exp.replace(e.getEventId(), "true");
            LOGGER.debug(exp.getBoolExp());
        }
        LOGGER.debug("--------->" + exp.getBoolExp());
        return exp.evaluateExpression();
    }

    public boolean checkEventExists(String condition, String eventClass) throws Exception {
        resetFilter();
        filter.setEventClass(eventClass);
        LOGGER.debug("eventClass:" + eventClass);
        boolean result = false;
        List<SchedulerEventDBItem> listOfActiveEvents = getEventsFromDb();
        Iterator<SchedulerEventDBItem> iExit = listOfActiveEvents.iterator();
        BooleanExp exp = new BooleanExp(condition);
        while (iExit.hasNext()) {
            SchedulerEventDBItem e = iExit.next();
            exp.replace(e.getEventId() + ":" + e.getExitCode(), "true");
        }
        Iterator<SchedulerEventDBItem> iEventId = listOfActiveEvents.iterator();
        while (iEventId.hasNext()) {
            SchedulerEventDBItem e = iEventId.next();
            exp.replace(e.getEventId(), "true");
        }
        return exp.evaluateExpression();
    }

    public List<SchedulerEventDBItem> getEventsFromDb() throws Exception {
 
        String getWhere = getWhere();
        Query query = null;
        List<SchedulerEventDBItem> resultList = null;
        sosHibernateSession.beginTransaction();
        query = setQueryParams("from SchedulerEventDBItem  " + getWhere);
        LOGGER.debug("where:" + getWhere);
        resultList = query.list();
        return resultList;
    }

    public SchedulerEventsCollection getMissingEvents(List<SchedulerEventDBItem> eventList) throws Exception {
        resetFilter();
        SchedulerEventsCollection missingEvents = new SchedulerEventsCollection();
        filter.setEventList(eventList);
        if (filter.getEventList().isEmpty()) {
            LOGGER.info("no events to check - eventList is empty.");
        } else {
            List<SchedulerEventDBItem> resultList = getEventsFromDb();
            missingEvents.addAll(eventList);
            for (SchedulerEventDBItem item : resultList) {
                missingEvents.remove(item.getEventId());
            }
            LOGGER.info("missing events " + missingEvents.toString());
        }
        return missingEvents;
    }

    public void createEvent(SchedulerEventDBItem event) throws Exception {
        if (!checkEventExists(event)) {
            DateTime now = new DateTime();
            DateTime expired = now.plusDays(60);
            sosHibernateSession.beginTransaction();
            event.setCreated(new DateTime());
            event.setExpires(expired);
            sosHibernateSession.saveOrUpdate(event);
        }
    }

    public void deleteEventsForClass(String eventClass) throws Exception {
        resetFilter();
        SchedulerEventFilter filter = new SchedulerEventFilter();
        filter.setEventClass(eventClass);
        delete();
    }

    public SchedulerEventFilter getFilter() {
        return filter;
    }

    public void setFilter(final SchedulerEventFilter filter) {
        this.filter = filter;
    }

}