package com.sos.jitl.dailyplan.db;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.UtcTimeHelper;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.hibernate.exceptions.SOSHibernateObjectOperationException;
import com.sos.jitl.dailyplan.job.CheckDailyPlanOptions;
import com.sos.jitl.dailyplan.job.CreateDailyPlanOptions;
import com.sos.jitl.inventory.db.DBLayerInventory;
import com.sos.jitl.jobstreams.classes.JSStarter;
import com.sos.jitl.jobstreams.db.DBItemJobStream;
import com.sos.jitl.jobstreams.db.DBItemJobStreamStarter;
import com.sos.jitl.jobstreams.db.DBItemJobStreamStarterJob;
import com.sos.jitl.jobstreams.db.DBLayerJobStreamStarters;
import com.sos.jitl.jobstreams.db.DBLayerJobStreams;
import com.sos.jitl.jobstreams.db.DBLayerJobStreamsStarterJobs;
import com.sos.jitl.jobstreams.db.FilterJobStreamStarterJobs;
import com.sos.jitl.jobstreams.db.FilterJobStreamStarters;
import com.sos.jitl.jobstreams.db.FilterJobStreams;
import com.sos.jitl.reporting.db.DBItemInventoryJob;
import com.sos.jitl.reporting.db.DBItemInventoryOrder;
import com.sos.jitl.reporting.db.DBItemInventorySchedule;
import com.sos.jitl.reporting.db.DBLayerReporting;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.answers.Calendar;
import com.sos.scheduler.model.answers.Order;
import com.sos.scheduler.model.answers.Period;
import com.sos.scheduler.model.commands.JSCmdShowCalendar;
import com.sos.scheduler.model.commands.JSCmdShowOrder;
import com.sos.scheduler.model.objects.Spooler;

public class Calendar2DB {

    private static final int DEFAULT_DAYS_OFFSET = 31;
    private static final int AVERAGE_DURATION_ONE_ITEM = 5;
    private static final int LIMIT_CALENDAR_CALL = 39999;
    private static final int DAYLOOP = 3;
    private static final int DEFAULT_LIMIT = 30;
    private static final Logger LOGGER = LoggerFactory.getLogger(Calendar2DB.class);
    private static final int MAX_DAY_OFFSET = 2000;
    private static SchedulerObjectFactory schedulerObjectFactory = null;
    private Date from;
    private Date to;
    private int dayOffset;
    private String schedulerId = "";

    private String dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
    private DailyPlanDBLayer dailyPlanDBLayer;
    private DBLayerJobStreamStarters dbLayerJobStreamStarters;

    private CreateDailyPlanOptions options = null;
    private CheckDailyPlanOptions checkDailyPlanOptions;
    private sos.spooler.Spooler spooler;
    List<DailyPlanDBItem> dailyPlanList;
    Map<String, DailyPlanCalender2DBFilter> listOfDailyPlanCalender2DBFilter;
    List<DailyPlanCalendarItem> listOfCalendars;
    private DailyPlanInterval dailyPlanInterval;
    private DBLayerInventory dbLayerInventory;
    private Map<String, Order> listOfOrders;
    private Map<String, Long> listOfDurations;
    private Map<String, DailyPlanDBItem> listOfPlanEntries;
    Date maxPlannedTime;

    public Calendar2DB(SOSHibernateSession session, String schedulerId) throws Exception {
        dailyPlanDBLayer = new DailyPlanDBLayer(session);
        dbLayerJobStreamStarters = new DBLayerJobStreamStarters(session);

        listOfOrders = new HashMap<String, Order>();
        listOfDurations = new HashMap<String, Long>();
        listOfPlanEntries = new HashMap<String, DailyPlanDBItem>();

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        this.schedulerId = schedulerId;
    }

    public void beginTransaction() throws SOSHibernateException {
        dailyPlanDBLayer.getSession().beginTransaction();
    }

    public void commit() throws SOSHibernateException {
        dailyPlanDBLayer.getSession().commit();
    }

    public void rollback() throws SOSHibernateException {
        dailyPlanDBLayer.getSession().rollback();
    }

