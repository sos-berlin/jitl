package com.sos.jitl.dailyplan.db;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.persistence.TemporalType;

import org.hibernate.query.Query;
import org.joda.time.DateTime;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.SearchStringHelper;
import com.sos.hibernate.classes.UtcTimeHelper;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.hibernate.layer.SOSHibernateIntervalDBLayer;
import com.sos.jitl.dailyplan.filter.DailyPlanFilter;
import com.sos.jitl.reporting.db.DBItemReportTask;
import com.sos.jitl.reporting.db.DBItemReportTrigger;
import com.sos.jitl.reporting.db.ReportTaskExecutionsDBLayer;
import com.sos.jitl.reporting.db.ReportTriggerDBLayer;
import com.sos.joc.model.common.Folder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DailyPlanDBLayer extends SOSHibernateIntervalDBLayer<DailyPlanDBItem> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Calendar2DB.class);
    private static final String DailyPlanDBItem = DailyPlanDBItem.class.getName();
    private static final String DBItemReportTask = DBItemReportTask.class.getName();
    private static final String DBItemReportTrigger = DBItemReportTrigger.class.getName();

    @SuppressWarnings("unused")
    private String whereFromIso = null;
    @SuppressWarnings("unused")
    private String whereToIso = null;
    private DailyPlanFilter filter = null;

    public DailyPlanDBLayer(SOSHibernateSession session) throws Exception {
        super();
        this.setConfigurationFileName(session.getFactory().getConfigFile().get().toFile().getAbsolutePath());
        this.sosHibernateSession = session;
        resetFilter();
    }

    public DailyPlanDBLayer(final File configurationFile) throws Exception {
        super();
        this.setConfigurationFileName(configurationFile.getAbsolutePath());
        createStatelessConnection(configurationFile.getCanonicalPath());
        resetFilter();
    }

    public DailyPlanDBItem getPlanDbItem(final Long id) throws Exception {
        return (DailyPlanDBItem) sosHibernateSession.get(DailyPlanDBItem.class, id);
    }

    public void resetFilter() {
        filter = new DailyPlanFilter();
        filter.setSchedulerId("");
        filter.setJob("");
        filter.setJobChain("");
        filter.setOrderId("");
    }

    public int delete() throws SOSHibernateException {
        String whereClause = getWhere();
        if (whereClause.isEmpty()) {
            whereClause += " where auditLogId is null";
        } else {
            whereClause += " and auditLogId is null";
        }
        String hql = "delete from " + DailyPlanDBItem + " p " + whereClause;
        int row = 0;
        Query<DailyPlanDBItem> query = sosHibernateSession.createQuery(hql);
        if (filter.getPlannedStart() != null) {
            query.setParameter("plannedStart", filter.getPlannedStart(), TemporalType.TIMESTAMP);
        } else {
            if (filter.getPlannedStartFrom() != null) {
                query.setParameter("plannedStartFrom", filter.getPlannedStartFrom(), TemporalType.TIMESTAMP);
            }
            if (filter.getPlannedStartTo() != null) {
                query.setParameter("plannedStartTo", filter.getPlannedStartTo(), TemporalType.TIMESTAMP);
            }
        }
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            query.setParameter("orderId", filter.getOrderId());
        }
        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            query.setParameter("job", filter.getJob());
        }
        if (filter.getJobChain() != null && !"".equals(filter.getJobChain())) {
            query.setParameter("jobChain", filter.getJobChain());
        }
        row = sosHibernateSession.executeUpdate(query);            
        return row;
    }

    public long deleteInterval() throws SOSHibernateException {
        String whereClause = getWhere();
        if (whereClause.isEmpty()) {
            whereClause += " where auditLogId is null";
        } else {
            whereClause += " and auditLogId is null";
        }
        String hql = "delete from " + DailyPlanDBItem + " p " + whereClause;
        int row = 0;
        Query<DailyPlanDBItem> query = sosHibernateSession.createQuery(hql);
        if (filter.getPlannedStartFrom() != null) {
            query.setParameter("plannedStartFrom", filter.getPlannedStartFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getPlannedStartTo() != null) {
            query.setParameter("plannedStartTo", filter.getPlannedStartTo(), TemporalType.TIMESTAMP);
        }
        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public String getWhere() {
        return getWhere("");
    }

    private String getWhere(String pathField) {
        String where = "";
        String and = "";
        if (filter.getPlannedStart() != null ) {
            where += and + " p.plannedStart = :plannedStart";
            and = " and ";
        } else {
            if (filter.getPlannedStartFrom() != null) {
                where += and + " p.plannedStart>= :plannedStartFrom";
                and = " and ";
            }
            if (filter.getPlannedStartTo() != null) {
                where += and + " p.plannedStart < :plannedStartTo ";
                and = " and ";
            }
        }
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            where += and + " p.schedulerId = :schedulerId";
            and = " and ";
        }
        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            where += String.format(and + " p.job %s :job", SearchStringHelper.getSearchPathOperator(filter.getJob()));
            and = " and ";
        }
        if (filter.getJobChain() != null && !"".equals(filter.getJobChain())) {
            where += String.format(and + " p.jobChain %s :jobChain", SearchStringHelper.getSearchPathOperator(filter.getJobChain()));
            and = " and ";
        }
        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            where += String.format(and + " p.orderId %s :orderId", SearchStringHelper.getSearchOperator(filter.getOrderId()));
            and = " and ";
        }
        if (filter.getIsLate() != null) {
            if (filter.isLate()) {
                where += and + " p.isLate = 1";
            } else {
                where += and + " p.isLate = 0";
            }
            and = " and ";
        }
        if (filter.getStates() != null && filter.getStates().size() > 0) {
            where += and + "(";
            for (String state : filter.getStates()) {
                where += " p.state = '" + state + "' or";
            }
            where += " 1=0)";
            and = " and ";
        }

        if (!"".equals(pathField) && filter.getListOfFolders() != null && filter.getListOfFolders().size() > 0) {
            where += and + "(";
            for (Folder filterFolder : filter.getListOfFolders()) {
                if (filterFolder.getRecursive()) {
                    String likeFolder = (filterFolder.getFolder() + "/%").replaceAll("//+", "/");
                    where += " (" + pathField + " = '" + filterFolder.getFolder() + "' or "+ pathField + " like '" + likeFolder + "')";
                } else {
                    where += String.format(pathField + " %s '" + filterFolder.getFolder() + "'",SearchStringHelper.getSearchOperator(filterFolder.getFolder()));
                }
                where += " or ";
            }
            where += " 0=1)";
            and = " and ";
        }
        
        

        if (!"".equals(where.trim())) {
            where = "where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(Query<T> query) {
        if (filter.getPlannedStartFrom() != null) {
            query.setParameter("plannedStartFrom", filter.getPlannedStartFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getPlannedStartTo() != null) {
            query.setParameter("plannedStartTo", filter.getPlannedStartTo(), TemporalType.TIMESTAMP);
        }
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            query.setParameter("job", SearchStringHelper.getSearchPathValue(filter.getJob()));
        }
        if (filter.getJobChain() != null && !"".equals(filter.getJobChain())) {
            query.setParameter("jobChain", SearchStringHelper.getSearchPathValue(filter.getJobChain()));
        }
        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            query.setParameter("orderId", filter.getOrderId());
        }
        return query;

    }

    public List<DailyPlanWithReportTriggerDBItem> getDailyPlanListOrder(final int limit) throws SOSHibernateException {
        Query<DailyPlanWithReportTriggerDBItem> query = sosHibernateSession.createQuery("select new com.sos.jitl.dailyplan.db.DailyPlanWithReportTriggerDBItem(p,t) from "
                + DailyPlanDBItem + " p," + " " + DBItemReportTrigger + " t " + getWhere("p.jobChain") + " and p.reportExecutionId is null  "
                + " and p.reportTriggerId = t.id  " + filter.getOrderCriteria() + filter.getSortMode());

        query = bindParameters(query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public List<DailyPlanWithReportExecutionDBItem> getDailyPlanListStandalone(final int limit) throws SOSHibernateException {
        Query<DailyPlanWithReportExecutionDBItem> query = sosHibernateSession.createQuery("select new com.sos.jitl.dailyplan.db.DailyPlanWithReportExecutionDBItem(p,e) from " + DailyPlanDBItem
                + " p," + " " + DBItemReportTask + " e " + getWhere("p.job") + " and p.reportExecutionId = e.id  " + " and p.reportTriggerId is null "
                + filter.getOrderCriteria() + filter.getSortMode());

        query = bindParameters(query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public List<DailyPlanWithReportTriggerDBItem> getWaitingDailyPlanOrderList(final int limit) throws SOSHibernateException {
        String q = "from " + DailyPlanDBItem + " p " + getWhere("p.jobChain") + " and p.jobChain <> '.'"
                + " and (p.isAssigned = 0 or p.state = 'PLANNED' or p.state='INCOMPLETE') " + filter.getOrderCriteria() + filter.getSortMode();

        Query<DailyPlanDBItem> query = sosHibernateSession.createQuery(q);
        query = bindParameters(query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        List<DailyPlanDBItem> l = query.getResultList();
        List<DailyPlanWithReportTriggerDBItem> resultList = new ArrayList<DailyPlanWithReportTriggerDBItem>();
        for (DailyPlanDBItem d : l) {
            DBItemReportTrigger t = null;
            if (d.getReportTriggerId() != null) {
                ReportTriggerDBLayer triggerDbLayer = new ReportTriggerDBLayer(sosHibernateSession);
                t = triggerDbLayer.get(d.getReportTriggerId());
            }
            resultList.add(new DailyPlanWithReportTriggerDBItem(d, t));
        }
        return resultList;
    }

    public List<DailyPlanWithReportExecutionDBItem> getWaitingDailyPlanStandaloneList(final int limit) throws SOSHibernateException {
        String q = "from " + DailyPlanDBItem + " p " + getWhere("p.job") + " and p.job <> '.' "
                + " and (p.isAssigned = 0 or p.state = 'PLANNED' or p.state='INCOMPLETE') " + filter.getOrderCriteria() + filter.getSortMode();

        Query<DailyPlanDBItem> query = sosHibernateSession.createQuery(q);
        query = bindParameters(query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        List<DailyPlanDBItem> l = sosHibernateSession.getResultList(query);
        List<DailyPlanWithReportExecutionDBItem> resultList = new ArrayList<DailyPlanWithReportExecutionDBItem>();
        for (DailyPlanDBItem d : l) {
            DBItemReportTask t = null;
            if (d.getReportExecutionId() != null) {
                ReportTaskExecutionsDBLayer taskDbLayer = new ReportTaskExecutionsDBLayer(sosHibernateSession);
                t = taskDbLayer.get(d.getReportExecutionId());
            }
            resultList.add(new DailyPlanWithReportExecutionDBItem(d, t));
        }
        return resultList;
    }

    public List<DailyPlanDBItem> getDailyPlanList(final int limit) throws SOSHibernateException {
        String q = "from " + DailyPlanDBItem + " p " + getWhere();
        LOGGER.debug("DailyPlan sql: " + q + " from " + filter.getPlannedStartFrom() + " to " + filter.getPlannedStartTo());

        Query<DailyPlanDBItem> query = sosHibernateSession.createQuery(q);
        query = bindParameters(query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public int updateDailyPlanList(String schedulerId) throws SOSHibernateException {
        String q = "update " + DailyPlanDBItem + " set state='PLANNED', schedulerId=:schedulerId" + " where state='PLANNEDFORUPDATE'";
        Query<DailyPlanDBItem> query = sosHibernateSession.createQuery(q);
        query.setParameter("schedulerId", schedulerId);

        return sosHibernateSession.executeUpdate(query);
    }

    public DailyPlanFilter getFilter() {
        return filter;
    }

    public void setWhereFrom(final Date whereFrom) {
        filter.setPlannedStartFrom(whereFrom);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        whereFromIso = formatter.format(whereFrom);
    }

    public void setWhereTo(final Date whereTo) {
        UtcTimeHelper.convertTimeZonesToDate(UtcTimeHelper.localTimeZoneString(), "UTC", new DateTime(whereTo));
        filter.setPlannedStartTo(whereTo);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        whereToIso = formatter.format(whereTo);
    }

    public void setWhereSchedulerId(final String whereschedulerId) {
        filter.setSchedulerId(whereschedulerId);
    }

    public void setFilter(final DailyPlanFilter filter) {
        this.filter = filter;
    }

    @Override
    public void onAfterDeleting(DailyPlanDBItem h) {
    }

    @Override
    public List<DailyPlanDBItem> getListOfItemsToDelete() throws SOSHibernateException {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
        int limit = this.getFilter().getLimit();
        Query<DailyPlanDBItem> query = sosHibernateSession.createQuery("from " + DailyPlanDBItem + " p " + getWhere() + filter.getOrderCriteria() + filter.getSortMode());
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getPlannedStartFrom() != null) {
            query.setParameter("plannedStartFrom", filter.getPlannedStartFrom(), TemporalType.TIMESTAMP);
        }
        if (filter.getPlannedStartTo() != null) {
            query.setParameter("plannedStartTo", filter.getPlannedStartTo(), TemporalType.TIMESTAMP);
        }

        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            query.setParameter("job", filter.getJob());
        }
        if (filter.getJobChain() != null && !"".equals(filter.getJobChain())) {
            query.setParameter("jobChain", filter.getJobChain());
        }
        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            query.setParameter("orderId", filter.getOrderId());
        }

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public Date getMaxPlannedStart(String schedulerId) {
        String q = "select max(plannedStart) from " + DailyPlanDBItem + " where schedulerId=:schedulerId";
        Query<Date> query;
        try {
            query = sosHibernateSession.createQuery(q);
            query.setParameter("schedulerId", schedulerId);
            Date d = sosHibernateSession.getSingleValue(query);

            if (d != null) {
                return d;
            } else {
                return new Date();
            }
        } catch (SOSHibernateException e) {
            return new Date();
        }
    }

    public void delete(DailyPlanDBItem dailyPlanDBItem) throws SOSHibernateException {
        filter.setPlannedStart(dailyPlanDBItem.getPlannedStart());
        filter.setJob(dailyPlanDBItem.getJob());
        filter.setJobChain(dailyPlanDBItem.getJobChain());
        filter.setOrderId(dailyPlanDBItem.getOrderId());
        filter.setSchedulerId(dailyPlanDBItem.getSchedulerId());
        delete();
    }

}