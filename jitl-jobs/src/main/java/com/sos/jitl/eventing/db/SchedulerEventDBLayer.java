package com.sos.jitl.eventing.db;

import java.util.Iterator;
import java.util.List;

import javax.persistence.TemporalType;

import org.apache.log4j.Logger;
import org.hibernate.query.Query;
import com.sos.jitl.eventing.evaluate.BooleanExp;
import com.sos.joc.model.order.OrderPath;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.hibernate.layer.SOSHibernateDBLayer;

/** @author Uwe Risse */
public class SchedulerEventDBLayer extends SOSHibernateDBLayer {

    private static final Logger LOGGER = Logger.getLogger(SchedulerEventDBLayer.class);
    private static final String SchedulerEventDBItem = SchedulerEventDBItem.class.getName();

    private SchedulerEventFilter filter = null;

    public SchedulerEventDBLayer(final String configurationFilename) throws Exception {
        super();
        this.setConfigurationFileName(configurationFilename);
        this.createStatefullConnection(this.getConfigurationFileName());
        resetFilter();
    }

    public SchedulerEventDBLayer(SOSHibernateSession session) throws Exception {
        super();
        this.setConfigurationFileName(session.getFactory().getConfigFile().get().toFile().getAbsolutePath());
        this.sosHibernateSession = session;
        resetFilter();
    }

    public void beginTransaction() throws Exception {
        this.sosHibernateSession.beginTransaction();
    }

    public void rollback() throws Exception {
        try {
            this.sosHibernateSession.rollback();
        } catch (Exception e) {
        }

    }

    public void commit() throws Exception {
        this.sosHibernateSession.commit();
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
        this.filter = new SchedulerEventFilter();
        this.filter.setDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        this.filter.setOrderCriteria("id");
        this.filter.setSortMode("desc");
    }

    private Query<SchedulerEventDBItem> bindParameters(String hql) throws SOSHibernateException {
        Query<SchedulerEventDBItem> query = sosHibernateSession.createQuery(hql);

        if (filter.hasEventIds()) {
            query.setParameterList("eventId", filter.getListOfEventIds());
        }
        if (filter.hasJobs()) {
            query.setParameterList("job", filter.getListOfJobs());
        }
        if (filter.hasExitCodes()) {
            query.setParameterList("exitCode", filter.getListOfExitCodes());
        }
        if (filter.hasEventClasses()) {
            query.setParameterList("eventClass", filter.getListOfEventClasses());
        }
        if (filter.hasIds()) {
            query.setParameterList("ids", filter.getListOfIds());
        }

        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getRemoteUrl() != null && !"".equals(filter.getRemoteUrl())) {
            query.setParameter("remoteUrl", filter.getRemoteUrl());
        }
        if (filter.getRemoteSchedulerHost() != null && !"".equals(filter.getRemoteSchedulerHost())) {
            query.setParameter("remoteSchedulerHost", filter.getRemoteSchedulerHost());
        }
        if (filter.getRemoteSchedulerPort() != null) {
            query.setParameter("remoteSchedulerPort", filter.getRemoteSchedulerPort());
        }
        if (filter.getJobChain() != null && !"".equals(filter.getJobChain())) {
            query.setParameter("jobChain", filter.getJobChain());
        }
        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            query.setParameter("orderId", filter.getOrderId());
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
        if (filter.getExitCode() != null) {
            query.setParameter("exitCode", filter.getExitCode());
        }
        if (filter.getExpiresFrom() != null) {
            query.setParameter("expiresFrom", filter.getExpiresFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getExpiresTo() != null) {
            query.setParameter("expiresTo", filter.getExpiresTo(), TemporalType.TIMESTAMP);
        }
        return query;
    }

    public int delete() throws Exception {
        int row = 0;
        String hql = "delete from " + SchedulerEventDBItem + " " + getWhere();
        Query<SchedulerEventDBItem> query = bindParameters(hql);
        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    private String getOrderClause(OrderPath order) {
        if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
            return "(jobChain=" + order.getJobChain() + ")";
        } else {
            if (order.getJobChain() == null || order.getJobChain().isEmpty()) {
                return "(orderId=" + order.getOrderId() + ")";
            } else {
                return "(orderId = " + order.getOrderId() + " and jobChain=" + order.getJobChain() + ")";
            }
        }

    }

