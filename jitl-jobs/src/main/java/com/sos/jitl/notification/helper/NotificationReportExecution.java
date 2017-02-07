package com.sos.jitl.notification.helper;

import java.io.Serializable;
import java.util.Date;

import sos.util.SOSString;

public class NotificationReportExecution implements Serializable {

	private static final long serialVersionUID = 1L;
	/** unique */
	private String schedulerId;
	private boolean standalone;
	private Long taskId;
	private Long step;
	private Long orderHistoryId;
	/** others */
	private String jobChainName;
	private String jobChainTitle;
	private String orderId;
	private String orderTitle;
	private Date orderStartTime;
	private Date orderEndTime;
	private String orderStepState;
	private Date orderStepStartTime;
	private Date orderStepEndTime;
	private String jobName;
	private String jobTitle;
	private Date taskStartTime;
	private Date taskEndTime;
	private Long returnCode;
	private boolean error;
	private String errorCode;
	private String errorText;

	public NotificationReportExecution() {
	}

	public void setSchedulerId(String val) {
		this.schedulerId = val;
	}

	public String getSchedulerId() {
		return this.schedulerId;
	}

	public void setStandalone(boolean val) {
		this.standalone = val;
	}

	public boolean getStandalone() {
		return this.standalone;
	}

	public void setTaskId(Long val) {
		this.taskId = val;
	}

	public Long getTaskId() {
		return this.taskId;
	}

	public void setStep(Long val) {
		this.step = val;
	}

	public Long getStep() {
		return this.step;
	}

	public void setOrderHistoryId(Long val) {
		this.orderHistoryId = val;
	}

	public Long getOrderHistoryId() {
		return this.orderHistoryId;
	}

	public void setJobChainName(String val) {
		this.jobChainName = val;
	}

	public String getJobChainName() {
		return this.jobChainName;
	}

	public void setJobChainTitle(String val) {
		this.jobChainTitle = val;
	}

	public String getJobChainTitle() {
		return this.jobChainTitle;
	}

	public void setOrderId(String val) {
		this.orderId = val;
	}

	public String getOrderId() {
		return this.orderId;
	}

	public void setOrderTitle(String val) {
		this.orderTitle = SOSString.isEmpty(val) ? null : val;
	}

	public String getOrderTitle() {
		return this.orderTitle;
	}

	public void setOrderStartTime(Date val) {
		this.orderStartTime = val;
	}

	public Date getOrderStartTime() {
		return this.orderStartTime;
	}

	public void setOrderEndTime(Date val) {
		this.orderEndTime = val;
	}

	public Date getOrderEndTime() {
		return this.orderEndTime;
	}

	public void setOrderStepState(String val) {
		this.orderStepState = val;
	}

	public String getOrderStepState() {
		return this.orderStepState;
	}

	public void setOrderStepStartTime(Date val) {
		this.orderStepStartTime = val;
	}

	public Date getOrderStepStartTime() {
		return this.orderStepStartTime;
	}

	public void setOrderStepEndTime(Date val) {
		this.orderStepEndTime = val;
	}

	public Date getOrderStepEndTime() {
		return this.orderStepEndTime;
	}

	public void setJobName(String val) {
		this.jobName = val;
	}

	public String getJobName() {
		return this.jobName;
	}

	public void setJobTitle(String val) {
		this.jobTitle = val;
	}

	public String getJobTitle() {
		return this.jobTitle;
	}

	public void setTaskStartTime(Date val) {
		this.taskStartTime = val;
	}

	public Date getTaskStartTime() {
		return this.taskStartTime;
	}

	public void setTaskEndTime(Date val) {
		this.taskEndTime = val;
	}

	public Date getTaskEndTime() {
		return this.taskEndTime;
	}

	public void setReturnCode(Long val) {
		this.returnCode = val;
	}

	public Long getReturnCode() {
		return this.returnCode;
	}

	public void setError(boolean val) {
		this.error = val;
	}

	public boolean getError() {
		return this.error;
	}

	public void setErrorCode(String val) {
		this.errorCode = val;
	}

	public String getErrorCode() {
		return this.errorCode;
	}

	public void setErrorText(String val) {
		this.errorText = val;
	}

	public String getErrorText() {
		return this.errorText;
	}

}
