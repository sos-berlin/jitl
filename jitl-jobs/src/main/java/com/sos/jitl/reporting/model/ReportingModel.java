package com.sos.jitl.reporting.model;

import java.util.Optional;

import com.sos.hibernate.classes.SOSHibernateStatelessSession;
import com.sos.jitl.reporting.db.DBLayerReporting;

public class ReportingModel {

    private DBLayerReporting dbLayer;

    public ReportingModel(){
    	
    }
    
    public ReportingModel(SOSHibernateStatelessSession reportingConn) throws Exception {
        if (reportingConn == null) {
            throw new Exception("reportingConn is null");
        }
        dbLayer = new DBLayerReporting(reportingConn);
    }

    public DBLayerReporting getDbLayer() {
        return dbLayer;
    }
    
    public Optional<Integer> getFetchSize(int value){
    	return value == -1 ? Optional.empty() : Optional.of(value);
    }
    
    public void setReportingConnection(SOSHibernateStatelessSession reportingConn) throws Exception{
    	if (reportingConn == null) {
            throw new Exception("reportingConn is null");
        }
        dbLayer = new DBLayerReporting(reportingConn);
    }
}
