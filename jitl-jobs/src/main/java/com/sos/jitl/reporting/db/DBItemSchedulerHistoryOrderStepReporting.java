package com.sos.jitl.reporting.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Type;

@Entity
public class DBItemSchedulerHistoryOrderStepReporting implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long stepHistoryId;
    private Long stepStep;
    private Long stepTaskId;
    private Date stepStartTime;
    private Date stepEndTime;
    private String stepState;
    private boolean stepError;
    private String stepErrorCode;
    private String stepErrorText;

    private Long orderHistoryId;
    private String orderSchedulerId;
    private String orderId;
    private String orderTitle;
    private String orderJobChain;
    private String orderState;
    private String orderStateText;
    private Date orderStartTime;
    private Date orderEndTime;

    private Long taskId;
    private String taskClusterMemberId;
    private Integer taskSteps;
    private String taskJobName;
    private Integer taskExitCode;
    private String taskCause;
    private String taskAgentUrl;
    private Date taskStartTime;
    private Date taskEndTime;
   

    public DBItemSchedulerHistoryOrderStepReporting() {
    }

    @Id
    @Column(name = "`STEP_HISTORY_ID`", nullable = false)
    public Long getStepHistoryId() {
        return stepHistoryId;
    }

    @Id
    @Column(name = "`STEP_HISTORY_ID`", nullable = false)
    public void setStepHistoryId(Long val) {
        this.stepHistoryId = val;
    }

    @Id
    @Column(name = "`STEP_STEP`", nullable = false)
    public Long getStepStep() {
        return stepStep;
    }

    @Id
    @Column(name = "`STEP_STEP`", nullable = false)
    public void setStepStep(Long val) {
        this.stepStep = val;
    }

    @Column(name = "`STEP_TASK_ID`", nullable = false)
    public Long getStepTaskId() {
        return stepTaskId;
    }

    @Column(name = "`STEP_TASK_ID`", nullable = false)
    public void setStepTaskId(Long val) {
        this.stepTaskId = val;
    }

    @Column(name = "`STEP_START_TIME`", nullable = false)
    public Date getStepStartTime() {
        return stepStartTime;
    }

    @Column(name = "`STEP_START_TIME`", nullable = false)
    public void setStepStartTime(Date val) {
        this.stepStartTime = val;
    }

    @Column(name = "`STEP_END_TIME`", nullable = true)
    public Date getStepEndTime() {
        return stepEndTime;
    }

    @Column(name = "`STEP_END_TIME`", nullable = true)
    public void setStepEndTime(Date val) {
        this.stepEndTime = val;
    }

    @Column(name = "`STEP_STATE`", nullable = false)
    public String getStepState() {
        return stepState;
    }

    @Column(name = "`STEP_STATE`", nullable = false)
    public void setStepState(String val) {
        this.stepState = val;
    }

    @Column(name = "`STEP_ERROR`", nullable = true)
    @Type(type = "numeric_boolean")
    public boolean isStepError() {
        return stepError;
    }

    @Column(name = "`STEP_ERROR`", nullable = true)
    @Type(type = "numeric_boolean")
    public void setStepError(Boolean val) {
        if (val == null) {
            val = false;
        }
        this.stepError = val;
    }

    @Column(name = "`STEP_ERROR_CODE`", nullable = true)
    public String getStepErrorCode() {
        return stepErrorCode;
    }

    @Column(name = "`STEP_ERROR_CODE`", nullable = true)
    public void setStepErrorCode(String val) {
        this.stepErrorCode = val;
    }

    @Column(name = "`STEP_ERROR_TEXT`", nullable = true)
    public String getStepErrorText() {
        return stepErrorText;
    }

    @Column(name = "`STEP_ERROR_TEXT`", nullable = true)
    public void setStepErrorText(String val) {
        this.stepErrorText = val;
    }

    @Column(name = "`ORDER_HISTORY_ID`", nullable = true)
    public Long getOrderHistoryId() {
        return orderHistoryId;
    }

    @Column(name = "`ORDER_HISTORY_ID`", nullable = true)
    public void setOrderHistoryId(Long val) {
        this.orderHistoryId = val;
    }

    @Column(name = "`ORDER_SCHEDULER_ID`", nullable = true)
    public String getOrderSchedulerId() {
        return orderSchedulerId;
    }

    @Column(name = "`ORDER_SCHEDULER_ID`", nullable = true)
    public void setOrderSchedulerId(String val) {
        this.orderSchedulerId = val;
    }

    @Column(name = "`ORDER_ID`", nullable = true)
    public String getOrderId() {
        return orderId;
    }

    @Column(name = "`ORDER_ID`", nullable = true)
    public void setOrderId(String val) {
        this.orderId = val;
    }

    @Column(name = "`ORDER_TITLE`", nullable = true)
    public String getOrderTitle() {
        return orderTitle;
    }

    @Column(name = "`ORDER_TITLE`", nullable = true)
    public void setOrderTitle(String val) {
        this.orderTitle = val;
    }
    
    @Column(name = "`ORDER_JOB_CHAIN`", nullable = true)
    public String getOrderJobChain() {
        return orderJobChain;
    }

    @Column(name = "`ORDER_JOB_CHAIN`", nullable = true)
    public void setOrderJobChain(String val) {
        this.orderJobChain = val;
    }

    @Column(name = "`ORDER_STATE`", nullable = true)
    public String getOrderState() {
        return orderState;
    }

    @Column(name = "`ORDER_STATE`", nullable = true)
    public void setOrderState(String val) {
        this.orderState = val;
    }

    @Column(name = "`ORDER_STATE_TEXT`", nullable = true)
    public String getOrderStateText() {
        return orderStateText;
    }

    @Column(name = "`ORDER_STATE_TEXT`", nullable = true)
    public void setOrderStateText(String val) {
        this.orderStateText = val;
    }

    @Column(name = "`ORDER_START_TIME`", nullable = true)
    public Date getOrderStartTime() {
        return orderStartTime;
    }

    @Column(name = "`ORDER_START_TIME`", nullable = true)
    public void setOrderStartTime(Date val) {
        this.orderStartTime = val;
    }

    @Column(name = "`ORDER_END_TIME`", nullable = true)
    public Date getOrderEndTime() {
        return orderEndTime;
    }

    @Column(name = "`ORDER_END_TIME`", nullable = true)
    public void setOrderEndTime(Date val) {
        this.orderEndTime = val;
    }

    @Column(name = "`TASK_ID`", nullable = true)
    public Long getTaskId() {
        return taskId;
    }

    @Column(name = "`TASK_ID`", nullable = true)
    public void setTaskId(Long val) {
        this.taskId = val;
    }

    @Column(name = "`TASK_CLUSTER_MEMBER_ID`", nullable = true)
    public String getTaskClusterMemberId() {
        return taskClusterMemberId;
    }

    @Column(name = "`TASK_CLUSTER_MEMBER_ID`", nullable = true)
    public void setTaskClusterMemberId(String val) {
        this.taskClusterMemberId = val;
    }
    
    @Column(name = "`TASK_STEPS`", nullable = true)
    public Integer getTaskSteps() {
        return taskSteps;
    }

    @Column(name = "`TASK_STEPS`", nullable = true)
    public void setTaskSteps(Integer val) {
        this.taskSteps = val;
    }
    
    @Column(name = "`TASK_JOB_NAME`", nullable = true)
    public String getTaskJobName() {
        return taskJobName;
    }

    @Column(name = "`TASK_JOB_NAME`", nullable = true)
    public void setTaskJobName(String val) {
        this.taskJobName = val;
    }

    @Column(name = "`TASK_EXIT_CODE`", nullable = true)
    public Integer getTaskExitCode() {
        return taskExitCode;
    }

    @Column(name = "`TASK_EXIT_CODE`", nullable = true)
    public void setTaskExitCode(Integer val) {
        this.taskExitCode = val;
    }
    
    @Column(name = "`TASK_CAUSE`", nullable = true)
    public String getTaskCause() {
        return taskCause;
    }

    @Column(name = "`TASK_CAUSE`", nullable = true)
    public void setTaskCause(String val) {
        this.taskCause = val;
    }

    @Column(name = "`TASK_AGENT_URL`", nullable = true)
    public String getAgentUrl() {
        return taskAgentUrl;
    }

    @Column(name = "`TASK_AGENT_URL`", nullable = true)
    public void setAgentUrl(String val) {
        this.taskAgentUrl = val;
    }
    
    @Column(name = "`TASK_START_TIME`", nullable = false)
    public Date getTaskStartTime() {
        return taskStartTime;
    }

    @Column(name = "`TASK_START_TIME`", nullable = false)
    public void setTaskStartTime(Date val) {
        this.taskStartTime = val;
    }

    @Column(name = "`TASK_END_TIME`", nullable = true)
    public Date getTaskEndTime() {
        return taskEndTime;
    }

    @Column(name = "`TASK_END_TIME`", nullable = true)
    public void setTaskEndTime(Date val) {
        this.taskEndTime = val;
    }
}
