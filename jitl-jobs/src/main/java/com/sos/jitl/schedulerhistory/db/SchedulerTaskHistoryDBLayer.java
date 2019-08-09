package com.sos.jitl.schedulerhistory.db;

import java.io.File;
import java.util.List;
import java.util.TimeZone;

import javax.persistence.TemporalType;

import org.hibernate.query.Query;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.hibernate.layer.SOSHibernateIntervalDBLayer;
import com.sos.jitl.schedulerhistory.SchedulerTaskHistoryFilter;

public class SchedulerTaskHistoryDBLayer extends SOSHibernateIntervalDBLayer<SchedulerTaskHistoryDBItem> {

    protected SchedulerTaskHistoryFilter filter = null;
    private String lastQuery = "";

    public SchedulerTaskHistoryDBLayer(File configurationFile_) throws SOSHibernateException {
        super();
        this.setConfigurationFileName(configurationFile_.getAbsolutePath());
        this.createStatelessConnection(configurationFile_.getAbsolutePath());
        this.resetFilter();
    }

    public SchedulerTaskHistoryDBLayer(SOSHibernateSession session) {
        super();
        this.setConfigurationFileName(session.getFactory().getConfigFile().get().toFile().getAbsolutePath());
        this.sosHibernateSession = session;
        this.resetFilter();
    }

    public SchedulerTaskHistoryDBItem get(Long id) {
        SchedulerTaskHistoryDBItem schedulerHistoryDBItem = null;
        try {
            schedulerHistoryDBItem = (SchedulerTaskHistoryDBItem) this.getSession().get(SchedulerTaskHistoryDBItem.class, id);
            return schedulerHistoryDBItem;
        } catch (Exception e) {
            return null;
        }
    }

    public void resetFilter() {
        this.filter = new SchedulerTaskHistoryFilter();
        this.filter.setDateFormat("yyyy-MM-dd HH:mm:ss");
        this.filter.setOrderCriteria("startTime");
        this.filter.setSortMode("desc");
    }

    public SchedulerTaskHistoryFilter getFilter() {
        return filter;
    }

    public void setFilter(SchedulerTaskHistoryFilter filter_) {
        filter = filter_;
    }