    public List<DailyPlanDBItem> getStartTimesFromScheduler(Date from, Date to) throws ParseException, SOSHibernateException {
        initSchedulerConnection();

        String fromTimeZoneString = "UTC";
        String toTimeZoneString = DateTimeZone.getDefault().getID();

        DateTimeZone fromZone = DateTimeZone.forID(fromTimeZoneString);

        this.from = UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, new DateTime(from).withZone(fromZone));
        this.to = UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, new DateTime(to).withZone(fromZone));

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(this.from);
        calendar.add(GregorianCalendar.DAY_OF_MONTH, MAX_DAY_OFFSET);
        Date max = calendar.getTime();
        if (max.before(this.to)) {
            this.to = max;
        }

        fillListOfCalendars(true);
        return getCalendarFromJobScheduler();
    }

    public void store() throws Exception {
        final long timeStartAll = System.currentTimeMillis();
        try {
            initSchedulerConnection();
            fillListOfCalendars(false);
            final long timeStart = System.currentTimeMillis();
            store(null);
            storeJobStreamStarters();
            final long timeEnd = System.currentTimeMillis();
            LOGGER.debug("Duration store: " + (timeEnd - timeStart) + " ms");
            checkDaysSchedule();
            final long timeEndAll = System.currentTimeMillis();
            LOGGER.debug("Duration total store " + (timeEndAll - timeStartAll) + " ms");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollback();
            throw e;
        }
    }

    private void storeJobStreamStarters() throws Exception {
        setFrom();
        setTo();
        beginTransaction();
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DBLayerReporting dbLayerReporting = new DBLayerReporting(dailyPlanDBLayer.getSession());
        DBLayerJobStreamsStarterJobs dbLayerJobStreamsStarterJobs = new DBLayerJobStreamsStarterJobs(dailyPlanDBLayer.getSession());
        DBLayerJobStreams dbLayerJobStreams = new DBLayerJobStreams(dailyPlanDBLayer.getSession());
        FilterJobStreams filterJobStreams = new FilterJobStreams();
        List<DBItemJobStream> listOfJobStreams = dbLayerJobStreams.getJobStreamsList(filterJobStreams, 0);
        Map<Long, DBItemJobStream> mapOfJobStreams = new HashMap<Long, DBItemJobStream>();
        for (DBItemJobStream dbItemJobStream : listOfJobStreams) {
            mapOfJobStreams.put(dbItemJobStream.getId(), dbItemJobStream);
        }
        FilterJobStreamStarters filterJobStreamStarters = new FilterJobStreamStarters();
        FilterJobStreamStarterJobs filterJobStreamStarterJobs = new FilterJobStreamStarterJobs();
        List<DBItemJobStreamStarter> listOfStarters = dbLayerJobStreamStarters.getJobStreamStartersList(filterJobStreamStarters, 0);
        for (DBItemJobStreamStarter dbItemJobStreamStarter : listOfStarters) {
            JSStarter jsStarter = new JSStarter(objectMapper);
            jsStarter.setItemJobStreamStarter(from, to, dbItemJobStreamStarter);
            filterJobStreamStarterJobs.setJobStreamStarter(dbItemJobStreamStarter.getId());
            String start;
            for (com.sos.joc.model.calendar.Period period : jsStarter.getJobStreamScheduler().getPlan().getPeriods()) {

                for (DBItemJobStreamStarterJob dbItemJobStreamStarterJob : dbLayerJobStreamsStarterJobs.getJobStreamStarterJobsList(
                        filterJobStreamStarterJobs, 0)) {
                    boolean isNew;

                    if (period.getSingleStart() != null) {
                        start = period.getSingleStart();
                        LOGGER.debug("start jobstream: " + jsStarter.getItemJobStreamStarter().getJobStream() + " at " + start);
                    } else {
                        start = period.getBegin();
                        LOGGER.debug("start jobstream: " + jsStarter.getItemJobStreamStarter().getJobStream() + " at " + start);
                    }

                    DailyPlanDBItem dailyPlanDBItem;
                    dailyPlanDBLayer.resetFilter();
                    dailyPlanDBLayer.getFilter().setJob(dbItemJobStreamStarterJob.getJob());
                    DailyPlanDate dailyScheduleDate = new DailyPlanDate(dateFormat);
                    dailyScheduleDate.setSchedule(start);
                    dailyPlanDBLayer.getFilter().setPlannedStart(dailyScheduleDate.getSchedule());
                    dailyPlanDBLayer.getFilter().setSchedulerId(this.schedulerId);
                    List<DailyPlanDBItem> l = dailyPlanDBLayer.getDailyPlanList(0, true);

                    if (l.size() > 0) {
                        dailyPlanDBItem = l.get(0);
                        isNew = false;
                    } else {
                        dailyPlanDBItem = new DailyPlanDBItem();
                        isNew = true;
                    }
                    dailyPlanDBItem.setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

                    if (period.getSingleStart() != null) {
                        LOGGER.debug("start jobstream: " + jsStarter.getItemJobStreamStarter().getJobStream() + " at " + start);
                    } else {
                        dailyPlanDBItem.setPeriodBegin(period.getBegin());
                        dailyPlanDBItem.setPeriodEnd(period.getEnd());
                        try {
                            dailyPlanDBItem.setRepeatInterval(new BigInteger(period.getAbsoluteRepeat()), new BigInteger(period.getRepeat()));
                        } catch (Exception e) {
                        }
                        LOGGER.debug("start jobstream: " + jsStarter.getItemJobStreamStarter().getJobStream() + " at " + start);
                    }

                    dailyPlanDBItem.setSchedulerId(this.schedulerId);
                    dailyPlanDBItem.setIsAssigned(false);
                    dailyPlanDBItem.setIsLate(false);
                    dailyPlanDBItem.setJob(dbItemJobStreamStarterJob.getJob());
                    dailyPlanDBItem.setJobStream(mapOfJobStreams.get(dbItemJobStreamStarter.getJobStream()).getJobStream());

                    dailyPlanDBItem.setJobChain(".");
                    dailyPlanDBItem.setOrderId(".");
                    dailyPlanDBItem.setPlannedStart(start);
                    Long duration = getDuration(dbLayerReporting, dbItemJobStreamStarterJob.getJob(), null);

                    if (dailyPlanDBItem.getPlannedStart() != null) {
                        dailyPlanDBItem.setExpectedEnd(new Date(dailyPlanDBItem.getPlannedStart().getTime() + duration));
                    }

                    dailyPlanDBItem.setStartStart(false);
                    dailyPlanDBItem.setState("PLANNED");
                    dailyPlanDBItem.setReportTriggerId(null);
                    dailyPlanDBItem.setReportExecutionId(null);
                    dailyPlanDBItem.setDateFormat(this.dateFormat);
                    dailyPlanDBItem.setModified(new Date());

                    if (isNew) {
                        dailyPlanDBItem.setCreated(new Date());
                        dailyPlanDBLayer.getSession().save(dailyPlanDBItem);
                    } else {
                        try {
                            dailyPlanDBLayer.getSession().update(dailyPlanDBItem);
                        } catch (SOSHibernateObjectOperationException e) {
                            dailyPlanDBLayer.getSession().save(dailyPlanDBItem);
                        }
                    }
                }
            }
        }
        commit();
    }

    public void addDailyplan2DBFilter(DailyPlanCalender2DBFilter dailyPlanCalender2DBFilter, Long instanceId) throws SOSHibernateException,
            ParseException {
        if (listOfDailyPlanCalender2DBFilter == null) {
            initSchedulerConnection();
            listOfDailyPlanCalender2DBFilter = new HashMap<String, DailyPlanCalender2DBFilter>();
        }
        if ((!"".equals(dailyPlanCalender2DBFilter.getForSchedule()) && (dailyPlanCalender2DBFilter.getForSchedule() != null))) {
            if (dbLayerInventory == null) {
                dbLayerInventory = new DBLayerInventory(dailyPlanDBLayer.getSession());
            }

            if (instanceId != null) {
                String schedule;
                DBItemInventorySchedule dbItemInventorySchedule = dbLayerInventory.getSubstituteIfExists(dailyPlanCalender2DBFilter.getForSchedule(),
                        instanceId);
                if (dbItemInventorySchedule != null && !".".equals(dbItemInventorySchedule.getSubstituteName())) {
                    schedule = dbItemInventorySchedule.getSubstituteName();
                } else {
                    schedule = dailyPlanCalender2DBFilter.getForSchedule();
                }
                List<DBItemInventoryJob> listOfUsedJobs = dbLayerInventory.getJobsReferencingSchedule(instanceId, schedule);
                for (DBItemInventoryJob dbItemInventoryJob : listOfUsedJobs) {
                    dailyPlanCalender2DBFilter = new DailyPlanCalender2DBFilter();
                    dailyPlanCalender2DBFilter.setForJob(dbItemInventoryJob.getName());
                    listOfDailyPlanCalender2DBFilter.put(dailyPlanCalender2DBFilter.getKey(), dailyPlanCalender2DBFilter);

                }
                List<DBItemInventoryOrder> listOfUsedOrders = dbLayerInventory.getOrdersReferencingSchedule(instanceId, schedule);
                for (DBItemInventoryOrder dbItemInventoryOrder : listOfUsedOrders) {
                    dailyPlanCalender2DBFilter = new DailyPlanCalender2DBFilter();
                    dailyPlanCalender2DBFilter.setForJobChain(dbItemInventoryOrder.getJobChainName());
                    dailyPlanCalender2DBFilter.setForOrderId(dbItemInventoryOrder.getOrderId());
                    listOfDailyPlanCalender2DBFilter.put(dailyPlanCalender2DBFilter.getKey(), dailyPlanCalender2DBFilter);

                }
            }
        } else {
            listOfDailyPlanCalender2DBFilter.put(dailyPlanCalender2DBFilter.getKey(), dailyPlanCalender2DBFilter);
        }
    }

    public void processDailyplan2DBFilter() throws Exception {
        long timeStartAll = System.currentTimeMillis();
        long timeStoreSum = 0L;

        if (listOfDailyPlanCalender2DBFilter == null) {
            listOfDailyPlanCalender2DBFilter = new HashMap<String, DailyPlanCalender2DBFilter>();
        }

        initSchedulerConnection();
        LOGGER.debug(String.format("processDailyplan2DBFilter: day_offset is %s SchedulerId is %s", this.dayOffset, this.schedulerId));
        if (from == null) {
            setFrom();
        }
        if (to == null) {
            setTo();
        }
        LOGGER.debug(String.format("from: %s, to: %s", from, to));

        try {
            fillListOfCalendars(false);

            DailyPlanCalendarItem dailyPlanCalendarItem = listOfCalendars.get(0);
            Set<String> calendarEntries = new HashSet<String>();
            for (Object calendarObject : dailyPlanCalendarItem.getCalendar().getAtOrPeriod()) {
                if (calendarObject instanceof Period) {
                    Period period = (Period) calendarObject;
                    String orderId = period.getOrder();
                    String jobChain = period.getJobChain();
                    String job = period.getJob();
                    calendarEntries.add(job + ":" + jobChain + ":" + orderId);
                }
            }
            long numberOfDailyPlanItems = calendarEntries.size();
            long numberOfFilters = listOfDailyPlanCalender2DBFilter.size();
            long estimatedDurationAll = AVERAGE_DURATION_ONE_ITEM * numberOfDailyPlanItems * dayOffset;
            long estimatatedDurationSelect = AVERAGE_DURATION_ONE_ITEM * numberOfFilters * dayOffset;

            long percentage = 0;
            if (estimatedDurationAll > 0) {
                percentage = 100 * estimatatedDurationSelect / estimatedDurationAll;
            }

            LOGGER.debug("-> estimated all: " + estimatedDurationAll);
            LOGGER.debug("-> estimated selected: " + estimatatedDurationSelect);
            LOGGER.debug("-> duration percentage selected: " + percentage);

            if (percentage < 90) {

                for (Map.Entry<String, DailyPlanCalender2DBFilter> entry : listOfDailyPlanCalender2DBFilter.entrySet()) {
                    DailyPlanCalender2DBFilter dailyPlanCalender2DBFilter = entry.getValue();
                    final long timeStart = System.currentTimeMillis();
                    store(dailyPlanCalender2DBFilter);
                    final long timeEnd = System.currentTimeMillis();
                    LOGGER.debug("Duration: " + dailyPlanCalender2DBFilter.getName() + ":" + (timeEnd - timeStart) + " ms");
                    timeStoreSum = timeStoreSum + (timeEnd - timeStart);
                }
            } else {
                long timeStoreStart = System.currentTimeMillis();
                store(null);
                final long timeStoreEnd = System.currentTimeMillis();
                timeStoreSum = timeStoreSum + (timeStoreEnd - timeStoreStart);
            }
            LOGGER.debug("Duration total: " + timeStoreSum + " ms");
            checkDaysSchedule();
            long timeEndAll = System.currentTimeMillis();
            LOGGER.debug("Duration process all: " + (timeEndAll - timeStartAll) + " ms");
        } catch (SOSHibernateException e) {
            LOGGER.error(e.getMessage(), e);
            rollback();
            throw e;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    private void fillListOfCalendars(boolean withTime) {
        if (listOfCalendars == null) {
            listOfCalendars = new ArrayList<DailyPlanCalendarItem>();
        }

        LOGGER.debug(String.format("fillListOfCalendars: from %s to %s", from, to));
        dailyPlanInterval = new DailyPlanInterval(from, to);

        Date tFrom = from;
        while (tFrom.before(to)) {
            Date before = addCalendar(from, DAYLOOP, java.util.Calendar.DAY_OF_MONTH);
            if (to.before(before)) {
                before = to;
            }
            Date xFrom = from;
            Calendar calendar = getCalendar(from, before, withTime);
            DailyPlanCalendarItem dailyPlanCalendarItem = new DailyPlanCalendarItem(xFrom, before, calendar);
            LOGGER.debug(String.format("Calendar: from=%s to=%s", xFrom, before));
            listOfCalendars.add(dailyPlanCalendarItem);
            tFrom = addCalendar(from, 1, java.util.Calendar.SECOND);
        }
    }

    private void initSchedulerConnection() throws ParseException {
        LOGGER.debug("initSchedulerConnection");

        if (options.dayOffset.isDirty()) {
            dayOffset = options.getdayOffset().value();
        } else {
            dayOffset = getDayOffsetFromPlan();
        }

        if (dayOffset > MAX_DAY_OFFSET) {
            LOGGER.warn("Changing dayOffset from %s to %s. See: CVE-2020-6855", dayOffset, 2000);
            dayOffset = MAX_DAY_OFFSET;
        }

        if (schedulerObjectFactory == null) {
            LOGGER.debug("schedulerObjectFactory is null");
            LOGGER.debug("Calender2DB");
            if (spooler == null) {
                if (options.basicAuthorization.isDirty() && !options.basicAuthorization.getValue().isEmpty()) {
                    schedulerObjectFactory = new SchedulerObjectFactory(options.getCommandUrl().getValue(), options.basicAuthorization.getValue());
                } else {
                    schedulerObjectFactory = new SchedulerObjectFactory(options.getCommandUrl().getValue());
                }
            } else {
                schedulerObjectFactory = new SchedulerObjectFactory(spooler);
            }
            schedulerObjectFactory.initMarshaller(Spooler.class);

            setFrom();
            setTo();

        }
    }

    private int getDayOffsetFromPlan() {
        maxPlannedTime = dailyPlanDBLayer.getMaxPlannedStart(schedulerId);
        Date today = new Date();
        int days = (int) ((maxPlannedTime.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
        if (days == 0) {
            days = DEFAULT_DAYS_OFFSET;
        }
        LOGGER.debug(String.format("Calculated  day_offset for SchedulerId: %s is %s ", schedulerId, days));
        return days;
    }

    private Calendar getCalendar(Date start, Date before, boolean withTime) {
        JSCmdShowCalendar jsCmdShowCalendar = schedulerObjectFactory.createShowCalendar();
        jsCmdShowCalendar.setWhat("orders");
        jsCmdShowCalendar.setLimit(LIMIT_CALENDAR_CALL);
        if (withTime) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            jsCmdShowCalendar.setFrom(sdf.format(start));
            jsCmdShowCalendar.setBefore(sdf.format(before));
            from = addCalendar(before, 1, java.util.Calendar.SECOND);
        } else {
            start = addCalendar(start, -1, java.util.Calendar.DAY_OF_MONTH);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'23:59:59");
            jsCmdShowCalendar.setFrom(sdf.format(start));
            sdf = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00");
            jsCmdShowCalendar.setBefore(sdf.format(before));
            from = addCalendar(before, 0, java.util.Calendar.DAY_OF_MONTH);
        }

        LOGGER.debug(String.format("... day_offset is %s SchedulerId is %s", this.dayOffset, this.schedulerId));
        LOGGER.debug(String.format("... calculating plan for %s - %s ", jsCmdShowCalendar.getFrom(), jsCmdShowCalendar.getBefore()));

        jsCmdShowCalendar.run();
        from = addCalendar(from, -1, java.util.Calendar.SECOND);
        return jsCmdShowCalendar.getCalendar();
    }

    private void getCurrentDailyPlan(DailyPlanCalender2DBFilter dailyPlanCalender2DBFilter) throws Exception {
        dailyPlanDBLayer.setWhereFrom(dailyPlanInterval.getConvertedFrom());
        dailyPlanDBLayer.setWhereTo(dailyPlanInterval.getConvertedTo());
        dailyPlanDBLayer.setWhereSchedulerId(schedulerId);
        if (dailyPlanCalender2DBFilter != null) {
            dailyPlanDBLayer.getFilter().setCalender2DBFilter(dailyPlanCalender2DBFilter);
        }

        dailyPlanList = dailyPlanDBLayer.getDailyPlanList(0, true);
    }

    private void deleteItemsAfterTo(DailyPlanCalender2DBFilter dailyPlanCalender2DBFilter) throws SOSHibernateException {
        dailyPlanDBLayer.setWhereFrom(dailyPlanInterval.getConvertedTo());
        dailyPlanDBLayer.setWhereSchedulerId(schedulerId);
        if (dailyPlanCalender2DBFilter != null) {
            dailyPlanDBLayer.getFilter().setCalender2DBFilter(dailyPlanCalender2DBFilter);
        }
        dailyPlanDBLayer.delete(true);
    }

    private boolean isSetback(Order order) {
        return order.getSetback() != null;
    }

    private Order getOrder(String jobChain, String orderId) {
        if (orderId == null) {
            return null;
        } else {
            String orderKey = jobChain + "(" + orderId + ")";
            Order order = listOfOrders.get(orderKey);

            if (order == null) {
                JSCmdShowOrder jsCmdShowOrder = schedulerObjectFactory.createShowOrder();
                jsCmdShowOrder.setJobChain(jobChain);
                jsCmdShowOrder.setOrder(orderId);
                try {
                    jsCmdShowOrder.run();
                    order = jsCmdShowOrder.getAnswer().getOrder();
                    listOfOrders.put(orderKey, order);
                } catch (Exception e) {
                    String cause = "";
                    if (e.getCause() != null) {
                        cause = e.getCause().toString();
                    }

                    LOGGER.info("order:" + orderKey + " not found --> " + e.toString() + ":" + cause);
                    order = null;
                }

            }
            return order;
        }
    }

    private Long getDuration(DBLayerReporting dbLayerReporting, String job, Order order) throws SOSHibernateException {
        Long duration = 0L;
        String key = "";
        if (order == null) {
            key = job;
        } else {
            key = order.getJobChain() + "(" + order.getId() + ")";
        }

        duration = listOfDurations.get(key);

        if (duration == null) {

            if (order == null) {
                duration = dbLayerReporting.getTaskEstimatedDuration(job, DEFAULT_LIMIT);
            } else {
                duration = dbLayerReporting.getOrderEstimatedDuration(order.getJobChain(), order.getId(), DEFAULT_LIMIT);
            }
            listOfDurations.put(key, duration);
        }
        return duration;
    }

    public Date addCalendar(Date date, Integer add, Integer c) {
        java.util.Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(c, add);
        return calendar.getTime();
    }

    private boolean isBeforeToday(Date d) {
        UtcTimeHelper utcTimeHelper = new UtcTimeHelper();
        DateTime dLocal = new DateTime(d);
        DateTime today = new DateTime(utcTimeHelper.getNowUtc());
        return today.getDayOfYear() > dLocal.getDayOfYear();
    }

    private void store(DailyPlanCalender2DBFilter dailyPlanCalender2DBFilter) throws Exception {

        beginTransaction();
        DBLayerReporting dbLayerReporting = new DBLayerReporting(dailyPlanDBLayer.getSession());

        int i = 0;
        boolean isNew = false;

        deleteItemsAfterTo(dailyPlanCalender2DBFilter);
        getCurrentDailyPlan(dailyPlanCalender2DBFilter);
        commit();

        beginTransaction();

        for (DailyPlanCalendarItem dailyPlanCalendarItem : listOfCalendars) {

            from = dailyPlanCalendarItem.getFrom();
            to = dailyPlanCalendarItem.getTo();

            for (Object calendarObject : dailyPlanCalendarItem.getCalendar().getAtOrPeriod()) {
                Order order = null;
                String job = null;
                String jobChain = null;
                DailyPlanDBItem dailyPlanDBItem;

                if (i < dailyPlanList.size()) {
                    isNew = false;
                    dailyPlanDBItem = dailyPlanList.get(i);
                    dailyPlanDBItem.setIsAssigned(false);
                    dailyPlanDBItem.setIsLate(false);
                    dailyPlanDBItem.setJob(".");
                    dailyPlanDBItem.setJobStream("");
                    dailyPlanDBItem.setJobChain(".");
                    dailyPlanDBItem.setOrderId(".");
                    dailyPlanDBItem.nullPlannedStart();
                    dailyPlanDBItem.setExpectedEnd(null);
                    dailyPlanDBItem.nullPeriodBegin();
                    dailyPlanDBItem.nullPeriodEnd();
                    dailyPlanDBItem.setRepeatInterval(null);
                    dailyPlanDBItem.setStartStart(false);
                    dailyPlanDBItem.setState("");
                    dailyPlanDBItem.setReportTriggerId(null);
                    dailyPlanDBItem.setReportExecutionId(null);
                    dailyPlanDBItem.setDateFormat(this.dateFormat);
                } else {
                    isNew = true;
                    dailyPlanDBItem = new DailyPlanDBItem(this.dateFormat);
                    dailyPlanDBItem.setCreated(new Date());
                }

                dailyPlanDBItem.setSchedulerId(String.valueOf(i));
                dailyPlanDBItem.setState("PLANNEDFORUPDATE");

                if (calendarObject instanceof Period) {

                    Period period = (Period) calendarObject;
                    String orderId = period.getOrder();
                    String singleStart = period.getSingleStart();

                    jobChain = period.getJobChain();
                    job = period.getJob();
                    if (job == null) {
                        order = getOrder(jobChain, orderId);
                    }

                    DailyPlanDBItem dailyPlanEntry = new DailyPlanDBItem(this.dateFormat);
                    dailyPlanEntry.setJob(job);
                    dailyPlanEntry.setJobChain(jobChain);
                    dailyPlanEntry.setOrderId(orderId);
                    dailyPlanEntry.setSchedulerId(schedulerId);
                    if (job != null || order != null) {
                        if (singleStart != null) {
                            if (orderId == null || !isSetback(order)) {
                                dailyPlanEntry.setPlannedStart(singleStart);
                            }
                        } else {
                            dailyPlanEntry.setPlannedStart(period.getBegin());
                        }
                    }

                    boolean handleEntry = (listOfPlanEntries.get(getUniqueKey(dailyPlanEntry)) == null && !isBeforeToday(dailyPlanEntry
                            .getPlannedStart()) && (dailyPlanCalender2DBFilter == null || dailyPlanCalender2DBFilter.handleEntry(job, jobChain,
                                    orderId)));

                    if (handleEntry) {

                        i = i + 1;
                        listOfPlanEntries.put(getUniqueKey(dailyPlanEntry), dailyPlanEntry);

                        if (job != null || order != null) {
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

                            Long duration = getDuration(dbLayerReporting, job, order);

                            if (dailyPlanDBItem.getPlannedStart() != null) {
                                dailyPlanDBItem.setExpectedEnd(new Date(dailyPlanDBItem.getPlannedStart().getTime() + duration));
                            }
                            dailyPlanDBItem.setIsAssigned(false);
                            dailyPlanDBItem.setModified(new Date());
                            if (dailyPlanDBItem.getPlannedStart() != null && ("".equals(dailyPlanDBItem.getJob()) || !"(Spooler)".equals(
                                    dailyPlanDBItem.getJob()))) {

                                if (isNew) {
                                    dailyPlanDBLayer.getSession().save(dailyPlanDBItem);
                                } else {
                                    try {
                                        dailyPlanDBLayer.getSession().update(dailyPlanDBItem);
                                    } catch (SOSHibernateObjectOperationException e) {
                                        dailyPlanDBLayer.getSession().save(dailyPlanDBItem);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int ii = i; ii < dailyPlanList.size(); ii++) {
            DailyPlanDBItem dailyPlanDBItem = dailyPlanList.get(ii);
            dailyPlanDBLayer.getSession().delete(dailyPlanDBItem);
        }

        dailyPlanDBLayer.updateDailyPlanList(schedulerId);

        commit();

    }

    private String getUniqueKey(DailyPlanDBItem dailyPlanEntry) {
        return dailyPlanEntry.getPlannedStart() + dailyPlanEntry.getJob() + dailyPlanEntry.getJobOrJobchain() + dailyPlanEntry.getSchedulerId();
    }

    private List<DailyPlanDBItem> getCalendarFromJobScheduler() throws ParseException, SOSHibernateException {
        DBLayerReporting dbLayerReporting = new DBLayerReporting(dailyPlanDBLayer.getSession());
        dailyPlanList = new ArrayList<DailyPlanDBItem>();
        String fromTimeZoneString = DateTimeZone.getDefault().getID();
        String toTimeZoneString = "UTC";
        DateTimeZone fromZone = DateTimeZone.forID(toTimeZoneString);

        for (DailyPlanCalendarItem dailyPlanCalendarItem : listOfCalendars) {

            from = dailyPlanCalendarItem.getFrom();
            Date utcFrom = UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, new DateTime(from).withZone(fromZone));

            to = dailyPlanCalendarItem.getTo();
            LOGGER.debug(String.format("Starttimes from Calendar: from=%s  to=%s", dailyPlanCalendarItem.getFrom(), dailyPlanCalendarItem.getTo()));

            for (Object calendarObject : dailyPlanCalendarItem.getCalendar().getAtOrPeriod()) {

                Order order = null;
                String job = null;
                String jobChain = null;
                DailyPlanDBItem dailyPlanDBItem;

                dailyPlanDBItem = new DailyPlanDBItem(this.dateFormat);
                dailyPlanDBItem.setCreated(new Date());

                dailyPlanDBItem.setSchedulerId(schedulerId);
                dailyPlanDBItem.setState("PLANNED");

                if (calendarObject instanceof Period) {

                    Period period = (Period) calendarObject;
                    String orderId = period.getOrder();
                    String singleStart = period.getSingleStart();

                    jobChain = period.getJobChain();
                    job = period.getJob();

                    if (job == null) {
                        order = getOrder(jobChain, orderId);
                    }
                    if (job != null || order != null) {
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

                            if (dailyPlanDBItem.getPlannedStart() != null && dailyPlanDBItem.getPlannedStart().compareTo(utcFrom) < 0
                                    && dailyPlanDBItem.getPlannedStart().compareTo(to) <= 0) {
                                dailyPlanDBItem.nullPlannedStart();
                            } else {
                                dailyPlanDBItem.setPeriodEnd(period.getEnd());
                                dailyPlanDBItem.setRepeatInterval(period.getAbsoluteRepeat(), period.getRepeat());
                                LOGGER.debug("Absolute Repeat Interval :" + period.getAbsoluteRepeat());
                                LOGGER.debug("Timerange start :" + period.getBegin());
                                LOGGER.debug("Timerange end :" + period.getEnd());
                                LOGGER.debug("Job-Name :" + period.getJob());
                            }
                        }

                        if (dailyPlanDBItem.getPlannedStart() != null && ("".equals(dailyPlanDBItem.getJob()) || !"(Spooler)".equals(dailyPlanDBItem
                                .getJob()))) {

                            dailyPlanDBItem.setJob(job);
                            dailyPlanDBItem.setJobChain(jobChain);
                            dailyPlanDBItem.setOrderId(orderId);
                            Long duration = getDuration(dbLayerReporting, job, order);
                            dailyPlanDBItem.setExpectedEnd(new Date(dailyPlanDBItem.getPlannedStart().getTime() + duration));
                            dailyPlanDBItem.setIsAssigned(false);
                            dailyPlanDBItem.setModified(new Date());
                            if (dailyPlanList.size() > MAX_DAY_OFFSET) {
                                break;
                            }
                            dailyPlanList.add(dailyPlanDBItem);

                        }
                    }
                }
            }
        }
        return dailyPlanList;
    }

    private void checkDaysSchedule() throws Exception {
        DailyPlanAdjustment dailyPlanAdjustment = new DailyPlanAdjustment(dailyPlanDBLayer.getSession());

        checkDailyPlanOptions = new CheckDailyPlanOptions();
        checkDailyPlanOptions.dayOffset.value(dayOffset);
        if (schedulerId != null) {
            checkDailyPlanOptions.scheduler_id.setValue(schedulerId);
        }
        dailyPlanAdjustment.setOptions(checkDailyPlanOptions);
        dailyPlanAdjustment.setTo(new Date());
        dailyPlanAdjustment.adjustWithHistory();
    }

    private void setFrom() throws ParseException {
        Date now = new Date();
        if (dayOffset < 0) {
            now = addCalendar(now, dayOffset, java.util.Calendar.DAY_OF_MONTH);
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
            now = addCalendar(now, dayOffset, java.util.Calendar.DAY_OF_MONTH);
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String tos = formatter.format(now);
        tos = tos + "T00:00:00";
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
    }

    public void setSpooler(sos.spooler.Spooler spooler) {
        this.spooler = spooler;
    }

    public int getDayOffset() {
        return dayOffset;
    }

    public Date getMaxPlannedTime(String schedulerId) {
        if (maxPlannedTime == null) {
            this.schedulerId = schedulerId;
            dayOffset = this.getDayOffsetFromPlan();
        }
        return maxPlannedTime;
    }

}