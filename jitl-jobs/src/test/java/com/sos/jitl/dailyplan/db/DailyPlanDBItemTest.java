package com.sos.jitl.dailyplan.db;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.jitl.reporting.db.DBItemReportExecution;
import com.sos.jitl.reporting.db.DBItemReportTrigger;
import com.sos.jitl.reporting.db.DBItemReportTriggerAndResult;

public class DailyPlanDBItemTest {

    @SuppressWarnings("unused")
    private final String conClassName = "DailyPlanDBItemTest";
    private DailyPlanDBItem dailyPlanDBItem = null;

    public DailyPlanDBItemTest() {
        //
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        dailyPlanDBItem = new DailyPlanDBItem();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSetId() {
        Long myId = new Long(4711);
        dailyPlanDBItem.setId(myId);
        Long id = dailyPlanDBItem.getId();
        assertEquals("testSetid faild: ", myId, id);
    }

    @Test
    public void testSetInstanceId() {
        String myId = "myInstanceId";
        dailyPlanDBItem.setSchedulerId(myId);
        String id = dailyPlanDBItem.getSchedulerId();
        assertEquals("testSetInstanceId faild: ", myId, id);
    }

    @Test
    public void testSetSchedulerHistoryId() {
        Long myId = new Long(4711);
        dailyPlanDBItem.setReportExecutionId(myId);
        Long id = dailyPlanDBItem.getReportExecutionId();
        assertEquals("testSetSchedulerHistoryId faild: ", myId, id);
    }

    @Test
    public void testSetSchedulerOrderHistoryId() {
        Long myId = new Long(4711);
        dailyPlanDBItem.setReportTriggerId(myId);
        Long id = dailyPlanDBItem.getReportTriggerId();
        assertEquals("testSetSchedulerOrderHistoryId faild: ", myId, id);
    }

    @Test
    public void testSetJob() {
        String myJob = "Job";
        dailyPlanDBItem.setJob(myJob);
        String job = dailyPlanDBItem.getJob();
        assertEquals("testSetjob failed: ", myJob, job);
    }

    @Test
    public void testSetOrderId() {
        String myId = "myId";
        dailyPlanDBItem.setOrderId(myId);
        String id = dailyPlanDBItem.getOrderId();
        assertEquals("testSetOrderId faild: ", myId, id);
    }

    @Test
    public void testSetJobChain() {
        String myJobChain = "JobChain";
        dailyPlanDBItem.setJobChain(myJobChain);
        String jobChain = dailyPlanDBItem.getJobChain();
        assertEquals("testSetjobChain failed: ", myJobChain, jobChain);
    }

    @Test
    public void testSetStatus() {
        dailyPlanDBItem.setIsAssigned(true);
        boolean isAssigned = dailyPlanDBItem.getIsAssigned();
        assertEquals("testSetSchedulerHistoryId faild: ", true, isAssigned);
    }


    @Test
    public void testSetPlannedStart() {
        Date d1 = new Date();
        dailyPlanDBItem.setPlannedStart(d1);
        Date d2 = dailyPlanDBItem.getPlannedStart();
        assertEquals("Test setSchedulePlannedString failed...", 0, d1.compareTo(d2));
    }

    @Test
    public void testSetPlannedStartString() throws ParseException {

        dailyPlanDBItem.setPlannedStart("now");

        Date d1 = dailyPlanDBItem.getPlannedStart();
        Date d2 = new Date();
        String testDateFormat = "yyyy-MM-dd";
        SimpleDateFormat formatter = new SimpleDateFormat(testDateFormat);
        String today = formatter.format(d1);
        String today2 = formatter.format(d2);

        assertEquals("Test setSchedulePlannedString failed...", today, today2);
    }

    @Test
    public void getPlannedStartIso() {
        DailyPlanDBItem dailyPlanDBItem = new DailyPlanDBItem();
        assertEquals("Test getPlannedStartIso failed...", "", dailyPlanDBItem.getPlannedStartIso());

        Date d1 = new Date();
        dailyPlanDBItem.setPlannedStart(d1);
        String today = dailyPlanDBItem.getPlannedStartIso();

        String testDateFormat = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat formatter = new SimpleDateFormat(testDateFormat);
        String today2 = formatter.format(d1);

        assertEquals("Test setPlannedStartIso failed...", today, today2);
    }

    @Test
    public void testGetExpectedEndIso() {
        DailyPlanDBItem dailyPlanDBItem = new DailyPlanDBItem();
        assertEquals("Test testGetExpectedEndIso failed...", "", dailyPlanDBItem.getScheduleExecutedIso());

        Date d1 = new Date();
        dailyPlanDBItem.setExpectedEnd(d1);
        String today = dailyPlanDBItem.getScheduleExecutedIso();

        String testDateFormat = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat formatter = new SimpleDateFormat(testDateFormat);
        String today2 = formatter.format(d1);

        assertEquals("Test testGetExpectedEndIso failed...", today, today2);
    }

    @Test
    public void testSetExpectedEnd() throws ParseException {
        Date d1 = new Date();
        dailyPlanDBItem.setExpectedEnd(d1);
        Date d2 = dailyPlanDBItem.getExpectedEnd();
        assertEquals("Test testSetExpectedEnd failed...", 0, d1.compareTo(d2));
    }

 

    @Test
    public void testSetRepeatBigIntegerBigInteger() {
        BigInteger b = BigInteger.valueOf(18);

        dailyPlanDBItem.setRepeatInterval(b, BigInteger.ZERO);
        Long l = dailyPlanDBItem.getRepeatInterval();
        assertEquals("Test testSetRepeatBigIntegerBigInteger...", b, BigInteger.valueOf(l));
        boolean isStartStart = dailyPlanDBItem.getStartStart();
        assertEquals("Test setRepeatBigIntegerBigInteger...", true, isStartStart);

        dailyPlanDBItem.setRepeatInterval(BigInteger.ZERO, b);
        l = dailyPlanDBItem.getRepeatInterval();
        assertEquals("Test testSetRepeatBigIntegerBigInteger...", b, BigInteger.valueOf(l));
        isStartStart = dailyPlanDBItem.getStartStart();
        assertEquals("Test testSetRepeatBigIntegerBigInteger...", false, isStartStart);
    }

    @Test
    public void testSetPeriodBeginDate() {
        Date d1 = new Date();
        dailyPlanDBItem.setPeriodBegin(d1);
        Date d2 = dailyPlanDBItem.getPeriodBegin();
        Date d3 = dailyPlanDBItem.getPlannedStart();

        assertEquals("Test testSetPeriodBeginDate failed...", 0, d1.compareTo(d2));
        assertEquals("Test testSetPeriodBeginDate failed...", 0, d1.compareTo(d3));
    }

    @Test
    public void testSetPeriodBeginString() throws ParseException {
        dailyPlanDBItem.setPeriodBegin("now");

        Date d1 = dailyPlanDBItem.getPeriodBegin();
        Date d2 = dailyPlanDBItem.getPlannedStart();
        Date d3 = new Date();
        String testDateFormat = "yyyy-MM-dd";
        SimpleDateFormat formatter = new SimpleDateFormat(testDateFormat);
        String today = formatter.format(d1);
        String today2 = formatter.format(d3);

        assertEquals("Test testSetPeriodBeginString failed...", today, today2);
        assertEquals("Test testSetPeriodBeginString failed...", 0, d1.compareTo(d2));
    }

    @Test
    public void testSetPeriodEndDate() {
        Date d1 = new Date();
        dailyPlanDBItem.setPeriodEnd(d1);
        Date d2 = dailyPlanDBItem.getPeriodEnd();

        assertEquals("Test setPeriodEndDate failed...", 0, d1.compareTo(d2));
    }

    @Test
    public void testSetPeriodEndString() throws ParseException {
        dailyPlanDBItem.setPeriodBegin("now");

        Date d1 = dailyPlanDBItem.getPeriodBegin();
        Date d3 = new Date();
        String testDateFormat = "yyyy-MM-dd";
        SimpleDateFormat formatter = new SimpleDateFormat(testDateFormat);
        String today = formatter.format(d1);
        String today2 = formatter.format(d3);

        assertEquals("Test setPeriodEndString failed...", today, today2);
    }

    @Test
    public void testSetStartStart() {
        dailyPlanDBItem.setStartStart(true);
        assertEquals("Test setSchedulePlannedString failed...", dailyPlanDBItem.getStartStart(), true);
    }

    @Test
    public void testSetCreated() {
        Date d1 = new Date();
        dailyPlanDBItem.setCreated(d1);
        Date d2 = dailyPlanDBItem.getCreated();

        assertEquals("Test setCreated failed...", 0, d1.compareTo(d2));
    }

    @Test
    public void testSetModified() {
        Date d1 = new Date();
        dailyPlanDBItem.setModified(d1);
        Date d2 = dailyPlanDBItem.getModified();

        assertEquals("Test setScheduleModified failed...", 0, d1.compareTo(d2));
    }

    @Test
    public void testIsEqualSchedulerOrderHistoryDBItem() {
        DBItemReportTrigger dbItemReportTrigger = new DBItemReportTrigger();
        Date d = new Date();
        String jobChain = "/test/rest/fest";
        String orderId = "2789034";
        dbItemReportTrigger.setStartTime(d);
        dbItemReportTrigger.setName(orderId);
        dbItemReportTrigger.setParentName(jobChain);
        dailyPlanDBItem.setJobChain(jobChain);
        dailyPlanDBItem.setPlannedStart(d);
        dailyPlanDBItem.setOrderId(orderId);

        assertEquals("Test testIsEqualSchedulerOrderHistoryDBItem failed...", true, dailyPlanDBItem.isEqual(dbItemReportTrigger));

        dailyPlanDBItem.setOrderId(orderId + "*");
        assertEquals("Test testIsEqualSchedulerOrderHistoryDBItem failed...", false, dailyPlanDBItem.isEqual(dbItemReportTrigger));
    }

    @Test
    public void testIsEqualSchedulerHistoryDBItem() {
        DBItemReportExecution dbItemReportExecution = new DBItemReportExecution();
        Date d = new Date();
        String job = "/test/rest/fest";
        dbItemReportExecution.setStartTime(d);
        dbItemReportExecution.setName(job);
        dailyPlanDBItem.setJob(job);
        dailyPlanDBItem.setPlannedStart(d);

        assertEquals("Test testIsEqualSchedulerHistoryDBItem failed...", true, dailyPlanDBItem.isEqual(dbItemReportExecution));
        dailyPlanDBItem.setJob(job + "*");
        assertEquals("Test testIsEqualSchedulerHistoryDBItem failed...", false, dailyPlanDBItem.isEqual(dbItemReportExecution));

    }

    @Test
    public void testIsOrderJob() {
        dailyPlanDBItem.setJob(null);
        assertEquals("Test isOrderJob failed...", true, dailyPlanDBItem.isOrderJob());

        dailyPlanDBItem.setJob("job");
        dailyPlanDBItem.setJobChain("");
        assertEquals("Test isOrderJob failed...", false, dailyPlanDBItem.isOrderJob());

        dailyPlanDBItem.setJob("job");
        dailyPlanDBItem.setJobChain("jobChain");
        assertEquals("Test isOrderJob failed...", true, dailyPlanDBItem.isOrderJob());

    }

    @Test
    public void testIsStandalone() {
        dailyPlanDBItem.setJob(null);
        assertEquals("Test isStandalone failed...", false, dailyPlanDBItem.isStandalone());

        dailyPlanDBItem.setJob("job");
        dailyPlanDBItem.setJobChain("");
        assertEquals("Test isStandalone failed...", true, dailyPlanDBItem.isStandalone());

        dailyPlanDBItem.setJob("job");
        dailyPlanDBItem.setJobChain("jobChain");
        assertEquals("Test isStandalone failed...", false, dailyPlanDBItem.isStandalone());
    }
}
