package com.sos.jitl.reporting.plugin;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.exception.InvalidDataException;
import com.sos.exception.NoResponseException;
import com.sos.scheduler.engine.kernel.plugin.AbstractPlugin;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.scheduler.engine.kernel.variable.VariableSet;

import sos.xml.SOSXMLXPath;

public class ReportingPlugin extends AbstractPlugin {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportingPlugin.class);
	private static final String DUMMY_COMMAND = "<show_state subsystems=\"folder\" what=\"folders cluster no_subfolders\" path=\"/any/path/that/does/not/exists\" />";
	private static final String HIBERNATE_CONFIG_FILE_NAME = "hibernate.cfg.xml";

	private SchedulerXmlCommandExecutor xmlCommandExecutor;
	private VariableSet variableSet;

	private IReportingEventHandler eventHandler;
	private PluginSettings settings;

	private ExecutorService fixedThreadPoolExecutor = Executors.newFixedThreadPool(1);
	private String proxyUrl;

	public ReportingPlugin(SchedulerXmlCommandExecutor xmlCommandExecutor, VariableSet variables) {
		this.xmlCommandExecutor = xmlCommandExecutor;
		this.variableSet = variables;
	}
	
	public void executeOnPrepare(IReportingEventHandler handler) {
		try {
			eventHandler = handler;
			
			setProxyUrl();
			Runnable thread = new Runnable() {
				@Override
				public void run() {
					try {
						init();
						eventHandler.onPrepare(xmlCommandExecutor, variableSet, settings);
					} catch (Exception e) {
						LOGGER.error(e.toString(), e);
					}
				}
			};
			fixedThreadPoolExecutor.submit(thread);
		} catch (Exception e) {
			try {
				eventHandler.close();
			} catch (Exception e1) {
				LOGGER.warn(e1.toString(), e1);
			}
			LOGGER.error("Fatal Error in @OnPrepare:" + e.toString(), e);
		}
		super.onPrepare();
	}

	public void executeOnActivate() {
		try {
			Runnable thread = new Runnable() {
				@Override
				public void run() {
					try {
						eventHandler.onActivate();
					} catch (Exception e) {
						LOGGER.error(e.toString(), e);
					}
				}
			};
			fixedThreadPoolExecutor.submit(thread);
		} catch (Exception e) {
			try {
				eventHandler.close();
			} catch (Exception e1) {
				LOGGER.warn(e1.toString(), e1);
			}
			LOGGER.error("Fatal Error in OnActivate:" + e.toString(), e);
		}
		super.onActivate();
	}

	public void executeClose() {

		try {
			eventHandler.close();
		} catch (Exception e1) {
			LOGGER.warn(e1.toString(), e1);
		}

		try {
			fixedThreadPoolExecutor.shutdownNow();
			boolean shutdown = fixedThreadPoolExecutor.awaitTermination(1L, TimeUnit.SECONDS);
			if (shutdown) {
				LOGGER.debug("Thread has been shut down correctly.");
			} else {
				LOGGER.debug("Thread has ended due to timeout on shutdown. Doesn´t wait for answer from thread.");
			}
		} catch (InterruptedException e) {
			LOGGER.error(e.toString(), e);
		}
		super.close();
	}

	private void init() throws Exception {

		settings = new PluginSettings();
		for (int i = 0; i < 120; i++) {
			try {
				Thread.sleep(1000);
				settings.setSchedulerAnswerXml(executeXML(DUMMY_COMMAND));
				if (settings.getSchedulerAnswerXml() != null && !settings.getSchedulerAnswerXml().isEmpty()) {
					settings.setSchedulerAnswerXpath(new SOSXMLXPath(new StringBuffer(settings.getSchedulerAnswerXml())));
					String state = settings.getSchedulerAnswerXpath().selectSingleNodeValue("/spooler/answer/state/@state");
					if ("running,waiting_for_activation,paused".contains(state)) {
						break;
					}
				}
			} catch (Exception e) {
				LOGGER.error("", e);
			}
		}

		if (settings.getSchedulerAnswerXml() == null || settings.getSchedulerAnswerXml().isEmpty()) {
			throw new NoResponseException("JobScheduler doesn't response the state");
		}
		
		LOGGER.debug(settings.getSchedulerAnswerXml());
		
		settings.setSchedulerXmlPath(Paths.get(settings.getSchedulerAnswerXpath().selectSingleNodeValue("/spooler/answer/state/@config_file")));
		if (settings.getSchedulerXmlPath() == null) {
			throw new InvalidDataException("Couldn't determine path of scheduler.xml");
		}
		if (!Files.exists(settings.getSchedulerXmlPath())) {
			throw new IOException(String.format("Configuration file %1$s doesn't exist", settings.getSchedulerXmlPath()));
		}

		// TODO consider scheduler.xml to get "live" directory in
		// /spooler/config/@configuration_directory
		Path configDirectory = settings.getSchedulerXmlPath().getParent();
		if (configDirectory == null) {
			throw new InvalidDataException("Couldn't determine \"config\" directory.");
		}

		settings.setLiveDirectory(configDirectory.resolve("live"));
		settings.setSchedulerHibernateConfigPath(configDirectory.resolve(HIBERNATE_CONFIG_FILE_NAME));
		settings.setReportingHibernateConfigPath(settings.getSchedulerHibernateConfigPath());
		settings.setSchedulerId(settings.getSchedulerAnswerXpath().selectSingleNodeValue("/spooler/answer/state/@spooler_id"));
		settings.setHostname(settings.getSchedulerAnswerXpath().selectSingleNodeValue("/spooler/answer/state/@host"));
		settings.setTimezone(settings.getSchedulerAnswerXpath().selectSingleNodeValue("/spooler/answer/state/@time_zone"));
		settings.setHttpPort(settings.getSchedulerAnswerXpath().selectSingleNodeValue("/spooler/answer/state/@http_port", "40444"));
		if(settings.getSchedulerId() == null || settings.getSchedulerId().isEmpty()){
			throw new Exception("Missing @spooler_id in the scheduler answer");
		}
		if(settings.getHostname() == null || settings.getHostname().isEmpty()){
			throw new Exception("Missing @host in the scheduler answer");
		}
		if(settings.getHttpPort() == null || settings.getHttpPort().isEmpty()){
			throw new Exception("Missing @http_port in the scheduler answer");
		}
		try {
			settings.setMasterUrl(getMasterUrl(settings.getSchedulerAnswerXpath()));
		} catch (Exception e) {
			throw new InvalidDataException("Couldn't determine JobScheduler http url", e);
		}
	}

	private String executeXML(String xmlCommand) {
		if (xmlCommandExecutor != null) {
			return xmlCommandExecutor.executeXml(xmlCommand);
		} else {
			LOGGER.error("xmlCommandExecutor is null");
		}
		return null;
	}

	private void setProxyUrl() {
		if (variableSet.apply("sos.proxy_url") != null && !variableSet.apply("sos.proxy_url").isEmpty()) {
			this.proxyUrl = variableSet.apply("sos.proxy_url");
		}
	}

	private String getMasterUrl(SOSXMLXPath xPath) throws Exception {
		if (proxyUrl != null) {
			return proxyUrl;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("http://");
		sb.append(InetAddress.getLocalHost().getCanonicalHostName().toLowerCase());
		sb.append(":");
		sb.append(settings.getHttpPort());
		return sb.toString();
	}
}