package com.sos.jitl.dailyplan.db;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.joda.time.DateTime;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.classes.UtcTimeHelper;
import com.sos.hibernate.layer.SOSHibernateIntervalDBLayer;
import com.sos.jitl.dailyplan.filter.DailyPlanFilter;

/** @author Uwe Risse */
public class DailyPlanDBLayer extends SOSHibernateIntervalDBLayer {

    private static final String DailyPlanDBItem = DailyPlanDBItem.class.getName();

    private String whereFromIso = null;
    private String whereToIso = null;
    private DailyPlanFilter filter = null;
    private static final Logger LOGGER = Logger.getLogger(DailyPlanDBLayer.class);

    public DailyPlanDBLayer(final String configurationFilename) {
        super();
        this.setConfigurationFileName(configurationFilename);
        this.initConnection(this.getConfigurationFileName());
        resetFilter();
    }

    public DailyPlanDBLayer(SOSHibernateConnection connection) {
        super();
        this.initConnection(connection);
        resetFilter();
    }

    public DailyPlanDBLayer(final File configurationFile) {
        super();
        try {
            this.setConfigurationFileName(configurationFile.getCanonicalPath());
        } catch (IOException e) {
            this.setConfigurationFileName("");
            LOGGER.error(e.getMessage(), e);
        }
        this.initConnection(this.getConfigurationFileName());
        resetFilter();
    }

    public DailyPlanDBItem getPlanDbItem(final Long id) throws Exception {
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        return (DailyPlanDBItem) ((Session) connection.getCurrentSession()).get(DailyPlanDBItem.class, id);
    }

    public void resetFilter() {
        filter = new DailyPlanFilter();
        filter.setSchedulerId("");
        filter.setJob("");
        filter.setJobChain("");
        filter.setOrderId("");
    }

    public int delete() throws Exception {
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        String hql = "delete from " + DailyPlanDBItem + " " + getWhere();
        Query query = null;
        int row = 0;
        query = connection.createQuery(hql);
        if (filter.getPlannedStartFrom() != null && !"".equals(filter.getPlannedStartFrom())) {
            query.setTimestamp("plannedStartFrom", filter.getPlannedStartFrom());
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
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        String hql = "delete from " + DailyPlanDBItem + " " + getWhere();
        Query query = null;
        int row = 0;
        query = connection.createQuery(hql);
        if (filter.getPlannedStartFrom() != null) {
            query.setTimestamp("plannedStartFrom", filter.getPlannedStartFrom());
        }
        if (filter.getPlannedStartTo() != null) {
            query.setTimestamp("plannedStartTo", filter.getPlannedStartTo());
        }
        row = query.executeUpdate();
        connection.commit();
        return row;
    }

    private String getWhere() {
        String where = "";
        String and = "";
        if (filter.getPlannedStartFrom() != null && !"".equals(filter.getPlannedStartFrom())) {
            where += and + " plannedStart>= :plannedStartFrom";
            and = " and ";
        }
        if (filter.getPlannedStartTo() != null && !"".equals(filter.getPlannedStartTo())) {
            where += and + " plannedStart < :plannedStartTo ";
            and = " and ";
        }
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            where += and + " schedulerId = :schedulerId";
            and = " and ";
        }
        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            where += and + " job = :job";
            and = " and ";
        }
        if (filter.getJobChain() != null && !"".equals(filter.getJobChain())) {
            where += and + " jobChain = :jobChain";
            and = " and ";
        }
        if (filter.getOrderId() != null && !"".equals(filter.getOrderId())) {
            where += and + " orderId = :orderId";
            and = " and ";
        }
        if (filter.getIsLate() != null) {
            if (filter.isLate()) {
                where += and + " isLate = 1";
            } else {
                where += and + " isLate = 0";
            }
            and = " and ";
        }
        if (filter.getStates() != null && filter.getStates().size() > 0) {
            where += and + "(";
            for (String state : filter.getStates()) {
                where += " state = '" + state + "' or";
            }
            where += " 1=0)";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = "where " + where;
        }
        return where;
    }

    @SuppressWarnings("unchecked")
    public List<DailyPlanDBItem> getDailyPlanList(final int limit) throws Exception {
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        Query query = null;
        List<DailyPlanDBItem> daysScheduleList = null;
        query = connection.createQuery("from " + DailyPlanDBItem + " " + getWhere() + filter.getOrderCriteria() + filter.getSortMode());
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
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        daysScheduleList = query.list();
        return daysScheduleList;
    }

    @SuppressWarnings("unchecked")
    private List<DailyPlanDBItem> executeQuery(Query query, int limit) {
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

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return query.list();
    }

    public List<DailyPlanDBItem> getDailyPlanSchedulerList(int limit) throws Exception {
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        String q = "from " + DailyPlanDBItem + " e where e.schedulerId IN (select DISTINCT schedulerId from " + DailyPlanDBItem + " " + getWhere() + ")";
        Query query = null;
        query = connection.createQuery(q);
        return executeQuery(query, limit);
    }

    public List<DailyPlanDBItem> getWaitingDailyPlanList(final int limit) throws Exception {
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        String q = "from " + DailyPlanDBItem + " " + getWhere() + "  and (isAssigned = 0 or state = 'PLANNED' or state='INCOMPLETE') " + filter.getOrderCriteria() + filter.getSortMode();
        Query query = null;
        query = connection.createQuery(q);
        return executeQuery(query, limit);
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

    public void setWhereFromUtc(final String whereFrom) throws ParseException {
        if ("".equals(whereFrom)) {
            filter.setPlannedStartFrom("");
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat(filter.getDateFormat());
            Date d = formatter.parse(whereFrom);
            d = UtcTimeHelper.convertTimeZonesToDate(UtcTimeHelper.localTimeZoneString(), "UTC", new DateTime(d));
            setWhereFrom(d);
        }
    }

    public void setWhereToUtc(final String whereTo) throws ParseException {
        if ("".equals(whereTo)) {
            filter.setPlannedStartTo("");
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat(filter.getDateFormat());
            Date d = formatter.parse(whereTo);
            d = UtcTimeHelper.convertTimeZonesToDate(UtcTimeHelper.localTimeZoneString(), "UTC", new DateTime(d));
            setWhereTo(d);
        }
    }

    public void setWhereFrom(final String whereFrom) throws ParseException {
        if ("".equals(whereFrom)) {
            filter.setPlannedStartFrom("");
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat(filter.getDateFormat());
            Date d = formatter.parse(whereFrom);
            setWhereFrom(d);
        }
    }

    public void setWhereTo(final String whereTo) throws ParseException {
        if ("".equals(whereTo)) {
            filter.setPlannedStartTo("");
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat(filter.getDateFormat());
            Date d = formatter.parse(whereTo);
            setWhereTo(d);
        }
    }

    public Date getWhereUtcFrom() {
        return filter.getPlannedStartFrom();
    }

    public Date getWhereUtcTo() {
        return filter.getPlannedStartTo();
    }

    public void setWhereSchedulerId(final String whereschedulerId) {
        filter.setSchedulerId(whereschedulerId);
    }

    public void setDateFormat(final String dateFormat) {
        filter.setDateFormat(dateFormat);
    }

    public String getWhereFromIso() {
        return whereFromIso;
    }

    public String getWhereToIso() {
        return whereToIso;
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
        if (connection == null) {
            initConnection(getConfigurationFileName());
        }
        Query query = null;
        List<DbItem> schedulerPlannedList = null;
        query = connection.createQuery("from " + DailyPlanDBItem + " " + getWhere() + filter.getOrderCriteria() + filter.getSortMode());
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