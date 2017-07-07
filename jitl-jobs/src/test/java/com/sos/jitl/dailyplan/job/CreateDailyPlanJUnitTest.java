package com.sos.jitl.dailyplan.job;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.hibernate.query.Query;
import org.junit.Before;
import org.junit.Test;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.jitl.dailyplan.db.Calendar2DB;
import com.sos.jitl.dailyplan.db.DailyPlanCalender2DBFilter;
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

    private SOSHibernateSession getConnection(String confFile) throws Exception {
        SOSHibernateFactory sosHibernateFactory = new SOSHibernateFactory(confFile);
        sosHibernateFactory.addClassMapping(DBLayer.getReportingClassMapping());
        sosHibernateFactory.build();
        return sosHibernateFactory.openStatelessSession();
    }

    @Test
    public void testExecute() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        try {
            HashMap pobjHM = new HashMap();
            pobjHM.put("command_url", "http://ur_dell:44001/jobscheduler/master/api/command");
            // pobjHM.put("configurationFile", "R:/nobackup/junittests/hibernate/hibernate.cfg.xml");
            pobjHM.put("configurationFile", "C:/Users/ur/Documents/sos-berlin.com/jobscheduler/scheduler_joc_cockpit/config/hibernate.cfg.xml");
            // pobjHM.put("configurationFile",
            // "D:/Arbeit/scheduler/jobscheduler/re-dell_4444_jobscheduler.1.11x64-snapshot/scheduler_data/config/hibernate.cfg.xml");
            objE.getOptions().setAllOptions(pobjHM);
            assertEquals("", objOptions.commandUrl.getValue(), "http://ur_dell:44001/jobscheduler/master/api/command");
            // objE.setSchedulerId("re-dell_4444_jobscheduler.1.11x64-snapshot");

            objE.Execute();
            // DailyPlanDBLayer d = new DailyPlanDBLayer("R:/nobackup/junittests/hibernate/hibernate.cfg.xml");
            DailyPlanDBLayer d = new DailyPlanDBLayer(getConnection(
                    "C:/Users/ur/Documents/sos-berlin.com/jobscheduler/scheduler_joc_cockpit/config/hibernate.cfg.xml"));
            d.getSession().beginTransaction();
            @SuppressWarnings("unchecked")
            Query<DailyPlanDBItem> query = d.getSession().createQuery(" from DailyPlanDBItem where job like :test");
            query.setParameter("test", "/sos/dailyschedule/CreateDaysSchedule");
            List<DailyPlanDBItem> calendarList = query.list();
            for (int i = 0; i < calendarList.size(); i++) {
                DailyPlanDBItem calendarItem = (DailyPlanDBItem) calendarList.get(i);
                if (i == 0) {
                    assertEquals("/sos/dailyschedule/CreateDaysSchedule", calendarItem.getJob());
                    break;
                }
            }
            d.getSession().commit();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void testCreateDailyPlan() throws Exception {
        try {

            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            // TimeZone.setDefault(TimeZone.getDefault());

            HashMap createDaysScheduleOptionsMap = new HashMap();
            createDaysScheduleOptionsMap.put("command_url", "http://ur_dell:44001/jobscheduler/master/api/command");
            createDaysScheduleOptionsMap.put("configurationFile",
                    "C:/Users/ur/Documents/sos-berlin.com/jobscheduler/scheduler_joc_cockpit/config/hibernate.cfg.xml");
            CreateDailyPlanOptions createDailyPlanOptions = new CreateDailyPlanOptions();
            createDailyPlanOptions.setAllOptions(createDaysScheduleOptionsMap);

            SOSHibernateFactory sosHibernateFactory = new SOSHibernateFactory(createDailyPlanOptions.configuration_file.getValue());
            sosHibernateFactory.addClassMapping(DBLayer.getReportingClassMapping());
            sosHibernateFactory.addClassMapping(DBLayer.getInventoryClassMapping());

            sosHibernateFactory.build();
            SOSHibernateSession session = sosHibernateFactory.openStatelessSession();

            Calendar2DB calendar2Db = new Calendar2DB(session);

            calendar2Db.setOptions(createDailyPlanOptions);

            /*
             * DailyPlanCalender2DBFilter dailyPlanCalender2DBFilter1 = new DailyPlanCalender2DBFilter(); dailyPlanCalender2DBFilter1.setForJob("/job2");
             * calendar2Db.addDailyplan2DBFilter(dailyPlanCalender2DBFilter1); DailyPlanCalender2DBFilter dailyPlanCalender2DBFilter2 = new
             * DailyPlanCalender2DBFilter(); dailyPlanCalender2DBFilter2.setForJobChain("/job_chain1"); dailyPlanCalender2DBFilter2.setForOrderId("test");
             * calendar2Db.addDailyplan2DBFilter(dailyPlanCalender2DBFilter2); DailyPlanCalender2DBFilter dailyPlanCalender2DBFilter4 = new
             * DailyPlanCalender2DBFilter(); dailyPlanCalender2DBFilter4.setForSchedule("/R"); calendar2Db.addDailyplan2DBFilter(dailyPlanCalender2DBFilter4);
             */
            DailyPlanCalender2DBFilter dailyPlanCalender2DBFilter3 = new DailyPlanCalender2DBFilter();
            dailyPlanCalender2DBFilter3.setForSchedule("/Atest");
            calendar2Db.addDailyplan2DBFilter(dailyPlanCalender2DBFilter3);

            /*
             * DailyPlanCalender2DBFilter dailyPlanCalender2DBFilter2 = new DailyPlanCalender2DBFilter();
             * dailyPlanCalender2DBFilter2.setForJobChain("/sos/dailyplan/CreateDailyPlan"); dailyPlanCalender2DBFilter2.setForOrderId("createDailyPlan");
             * calendar2Db.addDailyplan2DBFilter(dailyPlanCalender2DBFilter2);
             */

            calendar2Db.setSpooler(null); // wenn hier ein Spooler_object bekannt ist, dann setzten. Dann wird nämlich die interne API verwendet.
            calendar2Db.processDailyplan2DBFilter();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}