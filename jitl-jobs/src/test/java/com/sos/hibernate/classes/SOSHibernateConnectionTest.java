package com.sos.hibernate.classes;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBLayer;


public class SOSHibernateConnectionTest {

    private static final String HIBERNATE_CONFIG_FILE = "C:/sp/jobscheduler_1.10.6-SNAPSHOT/scheduler_4444/config/hibernate.cfg.xml";
    private static final Logger LOGGER = Logger.getLogger(SOSHibernateConnectionTest.class);
    private SOSHibernateConnection connection;

    @Before
    public void init() throws Exception {
        connection = new SOSHibernateConnection(HIBERNATE_CONFIG_FILE);
        connection.addClassMapping(DBLayer.getInventoryClassMapping());
        connection.connect();
    }

    @After
    public void exit() {
        if (connection != null) {
            connection.disconnect();
        }
    }

    @Test
    public void testJITL_319() throws Exception {
        DBItemInventoryInstance instance = (DBItemInventoryInstance)connection.get(DBItemInventoryInstance.class, 2L);
        Assert.assertEquals("scheduler_4444", instance.getSchedulerId());
        LOGGER.info("***** schedulerId from DB is: expected -> scheduler_4444 - actual -> " + instance.getSchedulerId() + " *****");
    }
    
}