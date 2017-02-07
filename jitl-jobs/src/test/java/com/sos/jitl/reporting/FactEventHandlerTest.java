package com.sos.jitl.reporting;

import java.nio.file.Paths;

import com.sos.jitl.classes.event.EventHandlerSettings;
import com.sos.jitl.reporting.plugin.FactEventHandler;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public class FactEventHandlerTest {

	public static void main(String[] args) throws Exception {

		String baseDir = "D:/Arbeit/scheduler/jobscheduler_data/";
		String schedulerId = "re-dell_4444_jobscheduler.1.11x64-snapshot";
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
		settings.setHibernateConfigurationReporting(settings.getConfigDirectory().resolve("hibernate.cfg.xml"));
		settings.setHibernateConfigurationScheduler(settings.getConfigDirectory().resolve("hibernate.cfg.xml"));
		settings.setMasterUrl("http://" + settings.getHost() + ":" + settings.getHttpPort());
		settings.setTimezone("Europe/Berlin");

		FactEventHandler eventHandler = new FactEventHandler();
		eventHandler.setIdentifier("reporting");
		try {
			SchedulerXmlCommandExecutor xmlExecutor = null;
			VariableSet variables = null;

			eventHandler.onPrepare(xmlExecutor, variables, settings);
			eventHandler.onActivate();
		} catch (Exception e) {
			throw e;
		} finally {
			eventHandler.close();
		}
	}

}
