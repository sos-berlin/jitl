package com.sos.jitl.dailyplan.db;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.layer.SOSHibernateIntervalDBLayer;
import com.sos.jitl.reporting.db.DBItemReportExecution;
import com.sos.scheduler.history.ReportExecutionFilter;

/** @author Uwe Risse */
public class DailyPlanExecutionsDBLayer extends SOSHibernateIntervalDBLayer {

    protected ReportExecutionFilter filter = null;
    private static final Logger LOGGER = Logger.getLogger(DailyPlanExecutionsDBLayer.class);
    private String lastQuery = "";

    public DailyPlanExecutionsDBLayer(String configurationFilename) {
        super();
        this.setConfigurationFileName(configurationFilename);
        this.initConnection(this.getConfigurationFileName());
        this.resetFilter();
    }

    public DailyPlanExecutionsDBLayer(File configurationFile) {
        super();
        try {
            this.setConfigurationFileName(configurationFile.getCanonicalPath());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            this.setConfigurationFileName("");
        }
        this.initConnection(this.getConfigurationFileName());
        this.resetFilter();
    }

    public DBItemReportExecution get(Long id) throws Exception {
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        DBItemReportExecution dbItemReportExecution = null;
        connection.beginTransaction();
        dbItemReportExecution = (DBItemReportExecution) ((Session) connection.getCurrentSession()).get(DBItemReportExecution.class, id);
        return dbItemReportExecution;
    }

    public void resetFilter() {
        this.filter = new ReportExecutionFilter();
        this.filter.setDateFormat("yyyy-MM-dd HH:mm:ss");
        this.filter.setOrderCriteria("startTime");
        this.filter.setSortMode("desc");
    }

    public ReportExecutionFilter getFilter() {
        return filter;
    }

    public void setFilter(ReportExecutionFilter filter_) {
        filter = filter_;
    }

