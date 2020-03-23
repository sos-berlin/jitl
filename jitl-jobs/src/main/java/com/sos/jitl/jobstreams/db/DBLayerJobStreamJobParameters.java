package com.sos.jitl.jobstreams.db;

import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;

public class DBLayerJobStreamJobParameters {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerJobStreamJobParameters.class);
    private static final String DBItemJobStreamJobParameter = DBItemJobStreamJobParameter.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerJobStreamJobParameters(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemJobStreamJobParameter getJobStreamJobParametersDbItem(final Long id) throws SOSHibernateException {
        return (DBItemJobStreamJobParameter) sosHibernateSession.get(DBItemJobStreamJobParameter.class, id);
    }

    public FilterJobStreamJobParameters resetFilter() {
        FilterJobStreamJobParameters filter = new FilterJobStreamJobParameters();
        filter.setName("");
        return filter;
    }

    private String getWhere(FilterJobStreamJobParameters filter) {
        String where = "1=1";
        String and = " and ";

        if (filter.getName() != null && !"".equals(filter.getName())) {
            where += and + " name = :name";
            and = " and ";
        }

        if (filter.getJobId() != null) {
            where += and + " jobStreamJob  = :jobStreamJob";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterJobStreamJobParameters filter, Query<T> query) {
        if (filter.getName() != null) {
            query.setParameter("name", filter.getName());
        }
        if (filter.getJobId() != null) {
            query.setParameter("jobStreamJob", filter.getJobId());
        }

        return query;
    }

    public List<DBItemJobStreamJobParameter> getJobStreamParametersList(FilterJobStreamJobParameters filter, final int limit)
            throws SOSHibernateException {
        String q = "  from " + DBItemJobStreamJobParameter + getWhere(filter);

        Query<DBItemJobStreamJobParameter> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public Integer delete(FilterJobStreamJobParameters filter) throws SOSHibernateException {
        int row = 0;
        String hql = "";

        hql = "delete from " + DBItemJobStreamJobParameter + getWhere(filter);
        Query<DBItemJobStreamJobParameter> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public void save(DBItemJobStreamJobParameter dbItemJobStreamJobParameter) throws SOSHibernateException {
        sosHibernateSession.save(dbItemJobStreamJobParameter);
    }

}