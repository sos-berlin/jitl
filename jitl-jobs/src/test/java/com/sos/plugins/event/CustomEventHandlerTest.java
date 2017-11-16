package com.sos.plugins.event;

import java.nio.file.Paths;
import java.util.HashMap;

import com.sos.jitl.classes.event.EventHandlerSettings;
import com.sos.jitl.classes.plugin.PluginMailer;
import com.sos.scheduler.engine.eventbus.EventBus;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

public class CustomEventHandlerTest {

    public static void main(String[] args) throws Exception {

        String baseDir = "C:/sp/jobschedulers/approvals/jobscheduler_1.12-SNAPSHOT/";
        String schedulerId = "sp_4012";
        String host = "sp";
        String port = "40012";
        String configDir = baseDir + schedulerId + "/config";

        EventHandlerSettings settings = new EventHandlerSettings();
        settings.setSchedulerId(schedulerId);
        settings.setHost(host);
        settings.setHttpHost(host);
        settings.setHttpPort(port);
        settings.setConfigDirectory(Paths.get(configDir));
        settings.setLiveDirectory(settings.getConfigDirectory().resolve("live"));
        settings.setSchedulerXml(settings.getConfigDirectory().resolve("scheduler.xml"));
        settings.setHibernateConfigurationReporting(settings.getConfigDirectory().resolve("reporting.hibernate.cfg.xml"));
        settings.setTimezone("Europe/Berlin");

        SchedulerXmlCommandExecutor xmlExecutor = null;
        EventBus eventBus = null;
        CustomEventHandler eventHandler = new CustomEventHandler(xmlExecutor, eventBus);
        eventHandler.setIdentifier(CustomEventHandler.class.getSimpleName());
        try {
            PluginMailer mailer = new PluginMailer(eventHandler.getIdentifier(), new HashMap<>());
            eventHandler.onPrepare(settings);
            eventHandler.onActivate(mailer);
        } catch (Exception e) {
            throw e;
        } finally {
            eventHandler.close();
        }
    }

}
