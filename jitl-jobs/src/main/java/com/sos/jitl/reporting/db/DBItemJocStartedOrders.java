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

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = DBLayer.TABLE_STARTED_ORDERS)
@SequenceGenerator(name = DBLayer.TABLE_STARTED_ORDERS_SEQUENCE, sequenceName = DBLayer.TABLE_STARTED_ORDERS_SEQUENCE, allocationSize = 1)
public class DBItemJocStartedOrders extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String schedulerId;
    private String jobChain;
    private String orderId;
    private Date plannedStart;
    private Date created;

     /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_STARTED_ORDERS_SEQUENCE)
    @Column(name = "[ID]",  nullable = false)
    public Long getId() {
        return this.id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_STARTED_ORDERS_SEQUENCE)
    @Column(name = "[ID]",  nullable = false)
    public void setId(Long val) {
        this.id = val;
    }

    /** Others */
    @Column(name = "[SCHEDULER_ID]",  nullable = false)
    public void setSchedulerId(String val) {
        this.schedulerId = val;
    }

    @Column(name = "[SCHEDULER_ID]",  nullable = false)
    public String getSchedulerId() {
        return this.schedulerId;
    }
    
    @Column(name = "[JOB_CHAIN]",  nullable = false)
    public String getJobChain() {
        return jobChain;
    }
    
    @Column(name = "[JOB_CHAIN]",  nullable = false)
    public void setJobChain(String jobChain) {
        this.jobChain = jobChain;
    }
    
    @Column(name = "[ORDER_ID]",  nullable = false)
    public String getOrderId() {
        return orderId;
    }
    
    @Column(name = "[ORDER_ID]",  nullable = false)
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[PLANNED_START]",  nullable = false)
    public Date getPlannedStart() {
        return plannedStart;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[PLANNED_START]",  nullable = false)
    public void setPlannedStart(Date val) {
        this.plannedStart = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]",  nullable = false)
    public Date getCreated() {
        return created;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]",  nullable = false)
    public void setCreated(Date created) {
        this.created = created;
    }

}