    protected String getWhere() {
        String where = "";
        String and = "";
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            where += and + " schedulerId=:schedulerId";
            and = " and ";
        }
        if (filter.getJobname() != null && !"".equals(filter.getJobname())) {
            where += and + " name=:jobName and cause <> 'order'";
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
            where += and + " schedulerId=:schedulerId";
            and = " and ";
        }
        if (filter.getJobname() != null && !"".equals(filter.getJobname())) {
            if (filter.getJobname().contains("%")) {
                where += and + " name like :jobName";
            } else {
                where += and + " name=:jobName";
            }
            and = " and cause <> 'order' and ";
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

    public long deleteInterval() throws Exception {
        int row = 0;
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        String hql = "delete from DBItemReportExecution " + getWhereFromTo();
        connection.beginTransaction();
        Query query = connection.createQuery(hql);
        if (filter.getExecutedUtcFrom() != null) {
            query.setTimestamp("startTimeFrom", filter.getExecutedUtcFrom());
        }
        if (filter.getExecutedUtcTo() != null) {
            query.setTimestamp("startTimeTo", filter.getExecutedUtcTo());
        }
        row = query.executeUpdate();
        connection.commit();
        return row;
    }

    public int delete() throws Exception {
        int row = 0;
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        String hql = "delete from DBItemReportExecution " + getWhereFromTo();
        connection.beginTransaction();
        Query query = connection.createQuery(hql);
        if (filter.getSchedulerId() != null && !"".equalsIgnoreCase(filter.getSchedulerId())) {
            query.setText("schedulerId", filter.getSchedulerId());
        }
        if (filter.getSeverity() != null) {
            query.setInteger("severity", filter.getSeverity().getIntValue());
        }
        if (filter.getJobname() != null && !"".equalsIgnoreCase(filter.getJobname())) {
            query.setText("jobName", filter.getJobname());
        }
        if (filter.getExecutedUtcFrom() != null) {
            query.setTimestamp("startTimeFrom", filter.getExecutedUtcFrom());
        }
        if (filter.getExecutedUtcTo() != null) {
            query.setTimestamp("startTimeTo", filter.getExecutedUtcTo());
        }
        row = query.executeUpdate();
        connection.commit();
        return row;
    }

    @SuppressWarnings("unchecked")
    private List<DBItemReportExecution> executeQuery(Query query, int limit) {
        lastQuery = query.getQueryString();
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setText("schedulerId", filter.getSchedulerId());
        }
        if (filter.getSeverity() != null) {
            query.setInteger("severity", filter.getSeverity().getIntValue());
        }
        if (filter.getJobname() != null && !"".equals(filter.getJobname())) {
            query.setText("jobName", filter.getJobname());
        }
        if (filter.getExecutedUtcFrom() != null) {
            query.setTimestamp("startTimeFrom", filter.getExecutedUtcFrom());
        }
        if (filter.getExecutedUtcTo() != null) {
            query.setTimestamp("startTimeTo", filter.getExecutedUtcTo());
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return query.list();
    }

    public List<DBItemReportExecution> getSchedulerHistoryListFromTo() throws Exception {
        int limit = this.getFilter().getLimit();
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        Query query = null;
        connection.beginTransaction();
        query = connection.createQuery("from DBItemReportExecution " + getWhereFromTo() + filter.getOrderCriteria() + filter.getSortMode());
        return executeQuery(query, limit);
    }

    public List<DBItemReportExecution> getUnassignedSchedulerHistoryListFromTo() throws Exception {
        int limit = this.getFilter().getLimit();
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        Query query = null;
        connection.beginTransaction();
        query =
                connection.createQuery("from DBItemReportExecution " + getWhereFromTo() + " and id NOT IN (select reportExecutionId from "
                        + "DailyPlanDBItem where not reportExecutionId is null and isAssigned=1 and schedulerId=:schedulerId) "
                        + filter.getOrderCriteria() + filter.getSortMode());
        return executeQuery(query, limit);
    }

    public List<DBItemReportExecution> getSchedulerHistoryListFromToStart() throws Exception {
        int limit = this.getFilter().getLimit();
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        Query query = null;
        connection.beginTransaction();
        query = connection.createQuery("from DBItemReportExecution " + getWhereFromToStart() + filter.getOrderCriteria()
                + filter.getSortMode());
        return executeQuery(query, limit);
    }

    public List<DBItemReportExecution> getSchedulerHistoryListFromToEnd() throws Exception {
        int limit = this.getFilter().getLimit();
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        Query query = null;
        connection.beginTransaction();
        query = connection.createQuery("from DBItemReportExecution " + getWhereFromToStart() + filter.getOrderCriteria()
                + filter.getSortMode());
        return executeQuery(query, limit);
    }

    public List<DBItemReportExecution> getSchedulerHistoryListSchedulersFromTo() throws Exception {
        int limit = this.getFilter().getLimit();
        String q = "from DBItemReportExecution e where e.schedulerId IN (select distinct e.schedulerId from DBItemReportExecution "
                        + getWhereFromTo() + ")";
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        Query query = null;
        connection.beginTransaction();
        query = connection.createQuery(q);
        return executeQuery(query, limit);
    }

    @SuppressWarnings("unchecked")
    public List<DBItemReportExecution> getHistoryItems() throws Exception {
        int limit = this.getFilter().getLimit();
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        List<DBItemReportExecution> historyList = null;
        Query query = null;
        connection.beginTransaction();
        query =
                connection.createQuery("from DBItemReportExecution " + getWhere() + this.filter.getOrderCriteria()
                        + this.filter.getSortMode());
        if (filter.getSchedulerId() != null && !"".equalsIgnoreCase(filter.getSchedulerId())) {
            query.setText("schedulerId", filter.getSchedulerId());
        }
        if (filter.getSeverity() != null) {
            query.setInteger("severity", filter.getSeverity().getIntValue());
        }
        if (filter.getJobname() != null && !"".equalsIgnoreCase(filter.getJobname())) {
            query.setText("jobName", filter.getJobname());
        }
        if (filter.getStartTime() != null) {
            query.setTimestamp("startTime", filter.getStartTime());
        }
        if (filter.getEndTime() != null && !"".equals(filter.getEndTime())) {
            query.setTimestamp("endTime", filter.getEndTime());
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        historyList = query.list();
        return historyList;
    }

    @SuppressWarnings("unchecked")
    public DBItemReportExecution getHistoryItem() throws Exception {
        this.filter.setLimit(1);
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        List<DBItemReportExecution> historyList = null;
        Query query = null;
        connection.beginTransaction();
        query = connection.createQuery("from DBItemReportExecution " + getWhere() + this.filter.getOrderCriteria()
                        + this.filter.getSortMode());
        if (filter.getSchedulerId() != null && !"".equalsIgnoreCase(filter.getSchedulerId())) {
            query.setText("schedulerId", filter.getSchedulerId());
        }
        if (filter.getSeverity() != null) {
            query.setInteger("severity", filter.getSeverity().getIntValue());
        }
        if (filter.getJobname() != null && !"".equalsIgnoreCase(filter.getJobname())) {
            query.setText("jobName", filter.getJobname());
        }
        if (filter.getStartTime() != null) {
            query.setTimestamp("startTime", filter.getStartTime());
        }
        if (filter.getEndTime() != null && !"".equals(filter.getEndTime())) {
            query.setTimestamp("endTime", filter.getEndTime());
        }
        if (this.filter.getLimit() > 0) {
            query.setMaxResults(this.filter.getLimit());
        }
        historyList = query.list();
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
    public void onAfterDeleting(DbItem h) {
        // Nothing to do
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DbItem> getListOfItemsToDelete() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
        int limit = this.getFilter().getLimit();
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        Query query = null;
        List<DbItem> schedulerHistoryList = null;
        connection.beginTransaction();
        query = connection.createQuery("from DBItemReportExecution " + getWhereFromTo() + filter.getOrderCriteria() + filter.getSortMode());
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setText("schedulerId", filter.getSchedulerId());
        }
        if (filter.getSeverity() != null) {
            query.setInteger("severity", filter.getSeverity().getIntValue());
        }
        if (filter.getJobname() != null && !"".equals(filter.getJobname())) {
            query.setText("jobName", filter.getJobname());
        }
        if (filter.getExecutedUtcFrom() != null) {
            query.setTimestamp("startTimeFrom", filter.getExecutedUtcFrom());
        }
        if (filter.getExecutedUtcTo() != null) {
            query.setTimestamp("startTimeTo", filter.getExecutedUtcTo());
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        schedulerHistoryList = query.list();
        return schedulerHistoryList;
    }

}