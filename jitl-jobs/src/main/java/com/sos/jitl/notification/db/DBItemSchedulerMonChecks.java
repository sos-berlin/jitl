package com.sos.jitl.notification.db;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;

import com.sos.hibernate.classes.DbItem;

import sos.util.SOSString;

@Entity
@Table(name = DBLayer.TABLE_SCHEDULER_MON_CHECKS)
@SequenceGenerator(name = DBLayer.SEQUENCE_SCHEDULER_MON_CHECKS, sequenceName = DBLayer.SEQUENCE_SCHEDULER_MON_CHECKS, allocationSize = 1)
public class DBItemSchedulerMonChecks extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** id */
    private Long id;

    /** logical foreign key SCHEDULER_MON_NOTIFICATIONS.ID */
    private Long notificationId;

    /** others */
    private String name;
    private String stepFrom;
    private String stepTo;
    private Date stepFromStartTime;
    private Date stepToEndTime;
    /** logical foreign key SCHEDULER_MON_RESULTS.ID durch ; getrennt */
    private String resultIds;
    private boolean checked;
    private String checkText;
    private Long objectType; // notification object 0 - JobChain, 1 - Job

    private Date created;
    private Date modified;

    /**
     * 
     */
    public DBItemSchedulerMonChecks() {
        setNotificationId(new Long(0));
        setChecked(false);
    }

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.SEQUENCE_SCHEDULER_MON_CHECKS)
    @Column(name = "[ID]", nullable = false)
    public Long getId() {
        return id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.SEQUENCE_SCHEDULER_MON_CHECKS)
    @Column(name = "[ID]", nullable = false)
    public void setId(Long val) {
        id = val;
    }

    /** logical foreign key SCHEDULER_MON_NOTIFICATIONS.ID */
    @Column(name = "[NOTIFICATION_ID]", nullable = false)
    public Long getNotificationId() {
        return notificationId;
    }

    @Column(name = "[NOTIFICATION_ID]", nullable = false)
    public void setNotificationId(Long val) {
        notificationId = val;
    }

    /** others */
    @Column(name = "[NAME]", nullable = false)
    public void setName(String val) {
        name = val;
    }

    @Column(name = "[NAME]", nullable = false)
    public String getName() {
        return name;
    }

    @Column(name = "[STEP_FROM]", nullable = false)
    public void setStepFrom(String val) {
        stepFrom = val;
    }

    @Column(name = "[STEP_FROM]", nullable = false)
    public String getStepFrom() {
        return stepFrom;
    }

    @Column(name = "[STEP_TO]", nullable = false)
    public void setStepTo(String val) {
        stepTo = val;
    }

    @Column(name = "[STEP_TO]", nullable = false)
    public String getStepTo() {
        return stepTo;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[STEP_FROM_START_TIME]", nullable = true)
    public void setStepFromStartTime(Date val) {
        stepFromStartTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[STEP_FROM_START_TIME]", nullable = true)
    public Date getStepFromStartTime() {
        return stepFromStartTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[STEP_TO_END_TIME]", nullable = true)
    public void setStepToEndTime(Date val) {
        stepToEndTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[STEP_TO_END_TIME]", nullable = true)
    public Date getStepToEndTime() {
        return stepToEndTime;
    }

    /** logical foreign key SCHEDULER_MON_RESULTS.ID */
    @Column(name = "[RESULT_IDS]", nullable = true)
    public String getResultIds() {
        return resultIds;
    }

    @Column(name = "[RESULT_IDS]", nullable = true)
    public void setResultIds(String val) {
        resultIds = val;
    }

    @Column(name = "[CHECKED]", nullable = false)
    @Type(type = "numeric_boolean")
    public void setChecked(boolean val) {
        checked = val;
    }

    @Column(name = "[CHECKED]", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getChecked() {
        return checked;
    }

    @Column(name = "[CHECK_TEXT]", nullable = true)
    public void setCheckText(String val) {
        if (SOSString.isEmpty(val)) {
            val = null;
        } else {
            val = StringUtils.left(val, 255);
        }
        checkText = val;
    }

    @Column(name = "[CHECK_TEXT]", nullable = true)
    public String getCheckText() {
        return checkText;
    }

    @Column(name = "[OBJECT_TYPE]", nullable = false)
    public void setObjectType(Long val) {
        objectType = (val == null) ? DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN : val;
    }

    @Column(name = "[OBJECT_TYPE]", nullable = false)
    public Long getObjectType() {
        return objectType;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    public void setCreated(Date val) {
        created = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    public Date getCreated() {
        return created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = false)
    public void setModified(Date val) {
        modified = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = false)
    public Date getModified() {
        return modified;
    }

    @Override
    public int hashCode() {
        // always build on unique constraint
        return new HashCodeBuilder().append(id).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemSchedulerMonChecks)) {
            return false;
        }
        DBItemSchedulerMonChecks otherEntity = ((DBItemSchedulerMonChecks) other);
        return new EqualsBuilder().append(id, otherEntity.id).isEquals();
    }
}
