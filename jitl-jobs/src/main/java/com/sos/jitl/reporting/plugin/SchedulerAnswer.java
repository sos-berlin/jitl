package com.sos.jitl.reporting.plugin;

import java.nio.file.Path;

import sos.xml.SOSXMLXPath;

public class SchedulerAnswer {
	private SOSXMLXPath xpath;
	private String xml;
	private Path liveDirectory;
	private String masterUrl;
	private Path hibernateConfigPath;
	private Path schedulerXmlPath;
	private String httpPort;
	private String schedulerId;
	private String hostname;
	private String timezone;
	
	public SOSXMLXPath getXpath() {
		return xpath;
	}
	public void setXpath(SOSXMLXPath val) {
		this.xpath = val;
	}
	public String getXml() {
		return xml;
	}
	public void setXml(String val) {
		this.xml = val;
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
	public Path getHibernateConfigPath() {
		return hibernateConfigPath;
	}
	public void setHibernateConfigPath(Path val) {
		this.hibernateConfigPath = val;
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
