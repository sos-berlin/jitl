package com.sos.jitl.inventory.db;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.sos.jitl.reporting.db.DBItemCalendar;
import com.sos.jitl.reporting.db.DBItemInventoryClusterCalendar;

public class ClusterCalendar {
    
    private DBItemInventoryClusterCalendar dbItem = null;
    private Long newId = null;
    private Long oldId = null;

    public ClusterCalendar(DBItemCalendar calendar, String schedulerId) {
        dbItem = new DBItemInventoryClusterCalendar();
        dbItem.setBaseName(calendar.getBaseName());
        dbItem.setCategory(calendar.getCategory());
        dbItem.setConfiguration(calendar.getConfiguration());
        dbItem.setCreated(calendar.getCreated());
        dbItem.setDirectory(calendar.getDirectory());
        dbItem.setId(0L);
        //dbItem.setId(null);
        dbItem.setModified(calendar.getModified());
        dbItem.setName(calendar.getName());
        dbItem.setSchedulerId(schedulerId);
        dbItem.setTitle(calendar.getTitle());
        dbItem.setType(calendar.getType());
        oldId = calendar.getId();
    }
    
    public DBItemInventoryClusterCalendar get() {
        return dbItem;
    }

    public void setNewId(Long id) {
        newId = id;
    }
    
    public Long getNewId() {
        return newId;
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
        if (!(other instanceof ClusterCalendar)) {
            return false;
        }
        ClusterCalendar rhs = ((ClusterCalendar) other);
        return new EqualsBuilder().append(dbItem, rhs.dbItem).append(dbItem, rhs.dbItem).isEquals();
    }

}
