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

import sos.util.SOSString;

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = DBLayer.TABLE_INVENTORY_JOB_CHAIN_NODES)
@SequenceGenerator(
		name=DBLayer.TABLE_INVENTORY_JOB_CHAIN_NODES_SEQUENCE, 
		sequenceName=DBLayer.TABLE_INVENTORY_JOB_CHAIN_NODES_SEQUENCE,
		allocationSize=1)
public class DBItemInventoryJobChainNode extends DbItem implements Serializable{
	private static final long serialVersionUID = 1L;
	//wegen Oracle kann DEFAULT_JOB_NAME keinen Leerstring sein - Oracle/Hibernate macht Leerstring zum NULL
	private static final String DEFAULT_JOB_NAME = ".";
	
	/** Primary key */
	private Long id;

	/** Foreign key INVENTORY_INSTANCES.ID */
	private Long instanceId;
	/** Foreign key INVENTORY_JOB_CHAINS.ID */
	private Long jobChainId;
	/** Foreign key INVENTORY_JOBS.NAME */
	private String jobName;

	/** Others */
	private Long ordering;
	private String name;
	private String state;
	private String nextState;
	private String errorState;
	private String job;
	private Date created;
	private Date modified;
	
	public DBItemInventoryJobChainNode(){}
	
	/** Primary key */
    @Id
    @GeneratedValue(
        	strategy=GenerationType.AUTO,
        	generator=DBLayer.TABLE_INVENTORY_JOB_CHAIN_NODES_SEQUENCE)
    @Column(name="`ID`", nullable = false)
    public Long getId() {
        return this.id;
    }
    
    @Id
    @GeneratedValue(
        	strategy=GenerationType.AUTO,
        	generator=DBLayer.TABLE_INVENTORY_JOB_CHAIN_NODES_SEQUENCE)
    @Column(name="`ID`", nullable = false)
    public void setId(Long val) {
       this.id = val;
    }
    
    /** Foreign key INVENTORY_INSTANCES.ID */
    @Column(name="`INSTANCE_ID`", nullable = false)
    public Long getInstanceId() {
        return this.instanceId;
    }
    
    @Column(name="`INSTANCE_ID`", nullable = false)
    public void setInstanceId(Long val) {
       this.instanceId = val;
    }
    
    /** Foreign key INVENTORY_JOB_CHAINS.ID */
    @Column(name="`JOB_CHAIN_ID`", nullable = false)
    public Long getJobChainId() {
        return this.jobChainId;
    }
    
    @Column(name="`JOB_CHAIN_ID`", nullable = false)
    public void setJobChainId(Long val) {
       this.jobChainId = val;
    }
    
    /** Foreign key INVENTORY_JOBS.NAME */
    @Column(name = "`JOB_NAME`", nullable = false)
   	public void setJobName(String val) {
    	if(SOSString.isEmpty(val)){
    		val = DEFAULT_JOB_NAME;
    	}
   		this.jobName = val;
   	}

   	@Column(name = "`JOB_NAME`", nullable = false)
   	public String getJobName() {
   		return this.jobName;
   	}
    
    
    /** Others */
    @Column(name = "`ORDERING`", nullable = false)
   	public void setOrdering(Long val) {
   		this.ordering = val;
   	}

   	@Column(name = "`ORDERING`", nullable = false)
   	public Long getOrdering() {
   		return this.ordering;
   	}
   	
    @Column(name = "`STATE`", nullable = true)
	public void setState(String val) {
    	if(SOSString.isEmpty(val)){
    		val = null;
    	}
		this.state = val;
	}

	@Column(name = "`STATE`", nullable = true)
	public String getState() {
		return this.state;
	}

	@Column(name = "`NEXT_STATE`", nullable = true)
	public void setNextState(String val) {
	   	if(SOSString.isEmpty(val)){
    		val = null;
    	}

		this.nextState = val;
	}

	@Column(name = "`NEXT_STATE`", nullable = true)
	public String getNextState() {
		return this.nextState;
	}
	
	@Column(name = "`ERROR_STATE`", nullable = true)
	public void setErrorState(String val) {
	   	if(SOSString.isEmpty(val)){
    		val = null;
    	}

		this.errorState = val;
	}

	@Column(name = "`ERROR_STATE`", nullable = true)
	public String getErrorState() {
		return this.errorState;
	}
	
	@Column(name = "`JOB`", nullable = true)
	public void setJob(String val) {
	   	if(SOSString.isEmpty(val)){
    		val = null;
    	}

		this.job = val;
	}

	@Column(name = "`JOB`", nullable = true)
	public String getJob() {
		return this.job;
	}
	
	@Column(name = "`NAME`", nullable = false)
	public void setName(String val) {
		this.name = val;
	}

	@Column(name = "`NAME`", nullable = false)
	public String getName() {
		return this.name;
	}
		
	@Temporal (TemporalType.TIMESTAMP)
	@Column(name = "`CREATED`", nullable = false)
	public void setCreated(Date val) {
		this.created = val;
	}

	@Temporal (TemporalType.TIMESTAMP)
	@Column(name = "`CREATED`", nullable = false)
	public Date getCreated() {
		return this.created;
	}

	@Temporal (TemporalType.TIMESTAMP)
	@Column(name = "`MODIFIED`", nullable = false)
	public void setModified(Date val) {
		this.modified = val;
	}

	@Temporal (TemporalType.TIMESTAMP)
	@Column(name = "`MODIFIED`", nullable = false)
	public Date getModified() {
		return this.modified;
	}
}
