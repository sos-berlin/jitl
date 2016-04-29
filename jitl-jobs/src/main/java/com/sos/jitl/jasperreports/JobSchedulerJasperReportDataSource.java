package com.sos.jitl.jasperreports;

import java.util.HashMap;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import sos.connection.SOSConnection;
import sos.util.SOSLogger;

public class JobSchedulerJasperReportDataSource implements JRDataSource {

    private SOSConnection connection = null;
    private SOSLogger logger = null;
    private HashMap record = null;

    public JobSchedulerJasperReportDataSource() {
    }

    public JobSchedulerJasperReportDataSource(SOSConnection connection) {
        this.setConnection(connection);
    }

    public JobSchedulerJasperReportDataSource(SOSConnection connection, SOSLogger logger) {
        this.setConnection(connection);
        this.setLogger(logger);
    }

    public boolean next() throws JRException {
        try {
            this.record = this.getConnection().get();
            return record != null && !record.isEmpty();
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public Object getFieldValue(JRField field) throws JRException {
        try {
            String fieldName = field.getName().toLowerCase();
            if (record != null && !record.isEmpty() && record.containsKey(fieldName)) {
                return record.get(fieldName);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public SOSConnection getConnection() {
        return connection;
    }

    public void setConnection(SOSConnection connection) {
        this.connection = connection;
    }

    public SOSLogger getLogger() {
        return logger;
    }

    public void setLogger(SOSLogger logger) {
        this.logger = logger;
    }

}