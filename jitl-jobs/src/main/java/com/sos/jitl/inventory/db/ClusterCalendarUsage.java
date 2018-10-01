package com.sos.jitl.inventory.db;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.sos.jitl.reporting.db.DBItemInventoryCalendarUsage;
import com.sos.jitl.reporting.db.DBItemInventoryClusterCalendarUsage;

public class ClusterCalendarUsage {
    
    private DBItemInventoryClusterCalendarUsage dbItem = null;
    private Long oldId = null;

    public ClusterCalendarUsage(DBItemInventoryCalendarUsage usage, String schedulerId) {
        dbItem = new DBItemInventoryClusterCalendarUsage();
        dbItem.setCalendarId(usage.getCalendarId());
        dbItem.setConfiguration(usage.getConfiguration());
        dbItem.setCreated(usage.getCreated());
        dbItem.setEdited(usage.getEdited());
        dbItem.setId(0L);
        //dbItem.setId(null);
        dbItem.setModified(usage.getModified());
        dbItem.setObjectType(usage.getObjectType());
        dbItem.setPath(usage.getPath());
        dbItem.setSchedulerId(schedulerId);
        oldId = usage.getId();
    }
    
    public DBItemInventoryClusterCalendarUsage get() {
        return dbItem;
    }
    
    public Long getOldId() {
        return oldId;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dbItem).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ClusterCalendarUsage)) {
            return false;
        }
        ClusterCalendarUsage rhs = ((ClusterCalendarUsage) other);
        return new EqualsBuilder().append(dbItem, rhs.dbItem).append(dbItem, rhs.dbItem).isEquals();
    }
    
}
