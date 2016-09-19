package com.sos.jitl.reporting.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.ibm.icu.text.SimpleDateFormat;
import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = DBLayer.TABLE_INVENTORY_OPERATING_SYSTEM)
public class DBItemInventoryOperatingSystem extends DbItem implements Serializable {

    private static final long serialVersionUID = 6639624402069204129L;

    /** Primary Key */
    private Long id;
    
    /** Unique Index */
    private String hostname;
    
    /** Others */
    private String name;
    private String architecture;
    private String distribution;
    private Date created;
    private Date modified;
    
    /** Primary key */
    @Transient
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return id;
    }
    
    /** Primary key */
    @Transient
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long id) {
        this.id = id;
    }
    
    /** Unique Index */
    @Transient
    @Column(name = "`HOSTNAME`", nullable = false)
    public String getHostname() {
        return hostname;
    }
    
    /** Unique Index */
    @Transient
    @Column(name = "`HOSTNAME`", nullable = false)
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    
    @Transient
    @Column(name = "`NAME`", nullable = true)
    public String getName() {
        return name;
    }
    
    @Transient
    @Column(name = "`NAME`", nullable = true)
    public void setName(String name) {
        this.name = name;
    }
    
    @Transient
    @Column(name = "`ARCHITECTURE`", nullable = true)
    public String getArchitecture() {
        return architecture;
    }
    
    @Transient
    @Column(name = "`ARCHITECTURE`", nullable = true)
    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }
    
    @Transient
    @Column(name = "`DISTRIBUTION`", nullable = true)
    public String getDistribution() {
        return distribution;
    }
    
    @Transient
    @Column(name = "`DISTRIBUTION`", nullable = true)
    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }
    
    @Transient
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public Date getCreated() {
        return created;
    }
    
    @Transient
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public void setCreated(Date created) {
        this.created = created;
    }
    
    @Transient
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public Date getModified() {
        return modified;
    }
    
    @Transient
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public void setModified(Date modified) {
        this.modified = modified;
    }
    
    public String toDebugString() {
        StringBuilder strb = new StringBuilder();
        strb.append("ID:").append(getId()).append("|");
        strb.append("HOSTNAME:").append(getHostname()).append("|");
        strb.append("NAME:").append(getName()).append("|");
        strb.append("ARCHITECTURE:").append(getArchitecture()).append("|");
        strb.append("DISTRIBUTION:").append(getDistribution()).append("|");
        strb.append("CREATED:").append(getCreated()).append("|");
        strb.append("MODIFIED:").append(getModified());
        return strb.toString();
    }
    
}