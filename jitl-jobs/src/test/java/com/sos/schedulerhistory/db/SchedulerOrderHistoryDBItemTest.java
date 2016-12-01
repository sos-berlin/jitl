package com.sos.schedulerhistory.db;

import static org.junit.Assert.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.scheduler.history.db.SchedulerOrderHistoryDBItem;

public class SchedulerOrderHistoryDBItemTest {

    @SuppressWarnings("unused")
    private final String conClassName = "SchedulerOrderHistoryDBItemTest";
    private SchedulerOrderHistoryDBItem schedulerOrderHistoryDBItem;

    public SchedulerOrderHistoryDBItemTest() {
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
        schedulerOrderHistoryDBItem = new SchedulerOrderHistoryDBItem();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSchedulerOrderHistoryDBItem() {
        schedulerOrderHistoryDBItem = new SchedulerOrderHistoryDBItem();
    }

    @Test
    public void testSetId() {
        Long myId = new Long(11);
        schedulerOrderHistoryDBItem.setHistoryId(myId);
        Long id = schedulerOrderHistoryDBItem.getHistoryId();
        assertEquals("testSetid failed: ", myId, id);
    }

    @Test
    public void testSetSpoolerId() {
        String mySpoolerId = "SpoolerId";
        schedulerOrderHistoryDBItem.setSpoolerId(mySpoolerId);
        String spoolerId = schedulerOrderHistoryDBItem.getSpoolerId();
        assertEquals("testSetspoolerId failed: ", mySpoolerId, spoolerId);
    }

    @Test
    public void testSetOrderId() {
        String myOrderId = "OrderId";
        schedulerOrderHistoryDBItem.setOrderId(myOrderId);
        String orderId = schedulerOrderHistoryDBItem.getOrderId();
        assertEquals("testSetspoolerId failed: ", myOrderId, orderId);
    }

    @Test
    public void testSetJobChain() {
        String myJobChain = "JobChain";
        schedulerOrderHistoryDBItem.setJobChain(myJobChain);
        String jobChain = schedulerOrderHistoryDBItem.getJobChain();
        assertEquals("testSetjobChain failed: ", myJobChain, jobChain);
    }

    @Test
    public void testSetStartTime() {
        Date myStartTime = new Date();
        schedulerOrderHistoryDBItem.setStartTime(myStartTime);
        Date startTime = schedulerOrderHistoryDBItem.getStartTime();
        assertEquals("testSetstartTime failed: ", 0, startTime.compareTo(myStartTime));
    }

    @Test
    public void testSetEndTime() {
        Date myEndTime = new Date();
        schedulerOrderHistoryDBItem.setEndTime(myEndTime);
        Date endTime = schedulerOrderHistoryDBItem.getEndTime();
        assertEquals("testSetstartTime failed: ", 0, endTime.compareTo(myEndTime));
    }

    @Test
    public void testSetCause() {
        String myCause = "Cause";
        schedulerOrderHistoryDBItem.setCause(myCause);
        String cause = schedulerOrderHistoryDBItem.getCause();
        assertEquals("testSetcause failed: ", myCause, cause);
    }

    @Test
    public void testSetState() {
        String myState = "State";
        schedulerOrderHistoryDBItem.setState(myState);
        String state = schedulerOrderHistoryDBItem.getState();
        assertEquals("testSetstate failed: ", myState, state);
    }

    @Test
    public void testSetStateText() {
        String myStateText = "StateText";
        schedulerOrderHistoryDBItem.setStateText(myStateText);
        String stateText = schedulerOrderHistoryDBItem.getStateText();
        assertEquals("testSetstateText failed: ", myStateText, stateText);
    }

    @Test
    public void testReadStartTimeIso() {
        Date myStartTime = new Date();
        schedulerOrderHistoryDBItem.setStartTime(null);
        assertEquals("testSetstartTime failed: ", "", schedulerOrderHistoryDBItem.readStartTimeIso());

        schedulerOrderHistoryDBItem.setStartTime(myStartTime);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String startTimeIso = formatter.format(schedulerOrderHistoryDBItem.getStartTime());
        assertEquals("testReadStartTimeIso failed: ", startTimeIso, schedulerOrderHistoryDBItem.readStartTimeIso());
    }

    @Test
    public void testReadEndTimeIso() {
        Date myEndTime = new Date();
        schedulerOrderHistoryDBItem.setEndTime(null);
        assertEquals("testSetEndTime failed: ", "", schedulerOrderHistoryDBItem.readEndTimeIso());

        schedulerOrderHistoryDBItem.setEndTime(myEndTime);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String endTimeIso = formatter.format(schedulerOrderHistoryDBItem.getEndTime());
        assertEquals("testReadEndTimeIso failed: ", endTimeIso, schedulerOrderHistoryDBItem.readEndTimeIso());
    }

    @Test
    public void testSetAssignToDaysScheduler() {
        boolean myAssignToDaysScheduler = true;
        schedulerOrderHistoryDBItem.setAssignToDaysScheduler(myAssignToDaysScheduler);
        boolean assignToDaysScheduler = schedulerOrderHistoryDBItem.isAssignToDaysScheduler();
        assertEquals("testSetassignToScheduler failed: ", myAssignToDaysScheduler, assignToDaysScheduler);
    }

}
