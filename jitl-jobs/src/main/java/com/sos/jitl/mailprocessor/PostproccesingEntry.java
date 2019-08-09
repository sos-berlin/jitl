package com.sos.jitl.mailprocessor;

import sos.net.SOSMimeMessage;

public class PostproccesingEntry {
	
	private SOSMimeMessage sosMimeMessage;
	private String body;
	private boolean addOrder;
	private boolean executeCommand;
	
	public SOSMimeMessage getSosMimeMessage() {
		return sosMimeMessage;
	}
	public void setSosMimeMessage(SOSMimeMessage sosMimeMessage) {
		this.sosMimeMessage = sosMimeMessage;
	}
	public boolean isAddOrder() {
		return addOrder;
	}
	public void setAddOrder(boolean addOrder) {
		this.addOrder = addOrder;
	}
	public boolean isExecuteCommand() {
		return executeCommand;
	}
	public void setExecuteCommand(boolean executeCommand) {
		this.executeCommand = executeCommand;
	}
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }

}
