package com.sos.jitl.reporting.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Type;
import com.sos.hibernate.classes.DbItem;

@Entity
public class DBItemReportInventoryInfo extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private String title;
    private boolean isRuntimeDefined;
    
    public DBItemReportInventoryInfo() {
    }

    @Column(name = "`TITLE`", nullable = true)
    public void setTitle(String val) {
        this.title = val;
    }

    @Column(name = "`TITLE`", nullable = true)
    public String getTitle() {
        return this.title;
    }
    
    @Column(name = "`IS_RUNTIME_DEFINED`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setIsRuntimeDefined(boolean val) {
        this.isRuntimeDefined = val;
    }

    @Column(name = "`IS_RUNTIME_DEFINED`", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getIsRuntimeDefined() {
        return this.isRuntimeDefined;
    }
}
