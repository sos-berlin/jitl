package com.sos.jitl.reporting.db;

import java.io.Serializable;
import java.util.Date;

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

import sos.util.SOSString;

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = DBLayer.TABLE_INVENTORY_JOB_CHAIN_NODES)
@SequenceGenerator(name = DBLayer.TABLE_INVENTORY_JOB_CHAIN_NODES_SEQUENCE, sequenceName = DBLayer.TABLE_INVENTORY_JOB_CHAIN_NODES_SEQUENCE,
        allocationSize = 1)
public class DBItemInventoryJobChainNode extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    private Long id;

    /** Foreign key INVENTORY_INSTANCES.ID */
    private Long instanceId;
    /** Foreign key INVENTORY_JOB_CHAINS.ID */
    private Long jobChainId;
    /** Foreign key INVENTORY_JOBS.NAME */
    private String jobName;

    /** Others */
    private Long ordering;
    private String name;
    private String state;
    private String nextState;
    private String errorState;
    private String job;
    private Date created;
    private Date modified;

    /** new fields starting release 1.11 */
    /** foreign key INVENTORY_JOBS.ID (= 0 if nested job chain) */
    private Long jobId;
    private String nestedJobChain;
    /** foreign key INVENTORY_JOB_CHAINS.NAME (= . if job) */
    private String nestedJobChainName;
    /** foreign key INVENTORY_JOB_CHAINS.ID (= 0 if job) */
    private Long nestedJobChainId;
    private Integer nodeType;
    private String onError;
    private Integer delay;
    private String directory;
    private String regex;
    private Integer fileSinkOp;     
    private String movePath;

    public DBItemInventoryJobChainNode() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INVENTORY_JOB_CHAIN_NODES_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INVENTORY_JOB_CHAIN_NODES_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long val) {
        this.id = val;
    }

    /** Foreign key INVENTORY_INSTANCES.ID */
    @Column(name = "`INSTANCE_ID`", nullable = false)
    public Long getInstanceId() {
        return this.instanceId;
    }

    @Column(name = "`INSTANCE_ID`", nullable = false)
    public void setInstanceId(Long val) {
        if (val == null) {
            val = DBLayer.DEFAULT_ID;
        }
        this.instanceId = val;
    }

    /** Foreign key INVENTORY_JOB_CHAINS.ID */
    @Column(name = "`JOB_CHAIN_ID`", nullable = false)
    public Long getJobChainId() {
        return this.jobChainId;
    }

    @Column(name = "`JOB_CHAIN_ID`", nullable = false)
    public void setJobChainId(Long val) {
        if (val == null) {
            val = DBLayer.DEFAULT_ID;
        }
        this.jobChainId = val;
    }

    /** Foreign key INVENTORY_JOBS.NAME */
    @Column(name = "`JOB_NAME`", nullable = false)
    public void setJobName(String val) {
        if (SOSString.isEmpty(val)) {
            val = DBLayer.DEFAULT_NAME;
        }
        this.jobName = val;
    }

    @Column(name = "`JOB_NAME`", nullable = false)
    public String getJobName() {
        return this.jobName;
    }

    /** Others */
    @Column(name = "`ORDERING`", nullable = false)
    public void setOrdering(Long val) {
        this.ordering = val;
    }

    @Column(name = "`ORDERING`", nullable = false)
    public Long getOrdering() {
        return this.ordering;
    }

    @Column(name = "`STATE`", nullable = true)
    public void setState(String val) {
        if (SOSString.isEmpty(val)) {
            val = null;
        }
        this.state = val;
    }

    @Column(name = "`STATE`", nullable = true)
    public String getState() {
        return this.state;
    }

    @Column(name = "`NEXT_STATE`", nullable = true)
    public void setNextState(String val) {
        if (SOSString.isEmpty(val)) {
            val = null;
        }
        this.nextState = val;
    }

    @Column(name = "`NEXT_STATE`", nullable = true)
    public String getNextState() {
        return this.nextState;
    }

    @Column(name = "`ERROR_STATE`", nullable = true)
    public void setErrorState(String val) {
        if (SOSString.isEmpty(val)) {
            val = null;
        }
        this.errorState = val;
    }

    @Column(name = "`ERROR_STATE`", nullable = true)
    public String getErrorState() {
        return this.errorState;
    }

    @Column(name = "`JOB`", nullable = true)
    public void setJob(String val) {
        if (SOSString.isEmpty(val)) {
            val = null;
        }
        this.job = val;
    }

    @Column(name = "`JOB`", nullable = true)
    public String getJob() {
        return this.job;
    }

    @Column(name = "`NAME`", nullable = false)
    public void setName(String val) {
        this.name = val;
    }

    @Column(name = "`NAME`", nullable = false)
    public String getName() {
        return this.name;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public void setCreated(Date val) {
        this.created = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public Date getCreated() {
        return this.created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public void setModified(Date val) {
        this.modified = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public Date getModified() {
        return this.modified;
    }

    @Transient
//    @Column(name = "`JOB_ID`", nullable = false)
    public Long getJobId() {
        return jobId;
    }

    @Transient
//    @Column(name = "`JOB_ID`", nullable = false)
    public void setJobId(Long jobId) {
        if (jobId == null) {
            jobId = DBLayer.DEFAULT_ID;
        }
        this.jobId = jobId;
    }

    @Transient
//    @Column(name = "`NESTED_JOB_CHAIN`", nullable = true)
    public String getNestedJobChain() {
        return nestedJobChain;
    }

    @Transient
//    @Column(name = "`NESTED_JOB_CHAIN`", nullable = true)
    public void setNestedJobChain(String nestedJobChain) {
        this.nestedJobChain = nestedJobChain;
    }

    @Transient
//    @Column(name = "`NESTED_JOB_CHAIN_NAME`", nullable = false)
    public String getNestedJobChainName() {
        return nestedJobChainName;
    }

    @Transient
//    @Column(name = "`NESTED_JOB_CHAIN_NAME`", nullable = false)
    public void setNestedJobChainName(String nestedJobChainName) {
        if (nestedJobChainName == null || nestedJobChainName.isEmpty()) {
            nestedJobChainName = DBLayer.DEFAULT_NAME;
        }
        this.nestedJobChainName = nestedJobChainName;
    }

    @Transient
//    @Column(name = "`NESTED_JOB_CHAIN_ID`", nullable = false)
    public Long getNestedJobChainId() {
        return nestedJobChainId;
    }

    @Transient
//    @Column(name = "`NESTED_JOB_CHAIN_ID`", nullable = false)
    public void setNestedJobChainId(Long nestedJobChainId) {
        if (nestedJobChainId == null) {
            nestedJobChainId = DBLayer.DEFAULT_ID;
        }
        this.nestedJobChainId = nestedJobChainId;
    }

    @Transient
//    @Column(name = "`NODE_TYPE`", nullable = false)
    public Integer getNodeType() {
        return nodeType;
    }

    @Transient
//    @Column(name = "`NODE_TYPE`", nullable = false)
    public void setNodeType(Integer nodeType) {
        this.nodeType = nodeType;
    }

    @Transient
//    @Column(name = "`ON_ERROR`", nullable = true)
    public String getOnError() {
        return onError;
    }

    @Transient
//    @Column(name = "`ON_ERROR`", nullable = true)
    public void setOnError(String onError) {
        this.onError = onError;
    }

    @Transient
//    @Column(name = "`DELAY`", nullable = true)
    public Integer getDelay() {
        return delay;
    }

    @Transient
//    @Column(name = "`DELAY`", nullable = true)
    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    @Transient
//    @Column(name = "`DIRECTORY`", nullable = true)
    public String getDirectory() {
        return directory;
    }

    @Transient
//    @Column(name = "`DIRECTORY`", nullable = true)
    public void setDirectory(String directory) {
        this.directory = directory;
    }

    @Transient
//    @Column(name = "`REGEX`", nullable = true)
    public String getRegex() {
        return regex;
    }

    @Transient
//    @Column(name = "`REGEX`", nullable = true)
    public void setRegex(String regex) {
        this.regex = regex;
    }

    @Transient
//    @Column(name = "`FILE_SINK_OP`", nullable = true)
    public Integer getFileSinkOp() {
        return fileSinkOp;
    }

    @Transient
//    @Column(name = "`FILE_SINK_OP`", nullable = true)
    public void setFileSinkOp(Integer fileSinkOp) {
        this.fileSinkOp = fileSinkOp;
    }

    @Transient
//    @Column(name = "`MOVE_PATH`", nullable = true)
    public String getMovePath() {
        return movePath;
    }

    @Transient
//    @Column(name = "`MOVE_PATH`", nullable = true)
    public void setMovePath(String movePath) {
        this.movePath = movePath;
    }

}