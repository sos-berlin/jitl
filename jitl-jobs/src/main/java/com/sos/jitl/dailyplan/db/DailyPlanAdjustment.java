package com.sos.jitl.dailyplan.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.UtcTimeHelper;
import com.sos.jitl.dailyplan.job.CheckDailyPlanOptions;
import com.sos.jitl.reporting.db.DBItemReportExecution;
import com.sos.jitl.reporting.db.DBItemReportTrigger;
import com.sos.jitl.reporting.db.ReportExecutionsDBLayer;
import com.sos.jitl.reporting.db.ReportTriggerDBLayer;

public class DailyPlanAdjustment {

    private static final Logger LOGGER = Logger.getLogger(DailyPlanAdjustment.class);
    private DailyPlanDBLayer dailyPlanDBLayer;
    private ReportExecutionsDBLayer dailyPlanExecutionsDBLayer;
    private ReportTriggerDBLayer dailyPlanTriggerDbLayer;
    private String dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
    private String schedulerId;
    private Date from;
    private Date to;
    private int dayOffset;
    private CheckDailyPlanOptions options = null;
    private boolean dailyPlanUpdated = false;
    private HashMap<Long, String> reportExecutions;
    private HashMap<Long, String> reportTriggers;

    public DailyPlanAdjustment(SOSHibernateSession sosHibernateSession) throws Exception {
        dailyPlanDBLayer = new DailyPlanDBLayer(sosHibernateSession);
        dailyPlanExecutionsDBLayer = new ReportExecutionsDBLayer(dailyPlanDBLayer.getSession());
        dailyPlanTriggerDbLayer = new ReportTriggerDBLayer(dailyPlanDBLayer.getSession());
    }

    public void beginTransaction() throws Exception {
        dailyPlanDBLayer.getSession().beginTransaction();
    }

    public void commit() throws Exception {
        dailyPlanDBLayer.getSession().commit();
    }

    public void rollback() throws Exception {
        dailyPlanDBLayer.getSession().rollback();
    }

    public void closeSession() throws Exception {
        dailyPlanDBLayer.getSession().close();
    }

    private void initHashMaps() throws Exception {
        if (reportExecutions == null) {
            reportExecutions = new HashMap<Long, String>();
            reportTriggers = new HashMap<Long, String>();

            dailyPlanDBLayer.setWhereSchedulerId(this.schedulerId);
            dailyPlanDBLayer.setWhereFrom(from);
            dailyPlanDBLayer.setWhereTo(to);
            List<DailyPlanDBItem> dailyPlanList;

            dailyPlanList = dailyPlanDBLayer.getDailyPlanList(-1);
            for (DailyPlanDBItem dailyPlanDBItem : dailyPlanList) {
                if (dailyPlanDBItem.isOrderJob()) {
                    reportTriggers.put(dailyPlanDBItem.getReportTriggerId(), dailyPlanDBItem.getIdentifier());
                } else {
                    reportExecutions.put(dailyPlanDBItem.getReportExecutionId(), dailyPlanDBItem.getIdentifier());
                }
            }
        }
    }

    private boolean reportExecutionIsAssigned(DBItemReportExecution dbItemReportExecution) throws Exception {
        initHashMaps();
        return reportExecutions.get(dbItemReportExecution.getId()) != null;
    }

    private boolean reportTriggerIsAssigned(DBItemReportTrigger dbItemReportTrigger) throws Exception {
        initHashMaps();
        return reportTriggers.get(dbItemReportTrigger.getId()) != null;
    }

    private void adjustDailyPlanStandaloneItem(DailyPlanWithReportExecutionDBItem dailyPlanWithReportExecutionDBItem, List<DBItemReportExecution> reportExecutionList)
            throws Exception {
        LOGGER.debug(String.format("%s records in reportExecutionList", reportExecutionList.size()));
        // It can be late even it has never been startet
        if (dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().isStandalone() && !dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().getIsLate()
                && !dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().getIsAssigned()) {
            dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().setIsLate(dailyPlanWithReportExecutionDBItem.getExecutionState().isLate());

            try {
                dailyPlanDBLayer.getSession().update(dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem());
            } catch (org.hibernate.StaleStateException e) {
            }
        }

        for (int i = 0; i < reportExecutionList.size(); i++) {
            DBItemReportExecution dbItemReportExecution = (DBItemReportExecution) reportExecutionList.get(i);
            if (!reportExecutionIsAssigned(dbItemReportExecution) && dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().isStandalone() && dailyPlanWithReportExecutionDBItem
                    .isEqual(dbItemReportExecution)) {
                LOGGER.debug(String.format("... assign %s to %s", dbItemReportExecution.getId(), dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().getJobName()));
                dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().setReportExecutionId(dbItemReportExecution.getId());
                dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().setIsAssigned(true);
                dailyPlanWithReportExecutionDBItem.setDbItemReportExecution(reportExecutionList.get(i));

                dailyPlanWithReportExecutionDBItem.setExecutionState(null);

                dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().setIsLate(dailyPlanWithReportExecutionDBItem.getExecutionState().isLate());
                dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().setState(dailyPlanWithReportExecutionDBItem.getExecutionState().getState());
                reportExecutions.put(dbItemReportExecution.getId(), dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().getIdentifier());

                try {
                    dailyPlanDBLayer.getSession().update(dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem());
                    dailyPlanUpdated = true;
                } catch (org.hibernate.StaleStateException e) {
                }
                break;
            }
        }
    }

