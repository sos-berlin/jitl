package com.sos.jitl.jobstreams.db;

import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;

public class DBLayerJobStreamHistory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerJobStreamHistory.class);
    private static final String DBItemJobStreamHistory = DBItemJobStreamHistory.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerJobStreamHistory(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemJobStreamHistory getJobStreamHistoryDbItem(final Long id) throws SOSHibernateException {
        return (DBItemJobStreamHistory) sosHibernateSession.get(DBItemJobStreamHistory.class, id);
    }

    public FilterJobStreamHistory resetFilter() {
        FilterJobStreamHistory filter = new FilterJobStreamHistory();
        filter.setContextId("");
        return filter;
    }

    private String getWhere(FilterJobStreamHistory filter) {
        String where = "1=1";
        String and = " and ";

        if (filter.getId() != null) {
            where += and + " id = :id";
            and = " and ";
        }

        if (filter.getRunning() != null) {
            where += and + " running = :running";
            and = " and ";
        }

        if (filter.getContextId() != null && !filter.getContextId().isEmpty()) {
            where += and + " contextId  = :contextId";
            and = " and ";
        }

        if (filter.getSchedulerId() != null && !filter.getSchedulerId().isEmpty()) {
            where += and + " schedulerId  = :schedulerId";
            and = " and ";
        }

        if (filter.getJobStreamStarter() != null) {
            where += and + " jobStreamStarter  = :jobStreamStarter";
            and = " and ";
        }

        if (filter.getJobStreamId() != null) {
            where += and + " jobStream  = :jobStream";
            and = " and ";
        }

        if (filter.getStartedFrom() != null) {
            where += and + " started>= :startedFrom";
            and = " and ";
        }
        if (filter.getStartedTo() != null) {
            where += and + " started < :startedTo ";
            and = " and ";
        }
        if (!"".equals(where.trim())) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterJobStreamHistory filter, Query<T> query) {
        if (filter.getId() != null) {
            query.setParameter("id", filter.getId());
        }

        if (filter.getRunning() != null) {
            query.setParameter("running", filter.getRunning());
        }
        if (filter.getJobStreamStarter() != null) {
            query.setParameter("jobStreamStarter", filter.getJobStreamStarter());
        }
        if (filter.getJobStreamId() != null) {
            query.setParameter("jobStream", filter.getJobStreamId());
        }
        if (filter.getContextId() != null && !filter.getContextId().isEmpty()) {
            query.setParameter("contextId", filter.getContextId());
        }
        if (filter.getSchedulerId() != null && !filter.getSchedulerId().isEmpty()) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getStartedFrom() != null) {
            query.setParameter("startedFrom", filter.getStartedFrom());
        }
        if (filter.getStartedTo() != null) {
            query.setParameter("startedTo", filter.getStartedTo());
        }

        return query;
    }

    public List<DBItemJobStreamHistory> getJobStreamHistoryList(FilterJobStreamHistory filter, final int limit) throws SOSHibernateException {
        String q = "  from " + DBItemJobStreamHistory + getWhere(filter) + " order by id desc";

        Query<DBItemJobStreamHistory> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public Integer delete(FilterJobStreamHistory filter) throws SOSHibernateException {

        int row = 0;
        String hql = "";

        hql = "delete from " + DBItemJobStreamHistory + getWhere(filter);
        Query<DBItemJobStreamHistory> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public void save(DBItemJobStreamHistory dbItemJobStreamHistory) throws SOSHibernateException {
        sosHibernateSession.save(dbItemJobStreamHistory);
    }

    public void update(DBItemJobStreamHistory dbItemJobStreamHistory) throws SOSHibernateException {
        sosHibernateSession.update(dbItemJobStreamHistory);
    }

    public void deleteCascading(FilterJobStreamHistory filterJobStreamHistory) throws SOSHibernateException {
        DBLayerJobStreamsTaskContext dbLayerJobStreamTasksContext = new DBLayerJobStreamsTaskContext(sosHibernateSession);
        DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);
        List<DBItemJobStreamHistory> lHistory = getJobStreamHistoryList(filterJobStreamHistory, 0);
        if (filterJobStreamHistory.getContextId() != null && !filterJobStreamHistory.getContextId().isEmpty()) {
            FilterJobStreamTaskContext filterJobStreamTaskContext = new FilterJobStreamTaskContext();
            filterJobStreamTaskContext.setJobstreamHistoryId(filterJobStreamHistory.getContextId());
            dbLayerJobStreamTasksContext.delete(filterJobStreamTaskContext);
        } else {
            for (DBItemJobStreamHistory dbItemJobStreamHistory : lHistory) {
                FilterJobStreamTaskContext filterJobStreamTaskContext = new FilterJobStreamTaskContext();
                filterJobStreamTaskContext.setJobstreamHistoryId(dbItemJobStreamHistory.getContextId());
                dbLayerJobStreamTasksContext.delete(filterJobStreamTaskContext);

                FilterEvents filterEvents = new FilterEvents();
                filterEvents.setJobStreamHistoryId(dbItemJobStreamHistory.getId());
                dbLayerEvents.delete(filterEvents);
            }
        }

        delete(filterJobStreamHistory);

    }

    public int updateHistoryWithStarter(Long oldId, Long newId) throws SOSHibernateException {
        String hql = "update " + DBItemJobStreamHistory + " set jobStreamStarter=" + newId + " where jobStreamStarter=:oldId";
        int row = 0;
        Query<DBItemJobStreamHistory> query = sosHibernateSession.createQuery(hql);
        query.setParameter("oldId", oldId);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public int updateHistoryWithJobStream(Long oldId, Long newId) throws SOSHibernateException {
        String hql = "update " + DBItemJobStreamHistory + " set jobStream=" + newId + " where jobStream=:oldId";
        int row = 0;
        Query<DBItemJobStreamHistory> query = sosHibernateSession.createQuery(hql);
        query.setParameter("oldId", oldId);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public int updateHistoryWithJobStreamStarter(Long oldStarterId, Long newStarterId) throws SOSHibernateException {
        String hql = "update " + DBItemJobStreamHistory + " set jobStreamStarter=" + newStarterId + " where jobStreamStarter=:oldStarterId";
        int row = 0;
        Query<DBItemJobStreamHistory> query = sosHibernateSession.createQuery(hql);
        query.setParameter("oldStarterId", oldStarterId);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

}