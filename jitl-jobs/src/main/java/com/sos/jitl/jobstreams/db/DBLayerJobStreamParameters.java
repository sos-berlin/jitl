package com.sos.jitl.jobstreams.db;

import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;

public class DBLayerJobStreamParameters {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerJobStreamParameters.class);
    private static final String DBItemJobStreamParameter = DBItemJobStreamParameter.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerJobStreamParameters(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemJobStreamParameter getJobStreamParametersDbItem(final Long id) throws SOSHibernateException {
        return (DBItemJobStreamParameter) sosHibernateSession.get(DBItemJobStreamParameter.class, id);
    }

    public FilterJobStreamParameters resetFilter() {
        FilterJobStreamParameters filter = new FilterJobStreamParameters();
        filter.setName("");
        return filter;
    }

    private String getWhere(FilterJobStreamParameters filter) {
        String where = "";
        String and = " ";

        if (filter.getName() != null && !"".equals(filter.getName())) {
            where += and + " name = :name";
            and = " and ";
        }

        if (filter.getJobStreamStarterId() != null) {
            where += and + " jobStreamStarter  = :jobStreamStarter";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterJobStreamParameters filter, Query<T> query) {
        if (filter.getName() != null) {
            query.setParameter("name", filter.getName());
        }
        if (filter.getJobStreamStarterId() != null) {
            query.setParameter("jobStreamStarter", filter.getJobStreamStarterId());
        }

        return query;
    }

    public List<DBItemJobStreamParameter> getJobStreamParametersList(FilterJobStreamParameters filter, final int limit) throws SOSHibernateException {
        String q = "  from " + DBItemJobStreamParameter + getWhere(filter);

        Query<DBItemJobStreamParameter> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public Integer delete(FilterJobStreamParameters filter) throws SOSHibernateException {
        int row = 0;
        String hql = "";

        hql = "delete from " + DBItemJobStreamParameter + getWhere(filter);
        Query<DBItemJobStreamParameter> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public void save(DBItemJobStreamParameter jsJobStreamParameter) throws SOSHibernateException {
        sosHibernateSession.save(jsJobStreamParameter);
    }

}