    private void adjustDailyPlanOrderItem(DailyPlanWithReportTriggerDBItem dailyPlanWithReportTriggerDBItem, List<DBItemReportTrigger> dbItemReportTriggerList) throws Exception {
        LOGGER.debug(String.format("%s records in dbItemReportTriggerList", dbItemReportTriggerList.size()));
        // It can be late even it has never been startet
        if (dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().isOrderJob() && !dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getIsLate()
                && !dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getIsAssigned()) {
            dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setIsLate(dailyPlanWithReportTriggerDBItem.getExecutionState().isLate());
            try {
                dailyPlanDBLayer.getSession().update(dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem());
            } catch (org.hibernate.StaleStateException e) {
            }
        }

        for (DBItemReportTrigger dbItemReportTrigger : dbItemReportTriggerList) {
            if (!reportTriggerIsAssigned(dbItemReportTrigger) && dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().isOrderJob() && dailyPlanWithReportTriggerDBItem.isEqual(
                    dbItemReportTrigger)) {
                LOGGER.debug(String.format("... assign %s to %s/%s", dbItemReportTrigger.getHistoryId(), dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getJobChainNotNull(),
                        dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getOrderId()));
                dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setReportTriggerId(dbItemReportTrigger.getId());
                dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setIsAssigned(true);

                dailyPlanWithReportTriggerDBItem.setDbItemReportTrigger(dbItemReportTrigger);
                dailyPlanWithReportTriggerDBItem.setExecutionState(null);

                dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setIsLate(dailyPlanWithReportTriggerDBItem.getExecutionState().isLate());
                dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setState(dailyPlanWithReportTriggerDBItem.getExecutionState().getState());

                reportTriggers.put(dbItemReportTrigger.getId(), dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getIdentifier());
                try {
                    try {
                        dailyPlanDBLayer.getSession().update(dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem());
                        dailyPlanUpdated = true;
                    } catch (org.hibernate.StaleStateException e) {
                    }

                } catch (org.hibernate.StaleStateException e) {
                }
                break;
            }
        }

        /*
         * if
         * (!dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getIsAssigned
         * ()) { for (DBItemReportTrigger
         * dbItemReportTrigger:dbItemReportTriggerList) { if
         * (dbItemReportTrigger.getEndTime() == null &&
         * !reportTriggerIsAssigned(dbItemReportTrigger) &&
         * dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().isOrderJob() &&
         * dailyPlanWithReportTriggerDBItem.isEqual(dbItemReportTrigger)) {
         * LOGGER.debug(String.format("... assign %s to %s/%s",
         * dbItemReportTrigger.getHistoryId(),
         * dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem()
         * .getJobChainNotNull(),
         * dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getOrderId()));
         * dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().
         * setReportTriggerId(dbItemReportTrigger.getId());
         * dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setIsAssigned(
         * true);
         * 
         * dailyPlanWithReportTriggerDBItem.setExecutionState(null);
         * 
         * dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setIsLate(
         * dailyPlanWithReportTriggerDBItem.getExecutionState().isLate());
         * dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setState(
         * dailyPlanWithReportTriggerDBItem.getExecutionState().getState());
         * 
         * try {
         * dailyPlanDBLayer.getSession().update(dailyPlanWithReportTriggerDBItem
         * .getDailyPlanDbItem()); } catch (org.hibernate.StaleStateException e)
         * { }
         * 
         * reportTriggers.put(dbItemReportTrigger.getId(),
         * dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getIdentifier()
         * ); break; } } }
         */
    }

