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
@Table(name = DBLayer.TABLE_XML_EDITOR_OBJECTS)
@SequenceGenerator(name = DBLayer.TABLE_XML_EDITOR_OBJECTS_SEQUENCE, sequenceName = DBLayer.TABLE_XML_EDITOR_OBJECTS_SEQUENCE, allocationSize = 1)
public class DBItemXmlEditorObject extends DbItem implements Serializable {

    private static final long serialVersionUID = 3053835186921536145L;

    /** Primary Key */
    private Long id;

    /** Others */
    private String schedulerId;
    private String objectType;
    private String name;
    private String configuration;
    private String account;
    private Long auditLogId;
    private Date modified;
    private Date created;

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_XML_EDITOR_OBJECTS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public Long getId() {
        return id;
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_XML_EDITOR_OBJECTS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public void setId(Long val) {
        id = val;
    }

    @Column(name = "[SCHEDULER_ID]", nullable = false)
    public String getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(String val) {
        schedulerId = val;
    }

    @Column(name = "[OBJECT_TYPE]", nullable = false)
    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String val) {
        objectType = val;
    }

    @Column(name = "[NAME]", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    @Column(name = "[CONFIGURATION]", nullable = true)
    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String val) {
        configuration = val;
    }

    @Column(name = "[ACCOUNT]", nullable = false)
    public String getAccount() {
        return account;
    }

    public void setAccount(String val) {
        account = val;
    }

    @Column(name = "[AUDIT_LOG_ID]", nullable = false)
    public Long getAuditLogId() {
        return auditLogId;
    }

    public void setAuditLogId(Long val) {
        auditLogId = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = false)
    public Date getModified() {
        return modified;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = false)
    public void setModified(Date val) {
        modified = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    public Date getCreated() {
        return created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    public void setCreated(Date val) {
        created = val;
    }

    @Override
    public int hashCode() {
        // always build on unique constraint
        return new HashCodeBuilder().append(schedulerId).append(objectType).append(name).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemXmlEditorObject)) {
            return false;
        }
        DBItemXmlEditorObject rhs = ((DBItemXmlEditorObject) other);
        return new EqualsBuilder().append(schedulerId, rhs.schedulerId).append(objectType, rhs.objectType).append(name, rhs.name).isEquals();
    }

}