package com.sos.jitl.dailyplan.db;

import com.sos.jitl.dailyplan.job.CheckDailyPlanOptions;
import com.sos.jitl.reporting.db.DBItemReportExecution;
import com.sos.jitl.reporting.db.DBItemReportTrigger;

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
    private ReportExecutionsDBLayer dailyPlanExecutionsDBLayer;
    private ReportTriggerDBLayer dailyPlanTriggerDbLayer ;
    private String dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
    private String schedulerId;
    private Date from;
    private Date to;
    private int dayOffset;
    private CheckDailyPlanOptions options = null;

    public DailyPlanAdjustment(File configurationFile) {
        dailyPlanDBLayer = new DailyPlanDBLayer(configurationFile);
        dailyPlanExecutionsDBLayer = new ReportExecutionsDBLayer(dailyPlanDBLayer.getConnection());
        dailyPlanTriggerDbLayer = new ReportTriggerDBLayer(dailyPlanDBLayer.getConnection());
    }

    private void adjustDailyPlanItem(DailyPlanDBItem dailyPlanItem, List<DBItemReportExecution> reportExecutionList) throws Exception {
        LOGGER.debug(String.format("%s records in reportExecutionList", reportExecutionList.size()));
        dailyPlanItem.setIsLate(dailyPlanItem.getExecutionState().isLate());
        dailyPlanItem.setState(dailyPlanItem.getExecutionState().getState());
        for (int i = 0; i < reportExecutionList.size(); i++) {
            DBItemReportExecution dbItemReportExecution = (DBItemReportExecution) reportExecutionList.get(i);
            if (!dbItemReportExecution.isAssignToDaysScheduler() && dailyPlanItem.isStandalone()
                    && dailyPlanItem.isEqual(dbItemReportExecution)) {
                LOGGER.debug(String.format("... assign %s to %s", dbItemReportExecution.getId(), dailyPlanItem.getJobName()));
                dailyPlanItem.setReportExecutionId(dbItemReportExecution.getId());
                dailyPlanItem.setIsAssigned(true);
                dailyPlanDBLayer.getConnection().update(dailyPlanItem);
                dbItemReportExecution.setAssignToDaysScheduler(true);
                break;
            }
        }
        LOGGER.debug(String.format("... could not assign %s planned at:%s", dailyPlanItem.getJobName(), 
                dailyPlanItem.getPlannedStartFormated()));
    }

    private void adjustDailyPlanOrderItem(DailyPlanDBItem dailyPlanItem, List<DBItemReportTrigger> dbItemReportTriggerList) throws Exception {
        if (dailyPlanDBLayer.getConnection() == null) {
            dailyPlanDBLayer.initConnection(dailyPlanDBLayer.getConfigurationFileName());
        }
        LOGGER.debug(String.format("%s records in dbItemReportTriggerList", dbItemReportTriggerList.size()));
        dailyPlanItem.setIsLate(dailyPlanItem.getExecutionState().isLate());
        dailyPlanItem.setState(dailyPlanItem.getExecutionState().getState());
        for (int i = 0; i < dbItemReportTriggerList.size(); i++) {
            DBItemReportTrigger dbItemReportTrigger = (DBItemReportTrigger) dbItemReportTriggerList.get(i);
            if (!dbItemReportTrigger.isAssignToDaysScheduler() && dailyPlanItem.isOrderJob()
                    && dailyPlanItem.isEqual(dbItemReportTrigger)) {
                LOGGER.debug(String.format("... assign %s to %s/%s", dbItemReportTrigger.getHistoryId(), 
                        dailyPlanItem.getJobChainNotNull(), dailyPlanItem.getOrderId()));
                dailyPlanItem.setReportTriggerId(dbItemReportTrigger.getId());
                dailyPlanItem.setIsAssigned(true);
                dbItemReportTrigger.setAssignToDaysScheduler(true);
                break;
            }
        }
    }

    public void adjustWithHistory() throws Exception {
        String lastSchedulerId = "***";
        dailyPlanDBLayer.setWhereSchedulerId(this.schedulerId);
        dailyPlanDBLayer.setWhereFrom(from);
        dailyPlanDBLayer.setWhereTo(to);
        List<DailyPlanDBItem> dailyPlanList = dailyPlanDBLayer.getWaitingDailyPlanList(-1);
        dailyPlanExecutionsDBLayer.getFilter().setLimit(-1);
        dailyPlanExecutionsDBLayer.getFilter().setExecutedFrom(from);
        dailyPlanExecutionsDBLayer.getFilter().setExecutedTo(dailyPlanDBLayer.getWhereUtcTo());
        dailyPlanTriggerDbLayer.getFilter().setLimit(-1);
        dailyPlanTriggerDbLayer.getFilter().setExecutedFrom(from);
        dailyPlanTriggerDbLayer.getFilter().setExecutedTo(dailyPlanDBLayer.getWhereUtcTo());
        try {
            dailyPlanDBLayer.getConnection().beginTransaction();
            List<DBItemReportExecution> dbItemReportExecutionList = null;
            List<DBItemReportTrigger> dbItemReportTriggerList = null;        
            
            for (int i = 0; i < dailyPlanList.size(); i++) {
                DailyPlanDBItem dailyPlanItem = (DailyPlanDBItem) dailyPlanList.get(i);
                String schedulerId = dailyPlanItem.getSchedulerId();
                if (dailyPlanItem.isStandalone()) {
                    if (dbItemReportExecutionList == null || !schedulerId.equals(lastSchedulerId)) {
                        dailyPlanDBLayer.getConnection().commit();
                        dailyPlanDBLayer.getConnection().beginTransaction();
                        dailyPlanExecutionsDBLayer.getFilter().setSchedulerId(schedulerId);
                        dbItemReportExecutionList = dailyPlanExecutionsDBLayer.getSchedulerHistoryListFromTo();
                        lastSchedulerId = schedulerId;
                    LOGGER.debug(String.format("... Reading scheduler_id: %s", schedulerId));
                    }
                    adjustDailyPlanItem(dailyPlanItem, dbItemReportExecutionList);
                } else {
                    if (dbItemReportTriggerList == null || !schedulerId.equals(lastSchedulerId)) {
                        dailyPlanDBLayer.getConnection().commit();
                        dailyPlanDBLayer.getConnection().beginTransaction();
                        dailyPlanTriggerDbLayer.getFilter().setSchedulerId(schedulerId);
                        dbItemReportTriggerList = dailyPlanTriggerDbLayer.getSchedulerOrderHistoryListFromTo();
                        LOGGER.debug(String.format("... Reading scheduler_id: %s", schedulerId));
                        lastSchedulerId = schedulerId;
                    }
                    adjustDailyPlanOrderItem(dailyPlanItem, dbItemReportTriggerList);
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

 
    public void setTo(Date to) {
        this.to = to;
    }

}