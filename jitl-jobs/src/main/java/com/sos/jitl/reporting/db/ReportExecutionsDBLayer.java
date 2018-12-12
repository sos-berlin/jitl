package com.sos.jitl.reporting.db;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TimeZone;

import javax.persistence.TemporalType;

import org.apache.log4j.Logger;
import org.hibernate.query.Query;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.SearchStringHelper;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.hibernate.layer.SOSHibernateIntervalDBLayer;
import com.sos.jitl.reporting.db.filter.ReportExecutionFilter;
import com.sos.joc.model.common.Folder;

/** @author Uwe Risse */
public class ReportExecutionsDBLayer extends SOSHibernateIntervalDBLayer<DBItemReportExecution> {

    private static final String DBItemReportExecution = DBItemReportExecution.class.getName();

    protected ReportExecutionFilter filter = null;
    private static final Logger LOGGER = Logger.getLogger(ReportExecutionsDBLayer.class);
    private String lastQuery = "";

    public ReportExecutionsDBLayer(String configurationFilename) throws SOSHibernateException {
        super();
        this.setConfigurationFileName(configurationFilename);
        this.createStatelessConnection(this.getConfigurationFileName());
        this.resetFilter();
    }

    public ReportExecutionsDBLayer(File configurationFile) throws SOSHibernateException {
        super();
        try {
            this.createStatelessConnection(configurationFile.getCanonicalPath());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            this.setConfigurationFileName("");
        }
        this.resetFilter();
    }

    public ReportExecutionsDBLayer(SOSHibernateSession conn) {
        super();
        sosHibernateSession = conn;
        resetFilter();
    }

    public DBItemReportExecution get(Long id) throws SOSHibernateException {
        return (DBItemReportExecution) (sosHibernateSession.get(DBItemReportExecution.class, id));
    }

    public void resetFilter() {
        this.filter = new ReportExecutionFilter();
        this.filter.setDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        this.filter.setOrderCriteria("startTime");
        this.filter.setSortMode("desc");
    }

    public ReportExecutionFilter getFilter() {
        return filter;
    }

    public void setFilter(ReportExecutionFilter filter_) {
        filter = filter_;
    }

    private String getStatusClause(String status) {
        if ("SUCCESSFUL".equals(status)) {
            return "(endTime is not null and error <> 1)";
        }

        if ("INCOMPLETE".equals(status)) {
            return "(startTime is not null and endTime is null)";
        }

        if ("FAILED".equals(status)) {
            return "(endTime is not null and error = 1)";
        }
        return "";
    }

