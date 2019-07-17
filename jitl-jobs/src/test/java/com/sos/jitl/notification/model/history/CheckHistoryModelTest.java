package com.sos.jitl.notification.model.history;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.notification.jobs.history.CheckHistoryJobOptions;
import com.sos.jitl.notification.model.ConfigTest;
import com.sos.jitl.notification.model.ModelTest;
import com.sos.jitl.notification.plugins.history.CheckHistoryTimerPlugin;

public class CheckHistoryModelTest extends ModelTest {

    private static Logger LOGGER = LoggerFactory.getLogger(CheckHistoryModelTest.class);

    public static void main(String[] args) throws Exception {
        CheckHistoryModelTest t = new CheckHistoryModelTest();

        CheckHistoryJobOptions opt = new CheckHistoryJobOptions();
        opt.hibernate_configuration_file_reporting.setValue(ConfigTest.HIBERNATE_CONFIGURATION_FILE);
        opt.schema_configuration_file.setValue(ConfigTest.SCHEMA_CONFIGURATION_FILE);
        opt.plugins.setValue(CheckHistoryTimerPlugin.class.getName());

        try {
            LOGGER.info("START");

            t.init(opt.hibernate_configuration_file_reporting.getValue());
            CheckHistoryModel model = new CheckHistoryModel(t.getSession(), opt);
            model.init();
            model.process();

            LOGGER.info("END");
        } catch (Exception ex) {
            throw ex;
        } finally {
            t.exit();
        }
    }
}
