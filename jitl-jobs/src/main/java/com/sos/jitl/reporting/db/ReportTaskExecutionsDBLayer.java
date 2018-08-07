package com.sos.jitl.reporting.db;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TimeZone;

import javax.persistence.TemporalType;

import org.apache.log4j.Logger;
import org.hibernate.query.Query;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.hibernate.layer.SOSHibernateIntervalDBLayer;
import com.sos.jitl.reporting.db.filter.ReportExecutionFilter;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.job.OrderPath;
import com.sos.joc.model.job.TaskIdOfOrder;

/** @author Uwe Risse */
public class ReportTaskExecutionsDBLayer extends SOSHibernateIntervalDBLayer<DBItemReportTask> {

    private static final String DBItemReportTask = DBItemReportTask.class.getName();

    protected ReportExecutionFilter filter = null;
    private static final Logger LOGGER = Logger.getLogger(ReportTaskExecutionsDBLayer.class);
    private String lastQuery = "";

    public ReportTaskExecutionsDBLayer(String configurationFilename) throws SOSHibernateException  {
        super();
        this.setConfigurationFileName(configurationFilename);
        this.createStatelessConnection(this.getConfigurationFileName());
        this.resetFilter();
    }

    public ReportTaskExecutionsDBLayer(File configurationFile) throws SOSHibernateException  {
        super();
        try {
            this.createStatelessConnection(configurationFile.getCanonicalPath());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            this.setConfigurationFileName("");
        }
        this.resetFilter();
    }

    public ReportTaskExecutionsDBLayer(SOSHibernateSession conn) {
        super();
        sosHibernateSession = conn;
        resetFilter();
    }

