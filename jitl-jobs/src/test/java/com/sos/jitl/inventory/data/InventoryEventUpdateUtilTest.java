package com.sos.jitl.inventory.data;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.jitl.reporting.db.DBLayer;

public class InventoryEventUpdateUtilTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryEventUpdateUtilTest.class);
    // test with local MySQL DB
    private String hibernateCfgFile = "C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT1/sp_41110x1/config/hibernate.cfg.xml"; 
    // test with local PostgreSQL DB
//    private String hibernateCfgFile = "C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT3/sp_41110x3/config/hibernate.cfg.xml"; 
    
    @Test
    public void testExecute() {
        try {
            SOSHibernateFactory factory = new SOSHibernateFactory(hibernateCfgFile);
            factory.setAutoCommit(false);
            factory.addClassMapping(DBLayer.getInventoryClassMapping());
            factory.build();
            InventoryEventUpdateUtil eventUpdates = new InventoryEventUpdateUtil("SP", 40119, factory);
            eventUpdates.execute();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
}
