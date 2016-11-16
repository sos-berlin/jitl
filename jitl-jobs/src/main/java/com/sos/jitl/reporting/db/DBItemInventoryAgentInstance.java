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

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = DBLayer.TABLE_INVENTORY_AGENT_INSTANCES)
@SequenceGenerator(name = DBLayer.TABLE_INVENTORY_AGENT_INSTANCES_SEQUENCE, sequenceName = DBLayer.TABLE_INVENTORY_AGENT_INSTANCES_SEQUENCE,
    allocationSize = 1)
public class DBItemInventoryAgentInstance extends DbItem implements Serializable {

    private static final long serialVersionUID = 6908223871310840514L;

    /** Primary Key */
    private Long id;

    /** Foreign Key INVENTORY_INSTANCES.ID */
    private Long instanceId;
    /** Foreign Key INVENTORY_OPERATING_SYSTEM.ID */
    private Long osId;

    /** Others */
    private String hostname;
    private String version;
    private String url;
    private Integer state;
    private Date startedAt;
    private Date created;
    private Date modified;
    
    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INVENTORY_AGENT_INSTANCES_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return id;
    }
    
    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INVENTORY_AGENT_INSTANCES_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long id) {
        this.id = id;
    }
    
    /** Foreign Key */
    @Column(name = "`INSTANCE_ID`", nullable = false)
    public Long getInstanceId() {
        return instanceId;
    }
    
    /** Foreign Key */
    @Column(name = "`INSTANCE_ID`", nullable = false)
    public void setInstanceId(Long instanceId) {
        if (instanceId == null) {
            instanceId = DBLayer.DEFAULT_ID;
        }
        this.instanceId = instanceId;
    }
    
    /** Foreign Key */
    @Column(name = "`HOSTNAME`", nullable = true)
    public String getHostname() {
        return hostname;
    }
    
    /** Foreign Key */
    @Column(name = "`HOSTNAME`", nullable = true)
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    
    /** Foreign Key */
    @Column(name = "`OS_ID`", nullable = false)
    public Long getOsId() {
        return osId;
    }
    
    /** Foreign Key */
    @Column(name = "`OS_ID`", nullable = false)
    public void setOsId(Long osId) {
        if (osId == null) {
            osId = DBLayer.DEFAULT_ID;
        }
        this.osId = osId;
    }
    
    @Column(name = "`VERSION`", nullable = true)
    public String getVersion() {
        return version;
    }
    
    @Column(name = "`VERSION`", nullable = true)
    public void setVersion(String version) {
        this.version = version;
    }
    
    @Column(name = "`URL`", nullable = false)
    public String getUrl() {
        return url;
    }
    
    @Column(name = "`URL`", nullable = false)
    public void setUrl(String url) {
        this.url = url;
    }
    
    @Column(name = "`STATE`", nullable = false)
    public Integer getState() {
        return state;
    }
    
    @Column(name = "`STATE`", nullable = false)
    public void setState(Integer state) {
        this.state = state;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`STARTED_AT`", nullable = true)
    public Date getStartedAt() {
        return startedAt;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`STARTED_AT`", nullable = true)
    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public Date getCreated() {
        return created;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public void setCreated(Date created) {
        this.created = created;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public Date getModified() {
        return modified;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public void setModified(Date modified) {
        this.modified = modified;
    }
    
}