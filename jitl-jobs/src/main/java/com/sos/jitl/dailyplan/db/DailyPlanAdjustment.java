package com.sos.jitl.dailyplan.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.UtcTimeHelper;
import com.sos.jitl.dailyplan.job.CheckDailyPlanOptions;
import com.sos.jitl.reporting.db.DBItemReportExecution;
import com.sos.jitl.reporting.db.DBItemReportTriggerWithResult;
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
    private boolean dailyPlanUpdated=false;

    public DailyPlanAdjustment(SOSHibernateSession connection) throws Exception {
        dailyPlanDBLayer = new DailyPlanDBLayer(connection);
        dailyPlanExecutionsDBLayer = new ReportExecutionsDBLayer(dailyPlanDBLayer.getConnection());
        dailyPlanTriggerDbLayer = new ReportTriggerDBLayer(dailyPlanDBLayer.getConnection());
    }

    public void beginTransaction() throws Exception {
        dailyPlanDBLayer.getConnection().beginTransaction();
    }

    public void commit() throws Exception {
        dailyPlanDBLayer.getConnection().commit();
    }

    public void rollback() throws Exception {
        dailyPlanDBLayer.getConnection().rollback();
    }

    public void disconnect() throws Exception {
        dailyPlanDBLayer.getConnection().disconnect();
    }

    private void adjustDailyPlanStandaloneItem(DailyPlanWithReportExecutionDBItem dailyPlanWithReportExecutionDBItem, List<DBItemReportExecution> reportExecutionList)
            throws Exception {
        LOGGER.debug(String.format("%s records in reportExecutionList", reportExecutionList.size()));
        dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().setIsLate(dailyPlanWithReportExecutionDBItem.getExecutionState().isLate());
        dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().setState(dailyPlanWithReportExecutionDBItem.getExecutionState().getState());
        for (int i = 0; i < reportExecutionList.size(); i++) {
            DBItemReportExecution dbItemReportExecution = (DBItemReportExecution) reportExecutionList.get(i);
            // It can be late even it has never been startet
            if (!dbItemReportExecution.isAssignToDaysScheduler() && dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().isStandalone()) {
                dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().setIsLate(dailyPlanWithReportExecutionDBItem.getExecutionState().isLate());

                try {
                    dailyPlanDBLayer.getConnection().update(dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem());
                } catch (org.hibernate.StaleStateException e) {
                }

            }
            if (!dbItemReportExecution.isAssignToDaysScheduler() && dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().isStandalone() && dailyPlanWithReportExecutionDBItem
                    .isEqual(dbItemReportExecution)) {
                LOGGER.debug(String.format("... assign %s to %s", dbItemReportExecution.getId(), dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().getJobName()));
                dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().setReportExecutionId(dbItemReportExecution.getId());
                dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().setIsAssigned(true);
                dailyPlanWithReportExecutionDBItem.setDbItemReportExecution(reportExecutionList.get(i));

                dailyPlanWithReportExecutionDBItem.setExecutionState(null);

                dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().setIsLate(dailyPlanWithReportExecutionDBItem.getExecutionState().isLate());
                dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().setState(dailyPlanWithReportExecutionDBItem.getExecutionState().getState());
                dbItemReportExecution.setAssignToDaysScheduler(true);

                try {
                    dailyPlanDBLayer.getConnection().update(dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem());
                    dailyPlanUpdated = true;
                } catch (org.hibernate.StaleStateException e) {
                }
                break;
            }
        }
        LOGGER.debug(String.format("... could not assign %s planned at:%s", dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().getJobName(), dailyPlanWithReportExecutionDBItem
                .getDailyPlanDbItem().getPlannedStartFormated()));
    }

    private void adjustDailyPlanOrderItem(DailyPlanWithReportTriggerDBItem dailyPlanWithReportTriggerDBItem, List<DBItemReportTriggerWithResult> dbItemReportTriggerList)
            throws Exception {
        LOGGER.debug(String.format("%s records in dbItemReportTriggerList", dbItemReportTriggerList.size()));
        dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setIsLate(dailyPlanWithReportTriggerDBItem.getExecutionState().isLate());
        dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setState(dailyPlanWithReportTriggerDBItem.getExecutionState().getState());
        for (int i = 0; i < dbItemReportTriggerList.size(); i++) {
            DBItemReportTriggerWithResult dbItemReportTriggerWithResult = (DBItemReportTriggerWithResult) dbItemReportTriggerList.get(i);
            // It can be late even it has never been startet
            if (dbItemReportTriggerWithResult.getDbItemReportTrigger().getEndTime() != null && !dbItemReportTriggerWithResult.getDbItemReportTrigger().isAssignToDaysScheduler()
                    && dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().isOrderJob()) {
                dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setIsLate(dailyPlanWithReportTriggerDBItem.getExecutionState().isLate());
                try {
                    dailyPlanDBLayer.getConnection().update(dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem());
                } catch (org.hibernate.StaleStateException e) {
                }
            }
            if (dbItemReportTriggerWithResult.getDbItemReportTrigger().getEndTime() != null && !dbItemReportTriggerWithResult.getDbItemReportTrigger().isAssignToDaysScheduler()
                    && dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().isOrderJob() && dailyPlanWithReportTriggerDBItem.isEqual(dbItemReportTriggerWithResult
                            .getDbItemReportTrigger())) {
                LOGGER.debug(String.format("... assign %s to %s/%s", dbItemReportTriggerWithResult.getDbItemReportTrigger().getHistoryId(), dailyPlanWithReportTriggerDBItem
                        .getDailyPlanDbItem().getJobChainNotNull(), dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getOrderId()));
                dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setReportTriggerId(dbItemReportTriggerWithResult.getDbItemReportTrigger().getId());
                dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setIsAssigned(true);

                dailyPlanWithReportTriggerDBItem.setDbItemReportTrigger(dbItemReportTriggerList.get(i).getDbItemReportTrigger());
                dailyPlanWithReportTriggerDBItem.setDbItemReportTriggerResult(dbItemReportTriggerList.get(i).getDbItemReportTriggerResult());
                dailyPlanWithReportTriggerDBItem.setExecutionState(null);

                dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setIsLate(dailyPlanWithReportTriggerDBItem.getExecutionState().isLate());
                dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setState(dailyPlanWithReportTriggerDBItem.getExecutionState().getState());

                dbItemReportTriggerWithResult.getDbItemReportTrigger().setAssignToDaysScheduler(true);
                try {
                    try {
                        dailyPlanDBLayer.getConnection().update(dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem());
                        dailyPlanUpdated = true;
                    } catch (org.hibernate.StaleStateException e) {
                    }

                } catch (org.hibernate.StaleStateException e) {
                }
                break;
            }
        }

        if (!dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getIsAssigned()) {
            for (int i = 0; i < dbItemReportTriggerList.size(); i++) {
                DBItemReportTriggerWithResult dbItemReportTriggerWithResult = (DBItemReportTriggerWithResult) dbItemReportTriggerList.get(i);
                if (dbItemReportTriggerWithResult.getDbItemReportTrigger().getEndTime() == null && !dbItemReportTriggerWithResult.getDbItemReportTrigger().isAssignToDaysScheduler()
                        && dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().isOrderJob() && dailyPlanWithReportTriggerDBItem.isEqual(dbItemReportTriggerWithResult
                                .getDbItemReportTrigger())) {
                    LOGGER.debug(String.format("... assign %s to %s/%s", dbItemReportTriggerWithResult.getDbItemReportTrigger().getHistoryId(), dailyPlanWithReportTriggerDBItem
                            .getDailyPlanDbItem().getJobChainNotNull(), dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getOrderId()));
                    dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setReportTriggerId(dbItemReportTriggerWithResult.getDbItemReportTrigger().getId());
                    dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setIsAssigned(true);
                    try {
                        dailyPlanDBLayer.getConnection().update(dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem());
                    } catch (org.hibernate.StaleStateException e) {
                    }

                    dailyPlanWithReportTriggerDBItem.setExecutionState(null);

                    dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setIsLate(dailyPlanWithReportTriggerDBItem.getExecutionState().isLate());
                    dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setState(dailyPlanWithReportTriggerDBItem.getExecutionState().getState());

                    dbItemReportTriggerWithResult.getDbItemReportTrigger().setAssignToDaysScheduler(true);
                    break;
                }
            }
        }

    }

    public void adjustWithHistory() throws Exception {
        String toTimeZoneString = "UTC";
        String fromTimeZoneString = DateTimeZone.getDefault().getID();

        from = UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, new DateTime(from));
        to = UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, new DateTime(to));
        LOGGER.debug(String.format("reading from: %s to %s", from, to));

        String lastSchedulerId = "***";
        dailyPlanDBLayer.setWhereSchedulerId(this.schedulerId);
        dailyPlanDBLayer.setWhereFrom(from);
        dailyPlanDBLayer.setWhereTo(to);
        dailyPlanDBLayer.getFilter().setOrderCriteria("p.plannedStart");
        dailyPlanDBLayer.getFilter().setSortMode("desc");
        List<DailyPlanWithReportTriggerDBItem> dailyPlanOrderList = dailyPlanDBLayer.getWaitingDailyPlanOrderList(-1);
        List<DailyPlanWithReportExecutionDBItem> dailyPlanStandaloneList = dailyPlanDBLayer.getWaitingDailyPlanStandaloneList(-1);

        dailyPlanExecutionsDBLayer.getFilter().setLimit(-1);
        dailyPlanExecutionsDBLayer.getFilter().setExecutedFrom(from);
        dailyPlanExecutionsDBLayer.getFilter().setExecutedTo(to);
        dailyPlanTriggerDbLayer.getFilter().setLimit(-1);
        dailyPlanTriggerDbLayer.getFilter().setExecutedFrom(from);
        dailyPlanTriggerDbLayer.getFilter().setExecutedTo(to);

        List<DBItemReportExecution> dbItemReportExecutionList = null;
        List<DBItemReportTriggerWithResult> dbItemReportTriggerWithResultList = null;

        for (int i = 0; i < dailyPlanOrderList.size(); i++) {
            DailyPlanWithReportTriggerDBItem dailyPlanWithReportTriggerItem = (DailyPlanWithReportTriggerDBItem) dailyPlanOrderList.get(i);
            String schedulerId = dailyPlanWithReportTriggerItem.getDailyPlanDbItem().getSchedulerId();
            if (dbItemReportTriggerWithResultList == null || !schedulerId.equals(lastSchedulerId)) {
                commit();
                beginTransaction();
                dailyPlanTriggerDbLayer.getFilter().setSchedulerId(schedulerId);
                dbItemReportTriggerWithResultList = dailyPlanTriggerDbLayer.getSchedulerOrderHistoryListFromTo();
                LOGGER.debug(String.format("... Reading scheduler_id: %s", schedulerId));
                lastSchedulerId = schedulerId;
            }
            adjustDailyPlanOrderItem(dailyPlanWithReportTriggerItem, dbItemReportTriggerWithResultList);
        }

        for (int i = 0; i < dailyPlanStandaloneList.size(); i++) {
            DailyPlanWithReportExecutionDBItem dailyPlanWithReportExecutionItem = (DailyPlanWithReportExecutionDBItem) dailyPlanStandaloneList.get(i);
            String schedulerId = dailyPlanWithReportExecutionItem.getDailyPlanDbItem().getSchedulerId();
            if (dbItemReportExecutionList == null || !schedulerId.equals(lastSchedulerId)) {
                dailyPlanDBLayer.getConnection().commit();
                beginTransaction();
                dailyPlanExecutionsDBLayer.getFilter().setSchedulerId(schedulerId);
                dbItemReportExecutionList = dailyPlanExecutionsDBLayer.getSchedulerHistoryListFromTo();
                lastSchedulerId = schedulerId;
                LOGGER.debug(String.format("... Reading scheduler_id: %s", schedulerId));
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