    public DBItemReportTask get(Long id) throws SOSHibernateException   {
        return (DBItemReportTask) ( sosHibernateSession.get(DBItemReportTask.class, id));
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
    
    private String getStatusClause(String status){
        if ("SUCCESSFUL".equals(status)){
            return "(endTime is not null and error <> 1)";
        }

        if ("INCOMPLETE".equals(status)){
            return "(startTime is not null and endTime is null)";
        }

        if ("FAILED".equals(status)){
            return "(endTime is not null and error = 1)";
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
            where += and +  " historyId in (:taskIds)";
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
            where += and + "(";
            for (String job : filter.getListOfJobs()) {
                where += " name = '" + job + "' or";
            }
            where += " 1=0)";
            and = " and ";
        } else {

            if (filter.getStates() != null && filter.getStates().size() > 0) {
                where += and + "(";
                for (String state : filter.getStates()) {
                    where +=  getStatusClause(state) + " or";
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
        if (filter.getTaskIds() != null && !filter.getTaskIds().isEmpty()) {
            where += and +  " taskId in (:taskIds)";
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

        if (filter.getStates() != null && filter.getStates().size() > 0) {
            where += and + "(";
            for (String state : filter.getStates()) {
                where +=  getStatusClause(state) + " or";
            }
            where += " 1=0)";
            and = " and ";
        }

        if (filter.getListOfJobs() != null && filter.getListOfJobs().size() > 0) {
            where += and + "(";
            for (String job : filter.getListOfJobs()) {
                where += " name = '" + job + "' or";
            }
            where += " 1=0)";
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

    public long deleteInterval() throws SOSHibernateException{
        int row = 0;
        String hql = "delete from " + DBItemReportTask + " " + getWhereFromTo();
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
        String hql = "delete from " + DBItemReportTask + " " + getWhereFromTo();
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
        if (filter.getLimit() > 0) {
            query.setMaxResults(filter.getLimit());
        }
        return sosHibernateSession.getResultList(query);
    }


    public List<DBItemReportTask> getSchedulerHistoryListFromTo() throws SOSHibernateException{
        Query<DBItemReportTask> query = sosHibernateSession.createQuery(String.format("from %s %s %s %s", DBItemReportTask, getWhereFromTo(), filter.getOrderCriteria(), filter.getSortMode()));
        return executeQuery(query);
    }

    public List<DBItemReportTask> getSchedulerHistoryListFromHistoryIdAndNode(List<TaskIdOfOrder> o) throws SOSHibernateException {
        if (o != null && !o.isEmpty()) {
            StringBuilder sql = new StringBuilder();
            sql.append("select ta from " + DBItemReportTask + " ta, " + DBItemReportExecution.class.getName() + " e, " + DBItemReportTrigger.class.getName() + " tr");
            sql.append(" where ta.id=e.taskId and e.triggerId=tr.id");
            if (filter.getSchedulerId() != null && !filter.getSchedulerId().isEmpty()) {
                sql.append(" and ta.schedulerId=:schedulerId");
            }

            if (o.size() == 1) {
                sql.append(" and tr.historyId = " + o.get(0).getHistoryId() + " and e.state = '" + o.get(0).getState() + "'");
            } else {
                sql.append(" and ( 1=0");
                for (TaskIdOfOrder item : o) {
                    sql.append(" or (tr.historyId = " + item.getHistoryId() + " and e.state = '" + item.getState() + "')");
                }
                sql.append(" )");
            }
            sql.append(" order by ta.historyId desc");

            Query<DBItemReportTask> query = sosHibernateSession.createQuery(sql.toString());
            return executeQuery(query);
        } else {
            return null;
        }
    }
    
    public List<DBItemReportTask> getUnassignedSchedulerHistoryListFromTo() throws SOSHibernateException {
        Query<DBItemReportTask> query = sosHibernateSession.createQuery("from " + DBItemReportTask + " " + getWhereFromTo() + " and id NOT IN (select reportExecutionId from "
                + "DailyPlanDBItem where reportExecutionId is not null and isAssigned=1 and schedulerId=:schedulerId) " + filter.getOrderCriteria() + filter.getSortMode());
        return executeQuery(query);
    }

    public List<DBItemReportTask> getSchedulerHistoryListFromToStart() throws SOSHibernateException  {
        Query<DBItemReportTask> query = sosHibernateSession.createQuery(String.format("from %s %s %s %s", DBItemReportTask, getWhereFromToStart(), filter.getOrderCriteria(), filter.getSortMode()));
        return executeQuery(query);
    }

    public List<DBItemReportTask> getSchedulerHistoryListFromToEnd() throws SOSHibernateException  {
        Query<DBItemReportTask> query = sosHibernateSession.createQuery(String.format("from %s %s %s %s", DBItemReportTask, getWhereFromToEnd(), filter.getOrderCriteria(), filter.getSortMode()));
        return executeQuery(query);
    }

    public List<DBItemReportTask> getSchedulerHistoryListSchedulersFromTo() throws SOSHibernateException  {
        String q = "from " + DBItemReportTask + " e where e.schedulerId IN (select distinct e.schedulerId from " + DBItemReportTask + " " + getWhereFromTo() + ")";
        Query<DBItemReportTask> query = sosHibernateSession.createQuery(q);
        return executeQuery(query);
    }
    
    public Long getCountSchedulerJobHistoryListFromTo(boolean successful) throws SOSHibernateException {
        if (this.getFilter().getStates() != null) {
            this.getFilter().getStates().clear();
        }
        if (successful) {
            this.getFilter().addState("SUCCESSFUL");
        } else {
            this.getFilter().addState("FAILED");
        }
        String where = getWhereFromToStart();
//        where += (where.isEmpty()) ? " where" : " and";
        //where += (successful) ? " exitCode = 0" : " exitCode != 0";
        Query<Long> query = sosHibernateSession.createQuery("select count(*) from " + DBItemReportTask + " " + where );
        
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

    public List<DBItemReportTask> getHistoryItems() throws SOSHibernateException  {
        int limit = this.getFilter().getLimit();
        Query<DBItemReportTask> query = sosHibernateSession.createQuery(String.format("from %s %s %s %s", DBItemReportTask, getWhere(), filter.getOrderCriteria(), filter.getSortMode()));
        
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

    public DBItemReportTask getHistoryItem() throws SOSHibernateException  {
        this.filter.setLimit(1);
        Query<DBItemReportTask> query = sosHibernateSession.createQuery(String.format("from %s %s %s %s", DBItemReportTask, getWhere(), filter.getOrderCriteria(), filter.getSortMode()));

        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
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
    public void onAfterDeleting(DBItemReportTask h) {
        // Nothing to do
    }

    @Override
    public List<DBItemReportTask> getListOfItemsToDelete() throws SOSHibernateException {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
        int limit = this.getFilter().getLimit();
        Query<DBItemReportTask> query = sosHibernateSession.createQuery(String.format("from %s %s %s %s", DBItemReportTask, getWhereFromTo(), filter.getOrderCriteria(), filter.getSortMode()));
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
            sql.append("select ta from " + DBItemReportTask + " ta, " + DBItemReportExecution.class.getName() + " e, " + DBItemReportTrigger.class
                    .getName() + " tr");
            sql.append(" where ta.id=e.taskId and e.triggerId=tr.id");
            if (filter.getSchedulerId() != null && !filter.getSchedulerId().isEmpty()) {
                sql.append(" and ta.schedulerId=:schedulerId");
            }

            if (o.size() == 1) {
                OrderPath orderPath = o.get(0);
                sql.append(" and tr.parentName = '" + orderPath.getJobChain() + "'");
                if (orderPath.getOrderId() != null && !orderPath.getOrderId().isEmpty()) {
                    sql.append(" and tr.name = '" + orderPath.getOrderId() + "'");
                }
                if (orderPath.getState() != null && !orderPath.getState().isEmpty()) {
                    sql.append(" and e.state = '" + orderPath.getState() + "'");
                }
            } else {
                sql.append(" and ( 1=0");
                for (OrderPath item : o) {
                    sql.append(" or (tr.parentName = '" + item.getJobChain() + "'");
                    if (item.getOrderId() != null && !item.getOrderId().isEmpty()) {
                        sql.append(" and tr.name = '" + item.getOrderId() + "'");
                    }
                    if (item.getState() != null && !item.getState().isEmpty()) {
                        sql.append(" and e.state = '" + item.getState() + "'");
                    }
                    sql.append(")");
                }
                sql.append(" )");
            }
            sql.append(" order by tr.historyId desc, ta.historyId desc");

            Query<DBItemReportTask> query = sosHibernateSession.createQuery(sql.toString());
            int limit = this.getFilter().getLimit();
            if (limit > 0) {
                query.setMaxResults(limit);
            }
            return executeQuery(query);
        } else {
            return null;
        }
    }

}