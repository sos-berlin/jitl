package com.sos.jitl.dailyplan.db;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.layer.SOSHibernateIntervalDBLayer;
import com.sos.jitl.dailyplan.filter.ReportTriggerFilter;
import com.sos.jitl.reporting.db.DBItemReportTrigger;
 
/** @author Uwe Risse */
public class DailyPlanTriggerDBLayer extends SOSHibernateIntervalDBLayer {

    protected ReportTriggerFilter filter = null;
    private static final Logger LOGGER = Logger.getLogger(DailyPlanTriggerDBLayer.class);
    private String lastQuery = "";

    public DailyPlanTriggerDBLayer(String configurationFilename) {
        super();
        this.setConfigurationFileName(configurationFilename);
        this.resetFilter();
        this.initConnection(this.getConfigurationFileName());
    }

    public DailyPlanTriggerDBLayer(File configurationFile) {
        super();
        try {
            this.setConfigurationFileName(configurationFile.getCanonicalPath());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            this.setConfigurationFileName("");
        }
        this.resetFilter();
        this.initConnection(this.getConfigurationFileName());
    }
    
    public DailyPlanTriggerDBLayer(SOSHibernateConnection connection) {
        super();
        this.initConnection(connection);
        resetFilter();
    }  
    public DBItemReportTrigger get(Long id) throws Exception {
        if (id == null) {
            return null;
        }
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        connection.beginTransaction();
        return (DBItemReportTrigger) ((Session) connection.getCurrentSession()).get(DBItemReportTrigger.class, id);
    }

    protected String getWhere() {
        String where = "";
        String and = "";
        if (filter.getReportTriggerId() != null) {
            where += and + " reportTriggerId=:reportTriggerId";
            and = " and ";
        } else {
            if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
                where += and + " schedulerId=:schedulerId";
                and = " and ";
            }
            if (filter.getJobchain() != null && !"".equals(filter.getJobchain())) {
                where += and + " parentName=:jobChain";
                and = " and ";
            }
            if (filter.getOrderid() != null && !"".equals(filter.getOrderid())) {
                where += and + " name=:orderId";
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
        }
        return where;
    }

