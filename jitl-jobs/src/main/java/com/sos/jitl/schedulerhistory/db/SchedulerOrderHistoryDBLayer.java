package com.sos.jitl.schedulerhistory.db;

import java.io.File;
import java.util.List;
import java.util.TimeZone;

import javax.persistence.TemporalType;

import org.apache.log4j.Logger;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.query.Query;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.hibernate.layer.SOSHibernateIntervalDBLayer;
import com.sos.jitl.schedulerhistory.SchedulerOrderHistoryFilter;

public class SchedulerOrderHistoryDBLayer extends SOSHibernateIntervalDBLayer<SchedulerOrderHistoryDBItem> {

    protected SchedulerOrderHistoryFilter filter = null;
    private static final Logger LOGGER = Logger.getLogger(SchedulerOrderHistoryDBLayer.class);
    private String lastQuery = "";

    public SchedulerOrderHistoryDBLayer(File configurationFile_) throws SOSHibernateException {
        super();
        this.setConfigurationFileName(configurationFile_.getAbsolutePath());
        createStatelessConnection(configurationFile_.getAbsolutePath());
        this.resetFilter();
    }

    public SchedulerOrderHistoryDBLayer(SOSHibernateSession session) {
        super();
        this.setConfigurationFileName(session.getFactory().getConfigFile().get().toFile().getAbsolutePath());
        this.sosHibernateSession = session;
        this.resetFilter();
    }

    public SchedulerOrderHistoryDBItem get(Long id) throws SOSHibernateException {
        if (id == null) {
            return null;
        }
        createStatelessConnection(this.getConfigurationFileName());
        try {
            return (SchedulerOrderHistoryDBItem) this.getSession().get(SchedulerOrderHistoryDBItem.class, id);
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
    public void onAfterDeleting(SchedulerOrderHistoryDBItem h) throws SOSHibernateException {
        String q = "delete from SchedulerOrderStepHistoryDBItem where id.historyId=" + h.getHistoryId();
        Query<SchedulerOrderStepHistoryDBItem> query = sosHibernateSession.createQuery(q);
        int row = sosHibernateSession.executeUpdate(query);
        LOGGER.debug(String.format("%s steps deleted", row));
    }

    public int delete() throws SOSHibernateException  {
        String q = "delete from SchedulerOrderStepHistoryDBItem e where e.schedulerOrderHistoryDBItem.historyId IN "
                + "(select historyId from SchedulerOrderHistoryDBItem " + getWhereFromTo() + ")";
        Query<SchedulerOrderStepHistoryDBItem> query = sosHibernateSession.createQuery(q);
        if (filter.getExecutedUtcFrom() != null) {
            query.setParameter("startTimeFrom", filter.getExecutedUtcFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getExecutedUtcTo() != null) {
            query.setParameter("startTimeTo", filter.getExecutedUtcTo(), TemporalType.TIMESTAMP);
        }
        int row = query.executeUpdate();
        String hql = "delete from SchedulerOrderHistoryDBItem " + getWhereFromTo();
        query = sosHibernateSession.createQuery(hql);
        if (filter.getExecutedUtcFrom() != null) {
            query.setParameter("startTimeFrom", filter.getExecutedUtcFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getExecutedUtcTo() != null) {
            query.setParameter("startTimeTo", filter.getExecutedUtcTo(), TemporalType.TIMESTAMP);
        }
        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

//    private List<SchedulerOrderHistoryDBItem> executeQuery(Query query, int limit) throws SOSHibernateException {
//        lastQuery = query.getQueryString();
//        if (filter.getExecutedUtcFrom() != null && !"".equals(filter.getExecutedUtcFrom())) {
//            query.setParameter("startTimeFrom", filter.getExecutedUtcFrom(), TemporalType.TIMESTAMP);
//        }
//        if (filter.getExecutedUtcTo() != null && !"".equals(filter.getExecutedUtcTo())) {
//            query.setParameter("startTimeTo", filter.getExecutedUtcTo(), TemporalType.TIMESTAMP);
//        }
//        if (filter.getOrderid() != null && !"".equals(filter.getOrderid())) {
//            query.setParameter("orderId", filter.getOrderid());
//        }
//        if (filter.getJobchain() != null && !"".equals(filter.getJobchain())) {
//            query.setParameter("jobChain", filter.getJobchain());
//        }
//        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
//            query.setParameter("schedulerId", filter.getSchedulerId());
//        }
//        if (limit > 0) {
//            query.setMaxResults(limit);
//        }
//        return sosHibernateSession.getResultList(query);
//    }
  
    
 
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
    public List<SchedulerOrderHistoryDBItem> getListOfItemsToDelete() throws SOSHibernateException{
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
        int limit = this.getFilter().getLimit();
        Query<SchedulerOrderHistoryDBItem> query = sosHibernateSession.createQuery("from SchedulerOrderHistoryDBItem " + getWhereFromTo() + filter.getOrderCriteria() + filter.getSortMode());
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
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

    @Override
    public long deleteInterval() throws SOSHibernateException  {
        return delete();
    }

}