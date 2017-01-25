package com.sos.jitl.inventory.data;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.jitl.reporting.db.DBLayer;

public class InventoryEventUpdateUtilTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryEventUpdateUtilTest.class);
    private String hibernateCfgFile = "C:/sp/jobschedulers/DB-test/jobscheduler_1.11.0-SNAPSHOT3/sp_41110x3/config/hibernate.cfg.xml"; 
    
    @Test
    public void testExecute() {
        try {
            SOSHibernateFactory factory = new SOSHibernateFactory(hibernateCfgFile);
            factory.setAutoCommit(false);
            factory.setIgnoreAutoCommitTransactions(true);
            factory.addClassMapping(DBLayer.getInventoryClassMapping());
            factory.build();
            SOSHibernateConnection session = new SOSHibernateConnection(factory);
            session.connect();
            InventoryEventUpdateUtil eventUpdates = new InventoryEventUpdateUtil("SP", 40117, session);
            eventUpdates.execute();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
}
