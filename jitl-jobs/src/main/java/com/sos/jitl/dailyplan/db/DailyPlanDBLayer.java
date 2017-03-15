package com.sos.jitl.dailyplan.db;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.hibernate.query.Query;
import org.joda.time.DateTime;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateStatelessSession;
import com.sos.hibernate.classes.UtcTimeHelper;
import com.sos.hibernate.layer.SOSHibernateIntervalDBLayer;
import com.sos.jitl.dailyplan.filter.DailyPlanFilter;
import com.sos.jitl.reporting.db.DBItemReportExecution;
import com.sos.jitl.reporting.db.DBItemReportTrigger;

/** @author Uwe Risse */
public class DailyPlanDBLayer extends SOSHibernateIntervalDBLayer {

    private static final String DailyPlanDBItem = DailyPlanDBItem.class.getName();
    private static final String DBItemReportExecution = DBItemReportExecution.class.getName();
    private static final String DBItemReportTrigger = DBItemReportTrigger.class.getName();
    
    private String whereFromIso = null;
    private String whereToIso = null;
    private DailyPlanFilter filter = null;
    private static final Logger LOGGER = Logger.getLogger(DailyPlanDBLayer.class);
 
   
    public DailyPlanDBLayer(SOSHibernateSession session) throws Exception {
        super();
        this.sosHibernateSession = session;
        resetFilter();
    }

    public DailyPlanDBLayer(final File configurationFile) throws Exception {
        super();
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

    public int delete() throws Exception {
        String hql = "delete from " + DailyPlanDBItem + " p " + getWhere();
        Query query = null;
        int row = 0;
        query = sosHibernateSession.createQuery(hql);
        if (filter.getPlannedStartFrom() != null && !"".equals(filter.getPlannedStartFrom())) {
            query.setTimestamp("plannedStartFrom", filter.getPlannedStartFrom());
            query.setParameter("plannedStartFrom", filter.getPlannedStartFrom());
        }
        if (filter.getPlannedStartTo() != null && !"".equals(filter.getPlannedStartTo())) {
            query.setTimestamp("plannedStartTo", filter.getPlannedStartTo());
        }
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        row = query.executeUpdate();
        return row;
    }

    public long deleteInterval() throws Exception {
        String hql = "delete from " + DailyPlanDBItem + " " + getWhere();
        Query query = null;
        int row = 0;
        query = sosHibernateSession.createQuery(hql);
        if (filter.getPlannedStartFrom() != null) {
            query.setTimestamp("plannedStartFrom", filter.getPlannedStartFrom());
        }
        if (filter.getPlannedStartTo() != null) {
            query.setTimestamp("plannedStartTo", filter.getPlannedStartTo());
        }
        row = query.executeUpdate();
        sosHibernateSession.commit();
        return row;
    }

    private String getWhere() {
        String where = "";
        String and = "";
        if (filter.getPlannedStartFrom() != null && !"".equals(filter.getPlannedStartFrom())) {
            where += and + " p.plannedStart>= :plannedStartFrom";
            and = " and ";
        }
        if (filter.getPlannedStartTo() != null && !"".equals(filter.getPlannedStartTo())) {
            where += and + " p.plannedStart < :plannedStartTo ";
            and = " and ";
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
    public List<DailyPlanWithReportTriggerDBItem> getDailyPlanListOrder(final int limit) throws Exception {
        Query query = null;
        List<DailyPlanWithReportTriggerDBItem> daysScheduleList = null;
        query = sosHibernateSession.createQuery("select new com.sos.jitl.dailyplan.db.DailyPlanWithReportTriggerDBItem(p,t) from " + DailyPlanDBItem + " p," + " " + DBItemReportTrigger
                + " t " + getWhere() + " and p.reportExecutionId is null  " + " and p.reportTriggerId = t.id  " + 
                filter.getOrderCriteria() + filter.getSortMode());

        query = bindParameters(query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        daysScheduleList = query.list();
        return daysScheduleList;
    }

    @SuppressWarnings("unchecked")
    public List<DailyPlanWithReportExecutionDBItem> getDailyPlanListStandalone(final int limit) throws Exception {
        Query query = null;
        List<DailyPlanWithReportExecutionDBItem> dailyPlanList = null;
        query = sosHibernateSession.createQuery("select new com.sos.jitl.dailyplan.db.DailyPlanWithReportExecutionDBItem(p,e) from " + DailyPlanDBItem + " p," + " " + DBItemReportExecution
                + " e " + getWhere() + " and e.triggerId = 0" + " and p.reportExecutionId = e.id  " + " and p.reportTriggerId is null " + filter.getOrderCriteria() + filter
                        .getSortMode());

        query = bindParameters(query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        dailyPlanList = query.list();
        return dailyPlanList;
    }

    @SuppressWarnings("unchecked")
    public List<DailyPlanWithReportTriggerDBItem> getWaitingDailyPlanOrderList(final int limit) throws Exception {
        String q = "from " + DailyPlanDBItem + " p "
                + getWhere()
                + " and p.reportTriggerId is null" 
                + " and p.jobChain is not null" 
                + " and (p.isAssigned = 0 or p.state = 'PLANNED' or p.state='INCOMPLETE') " + filter.getOrderCriteria() + filter.getSortMode();

        Query query = sosHibernateSession.createQuery(q);
        query = bindParameters(query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        List<DailyPlanDBItem> l = query.list();
        ArrayList<DailyPlanWithReportTriggerDBItem> resultList = new ArrayList<DailyPlanWithReportTriggerDBItem>();
        for (int i = 0; i < l.size(); i++) {
            resultList.add(new DailyPlanWithReportTriggerDBItem(l.get(i), null)) ;
        }
        return resultList;
    }

    @SuppressWarnings("unchecked")
    public List<DailyPlanWithReportExecutionDBItem> getWaitingDailyPlanStandaloneList(final int limit) throws Exception {
        String q = "from " + DailyPlanDBItem + " p " + getWhere()
                + " and p.reportExecutionId is null " 
                + " and p.job is not null " 
                + " and (p.isAssigned = 0 or p.state = 'PLANNED' or p.state='INCOMPLETE') " + filter.getOrderCriteria() + filter.getSortMode();

        Query query = sosHibernateSession.createQuery(q);
        query = bindParameters(query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        List<DailyPlanDBItem> l = query.list();
        ArrayList<DailyPlanWithReportExecutionDBItem> resultList = new ArrayList<DailyPlanWithReportExecutionDBItem>();
        for (int i = 0; i < l.size(); i++) {
            resultList.add(new DailyPlanWithReportExecutionDBItem(l.get(i), null)) ;
        }
        return resultList;
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
    public List<DbItem> getListOfItemsToDelete() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
        int limit = this.getFilter().getLimit();
        Query query = null;
        List<DbItem> schedulerPlannedList = null;
        query = sosHibernateSession.createQuery("from " + DailyPlanDBItem + " " + getWhere() + filter.getOrderCriteria() + filter.getSortMode());
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
        schedulerPlannedList = query.list();
        return schedulerPlannedList;
    }

}