package com.sos.jitl.managed.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.managed.model.ManagedDatabaseModel;

import sos.spooler.Variable_set;

public class ManagedDatabaseJob extends JSJobUtilitiesClass<ManagedDatabaseJobOptions> {

    private static Logger LOGGER = LoggerFactory.getLogger(ManagedDatabaseJob.class);
    ManagedDatabaseModel model;

    public ManagedDatabaseJob() {
        super(new ManagedDatabaseJobOptions());
    }

    public void execute(boolean isOrder, Variable_set orderParams) throws Exception {
        final String methodName = ManagedDatabaseJob.class.getSimpleName() + "::execute";

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            getOptions().checkMandatory();
            LOGGER.debug(getOptions().toString());

            factory = new SOSHibernateFactory(getOptions().hibernate_configuration_file.getValue());
            factory.setIdentifier("managed");
            factory.build();
            session = factory.openStatelessSession();

            model = new ManagedDatabaseModel(session, getOptions(), isOrder, orderParams);
            model.process();
        } catch (Exception e) {
            LOGGER.error(String.format("%s: %s", methodName, e.toString()), e);
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
            if (factory != null) {
                factory.close();
            }
        }
    }

    public ManagedDatabaseJobOptions getOptions() {
        if (objOptions == null) {
            objOptions = new ManagedDatabaseJobOptions();
        }
        return objOptions;
    }

    public ManagedDatabaseModel getModel() {
        return model;
    }
}
