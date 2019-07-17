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
    private String agentUrl;
    private String clusterMemberId;
    private boolean error;
    private String errorCode;
    private String errorText;

    public NotificationReportExecution() {
    }

    public void setSchedulerId(String val) {
        schedulerId = val;
    }

    public String getSchedulerId() {
        return schedulerId;
    }

    public void setStandalone(boolean val) {
        standalone = val;
    }

    public boolean getStandalone() {
        return standalone;
    }

    public void setTaskId(Long val) {
        taskId = val;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setStep(Long val) {
        step = val;
    }

    public Long getStep() {
        return step;
    }

    public void setOrderHistoryId(Long val) {
        orderHistoryId = val;
    }

    public Long getOrderHistoryId() {
        return orderHistoryId;
    }

    public void setJobChainName(String val) {
        jobChainName = val;
    }

    public String getJobChainName() {
        return jobChainName;
    }

    public void setJobChainTitle(String val) {
        jobChainTitle = val;
    }

    public String getJobChainTitle() {
        return jobChainTitle;
    }

    public void setOrderId(String val) {
        orderId = val;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderTitle(String val) {
        orderTitle = SOSString.isEmpty(val) ? null : val;
    }

    public String getOrderTitle() {
        return orderTitle;
    }

    public void setOrderStartTime(Date val) {
        orderStartTime = val;
    }

    public Date getOrderStartTime() {
        return orderStartTime;
    }

    public void setOrderEndTime(Date val) {
        orderEndTime = val;
    }

    public Date getOrderEndTime() {
        return orderEndTime;
    }

    public void setOrderStepState(String val) {
        orderStepState = val;
    }

    public String getOrderStepState() {
        return orderStepState;
    }

    public void setOrderStepStartTime(Date val) {
        orderStepStartTime = val;
    }

    public Date getOrderStepStartTime() {
        return orderStepStartTime;
    }

    public void setOrderStepEndTime(Date val) {
        orderStepEndTime = val;
    }

    public Date getOrderStepEndTime() {
        return orderStepEndTime;
    }

    public void setJobName(String val) {
        jobName = val;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobTitle(String val) {
        jobTitle = val;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setTaskStartTime(Date val) {
        taskStartTime = val;
    }

    public Date getTaskStartTime() {
        return taskStartTime;
    }

    public void setTaskEndTime(Date val) {
        taskEndTime = val;
    }

    public Date getTaskEndTime() {
        return taskEndTime;
    }

    public void setReturnCode(Long val) {
        returnCode = val;
    }

    public Long getReturnCode() {
        return returnCode;
    }

    public void setAgentUrl(String val) {
        agentUrl = val;
    }

    public String getAgentUrl() {
        return agentUrl;
    }

    public void setClusterMemberId(String val) {
        clusterMemberId = val;
    }

    public String getClusterMemberId() {
        return clusterMemberId;
    }

    public void setError(boolean val) {
        error = val;
    }

    public boolean getError() {
        return error;
    }

    public void setErrorCode(String val) {
        errorCode = val;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorText(String val) {
        errorText = val;
    }

    public String getErrorText() {
        return errorText;
    }

}
