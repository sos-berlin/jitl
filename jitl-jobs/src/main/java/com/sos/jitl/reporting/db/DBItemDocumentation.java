package com.sos.jitl.reporting.db;

import java.awt.Image;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = DBLayer.TABLE_DOCUMENTATION)
@SequenceGenerator(name = DBLayer.TABLE_DOCUMENTATION_SEQUENCE, sequenceName = DBLayer.TABLE_DOCUMENTATION_SEQUENCE, allocationSize = 1)
public class DBItemDocumentation extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String schedulerId;
    private String name;
    private String directory;
    private String type;    
    private String content;
    private byte[] image;
    private String title;
    private Date created;
    private Date modified;
    
    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_DOCUMENTATION_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public Long getId() {
        return id;
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_DOCUMENTATION_SEQUENCE)
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
    
    @Column(name = "[NAME]", nullable = false)
    public String getName() {
        return name;
    }
    
    @Column(name = "[NAME]", nullable = false)
    public void setName(String name) {
        this.name = name;
    }
    
    @Column(name = "[DIRECTORY]", nullable = false)
    public String getDirectory() {
        return directory;
    }
    
    @Column(name = "[DIRECTORY]", nullable = false)
    public void setDirectory(String directory) {
        this.directory = directory;
    }
    
    @Column(name = "[TYPE]", nullable = false)
    public String getType() {
        return type;
    }
    
    @Column(name = "[TYPE]", nullable = false)
    public void setType(String type) {
        this.type = type;
    }
    
    @Column(name = "[CONTENT]", nullable = true)
    public String getContent() {
        return content;
    }
    
    @Column(name = "[CONTENT]", nullable = true)
    public void setContent(String content) {
        this.content = content;
    }
    
    @Column(name = "[TITLE]", nullable = true)
    public String getTitle() {
        return title;
    }
    
    @Column(name = "[TITLE]", nullable = true)
    public void setTitle(String title) {
        this.title = title;
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
    
    @Column(name = "[IMAGE]", nullable = true)
    @Type(type="org.hibernate.type.BinaryType")
    public byte[] getImage() {
        return image;
    }
    
    @Column(name = "[IMAGE]", nullable = true)
    @Type(type="org.hibernate.type.BinaryType")
    public void setImage(byte[] image) {
        this.image = image;
    }

    @Override
    public int hashCode() {
        // always build on unique constraint
        return new HashCodeBuilder().append(schedulerId).append(name).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemDocumentation)) {
            return false;
        }
        DBItemDocumentation rhs = ((DBItemDocumentation) other);
        return new EqualsBuilder().append(schedulerId, rhs.schedulerId).append(name, rhs.name).isEquals();
    }

}
