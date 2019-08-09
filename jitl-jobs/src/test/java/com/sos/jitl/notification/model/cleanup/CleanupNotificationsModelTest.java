package com.sos.jitl.notification.model.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.notification.jobs.cleanup.CleanupNotificationsJobOptions;
import com.sos.jitl.notification.model.ConfigTest;
import com.sos.jitl.notification.model.ModelTest;

public class CleanupNotificationsModelTest extends ModelTest {

    private static Logger LOGGER = LoggerFactory.getLogger(CleanupNotificationsModelTest.class);

    public static void main(String[] args) throws Exception {
        CleanupNotificationsModelTest t = new CleanupNotificationsModelTest();

        CleanupNotificationsJobOptions opt = new CleanupNotificationsJobOptions();
        opt.hibernate_configuration_file_reporting.setValue(ConfigTest.HIBERNATE_CONFIGURATION_FILE);
        opt.age.setValue("2w");
        try {
            LOGGER.info("START");

            t.init(opt.hibernate_configuration_file_reporting.getValue());
            CleanupNotificationsModel model = new CleanupNotificationsModel(t.getSession(), opt);
            model.process();

            LOGGER.info("END");
        } catch (Exception ex) {
            throw ex;
        } finally {
            t.exit();
        }
    }
}