    public void adjustWithHistory() throws Exception {
        String toTimeZoneString = "UTC";
        String fromTimeZoneString = DateTimeZone.getDefault().getID();

        from = UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, new DateTime(from));
        to = UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, new DateTime(to));
        LOGGER.debug(String.format("reading from: %s to %s", from, to));

        // String lastSchedulerId = "***";
        dailyPlanDBLayer.setWhereSchedulerId(this.schedulerId);
        dailyPlanDBLayer.setWhereFrom(from);
        dailyPlanDBLayer.setWhereTo(to);
        dailyPlanDBLayer.getFilter().setOrderCriteria("p.job,p.plannedStart");
        dailyPlanDBLayer.getFilter().setSortMode("desc");

        dailyPlanDBLayer.getFilter().setOrderCriteria("p.jobChain,p.plannedStart");
        List<DailyPlanWithReportTriggerDBItem> dailyPlanOrderList = dailyPlanDBLayer.getWaitingDailyPlanOrderList(-1);

        dailyPlanDBLayer.getFilter().setOrderCriteria("p.job,p.plannedStart");
        List<DailyPlanWithReportExecutionDBItem> dailyPlanStandaloneList = dailyPlanDBLayer.getWaitingDailyPlanStandaloneList(-1);

        dailyPlanExecutionsDBLayer.getFilter().setLimit(-1);
        dailyPlanExecutionsDBLayer.getFilter().setExecutedFrom(from);
        dailyPlanExecutionsDBLayer.getFilter().setExecutedTo(to);

        dailyPlanTriggerDbLayer.getFilter().setLimit(-1);
        dailyPlanTriggerDbLayer.getFilter().setExecutedFrom(from);
        dailyPlanTriggerDbLayer.getFilter().setExecutedTo(to);

        List<DBItemReportExecution> dbItemReportExecutionList = null;
        List<DBItemReportTrigger> dbItemReportTriggerList = null;
 

        String lastJobChain = "";
        for (int i = 0; i < dailyPlanOrderList.size(); i++) {
            DailyPlanWithReportTriggerDBItem dailyPlanWithReportTriggerItem = (DailyPlanWithReportTriggerDBItem) dailyPlanOrderList.get(i);

            String orderId = dailyPlanWithReportTriggerItem.getDailyPlanDbItem().getOrderId();
            String jobChain = dailyPlanWithReportTriggerItem.getDailyPlanDbItem().getJobChain();
            String s = String.format("%s(%s)", jobChain, orderId);
            if (!lastJobChain.equals(s)) {
                dailyPlanTriggerDbLayer.getFilter().setSchedulerId(schedulerId);
                dailyPlanTriggerDbLayer.getFilter().setOrderId(orderId);
                dailyPlanTriggerDbLayer.getFilter().setJobChain(jobChain);
                dbItemReportTriggerList = dailyPlanTriggerDbLayer.getSchedulerOrderHistoryListFromTo();
                lastJobChain = String.format("%s(%s)", jobChain, orderId);
            }

            adjustDailyPlanOrderItem(dailyPlanWithReportTriggerItem, dbItemReportTriggerList);
        }

        String lastJob = "";
        for (int i = 0; i < dailyPlanStandaloneList.size(); i++) {
            DailyPlanWithReportExecutionDBItem dailyPlanWithReportExecutionItem = (DailyPlanWithReportExecutionDBItem) dailyPlanStandaloneList.get(i);
            String jobName = dailyPlanWithReportExecutionItem.getDailyPlanDbItem().getJobName();
            if (!lastJob.equals(jobName)) {
                dailyPlanExecutionsDBLayer.getFilter().setSchedulerId(schedulerId);
                dailyPlanExecutionsDBLayer.getFilter().addJobPath(jobName);
                dbItemReportExecutionList = dailyPlanExecutionsDBLayer.getSchedulerHistoryListFromTo();
                lastJob = jobName;
            }
            adjustDailyPlanStandaloneItem(dailyPlanWithReportExecutionItem, dbItemReportExecutionList);
        }

    }

    private void setFrom() throws ParseException {
        TimeZone.setDefault(TimeZone.getTimeZone(DateTimeZone.getDefault().getID()));
        Date now = new Date();
        if (dayOffset < 0) {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(now);
            calendar.add(GregorianCalendar.DAY_OF_MONTH, dayOffset);
            now = calendar.getTime();
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String froms = formatter.format(now);
        froms = froms + "T00:00:00";
        formatter = new SimpleDateFormat(dateFormat);
        this.from = formatter.parse(froms);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    private void setTo() throws ParseException {
        TimeZone.setDefault(TimeZone.getTimeZone(DateTimeZone.getDefault().getID()));
        Date now = new Date();
        if (dayOffset > 0) {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(now);
            calendar.add(GregorianCalendar.DAY_OF_MONTH, dayOffset);
            now = calendar.getTime();
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String tos = formatter.format(now);
        tos = tos + "T23:59:59";
        formatter = new SimpleDateFormat(dateFormat);
        this.to = formatter.parse(tos);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public Date getFrom() {
        return from;
    }

    public Date getTo() {
        return to;
    }

    public void setOptions(CheckDailyPlanOptions options) throws ParseException {
        this.options = options;
        schedulerId = this.options.getscheduler_id().getValue();
        dayOffset = this.options.getdayOffset().value();
        setFrom();
        setTo();
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public boolean isDailyPlanUpdated() {
        return dailyPlanUpdated;
    }

}