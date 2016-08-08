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
import javax.persistence.Transient;

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = DBLayer.TABLE_INVENTORY_INSTANCES)
@SequenceGenerator(name = DBLayer.TABLE_INVENTORY_INSTANCES_SEQUENCE, sequenceName = DBLayer.TABLE_INVENTORY_INSTANCES_SEQUENCE, allocationSize = 1)
public class DBItemInventoryInstance extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    private Long id;

    /** Others */
    private String schedulerId;
    private String hostname;
    private Integer port;
    private String liveDirectory;
    private Date created;
    private Date modified;
    private Date startTime;
    private String url;
    private String jobSchedulerVersion;
    private Integer clusterMemberPrecedence;
    private String clusterMemberTypeSchema;
    private String supervisorId;
    private String timeZone;

    public DBItemInventoryInstance() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INVENTORY_INSTANCES_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INVENTORY_INSTANCES_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
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

    @Column(name = "`PORT`", nullable = false)
    public Integer getPort() {
        return this.port;
    }

    @Column(name = "`PORT`", nullable = false)
    public void setPort(Integer val) {
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

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public void setCreated(Date val) {
        this.created = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public Date getCreated() {
        return this.created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public void setModified(Date val) {
        this.modified = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public Date getModified() {
        return this.modified;
    }
    
    @Transient
    public Date getStartTime(){
        return new Date();
    }
    
    @Transient
    public String getUrl(){
        return "http://localhost:4444";
    }
    
    @Transient
    public String getJobSchedulerVersion(){
        return "1.11";
    }

    @Transient
    public Integer getClusterMemberPrecedence(){
        return 0;
    }

    @Transient
    public String getClusterMemberTypeSchema(){
        return "active";
    }

    @Transient
    public String getSupervisorId(){
        return "scheduler_current";
    }
    
    @Transient
    public String getTimeZone(){
        return "UTC";
    }
    
    
        
}
