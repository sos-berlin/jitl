package com.sos.jitl.reporting.model;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBLayerReporting;

public class ReportingModel {

    private DBLayerReporting dbLayer;

    public ReportingModel(SOSHibernateConnection reportingConn) throws Exception {
        if (reportingConn == null) {
            throw new Exception("reporingConn is null");
        }
        dbLayer = new DBLayerReporting(reportingConn);
    }

    public DBLayerReporting getDbLayer() {
        return dbLayer;
    }
}
