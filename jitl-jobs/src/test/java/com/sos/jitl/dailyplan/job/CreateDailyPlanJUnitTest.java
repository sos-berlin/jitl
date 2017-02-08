package com.sos.jitl.dailyplan.job;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.query.Query;
import org.junit.Before;
import org.junit.Test;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateStatelessConnection;
import com.sos.jitl.dailyplan.db.DailyPlanDBItem;
import com.sos.jitl.dailyplan.db.DailyPlanDBLayer;
import com.sos.jitl.reporting.db.DBLayer;

public class CreateDailyPlanJUnitTest extends JSToolBox {

    protected CreateDailyPlanOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(CreateDailyPlanJUnitTest.class);
    private CreateDailyPlan objE = null;

    public CreateDailyPlanJUnitTest() {
        //
    }

    @Before
    public void setUp() throws Exception {
        objE = new CreateDailyPlan();
        objE.registerMessageListener(this);
        objOptions = objE.getOptions();
        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }
    
    private SOSHibernateConnection getConnection(String confFile) throws Exception {
        SOSHibernateFactory sosHibernateFactory = new SOSHibernateFactory(confFile);
        sosHibernateFactory.addClassMapping(DBLayer.getReportingClassMapping());
        sosHibernateFactory.build();
        SOSHibernateConnection connection = new SOSHibernateStatelessConnection(sosHibernateFactory);
        connection.connect();
        return connection;
    }


    @Test
    public void testExecute() throws Exception {
        try {
            HashMap pobjHM = new HashMap();
            pobjHM.put("scheduler_port", 4444);
            pobjHM.put("schedulerHostName", "localhost");
            pobjHM.put("dayOffset", 10);
           // pobjHM.put("configurationFile", "R:/nobackup/junittests/hibernate/hibernate.cfg.xml");
            pobjHM.put("configurationFile", "D:/Arbeit/scheduler/jobscheduler/re-dell_4444_jobscheduler.1.11x64-snapshot/scheduler_data/config/hibernate.cfg.xml");
            objE.getOptions().setAllOptions(pobjHM);
            assertEquals("", objOptions.scheduler_port.value(), 4444);
            objE.setSchedulerId("re-dell_4444_jobscheduler.1.11x64-snapshot");
            objE.Execute();
           // DailyPlanDBLayer d = new DailyPlanDBLayer("R:/nobackup/junittests/hibernate/hibernate.cfg.xml");
            DailyPlanDBLayer d = new DailyPlanDBLayer(getConnection("D:/Arbeit/scheduler/jobscheduler/re-dell_4444_jobscheduler.1.11x64-snapshot/scheduler_data/config/hibernate.cfg.xml"));
            d.getConnection().beginTransaction();
            @SuppressWarnings("unchecked")
            Query<DailyPlanDBItem> query = d.getConnection().createQuery(" from DailyPlanDBItem where job like :test");
            query.setParameter("test", "/sos/dailyschedule/CreateDaysSchedule");
            List<DailyPlanDBItem> calendarList = query.list();
            for (int i = 0; i < calendarList.size(); i++) {
                DailyPlanDBItem calendarItem = (DailyPlanDBItem) calendarList.get(i);
                if (i == 0) {
                    assertEquals("/sos/dailyschedule/CreateDaysSchedule", calendarItem.getJob());
                    break;
                }
            }
            d.getConnection().commit();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}