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
@Table(name = DBLayer.TABLE_INVENTORY_INSTANCES)
@SequenceGenerator(
		name=DBLayer.TABLE_INVENTORY_INSTANCES_SEQUENCE, 
		sequenceName=DBLayer.TABLE_INVENTORY_INSTANCES_SEQUENCE,
		allocationSize=1)
public class DBItemInventoryInstance extends DbItem implements Serializable{
	private static final long serialVersionUID = 1L;
	
	/** Primary key */
	private Long id;

	/** Others */
	private String schedulerId;
	private String hostname;
	private Long port;
	private String liveDirectory;
	private Date created;
	private Date modified;
	
	public DBItemInventoryInstance(){}
	
	/** Primary key */
    @Id
    @GeneratedValue(
        	strategy=GenerationType.AUTO,
        	generator=DBLayer.TABLE_INVENTORY_INSTANCES_SEQUENCE)
    @Column(name="`ID`", nullable = false)
    public Long getId() {
        return this.id;
    }
    
    @Id
    @GeneratedValue(
        	strategy=GenerationType.AUTO,
        	generator=DBLayer.TABLE_INVENTORY_INSTANCES_SEQUENCE)
    @Column(name="`ID`", nullable = false)
    public void setId(Long val) {
       this.id = val;
    }
    
    /** Others */
	@Column(name = "`SCHEDULER_ID`", nullable = false)
	public void setSchedulerId(String val) {
		this.schedulerId = val;
	}

	@Column(name = "`SCHEDULER_ID`", nullable = false)
	public String getSchedulerId() {
		return this.schedulerId;
	}
	
	@Column(name = "`HOSTNAME`", nullable = false)
	public void setHostname(String val) {
		this.hostname = val;
	}

	@Column(name = "`HOSTNAME`", nullable = false)
	public String getHostname() {
		return this.hostname;
	}
	
	@Column(name="`PORT`", nullable = false)
    public Long getPort() {
        return this.port;
    }
    
    @Column(name="`PORT`", nullable = false)
    public void setPort(Long val) {
       this.port = val;
    }
    
    @Column(name = "`LIVE_DIRECTORY`", nullable = false)
	public void setLiveDirectory(String val) {
		this.liveDirectory = val;
	}

	@Column(name = "`LIVE_DIRECTORY`", nullable = false)
	public String getLiveDirectory() {
		return this.liveDirectory;
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
