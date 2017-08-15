package com.sos.jitl.dailyplan.db;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.query.Query;
import org.joda.time.DateTime;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.UtcTimeHelper;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.hibernate.layer.SOSHibernateIntervalDBLayer;
import com.sos.jitl.dailyplan.filter.DailyPlanFilter;
import com.sos.jitl.reporting.db.DBItemReportTrigger;
import com.sos.jitl.reporting.db.ReportTaskExecutionsDBLayer;
import com.sos.jitl.reporting.db.ReportTriggerDBLayer;
import com.sos.jitl.reporting.db.filter.FilterFolder;
import com.sos.jitl.reporting.db.DBItemReportTask;

/** @author Uwe Risse */
public class DailyPlanDBLayer extends SOSHibernateIntervalDBLayer {

    private static final String DailyPlanDBItem = DailyPlanDBItem.class.getName();
    private static final String DBItemReportTask = DBItemReportTask.class.getName();
    private static final String DBItemReportTrigger = DBItemReportTrigger.class.getName();

    private String whereFromIso = null;
    private String whereToIso = null;
    private DailyPlanFilter filter = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanDBLayer.class);

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
        String hql = "delete from " + DailyPlanDBItem + " p " + getWhere();
        Query query = null;
        int row = 0;
        query = sosHibernateSession.createQuery(hql);
        if (filter.getPlannedStart() != null && !"".equals(filter.getPlannedStart())) {
            query.setTimestamp("plannedStart", filter.getPlannedStart());
        } else {
            if (filter.getPlannedStartFrom() != null && !"".equals(filter.getPlannedStartFrom())) {
                query.setTimestamp("plannedStartFrom", filter.getPlannedStartFrom());
            }
            if (filter.getPlannedStartTo() != null && !"".equals(filter.getPlannedStartTo())) {
                query.setTimestamp("plannedStartTo", filter.getPlannedStartTo());
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
        String hql = "delete from " + DailyPlanDBItem + " p " + getWhere();
        Query query = null;
        int row = 0;
        query = sosHibernateSession.createQuery(hql);
        if (filter.getPlannedStartFrom() != null) {
            query.setTimestamp("plannedStartFrom", filter.getPlannedStartFrom());
        }
        if (filter.getPlannedStartTo() != null) {
            query.setTimestamp("plannedStartTo", filter.getPlannedStartTo());
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
        if (filter.getPlannedStart() != null && !"".equals(filter.getPlannedStart())) {
            where += and + " p.plannedStart = :plannedStart";
            and = " and ";
        } else {
            if (filter.getPlannedStartFrom() != null && !"".equals(filter.getPlannedStartFrom())) {
                where += and + " p.plannedStart>= :plannedStartFrom";
                and = " and ";
            }
            if (filter.getPlannedStartTo() != null && !"".equals(filter.getPlannedStartTo())) {
                where += and + " p.plannedStart < :plannedStartTo ";
                and = " and ";
            }
        }
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            where += and + " p.schedulerId = :schedulerId";
            and = " and ";
        }
        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            where += and + " p.job = :job";
            and = " and ";
        }
        if (filter.getJobChain() != null && !"".equals(filter.getJobChain())) {
            where += and + " p.jobChain = :jobChain";
            and = " and ";
        }
        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            where += and + " p.orderId = :orderId";
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
            for (FilterFolder filterFolder : filter.getListOfFolders()) {
                where += pathField + " like '" + filterFolder.getFolder() + "%'";
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

    private Query bindParameters(Query query) {
        if (filter.getPlannedStartFrom() != null && !"".equals(filter.getPlannedStartFrom())) {
            query.setTimestamp("plannedStartFrom", filter.getPlannedStartFrom());
        }
        if (filter.getPlannedStartTo() != null && !"".equals(filter.getPlannedStartTo())) {
            query.setTimestamp("plannedStartTo", filter.getPlannedStartTo());
        }
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
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
        return query;

    }

    @SuppressWarnings("unchecked")
    public List<DailyPlanWithReportTriggerDBItem> getDailyPlanListOrder(final int limit) throws SOSHibernateException {
        List<DailyPlanWithReportTriggerDBItem> daysScheduleList = null;
        Query query = sosHibernateSession.createQuery("select new com.sos.jitl.dailyplan.db.DailyPlanWithReportTriggerDBItem(p,t) from "
                + DailyPlanDBItem + " p," + " " + DBItemReportTrigger + " t " + getWhere("p.jobChain") + " and p.reportExecutionId is null  "
                + " and p.reportTriggerId = t.id  " + filter.getOrderCriteria() + filter.getSortMode());

        query = bindParameters(query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        daysScheduleList = sosHibernateSession.getResultList(query);
        return daysScheduleList;
    }

    @SuppressWarnings("unchecked")
    public List<DailyPlanWithReportExecutionDBItem> getDailyPlanListStandalone(final int limit) throws SOSHibernateException {
        Query query = null;
        List<DailyPlanWithReportExecutionDBItem> dailyPlanList = null;
        query = sosHibernateSession.createQuery("select new com.sos.jitl.dailyplan.db.DailyPlanWithReportExecutionDBItem(p,e) from " + DailyPlanDBItem
                + " p," + " " + DBItemReportTask + " e " + getWhere("p.job") + " and p.reportExecutionId = e.id  " + " and p.reportTriggerId is null "
                + filter.getOrderCriteria() + filter.getSortMode());

        query = bindParameters(query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        dailyPlanList = sosHibernateSession.getResultList(query);
        return dailyPlanList;
    }

    @SuppressWarnings("unchecked")
    public List<DailyPlanWithReportTriggerDBItem> getWaitingDailyPlanOrderList(final int limit) throws SOSHibernateException {
        String q = "from " + DailyPlanDBItem + " p " + getWhere("p.jobChain") + " and p.jobChain <> '.'"
                + " and (p.isAssigned = 0 or p.state = 'PLANNED' or p.state='INCOMPLETE') " + filter.getOrderCriteria() + filter.getSortMode();

        Query query = sosHibernateSession.createQuery(q);
        query = bindParameters(query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        List<DailyPlanDBItem> l = query.list();
        ArrayList<DailyPlanWithReportTriggerDBItem> resultList = new ArrayList<DailyPlanWithReportTriggerDBItem>();
        for (int i = 0; i < l.size(); i++) {
            DailyPlanDBItem d = l.get(i);
            DBItemReportTrigger t = null;
            if (d.getReportTriggerId() != null) {
                ReportTriggerDBLayer triggerDbLayer = new ReportTriggerDBLayer(sosHibernateSession);
                t = triggerDbLayer.get(d.getReportTriggerId());
            }
            resultList.add(new DailyPlanWithReportTriggerDBItem(d, t));
        }
        return resultList;
    }

    @SuppressWarnings("unchecked")
    public List<DailyPlanWithReportExecutionDBItem> getWaitingDailyPlanStandaloneList(final int limit) throws SOSHibernateException {
        String q = "from " + DailyPlanDBItem + " p " + getWhere("p.job") + " and p.job <> '.' "
                + " and (p.isAssigned = 0 or p.state = 'PLANNED' or p.state='INCOMPLETE') " + filter.getOrderCriteria() + filter.getSortMode();

        Query query = sosHibernateSession.createQuery(q);
        query = bindParameters(query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        List<DailyPlanDBItem> l = sosHibernateSession.getResultList(query);
        ArrayList<DailyPlanWithReportExecutionDBItem> resultList = new ArrayList<DailyPlanWithReportExecutionDBItem>();
        for (int i = 0; i < l.size(); i++) {
            DailyPlanDBItem d = l.get(i);
            DBItemReportTask t = null;
            if (d.getReportExecutionId() != null) {
                ReportTaskExecutionsDBLayer taskDbLayer = new ReportTaskExecutionsDBLayer(sosHibernateSession);
                t = taskDbLayer.get(d.getReportExecutionId());
            }
            resultList.add(new DailyPlanWithReportExecutionDBItem(d, t));
        }
        return resultList;
    }

    @SuppressWarnings("unchecked")
    public List<DailyPlanDBItem> getDailyPlanList(final int limit) throws SOSHibernateException {
        String q = "from " + DailyPlanDBItem + " p " + getWhere();

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
    public void onAfterDeleting(DbItem h) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DbItem> getListOfItemsToDelete() throws SOSHibernateException {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
        int limit = this.getFilter().getLimit();
        Query query = null;
        List<DbItem> schedulerPlannedList = null;
        query = sosHibernateSession.createQuery("from " + DailyPlanDBItem + " p " + getWhere() + filter.getOrderCriteria() + filter.getSortMode());
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setText("schedulerId", filter.getSchedulerId());
        }
        if (filter.getPlannedStartFrom() != null) {
            query.setTimestamp("plannedStartFrom", filter.getPlannedStartFrom());
        }
        if (filter.getPlannedStartTo() != null) {
            query.setTimestamp("plannedStartTo", filter.getPlannedStartTo());
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
        schedulerPlannedList = sosHibernateSession.getResultList(query);
        return schedulerPlannedList;
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