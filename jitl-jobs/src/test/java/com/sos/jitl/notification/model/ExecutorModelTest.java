package com.sos.jitl.notification.model;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.notification.helper.settings.InternalNotificationSettings;
import com.sos.jitl.notification.helper.settings.MailSettings;
import com.sos.jitl.notification.model.internal.ExecutorModel;
import com.sos.jitl.notification.model.internal.ExecutorModel.InternalType;

public class ExecutorModelTest {

    private static Logger LOGGER = LoggerFactory.getLogger(ExecutorModelTest.class);

    public static void main(String[] args) throws Exception {

        try {
            LOGGER.info("START --");

            MailSettings mailSettings = new MailSettings();
            mailSettings.setIniPath(Paths.get(Config.CONFIG_DIR, "factory.ini").toFile().getCanonicalPath());
            mailSettings.setSmtp("localhost");
            mailSettings.setQueueDir(Paths.get(Config.CONFIG_DIR, "mail").toFile().getCanonicalPath());
            mailSettings.setFrom("scheduler@localhost");
            mailSettings.setTo("user@localhost");
            // mailSettings.setCc();
            // mailSettings.setBcc();

            ExecutorModel model = new ExecutorModel(Paths.get(Config.CONFIG_DIR), Paths.get(Config.HIBERNATE_CONFIGURATION_FILE), mailSettings);

            InternalNotificationSettings settings = new InternalNotificationSettings();
            settings.setSchedulerId("1.12.x");
            settings.setTaskId("12345");
            settings.setMessageCode("SCHEDULER-123");
            settings.setMessage("xxx xxx xx x x");

            boolean ok = model.process(InternalType.TASK_IF_LONGER_THAN, settings);
            LOGGER.info("END -- " + ok);

        } catch (Exception ex) {
            throw ex;
        }

    }

}
