package com.sos.hibernate.classes;

import static org.junit.Assert.*;

import java.util.List;

import org.hibernate.Session;
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
    SOSHibernateConnection connection;

    @After
    public void exit() {
        if (connection != null) {
            connection.disconnect();
        }
    }

    @Test
    public void testJITL_319() throws Exception {
        connection = new SOSHibernateConnection(HIBERNATE_CONFIG_FILE);
        connection.addClassMapping(DBLayer.getInventoryClassMapping());
        connection.connect();
        DBItemInventoryInstance instance = (DBItemInventoryInstance)connection.get(DBItemInventoryInstance.class, 2L);
        Assert.assertEquals("scheduler_4444", instance.getSchedulerId());
        LOGGER.info("***** schedulerId from DB is: expected -> scheduler_4444 - actual -> " + instance.getSchedulerId() + " *****");
    }
 
    @Test
    public void testConnectToDatabase() throws Exception {

        sosHibernateDBLayer = new SOSHibernateDBLayer(HIBERNATE_CONFIG_FILE);
        connection = sosHibernateDBLayer.getConnection();
        connection.getConfiguration().addAnnotatedClass(DailyPlanDBItem.class);

        Query query = null;
        List<DailyPlanDBItem> daysScheduleList = null;
        query = connection.createQuery(" from DailyPlanDBItem where 1=1");

        query.setMaxResults(2);
        daysScheduleList = query.list();
        Long id = daysScheduleList.get(0).getId();
        DailyPlanDBItem dailyPlanDBItem = (DailyPlanDBItem) ((Session) connection.getCurrentSession()).get(DailyPlanDBItem.class, id);
    }

    @Test
    public void testReConnectToDatabase() throws Exception {

        sosHibernateDBLayer = new SOSHibernateDBLayer(HIBERNATE_CONFIG_FILE);
        connection = sosHibernateDBLayer.getConnection();

        connection.setAutoCommit(true);
        connection.setIgnoreAutoCommitTransactions(true);
        connection.setUseOpenStatelessSession(true);

        connection.getConfiguration().addAnnotatedClass(DailyPlanDBItem.class);

        connection.reconnect();
        
        Query query = null;
        List<DailyPlanDBItem> daysScheduleList = null;
        query = connection.createQuery("from DailyPlanDBItem where 1=0");

        query.setMaxResults(2);
        daysScheduleList = query.list();
        Long id = daysScheduleList.get(0).getId();
        DailyPlanDBItem dailyPlanDBItem = (DailyPlanDBItem) ((StatelessSession) connection.getCurrentSession()).get(DailyPlanDBItem.class, id);

    }

    @Test
    public void testConnect() throws Exception {
        SOSHibernateConnection sosHibernateConnection;

        String confFile = HIBERNATE_CONFIG_FILE;
        sosHibernateConnection = new SOSHibernateConnection(confFile);
        sosHibernateConnection.setAutoCommit(true);
        sosHibernateConnection.setIgnoreAutoCommitTransactions(true);
        sosHibernateConnection.setUseOpenStatelessSession(true);
        sosHibernateConnection.connect();
    }

}
