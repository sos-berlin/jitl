package com.sos.jitl.reporting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sos.jitl.reporting.db.DBItemReportTrigger;
import com.sos.jitl.reporting.db.ReportExecutionsDBLayer;
import com.sos.jitl.reporting.db.ReportTriggerDBLayer;
  

public class ReportTriggerDBLayerTest {

    private ReportTriggerDBLayer reportTriggerDBLayer;
//    private final String configurationFilename = "R:/nobackup/junittests/hibernate/hibernate_oracle.cfg.xml";
    private final String configurationFilename = "C:/Users/ur/Documents/sos-berlin.com/jobscheduler/scheduler_joc_cockpit/config/hibernate.cfg.xml";

    
    public ReportTriggerDBLayerTest() {
    }

    @Before
    public void setUp() throws Exception {
        reportTriggerDBLayer = new ReportTriggerDBLayer(configurationFilename);
    }

    @Test
    public void testSchedulerOrderStepHistoryDBLayer() throws Exception {
        ReportExecutionsDBLayer d = new ReportExecutionsDBLayer(configurationFilename);
        assertNotNull(d);
    }
 

 
 
    @Test
  //  @Ignore("Test set to Ignore for later examination")
    public void testGetSchedulerOrderStepHistoryList() throws Exception {
 
        reportTriggerDBLayer.getFilter().setExecutedFrom("2016-11-30 08:42:12","yyyy-MM-dd HH:mm:ss");
        reportTriggerDBLayer.getFilter().setSchedulerId("scheduler_joc_cockpit");
        List<DBItemReportTrigger> historyList = reportTriggerDBLayer.getSchedulerOrderHistoryListFromTo();
        
        System.out.println(historyList.get(0).getParentName());
        System.out.println(historyList.get(0).getResultError());
        System.out.println(historyList.size());
       /* reportTriggerDBLayer.getFilter().setFailed(true);
        historyList = reportTriggerDBLayer.getSchedulerOrderHistoryListFromTo();
        reportTriggerDBLayer.getFilter().setSuccess(true);
        historyList = reportTriggerDBLayer.getSchedulerOrderHistoryListFromTo();
*/
        assertEquals("testGetSchedulerOrderStepHistoryList fails...:", 22525, historyList.size());
    }
    
    @Test
  //  @Ignore("Test set to Ignore for later examination")
    public void testGetCountSchedulerOrderHistoryListFromTo() throws Exception {
 
        reportTriggerDBLayer.getFilter().setExecutedFrom("2016-11-30 08:42:12","yyyy-MM-dd HH:mm:ss");
        reportTriggerDBLayer.getFilter().setSchedulerId("scheduler_joc_cockpit");
        Long c = reportTriggerDBLayer.getCountSchedulerOrderHistoryListFromTo();
        System.out.println(c);
        reportTriggerDBLayer.getFilter().setFailed(true);
        c = reportTriggerDBLayer.getCountSchedulerOrderHistoryListFromTo();
        reportTriggerDBLayer.getFilter().setSuccess(true);
        c = reportTriggerDBLayer.getCountSchedulerOrderHistoryListFromTo();
     }
 
 

}