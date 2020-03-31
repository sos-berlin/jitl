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
        if (filter.getJobStreamStarter() != null) {
            where += and + " jobStreamStarter  = :jobStreamStarter";
            and = " and ";
        }

        if (filter.getJobStreamId() != null) {
            where += and + " jobStream  = :jobStream";
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

        return query;
    }

    public List<DBItemJobStreamHistory> getJobStreamHistoryList(FilterJobStreamHistory filter, final int limit) throws SOSHibernateException {
        String q = "  from " + DBItemJobStreamHistory + getWhere(filter);

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

    public void save(DBItemJobStreamHistory jsJobStreamHistory) throws SOSHibernateException {
        sosHibernateSession.save(jsJobStreamHistory);
    }

    public Long store(DBItemJobStreamHistory dbItemJobStreamHistory) throws SOSHibernateException {
        FilterJobStreamHistory filterJobStreamHistory = new FilterJobStreamHistory();
        filterJobStreamHistory.setContextId(dbItemJobStreamHistory.getContextId());
        delete(filterJobStreamHistory);
        sosHibernateSession.save(dbItemJobStreamHistory);
        return dbItemJobStreamHistory.getId();
    }

}