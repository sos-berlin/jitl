package com.sos.jitl.notification.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateFactory;

import sos.util.SOSString;

@Entity
@Table(name = DBLayer.TABLE_SCHEDULER_MON_SYSNOTIFICATIONS)
@SequenceGenerator(name = DBLayer.SEQUENCE_SCHEDULER_MON_SYSNOTIFICATIONS, sequenceName = DBLayer.SEQUENCE_SCHEDULER_MON_SYSNOTIFICATIONS, allocationSize = 1)
/** uniqueConstraints = { @UniqueConstraint(columnNames = {"[NOTIFICATION_ID]", "[SYSTEM_ID`"})} */
public class DBItemSchedulerMonSystemNotifications extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    /** unique */
    // logical foreign key SCHEDULER_MON_NOTIFICATIONS.ID
    private Long notificationId;
    // logical foreign key SCHEDULER_MON_CHECKS.ID
    private Long checkId;
    private String systemId;
    private String serviceName;
    private String stepFrom;
    private String stepTo;
    private String returnCodeFrom;
    private String returnCodeTo;
    private Long objectType; // notification object 0 - JobChain, 1 - Job etc see DBLayer
    private String title;
    private Long notifications;
    private Long currentNotification;
    private boolean maxNotifications;
    private boolean acknowledged;
    private boolean recovered;
    private boolean success;
    private Date stepFromStartTime;
    private Date stepToEndTime;
    private String notificationResults;
    // logical foreign key AUDIT_LOG.ID
    private Long auditLogId;
    private Date created;
    private Date modified;
    /** parent table SCHEDULER_MON_NOTIFICATIONS */
    private DBItemSchedulerMonNotifications dbItemSchedulerMonNotifications;

    public DBItemSchedulerMonSystemNotifications() {
        setNotifications(new Long(0));
        setCurrentNotification(new Long(0));
        setMaxNotifications(false);
        setAcknowledged(false);
        setRecovered(false);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.SEQUENCE_SCHEDULER_MON_SYSNOTIFICATIONS)
    @Column(name = "[ID]", nullable = false)
    public Long getId() {
        return id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.SEQUENCE_SCHEDULER_MON_SYSNOTIFICATIONS)
    @Column(name = "[ID]", nullable = false)
    public void setId(Long val) {
        id = val;
    }

    /** unique */
    // logical foreign key SCHEDULER_MON_NOTIFICATIONS.ID
    @Column(name = "[NOTIFICATION_ID]", nullable = false)
    public Long getNotificationId() {
        return notificationId;
    }

    @Column(name = "[NOTIFICATION_ID]", nullable = false)
    public void setNotificationId(Long val) {
        notificationId = val;
    }

    // logical foreign key SCHEDULER_MON_CHECKS.ID
    @Column(name = "[CHECK_ID]", nullable = false)
    public Long getCheckId() {
        return checkId;
    }

    @Column(name = "[CHECK_ID]", nullable = false)
    public void setCheckId(Long val) {
        checkId = val;
    }

    @Column(name = "[SYSTEM_ID]", nullable = false)
    public void setSystemId(String val) {
        systemId = val;
    }

    @Column(name = "[SYSTEM_ID]", nullable = false)
    public String getSystemId() {
        return systemId;
    }

    @Column(name = "[SERVICE_NAME]", nullable = false)
    public void setServiceName(String val) {
        serviceName = val;
    }

    @Column(name = "[SERVICE_NAME]", nullable = false)
    public String getServiceName() {
        return serviceName;
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

    @Column(name = "[RETURN_CODE_FROM]", nullable = false)
    public void setReturnCodeFrom(String val) {
        returnCodeFrom = (val == null) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : val;
    }

    @Column(name = "[RETURN_CODE_FROM]", nullable = false)
    public String getReturnCodeFrom() {
        return returnCodeFrom;
    }

    @Column(name = "[RETURN_CODE_TO]", nullable = false)
    public void setReturnCodeTo(String val) {
        returnCodeTo = (val == null) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : val;
    }

    @Column(name = "[RETURN_CODE_TO]", nullable = false)
    public String getReturnCodeTo() {
        return returnCodeTo;
    }

    @Column(name = "[OBJECT_TYPE]", nullable = false)
    public void setObjectType(Long val) {
        objectType = (val == null) ? DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN : val;
    }

    @Column(name = "[OBJECT_TYPE]", nullable = false)
    public Long getObjectType() {
        return objectType;
    }

    @Column(name = "[TITLE]", nullable = true)
    public void setTitle(String val) {
        if (SOSString.isEmpty(val)) {
            val = null;
        } else {
            val = StringUtils.left(val, 255);

        }
        title = val;
    }

    @Column(name = "[TITLE]", nullable = true)
    public String getTitle() {
        return title;
    }

    @Column(name = "[NOTIFICATIONS]", nullable = true)
    public void setNotifications(Long val) {
        notifications = val;
    }

    @Column(name = "[NOTIFICATIONS]", nullable = false)
    public Long getNotifications() {
        return notifications;
    }

    @Column(name = "[CURRENT_NOTIFICATION]", nullable = false)
    public void setCurrentNotification(Long val) {
        currentNotification = (val == null) ? DBLayer.DEFAULT_EMPTY_NUMERIC : val;
    }

    @Column(name = "[CURRENT_NOTIFICATION]", nullable = false)
    public Long getCurrentNotification() {
        return currentNotification;
    }

    @Column(name = "[MAX_NOTIFICATIONS]", nullable = false)
    @Type(type = "numeric_boolean")
    public void setMaxNotifications(boolean val) {
        maxNotifications = val;
    }

    @Column(name = "[MAX_NOTIFICATIONS]", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getMaxNotifications() {
        return maxNotifications;
    }

    @Column(name = "[ACKNOWLEDGED]", nullable = false)
    @Type(type = "numeric_boolean")
    public void setAcknowledged(boolean val) {
        acknowledged = val;
    }

    @Column(name = "[ACKNOWLEDGED]", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getAcknowledged() {
        return acknowledged;
    }

    @Column(name = "[RECOVERED]", nullable = false)
    @Type(type = "numeric_boolean")
    public void setRecovered(boolean val) {
        recovered = val;
    }

    @Column(name = "[RECOVERED]", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getRecovered() {
        return recovered;
    }

    @Column(name = "[SUCCESS]", nullable = false)
    @Type(type = "numeric_boolean")
    public void setSuccess(boolean val) {
        success = val;
    }

    @Column(name = "[SUCCESS]", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getSuccess() {
        return success;
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

    @Column(name = "[NOTIFICATION_RESULTS]", nullable = true)
    public void setNotificationResults(String val) {
        notificationResults = val;
    }

    @Column(name = "[NOTIFICATION_RESULTS]", nullable = true)
    public String getNotificationResults() {
        return notificationResults;
    }

    @Column(name = "[AUDIT_LOG_ID]", nullable = true)
    public Long getAuditLogId() {
        return auditLogId;
    }

    @Column(name = "[AUDIT_LOG_ID]", nullable = true)
    public void setAuditLogId(Long val) {
        auditLogId = val;
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

    @ManyToOne(optional = true)
    @JoinColumn(name = "`NOTIFICATION_ID`", insertable = false, updatable = false)
    public DBItemSchedulerMonNotifications getDbItemSchedulerMonNotifications() {
        return dbItemSchedulerMonNotifications;
    }

    public void setDbItemSchedulerMonNotifications(DBItemSchedulerMonNotifications val) {
        dbItemSchedulerMonNotifications = val;
    }

    public String toString() {
        return SOSHibernateFactory.toString(this);
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
        if (!(other instanceof DBItemSchedulerMonSystemNotifications)) {
            return false;
        }
        DBItemSchedulerMonSystemNotifications otherEntity = ((DBItemSchedulerMonSystemNotifications) other);
        return new EqualsBuilder().append(id, otherEntity.id).isEquals();
    }

}
