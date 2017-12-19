package com.sos.jitl.eventing.db;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.reporting.db.DBLayer;

public class ReportCustomEventDBLayerTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    private SOSHibernateSession getSession(String confFile) throws Exception {
        SOSHibernateFactory sosHibernateFactory = new SOSHibernateFactory(confFile);
        sosHibernateFactory.addClassMapping(DBLayer.getReportingClassMapping());
        sosHibernateFactory.build();
        return sosHibernateFactory.openStatelessSession();
    }

    @Test
    public void getDeleteExpiredEventItems() throws Exception {
        String confFile = "C:/Users/ur/Documents/sos-berlin.com/jobscheduler/scheduler_joc_cockpit/config/hibernate.cfg.xml";
        SOSHibernateSession session = getSession(confFile);
        SchedulerEventDBLayer schedulerEventDBLayer = new SchedulerEventDBLayer(session);
 
        schedulerEventDBLayer.beginTransaction();
        schedulerEventDBLayer.resetFilter();
        schedulerEventDBLayer.getFilter().setExpiresTo(new Date());
        schedulerEventDBLayer.delete();
        schedulerEventDBLayer.commit();
    }

    @Test
    public void getCustomEventItems() throws Exception {
        // String confFile = "R:/nobackup/junittests/hibernate/hibernate.cfg.xml";
        String confFile = "C:/Users/ur/Documents/sos-berlin.com/jobscheduler/scheduler_joc_cockpit/config/hibernate.cfg.xml";
        SOSHibernateSession session = getSession(confFile);
        SchedulerEventDBLayer schedulerEventDBLayer = new SchedulerEventDBLayer(session);
        SchedulerEventFilter schedulerEventFilter = new SchedulerEventFilter();
        schedulerEventFilter.setEventClass("test");
        schedulerEventDBLayer.setFilter(schedulerEventFilter);
        List<SchedulerEventDBItem> listOfEvents = schedulerEventDBLayer.getSchedulerEventList();
        System.out.println(listOfEvents.get(0).getEventId());
    }

}
