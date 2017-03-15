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
                    dailyPlanDBLayer.getSession().update(dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem());
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
                    dailyPlanDBLayer.getSession().update(dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem());
                    dailyPlanUpdated = true;
                } catch (org.hibernate.StaleStateException e) {
                }
                break;
            }
        }
        LOGGER.debug(String.format("... could not assign %s planned at:%s", dailyPlanWithReportExecutionDBItem.getDailyPlanDbItem().getJobName(), dailyPlanWithReportExecutionDBItem
                .getDailyPlanDbItem().getPlannedStartFormated()));
    }

    private void adjustDailyPlanOrderItem(DailyPlanWithReportTriggerDBItem dailyPlanWithReportTriggerDBItem, List<DBItemReportTrigger> dbItemReportTriggerList)
            throws Exception {
        LOGGER.debug(String.format("%s records in dbItemReportTriggerList", dbItemReportTriggerList.size()));
        dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setIsLate(dailyPlanWithReportTriggerDBItem.getExecutionState().isLate());
        dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setState(dailyPlanWithReportTriggerDBItem.getExecutionState().getState());
        for (int i = 0; i < dbItemReportTriggerList.size(); i++) {
            DBItemReportTrigger DBItemReportTrigger = (DBItemReportTrigger) dbItemReportTriggerList.get(i);
            // It can be late even it has never been startet
            if (DBItemReportTrigger.getEndTime() != null && !DBItemReportTrigger.isAssignToDaysScheduler()
                    && dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().isOrderJob()) {
                dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setIsLate(dailyPlanWithReportTriggerDBItem.getExecutionState().isLate());
                try {
                    dailyPlanDBLayer.getSession().update(dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem());
                } catch (org.hibernate.StaleStateException e) {
                }
            }
            if (DBItemReportTrigger.getEndTime() != null && !DBItemReportTrigger.isAssignToDaysScheduler()
                    && dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().isOrderJob() && dailyPlanWithReportTriggerDBItem.isEqual(DBItemReportTrigger)) {
                LOGGER.debug(String.format("... assign %s to %s/%s", DBItemReportTrigger.getHistoryId(), dailyPlanWithReportTriggerDBItem
                        .getDailyPlanDbItem().getJobChainNotNull(), dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getOrderId()));
                dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setReportTriggerId(DBItemReportTrigger.getId());
                dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setIsAssigned(true);

                dailyPlanWithReportTriggerDBItem.setDbItemReportTrigger(dbItemReportTriggerList.get(i));
                dailyPlanWithReportTriggerDBItem.setDbItemReportTriggerResult(dbItemReportTriggerList.get(i));
                dailyPlanWithReportTriggerDBItem.setExecutionState(null);

                dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setIsLate(dailyPlanWithReportTriggerDBItem.getExecutionState().isLate());
                dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setState(dailyPlanWithReportTriggerDBItem.getExecutionState().getState());

                DBItemReportTrigger.setAssignToDaysScheduler(true);
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

        if (!dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getIsAssigned()) {
            for (int i = 0; i < dbItemReportTriggerList.size(); i++) {
                DBItemReportTrigger DBItemReportTrigger = (DBItemReportTrigger) dbItemReportTriggerList.get(i);
                if (DBItemReportTrigger.getEndTime() == null && !DBItemReportTrigger.isAssignToDaysScheduler()
                        && dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().isOrderJob() && dailyPlanWithReportTriggerDBItem.isEqual(DBItemReportTrigger)) {
                    LOGGER.debug(String.format("... assign %s to %s/%s", DBItemReportTrigger.getHistoryId(), dailyPlanWithReportTriggerDBItem
                            .getDailyPlanDbItem().getJobChainNotNull(), dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().getOrderId()));
                    dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setReportTriggerId(DBItemReportTrigger.getId());
                    dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setIsAssigned(true);
                    try {
                        dailyPlanDBLayer.getSession().update(dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem());
                    } catch (org.hibernate.StaleStateException e) {
                    }

                    dailyPlanWithReportTriggerDBItem.setExecutionState(null);

                    dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setIsLate(dailyPlanWithReportTriggerDBItem.getExecutionState().isLate());
                    dailyPlanWithReportTriggerDBItem.getDailyPlanDbItem().setState(dailyPlanWithReportTriggerDBItem.getExecutionState().getState());

                    DBItemReportTrigger.setAssignToDaysScheduler(true);
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

        //String lastSchedulerId = "***";
        dailyPlanDBLayer.setWhereSchedulerId(this.schedulerId);
        dailyPlanDBLayer.setWhereFrom(from);
        dailyPlanDBLayer.setWhereTo(to);
        dailyPlanDBLayer.getFilter().setOrderCriteria("p.plannedStart");
        dailyPlanDBLayer.getFilter().setSortMode("desc");
        List<DailyPlanWithReportTriggerDBItem> dailyPlanOrderList = dailyPlanDBLayer.getWaitingDailyPlanOrderList(-1);
        List<DailyPlanWithReportExecutionDBItem> dailyPlanStandaloneList = dailyPlanDBLayer.getWaitingDailyPlanStandaloneList(-1);

        dailyPlanExecutionsDBLayer.getFilter().setLimit(1);
        dailyPlanExecutionsDBLayer.getFilter().setExecutedFrom(from);
        dailyPlanExecutionsDBLayer.getFilter().setExecutedTo(to);
        
        dailyPlanTriggerDbLayer.getFilter().setLimit(1);
        dailyPlanTriggerDbLayer.getFilter().setExecutedFrom(from);
        dailyPlanTriggerDbLayer.getFilter().setExecutedTo(to);

        List<DBItemReportExecution> dbItemReportExecutionList = null;
        List<DBItemReportTrigger> DBItemReportTriggerList = null;

        for (int i = 0; i < dailyPlanOrderList.size(); i++) {
            DailyPlanWithReportTriggerDBItem dailyPlanWithReportTriggerItem = (DailyPlanWithReportTriggerDBItem) dailyPlanOrderList.get(i);
            dailyPlanTriggerDbLayer.getFilter().setSchedulerId(schedulerId);
            dailyPlanTriggerDbLayer.getFilter().setOrderId(dailyPlanWithReportTriggerItem.getDailyPlanDbItem().getOrderId());
            dailyPlanTriggerDbLayer.getFilter().setJobChain(dailyPlanWithReportTriggerItem.getDailyPlanDbItem().getJobChain());

            DBItemReportTriggerList = dailyPlanTriggerDbLayer.getSchedulerOrderHistoryListFromTo();
            adjustDailyPlanOrderItem(dailyPlanWithReportTriggerItem, DBItemReportTriggerList);
        }

        for (int i = 0; i < dailyPlanStandaloneList.size(); i++) {
            DailyPlanWithReportExecutionDBItem dailyPlanWithReportExecutionItem = (DailyPlanWithReportExecutionDBItem) dailyPlanStandaloneList.get(i);
            dailyPlanExecutionsDBLayer.getFilter().setSchedulerId(schedulerId);
            dailyPlanExecutionsDBLayer.getFilter().addJobPath(dailyPlanWithReportExecutionItem.getDailyPlanDbItem().getJobName());
            dbItemReportExecutionList = dailyPlanExecutionsDBLayer.getSchedulerHistoryListFromTo();
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