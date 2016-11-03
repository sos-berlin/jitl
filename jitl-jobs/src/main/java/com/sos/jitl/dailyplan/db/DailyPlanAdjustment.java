package com.sos.jitl.dailyplan.db;

import com.sos.dashboard.globals.DashBoardConstants;
import com.sos.jitl.dailyplan.job.CheckDailyPlanOptions;
import com.sos.scheduler.history.db.SchedulerOrderHistoryDBItem;
import com.sos.scheduler.history.db.SchedulerOrderHistoryDBLayer;
import com.sos.scheduler.history.db.SchedulerTaskHistoryDBItem;
import com.sos.scheduler.history.db.SchedulerTaskHistoryDBLayer;

import org.apache.log4j.Logger;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class DailyPlanAdjustment {

    private static final Logger LOGGER = Logger.getLogger(DailyPlanAdjustment.class);
    private DailyPlanDBLayer dailyPlanDBLayer;
    private SchedulerTaskHistoryDBLayer schedulerTaskHistoryDBLayer;
    private SchedulerOrderHistoryDBLayer schedulerOrderHistoryDBLayer;
    private String dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
    private String schedulerId;
    private Date from;
    private Date to;
    private int dayOffset;
    private CheckDailyPlanOptions options = null;

    public DailyPlanAdjustment(File configurationFile) {
        dailyPlanDBLayer = new DailyPlanDBLayer(configurationFile);
        schedulerTaskHistoryDBLayer = new SchedulerTaskHistoryDBLayer(configurationFile);
        schedulerOrderHistoryDBLayer = new SchedulerOrderHistoryDBLayer(configurationFile);
    }

    private void adjustDaysScheduleItem(DailyPlanDBItem dailyPlanItem, List<SchedulerTaskHistoryDBItem> schedulerHistoryList) throws Exception {
        LOGGER.debug(String.format("%s records in schedulerHistoryList", schedulerHistoryList.size()));
        for (int i = 0; i < schedulerHistoryList.size(); i++) {
            SchedulerTaskHistoryDBItem schedulerHistoryDBItem = (SchedulerTaskHistoryDBItem) schedulerHistoryList.get(i);
            if (!schedulerHistoryDBItem.isAssignToDaysScheduler() && dailyPlanItem.isStandalone()
                    && dailyPlanItem.isEqual(schedulerHistoryDBItem)) {
                LOGGER.debug(String.format("... assign %s to %s", schedulerHistoryDBItem.getId(), dailyPlanItem.getJobName()));
                dailyPlanItem.setReportExecutionId(schedulerHistoryDBItem.getId());
                dailyPlanItem.setStatus(DashBoardConstants.STATUS_ASSIGNED);
                dailyPlanDBLayer.getConnection().update(dailyPlanItem);
                schedulerHistoryDBItem.setAssignToDaysScheduler(true);
                break;
            }
        }
        LOGGER.debug(String.format("... could not assign %s planned at:%s", dailyPlanItem.getJobName(), 
                dailyPlanItem.getSchedulePlannedFormated()));
    }

    private void adjustDaysScheduleOrderItem(DailyPlanDBItem dailyPlanItem, List<SchedulerOrderHistoryDBItem> schedulerOrderHistoryList) throws Exception {
        if (dailyPlanDBLayer.getConnection() == null) {
            dailyPlanDBLayer.initConnection(dailyPlanDBLayer.getConfigurationFileName());
        }
        LOGGER.debug(String.format("%s records in schedulerOrderHistoryList", schedulerOrderHistoryList.size()));
        for (int i = 0; i < schedulerOrderHistoryList.size(); i++) {
            SchedulerOrderHistoryDBItem schedulerOrderHistoryDBItem = (SchedulerOrderHistoryDBItem) schedulerOrderHistoryList.get(i);
            if (!schedulerOrderHistoryDBItem.isAssignToDaysScheduler() && dailyPlanItem.isOrderJob()
                    && dailyPlanItem.isEqual(schedulerOrderHistoryDBItem)) {
                LOGGER.debug(String.format("... assign %s to %s/%s", schedulerOrderHistoryDBItem.getHistoryId(), 
                        dailyPlanItem.getJobChainNotNull(), dailyPlanItem.getOrderId()));
                dailyPlanItem.setReportTriggerId(schedulerOrderHistoryDBItem.getHistoryId());
                dailyPlanItem.setStatus(DashBoardConstants.STATUS_ASSIGNED);
                dailyPlanDBLayer.getConnection().update(dailyPlanItem);
                dailyPlanDBLayer.getConnection().commit();
                schedulerOrderHistoryDBItem.setAssignToDaysScheduler(true);
                break;
            }
        }
    }

    public void adjustWithHistory() throws Exception {
        String lastSchedulerId = "***";
        dailyPlanDBLayer.setWhereSchedulerId(this.schedulerId);
        dailyPlanDBLayer.getFilter().setOrderCriteria("schedulerId");
        dailyPlanDBLayer.setWhereFrom(from);
        dailyPlanDBLayer.setWhereTo(to);
        List<DailyPlanDBItem> dailyScheduleList = dailyPlanDBLayer.getWaitingDailyPlanList(-1);
        schedulerTaskHistoryDBLayer.getFilter().setLimit(-1);
        schedulerTaskHistoryDBLayer.getFilter().setExecutedFrom(from);
        schedulerTaskHistoryDBLayer.getFilter().setExecutedTo(dailyPlanDBLayer.getWhereUtcTo());
        schedulerOrderHistoryDBLayer.getFilter().setLimit(-1);
        schedulerOrderHistoryDBLayer.getFilter().setExecutedFrom(from);
        schedulerOrderHistoryDBLayer.getFilter().setExecutedTo(dailyPlanDBLayer.getWhereUtcTo());
        try {
            dailyPlanDBLayer.getConnection().beginTransaction();
            List<SchedulerTaskHistoryDBItem> schedulerHistoryList = null;
            List<SchedulerOrderHistoryDBItem> schedulerOrderHistoryList = null;
            for (int i = 0; i < dailyScheduleList.size(); i++) {
                DailyPlanDBItem daysScheduleItem = (DailyPlanDBItem) dailyScheduleList.get(i);
                String schedulerId = daysScheduleItem.getSchedulerId();
                if (daysScheduleItem.isStandalone()) {
                    if (schedulerHistoryList == null || !schedulerId.equals(lastSchedulerId)) {
                        dailyPlanDBLayer.getConnection().commit();
                        dailyPlanDBLayer.getConnection().connect();
                        dailyPlanDBLayer.getConnection().beginTransaction();
                        schedulerTaskHistoryDBLayer.getFilter().setSchedulerId(schedulerId);
                        schedulerHistoryList = schedulerTaskHistoryDBLayer.getUnassignedSchedulerHistoryListFromTo();
                        lastSchedulerId = schedulerId;
                    LOGGER.debug(String.format("... Reading scheduler_id: %s", schedulerId));
                    }
                    adjustDaysScheduleItem(daysScheduleItem, schedulerHistoryList);
                } else {
                    if (schedulerOrderHistoryList == null || !schedulerId.equals(lastSchedulerId)) {
                        dailyPlanDBLayer.getConnection().commit();
                        dailyPlanDBLayer.getConnection().connect();
                        dailyPlanDBLayer.getConnection().beginTransaction();
                        schedulerOrderHistoryDBLayer.getFilter().setSchedulerId(schedulerId);
                        schedulerOrderHistoryList = schedulerOrderHistoryDBLayer.getUnassignedSchedulerOrderHistoryListFromTo();
                        LOGGER.debug(String.format("... Reading scheduler_id: %s", schedulerId));
                        lastSchedulerId = schedulerId;
                    }
                    adjustDaysScheduleOrderItem(daysScheduleItem, schedulerOrderHistoryList);
                }
            }
            dailyPlanDBLayer.getConnection().commit();
        } catch (Exception e) {
            LOGGER.error("Error occurred adjusting the history: " + e.getMessage(), e);
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

    public DailyPlanDBLayer getDailyScheduleDBLayer() {
        return dailyPlanDBLayer;
    }

    public void setDailyScheduleDBLayer(DailyPlanDBLayer dailyScheduleDBLayer) {
        this.dailyPlanDBLayer = dailyScheduleDBLayer;
    }

    public void setTo(Date to) {
        this.to = to;
    }

}