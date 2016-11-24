package com.sos.jitl.inventory.data;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBLayer;

public class InventoryEventUpdateUtilTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryEventUpdateUtilTest.class);
    private String url = "http://sp.sos:40441";
    private String hibernateCfgFile = "C:/sp/jobschedulers/cluster/primary/sp_scheduler_cluster/config/hibernate.cfg.xml"; 
    
    @Test
    public void testExecute() {
        try {
            SOSHibernateConnection connection = new SOSHibernateConnection(hibernateCfgFile);
            connection.setAutoCommit(false);
            connection.setIgnoreAutoCommitTransactions(true);
            connection.addClassMapping(DBLayer.getInventoryClassMapping());
            connection.connect();
            InventoryEventUpdateUtil eventUpdates = new InventoryEventUpdateUtil(url, connection);
            eventUpdates.execute();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
