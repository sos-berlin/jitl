package com.sos.jitl.reporting.db;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;

public class DBLayerReportingTest {

    final Logger LOGGER = LoggerFactory.getLogger(DBLayerReporting.class);

    private SOSHibernateFactory factory;
    private SOSHibernateSession session;

    @Before
    public void setUp() throws Exception {
        String hibernate = "src/test/resources/hibernate.cfg.xml";
        factory = new SOSHibernateFactory(hibernate);
        factory.setIdentifier("reporting");
        factory.setAutoCommit(false);
        factory.addClassMapping(DBLayer.getReportingClassMapping());
        factory.addClassMapping(DBLayer.getInventoryClassMapping());
        factory.build();

        session = factory.openStatelessSession();
    }

    @After
    public void tearDown() throws Exception {
        if (session != null) {
            session.close();
        }
        if (factory != null) {
            factory.close();
        }
    }

    @Ignore
    @Test
    public void testOrderInventoryInfo() throws Exception {

        try {
            DBLayerReporting layer = new DBLayerReporting(session);

            session.beginTransaction();

            String schedulerId = "1.13.x.x64-snapshot";
            String schedulerHostname = "localhost";
            int schedulerHttpPort = 40444;
            String orderId = "Cleanup";
            String jobChainName = "/sos/notification/CleanupNotifications";

            LOGGER.info(layer.getInventoryOrderInfoByJobChain(schedulerId, schedulerHostname, schedulerHttpPort, orderId, jobChainName).toString());

            session.commit();
        } catch (Exception e) {
            try {
                session.rollback();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }

    }

}
