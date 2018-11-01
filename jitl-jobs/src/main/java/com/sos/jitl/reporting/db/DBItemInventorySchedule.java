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
@Table(name = DBLayer.TABLE_INVENTORY_SCHEDULES)
@SequenceGenerator(name = DBLayer.TABLE_INVENTORY_SCHEDULES_SEQUENCE, sequenceName = DBLayer.TABLE_INVENTORY_SCHEDULES_SEQUENCE, allocationSize = 1)
public class DBItemInventorySchedule extends DbItem implements Serializable {

    private static final long serialVersionUID = 6092300351708576464L;

    /** Primary Key */
    private Long id;

    /** Foreign Key INVENTORY_INSTANCES.ID */
    private Long instanceId;
    /** Foreign Key INVENTORY_FILES.ID */
    private Long fileId;
    /** Foreign Key INVENTORY_SCHEDULES.ID (= 0 if undefined) */
    private Long substituteId;
    /** Foreign Key INVENTORY_SCHEDULES.NAME (= . if undefined) */
    private String substituteName;

    /** Others */
    private String name;
    private String basename;
    private String title;
    private String substitute;
    private Date substituteValidFrom;
    private Date substituteValidTo;
    private Date created;
    private Date modified;
    
    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INVENTORY_SCHEDULES_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public Long getId() {
        return id;
    }
    
    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INVENTORY_SCHEDULES_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public void setId(Long id) {
        this.id = id;
    }
    
    /** Foreign Key */
    @Column(name = "[INSTANCE_ID]", nullable = false)
    public Long getInstanceId() {
        return instanceId;
    }
    
    /** Foreign Key */
    @Column(name = "[INSTANCE_ID]", nullable = false)
    public void setInstanceId(Long instanceId) {
        if (instanceId == null) {
            instanceId = DBLayer.DEFAULT_ID;
        }
        this.instanceId = instanceId;
    }
    
    /** Foreign Key */
    @Column(name = "[FILE_ID]", nullable = false)
    public Long getFileId() {
        return fileId;
    }
    
    /** Foreign Key */
    @Column(name = "[FILE_ID]", nullable = false)
    public void setFileId(Long fileId) {
        if (fileId == null) {
            fileId = DBLayer.DEFAULT_ID;
        }
        this.fileId = fileId;
    }
    
    /** Foreign Key */
    @Column(name = "[SUBSTITUTE_ID]", nullable = false)
    public Long getSubstituteId() {
        return substituteId;
    }
    
    /** Foreign Key */
    @Column(name = "[SUBSTITUTE_ID]", nullable = false)
    public void setSubstituteId(Long substituteId) {
        if (substituteId == null) {
            substituteId = DBLayer.DEFAULT_ID;
        }
        this.substituteId = substituteId;
    }
    
    /** Foreign Key */
    @Column(name = "[SUBSTITUTE_NAME]", nullable = false)
    public String getSubstituteName() {
        return substituteName;
    }
    
    /** Foreign Key */
    @Column(name = "[SUBSTITUTE_NAME]", nullable = false)
    public void setSubstituteName(String substituteName) {
        if (substituteName == null || substituteName.isEmpty()) {
            substituteName = DBLayer.DEFAULT_NAME;
        }
        this.substituteName = substituteName;
    }
    
    @Column(name = "[NAME]", nullable = false)
    public String getName() {
        return name;
    }
    
    @Column(name = "[NAME]", nullable = false)
    public void setName(String name) {
        this.name = name;
    }
    
    @Column(name = "[BASENAME]", nullable = false)
    public String getBasename() {
        return basename;
    }
    
    @Column(name = "[BASENAME]", nullable = false)
    public void setBasename(String basename) {
        this.basename = basename;
    }
    
    @Column(name = "[TITLE]", nullable = true)
    public String getTitle() {
        return title;
    }
    
    @Column(name = "[TITLE]", nullable = true)
    public void setTitle(String title) {
        this.title = title;
    }
    
    @Column(name = "[SUBSTITUTE]", nullable = true)
    public String getSubstitute() {
        return substitute;
    }
    
    @Column(name = "[SUBSTITUTE]", nullable = true)
    public void setSubstitute(String substitute) {
        this.substitute = substitute;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[SUBSTITUTE_VALID_FROM]", nullable = true)
    public Date getSubstituteValidFrom() {
        return substituteValidFrom;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[SUBSTITUTE_VALID_FROM]", nullable = true)
    public void setSubstituteValidFrom(Date substituteValidFrom) {
        this.substituteValidFrom = substituteValidFrom;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[SUBSTITUTE_VALID_TO]", nullable = true)
    public Date getSubstituteValidTo() {
        return substituteValidTo;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[SUBSTITUTE_VALID_TO]", nullable = true)
    public void setSubstituteValidTo(Date substituteValidTo) {
        this.substituteValidTo = substituteValidTo;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    public Date getCreated() {
        return created;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    public void setCreated(Date created) {
        this.created = created;
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
    
    @Override
    public int hashCode() {
        // always build on unique constraint
        return new HashCodeBuilder().append(instanceId).append(name).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemInventorySchedule)) {
            return false;
        }
        DBItemInventorySchedule rhs = ((DBItemInventorySchedule) other);
        return new EqualsBuilder().append(instanceId, rhs.instanceId).append(name, rhs.name).isEquals();
    }

}