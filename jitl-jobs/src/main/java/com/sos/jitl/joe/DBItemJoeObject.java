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
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;

import com.sos.hibernate.classes.DbItem;
import com.sos.jitl.reporting.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_JOE_OBJECTS)
@SequenceGenerator(name = DBLayer.TABLE_JOE_OBJECT_SEQUENCE, sequenceName = DBLayer.TABLE_JOE_OBJECT_SEQUENCE, allocationSize = 1)
public class DBItemJoeObject extends DbItem implements Serializable {

    private static final long serialVersionUID = 3053835186921536145L;

    /** Primary Key */
    private Long id;

    /** Others */
    private String schedulerId;
    private String path;
    private String configuration;
    private String objectType;
    private String account;
    private String operation;
    private Long auditLogId;
    private boolean deployed;
    private Date modified;

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_JOE_OBJECT_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public Long getId() {
        return id;
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_JOE_OBJECT_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "[SCHEDULER_ID]", nullable = false)
    public String getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    @Column(name = "[PATH]", nullable = false)
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Column(name = "[CONFIURATION]", nullable = true)
    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @Column(name = "[OBJECT_TYPE]", nullable = false)
    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    @Column(name = "[ACCOUNT]", nullable = false)
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Column(name = "[AUDIT_LOG_ID]", nullable = false)
    public Long getAuditLogId() {
        return auditLogId;
    }

    public void setAuditLogId(Long auditLogId) {
        this.auditLogId = auditLogId;
    }

    @Column(name = "[DEPLOYED]", nullable = false)
    @Type(type = "numeric_boolean")
    public void setDeployed(Boolean deployed) {
        this.deployed = deployed;
    }

    @Column(name = "[DEPLOYED]", nullable = false)
    @Type(type = "numeric_boolean")
    public Boolean getDeployed() {
        return deployed;
    }

    @Column(name = "[OPERATION]", nullable = false)
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = false)
    public Date getModified() {
        return modified;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = false)
    public void setModified(Date modified) {
        this.modified = modified;
    }
    
    @Transient
    public boolean isDeleted() {
        return "delete".equalsIgnoreCase(operation);
    }

    @Override
    public int hashCode() {
        // always build on unique constraint
        return new HashCodeBuilder().append(schedulerId).append(path).append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemJoeObject)) {
            return false;
        }
        DBItemJoeObject rhs = ((DBItemJoeObject) other);
        return new EqualsBuilder().append(schedulerId, rhs.schedulerId).append(path, rhs.path).append(objectType, rhs.objectType).isEquals();
    }

}