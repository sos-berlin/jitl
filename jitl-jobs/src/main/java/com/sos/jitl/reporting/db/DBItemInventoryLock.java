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


public class DBItemInventoryLock extends DbItem implements Serializable {

    private static final long serialVersionUID = 5303544268625780402L;

    /** Primary Key */
    private Long id;

    /** Foreign Key INVENTORY_INSTANCES.ID */
    private Long instanceId;
    /** Foreign Key INVENTORY_FILES.ID */
    private Long fileId;

    /** Others */
    private String name;
    private String basename;
    private Integer maxNonExclusive;
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
    @Column(name = "`FILE_ID`", nullable = false)
    public Long getFileId() {
        return fileId;
    }
    
    /** Foreign Key */
    @Transient
    @Column(name = "`FILE_ID`", nullable = false)
    public void setFileId(Long fileId) {
        if (fileId == null) {
            fileId = DBLayer.DEFAULT_ID;
        }
        this.fileId = fileId;
    }
    
    @Transient
    @Column(name = "`NAME`", nullable = false)
    public String getName() {
        return name;
    }
    
    @Transient
    @Column(name = "`NAME`", nullable = false)
    public void setName(String name) {
        this.name = name;
    }
    
    @Transient
    @Column(name = "`BASENAME`", nullable = false)
    public String getBasename() {
        return basename;
    }
    
    @Transient
    @Column(name = "`BASENAME`", nullable = false)
    public void setBasename(String basename) {
        this.basename = basename;
    }
    
    @Transient
    @Column(name = "`MAX_NON_EXCLUSIVE`", nullable = true)
    public Integer getMaxNonExclusive() {
        return maxNonExclusive;
    }
    
    @Transient
    @Column(name = "`MAX_NON_EXCLUSIVE`", nullable = true)
    public void setMaxNonExclusive(Integer maxNonExclusive) {
        this.maxNonExclusive = maxNonExclusive;
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