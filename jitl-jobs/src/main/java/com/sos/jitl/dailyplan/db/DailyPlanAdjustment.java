package com.sos.jitl.dailyplan.db;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

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

    public DailyPlanAdjustment(File configurationFile) throws Exception {
        dailyPlanDBLayer = new DailyPlanDBLayer(configurationFile);
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

    private void adjustDailyPlanStandaloneItem(DailyPlanWithReportExecutionDBItem dailyPlanItem, List<DBItemReportExecution> reportExecutionList) throws Exception {
        LOGGER.debug(String.format("%s records in reportExecutionList", reportExecutionList.size()));
        dailyPlanItem.getDailyPlanDbItem().setIsLate(dailyPlanItem.getExecutionState().isLate());
        dailyPlanItem.getDailyPlanDbItem().setState(dailyPlanItem.getExecutionState().getState());
        for (int i = 0; i < reportExecutionList.size(); i++) {
            DBItemReportExecution dbItemReportExecution = (DBItemReportExecution) reportExecutionList.get(i);
            if (!dbItemReportExecution.isAssignToDaysScheduler() && dailyPlanItem.getDailyPlanDbItem().isStandalone() && dailyPlanItem.isEqual(dbItemReportExecution)) {
                LOGGER.debug(String.format("... assign %s to %s", dbItemReportExecution.getId(), dailyPlanItem.getDailyPlanDbItem().getJobName()));
                dailyPlanItem.getDailyPlanDbItem().setReportExecutionId(dbItemReportExecution.getId());
                dailyPlanItem.getDailyPlanDbItem().setIsAssigned(true);
                dailyPlanDBLayer.getConnection().update(dailyPlanItem.getDailyPlanDbItem());
                dailyPlanItem.setDbItemReportExecution(reportExecutionList.get(i));

                dailyPlanItem.setExecutionState(null);

                dailyPlanItem.getDailyPlanDbItem().setIsLate(dailyPlanItem.getExecutionState().isLate());
                dailyPlanItem.getDailyPlanDbItem().setState(dailyPlanItem.getExecutionState().getState());
                dbItemReportExecution.setAssignToDaysScheduler(true);
                break;
            }
        }
        LOGGER.debug(String.format("... could not assign %s planned at:%s", dailyPlanItem.getDailyPlanDbItem().getJobName(), dailyPlanItem.getDailyPlanDbItem()
                .getPlannedStartFormated()));
    }

    private void adjustDailyPlanOrderItem(DailyPlanWithReportTriggerDBItem dailyPlanItem, List<DBItemReportTriggerWithResult> dbItemReportTriggerList) throws Exception {
        LOGGER.debug(String.format("%s records in dbItemReportTriggerList", dbItemReportTriggerList.size()));
        dailyPlanItem.getDailyPlanDbItem().setIsLate(dailyPlanItem.getExecutionState().isLate());
        dailyPlanItem.getDailyPlanDbItem().setState(dailyPlanItem.getExecutionState().getState());
        for (int i = 0; i < dbItemReportTriggerList.size(); i++) {
            DBItemReportTriggerWithResult dbItemReportTriggerWithResult = (DBItemReportTriggerWithResult) dbItemReportTriggerList.get(i);
            if (dbItemReportTriggerWithResult.getDbItemReportTrigger().getEndTime() != null && !dbItemReportTriggerWithResult.getDbItemReportTrigger().isAssignToDaysScheduler()
                    && dailyPlanItem.getDailyPlanDbItem().isOrderJob() && dailyPlanItem.isEqual(dbItemReportTriggerWithResult.getDbItemReportTrigger())) {
                LOGGER.debug(String.format("... assign %s to %s/%s", dbItemReportTriggerWithResult.getDbItemReportTrigger().getHistoryId(), dailyPlanItem.getDailyPlanDbItem()
                        .getJobChainNotNull(), dailyPlanItem.getDailyPlanDbItem().getOrderId()));
                dailyPlanItem.getDailyPlanDbItem().setReportTriggerId(dbItemReportTriggerWithResult.getDbItemReportTrigger().getId());
                dailyPlanItem.getDailyPlanDbItem().setIsAssigned(true);
                dailyPlanDBLayer.getConnection().update(dailyPlanItem.getDailyPlanDbItem());

                dailyPlanItem.setDbItemReportTrigger(dbItemReportTriggerList.get(i).getDbItemReportTrigger());
                dailyPlanItem.setDbItemReportTriggerResult(dbItemReportTriggerList.get(i).getDbItemReportTriggerResult());
                dailyPlanItem.setExecutionState(null);
                Session session = (Session) dailyPlanDBLayer.getConnection().getCurrentSession();
                session.refresh(dailyPlanItem.getDailyPlanDbItem());

                dailyPlanItem.getDailyPlanDbItem().setIsLate(dailyPlanItem.getExecutionState().isLate());
                dailyPlanItem.getDailyPlanDbItem().setState(dailyPlanItem.getExecutionState().getState());

                dbItemReportTriggerWithResult.getDbItemReportTrigger().setAssignToDaysScheduler(true);
                break;
            }
        }

        if (!dailyPlanItem.getDailyPlanDbItem().getIsAssigned()) {
            for (int i = 0; i < dbItemReportTriggerList.size(); i++) {
                DBItemReportTriggerWithResult dbItemReportTriggerWithResult = (DBItemReportTriggerWithResult) dbItemReportTriggerList.get(i);
                if (dbItemReportTriggerWithResult.getDbItemReportTrigger().getEndTime() == null && !dbItemReportTriggerWithResult.getDbItemReportTrigger().isAssignToDaysScheduler()
                        && dailyPlanItem.getDailyPlanDbItem().isOrderJob() && dailyPlanItem.isEqual(dbItemReportTriggerWithResult.getDbItemReportTrigger())) {
                    LOGGER.debug(String.format("... assign %s to %s/%s", dbItemReportTriggerWithResult.getDbItemReportTrigger().getHistoryId(), dailyPlanItem.getDailyPlanDbItem()
                            .getJobChainNotNull(), dailyPlanItem.getDailyPlanDbItem().getOrderId()));
                    dailyPlanItem.getDailyPlanDbItem().setReportTriggerId(dbItemReportTriggerWithResult.getDbItemReportTrigger().getId());
                    dailyPlanItem.getDailyPlanDbItem().setIsAssigned(true);
                    dailyPlanDBLayer.getConnection().update(dailyPlanItem);

                    dailyPlanItem.setExecutionState(null);
                    Session session = (Session) dailyPlanDBLayer.getConnection().getCurrentSession();
                    session.refresh(dailyPlanItem);

                    dailyPlanItem.getDailyPlanDbItem().setIsLate(dailyPlanItem.getExecutionState().isLate());
                    dailyPlanItem.getDailyPlanDbItem().setState(dailyPlanItem.getExecutionState().getState());

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
            DailyPlanWithReportTriggerDBItem dailyPlanItem = (DailyPlanWithReportTriggerDBItem) dailyPlanOrderList.get(i);
            String schedulerId = dailyPlanItem.getDailyPlanDbItem().getSchedulerId();
            if (dbItemReportTriggerWithResultList == null || !schedulerId.equals(lastSchedulerId)) {
                commit();
                beginTransaction();
                dailyPlanTriggerDbLayer.getFilter().setSchedulerId(schedulerId);
                dbItemReportTriggerWithResultList = dailyPlanTriggerDbLayer.getSchedulerOrderHistoryListFromTo();
                LOGGER.debug(String.format("... Reading scheduler_id: %s", schedulerId));
                lastSchedulerId = schedulerId;
            }
            adjustDailyPlanOrderItem(dailyPlanItem, dbItemReportTriggerWithResultList);
        }

        for (int i = 0; i < dailyPlanStandaloneList.size(); i++) {
            DailyPlanWithReportExecutionDBItem dailyPlanItem = (DailyPlanWithReportExecutionDBItem) dailyPlanStandaloneList.get(i);
            String schedulerId = dailyPlanItem.getDailyPlanDbItem().getSchedulerId();
            if (dbItemReportExecutionList == null || !schedulerId.equals(lastSchedulerId)) {
                dailyPlanDBLayer.getConnection().commit();
                beginTransaction();
                dailyPlanExecutionsDBLayer.getFilter().setSchedulerId(schedulerId);
                dbItemReportExecutionList = dailyPlanExecutionsDBLayer.getSchedulerHistoryListFromTo();
                lastSchedulerId = schedulerId;
                LOGGER.debug(String.format("... Reading scheduler_id: %s", schedulerId));
            }
            adjustDailyPlanStandaloneItem(dailyPlanItem, dbItemReportExecutionList);
        }

    }

    private void setFrom() throws ParseException {
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
    }

    private void setTo() throws ParseException {
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

}