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
import com.sos.hibernate.exceptions.SOSHibernateObjectOperationException;
import com.sos.jitl.dailyplan.job.CheckDailyPlanOptions;
import com.sos.jitl.reporting.db.DBItemReportTask;
import com.sos.jitl.reporting.db.DBItemReportTrigger;
import com.sos.jitl.reporting.db.DBLayerReporting;
import com.sos.jitl.reporting.db.ReportTaskExecutionsDBLayer;
import com.sos.jitl.reporting.db.ReportTriggerDBLayer;

import sos.util.SOSDuration;
import sos.util.SOSDurations;

public class DailyPlanAdjustment {

    private static final Logger LOGGER = Logger.getLogger(DailyPlanAdjustment.class);
    private DailyPlanDBLayer dailyPlanDBLayer;
    private ReportTaskExecutionsDBLayer dailyPlanTaskExecutionsDBLayer;
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
    DBLayerReporting dbLayerReporting;

    public DailyPlanAdjustment(SOSHibernateSession sosHibernateSession) throws Exception {
        dailyPlanDBLayer = new DailyPlanDBLayer(sosHibernateSession);
        dailyPlanTaskExecutionsDBLayer = new ReportTaskExecutionsDBLayer(dailyPlanDBLayer.getSession());
        dailyPlanTriggerDbLayer = new ReportTriggerDBLayer(dailyPlanDBLayer.getSession());
        dbLayerReporting = new DBLayerReporting(dailyPlanDBLayer.getSession());
    }

