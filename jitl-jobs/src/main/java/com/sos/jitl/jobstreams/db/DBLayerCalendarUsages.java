package com.sos.jitl.jobstreams.db;

import java.util.List;

import org.hibernate.query.Query;
import org.hibernate.transform.Transformers;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.reporting.db.DBLayer;

public class DBLayerCalendarUsages extends DBLayer {

    public DBLayerCalendarUsages(SOSHibernateSession sosHibernateSession) {
        super(sosHibernateSession);
    }

    private String getWhere(FilterCalendarUsage filter) {
        String where = "";
        String and = "";

        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            where += and + " c.schedulerId = :schedulerId";
            and = " and ";
        }

        if (filter.getPath() != null && !"".equals(filter.getPath())) {
            where += and + " u.path = :path";
            and = " and ";
        }
        if (filter.getObjectType() != null && !"".equals(filter.getObjectType())) {
            where += and + " u.objectType = :objectType";
            and = " and ";
        }
        if (filter.getJoin() != null && !"".equals(filter.getJoin())) {
            where += and + filter.getJoin();
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = "where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterCalendarUsage filter, Query<T> query) {
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getPath() != null && !"".equals(filter.getPath())) {
            query.setParameter("path", filter.getPath());
        }
        if (filter.getObjectType() != null && !"".equals(filter.getObjectType())) {
            query.setParameter("objectType", filter.getObjectType());
        }

        return query;
    }

    public List<DBItemCalendarWithUsages> getCalendarUsages(FilterCalendarUsage filter, final int limit) throws SOSHibernateException {

        filter.setJoin("c.id=u.calendarId");
        String q = "select u.path as path, u.configuration as restrictionConfiguration,c.configuration as calendarConfiguration, c.name as name from " + DBITEM_INVENTORY_CLUSTER_CALENDAR_USAGE + " u, "
                + DBITEM_CLUSTER_CALENDARS + " c " + getWhere(filter);
        Query<DBItemCalendarWithUsages> query = super.getSession().createQuery(q);
        query = bindParameters(filter, query);
        query.setResultTransformer(Transformers.aliasToBean(DBItemCalendarWithUsages.class));

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return super.getSession().getResultList(query);
    }

}
