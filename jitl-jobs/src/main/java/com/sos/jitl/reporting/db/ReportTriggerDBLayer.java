package com.sos.jitl.reporting.db;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.query.Query;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.hibernate.layer.SOSHibernateIntervalDBLayer;
import com.sos.jitl.reporting.db.filter.ReportTriggerFilter;
import com.sos.jitl.reporting.db.filter.FilterFolder;

/** @author Uwe Risse */
public class ReportTriggerDBLayer extends SOSHibernateIntervalDBLayer {

    private static final String DBItemReportTrigger = DBItemReportTrigger.class.getName();

    private ReportTriggerFilter filter = null;
    private static final Logger LOGGER = Logger.getLogger(ReportTriggerDBLayer.class);
    private String lastQuery = "";

    public ReportTriggerDBLayer(String configurationFilename) throws SOSHibernateException {
        super();
        this.setConfigurationFileName(configurationFilename);
        this.resetFilter();
        this.createStatelessConnection(this.getConfigurationFileName());
    }

    public ReportTriggerDBLayer(File configurationFile) throws SOSHibernateException{
        super();
        try {
            this.setConfigurationFileName(configurationFile.getCanonicalPath());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            this.setConfigurationFileName("");
        }
        this.resetFilter();
        this.createStatelessConnection(this.getConfigurationFileName());
    }

    public ReportTriggerDBLayer(SOSHibernateSession conn) {
        super();
        sosHibernateSession = conn;
        resetFilter();
    }

    private String getStatusClause(String status) {

        if ("SUCCESSFUL".equals(status)) {
            return "(not endTime is null and resultError <> 1)";
        }

        if ("INCOMPLETE".equals(status)) {
            return "(not startTime is null and endTime is null)";
        }

        if ("FAILED".equals(status)) {
            return "(resultError = 1)";
        }
        return "";
    }

    public DBItemReportTrigger get(Long id) throws SOSHibernateException   {
        if (id == null) {
            return null;
        }

        return (DBItemReportTrigger) (sosHibernateSession.get(DBItemReportTrigger.class, id));
    }

    private String getWhere() {
        String where = "";
        String and = "";
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            where += and + " schedulerId=:schedulerId";
            and = " and ";
        }

        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            where += and +  " name = :orderId";
            and = " and ";
        }
        if (filter.getJobChain() != null && !"".equals(filter.getJobChain())) {
            where += and +  " parentName = :jobchain";
            and = " and ";
        }

        
        if (filter.getListOfReportItems() != null && filter.getListOfReportItems().size() > 0) {
            where += and + "(";
            String or = "";
            for (DBItemReportTrigger dbItemReportTrigger : filter.getListOfReportItems()) {
                if (dbItemReportTrigger.getHistoryId() != null) {
                    where += or + "historyId = " + dbItemReportTrigger.getHistoryId().toString() + " ";
                } else {
                    where += or + "parentName = '" + dbItemReportTrigger.getParentName() + "' ";
                    if (dbItemReportTrigger.getName() != null && !dbItemReportTrigger.getName().isEmpty()) {
                        where += "and name = '" + dbItemReportTrigger.getName() + "' ";
                    }
                }
                or = "or ";
            }
            where += ")";
            and = " and ";

        } else {
            if (filter.getListOfIgnoredItems() != null && filter.getListOfIgnoredItems().size() > 0) {
                where += and + "(";
                for (DBItemReportTrigger dbItemReportTrigger : filter.getListOfIgnoredItems()) {

                    if (dbItemReportTrigger.getName() != null && !dbItemReportTrigger.getName().isEmpty()) {
                        where += " concat(concat(parentName,','),name) <> '" + String.format("%s,%s", dbItemReportTrigger.getParentName(), dbItemReportTrigger.getName())
                                + "' ";
                    } else {
                        where += " parentName <> '" + dbItemReportTrigger.getParentName() + "'";
                    }
                    where += " and ";
                }
                where += " 1=1)";
                and = " and ";
            }

            if (filter.getStates() != null && filter.getStates().size() > 0) {
                where += and + "(";
                for (String state : filter.getStates()) {
                    where += getStatusClause(state) + " or ";
                }
                where += " 1=0)";
                and = " and ";
            }

            if (filter.getListOfFolders() != null && filter.getListOfFolders().size() > 0) {
                where += and + "(";
                for (FilterFolder filterFolder : filter.getListOfFolders()) {
                    if (filterFolder.isRecursive()) {
                        where += " parentFolder like '" + filterFolder.getFolder() + "%'";
                    } else {
                        where += " parentFolder = '" + filterFolder.getFolder() + "'";
                    }
                    where += " or ";
                }
                where += " 0=1)";
                and = " and ";
            }

        }

        if (filter.getExecutedFrom() != null) {
            where += and + " startTime>= :startTimeFrom";
            and = " and ";
        }

        if (filter.getExecutedTo() != null) {
            where += and + " startTime < :startTimeTo ";
            and = " and ";
        }

        if (filter.getFailed() != null) {
            if (filter.getFailed()) {
                where += and + " resultError = 1";
                and = " and ";
            } else {
                where += and + " resultError = 0";
                and = " and ";
            }
        }

        if (filter.getSuccess() != null) {
            if (filter.getSuccess()) {
                where += and + " resultError = 0";
                and = " and ";
            } else {
                where += and + " resultError = 1";
                and = " and ";
            }
        }
        if (!"".equals(where.trim())) {
            where = " where " + where;
        }else{
            where = " ";
        }
        
        return where;
    }

    private Query bindParameters(Query query) {
        lastQuery = query.getQueryString();
        if (filter.getExecutedFrom() != null && !"".equals(filter.getExecutedFrom())) {
            query.setTimestamp("startTimeFrom", filter.getExecutedFrom());
        }

        if (filter.getExecutedTo() != null && !"".equals(filter.getExecutedTo())) {
            query.setTimestamp("startTimeTo", filter.getExecutedTo());
        }
        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            query.setParameter("orderId", filter.getOrderId());
        }
        if (filter.getJobChain() != null && !"".equals(filter.getJobChain())) {
            query.setParameter("jobchain", filter.getJobChain());
        }

        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        return query;
    }

    @SuppressWarnings("unchecked")
    public List<DBItemReportTrigger> getSchedulerOrderHistoryListFromTo() throws SOSHibernateException  {
        int limit = filter.getLimit();

        Query<DBItemReportTrigger> query = null;
        query = sosHibernateSession.createQuery(" from " + DBItemReportTrigger + getWhere() +  filter.getOrderCriteria() + filter.getSortMode());

        query = bindParameters(query);

        query.setMaxResults(limit);
        return sosHibernateSession.getResultList(query);
    }

    public Long getCountSchedulerOrderHistoryListFromTo() throws SOSHibernateException {
        Query<Long> query = null;
        query = sosHibernateSession.createQuery("Select count(*) from " + DBItemReportTrigger + getWhere() );
        query = bindParameters(query);
        Long count;
        if (sosHibernateSession.getResultList(query).size() > 0)
            count = sosHibernateSession.getResultList(query).get(0);
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
        this.filter.setOrderCriteria("startTime");
        this.filter.setSortMode("desc");
    }

    public String getLastQuery() {
        return lastQuery;
    }

    @Override
    public void onAfterDeleting(DbItem h) throws SOSHibernateException{

    }

    @Override
    public List<DbItem> getListOfItemsToDelete() throws SOSHibernateException{
        return null;
    }

    @Override
    public long deleteInterval() throws SOSHibernateException{
        return 0;
    }

}