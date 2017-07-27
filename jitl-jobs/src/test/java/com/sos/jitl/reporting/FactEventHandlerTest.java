package com.sos.jitl.reporting;

import java.nio.file.Paths;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.classes.event.EventHandlerSettings;
import com.sos.jitl.classes.plugin.PluginMailer;
import com.sos.jitl.reporting.plugin.FactEventHandler;
import com.sos.scheduler.engine.eventbus.EventBus;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

import sos.scheduler.job.JobSchedulerJob;
import sos.xml.SOSXMLXPath;

public class FactEventHandlerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FactEventHandlerTest.class);

    public static void main(String[] args) throws Exception {

        String baseDir = "D:/Arbeit/scheduler/jobscheduler_data/";
        String schedulerId = "re-dell_4444_jobscheduler.1.11.x.x64-snapshot";
        String host = "re-dell";
        String port = "40444";
        String configDir = baseDir + schedulerId + "/config";

        EventHandlerSettings settings = new EventHandlerSettings();
        settings.setSchedulerId(schedulerId);
        settings.setHost(host);
        settings.setHttpPort(port);
        settings.setConfigDirectory(Paths.get(configDir));
        settings.setLiveDirectory(settings.getConfigDirectory().resolve("live"));
        settings.setSchedulerXml(settings.getConfigDirectory().resolve("scheduler.xml"));
        settings.setHibernateConfigurationReporting(settings.getConfigDirectory().resolve("reporting.hibernate.cfg.xml"));
        settings.setHibernateConfigurationScheduler(settings.getConfigDirectory().resolve("hibernate.cfg.xml"));
        settings.setTimezone("Europe/Berlin");

        boolean useNotification = false;
        try {
            SOSXMLXPath xpath = new SOSXMLXPath(settings.getSchedulerXml());
            String useNotificationParam = xpath.selectSingleNodeValue("/spooler/config/params/param[@name='"
                    + JobSchedulerJob.SCHEDULER_PARAM_USE_NOTIFICATION + "']/@value");
            useNotification = Boolean.parseBoolean(useNotificationParam);
        } catch (Exception e) {
            LOGGER.error(String.format("exception on evaluate %s from scheduler.xml: %s", JobSchedulerJob.SCHEDULER_PARAM_USE_NOTIFICATION, e
                    .toString()));
        }

        SchedulerXmlCommandExecutor xmlExecutor = null;
        EventBus eventBus = null;
        FactEventHandler eventHandler = new FactEventHandler(xmlExecutor, eventBus);
        eventHandler.setUseNotificationPlugin(useNotification);
        eventHandler.setIdentifier("reporting");
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
