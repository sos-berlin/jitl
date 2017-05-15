package com.sos.jitl.schedulerhistory.db;

import java.io.File;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.hibernate.query.Query;

import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.hibernate.layer.SOSHibernateDBLayer;
import com.sos.jitl.schedulerhistory.SchedulerOrderStepHistoryFilter;

public class SchedulerOrderStepHistoryDBLayer extends SOSHibernateDBLayer {

    protected SchedulerOrderStepHistoryFilter filter = null;

    public SchedulerOrderStepHistoryDBLayer(final File configurationFile) throws SOSHibernateException {
        super();
        this.setConfigurationFileName(configurationFile.getAbsolutePath());
        this.createStatelessConnection(configurationFile.getAbsolutePath());
        resetFilter();
    }

    public SchedulerOrderStepHistoryDBItem get(final SchedulerOrderStepHistoryCompoundKey id) throws SOSHibernateException  {
        return (SchedulerOrderStepHistoryDBItem) this.getSession().get(SchedulerOrderStepHistoryDBItem.class, id);
    }

    public void resetFilter() {
        filter = new SchedulerOrderStepHistoryFilter();
        filter.setDateFormat("yyyy-MM-dd HH:mm:ss");
        filter.setOrderCriteria("startTime");
        filter.setSortMode("desc");
    }

    protected String getWhereFromTo() {
        String where = "";
        String and = "";
        if (filter.getExecutedFromUtc() != null) {
            where += and + " startTime>= :startTimeFrom";
            and = " and ";
        }
        if (filter.getExecutedToUtc() != null) {
            where += and + " startTime <= :startTimeTo ";
            and = " and ";
        }
        if (!"".equals(where.trim())) {
            where = "where " + where;
        }
        return where;
    }

    protected String getWhere() {
        String where = "";
        String and = "";
        if (filter.getHistoryId() != null) {
            where += and + " id.historyId = :historyId";
            and = " and ";
        }
        if (filter.getStartTime() != null && !"".equals(filter.getStartTime())) {
            where += and + " startTime>= :startTime";
            and = " and ";
        }
        if (filter.getEndTime() != null && !"".equals(filter.getEndTime())) {
            where += and + " endTime <= :endTime ";
            and = " and ";
        }
        if (!"".equals(where.trim())) {
            where = "where " + where;
        }
        return where;
    }

    public int deleteFromTo() throws SOSHibernateException  {
        sosHibernateSession.beginTransaction();
        String hql = "delete from SchedulerOrderStepHistoryDBItem " + getWhereFromTo();
        Query query = sosHibernateSession.createQuery(hql);
        query.setTimestamp("startTimeFrom", filter.getExecutedFromUtc());
        query.setTimestamp("startTimeTo", filter.getExecutedToUtc());
        return sosHibernateSession.executeUpdate(query);
    }

    public void deleteInterval(final int interval) throws SOSHibernateException {
        GregorianCalendar now = new GregorianCalendar();
        now.add(GregorianCalendar.DAY_OF_YEAR, -interval);
        filter.setExecutedTo(new Date());
        filter.setExecutedFrom(now.getTime());
        this.deleteFromTo();
    }

    @SuppressWarnings("unchecked")
    public List<SchedulerOrderStepHistoryDBItem> getSchedulerOrderStepHistoryListFromTo(final int limit) throws SOSHibernateException  {
        Query query =
                sosHibernateSession.createQuery("from SchedulerOrderStepHistoryDBItem " + getWhereFromTo() + filter.getOrderCriteria() + filter.getSortMode());
        if (filter.getExecutedFromUtc() != null && !"".equals(filter.getExecutedFromUtc())) {
            query.setTimestamp("startTimeFrom", filter.getExecutedFromUtc());
        }
        if (filter.getExecutedToUtc() != null && !"".equals(filter.getExecutedToUtc())) {
            query.setTimestamp("startTimeTo", filter.getExecutedToUtc());
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public List<SchedulerOrderStepHistoryDBItem> getOrderStepHistoryItems(final int limit, long historyId) throws SOSHibernateException  {
        filter.setHistoryId(historyId);
        sosHibernateSession.beginTransaction();
        Query query = sosHibernateSession.createQuery("from SchedulerOrderStepHistoryDBItem " + getWhere());
        if (filter.getHistoryId() != null) {
            query.setLong("historyId", filter.getHistoryId());
        }
        if (filter.getStartTime() != null && !"".equals(filter.getStartTime())) {
            query.setTimestamp("startTime", filter.getStartTime());
        }
        if (filter.getStartTime() != null && !"".equals(filter.getStartTime())) {
            query.setTimestamp("startTime", filter.getStartTime());
        }
        if (filter.getEndTime() != null && !"".equals(filter.getEndTime())) {
            query.setTimestamp("endTime", filter.getEndTime());
        }
        if (limit != 0) {
            query.setMaxResults(limit);
        }
        @SuppressWarnings("unchecked")
        List<SchedulerOrderStepHistoryDBItem> historyList = sosHibernateSession.getResultList(query);
        sosHibernateSession.commit();
        return historyList;
    }

    public SchedulerOrderStepHistoryFilter getFilter() {
        return filter;
    }

}