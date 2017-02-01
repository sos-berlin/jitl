package com.sos.jitl.classes.event;

import java.nio.file.Path;

import sos.xml.SOSXMLXPath;

public class EventHandlerSettings {
	private SOSXMLXPath schedulerAnswerXpath;
	private String schedulerAnswerXml;
	private Path configDirectory;
	private Path liveDirectory;
	private Path hibernateConfigurationScheduler;
	private Path hibernateConfigurationReporting;
	private Path schedulerXml;
	private String masterUrl;
	private String httpPort;
	private String schedulerId;
	private String hostname;
	private String timezone;

	public SOSXMLXPath getSchedulerAnswerXpath() {
		return schedulerAnswerXpath;
	}

	public void setSchedulerAnswerXpath(SOSXMLXPath val) {
		this.schedulerAnswerXpath = val;
	}

	public String getSchedulerAnswerXml() {
		return schedulerAnswerXml;
	}

	public void setSchedulerAnswerXml(String val) {
		this.schedulerAnswerXml = val;
	}

	public Path getLiveDirectory() {
		return liveDirectory;
	}

	public void setLiveDirectory(Path val) {
		this.liveDirectory = val;
	}

	public Path getConfigDirectory() {
		return configDirectory;
	}

	public void setConfigDirectory(Path val) {
		this.configDirectory = val;
	}

	public String getMasterUrl() {
		return masterUrl;
	}

	public void setMasterUrl(String val) {
		this.masterUrl = val;
	}

	public Path getHibernateConfigurationScheduler() {
		return hibernateConfigurationScheduler;
	}

	public void setHibernateConfigurationScheduler(Path val) {
		this.hibernateConfigurationScheduler = val;
	}

	public Path getHibernateConfigurationReporting() {
		return hibernateConfigurationReporting;
	}

	public void setHibernateConfigurationReporting(Path val) {
		this.hibernateConfigurationReporting = val;
	}

	public Path getSchedulerXml() {
		return schedulerXml;
	}

	public void setSchedulerXml(Path val) {
		this.schedulerXml = val;
	}

	public String getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(String val) {
		this.httpPort = val;
	}

	public String getSchedulerId() {
		return schedulerId;
	}

	public void setSchedulerId(String val) {
		this.schedulerId = val;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String val) {
		this.hostname = val;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String val) {
		this.timezone = val;
	}

}
