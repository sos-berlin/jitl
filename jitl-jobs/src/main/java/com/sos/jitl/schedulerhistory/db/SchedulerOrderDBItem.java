package com.sos.jitl.schedulerhistory.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = "SCHEDULER_ORDERS")
public class SchedulerOrderDBItem extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;
    private String spoolerId;
    private String jobChain;
    private String id;
    private String state;
    private String stateText;
    private String title;
    private Date createdTime;
    private Date modTime;
    private Long ordering;
    private String payload;
    private String runTime;
    private String initialState;
    private String orderXml;
    private Date distributedNextTime;
    private String occupyingClusterMemberId;

    public SchedulerOrderDBItem() {

    }

    @Id
    @Column(name = "`SPOOLER_ID`", nullable = false)
    public String getSpoolerId() {
        return spoolerId;
    }

    @Column(name = "`SPOOLER_ID`", nullable = false)
    public void setSpoolerId(String spoolerId) {
        this.spoolerId = spoolerId;
    }

    @Id
    @Column(name = "`ID`", nullable = false)
    public String getId() {
        return id;
    }

    @Column(name = "`ID`", nullable = false)
    public void setId(String orderId) {
        this.id = orderId;
    }

    @Id
    @Column(name = "`JOB_CHAIN`", nullable = false)
    public String getJobChain() {
        return jobChain;
    }

    @Column(name = "`JOB_CHAIN`", nullable = false)
    public void setJobChain(String jobChain) {
        this.jobChain = jobChain;
    }

    @Column(name = "`CREATED_TIME`", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getCreatedTime() {
        return createdTime;
    }

    @Column(name = "`CREATED_TIME`", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    @Column(name = "`MOD_TIME`", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getModTime() {
        return modTime;
    }

    @Column(name = "`MOD_TIME`", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    public void setModTime(Date modTime) {
        this.modTime = modTime;
    }

    @Column(name = "`TITLE`", nullable = true)
    public String getTitle() {
        return title;
    }

    @Column(name = "`TITLE`", nullable = true)
    public void setTitle(String title) {
        this.title = title;
    }

    @Column(name = "`STATE`", nullable = true)
    public String getState() {
        return state;
    }

    @Column(name = "`STATE`", nullable = true)
    public void setState(String state) {
        this.state = state;
    }

    @Column(name = "`PAYLOAD`", nullable = true)
    public String getPayload() {
        return payload;
    }

    @Column(name = "`PAYLOAD`", nullable = true)
    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Column(name = "`STATE_TEXT`", nullable = true)
    public String getStateText() {
        return stateText;
    }

    @Column(name = "`STATE_TEXT`", nullable = true)
    public void setStateText(String stateText) {
        this.stateText = stateText;
    }

    @Column(name = "`ORDERING`", nullable = true)
    public Long getOrdering() {
        return ordering;
    }

    @Column(name = "`ORDERING`", nullable = true)
    public void setOrdering(Long ordering) {
        this.ordering = ordering;
    }

    @Column(name = "`RUN_TIME`", nullable = true)
    public String getRunTime() {
        return runTime;
    }

    @Column(name = "`RUN_TIME`", nullable = true)
    public void setRunTime(String runtime) {
        this.runTime = runtime;
    }

    @Column(name = "`INITIAL_STATE`", nullable = true)
    public String getInitialState() {
        return initialState;
    }

    @Column(name = "`INITIAL_STATE`", nullable = true)
    public void setInitialState(String initialState) {
        this.initialState = initialState;
    }

    @Column(name = "`OCCUPYING_CLUSTER_MEMBER_ID`", nullable = true)
    public String getOccupyingClusterMemberId() {
        return occupyingClusterMemberId;
    }

    @Column(name = "`OCCUPYING_CLUSTER_MEMBER_ID`", nullable = true)
    public void setOccupyingClusterMemberId(String occupyingClusterMemberId) {
        this.occupyingClusterMemberId = occupyingClusterMemberId;
    }

    @Column(name = "`ORDER_XML`", nullable = true)
    public String getOrderXml() {
        return orderXml;
    }

    @Column(name = "`ORDER_XML`", nullable = true)
    public void setOrderXml(String orderXml) {
        this.orderXml = orderXml;
    }

    @Column(name = "`DISTRIBUTED_NEXT_TIME`", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getDistributedNextTime() {
        return distributedNextTime;
    }

    @Column(name = "`DISTRIBUTED_NEXT_TIME`", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    public void setDistributedNextTime(Date distributedNextTime) {
        this.distributedNextTime = distributedNextTime;
    }

    @Transient
    public String getSchedulerId() {
        return this.getSpoolerId();
    }
    
    @Override
    public int hashCode() {
        // always build on unique constraint
        return new HashCodeBuilder().append(spoolerId).append(jobChain).append(id).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof SchedulerOrderDBItem)) {
            return false;
        }
        SchedulerOrderDBItem rhs = ((SchedulerOrderDBItem) other);
        return new EqualsBuilder().append(spoolerId, rhs.spoolerId).append(jobChain, rhs.jobChain).append(id, rhs.id).isEquals();
    }

}