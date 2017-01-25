package com.sos.jitl.reporting.db;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.layer.SOSHibernateIntervalDBLayer;
import com.sos.jitl.reporting.db.filter.ReportTriggerFilter;
import com.sos.jitl.reporting.db.filter.FilterFolder;

/** @author Uwe Risse */
public class ReportTriggerDBLayer extends SOSHibernateIntervalDBLayer {

    private static final String DBItemReportTrigger = DBItemReportTrigger.class.getName();
    private static final String DBItemReportTriggerResult = DBItemReportTriggerResult.class.getName();

    private ReportTriggerFilter filter = null;
    private static final Logger LOGGER = Logger.getLogger(ReportTriggerDBLayer.class);
    private String lastQuery = "";

    public ReportTriggerDBLayer(String configurationFilename) throws Exception {
        super();
        this.setConfigurationFileName(configurationFilename);
        this.resetFilter();
        this.createStatelessConnection(this.getConfigurationFileName());
    }

    public ReportTriggerDBLayer(File configurationFile) throws Exception {
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

    public ReportTriggerDBLayer(SOSHibernateConnection connection) {
        super();
        this.connection = connection;
        resetFilter();
    }

    private String getStatusClause(String status){
           
        if ("SUCCESSFUL".equals(status)){
            return "(not t.endTime is null and r.error <> 1)";
        }

        if ("INCOMPLETE".equals(status)){
            return "(not t.startTime is null and t.endTime is null)";
        }

        if ("FAILED".equals(status)){
            return "(not t.endTime is null and r.error = 1)";
        }
        return "";
    }
    
    public DBItemReportTrigger get(Long id) throws Exception {
        if (id == null) {
            return null;
        }
     
        return (DBItemReportTrigger) (connection.get(DBItemReportTrigger.class, id));
    }

  

    private String getWhere() {
        String where = "";
        String and = "";
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            where += and + " t.schedulerId=:schedulerId";
            and = " and ";
        }

        if (filter.getListOfReportItems() != null && filter.getListOfReportItems().size() > 0) {
            where += and + "(";
            boolean first = true;
            for (DBItemReportTrigger dbItemReportTrigger : filter.getListOfReportItems()) {
                if(!first) {
                    where += " or ";
                }
                where += " t.parentName = '" + dbItemReportTrigger.getParentName() + "'";
                if (dbItemReportTrigger.getName() != null && !"".equals(dbItemReportTrigger.getName())) {
                    where += " and t.name = '" + dbItemReportTrigger.getName() + "' ";
                }
                first = false;
            }
//            where += " 0=1)";
            where += " )";
            and = " and ";

        } else {
            
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
                for ( FilterFolder filterFolder : filter.getListOfFolders()) {
                    if (filterFolder.isRecursive()){
                        where += " t.parentFolder like '" + filterFolder.getFolder() + "%'";
                    }else{
                        where += " t.parentFolder = '" + filterFolder.getFolder() + "'";
                    }
                    where += " or ";
                }
                where += " 0=1)";
                and = " and ";
            } 
             
        }

        if (filter.getExecutedFrom() != null) {
            where += and + " t.startTime>= :startTimeFrom";
            and = " and ";
        }
        
        if (filter.getExecutedTo() != null) {
            where += and + " t.startTime < :startTimeTo ";
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
        if (!"".equals(where.trim())) {
           where = "where " + where;
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
        
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setText("schedulerId", filter.getSchedulerId());
        }
        return query;
    }

    @SuppressWarnings("unchecked")
    public List<DBItemReportTriggerWithResult> getSchedulerOrderHistoryListFromTo() throws Exception {
        int limit = filter.getLimit();

        Query query = null;
        query = connection.createQuery("select new com.sos.jitl.reporting.db.DBItemReportTriggerWithResult(t,r) from " + DBItemReportTrigger + " t," + DBItemReportTriggerResult + " r  " + getWhere() +  " and t.id = r.triggerId  " + filter.getOrderCriteria() + filter.getSortMode());
                                                   
        query = bindParameters(query);
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return query.list();
    }

    public Long getCountSchedulerOrderHistoryListFromTo() throws Exception {
        Query query = null;
        query = connection.createQuery("Select count(*) from " + DBItemReportTrigger + " t," + DBItemReportTriggerResult + " r " + getWhere() + " and t.id=r.triggerId");
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