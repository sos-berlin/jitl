package com.sos.jitl.reporting.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = DBLayer.TABLE_DOCUMENTATION_IMAGES)
@SequenceGenerator(name = DBLayer.TABLE_DOCUMENTATION_IMAGES_SEQUENCE, sequenceName = DBLayer.TABLE_DOCUMENTATION_IMAGES_SEQUENCE, allocationSize = 1)
public class DBItemDocumentationImage extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String schedulerId;
    private byte[] image;
    
    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_DOCUMENTATION_IMAGES_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public Long getId() {
        return id;
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_DOCUMENTATION_IMAGES_SEQUENCE)
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
        return new HashCodeBuilder().append(schedulerId).append(image).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemDocumentationImage)) {
            return false;
        }
        DBItemDocumentationImage rhs = ((DBItemDocumentationImage) other);
        return new EqualsBuilder().append(schedulerId, rhs.schedulerId).append(image, rhs.image).isEquals();
    }

}
