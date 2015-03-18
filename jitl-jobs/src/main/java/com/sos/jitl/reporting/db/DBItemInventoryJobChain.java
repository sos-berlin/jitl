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
@Table(name = DBLayer.TABLE_INVENTORY_JOB_CHAINS)
@SequenceGenerator(
		name=DBLayer.TABLE_INVENTORY_JOB_CHAINS_SEQUENCE, 
		sequenceName=DBLayer.TABLE_INVENTORY_JOB_CHAINS_SEQUENCE,
		allocationSize=1)
public class DBItemInventoryJobChain extends DbItem implements Serializable{
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
	
	public DBItemInventoryJobChain(){}
	
	/** Primary key */
    @Id
    @GeneratedValue(
        	strategy=GenerationType.AUTO,
        	generator=DBLayer.TABLE_INVENTORY_JOB_CHAINS_SEQUENCE)
    @Column(name="`ID`", nullable = false)
    public Long getId() {
        return this.id;
    }
    
    @Id
    @GeneratedValue(
        	strategy=GenerationType.AUTO,
        	generator=DBLayer.TABLE_INVENTORY_JOB_CHAINS_SEQUENCE)
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
    
    /** Foreign key INVENTORY_FILES.ID */
    @Column(name="`FILE_ID`", nullable = false)
    public Long getFileId() {
        return this.fileId;
    }
    
    @Column(name="`FILE_ID`", nullable = false)
    public void setFileId(Long val) {
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
		if(SOSString.isEmpty(val)){
			val = null;
		}
		else{
			if(val.length() > TITLE_MAX_LENGTH){
				val = val.substring(0,TITLE_MAX_LENGTH);
			}
		}
		this.title = val;
	}

	@Column(name = "`TITLE`", nullable = true)
	public String getTitle() {
		return this.title;
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
