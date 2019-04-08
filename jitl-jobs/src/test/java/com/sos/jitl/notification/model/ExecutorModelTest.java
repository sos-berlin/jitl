package com.sos.jitl.notification.model;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.notification.model.internal.ExecutorModel;
import com.sos.jitl.notification.model.internal.ExecutorModel.InternalType;
import com.sos.jitl.notification.model.internal.Settings;

public class ExecutorModelTest {

    private static Logger LOGGER = LoggerFactory.getLogger(ExecutorModelTest.class);

    public static void main(String[] args) throws Exception {

        try {
            LOGGER.info("START --");

            ExecutorModel model = new ExecutorModel(Paths.get(Config.CONFIG_DIR), Paths.get(Config.HIBERNATE_CONFIGURATION_FILE));

            Settings settings = new Settings();
            settings.setSchedulerId("test");
            settings.setTaskId("12345");
            settings.setMessage("xxx xxx xx x x");

            boolean ok = model.process(InternalType.TASK_IF_LONGER_THAN, settings);

            LOGGER.info("END -- " + ok);

        } catch (Exception ex) {
            throw ex;
        }

    }

}
