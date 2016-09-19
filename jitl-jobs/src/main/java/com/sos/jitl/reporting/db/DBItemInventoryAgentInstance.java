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


public class DBItemInventoryAgentInstance extends DbItem implements Serializable {

    private static final long serialVersionUID = 6908223871310840514L;

    /** Primary Key */
    private Long id;

    /** Foreign Key INVENTORY_INSTANCES.ID */
    private Long instanceId;
    /** Foreign Key INVENTORY_OPERATING_SYSTEM.HOSTNAME */
    private String hostname;
    /** Foreign Key INVENTORY_OPERATING_SYSTEM.ID */
    private Long osId;

    /** Others */
    private String version;
    private String url;
    private Integer state;
    private Date startedAt;
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
    @Column(name = "`HOSTNAME`", nullable = false)
    public String getHostname() {
        return hostname;
    }
    
    /** Foreign Key */
    @Transient
    @Column(name = "`HOSTNAME`", nullable = false)
    public void setHostname(String hostname) {
        if (hostname == null || hostname.isEmpty()) {
            hostname = DBLayer.DEFAULT_NAME;
        }
        this.hostname = hostname;
    }
    
    /** Foreign Key */
    @Transient
    @Column(name = "`OS_ID`", nullable = false)
    public Long getOsId() {
        return osId;
    }
    
    /** Foreign Key */
    @Transient
    @Column(name = "`OS_ID`", nullable = false)
    public void setOsId(Long osId) {
        if (osId == null) {
            osId = DBLayer.DEFAULT_ID;
        }
        this.osId = osId;
    }
    
    @Transient
    @Column(name = "`VERSION`", nullable = false)
    public String getVersion() {
        return version;
    }
    
    @Transient
    @Column(name = "`VERSION`", nullable = false)
    public void setVersion(String version) {
        this.version = version;
    }
    
    @Transient
    @Column(name = "`URL`", nullable = false)
    public String getUrl() {
        return url;
    }
    
    @Transient
    @Column(name = "`URL`", nullable = false)
    public void setUrl(String url) {
        this.url = url;
    }
    
    @Transient
    @Column(name = "`STATE`", nullable = false)
    public Integer getState() {
        return state;
    }
    
    @Transient
    @Column(name = "`STATE`", nullable = false)
    public void setState(Integer state) {
        this.state = state;
    }
    
    @Transient
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`STARTED_AT`", nullable = false)
    public Date getStartedAt() {
        return startedAt;
    }
    
    @Transient
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`STARTED_AT`", nullable = false)
    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
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