package com.sos.jitl.dailyplan.db;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

 
public class DailyPlanDateTest {

    @SuppressWarnings("unused")
    private final String conClassName = "DailyPlanDateTest";
    private String dateFormat = "yyyy-MM-dd'T'hh:mm:ss";

    public DailyPlanDateTest() {
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
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testDailyPlanDate() throws ParseException {
        DailyPlanDate dailyPlanDate = new DailyPlanDate(dateFormat);
        dailyPlanDate.setSchedule("now");

        Date d1 = dailyPlanDate.getSchedule();
        Date d2 = new Date();
        String testDateFormat = "yyyy-MM-dd";
        SimpleDateFormat formatter = new SimpleDateFormat(testDateFormat);
        String today = formatter.format(d1);
        String today2 = formatter.format(d2);

        assertEquals("Test setScheduleString failed...", today, today2);
    }

    @Test
    public void testSetScheduleString() throws ParseException {
        DailyPlanDate d = new DailyPlanDate(dateFormat);

        String scheduleString = "2011-01-01T00:00:01";

        d.setSchedule(scheduleString);
        Date d1 = d.getSchedule();

        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Date d2 = formatter.parse(scheduleString);

        assertEquals("toString test failed", 0, d1.compareTo(d2));
    }

    @Test
    public void testGetSchedule() {
        DailyPlanDate d = new DailyPlanDate(dateFormat);
        Date d1 = new Date();
        d.setSchedule(d1);
        Date d2 = d.getSchedule();
        assertEquals("Test setScheduleString failed...", 0, d1.compareTo(d2));
    }

    @Test
    public void testGetIsoDate() {
        DailyPlanDate d = new DailyPlanDate(dateFormat);
        Date d1 = new Date();
        d.setSchedule(d1);
        String isoDateSchedule = d.getIsoDate();
        String isoDateFormat = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat formatter = new SimpleDateFormat(isoDateFormat);
        String isoDate = formatter.format(d1);
        assertEquals("Test setScheduleString failed...", isoDateSchedule, isoDate);
    }

    @Test
    public void testSetScheduleDate() {
        DailyPlanDate d = new DailyPlanDate(dateFormat);
        Date d1 = new Date();
        d.setSchedule(d1);
        Date d2 = d.getSchedule();
        assertEquals("Test setScheduleString failed...", 0, d1.compareTo(d2));
    }
}
