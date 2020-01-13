package com.sos.jitl.managed.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.managed.job.ManagedDatabaseJobOptions;

public class ManagedDatabaseModelTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedDatabaseModelTest.class);

    public static void main(String[] args) throws Exception {

        ManagedDatabaseJobOptions opt = new ManagedDatabaseJobOptions();
        opt.hibernate_configuration_file.setValue(Config.HIBERNATE_CONFIGURATION_FILE);
        opt.command.setValue("select * from REPORTING_EXECUTIONS limit 0,1");
        //opt.command.setValue("D://scheduler/config/test.sql");
        opt.resultset_as_parameters.setValue(ManagedDatabaseModel.PARAMETER_NAME_VALUE);
        opt.resultset_as_warning.setValue("false");
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            LOGGER.info("START --");
            factory = new SOSHibernateFactory(opt.hibernate_configuration_file.getValue());
            factory.setIdentifier("managed");
            factory.build();
            session = factory.openStatelessSession();

            ManagedDatabaseModel model = new ManagedDatabaseModel(session, opt, true, null);
            model.process();
            if (model.getWarning() != null) {
                LOGGER.warn("Warning: " + model.getWarning());
            }

            LOGGER.info("END --");

        } catch (Exception ex) {
            throw ex;
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
