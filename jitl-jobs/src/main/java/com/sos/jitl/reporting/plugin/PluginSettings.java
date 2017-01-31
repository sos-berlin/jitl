package com.sos.jitl.reporting.plugin;

import java.nio.file.Path;

import sos.xml.SOSXMLXPath;

public class PluginSettings {
	private SOSXMLXPath schedulerAnswerXpath;
	private String schedulerAnswerXml;
	private Path liveDirectory;
	private String masterUrl;
	private Path schedulerHibernateConfigPath;
	private Path reportingHibernateConfigPath;
	private Path schedulerXmlPath;
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

	public String getMasterUrl() {
		return masterUrl;
	}

	public void setMasterUrl(String val) {
		this.masterUrl = val;
	}

	public Path getSchedulerHibernateConfigPath() {
		return schedulerHibernateConfigPath;
	}

	public void setSchedulerHibernateConfigPath(Path val) {
		this.schedulerHibernateConfigPath = val;
	}

	public Path getReportingHibernateConfigPath() {
		return reportingHibernateConfigPath;
	}

	public void setReportingHibernateConfigPath(Path val) {
		this.reportingHibernateConfigPath = val;
	}

	public Path getSchedulerXmlPath() {
		return schedulerXmlPath;
	}

	public void setSchedulerXmlPath(Path val) {
		this.schedulerXmlPath = val;
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
