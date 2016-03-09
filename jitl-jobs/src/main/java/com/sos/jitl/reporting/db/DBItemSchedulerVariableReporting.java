package com.sos.jitl.reporting.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = DBLayer.TABLE_SCHEDULER_VARIABLES)
public class DBItemSchedulerVariableReporting extends DbItem implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private String name;
    private Long numericValue;
    private String textValue;

    public DBItemSchedulerVariableReporting() {
    }

    @Id
    @Column(name = "`NAME`", nullable = false)
    public String getName() {
        return this.name;
    }

    public void setName(String val) {
        this.name = val;
    }

    @Column(name = "`WERT`", nullable = true)
    public void setNumericValue(Long val) {
        this.numericValue = val;
    }

    @Column(name = "`WERT`", nullable = true)
    public Long getNumericValue() {
        return this.numericValue;
    }

    @Column(name = "`TEXTWERT`", nullable = true)
    public void setTextValue(String val) {
        this.textValue = val;
    }

    @Column(name = "`TEXTWERT`", nullable = true)
    public String getTextValue() {
        return this.textValue;
    }

}
