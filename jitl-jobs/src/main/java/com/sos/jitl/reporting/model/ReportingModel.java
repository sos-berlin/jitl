package com.sos.jitl.reporting.model;

import java.util.Optional;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBLayerReporting;

public class ReportingModel {

    private DBLayerReporting dbLayer;

    public ReportingModel(SOSHibernateConnection reportingConn, Optional<String> fetchSize) throws Exception {
        if (reportingConn == null) {
            throw new Exception("reporingConn is null");
        }
        dbLayer = new DBLayerReporting(reportingConn, fetchSize);
    }

    public DBLayerReporting getDbLayer() {
        return dbLayer;
    }
}
