package com.sos.schedulerhistory.db;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.jitl.schedulerhistory.db.SchedulerTaskHistoryDBItem;

public class SchedulerTaskHistoryDBItemTest {

    @SuppressWarnings("unused")
    private final String conClassName = "SchedulerHistoryDBItemTest";
    private SchedulerTaskHistoryDBItem schedulerHistoryDBItem;

    public SchedulerTaskHistoryDBItemTest() {
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
        schedulerHistoryDBItem = new SchedulerTaskHistoryDBItem();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSchedulerHistoryDBItem() {
        SchedulerTaskHistoryDBItem d = new SchedulerTaskHistoryDBItem();
        assertEquals("Test testSchedulerHistoryDBItem fails ...", false, d.isAssignToDaysScheduler());
    }

    @Test
    public void testSetId() {
        Long myId = new Long(10);
        schedulerHistoryDBItem.setId(myId);
        Long Id = schedulerHistoryDBItem.getId();
        assertEquals("testSetId failed: ", myId, Id);
    }

    @Test
    public void testSetSpoolerId() {
        String mySpoolerId = "SpoolerId";
        schedulerHistoryDBItem.setSpoolerId(mySpoolerId);
        String SpoolerId = schedulerHistoryDBItem.getSpoolerId();
        assertEquals("testSetSpoolerId failed: ", mySpoolerId, SpoolerId);
    }

    @Test
    public void testSetClusterMemberId() {
        String myClusterMemberId = "ClusterMemberId";
        schedulerHistoryDBItem.setClusterMemberId(myClusterMemberId);
        String clusterMemberId = schedulerHistoryDBItem.getClusterMemberId();
        assertEquals("testSetclusterMemberId failed: ", myClusterMemberId, clusterMemberId);
    }

    @Test
    public void testSetJobName() {
        String myJobName = "JobName";
        schedulerHistoryDBItem.setJobName(myJobName);
        String jobName = schedulerHistoryDBItem.getJobName();
        assertEquals("testSetjobName failed: ", myJobName, jobName);
    }

    @Test
    public void testSetStartTime() {
        Date myStartTime = new Date();
        schedulerHistoryDBItem.setStartTime(myStartTime);
        Date startTime = schedulerHistoryDBItem.getStartTime();
        assertEquals("testSetstartTime failed: ", myStartTime, startTime);
    }

    @Test
    public void testSetEndTime() {
        Date myEndTime = new Date();
        schedulerHistoryDBItem.setEndTime(myEndTime);
        Date endTime = schedulerHistoryDBItem.getEndTime();
        assertEquals("testSetstartTime failed: ", myEndTime, endTime);
    }

    @Test
    public void testGetStartTimeIso() {
        Date myStartTime = new Date();
        schedulerHistoryDBItem.setStartTime(myStartTime);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String startTimeIso = schedulerHistoryDBItem.getStartTimeIso();
        String startTime = formatter.format(myStartTime);
        assertEquals("testGetStartTimeIso failed: ", startTimeIso, startTime);
    }

    @Test
    public void testGetEndTimeIso() {
        Date myEndTime = new Date();
        schedulerHistoryDBItem.setEndTime(myEndTime);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String endTimeIso = schedulerHistoryDBItem.getEndTimeIso();
        String endTime = formatter.format(myEndTime);
        assertEquals("testGetEndTimeIso failed: ", endTimeIso, endTime);
    }

    @Test
    public void testSetCause() {
        String myCause = "Cause";
        schedulerHistoryDBItem.setCause(myCause);
        String cause = schedulerHistoryDBItem.getCause();
        assertEquals("testSetcause failed: ", myCause, cause);
    }

    @Test
    public void testSetSteps() {
        Integer mySteps = new Integer(12);
        schedulerHistoryDBItem.setSteps(mySteps);
        Integer steps = schedulerHistoryDBItem.getSteps();
        assertEquals("testSetsteps failed: ", mySteps, steps);
    }

    @Test
    public void testSetExitCode() {
        Integer myExitCode = new Integer(12);
        schedulerHistoryDBItem.setExitCode(myExitCode);
        Integer ExitCode = schedulerHistoryDBItem.getExitCode();
        assertEquals("testSetExitCode failed: ", myExitCode, ExitCode);
    }

    @Test
    public void testSetError() {
        boolean myIsError = false;
        schedulerHistoryDBItem.setError(myIsError);
        boolean isError = schedulerHistoryDBItem.isError();
        assertEquals("testSetisError failed: ", myIsError, isError);
    }

    @Test
    public void testSetErrorCode() {
        String myErrorCode = "ErrorCode";
        schedulerHistoryDBItem.setErrorCode(myErrorCode);
        String errorCode = schedulerHistoryDBItem.getErrorCode();
        assertEquals("testSeterrorCode failed: ", myErrorCode, errorCode);
    }

    @Test
    public void testSetErrorText() {
        String myErrorText = "ErrorText";
        schedulerHistoryDBItem.setErrorText(myErrorText);
        String errorText = schedulerHistoryDBItem.getErrorText();
        assertEquals("testSeterrorText failed: ", myErrorText, errorText);
    }

    @Test
    public void testSetPid() {
        Integer myPid = new Integer(12);
        schedulerHistoryDBItem.setPid(myPid);
        Integer pid = schedulerHistoryDBItem.getPid();
        assertEquals("testSetpid failed: ", myPid, pid);
    }

    @Test
    public void testSetAssignToDaysScheduler() {
        boolean myAssignToDaysScheduler = true;
        schedulerHistoryDBItem.setAssignToDaysScheduler(myAssignToDaysScheduler);
        boolean assignToDaysScheduler = schedulerHistoryDBItem.isAssignToDaysScheduler();
        assertEquals("testSetassignToDaysScheduler failed: ", myAssignToDaysScheduler, assignToDaysScheduler);
    }

    @Test
    public void testGetDurationFormated() throws ParseException {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        Date myStartTime = formatter.parse("2013-04-23 17:12:40");
        Date myEndTime = formatter.parse("2013-04-23 18:06:08");

        schedulerHistoryDBItem.setStartTime(myStartTime);
        schedulerHistoryDBItem.setEndTime(myEndTime);
        String s = schedulerHistoryDBItem.getDurationFormated();

        assertEquals("testGetDurationFormated failed:", "00:53:28", s);

    }
}
