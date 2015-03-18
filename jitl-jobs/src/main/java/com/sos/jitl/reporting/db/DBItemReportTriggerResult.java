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

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = DBLayer.TABLE_REPORT_TRIGGER_RESULTS)
@SequenceGenerator(
		name=DBLayer.TABLE_REPORT_TRIGGER_RESULTS_SEQUENCE, 
		sequenceName=DBLayer.TABLE_REPORT_TRIGGER_RESULTS_SEQUENCE,
		allocationSize=1)
public class DBItemReportTriggerResult extends DbItem implements Serializable{
	private static final long serialVersionUID = 1L;
	
	/** Primary key */
	private Long id;

	
	/** Others */
	private String schedulerId;
	private Long historyId;
	private Long triggerId;
	
	private String startCase;
	private Long steps;
	private boolean error;
	private String errorCode;
	private String errorText;
		
	private Date created;
	private Date modified;
	
	
	public DBItemReportTriggerResult(){}
	
	/** Primary key */
    @Id
    @GeneratedValue(
        	strategy=GenerationType.AUTO,
        	generator=DBLayer.TABLE_REPORT_TRIGGER_RESULTS_SEQUENCE)
    @Column(name="`ID`", nullable = false)
    public Long getId() {
        return this.id;
    }
    
    @Id
    @GeneratedValue(
        	strategy=GenerationType.AUTO,
        	generator=DBLayer.TABLE_REPORT_TRIGGER_RESULTS_SEQUENCE)
    @Column(name="`ID`", nullable = false)
    public void setId(Long val) {
       this.id = val;
    }
   
    /** Others */
    @Column(name="`SCHEDULER_ID`", nullable = false)
    public String getSchedulerId() {
        return this.schedulerId;
    }
    
    @Column(name="`SCHEDULER_ID`", nullable = false)
    public void setSchedulerId(String val) {
       this.schedulerId = val;
    }
    
   @Column(name = "`HISTORY_ID`", nullable = false)
	public void setHistoryId(Long val) {
		this.historyId = val;
	}

	@Column(name = "`HISTORY_ID`", nullable = false)
	public Long getHistoryId() {
		return this.historyId;
	}
	
	@Column(name = "`TRIGGER_ID`", nullable = false)
	public void setTriggerId(Long val) {
		this.triggerId = val;
	}

	@Column(name = "`TRIGGER_ID`", nullable = false)
	public Long getTriggerId() {
		return this.triggerId;
	}
	
	@Column(name = "`START_CAUSE`", nullable = false)
	public void setStartCause(String val) {
		this.startCase = val;
	}

	@Column(name = "`START_CAUSE`", nullable = false)
	public String getStartCause() {
		return this.startCase;
	}
	
	@Column(name = "`STEPS`", nullable = false)
	public void setSteps(Long val) {
		this.steps = val;
	}

	@Column(name = "`STEPS`", nullable = false)
	public Long getSteps() {
		return this.steps;
	}
	
	@Column(name = "`ERROR`", nullable = false)
	@Type(type="numeric_boolean")
	public void setError(boolean val) {
		this.error = val;
	}

	@Column(name = "`ERROR`", nullable = false)
	@Type(type="numeric_boolean")
	public boolean getError() {
		return this.error;
	}
	
	@Column(name = "`ERROR_CODE`", nullable = true)
	public void setErrorCode(String val) {
		this.errorCode = val;
	}

	@Column(name = "`ERROR_CODE`", nullable = true)
	public String getErrorCode() {
		return this.errorCode;
	}
	
	@Column(name = "`ERROR_TEXT`", nullable = true)
	public void setErrorText(String val) {
		this.errorText = val;
	}

	@Column(name = "`ERROR_TEXT`", nullable = true)
	public String getErrorText() {
		return this.errorText;
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
