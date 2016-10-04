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
@Table(name = DBLayer.TABLE_INVENTORY_APPLIED_LOCKS)
@SequenceGenerator(name = DBLayer.TABLE_INVENTORY_APPLIED_LOCKS_SEQUENCE, sequenceName = DBLayer.TABLE_INVENTORY_APPLIED_LOCKS_SEQUENCE, allocationSize = 1)
public class DBItemInventoryAppliedLock extends DbItem implements Serializable {

    private static final long serialVersionUID = 3256337020570653879L;

    /** Primary Key */
    private Long id;

    /** Foreign Key INVENTORY_JOBS.ID */
    private Long jobId;
    /** Foreign Key INVENTORY_LOCKS.ID */
    private Long lockId;
    
    /** Others */
    private Date created;
    private Date modified;
    
    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INVENTORY_APPLIED_LOCKS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return id;
    }
    
    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INVENTORY_APPLIED_LOCKS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long id) {
        this.id = id;
    }
    
    /** Foreign Key */
    @Column(name = "`JOB_ID`", nullable = false)
    public Long getJobId() {
        return jobId;
    }
    
    /** Foreign Key */
    @Column(name = "`JOB_ID`", nullable = false)
    public void setJobId(Long jobId) {
        if (jobId == null) {
            jobId = DBLayer.DEFAULT_ID;
        }
        this.jobId = jobId;
    }
    
    /** Foreign Key */
    @Column(name = "`LOCK_ID`", nullable = false)
    public Long getLockId() {
        return lockId;
    }
    
    /** Foreign Key */
    @Column(name = "`LOCK_ID`", nullable = false)
    public void setLockId(Long lockId) {
        if (lockId == null) {
            lockId = DBLayer.DEFAULT_ID;
        }
        this.lockId = lockId;
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