    protected String getWhere() {
        String where = "";
        String and = "";
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            where += and + " spoolerId=:schedulerId";
            and = " and ";
        }
        if (filter.getJobname() != null && !"".equals(filter.getJobname())) {
            where += and + " jobName=:jobName";
            and = " and ";
        }
        if (filter.getSeverity() != null && filter.getSeverity().hasValue()) {
            where += and + " error=:severity";
            and = " and ";
        }
        if (filter.getStartTime() != null) {
            where += and + " startTime>= :startTime";
            and = " and ";
        }
        if (filter.getEndTime() != null) {
            where += and + " endTime <= :endTime ";
            and = " and ";
        }
        if (!"".equals(where.trim())) {
            where = "where " + where;
        }
        return where;
    }

    protected String getWhereFromTo() {
        return getWhereFromToStart();
    }

    protected String getWhereFromToStart() {
        return getWhereFromTo("startTime");
    }

    protected String getWhereFromToEnd() {
        return getWhereFromTo("endTime");
    }

    protected String getWhereFromTo(String fieldname_date_field) {
        String where = "";
        String and = "";
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            where += and + " spoolerId=:schedulerId";
            and = " and ";
        }
        if (filter.getJobname() != null && !"".equals(filter.getJobname())) {
            if (filter.getJobname().contains("%")) {
                where += and + " jobName like :jobName";
            } else {
                where += and + " jobName=:jobName";
            }
            and = " and ";
        }
        if (filter.getExecutedUtcFrom() != null) {
            where += and + fieldname_date_field + " >= :startTimeFrom";
            and = " and ";
        }
        if (filter.getExecutedUtcTo() != null) {
            where += and + fieldname_date_field + " <= :startTimeTo ";
            and = " and ";
        }
        if (!filter.isShowJobs()) {
            where += and + " 1=0";
            and = " and ";
        }
        if (filter.isShowSuccessfull()) {
            where += and + " exitCode=0";
            and = " and ";
        }
        if (!"".equals(where.trim())) {
            where = "where " + where;
        }
        return where;
    }

    public long deleteInterval() throws SOSHibernateException {
        String hql = "delete from SchedulerTaskHistoryDBItem " + getWhereFromTo();
        Query<SchedulerTaskHistoryDBItem> query = sosHibernateSession.createQuery(hql);
        if (filter.getExecutedUtcFrom() != null) {
            query.setParameter("startTimeFrom", filter.getExecutedUtcFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getExecutedUtcTo() != null) {
            query.setParameter("startTimeTo", filter.getExecutedUtcTo(), TemporalType.TIMESTAMP);
        }
        return sosHibernateSession.executeUpdate(query);
    }

    public int delete() throws SOSHibernateException {
        String hql = "delete from SchedulerTaskHistoryDBItem " + getWhereFromTo();
        Query<SchedulerTaskHistoryDBItem> query = sosHibernateSession.createQuery(hql);
        if (filter.getSchedulerId() != null && !"".equalsIgnoreCase(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getSeverity() != null) {
            query.setParameter("severity", filter.getSeverity().getIntValue());
        }
        if (filter.getJobname() != null && !"".equalsIgnoreCase(filter.getJobname())) {
            query.setParameter("jobName", filter.getJobname());
        }
        if (filter.getExecutedUtcFrom() != null) {
            query.setParameter("startTimeFrom", filter.getExecutedUtcFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getExecutedUtcTo() != null) {
            query.setParameter("startTimeTo", filter.getExecutedUtcTo(), TemporalType.TIMESTAMP);
        }
        return sosHibernateSession.executeUpdate(query);
    }

    private List<SchedulerTaskHistoryDBItem> executeQuery(Query<SchedulerTaskHistoryDBItem> query, int limit) throws SOSHibernateException {
        lastQuery = query.getQueryString();
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getSeverity() != null) {
            query.setParameter("severity", filter.getSeverity().getIntValue());
        }
        if (filter.getJobname() != null && !"".equals(filter.getJobname())) {
            query.setParameter("jobName", filter.getJobname());
        }
        if (filter.getExecutedUtcFrom() != null) {
            query.setParameter("startTimeFrom", filter.getExecutedUtcFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getExecutedUtcTo() != null) {
            query.setParameter("startTimeTo", filter.getExecutedUtcTo(), TemporalType.TIMESTAMP);
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public List<SchedulerTaskHistoryDBItem> getSchedulerHistoryListFromTo() throws SOSHibernateException {
        int limit = this.getFilter().getLimit();
        Query<SchedulerTaskHistoryDBItem> query = sosHibernateSession.createQuery("from SchedulerTaskHistoryDBItem " + getWhereFromTo() + filter
                .getOrderCriteria() + filter.getSortMode());
        return executeQuery(query, limit);
    }

    public List<SchedulerTaskHistoryDBItem> getUnassignedSchedulerHistoryListFromTo() throws SOSHibernateException {
        int limit = this.getFilter().getLimit();
        Query<SchedulerTaskHistoryDBItem> query = sosHibernateSession.createQuery("from SchedulerTaskHistoryDBItem " + getWhereFromTo()
                + " and id NOT IN (select schedulerHistoryId from "
                + "DailyScheduleDBItem where not schedulerHistoryId is null and  status=1 and schedulerId=:schedulerId) " + filter.getOrderCriteria()
                + filter.getSortMode());
        return executeQuery(query, limit);
    }

    public List<SchedulerTaskHistoryDBItem> getSchedulerHistoryListFromToStart() throws SOSHibernateException {
        int limit = this.getFilter().getLimit();
        Query<SchedulerTaskHistoryDBItem> query = sosHibernateSession.createQuery("from SchedulerTaskHistoryDBItem " + getWhereFromToStart() + filter
                .getOrderCriteria() + filter.getSortMode());
        return executeQuery(query, limit);
    }

    public List<SchedulerTaskHistoryDBItem> getSchedulerHistoryListFromToEnd() throws SOSHibernateException {
        int limit = this.getFilter().getLimit();
        Query<SchedulerTaskHistoryDBItem> query = sosHibernateSession.createQuery("from SchedulerTaskHistoryDBItem " + getWhereFromToStart() + filter
                .getOrderCriteria() + filter.getSortMode());
        return executeQuery(query, limit);
    }

    public List<SchedulerTaskHistoryDBItem> getSchedulerHistoryListSchedulersFromTo() throws SOSHibernateException {
        int limit = this.getFilter().getLimit();
        String q = "from SchedulerTaskHistoryDBItem e where e.spoolerId IN (select distinct e.spoolerId from SchedulerTaskHistoryDBItem "
                + getWhereFromTo() + ")";
        Query<SchedulerTaskHistoryDBItem> query = sosHibernateSession.createQuery(q);
        return executeQuery(query, limit);
    }

    public List<SchedulerTaskHistoryDBItem> getHistoryItems() throws SOSHibernateException {
        int limit = this.getFilter().getLimit();
        Query<SchedulerTaskHistoryDBItem> query = sosHibernateSession.createQuery("from SchedulerTaskHistoryDBItem " + getWhere() + this.filter
                .getOrderCriteria() + this.filter.getSortMode());
        if (filter.getSchedulerId() != null && !"".equalsIgnoreCase(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getSeverity() != null) {
            query.setParameter("severity", filter.getSeverity().getIntValue());
        }
        if (filter.getJobname() != null && !"".equalsIgnoreCase(filter.getJobname())) {
            query.setParameter("jobName", filter.getJobname());
        }
        if (filter.getStartTime() != null) {
            query.setParameter("startTime", filter.getStartTime(), TemporalType.TIMESTAMP);
        }
        if (filter.getEndTime() != null && !"".equals(filter.getEndTime())) {
            query.setParameter("endTime", filter.getEndTime(), TemporalType.TIMESTAMP);
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public SchedulerTaskHistoryDBItem getHistoryItem() throws SOSHibernateException {
        this.filter.setLimit(1);
        Query<SchedulerTaskHistoryDBItem> query = sosHibernateSession.createQuery("from SchedulerTaskHistoryDBItem " + getWhere() + this.filter
                .getOrderCriteria() + this.filter.getSortMode());
        if (filter.getSchedulerId() != null && !"".equalsIgnoreCase(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getSeverity() != null) {
            query.setParameter("severity", filter.getSeverity().getIntValue());
        }
        if (filter.getJobname() != null && !"".equalsIgnoreCase(filter.getJobname())) {
            query.setParameter("jobName", filter.getJobname());
        }
        if (filter.getStartTime() != null) {
            query.setParameter("startTime", filter.getStartTime(), TemporalType.TIMESTAMP);
        }
        if (filter.getEndTime() != null && !"".equals(filter.getEndTime())) {
            query.setParameter("endTime", filter.getEndTime(), TemporalType.TIMESTAMP);
        }
        if (this.filter.getLimit() > 0) {
            query.setMaxResults(this.filter.getLimit());
        }
        List<SchedulerTaskHistoryDBItem> historyList = sosHibernateSession.getResultList(query);
        if (historyList != null && !historyList.isEmpty()) {
            return historyList.get(0);
        } else {
            return null;
        }
    }

    public String getLastQuery() {
        return lastQuery;
    }

    @Override
    public void onAfterDeleting(SchedulerTaskHistoryDBItem h) {
        // Nothing to do
    }

    @Override
    public List<SchedulerTaskHistoryDBItem> getListOfItemsToDelete() throws SOSHibernateException {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
        int limit = this.getFilter().getLimit();
        Query<SchedulerTaskHistoryDBItem> query = sosHibernateSession.createQuery("from SchedulerTaskHistoryDBItem " + getWhereFromTo() + filter
                .getOrderCriteria() + filter.getSortMode());
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getSeverity() != null) {
            query.setParameter("severity", filter.getSeverity().getIntValue());
        }
        if (filter.getJobname() != null && !"".equals(filter.getJobname())) {
            query.setParameter("jobName", filter.getJobname());
        }
        if (filter.getExecutedUtcFrom() != null) {
            query.setParameter("startTimeFrom", filter.getExecutedUtcFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getExecutedUtcTo() != null) {
            query.setParameter("startTimeTo", filter.getExecutedUtcTo(), TemporalType.TIMESTAMP);
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

}