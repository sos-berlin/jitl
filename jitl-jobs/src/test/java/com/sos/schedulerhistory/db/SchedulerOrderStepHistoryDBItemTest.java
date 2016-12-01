package com.sos.schedulerhistory.db;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.sos.jitl.schedulerhistory.db.SchedulerOrderStepHistoryDBItem;

 
public class SchedulerOrderStepHistoryDBItemTest {

    private SchedulerOrderStepHistoryDBItem schedulerOrderStepHistoryDBItem;

    public SchedulerOrderStepHistoryDBItemTest() {
        //
    }

    @Before
    public void setUp() throws Exception {
        schedulerOrderStepHistoryDBItem = new SchedulerOrderStepHistoryDBItem();
    }

    @Test
    public void testSchedulerOrderStepHistoryDBItem() {
        schedulerOrderStepHistoryDBItem = new SchedulerOrderStepHistoryDBItem();
    }

    @Test
    public void testSetTaskId() {
        Long myTaskId = new Long(12);
        schedulerOrderStepHistoryDBItem.setTaskId(myTaskId);
        Long taskId = schedulerOrderStepHistoryDBItem.getTaskId();
        assertEquals("testSettaskId failed: ", myTaskId, taskId);
    }

    @Test
    public void testSetState() {
        String myState = "State";
        schedulerOrderStepHistoryDBItem.setState(myState);
        String state = schedulerOrderStepHistoryDBItem.getState();
        assertEquals("testSetstate failed: ", myState, state);
    }

    @Test
    public void testSetStartTime() {
        Date myStartTime = new Date();
        schedulerOrderStepHistoryDBItem.setStartTime(myStartTime);
        Date startTime = schedulerOrderStepHistoryDBItem.getStartTime();
        assertEquals("testSetstartTime failed: ", 0, startTime.compareTo(myStartTime));
    }

    @Test
    public void testSetEndTime() {
        Date myEndTime = new Date();
        schedulerOrderStepHistoryDBItem.setEndTime(myEndTime);
        Date endTime = schedulerOrderStepHistoryDBItem.getEndTime();
        assertEquals("testSetEndTime failed: ", 0, endTime.compareTo(myEndTime));
    }

    @Test
    public void testSetError() {
        Boolean myError = true;
        schedulerOrderStepHistoryDBItem.setError(myError);
        Boolean error = schedulerOrderStepHistoryDBItem.isError();
        assertEquals("testSeterror failed: ", myError, error);
    }

    @Test
    public void testSetErrorCode() {
        String myErrorCode = "ErrorCode";
        schedulerOrderStepHistoryDBItem.setErrorCode(myErrorCode);
        String errorCode = schedulerOrderStepHistoryDBItem.getErrorCode();
        assertEquals("testSeterrorCode failed: ", myErrorCode, errorCode);
    }

    @Test
    public void testSetErrorText() {
        String myErrorText = "ErrorText";
        schedulerOrderStepHistoryDBItem.setErrorText(myErrorText);
        String errorText = schedulerOrderStepHistoryDBItem.getErrorText();
        assertEquals("testSeterrorText failed: ", myErrorText, errorText);
    }

    @Test
    public void testGetStartTimeIso() {
        Date myStartTime = new Date();
        schedulerOrderStepHistoryDBItem.setStartTime(null);
        assertEquals("testSetStartTime failed: ", "", schedulerOrderStepHistoryDBItem.getStartTimeIso());
        schedulerOrderStepHistoryDBItem.setStartTime(myStartTime);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String startTimeIso = formatter.format(schedulerOrderStepHistoryDBItem.getStartTime());
        assertEquals("testReadEndTimeIso failed: ", startTimeIso, schedulerOrderStepHistoryDBItem.getStartTimeIso());
    }

    @Test
    public void testGetEndTimeIso() {
        Date myEndTime = new Date();
        schedulerOrderStepHistoryDBItem.setEndTime(null);
        assertEquals("testSetEndTime failed: ", "", schedulerOrderStepHistoryDBItem.getEndTimeIso());
        schedulerOrderStepHistoryDBItem.setEndTime(myEndTime);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String endTimeIso = formatter.format(schedulerOrderStepHistoryDBItem.getEndTime());
        assertEquals("testReadEndTimeIso failed: ", endTimeIso, schedulerOrderStepHistoryDBItem.getEndTimeIso());
    }

}