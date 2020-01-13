package com.sos.hibernate.classes;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.hibernate.StatelessSession;
import org.hibernate.query.Query;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.layer.SOSHibernateDBLayer;
import com.sos.jitl.dailyplan.db.DailyPlanDBItem;
import com.sos.jitl.inventory.db.DBLayerInventory;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBLayer;
 
public class SOSHibernateConnectionTest {
    private static final String HIBERNATE_CONFIG_FILE = "src/test/resources/hibernate.cfg.xml";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateConnectionTest.class);

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
        DBItemInventoryInstance instance = (DBItemInventoryInstance)sosHibernateSession.get(DBItemInventoryInstance.class, 3L);
        Assert.assertEquals("JobScheduler.1.10_4110", instance.getSchedulerId());
        LOGGER.info("***** schedulerId from DB is: expected -> JobScheduler.1.10_4110 - actual -> " + instance.getSchedulerId() + " *****");
    }
 
    @Test
    public void testConnectToDatabase() throws Exception {

        sosHibernateDBLayer = new SOSHibernateDBLayer();
        sosHibernateDBLayer.createStatelessConnection(HIBERNATE_CONFIG_FILE);
        sosHibernateSession = sosHibernateDBLayer.getSession();
        sosHibernateSession.getFactory().getConfiguration().addAnnotatedClass(DailyPlanDBItem.class);

        Query<DailyPlanDBItem> query = sosHibernateSession.createQuery(" from DailyPlanDBItem where 1=1");

        query.setMaxResults(2);
        List<DailyPlanDBItem> daysScheduleList = query.getResultList();
        Long id = daysScheduleList.get(0).getId();
        @SuppressWarnings("unused")
        DailyPlanDBItem dailyPlanDBItem = (DailyPlanDBItem) sosHibernateSession.get(DailyPlanDBItem.class, id);
    }

    @Test
    public void testReConnectToDatabase() throws Exception {

        sosHibernateDBLayer = new SOSHibernateDBLayer();
        SOSHibernateFactory sosHibernateFactory = new SOSHibernateFactory(HIBERNATE_CONFIG_FILE);
        sosHibernateDBLayer.createStatelessConnection(HIBERNATE_CONFIG_FILE);
        sosHibernateSession = sosHibernateDBLayer.getSession();
        sosHibernateFactory.build();
        sosHibernateSession.getFactory().getConfiguration().addAnnotatedClass(DailyPlanDBItem.class);

        sosHibernateSession.reopen();
        
        Query<DailyPlanDBItem> query = sosHibernateSession.createQuery("from DailyPlanDBItem where 1=0");

        query.setMaxResults(2);
        List<DailyPlanDBItem> daysScheduleList = query.getResultList();
        Long id = daysScheduleList.get(0).getId();
        @SuppressWarnings("unused")
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

    @Test
    public void testJITL_420() {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = new SOSHibernateFactory(HIBERNATE_CONFIG_FILE);
            factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            factory.addClassMapping(DBLayer.getInventoryClassMapping());
            factory.build();

            LOGGER.info(factory.getDialect().toString());
            session = factory.openStatelessSession();
            DBLayerInventory dbLayer = new DBLayerInventory(session);
            DBItemInventoryInstance instance = dbLayer.getInventoryInstance(5L);
            session.save(instance);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.close();
            }
            if (factory != null) {
                factory.close();
            }
        }
    }

}
