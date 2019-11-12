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
import org.hibernate.annotations.Type;

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = DBLayer.TABLE_SUBMITTED_OBJECTS)
@SequenceGenerator(name = DBLayer.TABLE_SUBMITTED_OBJECTS_SEQUENCE, sequenceName = DBLayer.TABLE_SUBMITTED_OBJECTS_SEQUENCE, allocationSize = 1)
public class DBItemSubmittedObject extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String schedulerId;
    private String path;
    private boolean toDelete;
    private String content;
    private Date modified;

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_SUBMITTED_OBJECTS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public Long getId() {
        return id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_SUBMITTED_OBJECTS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public void setId(Long id) {
        this.id = id;
    }

    /** Others */
    @Column(name = "[SCHEDULER_ID]", nullable = false)
    public String getSchedulerId() {
        return schedulerId;
    }

    @Column(name = "[SCHEDULER_ID]", nullable = false)
    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    @Column(name = "[PATH]", nullable = false)
    public String getPath() {
        return path;
    }

    @Column(name = "[PATH]", nullable = false)
    public void setPath(String path) {
        this.path = path;
    }

    @Column(name = "[TO_DELETE]", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getToDelete() {
        return toDelete;
    }

    @Column(name = "[TO_DELETE]", nullable = false)
    @Type(type = "numeric_boolean")
    public void setToDelete(boolean toDelete) {
        this.toDelete = toDelete;
    }
    
    @Column(name = "[CONTENT]", nullable = true)
    public String getContent() {
        return content;
    }
    
    @Column(name = "[CONTENT]", nullable = true)
    public void setContent(String content) {
        this.content = content;
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
        return new HashCodeBuilder().append(schedulerId).append(path).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemSubmittedObject)) {
            return false;
        }
        DBItemSubmittedObject rhs = ((DBItemSubmittedObject) other);
        return new EqualsBuilder().append(schedulerId, rhs.schedulerId).append(path, rhs.path).isEquals();
    }

}
