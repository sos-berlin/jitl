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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.sos.hibernate.classes.DbItem;


@Entity
@Table(name = DBLayer.TABLE_INVENTORY_LOCKS)
@SequenceGenerator(name = DBLayer.TABLE_INVENTORY_LOCKS_SEQUENCE, sequenceName = DBLayer.TABLE_INVENTORY_LOCKS_SEQUENCE, allocationSize = 1)
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
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INVENTORY_LOCKS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return id;
    }
    
    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INVENTORY_LOCKS_SEQUENCE)
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
    @Column(name = "`FILE_ID`", nullable = false)
    public Long getFileId() {
        return fileId;
    }
    
    /** Foreign Key */
    @Column(name = "`FILE_ID`", nullable = false)
    public void setFileId(Long fileId) {
        if (fileId == null) {
            fileId = DBLayer.DEFAULT_ID;
        }
        this.fileId = fileId;
    }
    
    @Column(name = "`NAME`", nullable = false)
    public String getName() {
        return name;
    }
    
    @Column(name = "`NAME`", nullable = false)
    public void setName(String name) {
        this.name = name;
    }
    
    @Column(name = "`BASENAME`", nullable = false)
    public String getBasename() {
        return basename;
    }
    
    @Column(name = "`BASENAME`", nullable = false)
    public void setBasename(String basename) {
        this.basename = basename;
    }
    
    @Column(name = "`MAX_NON_EXCLUSIVE`", nullable = true)
    public Integer getMaxNonExclusive() {
        return maxNonExclusive;
    }
    
    @Column(name = "`MAX_NON_EXCLUSIVE`", nullable = true)
    public void setMaxNonExclusive(Integer maxNonExclusive) {
        this.maxNonExclusive = maxNonExclusive;
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
    
    @Override
    public int hashCode() {
        // always build on unique constraint
        return new HashCodeBuilder().append(fileId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemInventoryLock)) {
            return false;
        }
        DBItemInventoryLock rhs = ((DBItemInventoryLock) other);
        return new EqualsBuilder().append(fileId, rhs.fileId).isEquals();
    }

}