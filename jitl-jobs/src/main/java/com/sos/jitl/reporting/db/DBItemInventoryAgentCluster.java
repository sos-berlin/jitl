package com.sos.jitl.reporting.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.sos.hibernate.classes.DbItem;


public class DBItemInventoryAgentCluster extends DbItem implements Serializable {

    private static final long serialVersionUID = 2550971072531081059L;

    /** Primary Key */
    private Long id;

    /** Foreign Key INVENTORY_INSTANCES.ID*/
    private Long instanceId;
    /** Foreign Key INVENTORY_PROCESS_CLASSES.ID*/
    private Long processClassId;
    
    /** Others */
    private String schedulingType;
    private Integer numberOfAgents;
    private Date created;
    private Date modified;
    
    /** Primary key */
    @Transient
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return id;
    }
    
    /** Primary key */
    @Transient
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long id) {
        this.id = id;
    }
    
    /** Foreign Key */
    @Transient
    @Column(name = "`INSTANCE_ID`", nullable = false)
    public Long getInstanceId() {
        return instanceId;
    }
    
    /** Foreign Key */
    @Transient
    @Column(name = "`INSTANCE_ID`", nullable = false)
    public void setInstanceId(Long instanceId) {
        if (instanceId == null) {
            instanceId = DBLayer.DEFAULT_ID;
        }
        this.instanceId = instanceId;
    }
    
    /** Foreign Key */
    @Transient
    @Column(name = "`PROCESS_CLASS_ID`", nullable = false)
    public Long getProcessClassId() {
        return processClassId;
    }
    
    /** Foreign Key */
    @Transient
    @Column(name = "`PROCESS_CLASS_ID`", nullable = false)
    public void setProcessClassId(Long processClassId) {
        if (processClassId == null) {
            processClassId = DBLayer.DEFAULT_ID;
        }
        this.processClassId = processClassId;
    }
    
    @Transient
    @Column(name = "`SCHEDULING_TYPE`", nullable = false)
    public String getSchedulingType() {
        return schedulingType;
    }
    
    @Transient
    @Column(name = "`SCHEDULING_TYPE`", nullable = false)
    public void setSchedulingType(String schedulingType) {
        this.schedulingType = schedulingType;
    }
    
    @Transient
    @Column(name = "`NUMBER_OF_AGENTS`", nullable = false)
    public Integer getNumberOfAgents() {
        return numberOfAgents;
    }
    
    @Transient
    @Column(name = "`NUMBER_OF_AGENTS`", nullable = false)
    public void setNumberOfAgents(Integer numberOfAgents) {
        this.numberOfAgents = numberOfAgents;
    }
    
    @Transient
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public Date getCreated() {
        return created;
    }
    
    @Transient
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public void setCreated(Date created) {
        this.created = created;
    }
    
    @Transient
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public Date getModified() {
        return modified;
    }
    
    @Transient
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public void setModified(Date modified) {
        this.modified = modified;
    }
    
}