package com.sos.jitl.notification.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;

@Entity
public class DBItemReportingTaskAndOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /** unique */
    private String schedulerId;
    private Long taskId;
    private Long orderStep;
    private Long orderHistoryId;
    /** others */
    private boolean isOrder;
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
    private Integer exitCode;
    private String agentUrl;
    private String clusterMemberId;

    public DBItemReportingTaskAndOrder() {
    }

    /** unique */
    @Id
    @Column(name = "[SCHEDULER_ID]", nullable = false)
    public String getSchedulerId() {
        return schedulerId;
    }

    @Id
    @Column(name = "[SCHEDULER_ID]", nullable = false)
    public void setSchedulerId(String val) {
        schedulerId = val;
    }

    @Id
    @Column(name = "[TASK_ID]", nullable = false)
    public Long getTaskId() {
        return taskId;
    }

    @Id
    @Column(name = "[TASK_ID]", nullable = false)
    public void setTaskId(Long val) {
        taskId = val;
    }

    @Id
    @Column(name = "[ORDER_STEP]", nullable = false)
    public Long getOrderStep() {
        return orderStep;
    }

    @Id
    @Column(name = "[ORDER_STEP]", nullable = false)
    public void setOrderStep(Long val) {
        orderStep = val;
    }

    @Id
    @Column(name = "[ORDER_HISTORY_ID]", nullable = false)
    public Long getOrderHistoryId() {
        return orderHistoryId;
    }

    @Id
    @Column(name = "[ORDER_HISTORY_ID]", nullable = false)
    public void setOrderHistoryId(Long val) {
        orderHistoryId = val;
    }

    /** others */
    @Column(name = "[STANDALONE]", nullable = false)
    @Type(type = "numeric_boolean")
    public void setIsOrder(boolean val) {
        isOrder = val;
    }

    @Column(name = "[STANDALONE]", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getIsOrder() {
        return isOrder;
    }

    @Column(name = "[JOB_CHAIN_NAME]", nullable = true)
    public String getJobChainName() {
        return jobChainName;
    }

    @Column(name = "[JOB_CHAIN_NAME]", nullable = true)
    public void setJobChainName(String val) {
        jobChainName = val;
    }

    @Column(name = "[JOB_CHAIN_TITLE]", nullable = true)
    public String getJobChainTitle() {
        return jobChainTitle;
    }

    @Column(name = "[JOB_CHAIN_TITLE]", nullable = true)
    public void setJobChainTitle(String val) {
        jobChainTitle = val;
    }

    @Column(name = "[ORDER_ID]", nullable = true)
    public String getOrderId() {
        return orderId;
    }

    @Column(name = "[ORDER_ID]", nullable = true)
    public void setOrderId(String val) {
        orderId = val;
    }

    @Column(name = "[ORDER_TITLE]", nullable = true)
    public String getOrderTitle() {
        return orderTitle;
    }

    @Column(name = "[ORDER_TITLE]", nullable = true)
    public void setOrderTitle(String val) {
        orderTitle = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[ORDER_START_TIME]", nullable = true)
    public Date getOrderStartTime() {
        return orderStartTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[ORDER_START_TIME]", nullable = true)
    public void setOrderStartTime(Date val) {
        orderStartTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[ORDER_END_TIME]", nullable = true)
    public Date getOrderEndTime() {
        return orderEndTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[ORDER_END_TIME]", nullable = true)
    public void setOrderEndTime(Date val) {
        orderEndTime = val;
    }

    @Column(name = "[ORDER_STEP_STATE]", nullable = true)
    public String getOrderStepState() {
        return orderStepState;
    }

    @Column(name = "[ORDER_STEP_STATE]", nullable = true)
    public void setOrderStepState(String val) {
        orderStepState = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[ORDER_STEP_START_TIME]", nullable = true)
    public Date getOrderStepStartTime() {
        return orderStepStartTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[ORDER_STEP_START_TIME]", nullable = true)
    public void setOrderStepStartTime(Date val) {
        orderStepStartTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[ORDER_STEP_END_TIME]", nullable = true)
    public Date getOrderStepEndTime() {
        return orderStepEndTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[ORDER_STEP_END_TIME]", nullable = true)
    public void setOrderStepEndTime(Date val) {
        orderStepEndTime = val;
    }

    @Column(name = "[JOB_NAME]", nullable = false)
    public String getJobName() {
        return jobName;
    }

    @Column(name = "[JOB_NAME]", nullable = false)
    public void setJobName(String val) {
        jobName = val;
    }

    @Column(name = "[JOB_TITLE]", nullable = true)
    public String getJobTitle() {
        return jobTitle;
    }

    @Column(name = "[JOB_TITLE]", nullable = true)
    public void setJobTitle(String val) {
        jobTitle = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[TASK_START_TIME]", nullable = false)
    public Date getTaskStartTime() {
        return taskStartTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[TASK_START_TIME]", nullable = false)
    public void setTaskStartTime(Date val) {
        taskStartTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[TASK_END_TIME]", nullable = true)
    public Date getTaskEndTime() {
        return taskEndTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[TASK_END_TIME]", nullable = true)
    public void setTaskEndTime(Date val) {
        taskEndTime = val;
    }

    @Column(name = "[EXIT_CODE]", nullable = true)
    public Integer getExitCode() {
        return exitCode;
    }

    @Column(name = "[EXIT_CODE]", nullable = true)
    public void setExitCode(Integer val) {
        exitCode = val;
    }

    @Column(name = "[AGENT_URL]", nullable = true)
    public String getAgentUrl() {
        return agentUrl;
    }

    @Column(name = "[AGENT_URL]", nullable = true)
    public void setAgentUrl(String val) {
        agentUrl = val;
    }

    @Column(name = "[CLUSTER_MEMBER_ID]", nullable = true)
    public String getClusterMemberId() {
        return clusterMemberId;
    }

    @Column(name = "[CLUSTER_MEMBER_ID]", nullable = true)
    public void setClusterMemberId(String val) {
        clusterMemberId = val;
    }

    @Override
    public int hashCode() {
        // always build on unique constraint
        return new HashCodeBuilder().append(schedulerId).append(taskId).append(orderStep).append(orderHistoryId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemReportingTaskAndOrder)) {
            return false;
        }
        DBItemReportingTaskAndOrder otherEntity = ((DBItemReportingTaskAndOrder) other);
        return new EqualsBuilder().append(schedulerId, otherEntity.schedulerId).append(taskId, otherEntity.taskId).append(orderStep,
                otherEntity.orderStep).append(orderHistoryId, otherEntity.orderHistoryId).isEquals();
    }

}
