package com.sos.hibernate.classes;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.hibernate.StatelessSession;
import org.hibernate.query.Query;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.apache.log4j.Logger;

import com.sos.hibernate.layer.SOSHibernateDBLayer;
import com.sos.jitl.dailyplan.db.DailyPlanDBItem;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBLayer;
 
public class SOSHibernateConnectionTest {
    // private static final String HIBERNATE_CONFIG_FILE =
    // "R:/nobackup/junittests/hibernate/hibernate.cfg.xml";
    private static final String HIBERNATE_CONFIG_FILE = "C:/Users/ur/Documents/sos-berlin.com/jobscheduler/scheduler_joc_cockpit/config/hibernate.cfg.xml";
    // private static final String HIBERNATE_CONFIG_FILE =
    // "C:/sp/jobscheduler_1.10.6-SNAPSHOT/scheduler_4444/config/hibernate.cfg.xml";
    private static final Logger LOGGER = Logger.getLogger(SOSHibernateConnectionTest.class);

    SOSHibernateDBLayer sosHibernateDBLayer;
    SOSHibernateSession sosHibernateSession;

    @After
    public void exit() {
        if (sosHibernateSession != null) {
            sosHibernateSession.close();
        }
    }

    @Test
    public void testJITL_319() throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(HIBERNATE_CONFIG_FILE);
        factory.addClassMapping(DBLayer.getInventoryClassMapping());
        factory.build();
        sosHibernateSession= factory.openSession();
        DBItemInventoryInstance instance = (DBItemInventoryInstance)sosHibernateSession.get(DBItemInventoryInstance.class, 2L);
        Assert.assertEquals("scheduler_4444", instance.getSchedulerId());
        LOGGER.info("***** schedulerId from DB is: expected -> scheduler_4444 - actual -> " + instance.getSchedulerId() + " *****");
    }
 
    @Test
    public void testConnectToDatabase() throws Exception {

        sosHibernateDBLayer = new SOSHibernateDBLayer();
        sosHibernateDBLayer.createStatelessConnection(HIBERNATE_CONFIG_FILE);
        sosHibernateSession = sosHibernateDBLayer.getSession();
        sosHibernateSession.getFactory().getConfiguration().addAnnotatedClass(DailyPlanDBItem.class);

        Query query = null;
        List<DailyPlanDBItem> daysScheduleList = null;
        query = sosHibernateSession.createQuery(" from DailyPlanDBItem where 1=1");

        query.setMaxResults(2);
        daysScheduleList = query.list();
        Long id = daysScheduleList.get(0).getId();
        DailyPlanDBItem dailyPlanDBItem = (DailyPlanDBItem) sosHibernateSession.get(DailyPlanDBItem.class, id);
    }

    @Test
    public void testReConnectToDatabase() throws Exception {

        SOSHibernateFactory sosHibernateFactory = new SOSHibernateFactory(HIBERNATE_CONFIG_FILE);
        sosHibernateFactory.build();
        sosHibernateSession.getFactory().getConfiguration().addAnnotatedClass(DailyPlanDBItem.class);

        sosHibernateSession.reconnect();
        
        Query query = null;
        List<DailyPlanDBItem> daysScheduleList = null;
        query = sosHibernateSession.createQuery("from DailyPlanDBItem where 1=0");

        query.setMaxResults(2);
        daysScheduleList = query.list();
        Long id = daysScheduleList.get(0).getId();
        DailyPlanDBItem dailyPlanDBItem = (DailyPlanDBItem) ((StatelessSession) sosHibernateSession.getCurrentSession()).get(DailyPlanDBItem.class, id);

    }

    @Test
    public void testConnect() throws Exception {
        SOSHibernateFactory sosHibernateConnection;

        String confFile = HIBERNATE_CONFIG_FILE;
        sosHibernateConnection = new SOSHibernateFactory(confFile);
        sosHibernateConnection.setAutoCommit(true);
        sosHibernateConnection.build();
    }


    @Test
    public void testSaveOrUpdate() throws Exception {
        SOSHibernateFactory sosHibernateFactory;

        String confFile = HIBERNATE_CONFIG_FILE;
        sosHibernateFactory = new SOSHibernateFactory(confFile);
 
        sosHibernateFactory.addClassMapping(DBLayer.getReportingClassMapping());
        sosHibernateFactory.build();
        sosHibernateSession = sosHibernateFactory.openStatelessSession();
        DailyPlanDBItem dailyPlanDBItem = new DailyPlanDBItem();
        dailyPlanDBItem.setJob("test");
        dailyPlanDBItem.setSchedulerId("schedulerId");
        dailyPlanDBItem.setPlannedStart(new Date());
        dailyPlanDBItem.setIsAssigned(false);
        dailyPlanDBItem.setIsLate(false);
        dailyPlanDBItem.setCreated(new Date());
        dailyPlanDBItem.setModified(new Date());
       
        sosHibernateSession.beginTransaction();
        sosHibernateSession.saveOrUpdate(dailyPlanDBItem);
        sosHibernateSession.commit();
        
        DailyPlanDBItem dailyPlanDBItem2 = (DailyPlanDBItem) sosHibernateSession.get(DailyPlanDBItem.class, dailyPlanDBItem.getId());
        Assert.assertEquals("DailyPlanDBItem", dailyPlanDBItem2.getJob(),"test");
        sosHibernateSession.beginTransaction();
        sosHibernateSession.delete(dailyPlanDBItem2);
        sosHibernateSession.commit();
        
        dailyPlanDBItem2.setJob("test2");
        sosHibernateSession.beginTransaction();
        sosHibernateSession.saveOrUpdate(dailyPlanDBItem2);
        sosHibernateSession.commit();
        
        DailyPlanDBItem dailyPlanDBItem3 = (DailyPlanDBItem) sosHibernateSession.get(DailyPlanDBItem.class, dailyPlanDBItem2.getId());
        Assert.assertEquals("DailyPlanDBItem", dailyPlanDBItem3.getJob(),"test2");
        
        sosHibernateSession.beginTransaction();
        sosHibernateSession.delete(dailyPlanDBItem3);
        sosHibernateSession.commit();
        
        
        
        sosHibernateSession.close();
        
    }
}