    public void beginTransaction() throws Exception {
        if (dailyPlanDBLayer.getSession() == null) {
            throw new Exception("session is null");
        }
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

    private boolean reportExecutionIsAssigned(DBItemReportTask dbItemReportTask) throws Exception {
        initHashMaps();
        return reportExecutions.get(dbItemReportTask.getId()) != null;
    }

    private boolean reportTriggerIsAssigned(DBItemReportTrigger dbItemReportTrigger) throws Exception {
        initHashMaps();
        return reportTriggers.get(dbItemReportTrigger.getId()) != null;
    }

    private void adjustDailyPlanStandaloneItem(DailyPlanWithReportExecutionDBItem dailyPlanWithReportExecutionDBItem,
            List<DBItemReportTask> reportTaskList) throws Exception {
        LOGGER.debug(String.format("%s records in reportTaskList", reportTaskList.size()));
        // dailyPlanWithReportExecutionDBItem.setExecutionState(null);
        // It can be late even it has never been startet
        boolean late = dailyPlanWithReportExecutionDBItem.getExecutionState().isLate();
        if (late && dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().isStandalone() && !dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem()
                .getIsLate() && !dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().getIsAssigned()) {
            dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().setIsLate(late);

            try {
                dailyPlanDBLayer.getSession().update(dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem());
                dailyPlanUpdated = true;
            } catch (SOSHibernateObjectOperationException e) {
            }
        }

        String actState = dailyPlanWithReportExecutionDBItem.getExecutionState().getState();
        String planState = dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().getState();

        if (!actState.equals(planState) && dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().isStandalone()
                && dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().getIsAssigned() && "INCOMPLETE".equals(planState)) {
            try {
                dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().setState(actState);
                dailyPlanDBLayer.getSession().update(dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem());
                dailyPlanUpdated = true;
            } catch (SOSHibernateObjectOperationException e) {
            }
        }

        for (int i = 0; i < reportTaskList.size(); i++) {
            DBItemReportTask dbItemReportTask = (DBItemReportTask) reportTaskList.get(i);
            if (!reportExecutionIsAssigned(dbItemReportTask) && dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().isStandalone()
                    && dailyPlanWithReportExecutionDBItem.isEqual(dbItemReportTask)) {
                LOGGER.debug(String.format("... assign %s to %s", dbItemReportTask.getId(), dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem()
                        .getJobName()));
                dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().setReportExecutionId(dbItemReportTask.getId());
                dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().setIsAssigned(true);
                dailyPlanWithReportExecutionDBItem.setDbItemReportExecution(reportTaskList.get(i));

                dailyPlanWithReportExecutionDBItem.setExecutionState(null);

                dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().setIsLate(dailyPlanWithReportExecutionDBItem.getExecutionState().isLate());
                dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().setState(dailyPlanWithReportExecutionDBItem.getExecutionState().getState());
                reportExecutions.put(dbItemReportTask.getId(), dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().getIdentifier());


                if (!dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().getPlannedStart().before(dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().getExpectedEnd())){
                    SOSDurations sosDurations = new SOSDurations();
                    SOSDuration sosDuration = new SOSDuration();
                    sosDuration.setStartTime(dbItemReportTask.getStartTime());
                    sosDuration.setEndTime(dbItemReportTask.getEndTime());
                    sosDurations.add(sosDuration);
                    dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().setExpectedEnd(new Date(dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().getPlannedStart().getTime() + sosDurations.average()));
                }
               

                try {
                    dailyPlanDBLayer.getSession().update(dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem());
                    dailyPlanUpdated = true;
                } catch (SOSHibernateObjectOperationException e) {
                }
                break;
            }
        }
    }

    private void adjustDailyPlanOrderItem(DailyPlanWithReportTriggerDBItem dailyPlanWithReportTriggerDBItem,
            List<DBItemReportTrigger> dbItemReportTriggerList) throws Exception {
        LOGGER.debug(String.format("%s records in dbItemReportTriggerList", dbItemReportTriggerList.size()));
        // It can be late even it has never been startet
        boolean late = dailyPlanWithReportTriggerDBItem.getExecutionState().isLate();
        if (late && dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().isOrderJob() && !dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem()
                .getIsLate() && !dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getIsAssigned()) {
            dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setIsLate(late);
            try {
                dailyPlanDBLayer.getSession().update(dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem());
                dailyPlanUpdated = true;
            } catch (SOSHibernateObjectOperationException e) {
            }
        }

        String actState = dailyPlanWithReportTriggerDBItem.getExecutionState().getState();
        String planState = dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getState();

        if (!actState.equals(planState) && dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().isOrderJob() && dailyPlanWithReportTriggerDBItem
                .getDailyPlanDbItem().getIsAssigned() && "INCOMPLETE".equals(dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getState())) {
            try {
                dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setState(dailyPlanWithReportTriggerDBItem.getExecutionState().getState());
                dailyPlanDBLayer.getSession().update(dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem());
                dailyPlanUpdated = true;
            } catch (SOSHibernateObjectOperationException e) {
            }
        }

        for (DBItemReportTrigger dbItemReportTrigger : dbItemReportTriggerList) {
            if (!reportTriggerIsAssigned(dbItemReportTrigger) && dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().isOrderJob()
                    && dailyPlanWithReportTriggerDBItem.isEqual(dbItemReportTrigger)) {
                LOGGER.debug(String.format("... assign %s to %s/%s", dbItemReportTrigger.getHistoryId(), dailyPlanWithReportTriggerDBItem
                        .getDailyPlanDbItem().getJobChainNotNull(), dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getOrderId()));
                dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setReportTriggerId(dbItemReportTrigger.getId());
                dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setIsAssigned(true);

                dailyPlanWithReportTriggerDBItem.setDbItemReportTrigger(dbItemReportTrigger);
                dailyPlanWithReportTriggerDBItem.setExecutionState(null);

                dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setIsLate(dailyPlanWithReportTriggerDBItem.getExecutionState().isLate());
                dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setState(dailyPlanWithReportTriggerDBItem.getExecutionState().getState());

                reportTriggers.put(dbItemReportTrigger.getId(), dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getIdentifier());

                if (!dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getPlannedStart().before(dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getExpectedEnd())){
                    SOSDurations sosDurations = new SOSDurations();
                    SOSDuration sosDuration = new SOSDuration();
                    sosDuration.setStartTime(dbItemReportTrigger.getStartTime());
                    sosDuration.setEndTime(dbItemReportTrigger.getEndTime());
                    sosDurations.add(sosDuration);
                    dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setExpectedEnd(new Date(dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getPlannedStart().getTime() + sosDurations.average()));
                }

                try {
                    try {
                        dailyPlanDBLayer.getSession().update(dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem());
                        dailyPlanUpdated = true;
                    } catch (SOSHibernateObjectOperationException e) {
                    }

                } catch (org.hibernate.StaleStateException e) {
                }
                break;
            }
        }
    }

    public void adjustWithHistory() throws Exception {

        try {
            beginTransaction();

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

            dailyPlanTaskExecutionsDBLayer.getFilter().setLimit(-1);
            dailyPlanTaskExecutionsDBLayer.getFilter().setExecutedFrom(from);
            dailyPlanTaskExecutionsDBLayer.getFilter().setExecutedTo(to);

            dailyPlanTriggerDbLayer.getFilter().setLimit(-1);
            dailyPlanTriggerDbLayer.getFilter().setExecutedFrom(from);
            dailyPlanTriggerDbLayer.getFilter().setExecutedTo(to);

            List<DBItemReportTask> dbItemReportTaskList = null;
            List<DBItemReportTrigger> dbItemReportTriggerList = null;

            commit();

            beginTransaction();

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
                DailyPlanWithReportExecutionDBItem dailyPlanWithReportExecutionItem = (DailyPlanWithReportExecutionDBItem) dailyPlanStandaloneList
                        .get(i);
                String jobName = dailyPlanWithReportExecutionItem.getDailyPlanDbItem().getJobName();
                if (!lastJob.equals(jobName)) {
                    dailyPlanTaskExecutionsDBLayer.getFilter().setSchedulerId(schedulerId);
                    dailyPlanTaskExecutionsDBLayer.getFilter().addJobPath(jobName);
                    dbItemReportTaskList = dailyPlanTaskExecutionsDBLayer.getSchedulerHistoryListFromTo();
                    lastJob = jobName;
                }
                adjustDailyPlanStandaloneItem(dailyPlanWithReportExecutionItem, dbItemReportTaskList);
            }

            commit();

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollback();
            throw e;
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