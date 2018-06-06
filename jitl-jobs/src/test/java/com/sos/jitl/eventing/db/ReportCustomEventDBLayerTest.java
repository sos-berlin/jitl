package com.sos.jitl.eventing.db;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.lowagie.text.Document;
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
 
        SchedulerEventFilter filter = new SchedulerEventFilter();
        schedulerEventDBLayer.beginTransaction();
        filter.setExpiresTo(new Date());
        schedulerEventDBLayer.delete(filter);
        schedulerEventDBLayer.commit();
    }

    @Test
    public void getCustomEventItems() throws Exception {
        // String confFile = "R:/nobackup/junittests/hibernate/hibernate.cfg.xml";
        String confFile = "D:/documents/sos-berlin.com/scheduler_joc_cockpit/config/hibernate.cfg.xml";
        SOSHibernateSession session = getSession(confFile);
        SchedulerEventDBLayer schedulerEventDBLayer = new SchedulerEventDBLayer(session);
        SchedulerEventFilter schedulerEventFilter = new SchedulerEventFilter();
        schedulerEventFilter.setEventClass("test");
        List<SchedulerEventDBItem> listOfEvents = schedulerEventDBLayer.getSchedulerEventList(schedulerEventFilter);
        System.out.println(listOfEvents.get(0).getEventId());
    }
    
    @Test
    public void getEventsAsXml() throws Exception {
        // String confFile = "R:/nobackup/junittests/hibernate/hibernate.cfg.xml";
        String confFile = "D:/documents/sos-berlin.com/scheduler_joc_cockpit/config/hibernate.cfg.xml";
        SOSHibernateSession session = getSession(confFile);
        SchedulerEventDBLayer schedulerEventDBLayer = new SchedulerEventDBLayer(session);
        org.w3c.dom.Document d = schedulerEventDBLayer.getEventsAsXml("scheduler_joc_cockpit");
    }

}
