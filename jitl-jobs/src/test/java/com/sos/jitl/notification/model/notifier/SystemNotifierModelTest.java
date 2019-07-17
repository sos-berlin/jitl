package com.sos.jitl.notification.model.notifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.notification.jobs.notifier.SystemNotifierJobOptions;
import com.sos.jitl.notification.model.ConfigTest;
import com.sos.jitl.notification.model.ModelTest;

public class SystemNotifierModelTest extends ModelTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemNotifierModelTest.class);

    public static void main(String[] args) throws Exception {
        SystemNotifierModelTest t = new SystemNotifierModelTest();

        SystemNotifierJobOptions opt = new SystemNotifierJobOptions();
        opt.hibernate_configuration_file_reporting.setValue(ConfigTest.HIBERNATE_CONFIGURATION_FILE);
        opt.schema_configuration_file.setValue(ConfigTest.SCHEMA_CONFIGURATION_FILE);
        opt.system_configuration_file.setValue(ConfigTest.SYSTEM_CONFIGURATION_FILE);
        opt.scheduler_mail_settings.setValue(getMailOptions());

        try {
            LOGGER.info("START");

            t.init(opt.hibernate_configuration_file_reporting.getValue());
            SystemNotifierModel model = new SystemNotifierModel(t.getSession(), opt, null);
            model.process();

            LOGGER.info("END");
        } catch (Exception ex) {
            throw ex;
        } finally {
            t.exit();
        }
    }
}
