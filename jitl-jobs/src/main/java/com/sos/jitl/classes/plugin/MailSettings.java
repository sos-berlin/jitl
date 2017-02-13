package com.sos.jitl.classes.plugin;

public class MailSettings {
	private boolean sendOnError = false;
	private boolean sendOnWarning = false;
	private String host;
	private int port = 25;
	private String user;
	private String password;
	private String from;
	private String to;
	private String cc;
	private String bcc;
	private String queueDir;

	public boolean isSendOnError() {
		return sendOnError;
	}

	public void setSendOnError(boolean sendOnError) {
		this.sendOnError = sendOnError;
	}

	public boolean isSendOnWarning() {
		return sendOnWarning;
	}

	public void setSendOnWarning(boolean sendOnWarning) {
		this.sendOnWarning = sendOnWarning;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getBcc() {
		return bcc;
	}

	public void setBcc(String bcc) {
		this.bcc = bcc;
	}

	public String getQueueDir() {
		return queueDir;
	}

	public void setQueueDir(String queueDir) {
		this.queueDir = queueDir;
	}

}
