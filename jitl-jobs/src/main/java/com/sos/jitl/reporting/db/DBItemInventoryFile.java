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
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import sos.util.SOSString;

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = DBLayer.TABLE_INVENTORY_FILES)
@SequenceGenerator(name = DBLayer.TABLE_INVENTORY_FILES_SEQUENCE, sequenceName = DBLayer.TABLE_INVENTORY_FILES_SEQUENCE, allocationSize = 1)
public class DBItemInventoryFile extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    private Long id;

    /** Foreign key INVENTORY_INSTANCES.ID */
    private Long instanceId;

    /** Others */
    private String fileType;
    private String fileName;
    private String fileBaseName;
    private String fileDirectory;
    private Date fileCreated;
    private Date fileModified;
    private Date fileLocalCreated;
    private Date fileLocalModified;
    private Date created;
    private Date modified;

    public DBItemInventoryFile() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INVENTORY_FILES_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INVENTORY_FILES_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public void setId(Long val) {
        this.id = val;
    }

    /** Foreign key INVENTORY_INSTANCES.ID */
    @Column(name = "[INSTANCE_ID]", nullable = false)
    public Long getInstanceId() {
        return this.instanceId;
    }

    @Column(name = "[INSTANCE_ID]", nullable = false)
    public void setInstanceId(Long val) {
        this.instanceId = val;
    }

    /** Others */
    @Column(name = "[FILE_TYPE]", nullable = false)
    public void setFileType(String val) {
        this.fileType = val;
    }

    @Column(name = "[FILE_TYPE]", nullable = false)
    public String getFileType() {
        return this.fileType;
    }

    @Column(name = "[FILE_NAME]", nullable = false)
    public void setFileName(String val) {
        this.fileName = val;
    }

    @Column(name = "[FILE_NAME]", nullable = false)
    public String getFileName() {
        return this.fileName;
    }

    @Column(name = "[FILE_BASENAME]", nullable = false)
    public void setFileBaseName(String val) {
        this.fileBaseName = val;
    }

    @Column(name = "[FILE_BASENAME]", nullable = false)
    public String getFileBaseName() {
        return this.fileBaseName;
    }

    @Column(name = "[FILE_DIRECTORY]", nullable = false)
    public void setFileDirectory(String val) {
        if (SOSString.isEmpty(val)) {
            val = DBLayer.DEFAULT_FOLDER;
        }
        this.fileDirectory = val;
    }

    @Column(name = "[FILE_DIRECTORY]", nullable = false)
    public String getFileDirectory() {
        return this.fileDirectory;
    }

    @Column(name = "[FILE_CREATED]", nullable = true)
    public void setFileCreated(Date val) {
        this.fileCreated = val;
    }

    @Column(name = "[FILE_CREATED]", nullable = true)
    public Date getFileCreated() {
        return this.fileCreated;
    }

    @Column(name = "[FILE_MODIFIED]", nullable = true)
    public void setFileModified(Date val) {
        this.fileModified = val;
    }

    @Column(name = "[FILE_MODIFIED]", nullable = true)
    public Date getFileModified() {
        return this.fileModified;
    }

    @Column(name = "[FILE_LOCAL_CREATED]", nullable = true)
    public void setFileLocalCreated(Date val) {
        this.fileLocalCreated = val;
    }

    @Column(name = "[FILE_LOCAL_CREATED]", nullable = true)
    public Date getFileLocalCreated() {
        return this.fileLocalCreated;
    }

    @Column(name = "[FILE_LOCAL_MODIFIED]", nullable = true)
    public void setFileLocalModified(Date val) {
        this.fileLocalModified = val;
    }

    @Column(name = "[FILE_LOCAL_MODIFIED]", nullable = true)
    public Date getFileLocalModified() {
        return this.fileLocalModified;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    public void setCreated(Date val) {
        this.created = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    public Date getCreated() {
        return this.created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = false)
    public void setModified(Date val) {
        this.modified = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = false)
    public Date getModified() {
        return this.modified;
    }

    @Override
    public int hashCode() {
        // always build on unique constraint
        if (isWindows()) {
            return new HashCodeBuilder().append(instanceId).append(fileName.toLowerCase()).toHashCode();
        } else {
            return new HashCodeBuilder().append(instanceId).append(fileName).toHashCode();
        }
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemInventoryFile)) {
            return false;
        }
        DBItemInventoryFile rhs = ((DBItemInventoryFile) other);
        if (isWindows()) {
            return new EqualsBuilder().append(instanceId, rhs.instanceId).append(fileName.toLowerCase(), rhs.fileName.toLowerCase())
                    .isEquals();
        } else {
            return new EqualsBuilder().append(instanceId, rhs.instanceId).append(fileName, rhs.fileName).isEquals();
        }
    }
    
    @Transient
    private boolean isWindows(){
        String os = System.getProperty("os.name");
        return os.toLowerCase().contains("win");
    }

}