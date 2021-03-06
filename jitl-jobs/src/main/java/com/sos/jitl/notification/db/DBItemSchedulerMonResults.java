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

import com.sos.hibernate.classes.DbItem;

import sos.util.SOSString;

// @MappedSuperclass
@Entity
@Table(name = DBLayer.TABLE_SCHEDULER_MON_RESULTS)
@SequenceGenerator(name = DBLayer.SEQUENCE_SCHEDULER_MON_RESULTS, sequenceName = DBLayer.SEQUENCE_SCHEDULER_MON_RESULTS, allocationSize = 1)
public class DBItemSchedulerMonResults extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** id */
    private Long id;

    /** logical foreign key SCHEDULER_MON_NOTIFICATIONS.ID */
    private Long notificationId;

    /** others */
    private String name;
    private String value;
    private Date created;
    private Date modified;

    public DBItemSchedulerMonResults() {

    }

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.SEQUENCE_SCHEDULER_MON_RESULTS)
    @Column(name = "[ID]", nullable = false)
    public Long getId() {
        return id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.SEQUENCE_SCHEDULER_MON_RESULTS)
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

    @Column(name = "[VALUE]", nullable = true)
    public void setValue(String val) {
        if (SOSString.isEmpty(val)) {
            val = null;
        }
        value = val;
    }

    @Column(name = "[VALUE]", nullable = true)
    public String getValue() {
        return value;
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
        if (!(other instanceof DBItemSchedulerMonResults)) {
            return false;
        }
        DBItemSchedulerMonResults otherEntity = ((DBItemSchedulerMonResults) other);
        return new EqualsBuilder().append(id, otherEntity.id).isEquals();
    }

}
