package com.sos.jitl.dailyplan.db;

import com.sos.jitl.dailyplan.job.CreateDailyPlanOptions;
import com.sos.jitl.reporting.db.DBLayerReporting;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.SchedulerObjectFactory.enu4What;
import com.sos.scheduler.model.answers.*;
import com.sos.scheduler.model.commands.JSCmdShowCalendar;
import com.sos.scheduler.model.commands.JSCmdShowOrder;
import com.sos.scheduler.model.commands.JSCmdShowState;
import com.sos.scheduler.model.objects.Spooler;

import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class Calendar2DB {

    private static final int DEFAULT_LIMIT = 30;
    private static final Logger LOGGER = Logger.getLogger(Calendar2DB.class);
    private static SchedulerObjectFactory schedulerObjectFactory = null;
    private Date from;
    private Date to;
    private int dayOffset;
    private String schedulerId = "";
    private String dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
    private DailyPlanDBLayer dailyPlanDBLayer;
    private CreateDailyPlanOptions options = null;

    public Calendar2DB(String configurationFilename) {
        dailyPlanDBLayer = new DailyPlanDBLayer(configurationFilename);
    }

    private void initSchedulerConnection() {
        if ("".equals(schedulerId)) {
            LOGGER.debug("Calender2DB");
            schedulerObjectFactory = new SchedulerObjectFactory(options.getSchedulerHostName().getValue(), options.getscheduler_port().value());
            schedulerObjectFactory.initMarshaller(Spooler.class);
            dayOffset = options.getdayOffset().value();
            schedulerId = this.getSchedulerId();
        }
    }

    private Calendar getCalender() {
        initSchedulerConnection();
        JSCmdShowCalendar jsCmdShowCalendar = schedulerObjectFactory.createShowCalendar();
        jsCmdShowCalendar.setWhat("orders");
        jsCmdShowCalendar.setLimit(9999);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00");
        jsCmdShowCalendar.setFrom(sdf.format(from));
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'23:59:59");
        jsCmdShowCalendar.setBefore(sdf.format(to));
        jsCmdShowCalendar.run();
        return jsCmdShowCalendar.getCalendar();
    }

    private void delete() throws Exception {
        dailyPlanDBLayer.setWhereFrom(from);
        dailyPlanDBLayer.setWhereTo(to);
        dailyPlanDBLayer.setWhereSchedulerId(schedulerId);
        dailyPlanDBLayer.delete();
    }

    private String getSchedulerId() {
        JSCmdShowState jsCmdShowState = schedulerObjectFactory.createShowState(new enu4What[] { enu4What.folders, enu4What.no_subfolders });
        jsCmdShowState.setPath("notexist_sos");
        jsCmdShowState.setSubsystems("folder");
        jsCmdShowState.setMaxTaskHistory(BigInteger.valueOf(1));
        jsCmdShowState.run();
        State objState = jsCmdShowState.getState();
        return objState.getSpoolerId();
    }

    private boolean isSetback(Order order) {
        return order.getSetback() != null;
    }

    private Order getOrder(String jobChain, String orderId) {
        if (orderId == null) {
            return null;
        } else {
            JSCmdShowOrder jsCmdShowOrder = schedulerObjectFactory.createShowOrder();
            jsCmdShowOrder.setJobChain(jobChain);
            jsCmdShowOrder.setOrder(orderId);
            jsCmdShowOrder.run();
            Order order = jsCmdShowOrder.getAnswer().getOrder();
            return order;
        }
    }

    public void store() throws ParseException {
        try {
            initSchedulerConnection();
            DBLayerReporting dbLayerReporting = new DBLayerReporting(dailyPlanDBLayer.getConnection());
            dailyPlanDBLayer.getConnection().beginTransaction();
            this.delete();
            Calendar calendar = getCalender();
            Order order = null;
            String job = null;
            for (Object calendarObject : calendar.getAtOrPeriod()) {
                DailyPlanDBItem dailyPlanDBItem = new DailyPlanDBItem(this.dateFormat);
                dailyPlanDBItem.setSchedulerId(schedulerId);
                if (calendarObject instanceof At) {
                    At at = (At) calendarObject;
                    String orderId = at.getOrder();
                    String jobChain = at.getJobChain();
                    job = at.getJob();
                    order = getOrder(jobChain, orderId);
                    

                    dailyPlanDBItem.setJob(job);
                    dailyPlanDBItem.setJobChain(jobChain);
                    dailyPlanDBItem.setOrderId(orderId);
                    if (orderId == null || !isSetback(order)) {
                        dailyPlanDBItem.setPlannedStart(at.getAt());
                        LOGGER.debug("Start at :" + at.getAt());
                        LOGGER.debug("Job Name :" + job);
                        LOGGER.debug("Job-Chain Name :" + jobChain);
                        LOGGER.debug("Order Name :" + orderId);
                    } else {
                        LOGGER.debug("Job-Chain Name :" + jobChain + "/" + orderId + " ignored because order is in setback state");
                    }
                } else

                {
                    if (calendarObject instanceof Period)

                    {
                        Period period = (Period) calendarObject;
                        String orderId = period.getOrder();
                        String jobChain = period.getJobChain();
                        job = period.getJob();
                        order = getOrder(jobChain, orderId);
                        dailyPlanDBItem.setJob(job);
                        dailyPlanDBItem.setJobChain(jobChain);
                        dailyPlanDBItem.setOrderId(orderId);
                        dailyPlanDBItem.setPeriodBegin(period.getBegin());
                        dailyPlanDBItem.setPeriodEnd(period.getEnd());
                        dailyPlanDBItem.setRepeatInterval(period.getAbsoluteRepeat(), period.getRepeat());
                        LOGGER.debug("Absolute Repeat Interval :" + period.getAbsoluteRepeat());
                        LOGGER.debug("Timerange start :" + period.getBegin());
                        LOGGER.debug("Timerange end :" + period.getEnd());
                        LOGGER.debug("Job-Name :" + period.getJob());
                    }
                }
                
                Long duration = 0L;
                if (order == null) {
                    duration = dbLayerReporting.getTaskEstimatedDuration(job, DEFAULT_LIMIT);
                } else {
                    duration = dbLayerReporting.getOrderEstimatedDuration(order,DEFAULT_LIMIT);
                }
                dailyPlanDBItem.setExpectedEnd(new Date(dailyPlanDBItem.getPlannedStart().getTime() + duration));
                dailyPlanDBItem.setIsAssigned(false);
                dailyPlanDBItem.setModified(new Date());
                dailyPlanDBItem.setCreated(new Date());
                if (dailyPlanDBItem.getPlannedStart() != null && (dailyPlanDBItem.getJob() == null || !"(Spooler)".equals(dailyPlanDBItem.getJob()))) {
                    dailyPlanDBLayer.getConnection().save(dailyPlanDBItem);
                }
            }
            dailyPlanDBLayer.getConnection().commit();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Error occurred storing items: ", e);
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

    public void setOptions(CreateDailyPlanOptions options) throws ParseException {
        this.options = options;
        dayOffset = options.getdayOffset().value();
        setFrom();
        setTo();
    }

}