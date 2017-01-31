package com.sos.jitl.reporting;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.reporting.plugin.FactEventHandler;
import com.sos.jitl.reporting.plugin.PluginSettings;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

public class FactEventHandlerTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(FactEventHandlerTest.class);

	public static void main(String[] args) throws Exception {

		String configDir = "D:/Arbeit/scheduler/jobscheduler_data/re-dell_4444_jobscheduler.1.11x64-snapshot/config";

		PluginSettings settings = new PluginSettings();
		settings.setLiveDirectory(Paths.get(configDir + "/live"));
		settings.setReportingHibernateConfigPath(Paths.get(configDir + "/hibernate.cfg.xml"));
		settings.setSchedulerHibernateConfigPath(Paths.get(configDir + "/hibernate.cfg.xml"));
		settings.setSchedulerXmlPath(Paths.get(configDir + "/scheduler.xml"));
		settings.setMasterUrl("http://re-dell:" + settings.getHttpPort());
		settings.setSchedulerId("re-dell_4444_jobscheduler.1.11x64-snapshot");
		settings.setHostname("re-dell");
		settings.setHttpPort("40444");

		settings.setTimezone("Europe/Berlin");
		settings.setSchedulerAnswerXml(null);
		settings.setSchedulerAnswerXpath(null);

		FactEventHandler eventHandler = new FactEventHandler();
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