    private String getWhere() {
        String where = "";
        String and = "";
        if (filter.hasIds()) {
            where += and + " id in ( :ids )";
            and = " and ";
        }
        if (filter.hasEventIds()) {
            where += and + " eventId in ( :eventId )";
            and = " and ";
        }
        if (filter.hasJobs()) {
            where += and + " job in ( :job )";
            and = " and ";
        }
        if (filter.hasEventClasses()) {
            where += and + " eventClass in ( :eventClass )";
            and = " and ";
        }
        if (filter.hasExitCodes()) {
            where += and + " exitCode in ( :exitCode )";
            and = " and ";
        }
        if (filter.hasOrders()) {
            where += and + "(";
            for (OrderPath order : filter.getListOfOrders()) {
                where += getOrderClause(order) + " or ";
            }
            where += " 1=0)";
            and = " and ";
        }
        if (filter.getRemoteUrl() != null && !filter.getRemoteUrl().isEmpty()) {
            where += and + " remoteUrl=:remoteUrl";
            and = " and ";
        }

        if (filter.getRemoteSchedulerPort() != null) {
            where += and + " remoteSchedulerPort = :remoteSchedulerPort";
            and = " and ";
        }
        if (filter.getRemoteSchedulerHost() != null && !"".equals(filter.getRemoteSchedulerHost())) {
            where += and + " remoteSchedulerHost = :remoteSchedulerHost";
            and = " and ";
        }
        if (filter.isSchedulerIdEmpty()) {
            if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
                where += and + " (schedulerId is null or schedulerId='' or schedulerId=:schedulerId)";
                and = " and ";
            } else {
                where += and + " (schedulerId is null or schedulerId='')";
                and = " and ";
            }
        } else {
            if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
                where += and + " schedulerId=:schedulerId";
                and = " and ";
            }
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
        if (filter.getExpiresFrom() != null) {
            where += and + " expires >= :expiresFrom";
            and = " and ";
        }
        if (filter.getExpiresTo() != null) {
            where += and + " expires <= :expiresTo";
            and = " and ";
        }
        if (!"".equals(where.trim())) {
            where = "where " + where;
        }
        return where;
    }

    public List<SchedulerEventDBItem> getSchedulerEventList(final int limit) throws Exception {
        List<SchedulerEventDBItem> listOfCustomEvents = null;
        Query<SchedulerEventDBItem> query = bindParameters(String.format("from %s %s %s %s", SchedulerEventDBItem, getWhere(), filter.getOrderCriteria(), filter.getSortMode()));

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        listOfCustomEvents = sosHibernateSession.getResultList(query);
        return listOfCustomEvents;
    }

    public List<SchedulerEventDBItem> getSchedulerEventList() throws Exception {
        return getSchedulerEventList(filter.getLimit());
    }

    public boolean checkEventExists() throws Exception {
        return !getSchedulerEventList(1).isEmpty();
    }

    public boolean checkEventExists(SchedulerEventDBItem event) throws Exception {
        resetFilter();
        filter.setEventClass(event.getEventClass());
        filter.setEventId(event.getEventId());
        return !getSchedulerEventList(1).isEmpty();
    }

    public boolean checkEventExists(SchedulerEventFilter filter) throws Exception {
        resetFilter();
        this.filter = filter;
        return !getSchedulerEventList(1).isEmpty();
    }

    public boolean checkEventExists(String condition) throws Exception {
        resetFilter();
        List<SchedulerEventDBItem> listOfActiveEvents = getSchedulerEventList();
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
        List<SchedulerEventDBItem> listOfActiveEvents = getSchedulerEventList();
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

    public SchedulerEventFilter getFilter() {
        return filter;
    }

    public void setFilter(final SchedulerEventFilter filter) {
        this.filter = filter;
    }

    public void insertItem(SchedulerEventDBItem schedulerEventDBItem2) throws SOSHibernateException {
        this.sosHibernateSession.save(schedulerEventDBItem2);
    }

}