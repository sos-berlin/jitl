package com.sos.jitl.jobstreams.db;

import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;

public class DBLayerJobStreamsTaskContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerJobStreamsTaskContext.class);
    private static final String DBItemJobStreamTaskContext = DBItemJobStreamTaskContext.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerJobStreamsTaskContext(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemJobStreamTaskContext getJobStreamStarterJobsDbItem(final Long id) throws SOSHibernateException {
        return (DBItemJobStreamTaskContext) sosHibernateSession.get(DBItemJobStreamTaskContext.class, id);
    }

    public FilterJobStreamTaskContext resetFilter() {
        FilterJobStreamTaskContext filter = new FilterJobStreamTaskContext();
        filter.setJobstreamHistoryId("");

        return filter;
    }

    private String getWhere(FilterJobStreamTaskContext filter) {
        String where = "1=1";
        String and = " and ";

        if (filter.getId() != null) {
            where += and + " id = :id";
            and = " and ";
        }

        if (filter.getJobstreamHistoryId() != null && !"".equals(filter.getJobstreamHistoryId())) {
            where += and + " jobStreamHistoryId = :jobStreamHistoryId";
            and = " and ";
        }

        if (filter.getTaskId() != null) {
            where += and + " taskId = :taskId";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterJobStreamTaskContext filter, Query<T> query) {
        if (filter.getId() != null) {
            query.setParameter("id", filter.getId());
        }
        if (filter.getJobstreamHistoryId() != null && !"".equals(filter.getJobstreamHistoryId())) {
            query.setParameter("jobStreamHistoryId", filter.getJobstreamHistoryId());
        }
        if (filter.getTaskId() != null) {
            query.setParameter("taskId", filter.getTaskId());
        }
        return query;
    }

    public List<DBItemJobStreamTaskContext> getJobStreamStarterJobsList(FilterJobStreamTaskContext filter, final int limit)
            throws SOSHibernateException {
        String q = "  from " + DBItemJobStreamTaskContext + getWhere(filter);

        Query<DBItemJobStreamTaskContext> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public Integer delete(FilterJobStreamTaskContext filter) throws SOSHibernateException {
         int row = 0;
        String hql = "";

        hql = "delete from " + DBItemJobStreamTaskContext + getWhere(filter);
        LOGGER.debug("delete context: " + hql);
        Query<DBItemJobStreamTaskContext> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public Long store(DBItemJobStreamTaskContext dbItemJobStreamTaskContext) throws SOSHibernateException {
        FilterJobStreamTaskContext filter = new FilterJobStreamTaskContext();
        filter.setTaskId(dbItemJobStreamTaskContext.getTaskId());
        filter.setJobstreamHistoryId(dbItemJobStreamTaskContext.getJobStreamHistoryId());
        delete(filter);
        LOGGER.debug("save context " + dbItemJobStreamTaskContext.getJobStreamHistoryId() + ":" + dbItemJobStreamTaskContext.getTaskId());
        sosHibernateSession.save(dbItemJobStreamTaskContext);
        LOGGER.debug("new id: " + dbItemJobStreamTaskContext.getId());
        return dbItemJobStreamTaskContext.getId();
    }

}