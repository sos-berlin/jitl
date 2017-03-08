package com.sos.jitl.reporting.model;

import java.util.Optional;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.reporting.db.DBLayerReporting;

public class ReportingModel {

    private DBLayerReporting dbLayer;

    public ReportingModel() {

    }

    public ReportingModel(SOSHibernateSession reportingSession) throws Exception {
        if (reportingSession == null) {
            throw new Exception("reportingSession is null");
        }
        dbLayer = new DBLayerReporting(reportingSession);
    }

    public DBLayerReporting getDbLayer() {
        return dbLayer;
    }

    public Optional<Integer> getFetchSize(int value) {
        return value == -1 ? Optional.empty() : Optional.of(value);
    }

    public void setReportingSession(SOSHibernateSession reportingSession) throws Exception {
        if (reportingSession == null) {
            throw new Exception("reportingSession is null");
        }
        dbLayer = new DBLayerReporting(reportingSession);
    }
}
