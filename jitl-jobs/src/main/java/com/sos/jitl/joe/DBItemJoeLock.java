package com.sos.jitl.joe;

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
import org.hibernate.annotations.Type;

import com.sos.hibernate.classes.DbItem;
import com.sos.jitl.reporting.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_JOE_LOCKS)
@SequenceGenerator(name = DBLayer.TABLE_JOE_LOCK_SEQUENCE, sequenceName = DBLayer.TABLE_JOE_LOCK_SEQUENCE, allocationSize = 1)
public class DBItemJoeLock extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary Key */
    private Long id;

    /** Contraint */
    private String schedulerId;
    private String folder;
    
    /** Others */
    private String account;
    private Boolean isLocked;
    private Date modified;
    private Date created;

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_JOE_LOCK_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public Long getId() {
        return id;
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_JOE_LOCK_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "[SCHEDULER_ID]", nullable = false)
    public String getSchedulerId() {
        return schedulerId;
    }

    @Column(name = "[SCHEDULER_ID]", nullable = false)
    public void setSchedulerId(String value) {
        this.schedulerId = value;
    }

    @Column(name = "[FOLDER]", nullable = false)
    public String getFolder() {
        return folder;
    }

    @Column(name = "[FOLDER]", nullable = false)
    public void setFolder(String value) {
        this.folder = value;
    }

    @Column(name = "[ACCOUNT]", nullable = false)
    public String getAccount() {
        return account;
    }

    @Column(name = "[ACCOUNT]", nullable = false)
    public void setAccount(String value) {
        this.account = value;
    }

    @Column(name = "[IS_LOCKED]", nullable = false)
    @Type(type = "numeric_boolean")
    public Boolean getIsLocked() {
        return isLocked;
    }

    @Column(name = "[IS_LOCKED]", nullable = false)
    @Type(type = "numeric_boolean")
    public void setIsLocked(Boolean value) {
        this.isLocked = value;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = false)
    public Date getModified() {
        return modified;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = false)
    public void setModified(Date value) {
        this.modified = value;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    public Date getCreated() {
        return created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    public void setCreated(Date value) {
        this.created = value;
    }
    
    @Override
    public int hashCode() {
        // always build on unique constraint
        return new HashCodeBuilder().append(schedulerId).append(folder).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemJoeLock)) {
            return false;
        }
        DBItemJoeLock rhs = ((DBItemJoeLock) other);
        return new EqualsBuilder().append(schedulerId, rhs.schedulerId).append(folder, rhs.folder).isEquals();
    }

}