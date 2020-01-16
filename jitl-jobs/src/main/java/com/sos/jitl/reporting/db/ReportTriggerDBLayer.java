package com.sos.jitl.reporting.db;

import java.util.List;

import javax.persistence.TemporalType;

import org.apache.log4j.Logger;
import org.hibernate.query.Query;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.SearchStringHelper;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.hibernate.layer.SOSHibernateIntervalDBLayer;
import com.sos.jitl.reporting.db.filter.ReportTriggerFilter;
import com.sos.joc.model.common.Folder;

public class ReportTriggerDBLayer extends SOSHibernateIntervalDBLayer<DBItemReportTrigger> {

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

  
    public ReportTriggerDBLayer(SOSHibernateSession conn) {
        super();
        sosHibernateSession = conn;
        resetFilter();
    }

    private String getStatusClause(String status) {

        if ("SUCCESSFUL".equals(status)) {
            return "(endTime != null and resultError <> 1)";
        }

        if ("INCOMPLETE".equals(status)) {
            return "(startTime != null and endTime is null)";
        }

        if ("FAILED".equals(status)) {
            return "(endTime != null and resultError = 1)";
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
        
    	if (filter.getListOfJobchains() != null && filter.getListOfJobchains().size() > 0) {
			where += and + SearchStringHelper.getStringListPathSql(filter.getListOfJobchains(), "parentName");
			and = " and ";
		}
    	
        if (filter.getJobChain() != null && !"".equals(filter.getJobChain())) {
            where += String.format(and + " parentName %s :jobChain", SearchStringHelper.getSearchPathOperator(filter.getJobChain()));
            and = " and ";
        }

        if (filter.getStates() != null && filter.getStates().size() > 0) {
            where += and;
            if (filter.getStates().size() == 1) {
                where += getStatusClause(filter.getStates().get(0));
            } else {
                where += "(";
                for (String state : filter.getStates()) {
                    where += getStatusClause(state) + " or ";
                }
                where += " 1=0)";
            }
            and = " and ";
        }
        
        if (filter.getHistoryIds() != null && !filter.getHistoryIds().isEmpty()) {
            where += and +  " historyId in (:historyIds)";
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

            if (filter.getListOfFolders() != null && filter.getListOfFolders().size() > 0) {
                where += and + "(";
                for (Folder filterFolder : filter.getListOfFolders()) {
                    if (filterFolder.getRecursive()) {
                        String likeFolder = (filterFolder.getFolder() + "/%").replaceAll("//+", "/");
                        where += " (parentFolder = '" + filterFolder.getFolder() + "' or parentFolder like '" + likeFolder + "')";
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

    private <T> Query<T> bindParameters(Query<T> query) {
        lastQuery = query.getQueryString();
        if (filter.getExecutedFrom() != null) {
            query.setParameter("startTimeFrom", filter.getExecutedFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getExecutedTo() != null) {
            query.setParameter("startTimeTo", filter.getExecutedTo(), TemporalType.TIMESTAMP);
        }
        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            query.setParameter("orderId", filter.getOrderId());
        }
        if (filter.getJobChain() != null && !"".equals(filter.getJobChain())) {
            query.setParameter("jobChain", SearchStringHelper.getSearchPathValue(filter.getJobChain()));
        }
        if (filter.getHistoryIds() != null && !filter.getHistoryIds().isEmpty()) {
            query.setParameterList("historyIds", filter.getHistoryIds());
        }
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        return query;
    }

    public List<DBItemReportTrigger> getSchedulerOrderHistoryListFromTo() throws SOSHibernateException  {
        int limit = filter.getLimit();

        Query<DBItemReportTrigger> query = sosHibernateSession.createQuery(" from " + DBItemReportTrigger + getWhere() +  filter.getOrderCriteria() + filter.getSortMode());

        query = bindParameters(query);

        query.setMaxResults(limit);
        return sosHibernateSession.getResultList(query);
    }

    public Long getCountSchedulerOrderHistoryListFromTo() throws SOSHibernateException {
        Query<Long> query = sosHibernateSession.createQuery("select count(*) from " + DBItemReportTrigger + getWhere() );
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
    public void onAfterDeleting(DBItemReportTrigger h) throws SOSHibernateException{

    }

    @Override
    public List<DBItemReportTrigger> getListOfItemsToDelete() throws SOSHibernateException{
        return null;
    }

    @Override
    public long deleteInterval() throws SOSHibernateException{
        return 0;
    }

}