package com.sos.jitl.jobstreams.db;

import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;

public class DBLayerJobStreamsStarterJobs {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerJobStreamsStarterJobs.class);
    private static final String DBItemJobStreamStarterJobs = DBItemJobStreamStarterJob.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerJobStreamsStarterJobs(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemJobStreamStarterJob getJobStreamStarterJobsDbItem(final Long id) throws SOSHibernateException {
        return (DBItemJobStreamStarterJob) sosHibernateSession.get(DBItemJobStreamStarterJob.class, id);
    }

    public FilterJobStreamStarterJobs resetFilter() {
        FilterJobStreamStarterJobs filter = new FilterJobStreamStarterJobs();
        filter.setJob("");

        return filter;
    }

    private String getWhere(FilterJobStreamStarterJobs filter) {
        String where = "1=1";
        String and = " and ";

        if (filter.getId() != null) {
            where += and + " id = :id";
            and = " and ";
        }

        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            where += and + " job = :job";
            and = " and ";
        }

        if (filter.getJobStreamStarter() != null) {
            where += and + " jobStreamStarter = :jobStreamStarter";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterJobStreamStarterJobs filter, Query<T> query) {
        if (filter.getId() != null) {
            query.setParameter("id", filter.getId());
        }
        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            query.setParameter("job", filter.getJob());
        }
        if (filter.getJobStreamStarter() != null) {
            query.setParameter("jobStreamStarter", filter.getJobStreamStarter());
        }
        return query;
    }

    public List<DBItemJobStreamStarterJob> getJobStreamStarterJobsList(FilterJobStreamStarterJobs filter, final int limit)
            throws SOSHibernateException {
        String q = "  from " + DBItemJobStreamStarterJobs + getWhere(filter);

        Query<DBItemJobStreamStarterJob> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public Integer deleteCascading(FilterJobStreamStarterJobs filter) throws SOSHibernateException {

        DBLayerJobStreamJobParameters dbLayerJobStreamJobParameters = new DBLayerJobStreamJobParameters(sosHibernateSession);
        FilterJobStreamJobParameters filterJobStreamJobParameters = new FilterJobStreamJobParameters();
        filterJobStreamJobParameters.setJobId(filter.getId());
        dbLayerJobStreamJobParameters.delete(filterJobStreamJobParameters);

        int row = 0;
        String hql = "";

        hql = "delete from " + DBItemJobStreamStarterJobs + getWhere(filter);
        Query<DBItemJobStream> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public Long store(DBItemJobStreamStarterJob dbItemJobStreamStarterJob) throws SOSHibernateException {
        FilterJobStreamStarterJobs filter = new FilterJobStreamStarterJobs();
        filter.setId(dbItemJobStreamStarterJob.getId());
        deleteCascading(filter);
        sosHibernateSession.save(dbItemJobStreamStarterJob);
        return dbItemJobStreamStarterJob.getId();

    }

}