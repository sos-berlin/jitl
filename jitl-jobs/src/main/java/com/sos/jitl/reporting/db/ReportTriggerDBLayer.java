package com.sos.jitl.reporting.db;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.layer.SOSHibernateIntervalDBLayer;
import com.sos.jitl.reporting.db.filter.ReportTriggerFilter;

/** @author Uwe Risse */
public class ReportTriggerDBLayer extends SOSHibernateIntervalDBLayer {

    protected ReportTriggerFilter filter = null;
    private static final Logger LOGGER = Logger.getLogger(ReportTriggerDBLayer.class);
    private String lastQuery = "";

    public ReportTriggerDBLayer(String configurationFilename) {
        super();
        this.setConfigurationFileName(configurationFilename);
        this.resetFilter();
        this.initConnection(this.getConfigurationFileName());
    }

    public ReportTriggerDBLayer(File configurationFile) {
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

    public ReportTriggerDBLayer(SOSHibernateConnection connection) {
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
        return (DBItemReportTrigger) ((Session) connection.getCurrentSession()).get(DBItemReportTrigger.class, id);
    }

  

    protected String getWhereFromTo() {
        String where = "";
        String and = "";
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            where += and + " t.schedulerId=:schedulerId";
            and = " and ";
        }

        if (filter.getListOfReportItems() != null && filter.getListOfReportItems().size() > 0) {
            where += and + "(";
            for (DBItemReportTrigger dbItemReportTrigger : filter.getListOfReportItems()) {
                where += " t.parentName = '" + dbItemReportTrigger.getParentName() + "'";
                if (!"".equals(dbItemReportTrigger.getName())) {
                    where += " and NAME = '" + dbItemReportTrigger.getName() + "' ";
                }
                where += " or ";
            }
            where += " 0=1)";
            and = " and ";

        } else {
            if (filter.getJobchain() != null && !"".equals(filter.getJobchain())) {
                if (filter.getJobchain().contains("%")) {
                    where += and + " t.parentName like :jobChain";
                } else {
                    where += and + " t.parentName=:jobChain";
                }
                and = " and ";
            }
            if (filter.getOrderid() != null && !"".equals(filter.getOrderid())) {
                if (filter.getOrderid().contains("%")) {
                    where += and + " t.name like :orderId";
                } else {
                    where += and + " t.name=:orderId";
                }
                and = " and ";
            }
        }

        if (filter.getExecutedFrom() != null) {
            where += and + " t.startTime>= :startTimeFrom";
            and = " and ";
        }
        if (filter.getFailed() != null) {
            if (filter.getFailed()) {
                where += and + " r.error = 1";
                and = " and ";
            } else {
                where += and + " r.error = 0";
                and = " and ";
            }
        }
        if (filter.getSuccess() != null) {
            if (filter.getSuccess()) {
                where += and + " r.error = 0";
                and = " and ";
            } else {
                where += and + " r.error = 1";
                and = " and ";
            }
        }
        if (filter.getExecutedTo() != null) {
            where += and + " t.startTime <= :startTimeTo ";
            and = " and ";
        }
        if (!"".equals(where.trim())) {
           where = "where " + where;
        }
        return where;
    }

    @SuppressWarnings("unchecked")
    private Query bindParameters(Query query) {
        lastQuery = query.getQueryString();
        if (filter.getExecutedFrom() != null && !"".equals(filter.getExecutedFrom())) {
            query.setTimestamp("startTimeFrom", filter.getExecutedFrom());
        }
        if (filter.getExecutedTo() != null && !"".equals(filter.getExecutedTo())) {
            query.setTimestamp("startTimeTo", filter.getExecutedTo());
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
        return query;
    }

    @SuppressWarnings("unchecked")
    public List<DBItemReportTriggerWithResult> getSchedulerOrderHistoryListFromTo() throws Exception {
        int limit = filter.getLimit();
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        Query query = null;
        query = connection.createQuery("select new com.sos.jitl.reporting.db.DBItemReportTriggerWithResult(t,r) from DBItemReportTrigger t,DBItemReportTriggerResult r  " + getWhereFromTo() +  " and t.id = r.triggerId  " + filter.getOrderCriteria() + filter.getSortMode());
                                                   
        query = bindParameters(query);
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return query.list();
    }

    public Long getCountSchedulerOrderHistoryListFromTo() throws Exception {
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        Query query = null;
        query = connection.createQuery("Select count(*) from DBItemReportTrigger t,DBItemReportTriggerResult r " + getWhereFromTo());
        query = bindParameters(query);
        Long count;
        if (query.list().size() > 0)
            count = (long) query.list().get(0);
        else
            count = 0L;
        return count;
    }

     public ReportTriggerFilter getFilter() {
        return filter;
    }

    public void resetFilter() {
        this.filter = new ReportTriggerFilter();
        this.filter.setDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        this.filter.setOrderCriteria("t.startTime");
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