    protected String getWhereFromTo() {
        String where = "";
        String and = "";
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            where += and + " schedulerId=:schedulerId";
            and = " and ";
        }
        if (filter.getJobchain() != null && !"".equals(filter.getJobchain())) {
            if (filter.getJobchain().contains("%")) {
                where += and + " parentName like :jobChain";
            } else {
                where += and + " parentName=:jobChain";
            }
            and = " and ";
        }
        if (filter.getOrderid() != null && !"".equals(filter.getOrderid())) {
            if (filter.getOrderid().contains("%")) {
                where += and + " name like :orderId";
            } else {
                where += and + " name=:orderId";
            }
            and = " and ";
        }
        if (!filter.isShowJobChains()) {
            where += and + " 1=0";
            and = " and ";
        }
        if (filter.getExecutedUtcFrom() != null) {
            where += and + " startTime>= :startTimeFrom";
            and = " and ";
        }
        if (filter.getExecutedUtcTo() != null) {
            where += and + " startTime <= :startTimeTo ";
            and = " and ";
        }
        if (!"".equals(where.trim())) {
            where = "where " + where;
        }
        return where;
    }

  
  
 
    @SuppressWarnings("unchecked")
    private List<DBItemReportTrigger> executeQuery(Query query, int limit) {
        lastQuery = query.getQueryString();
        if (filter.getExecutedUtcFrom() != null && !"".equals(filter.getExecutedUtcFrom())) {
            query.setTimestamp("startTimeFrom", filter.getExecutedUtcFrom());
        }
        if (filter.getExecutedUtcTo() != null && !"".equals(filter.getExecutedUtcTo())) {
            query.setTimestamp("startTimeTo", filter.getExecutedUtcTo());
        }
        if (filter.getOrderid() != null && !"".equals(filter.getOrderid())) {
            query.setText("orderId", filter.getOrderid());
        }
        if (filter.getJobchain() != null && !"".equals(filter.getJobchain())) {
            query.setText("jobChain", filter.getJobchain());
        }
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setText("schedulerId", filter.getSchedulerId());
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return query.list();
    }

    public List<DBItemReportTrigger> getSchedulerOrderHistoryListFromTo() throws Exception {
        int limit = filter.getLimit();
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        Query query = null;
        connection.beginTransaction();
        query = connection.createQuery("from DBItemReportTrigger " + getWhereFromTo() + filter.getOrderCriteria() + filter.getSortMode());
        return executeQuery(query, limit);
    }

    public List<DBItemReportTrigger> getUnassignedSchedulerOrderHistoryListFromTo() throws Exception {
        int limit = filter.getLimit();
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        Query query = null;
        connection.beginTransaction();
        query = connection.createQuery("from DBItemReportTrigger " + getWhereFromTo() + " and id not in (select "
                + "reportTriggerId from DailyPlanDBItem where not reportTriggerId is null and isAssigned=1"
                + " and schedulerId=:schedulerId) " + filter.getOrderCriteria() + filter.getSortMode());
        return executeQuery(query, limit);
    }

    @SuppressWarnings("unchecked")
    public List<DBItemReportTrigger> getOrderHistoryItems() throws Exception {
        List<DBItemReportTrigger> historyList = null;
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        connection.beginTransaction();
        Query query = connection.createQuery("from DBItemReportTrigger " + getWhere());
        if (filter.getStartTime() != null && !"".equals(filter.getStartTime())) {
            query.setTimestamp("startTime", filter.getStartTime());
        }
        if (filter.getEndTime() != null && !"".equals(filter.getEndTime())) {
            query.setTimestamp("endTime", filter.getEndTime());
        }
        if (filter.getOrderid() != null && !"".equals(filter.getOrderid())) {
            query.setText("orderId", filter.getOrderid());
        }
        if (filter.getJobchain() != null && !"".equals(filter.getJobchain())) {
            query.setText("jobChain", filter.getJobchain());
        }
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setText("schedulerId", filter.getSchedulerId());
        }
        if (filter.getLimit() > 0) {
            query.setMaxResults(filter.getLimit());
        }
        historyList = query.list();
        return historyList;
    }

    public List<DBItemReportTrigger> getSchedulerOrderHistoryListSchedulersFromTo() throws Exception {
        int limit = filter.getLimit();
        Query query = null;
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        connection.beginTransaction();
        String q = "from DBItemReportTrigger e where e.schedulerId IN (select distinct e.schedulerId from DBItemReportTrigger "
                + getWhereFromTo() + ")";
        query = connection.createQuery(q);
        return executeQuery(query, limit);
    }

    @SuppressWarnings("unchecked")
    public DBItemReportTrigger getOrderHistoryItem() throws Exception {
        List<DBItemReportTrigger> historyList = null;
        this.filter.setLimit(1);
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        connection.beginTransaction();
        Query query = connection.createQuery("from DBItemReportTrigger " + getWhere());
        if (filter.getReportTriggerId() != null) {
            query.setLong("reportTriggerId", filter.getReportTriggerId());
        } else {
            if (filter.getStartTime() != null && !"".equals(filter.getStartTime())) {
                query.setTimestamp("startTime", filter.getStartTime());
            }
            if (filter.getEndTime() != null && !"".equals(filter.getEndTime())) {
                query.setTimestamp("endTime", filter.getEndTime());
            }
            if (filter.getOrderid() != null && !"".equals(filter.getOrderid())) {
                query.setText("orderId", filter.getOrderid());
            }
            if (filter.getJobchain() != null && !"".equals(filter.getJobchain())) {
                query.setText("jobChain", filter.getJobchain());
            }
            if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
                query.setText("schedulerId", filter.getSchedulerId());
            }
        }
        query.setMaxResults(filter.getLimit());
        historyList = query.list();
        if (historyList != null && !historyList.isEmpty()) {
            return historyList.get(0);
        } else {
            return null;
        }
    }

    public ReportTriggerFilter getFilter() {
        return filter;
    }

    public void resetFilter() {
        this.filter = new ReportTriggerFilter();
        this.filter.setDateFormat("yyyy-MM-dd HH:mm:ss");
        this.filter.setOrderCriteria("startTime");
        this.filter.setSortMode("desc");
    }

    public String getLastQuery() {
        return lastQuery;
    }

    @Override
    public void onAfterDeleting(DbItem h) throws Exception {
       
    }

    @Override
    public List<DbItem> getListOfItemsToDelete() throws Exception {
        return null;
    }

    @Override
    public long deleteInterval() throws Exception {
        return 0;
    }

   
}