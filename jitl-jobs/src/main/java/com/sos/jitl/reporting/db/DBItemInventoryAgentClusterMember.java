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

import com.sos.hibernate.classes.DbItem;


@Entity
@Table(name = DBLayer.TABLE_INVENTORY_AGENT_CLUSTERMEMBERS)
public class DBItemInventoryAgentClusterMember extends DbItem implements Serializable {

    private static final long serialVersionUID = 8059333159913852093L;

    /** Primary Key */
    private Long id;

    /** Foreign Key INVENTORY_INSTANCES.ID */
    private Long instanceId;
    /** Foreign Key INVENTORY_AGENT_CLUSTER.ID */
    private Long agentClusterId;
    /** Foreign Key INVENTORY_AGENT_INSTANCES.ID */
    private Long agentInstanceId;
    /** Foreign Key INVENTORY_AGENT_INSTANCES.URL */
    private String url;

    /** Others */
    private Integer ordering;
    private Date created;
    private Date modified;
    
    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return id;
    }
    
    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long id) {
        this.id = id;
    }
    
    /** Foreign Key */
    @Column(name = "`INSTANCE_ID`", nullable = false)
    public Long getInstanceId() {
        return instanceId;
    }
    
    /** Foreign Key */
    @Column(name = "`INSTANCE_ID`", nullable = false)
    public void setInstanceId(Long instanceId) {
        if (instanceId == null) {
            instanceId = DBLayer.DEFAULT_ID;
        }
        this.instanceId = instanceId;
    }
    
    /** Foreign Key */
    @Column(name = "`AGENT_CLUSTER_ID`", nullable = false)
    public Long getAgentClusterId() {
        return agentClusterId;
    }
    
    /** Foreign Key */
    @Column(name = "`AGENT_CLUSTER_ID`", nullable = false)
    public void setAgentClusterId(Long agentClusterId) {
        if (agentClusterId == null) {
            agentClusterId = DBLayer.DEFAULT_ID;
        }
        this.agentClusterId = agentClusterId;
    }
    
    /** Foreign Key */
    @Column(name = "`AGENT_INSTANCE_ID`", nullable = false)
    public Long getAgentInstanceId() {
        return agentInstanceId;
    }
    
    /** Foreign Key */
    @Column(name = "`AGENT_INSTANCE_ID`", nullable = false)
    public void setAgentInstanceId(Long agentInstanceId) {
        if (agentInstanceId == null) {
            agentInstanceId = DBLayer.DEFAULT_ID;
        }
        this.agentInstanceId = agentInstanceId;
    }
    
    /** Foreign Key */
    @Column(name = "`URL`", nullable = false)
    public String getUrl() {
        return url;
    }
    
    /** Foreign Key */
    @Column(name = "`URL`", nullable = false)
    public void setUrl(String url) {
        if (url == null || url.isEmpty()) {
            url = DBLayer.DEFAULT_NAME;
        }
        this.url = url;
    }
    
    @Column(name = "`ORDERING`", nullable = false)
    public Integer getOrdering() {
        return ordering;
    }
    
    @Column(name = "`ORDERING`", nullable = false)
    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public Date getCreated() {
        return created;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public void setCreated(Date created) {
        this.created = created;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public Date getModified() {
        return modified;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public void setModified(Date modified) {
        this.modified = modified;
    }
    
}