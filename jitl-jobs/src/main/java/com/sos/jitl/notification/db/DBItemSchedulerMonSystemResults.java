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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = DBLayer.TABLE_SCHEDULER_MON_SYSRESULTS)
@SequenceGenerator(name = DBLayer.SEQUENCE_SCHEDULER_MON_SYSRESULTS, sequenceName = DBLayer.SEQUENCE_SCHEDULER_MON_SYSRESULTS, allocationSize = 1)
public class DBItemSchedulerMonSystemResults extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    // logical foreign key SCHEDULER_MON_SYSNOTIFICATIONS.ID
    private Long sysNotificationId;
    /** unique */
    // logical foreign key SCHEDULER_MON_NOTIFICATIONS.ID
    private Long notificationId;

    private Long orderStep;
    private String orderStepState;
    private Long currentNotification;
    private boolean recovered;
    private Date created;
    private Date modified;

    public DBItemSchedulerMonSystemResults() {
        setCurrentNotification(new Long(0));
        setRecovered(false);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.SEQUENCE_SCHEDULER_MON_SYSRESULTS)
    @Column(name = "[ID]", nullable = false)
    public Long getId() {
        return id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.SEQUENCE_SCHEDULER_MON_SYSRESULTS)
    @Column(name = "[ID]", nullable = false)
    public void setId(Long val) {
        id = val;
    }

    /** unique */
    // logical foreign key SCHEDULER_MON_SYSNOTIFICATIONS.ID
    @Column(name = "[SYS_NOTIFICATION_ID]", nullable = false)
    public Long getSysNotificationId() {
        return sysNotificationId;
    }

    @Column(name = "[SYS_NOTIFICATION_ID]", nullable = false)
    public void setSysNotificationId(Long val) {
        sysNotificationId = val;
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

    @Column(name = "[ORDER_STEP]", nullable = false)
    public void setOrderStep(Long val) {
        orderStep = (val == null) ? DBLayer.DEFAULT_EMPTY_NUMERIC : val;
    }

    @Column(name = "[ORDER_STEP]", nullable = false)
    public Long getOrderStep() {
        return orderStep;
    }

    @Column(name = "[ORDER_STEP_STATE]", nullable = false)
    public void setOrderStepState(String val) {
        orderStepState = val;
    }

    @Column(name = "[ORDER_STEP_STATE]", nullable = false)
    public String getOrderStepState() {
        return orderStepState;
    }

    @Column(name = "[CURRENT_NOTIFICATION]", nullable = false)
    public void setCurrentNotification(Long val) {
        currentNotification = (val == null) ? DBLayer.DEFAULT_EMPTY_NUMERIC : val;
    }

    @Column(name = "[CURRENT_NOTIFICATION]", nullable = false)
    public Long getCurrentNotification() {
        return currentNotification;
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
        return new HashCodeBuilder().append(sysNotificationId).append(notificationId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemSchedulerMonSystemResults)) {
            return false;
        }
        DBItemSchedulerMonSystemResults otherEntity = ((DBItemSchedulerMonSystemResults) other);
        return new EqualsBuilder().append(sysNotificationId, otherEntity.sysNotificationId).append(notificationId, otherEntity.notificationId)
                .isEquals();
    }

}
