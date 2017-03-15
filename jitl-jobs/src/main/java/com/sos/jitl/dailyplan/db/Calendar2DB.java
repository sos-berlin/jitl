package com.sos.jitl.dailyplan.db;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.UtcTimeHelper;
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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class Calendar2DB {

    private static final int LIMIT_CALENDAR_CALL = 19999;
    private static final int DAYLOOP = 3;
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
    private sos.spooler.Spooler spooler;

    public Calendar2DB(SOSHibernateSession connection) throws Exception {
        dailyPlanDBLayer = new DailyPlanDBLayer(connection);
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

    private void initSchedulerConnection() {
        if ("".equals(schedulerId)) {
            LOGGER.debug("Calender2DB");
            if (spooler == null) {
                schedulerObjectFactory = new SchedulerObjectFactory(options.getSchedulerHostName().getValue(), options.getscheduler_port().value());
            } else {
                schedulerObjectFactory = new SchedulerObjectFactory();
            }
            schedulerObjectFactory.initMarshaller(Spooler.class);
            dayOffset = options.getdayOffset().value();
            schedulerId = this.getSchedulerId();
        }
    }

    private Calendar getCalender(Date start, Date before) {
        initSchedulerConnection();
        JSCmdShowCalendar jsCmdShowCalendar = schedulerObjectFactory.createShowCalendar();
        jsCmdShowCalendar.setWhat("orders");
        jsCmdShowCalendar.setLimit(LIMIT_CALENDAR_CALL);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00");
        jsCmdShowCalendar.setFrom(sdf.format(start));
        String s = sdf.format(start);
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'23:59:59");
        jsCmdShowCalendar.setBefore(sdf.format(before));
        from = addCalendar(before,1,java.util.Calendar.DAY_OF_MONTH);

        String b = sdf.format(before);
        if (spooler != null) {
            jsCmdShowCalendar.getAnswerFromSpooler(spooler);
        } else {
            jsCmdShowCalendar.run();
        }
        return jsCmdShowCalendar.getCalendar();
    }

    private void delete() throws Exception {
        String toTimeZoneString = "UTC";
        String fromTimeZoneString = DateTimeZone.getDefault().getID();
        dailyPlanDBLayer.setWhereFrom(UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, new DateTime(from)));
        dailyPlanDBLayer.setWhereTo(UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, new DateTime(to)));
        dailyPlanDBLayer.setWhereSchedulerId(schedulerId);
        dailyPlanDBLayer.delete();
    }

    private String getSchedulerId() {
        JSCmdShowState jsCmdShowState = schedulerObjectFactory.createShowState(new enu4What[] { enu4What.folders, enu4What.no_subfolders });
        jsCmdShowState.setPath("notexist_sos");
        jsCmdShowState.setSubsystems("folder");
        jsCmdShowState.setMaxTaskHistory(BigInteger.valueOf(1));

        if (spooler != null) {
            jsCmdShowState.getAnswerFromSpooler(spooler);
        } else {
            jsCmdShowState.run();
        }

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
            if (spooler != null) {
                jsCmdShowOrder.getAnswerFromSpooler(spooler);
            } else {
                jsCmdShowOrder.run();
            }

            Order order = jsCmdShowOrder.getAnswer().getOrder();
            return order;
        }
    }

    private Date addCalendar(Date date, Integer add, Integer c){
        java.util.Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(c, add);
        return calendar.getTime();
    }
    
    public void store() throws Exception {
        initSchedulerConnection();
        DBLayerReporting dbLayerReporting = new DBLayerReporting(dailyPlanDBLayer.getSession());
        this.delete();

        while (from.before(to)) {
            Date before = addCalendar(from,DAYLOOP,java.util.Calendar.DAY_OF_MONTH);
            if (to.before(before)){
                before = to;
            }
            Calendar calendar = getCalender(from, before);

            Order order = null;
            String job = null;
            for (Object calendarObject : calendar.getAtOrPeriod()) {
                DailyPlanDBItem dailyPlanDBItem = new DailyPlanDBItem(this.dateFormat);
                dailyPlanDBItem.setSchedulerId(schedulerId);
                dailyPlanDBItem.setState("PLANNED");

                if (calendarObject instanceof Period) {
                    Period period = (Period) calendarObject;
                    String orderId = period.getOrder();
                    String jobChain = period.getJobChain();
                    String singleStart = period.getSingleStart();

                    job = period.getJob();
                    order = getOrder(jobChain, orderId);

                    if (singleStart != null) {
                        if (orderId == null || !isSetback(order)) {
                            dailyPlanDBItem.setPlannedStart(singleStart);
                            LOGGER.debug("Start at :" + singleStart);
                            LOGGER.debug("Job Name :" + job);
                            LOGGER.debug("Job-Chain Name :" + jobChain);
                            LOGGER.debug("Order Name :" + orderId);
                        } else {
                            LOGGER.debug("Job-Chain Name :" + jobChain + "/" + orderId + " ignored because order is in setback state");
                        }
                    } else {
                        dailyPlanDBItem.setPeriodBegin(period.getBegin());
                        dailyPlanDBItem.setPeriodEnd(period.getEnd());
                        dailyPlanDBItem.setRepeatInterval(period.getAbsoluteRepeat(), period.getRepeat());
                        LOGGER.debug("Absolute Repeat Interval :" + period.getAbsoluteRepeat());
                        LOGGER.debug("Timerange start :" + period.getBegin());
                        LOGGER.debug("Timerange end :" + period.getEnd());
                        LOGGER.debug("Job-Name :" + period.getJob());
                    }
                    dailyPlanDBItem.setJob(job);
                    dailyPlanDBItem.setJobChain(jobChain);
                    dailyPlanDBItem.setOrderId(orderId);

                    Long duration = 0L;
                    if (order == null) {
                        duration = dbLayerReporting.getTaskEstimatedDuration(job, DEFAULT_LIMIT);
                    } else {
                        duration = dbLayerReporting.getOrderEstimatedDuration(order, DEFAULT_LIMIT);
                    }
                    dailyPlanDBItem.setExpectedEnd(new Date(dailyPlanDBItem.getPlannedStart().getTime() + duration));
                    dailyPlanDBItem.setIsAssigned(false);
                    dailyPlanDBItem.setModified(new Date());
                    dailyPlanDBItem.setCreated(new Date());
                    if (dailyPlanDBItem.getPlannedStart() != null && (dailyPlanDBItem.getJob() == null || !"(Spooler)".equals(dailyPlanDBItem.getJob()))) {
                        dailyPlanDBLayer.getSession().save(dailyPlanDBItem);
                    }
                }
            }
        }
    }

    private void setFrom() throws ParseException {
        Date now = new Date();
        if (dayOffset < 0) {
            now = addCalendar(now,dayOffset,java.util.Calendar.DAY_OF_MONTH);
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
            now = addCalendar(now,dayOffset,java.util.Calendar.DAY_OF_MONTH);
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

    public void setSpooler(sos.spooler.Spooler spooler) {
        this.spooler = spooler;
    }

}