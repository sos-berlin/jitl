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

import org.hibernate.annotations.Type;

import sos.util.SOSString;

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = DBLayer.TABLE_INVENTORY_JOBS)
@SequenceGenerator(name = DBLayer.TABLE_INVENTORY_JOBS_SEQUENCE, sequenceName = DBLayer.TABLE_INVENTORY_JOBS_SEQUENCE, allocationSize = 1)
public class DBItemInventoryJob extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int TITLE_MAX_LENGTH = 255;

    /** Primary key */
    private Long id;

    /** Foreign key INVENTORY_INSTANCES.ID */
    private Long instanceId;
    /** Foreign key INVENTORY_FILES.ID */
    private Long fileId;

    /** Others */
    private String name;
    private String baseName;
    private String title;
    private boolean isOrderJob;
    private boolean isRuntimeDefined;
    private Date created;
    private Date modified;

    /** new fields starting release 1.11 */
    private Integer usedInJobChains;
    private String processClass;
    /** foreign key INVENTORY_PROCESS_CLASSES.NAME (= . if undefined) */
    private String processClassName;
    /** foreign key INVENTORY_PROCESS_CLASSES.ID (= 0 if undefined) */
    private Long processClassId;
    private String schedule;
    /** foreign key INVENTORY_SCHEDULES.NAME (= . if undefined) */
    private String scheduleName;
    /** foreign key INVENTORY_SCHEDULES.ID (= 0 if undefined) */
    private Long scheduleId;
    private Integer maxTasks;
    private boolean hasDescription;
    
    public DBItemInventoryJob() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INVENTORY_JOBS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INVENTORY_JOBS_SEQUENCE)
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

    @Column(name = "`IS_ORDER_JOB`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setIsOrderJob(boolean val) {
        this.isOrderJob = val;
    }

    @Column(name = "`IS_ORDER_JOB`", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getIsOrderJob() {
        return this.isOrderJob;
    }

    @Column(name = "`IS_RUNTIME_DEFINED`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setIsRuntimeDefined(boolean val) {
        this.isRuntimeDefined = val;
    }

    @Column(name = "`IS_RUNTIME_DEFINED`", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getIsRuntimeDefined() {
        return this.isRuntimeDefined;
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

    @Column(name = "`USED_IN_JOB_CHAINS`", nullable = false)
    public Integer getUsedInJobChains() {
        return usedInJobChains;
    }

    @Column(name = "`USED_IN_JOB_CHAINS`", nullable = false)
    public void setUsedInJobChains(Integer usedInJobChains) {
        this.usedInJobChains = usedInJobChains;
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

    @Column(name = "`SCHEDULE`", nullable = true)
    public String getSchedule() {
        return schedule;
    }

    @Column(name = "`SCHEDULE`", nullable = true)
    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    @Column(name = "`SCHEDULE_NAME`", nullable = false)
    public String getScheduleName() {
        return scheduleName;
    }

    @Column(name = "`SCHEDULE_NAME`", nullable = false)
    public void setScheduleName(String scheduleName) {
        if (scheduleName == null || scheduleName.isEmpty()) {
            scheduleName = DBLayer.DEFAULT_NAME;
        }
        this.scheduleName = scheduleName;
    }

    @Column(name = "`SCHEDULE_ID`", nullable = false)
    public Long getScheduleId() {
        return scheduleId;
    }

    @Column(name = "`SCHEDULE_ID`", nullable = false)
    public void setScheduleId(Long scheduleId) {
        if (scheduleId == null) {
            scheduleId = DBLayer.DEFAULT_ID;
        }
        this.scheduleId = scheduleId;
    }

    @Column(name = "`MAX_TASKS`", nullable = false)
    public Integer getMaxTasks() {
        return maxTasks;
    }

    @Column(name = "`MAX_TASKS`", nullable = false)
    public void setMaxTasks(Integer maxTasks) {
        this.maxTasks = maxTasks;
    }

    @Column(name = "`HAS_DESCRIPTION`", nullable = true)
    @Type(type = "numeric_boolean")
    public boolean getHasDescription() {
        return hasDescription;
    }

    @Column(name = "`HAS_DESCRIPTION`", nullable = true)
    @Type(type = "numeric_boolean")
    public void setHasDescription(boolean hasDescription) {
        this.hasDescription = hasDescription;
    }
    
}