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

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = DBLayer.TABLE_SUBMISSIONS)
@SequenceGenerator(name = DBLayer.TABLE_SUBMISSIONS_SEQUENCE, sequenceName = DBLayer.TABLE_SUBMISSIONS_SEQUENCE, allocationSize = 1)
public class DBItemSubmission extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long instanceId;
    private Long submissionId;
    
    
    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_SUBMISSIONS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public Long getId() {
        return id;
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_SUBMISSIONS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    public void setId(Long id) {
        this.id = id;
    }
    
    /** Others */
    @Column(name = "[INSTANCE_ID]", nullable = false)
    public Long getInstanceId() {
        return instanceId;
    }
    
    @Column(name = "[INSTANCE_ID]", nullable = false)
    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }
    
    @Column(name = "[SUBMISSION_ID]", nullable = false)
    public Long getSubmissionId() {
        return submissionId;
    }
    
    @Column(name = "[SUBMISSION_ID]", nullable = false)
    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }
    
    @Override
    public int hashCode() {
        // always build on unique constraint
        return new HashCodeBuilder().append(instanceId).append(submissionId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemSubmission)) {
            return false;
        }
        DBItemSubmission rhs = ((DBItemSubmission) other);
        return new EqualsBuilder().append(instanceId, rhs.instanceId).append(submissionId, rhs.submissionId).isEquals();
    }

}
