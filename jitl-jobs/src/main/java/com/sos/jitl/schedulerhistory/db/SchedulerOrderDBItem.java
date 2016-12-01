package com.sos.jitl.schedulerhistory.db;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = "SCHEDULER_ORDERS")
public class SchedulerOrderDBItem extends DbItem {

    private String spoolerId;
    private String jobChain;
    private String orderId;
    private String state;
    private String stateText;
    private String title;
    private Date createdTime;
    private Date modTime;
    private Long ordering;
    private byte[] payload;
    private byte[] runTime;
    private String initialState;
    private byte[] orderXml;
    private Date distributedNextTime;
    private String occupyingClusterMemberId;

    public SchedulerOrderDBItem() {

    }

    @Column(name = "`SPOOLER_ID`", nullable = false)
    public String getSpoolerId() {
        return spoolerId;
    }

    @Column(name = "`SPOOLER_ID`", nullable = false)
    public void setSpoolerId(String spoolerId) {
        this.spoolerId = spoolerId;
    }

    @Column(name = "`ORDER_ID`", nullable = false)
    public String getOrderId() {
        return orderId;
    }

    @Column(name = "`ORDER_ID`", nullable = false)
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

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
    public void setEndTime(Date modTime) {
        this.modTime = modTime;
    }

    @Column(name = "`TITLE`", nullable = true)
    public String getCause() {
        return title;
    }

    @Column(name = "`TITLE`", nullable = true)
    public void setCause(String title) {
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

    @Lob
    @Column(name = "`PAYLOAD`", nullable = true)
    public byte[] getPayload() {
        return payload;
    }

    @Column(name = "`PAYLOAD`", nullable = true)
    public void setLog(byte[] log) {
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
    public Long getOrderingText() {
        return ordering;
    }

    @Column(name = "`ORDERING`", nullable = true)
    public void setOrderingText(Long ordering) {
        this.ordering = ordering;
    }

    @Lob
    @Column(name = "`RUN_TIME`", nullable = true)
    public byte[] getRunTime() {
        return runTime;
    }

    @Column(name = "`RUN_TIME`", nullable = true)
    public void setRunTime(byte[] runtime) {
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

    @Lob
    @Column(name = "`ORDER_XML`", nullable = true)
    public byte[] getOrderXml() {
        return orderXml;
    }

    @Column(name = "`ORDER_XML`", nullable = true)
    public void setOrderXml(byte[] orderXml) {
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

}