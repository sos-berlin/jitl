package com.sos.jitl.classes.event;

import java.nio.file.Path;

import sos.xml.SOSXMLXPath;

public class EventHandlerSettings {
	private Path configDirectory;
	private Path hibernateConfigurationReporting;
	private Path hibernateConfigurationScheduler;
	private String host;
	private String httpPort;
	private String httpsPort;
	private Path liveDirectory;
	private String masterUrl;
	private String runningSince;
	private String schedulerAnswer;
	private SOSXMLXPath schedulerAnswerXpath;
	private String schedulerId;
	private Path schedulerXml;
	private String state;
	private String tcpPort;
	private String time;
	private String timezone;
	private String udpPort;
	private String version;

	public Path getConfigDirectory() {
		return configDirectory;
	}

	public Path getHibernateConfigurationReporting() {
		return hibernateConfigurationReporting;
	}

	public Path getHibernateConfigurationScheduler() {
		return hibernateConfigurationScheduler;
	}

	public String getHost() {
		return host;
	}

	public String getHttpPort() {
		return httpPort;
	}

	public String getHttpsPort() {
		return httpsPort;
	}

	public Path getLiveDirectory() {
		return liveDirectory;
	}

	public String getMasterUrl() {
		return masterUrl;
	}

	public String getRunningSince() {
		return runningSince;
	}

	public String getSchedulerAnswer() {
		return schedulerAnswer;
	}

	public SOSXMLXPath getSchedulerAnswerXpath() {
		return schedulerAnswerXpath;
	}

	public String getSchedulerId() {
		return schedulerId;
	}

	public Path getSchedulerXml() {
		return schedulerXml;
	}

	public String getState() {
		return state;
	}

	public String getTcpPort() {
		return tcpPort;
	}

	public String getTime() {
		return time;
	}

	public String getTimezone() {
		return timezone;
	}

	public String getUdpPort() {
		return udpPort;
	}

	public String getVersion() {
		return version;
	}

	public void setConfigDirectory(Path val) {
		this.configDirectory = val;
	}

	public void setHibernateConfigurationReporting(Path val) {
		this.hibernateConfigurationReporting = val;
	}

	public void setHibernateConfigurationScheduler(Path val) {
		this.hibernateConfigurationScheduler = val;
	}

	public void setHost(String val) {
		this.host = val;
	}

	public void setHttpPort(String val) {
		this.httpPort = val;
	}

	public void setHttpsPort(String httpsPort) {
		this.httpsPort = httpsPort;
	}

	public void setLiveDirectory(Path val) {
		this.liveDirectory = val;
	}

	public void setMasterUrl(String val) {
		this.masterUrl = val;
	}

	public void setRunningSince(String runningSince) {
		this.runningSince = runningSince;
	}

	public void setSchedulerId(String val) {
		this.schedulerId = val;
	}

	public void setSchedulerXml(Path val) {
		this.schedulerXml = val;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setTcpPort(String tcpPort) {
		this.tcpPort = tcpPort;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void setTimezone(String val) {
		this.timezone = val;
	}

	public void setUdpPort(String udpPort) {
		this.udpPort = udpPort;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setSchedulerAnswer(String xml) throws Exception {
		if (xml != null) {
			this.schedulerAnswerXpath = new SOSXMLXPath(new StringBuffer(xml));
		}
		this.schedulerAnswer = xml;
	}

	public String getSchedulerAnswer(String xpath) throws Exception {
		return getSchedulerAnswer(xpath, null);
	}

	public String getSchedulerAnswer(String xpath, String defaultValue) throws Exception {
		if (defaultValue == null) {
			return schedulerAnswerXpath.selectSingleNodeValue(xpath);
		} else {
			return schedulerAnswerXpath.selectSingleNodeValue(xpath, defaultValue);
		}
	}

}
