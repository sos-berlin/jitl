package com.sos.schedulerhistory.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.jitl.schedulerhistory.db.SchedulerOrderStepHistoryDBItem;
import com.sos.jitl.schedulerhistory.db.SchedulerOrderStepHistoryDBLayer;

public class SchedulerOrderStepHistoryDBLayerTest {

    private SchedulerOrderStepHistoryDBLayer schedulerOrderStepHistoryDBLayer;
    private final String configurationFilename = "R:/nobackup/junittests/hibernate/hibernate_oracle.cfg.xml";
    private File configurationFile;

    public SchedulerOrderStepHistoryDBLayerTest() {
    }

    @Before
    public void setUp() throws Exception {
        configurationFile = new File(configurationFilename);
        schedulerOrderStepHistoryDBLayer = new SchedulerOrderStepHistoryDBLayer(configurationFile);
    }

    @Test
    public void testSchedulerOrderStepHistoryDBLayer() throws Exception {
        SchedulerOrderStepHistoryDBLayer d = new SchedulerOrderStepHistoryDBLayer(configurationFile);
        assertNotNull(d);
    }

    @Test
    public void testDeleteString() throws ParseException {
        schedulerOrderStepHistoryDBLayer.getFilter().setExecutedFrom("2011-01-01 00:00:00");
        schedulerOrderStepHistoryDBLayer.getFilter().setExecutedTo("2011-10-01 00:00:00");
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testGetSchedulerOrderStepHistoryList() throws Exception {
        schedulerOrderStepHistoryDBLayer.getFilter().setExecutedFrom("2000-01-01 00:00:00");
        schedulerOrderStepHistoryDBLayer.getFilter().setExecutedTo("2020-01-01 00:00:00");
        List<SchedulerOrderStepHistoryDBItem> historyList = schedulerOrderStepHistoryDBLayer.getSchedulerOrderStepHistoryListFromTo(1);
        assertEquals("testGetSchedulerOrderStepHistoryList fails...:", 1, historyList.size());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testGetOrderStepHistoryItems() throws Exception {
        schedulerOrderStepHistoryDBLayer.getFilter().setExecutedFrom("2000-01-01 00:00:00");
        schedulerOrderStepHistoryDBLayer.getFilter().setExecutedTo(new Date());
        List<SchedulerOrderStepHistoryDBItem> historyList = schedulerOrderStepHistoryDBLayer.getSchedulerOrderStepHistoryListFromTo(1);
        assertEquals("testGetOrderStepHistoryList fails...:", 1, historyList.size());
    }

}