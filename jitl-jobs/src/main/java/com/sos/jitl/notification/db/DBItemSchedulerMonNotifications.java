package com.sos.jitl.notification.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;

import sos.util.SOSString;

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = DBLayer.TABLE_SCHEDULER_MON_NOTIFICATIONS)
@SequenceGenerator(name = DBLayer.SEQUENCE_SCHEDULER_MON_NOTIFICATIONS, sequenceName = DBLayer.SEQUENCE_SCHEDULER_MON_NOTIFICATIONS, allocationSize = 1)
/** uniqueConstraints = {@UniqueConstraint(columnNames ={"[SCHEDULER_ID]", "[STANDALONE]", "[TASK_ID]", "[STEP]", "[ORDER_HISTORY_ID`"})} */
public class DBItemSchedulerMonNotifications extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
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
    private boolean recovered;
    private Long returnCode;
    private String agentUrl;
    private String clusterMemberId;
    private boolean error;
    private String errorCode;
    private String errorText;
    private Date created;
    private Date modified;
    List<Object> childs;

    public DBItemSchedulerMonNotifications() {
        setOrderHistoryId(DBLayer.DEFAULT_EMPTY_NUMERIC);
        setStep(DBLayer.DEFAULT_EMPTY_NUMERIC);
        setReturnCode(DBLayer.DEFAULT_EMPTY_NUMERIC);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.SEQUENCE_SCHEDULER_MON_NOTIFICATIONS)
    @Column(name = "[ID]", nullable = false)
    public Long getId() {
        return id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.SEQUENCE_SCHEDULER_MON_NOTIFICATIONS)
    @Column(name = "[ID]", nullable = false)
    public void setId(Long val) {
        id = val;
    }

    /** unique */
    @Column(name = "[SCHEDULER_ID]", nullable = false)
    public void setSchedulerId(String val) {
        schedulerId = val;
    }

    @Column(name = "[SCHEDULER_ID]", nullable = false)
    public String getSchedulerId() {
        return schedulerId;
    }

    @Column(name = "[STANDALONE]", nullable = false)
    @Type(type = "numeric_boolean")
    public void setStandalone(boolean val) {
        standalone = val;
    }

    @Column(name = "[STANDALONE]", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getStandalone() {
        return standalone;
    }

    @Column(name = "[TASK_ID]", nullable = false)
    public void setTaskId(Long val) {
        taskId = val;
    }

    @Column(name = "[TASK_ID]", nullable = false)
    public Long getTaskId() {
        return taskId;
    }

    @Column(name = "[STEP]", nullable = false)
    public void setStep(Long val) {
        step = (val == null) ? DBLayer.DEFAULT_EMPTY_NUMERIC : val;
    }

    @Column(name = "[STEP]", nullable = false)
    public Long getStep() {
        return step;
    }

    @Column(name = "[ORDER_HISTORY_ID]", nullable = false)
    public void setOrderHistoryId(Long val) {
        orderHistoryId = (val == null) ? DBLayer.DEFAULT_EMPTY_NUMERIC : val;
    }

    @Column(name = "[ORDER_HISTORY_ID]", nullable = false)
    public Long getOrderHistoryId() {
        return orderHistoryId;
    }

    /** others */
    @Column(name = "[JOB_CHAIN_NAME]", nullable = false)
    public void setJobChainName(String val) {
        if (SOSString.isEmpty(val)) {
            val = DBLayer.EMPTY_TEXT_VALUE;
        } else if (val.startsWith("/")) {
            val = val.substring(1);
        }

        jobChainName = val;
    }

    @Column(name = "[JOB_CHAIN_NAME]", nullable = false)
    public String getJobChainName() {
        return jobChainName;
    }

    @Column(name = "[JOB_CHAIN_TITLE]", nullable = true)
    public void setJobChainTitle(String val) {
        jobChainTitle = SOSString.isEmpty(val) ? null : val;
    }

    @Column(name = "[JOB_CHAIN_TITLE]", nullable = true)
    public String getJobChainTitle() {
        return jobChainTitle;
    }

    @Column(name = "[ORDER_ID]", nullable = false)
    public void setOrderId(String val) {
        orderId = SOSString.isEmpty(val) ? DBLayer.EMPTY_TEXT_VALUE : val;
    }

    @Column(name = "[ORDER_ID]", nullable = false)
    public String getOrderId() {
        return orderId;
    }

    @Column(name = "[ORDER_TITLE]", nullable = true)
    public void setOrderTitle(String val) {
        orderTitle = SOSString.isEmpty(val) ? null : val;
    }

    @Column(name = "[ORDER_TITLE]", nullable = true)
    public String getOrderTitle() {
        return orderTitle;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[ORDER_START_TIME]", nullable = true)
    public void setOrderStartTime(Date val) {
        orderStartTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[ORDER_START_TIME]", nullable = true)
    public Date getOrderStartTime() {
        return orderStartTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[ORDER_END_TIME]", nullable = true)
    public void setOrderEndTime(Date val) {
        orderEndTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[ORDER_END_TIME]", nullable = true)
    public Date getOrderEndTime() {
        return orderEndTime;
    }

    @Column(name = "[ORDER_STEP_STATE]", nullable = false)
    public void setOrderStepState(String val) {
        orderStepState = SOSString.isEmpty(val) ? DBLayer.EMPTY_TEXT_VALUE : val;
    }

    @Column(name = "[ORDER_STEP_STATE]", nullable = false)
    public String getOrderStepState() {
        return orderStepState;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[ORDER_STEP_START_TIME]", nullable = true)
    public void setOrderStepStartTime(Date val) {
        orderStepStartTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[ORDER_STEP_START_TIME]", nullable = true)
    public Date getOrderStepStartTime() {
        return orderStepStartTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[ORDER_STEP_END_TIME]", nullable = true)
    public void setOrderStepEndTime(Date val) {
        orderStepEndTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[ORDER_STEP_END_TIME]", nullable = true)
    public Date getOrderStepEndTime() {
        return orderStepEndTime;
    }

    @Column(name = "[JOB_NAME]", nullable = false)
    public void setJobName(String val) {
        jobName = val;
    }

    @Column(name = "[JOB_NAME]", nullable = false)
    public String getJobName() {
        return jobName;
    }

    @Column(name = "[JOB_TITLE]", nullable = true)
    public void setJobTitle(String val) {
        if (SOSString.isEmpty(val)) {
            val = null;
        }
        jobTitle = val;
    }

    @Column(name = "[JOB_TITLE]", nullable = true)
    public String getJobTitle() {
        return jobTitle;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[TASK_START_TIME]", nullable = false)
    public void setTaskStartTime(Date val) {
        taskStartTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[TASK_START_TIME]", nullable = false)
    public Date getTaskStartTime() {
        return taskStartTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[TASK_END_TIME]", nullable = true)
    public void setTaskEndTime(Date val) {
        taskEndTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[TASK_END_TIME]", nullable = true)
    public Date getTaskEndTime() {
        return taskEndTime;
    }

    @Column(name = "[RECOVERED]", nullable = false)
    @Type(type = "numeric_boolean")
    public void setRecovered(boolean val) {
        recovered = val;
    }

    @Column(name = "[RECOVERED]", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getRecovered() {
        return recovered;
    }

    @Column(name = "[RETURN_CODE]", nullable = false)
    public void setReturnCode(Long val) {
        returnCode = (val == null) ? DBLayer.DEFAULT_EMPTY_NUMERIC : val;
    }

    @Column(name = "[RETURN_CODE]", nullable = false)
    public Long getReturnCode() {
        return returnCode;
    }

    @Column(name = "[AGENT_URL]", nullable = true)
    public void setAgentUrl(String val) {
        agentUrl = val;
    }

    @Column(name = "[AGENT_URL]", nullable = true)
    public String getAgentUrl() {
        return agentUrl;
    }

    @Column(name = "[CLUSTER_MEMBER_ID]", nullable = true)
    public void setClusterMemberId(String val) {
        clusterMemberId = val;
    }

    @Column(name = "[CLUSTER_MEMBER_ID]", nullable = true)
    public String getClusterMemberId() {
        return clusterMemberId;
    }

    @Column(name = "[ERROR]", nullable = false)
    @Type(type = "numeric_boolean")
    public void setError(boolean val) {
        error = val;
    }

    @Column(name = "[ERROR]", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getError() {
        return error;
    }

    @Column(name = "[ERROR_CODE]", nullable = true)
    public void setErrorCode(String val) {
        if (SOSString.isEmpty(val)) {
            val = null;
        }
        errorCode = val;
    }

    @Column(name = "[ERROR_CODE]", nullable = true)
    public String getErrorCode() {
        return errorCode;
    }

    @Column(name = "[ERROR_TEXT]", nullable = true)
    public void setErrorText(String val) {
        if (SOSString.isEmpty(val)) {
            val = null;
        }
        errorText = val;
    }

    @Column(name = "[ERROR_TEXT]", nullable = true)
    public String getErrorText() {
        return errorText;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    public void setCreated(Date val) {
        created = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    public Date getCreated() {
        return created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = false)
    public void setModified(Date val) {
        modified = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = false)
    public Date getModified() {
        return modified;
    }

    @Transient
    public List<Object> getChilds() {
        return childs;
    }

    @Transient
    public void setChilds(List<Object> val) {
        childs = val;
    }

    @Transient
    public void addChild(Object val) {
        if (childs == null) {
            childs = new ArrayList<Object>();
        }
        childs.add(val);
    }

    @Override
    public int hashCode() {
        // always build on unique constraint
        return new HashCodeBuilder().append(schedulerId).append(orderHistoryId).append(step).append(taskId).append(standalone).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemSchedulerMonNotifications)) {
            return false;
        }
        DBItemSchedulerMonNotifications otherEntity = ((DBItemSchedulerMonNotifications) other);
        return new EqualsBuilder().append(schedulerId, otherEntity.schedulerId).append(orderHistoryId, otherEntity.orderHistoryId).append(step,
                otherEntity.step).append(taskId, otherEntity.taskId).append(standalone, otherEntity.standalone).isEquals();
    }

}
