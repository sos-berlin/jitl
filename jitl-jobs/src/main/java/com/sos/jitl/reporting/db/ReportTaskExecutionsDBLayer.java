package com.sos.jitl.reporting.db;

import java.util.List;
import java.util.TimeZone;

import javax.persistence.TemporalType;

import org.hibernate.query.Query;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.SearchStringHelper;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.hibernate.layer.SOSHibernateIntervalDBLayer;
import com.sos.jitl.reporting.db.filter.ReportExecutionFilter;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.job.OrderPath;
import com.sos.joc.model.job.TaskIdOfOrder;

public class ReportTaskExecutionsDBLayer extends SOSHibernateIntervalDBLayer<DBItemReportTask> {

    protected ReportExecutionFilter filter = null;
    private String lastQuery = "";

    public ReportTaskExecutionsDBLayer(SOSHibernateSession conn) {
        super();
        sosHibernateSession = conn;
        resetFilter();
    }

    public DBItemReportTask get(Long id) throws SOSHibernateException {
        return (DBItemReportTask) (sosHibernateSession.get(DBItemReportTask.class, id));
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
            return "(endTime != null and error <> 1)";
        }

        if ("INCOMPLETE".equals(status)) {
            return "(startTime != null and endTime is null)";
        }

        if ("FAILED".equals(status)) {
            return "(endTime != null and error = 1)";
        }
        return "";
    }

    protected String getWhere() {
        String where = "";
        String and = "";
        if (filter.getSchedulerId() != null && !filter.getSchedulerId().isEmpty()) {
            where += and + " schedulerId=:schedulerId";
            and = " and ";
        }
        if (filter.getTaskIds() != null && !filter.getTaskIds().isEmpty()) {
            where += and + " historyId in (:taskIds)";
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
            if (filter.getListOfExcludedJobs() != null && filter.getListOfExcludedJobs().size() > 0) {
                where += and + "(";
                for (String job : filter.getListOfExcludedJobs()) {
                    where += " name <> '" + job + "' and";
                }
                where += " 1=1)";
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
        if (filter.getCriticality() != null && filter.getCriticality().size() > 0) {
            where += and + " criticality in (:criticalities)";
            and = " and ";
        }

        if (filter.getStates() != null && filter.getStates().size() > 0) {
            where += and + "(";
            for (String state : filter.getStates()) {
                where += getStatusClause(state) + " or ";
            }
            where += "1=0)";
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
        if (filter.getTaskIds() != null && !filter.getTaskIds().isEmpty()) {
            where += and + " historyId in (:taskIds)";
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

       
        if (filter.getCriticality() != null && filter.getCriticality().size() > 0) {
            where += and + " criticality in (:criticalities)";
            and = " and ";
        }

        if (filter.getStates() != null && filter.getStates().size() > 0) {
            where += and + "(";
            for (String state : filter.getStates()) {
                where += getStatusClause(state) + " or ";
            }
            where += "1=0)";
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
        String hql = "delete from " + DBLayer.DBITEM_REPORT_TASKS + " " + getWhereFromTo();
        Query<DBItemReportTask> query = sosHibernateSession.createQuery(hql);
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

    public int delete() throws Exception {
        int row = 0;
        String hql = "delete from " + DBLayer.DBITEM_REPORT_TASKS + " " + getWhereFromTo();
        Query<DBItemReportTask> query = sosHibernateSession.createQuery(hql);
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
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

    private List<DBItemReportTask> executeQuery(Query<DBItemReportTask> query) throws SOSHibernateException {
        lastQuery = query.getQueryString();
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getTaskIds() != null && !filter.getTaskIds().isEmpty()) {
            query.setParameterList("taskIds", filter.getTaskIds());
        }
        if (filter.getExecutedFrom() != null) {
            query.setParameter("startTimeFrom", filter.getExecutedFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getExecutedTo() != null) {
            query.setParameter("startTimeTo", filter.getExecutedTo(), TemporalType.TIMESTAMP);
        }
        if (filter.getCriticality() != null && !filter.getCriticality().isEmpty()) {
            query.setParameterList("criticalities", filter.getCriticality());
        }
        
        if (filter.getLimit() > 0) {
            query.setMaxResults(filter.getLimit());
        }
        return sosHibernateSession.getResultList(query);
    }

    public List<DBItemReportTask> getSchedulerHistoryListFromTo() throws SOSHibernateException {
        Query<DBItemReportTask> query = sosHibernateSession.createQuery(String.format("from %s %s %s %s", DBLayer.DBITEM_REPORT_TASKS,
                getWhereFromTo(), filter.getOrderCriteria(), filter.getSortMode()));
        return executeQuery(query);
    }

    public List<DBItemReportTask> getSchedulerHistoryListFromHistoryIdAndNode(List<TaskIdOfOrder> o) throws SOSHibernateException {
        if (o != null && !o.isEmpty()) {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_REPORT_TASKS).append(" where id in (");
            sql.append("select ta.id from " + DBLayer.DBITEM_REPORT_TASKS + " ta, " + DBItemReportExecution.class.getName() + " e");
            sql.append(" where ta.id=e.taskId");

            if (filter.getSchedulerId() != null && !filter.getSchedulerId().isEmpty()) {
                sql.append(" and ta.schedulerId=:schedulerId");
            }

            if (o.size() == 1) {
                sql.append(" and e.triggerHistoryId = :historyId and e.state = :state");
            } else {
                sql.append(" and ( 1=0");
                for (int i = 0; i < o.size(); i++) {
                    sql.append(" or (e.triggerHistoryId = :historyId" + i + " and e.state = :state" + i + ")");
                }
                sql.append(" )");
            }
            sql.append(")");
            sql.append(" order by historyId desc");

            Query<DBItemReportTask> query = sosHibernateSession.createQuery(sql.toString());
            if (o.size() == 1) {
                query.setParameter("historyId", o.get(0).getHistoryId());
                query.setParameter("state", o.get(0).getState());
            } else {
                sql.append(" and ( 1=0");
                for (int i = 0; i < o.size(); i++) {
                    query.setParameter("historyId" + i, o.get(i).getHistoryId());
                    query.setParameter("state" + i, o.get(i).getState());
                }
                sql.append(" )");
            }
            return executeQuery(query);
        } else {
            return null;
        }
    }

    public List<DBItemReportTask> getUnassignedSchedulerHistoryListFromTo() throws SOSHibernateException {
        Query<DBItemReportTask> query = sosHibernateSession.createQuery("from " + DBLayer.DBITEM_REPORT_TASKS + " " + getWhereFromTo()
                + " and id NOT IN (select reportExecutionId from "
                + "DailyPlanDBItem where reportExecutionId is not null and isAssigned=1 and schedulerId=:schedulerId) " + filter.getOrderCriteria()
                + filter.getSortMode());
        return executeQuery(query);
    }

    public List<DBItemReportTask> getSchedulerHistoryListFromToStart() throws SOSHibernateException {
        Query<DBItemReportTask> query = sosHibernateSession.createQuery(String.format("from %s %s %s %s", DBLayer.DBITEM_REPORT_TASKS,
                getWhereFromToStart(), filter.getOrderCriteria(), filter.getSortMode()));
        return executeQuery(query);
    }

    public List<DBItemReportTask> getSchedulerHistoryListFromToEnd() throws SOSHibernateException {
        Query<DBItemReportTask> query = sosHibernateSession.createQuery(String.format("from %s %s %s %s", DBLayer.DBITEM_REPORT_TASKS,
                getWhereFromToEnd(), filter.getOrderCriteria(), filter.getSortMode()));
        return executeQuery(query);
    }

    public List<DBItemReportTask> getSchedulerHistoryListSchedulersFromTo() throws SOSHibernateException {
        String q = "from " + DBLayer.DBITEM_REPORT_TASKS + " e where e.schedulerId IN (select distinct e.schedulerId from "
                + DBLayer.DBITEM_REPORT_TASKS + " " + getWhereFromTo() + ")";
        Query<DBItemReportTask> query = sosHibernateSession.createQuery(q);
        return executeQuery(query);
    }

    public Long getCountSchedulerJobHistoryListFromTo(boolean successful) throws SOSHibernateException {
        if (this.getFilter().getStates() != null) {
            this.getFilter().getStates().clear();
        }
        if (this.getFilter().getCriticality() != null) {
            this.getFilter().getCriticality().clear();
        }
        if (successful) {
            this.getFilter().addState("SUCCESSFUL");
        } else {
            this.getFilter().addState("FAILED");
        }
        String where = getWhereFromToStart();
        // where += (where.isEmpty()) ? " where" : " and";
        // where += (successful) ? " exitCode = 0" : " exitCode != 0";
        Query<Long> query = sosHibernateSession.createQuery("select count(*) from " + DBLayer.DBITEM_REPORT_TASKS + " " + where);

        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getExecutedFrom() != null) {
            query.setParameter("startTimeFrom", filter.getExecutedFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getExecutedTo() != null) {
            query.setParameter("startTimeTo", filter.getExecutedTo(), TemporalType.TIMESTAMP);
        }
        return sosHibernateSession.getSingleResult(query);
    }

    public List<DBItemReportTask> getHistoryItems() throws SOSHibernateException {
        int limit = this.getFilter().getLimit();
        Query<DBItemReportTask> query = sosHibernateSession.createQuery(String.format("from %s %s %s %s", DBLayer.DBITEM_REPORT_TASKS, getWhere(),
                filter.getOrderCriteria(), filter.getSortMode()));

        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
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
        List<DBItemReportTask> historyList = sosHibernateSession.getResultList(query);
        return historyList;
    }

    public List<DBItemReportTask> getLastHistoryItems() throws SOSHibernateException {
        int limit = this.getFilter().getLimit();
        Query<DBItemReportTask> query = sosHibernateSession.createQuery(String.format("from %s %s %s %s", 
                DBLayer.DBITEM_REPORT_TASKS + " a ", getWhere() + " and historyId=(select max(historyId) from " + DBLayer.DBITEM_REPORT_TASKS + " b where a.name=b.name)",
                filter.getOrderCriteria(), filter.getSortMode()));

        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
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
        List<DBItemReportTask> historyList = sosHibernateSession.getResultList(query);
        return historyList;
    }
    
    public DBItemReportTask getHistoryItem() throws SOSHibernateException {
        this.filter.setLimit(1);
        Query<DBItemReportTask> query = sosHibernateSession.createQuery(String.format("from %s %s %s %s", DBLayer.DBITEM_REPORT_TASKS, getWhere(),
                filter.getOrderCriteria(), filter.getSortMode()));

        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getStartTime() != null) {
            query.setParameter("startTime", filter.getStartTime(), TemporalType.TIMESTAMP);
        }
        if (filter.getEndTime() != null) {
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
    public void onAfterDeleting(DBItemReportTask h) {
        // Nothing to do
    }

    @Override
    public List<DBItemReportTask> getListOfItemsToDelete() throws SOSHibernateException {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
        int limit = this.getFilter().getLimit();
        Query<DBItemReportTask> query = sosHibernateSession.createQuery(String.format("from %s %s %s %s", DBLayer.DBITEM_REPORT_TASKS,
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

    public List<DBItemReportTask> getSchedulerHistoryListFromOrder(List<OrderPath> o) throws SOSHibernateException {
        if (o != null && !o.isEmpty()) {
            StringBuilder sql = new StringBuilder();
            sql.append("from ").append(DBLayer.DBITEM_REPORT_TASKS).append(" where id in (");
            sql.append("select ta.id from " + DBLayer.DBITEM_REPORT_TASKS + " ta, " + DBItemReportExecution.class.getName() + " e, "
                    + DBItemReportTrigger.class.getName() + " tr");
            sql.append(" where ta.id=e.taskId and e.triggerId=tr.id");
            if (filter.getSchedulerId() != null && !filter.getSchedulerId().isEmpty()) {
                sql.append(" and ta.schedulerId=:schedulerId");
            }

            if (o.size() == 1) {
                OrderPath orderPath = o.get(0);
                sql.append(" and tr.parentName = :parentName");
                if (orderPath.getOrderId() != null && !orderPath.getOrderId().isEmpty()) {
                    sql.append(" and tr.name = :orderId");
                }
                if (orderPath.getState() != null && !orderPath.getState().isEmpty()) {
                    sql.append(" and e.state = :state");
                }
            } else {
                sql.append(" and ( 1=0");
                for (int i = 0; i < o.size(); i++) {
                    OrderPath orderPath = o.get(i);
                    sql.append(" or (tr.parentName = :parentName" + i);
                    if (orderPath.getOrderId() != null && !orderPath.getOrderId().isEmpty()) {
                        sql.append(" and tr.name = :orderId" + i);
                    }
                    if (orderPath.getState() != null && !orderPath.getState().isEmpty()) {
                        sql.append(" and e.state = :state" + i);
                    }
                    sql.append(")");
                }
                sql.append(" )");
            }
            sql.append(")");
            sql.append(" order by historyId desc");

            Query<DBItemReportTask> query = sosHibernateSession.createQuery(sql.toString());
            if (o.size() == 1) {
                OrderPath orderPath = o.get(0);
                query.setParameter("parentName", orderPath.getJobChain());
                if (orderPath.getOrderId() != null && !orderPath.getOrderId().isEmpty()) {
                    query.setParameter("orderId", orderPath.getOrderId());
                }
                if (orderPath.getState() != null && !orderPath.getState().isEmpty()) {
                    query.setParameter("state", orderPath.getState());
                }
            } else {
                sql.append(" and ( 1=0");
                for (int i = 0; i < o.size(); i++) {
                    OrderPath orderPath = o.get(i);
                    query.setParameter("parentName" + i, orderPath.getJobChain());
                    if (orderPath.getOrderId() != null && !orderPath.getOrderId().isEmpty()) {
                        query.setParameter("orderId" + i, orderPath.getOrderId());
                    }
                    if (orderPath.getState() != null && !orderPath.getState().isEmpty()) {
                        query.setParameter("state" + i, orderPath.getState());
                    }
                    sql.append(")");
                }
                sql.append(" )");
            }

            return executeQuery(query);
        } else {
            return null;
        }
    }

    public List<DBItemReportTask> getSchedulerHistoryListFromTo(ReportExecutionFilter filter) throws SOSHibernateException {
        this.filter = filter;
        return getSchedulerHistoryListFromTo();
    }

}