    protected String getWhere() {
        String where = "";
        String and = "";
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            where += and + " schedulerId=:schedulerId";
            and = " and ";
        }

        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            where += and + " name >= :orderId";
            and = " and ";
        }
        if (filter.getJobChain() != null && !"".equals(filter.getJobChain())) {
            where += String.format(and + " parentName %s :jobChain", SearchStringHelper.getSearchPathOperator(filter.getJobChain()));
            and = " and ";
        }

        if (filter.getStartTime() != null) {
            where += and + " startTime>= :startTime";
            and = " and ";
        }
        if (filter.getEndTime() != null) {
            where += and + " endTime < :endTime ";
            and = " and ";
        }
        if (filter.getListOfJobs() != null && filter.getListOfJobs().size() > 0) {
            where += and + SearchStringHelper.getStringListPathSql(filter.getListOfJobs(), "name");
            and = " and ";
        } else {

            if (filter.getStates() != null && filter.getStates().size() > 0) {
                where += and + "(";
                for (String state : filter.getStates()) {
                    where += getStatusClause(state) + " or";
                }
                where += " 1=0)";
                and = " and ";
            }
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

        if (filter.getExecutedFrom() != null) {
            where += and + fieldname_date_field + " >= :startTimeFrom";
            and = " and ";
        }

        if (filter.getExecutedTo() != null) {
            where += and + fieldname_date_field + " < :startTimeTo ";
            and = " and ";
        }

        if (filter.getListOfJobs() != null && filter.getListOfJobs().size() > 0) {
            where += and + SearchStringHelper.getStringListPathSql(filter.getListOfJobs(), "name");
            and = " and ";
        } else {
            if (filter.getListOfExcludedJobs() != null && filter.getListOfExcludedJobs().size() > 0) {
                where += and + "(";
                for (String job : filter.getListOfExcludedJobs()) {
                    where += " name <> '" + job + "' and";
                }
                where += " 1=1)";
                and = " and ";
            }
            if (filter.getStates() != null && filter.getStates().size() > 0) {
                where += and + "(";
                for (String state : filter.getStates()) {
                    where += getStatusClause(state) + " or";
                }
                where += " 1=0)";
                and = " and ";
            }
            if (filter.getListOfFolders() != null && filter.getListOfFolders().size() > 0) {
                where += and + "(";
                for (Folder filterFolder : filter.getListOfFolders()) {
                    if (filterFolder.getRecursive()) {
                        String likeFolder = (filterFolder.getFolder() + "/%").replaceAll("//+", "/");
                        where += " (folder = '" + filterFolder.getFolder() + "' or folder like '" + likeFolder + "')";
                    } else {
                        where += " folder = '" + filterFolder.getFolder() + "'";
                    }
                    where += " or ";
                }
                where += " 0=1)";
                and = " and ";
            }
        }

        if (!"".equals(where.trim())) {
            where = "where " + where;
        }
        return where;
    }

    public long deleteInterval() throws SOSHibernateException {
        int row = 0;
        String hql = "delete from " + DBItemReportExecution + " " + getWhereFromTo();
        Query<DBItemReportExecution> query = sosHibernateSession.createQuery(hql);
        if (filter.getExecutedFrom() != null) {
            query.setParameter("startTimeFrom", filter.getExecutedFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getExecutedTo() != null) {
            query.setParameter("startTimeTo", filter.getExecutedTo(), TemporalType.TIMESTAMP);
        }
        row = sosHibernateSession.executeUpdate(query);
        sosHibernateSession.commit();
        return row;
    }

    public int delete() throws SOSHibernateException {
        int row = 0;
        String hql = "delete from " + DBItemReportExecution + " " + getWhereFromTo();
        Query<DBItemReportExecution> query = sosHibernateSession.createQuery(hql);
        if (filter.getSchedulerId() != null && !"".equalsIgnoreCase(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getExecutedFrom() != null) {
            query.setParameter("startTimeFrom", filter.getExecutedFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getExecutedTo() != null) {
            query.setParameter("startTimeTo", filter.getExecutedTo(), TemporalType.TIMESTAMP);
        }
        row = sosHibernateSession.executeUpdate(query);
        sosHibernateSession.commit();
        return row;
    }

    private List<DBItemReportExecution> executeQuery(Query<DBItemReportExecution> query, int limit) throws SOSHibernateException {
        lastQuery = query.getQueryString();
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getExecutedFrom() != null) {
            query.setParameter("startTimeFrom", filter.getExecutedFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getExecutedTo() != null) {
            query.setParameter("startTimeTo", filter.getExecutedTo(), TemporalType.TIMESTAMP);
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public List<DBItemReportExecution> getOrderStepHistoryItems() throws SOSHibernateException {
        Query<DBItemReportExecution> query = sosHibernateSession.createQuery(String.format("from " + DBItemReportExecution
                + " where triggerHistoryId = %s order by step asc", filter.getOrderHistoryId()));
        return sosHibernateSession.getResultList(query);
    }

    public List<DBItemReportExecution> getSchedulerHistoryListFromTo() throws SOSHibernateException {
        int limit = this.getFilter().getLimit();
        Query<DBItemReportExecution> query = sosHibernateSession.createQuery(String.format("from %s %s %s %s", DBItemReportExecution,
                getWhereFromTo(), filter.getOrderCriteria(), filter.getSortMode()));
        return executeQuery(query, limit);
    }

    public List<DBItemReportExecution> getUnassignedSchedulerHistoryListFromTo() throws SOSHibernateException {
        int limit = this.getFilter().getLimit();
        Query<DBItemReportExecution> query = sosHibernateSession.createQuery("from " + DBItemReportExecution + " " + getWhereFromTo()
                + " and id NOT IN (select reportExecutionId from "
                + "DailyPlanDBItem where reportExecutionId is not null and isAssigned=1 and schedulerId=:schedulerId) " + filter.getOrderCriteria()
                + filter.getSortMode());
        return executeQuery(query, limit);
    }

    public List<DBItemReportExecution> getSchedulerHistoryListFromToStart() throws SOSHibernateException {
        int limit = this.getFilter().getLimit();
        Query<DBItemReportExecution> query = sosHibernateSession.createQuery(String.format("from %s %s %s %s", DBItemReportExecution,
                getWhereFromToStart(), filter.getOrderCriteria(), filter.getSortMode()));
        return executeQuery(query, limit);
    }

    public List<DBItemReportExecution> getSchedulerHistoryListFromToEnd() throws SOSHibernateException {
        int limit = this.getFilter().getLimit();
        Query<DBItemReportExecution> query = sosHibernateSession.createQuery(String.format("from %s %s %s %s", DBItemReportExecution,
                getWhereFromToEnd(), filter.getOrderCriteria(), filter.getSortMode()));
        return executeQuery(query, limit);
    }

    public List<DBItemReportExecution> getSchedulerHistoryListSchedulersFromTo() throws SOSHibernateException {
        int limit = this.getFilter().getLimit();
        String q = "from " + DBItemReportExecution + " e where e.schedulerId IN (select distinct e.schedulerId from " + DBItemReportExecution + " "
                + getWhereFromTo() + ")";
        Query<DBItemReportExecution> query = sosHibernateSession.createQuery(q);
        return executeQuery(query, limit);
    }

    public List<DBItemReportExecution> getHistoryItems() throws SOSHibernateException {
        int limit = this.getFilter().getLimit();
        List<DBItemReportExecution> historyList = null;
        Query<DBItemReportExecution> query = sosHibernateSession.createQuery(String.format("from %s %s %s %s", DBItemReportExecution, getWhere(),
                filter.getOrderCriteria(), filter.getSortMode()));

        if (filter.getSchedulerId() != null && !"".equalsIgnoreCase(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
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
        historyList = sosHibernateSession.getResultList(query);
        return historyList;
    }

    public DBItemReportExecution getHistoryItem() throws SOSHibernateException {
        this.filter.setLimit(1);
        Query<DBItemReportExecution> query = sosHibernateSession.createQuery(String.format("from %s %s %s %s", DBItemReportExecution, getWhere(),
                filter.getOrderCriteria(), filter.getSortMode()));

        if (filter.getSchedulerId() != null && !"".equalsIgnoreCase(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
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
        return sosHibernateSession.getSingleResult(query);
    }

    public String getLastQuery() {
        return lastQuery;
    }

    @Override
    public void onAfterDeleting(DBItemReportExecution h) {
        // Nothing to do
    }

    @Override
    public List<DBItemReportExecution> getListOfItemsToDelete() throws SOSHibernateException {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
        int limit = this.getFilter().getLimit();
        Query<DBItemReportExecution> query = sosHibernateSession.createQuery(String.format("from %s %s %s %s", DBItemReportExecution,
                getWhereFromTo(), filter.getOrderCriteria(), filter.getSortMode()));
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getExecutedFrom() != null) {
            query.setParameter("startTimeFrom", filter.getExecutedFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getExecutedTo() != null) {
            query.setParameter("startTimeTo", filter.getExecutedTo(), TemporalType.TIMESTAMP);
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

}