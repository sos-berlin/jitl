package com.sos.jitl.schedulerhistory.db;

import java.io.File;
import java.util.List;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.layer.SOSHibernateIntervalDBLayer;
import com.sos.scheduler.history.SchedulerOrderHistoryFilter;

public class SchedulerOrderHistoryDBLayer extends SOSHibernateIntervalDBLayer {

    protected SchedulerOrderHistoryFilter filter = null;
    private static final Logger LOGGER = Logger.getLogger(SchedulerOrderHistoryDBLayer.class);
    private String lastQuery = "";

    public SchedulerOrderHistoryDBLayer(File configurationFile_) {
        super();
        this.setConfigurationFileName(configurationFile_.getAbsolutePath());
        this.resetFilter();
    }

    public SchedulerOrderHistoryDBItem get(Long id) throws Exception {
        if (id == null) {
            return null;
        }
        initConnection();
        try {
            return (SchedulerOrderHistoryDBItem) this.getConnection().get(SchedulerOrderHistoryDBItem.class, id);
        } catch (ObjectNotFoundException e) {
            return null;
        }
    }

    protected String getWhere() {
        String where = "";
        String and = "";
        if (filter.getSchedulerOrderHistoryId() != null) {
            where += and + " schedulerOrderHistoryId=:schedulerOrderHistoryId";
            and = " and ";
        } else {
            if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
                where += and + " spoolerId=:schedulerId";
                and = " and ";
            }
            if (filter.getJobchain() != null && !"".equals(filter.getJobchain())) {
                where += and + " jobChain=:jobChain";
                and = " and ";
            }
            if (filter.getOrderid() != null && !"".equals(filter.getOrderid())) {
                where += and + " orderId=:orderId";
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
            where += and + " spoolerId=:schedulerId";
            and = " and ";
        }
        if (filter.getJobchain() != null && !"".equals(filter.getJobchain())) {
            if (filter.getJobchain().contains("%")) {
                where += and + " jobChain like :jobChain";
            } else {
                where += and + " jobChain=:jobChain";
            }
            and = " and ";
        }
        if (filter.getOrderid() != null && !"".equals(filter.getOrderid())) {
            if (filter.getOrderid().contains("%")) {
                where += and + " orderId like :orderId";
            } else {
                where += and + " orderId=:orderId";
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

    protected String getWhereFromToStep() {
        String where = "";
        String and = "";
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

    @Override
    public void onAfterDeleting(DbItem h) throws Exception {
        SchedulerOrderHistoryDBItem x = (SchedulerOrderHistoryDBItem) h;
        String q = "delete from SchedulerOrderStepHistoryDBItem where id.historyId=" + x.getHistoryId();
        Query query = connection.createQuery(q);
        int row = query.executeUpdate();
        LOGGER.debug(String.format("%s steps deleted", row));
    }

    public int delete() throws Exception {
        this.connection.beginTransaction();
        String q = "delete from SchedulerOrderStepHistoryDBItem e where e.schedulerOrderHistoryDBItem.historyId IN "
                + "(select historyId from SchedulerOrderHistoryDBItem " + getWhereFromTo() + ")";
        Query query = connection.createQuery(q);
        if (filter.getExecutedUtcFrom() != null) {
            query.setTimestamp("startTimeFrom", filter.getExecutedUtcFrom());
        }
        if (filter.getExecutedUtcTo() != null) {
            query.setTimestamp("startTimeTo", filter.getExecutedUtcTo());
        }
        int row = query.executeUpdate();
        String hql = "delete from SchedulerOrderHistoryDBItem " + getWhereFromTo();
        query = connection.createQuery(hql);
        if (filter.getExecutedUtcFrom() != null) {
            query.setTimestamp("startTimeFrom", filter.getExecutedUtcFrom());
        }
        if (filter.getExecutedUtcTo() != null) {
            query.setTimestamp("startTimeTo", filter.getExecutedUtcTo());
        }
        row = query.executeUpdate();
        return row;
    }

    private List<SchedulerOrderHistoryDBItem> executeQuery(Query query, int limit) {
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

    public List<SchedulerOrderHistoryDBItem> getSchedulerOrderHistoryListFromTo() throws Exception {
        int limit = filter.getLimit();
        initConnection(getConfigurationFileName());
        Query query = connection.createQuery("from SchedulerOrderHistoryDBItem " + getWhereFromTo() + filter.getOrderCriteria() + filter.getSortMode());
        return executeQuery(query, limit);
    }

    public List<SchedulerOrderHistoryDBItem> getUnassignedSchedulerOrderHistoryListFromTo() throws Exception {
        int limit = filter.getLimit();
        initConnection(getConfigurationFileName());
        Query query = connection.createQuery("from SchedulerOrderHistoryDBItem " + getWhereFromTo()
                + " and historyId not in (select schedulerOrderHistoryId from DailyScheduleDBItem where not schedulerOrderHistoryId is null "
                + "and status=1 and schedulerId=:schedulerId) " + filter.getOrderCriteria() + filter.getSortMode());
        return executeQuery(query, limit);
    }

    public List<SchedulerOrderHistoryDBItem> getOrderHistoryItems() throws Exception {
        initConnection(getConfigurationFileName());
        Query query = connection.createQuery("from SchedulerOrderHistoryDBItem " + getWhere());
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
        query.setMaxResults(filter.getLimit());
        return query.list();
    }

    public List<SchedulerOrderHistoryDBItem> getSchedulerOrderHistoryListSchedulersFromTo() throws Exception {
        int limit = filter.getLimit();
        initConnection(getConfigurationFileName());
        String q = "from SchedulerOrderHistoryDBItem e where e.spoolerId IN (select distinct e.spoolerId from SchedulerOrderHistoryDBItem "
                + getWhereFromTo() + ")";
        Query query = connection.createQuery(q);
        return executeQuery(query, limit);
    }

    public SchedulerOrderHistoryDBItem getOrderHistoryItem() throws Exception {
        initConnection(getConfigurationFileName());
        this.filter.setLimit(1);
        Query query = connection.createQuery("from SchedulerOrderHistoryDBItem " + getWhere());
        if (filter.getSchedulerOrderHistoryId() != null) {
            query.setLong("schedulerOrderHistoryId", filter.getSchedulerOrderHistoryId());
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
        List<SchedulerOrderHistoryDBItem> historyList = query.list();
        if (!historyList.isEmpty()) {
            return historyList.get(0);
        } else {
            return null;
        }
    }

    public SchedulerOrderHistoryFilter getFilter() {
        return filter;
    }

    public void resetFilter() {
        this.filter = new SchedulerOrderHistoryFilter();
        this.filter.setDateFormat("yyyy-MM-dd HH:mm:ss");
        this.filter.setOrderCriteria("startTime");
        this.filter.setSortMode("desc");
    }

    public String getLastQuery() {
        return lastQuery;
    }

    @Override
    public List<DbItem> getListOfItemsToDelete() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
        int limit = this.getFilter().getLimit();
        initConnection(getConfigurationFileName());
        Query query = connection.createQuery("from SchedulerOrderHistoryDBItem " + getWhereFromTo() + filter.getOrderCriteria() + filter.getSortMode());
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setText("schedulerId", filter.getSchedulerId());
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

    @Override
    public long deleteInterval() throws Exception {
        return delete();
    }

}