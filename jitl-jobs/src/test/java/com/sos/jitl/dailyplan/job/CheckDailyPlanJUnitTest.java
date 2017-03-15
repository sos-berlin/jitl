package com.sos.jitl.dailyplan.job;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.junit.Before;
import org.junit.Test;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.jitl.dailyplan.db.DailyPlanDBItem;
import com.sos.jitl.dailyplan.db.DailyPlanDBLayer;

public class CheckDailyPlanJUnitTest extends JSToolBox {

    protected CheckDailyPlanOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(CheckDailyPlanJUnitTest.class);
    private CheckDailyPlan objE = null;

    public CheckDailyPlanJUnitTest() {
        //
    }

    @Before
    public void setUp() throws Exception {
        objE = new CheckDailyPlan();
        objE.registerMessageListener(this);
        objOptions = objE.getOptions();
        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    @Test
    public void testExecute() throws Exception {
        try {
            HashMap pobjHM = new HashMap();
            pobjHM.put("dayOffset", 0);
            pobjHM.put("configurationFile", "R:/nobackup/junittests/hibernate/hibernate.cfg.xml");
            pobjHM.put("configurationFile", "C:/Users/ur/Documents/sos-berlin.com/jobscheduler/scheduler_joc_cockpit/config/hibernate.cfg.xml");
            objE.getOptions().setAllOptions(pobjHM);
            assertEquals("", objOptions.dayOffset.value(), 0);
            objE.Execute(); 
             
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}