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

import sos.util.SOSString;

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = DBLayer.TABLE_INVENTORY_JOB_CHAINS)
@SequenceGenerator(name = DBLayer.TABLE_INVENTORY_JOB_CHAINS_SEQUENCE, sequenceName = DBLayer.TABLE_INVENTORY_JOB_CHAINS_SEQUENCE, allocationSize = 1)
public class DBItemInventoryJobChain extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int TITLE_MAX_LENGTH = 255;

    /** Primary key */
    private Long id;

    /** Foreign key INVENTORY_INSTANCES.ID */
    private Long instanceId;
    /** Foreign key INVENTORY_FILES.ID */
    private Long fileId;

    /** Others */
    private String startCause;
    private String name;
    private String baseName;
    private String title;
    private Date created;
    private Date modified;
    
    /** new fields starting release 1.11 */
    private Integer maxOrders;
    private boolean distributed;
    private String processClass;
    /** foreign key INVENTORY_PROCESS_CLASSES.NAME (= . if undefined) */
    private String processClassName;
    /** foreign key INVENTORY_PROCESS_CLASSES.ID (= 0 if undefined) */
    private Long processClassId;
    private String fileWatchingProcessClass;
    /** foreign key INVENTORY_PROCESS_CLASSES.NAME (= . if undefined) */
    private String fileWatchingProcessClassName;
    /** foreign key INVENTORY_PROCESS_CLASSES.ID (= 0 if undefined) */
    private Long fileWatchingProcessClassId;

    public DBItemInventoryJobChain() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INVENTORY_JOB_CHAINS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INVENTORY_JOB_CHAINS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long val) {
        this.id = val;
    }

    /** Foreign key INVENTORY_INSTANCES.ID */
    @Column(name = "`INSTANCE_ID`", nullable = false)
    public Long getInstanceId() {
        return this.instanceId;
    }

    @Column(name = "`INSTANCE_ID`", nullable = false)
    public void setInstanceId(Long val) {
        if (instanceId == null) {
            instanceId = DBLayer.DEFAULT_ID;
        }
        this.instanceId = val;
    }

    /** Foreign key INVENTORY_FILES.ID */
    @Column(name = "`FILE_ID`", nullable = false)
    public Long getFileId() {
        return this.fileId;
    }

    @Column(name = "`FILE_ID`", nullable = false)
    public void setFileId(Long val) {
        if (fileId == null) {
            fileId = DBLayer.DEFAULT_ID;
        }
        this.fileId = val;
    }

    /** Others */
    @Column(name = "`START_CAUSE`", nullable = false)
    public void setStartCause(String val) {
        this.startCause = val;
    }

    @Column(name = "`START_CAUSE`", nullable = false)
    public String getStartCause() {
        return this.startCause;
    }

    @Column(name = "`NAME`", nullable = false)
    public void setName(String val) {
        this.name = val;
    }

    @Column(name = "`NAME`", nullable = false)
    public String getName() {
        return this.name;
    }

    @Column(name = "`BASENAME`", nullable = false)
    public void setBaseName(String val) {
        this.baseName = val;
    }

    @Column(name = "`BASENAME`", nullable = false)
    public String getBaseName() {
        return this.baseName;
    }

    @Column(name = "`TITLE`", nullable = true)
    public void setTitle(String val) {
        if (SOSString.isEmpty(val)) {
            val = null;
        } else {
            if (val.length() > TITLE_MAX_LENGTH) {
                val = val.substring(0, TITLE_MAX_LENGTH);
            }
        }
        this.title = val;
    }

    @Column(name = "`TITLE`", nullable = true)
    public String getTitle() {
        return this.title;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public void setCreated(Date val) {
        this.created = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public Date getCreated() {
        return this.created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public void setModified(Date val) {
        this.modified = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public Date getModified() {
        return this.modified;
    }

    @Column(name = "`MAX_ORDERS`", nullable = true)
    public Integer getMaxOrders() {
        return maxOrders;
    }

    @Column(name = "`MAX_ORDERS`", nullable = true)
    public void setMaxOrders(Integer maxOrders) {
        this.maxOrders = maxOrders;
    }

    @Column(name = "`DISTRIBUTED`", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getDistributed() {
        return distributed;
    }

    @Column(name = "`DISTRIBUTED`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setDistributed(boolean distributed) {
        this.distributed = distributed;
    }

    @Column(name = "`PROCESS_CLASS`", nullable = true)
    public String getProcessClass() {
        return processClass;
    }

    @Column(name = "`PROCESS_CLASS`", nullable = true)
    public void setProcessClass(String processClass) {
        this.processClass = processClass;
    }

    @Column(name = "`PROCESS_CLASS_NAME`", nullable = false)
    public String getProcessClassName() {
        return processClassName;
    }

    @Column(name = "`PROCESS_CLASS_NAME`", nullable = false)
    public void setProcessClassName(String processClassName) {
        if (processClassName == null || processClassName.isEmpty()) {
            processClassName = DBLayer.DEFAULT_NAME;
        }
        this.processClassName = processClassName;
    }

    @Column(name = "`PROCESS_CLASS_ID`", nullable = false)
    public Long getProcessClassId() {
        return processClassId;
    }

    @Column(name = "`PROCESS_CLASS_ID`", nullable = false)
    public void setProcessClassId(Long processClassId) {
        if (processClassId == null) {
            processClassId = DBLayer.DEFAULT_ID;
        }
        this.processClassId = processClassId;
    }

    @Column(name = "`FW_PROCESS_CLASS`", nullable = true)
    public String getFileWatchingProcessClass() {
        return fileWatchingProcessClass;
    }

    @Column(name = "`FW_PROCESS_CLASS`", nullable = true)
    public void setFileWatchingProcessClass(String fileWatchingProcessClass) {
        this.fileWatchingProcessClass = fileWatchingProcessClass;
    }

    @Column(name = "`FW_PROCESS_CLASS_NAME`", nullable = false)
    public String getFileWatchingProcessClassName() {
        return fileWatchingProcessClassName;
    }

    @Column(name = "`FW_PROCESS_CLASS_NAME`", nullable = false)
    public void setFileWatchingProcessClassName(String fileWatchingProcessClassName) {
        if (fileWatchingProcessClassName == null || fileWatchingProcessClassName.isEmpty()) {
            fileWatchingProcessClassName = DBLayer.DEFAULT_NAME;
        }
        this.fileWatchingProcessClassName = fileWatchingProcessClassName;
    }

    @Column(name = "`FW_PROCESS_CLASS_ID`", nullable = false)
    public Long getFileWatchingProcessClassId() {
        return fileWatchingProcessClassId;
    }

    @Column(name = "`FW_PROCESS_CLASS_ID`", nullable = false)
    public void setFileWatchingProcessClassId(Long fileWatchingProcessClassId) {
        if (fileWatchingProcessClassId == null) {
            fileWatchingProcessClassId = DBLayer.DEFAULT_ID;
        }
        this.fileWatchingProcessClassId = fileWatchingProcessClassId;
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
        if (!(other instanceof DBItemInventoryJobChain)) {
            return false;
        }
        DBItemInventoryJobChain rhs = ((DBItemInventoryJobChain) other);
        return new EqualsBuilder().append(instanceId, rhs.instanceId).append(name, rhs.name).isEquals();
    }

}