package com.sos.jitl.runonce.job;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBItemInventoryOperatingSystem;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.runonce.data.ProcessDataUtil;

public class InsertOrUpdateInventoryInstanceEntriesJob extends JSJobUtilitiesClass<InsertOrUpdateInventoryInstanceEntriesOptions> {

    protected InsertOrUpdateInventoryInstanceEntriesOptions options = null;
    private static final Logger LOGGER = Logger.getLogger(InsertOrUpdateInventoryInstanceEntriesJob.class);
    private JSJobUtilities jsJobUtilities = this;
    private String answerXml;
    private SOSHibernateConnection connection;
    private String liveDirectory;
    private String supervisorHost;
    private String supervisorPort;
    private String proxyUrl;
    
    public InsertOrUpdateInventoryInstanceEntriesJob() {
        super(new InsertOrUpdateInventoryInstanceEntriesOptions());
    }

    public InsertOrUpdateInventoryInstanceEntriesOptions getOptions() {
        if (options == null) {
            options = new InsertOrUpdateInventoryInstanceEntriesOptions();
        }
        return options;
    }

    public InsertOrUpdateInventoryInstanceEntriesOptions getOptions(final InsertOrUpdateInventoryInstanceEntriesOptions entriesOptions) {
        options = entriesOptions;
        return options;
    }

    public void init() throws Exception {
        try {
            connection = new SOSHibernateConnection(getOptions().inventoryHibernateConfigurationFile.getValue());
            connection.setAutoCommit(getOptions().connectionAutocommit.value());
            connection.setTransactionIsolation(getOptions().connectionTransactionIsolation.value());
            connection.setIgnoreAutoCommitTransactions(true);
            connection.addClassMapping(DBLayer.getInventoryClassMapping());
            connection.connect();
        } catch (Exception ex) {
            throw new Exception(String.format("init connection: %s", ex.toString()));
        }
    }

    public void exit() {
        if (connection != null) {
            connection.disconnect();
        }
    }

    public InsertOrUpdateInventoryInstanceEntriesJob execute() throws Exception {
        try {
            getOptions().checkMandatory();
            ProcessDataUtil dataUtil = new ProcessDataUtil(getOptions().getSchedulerHibernateConfigurationFile().getValue(), connection);
            dataUtil.setLiveDirectory(liveDirectory);
            if(proxyUrl != null && !proxyUrl.isEmpty()) {
                dataUtil.setProxyUrl(proxyUrl);
            }
            dataUtil.setSupervisorHost(supervisorHost);
            dataUtil.setSupervisorPort(supervisorPort);
            DBItemInventoryInstance jsInstanceItem = dataUtil.getDataFromJobscheduler(answerXml);
            DBItemInventoryOperatingSystem osItem = dataUtil.getOsData(jsInstanceItem);
            dataUtil.insertOrUpdateDB(jsInstanceItem, osItem);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
        return this;
    }

    public void setJSJobUtilites(JSJobUtilities newJsJobUtilities) {
        if (newJsJobUtilities == null) {
            jsJobUtilities = this;
        } else {
            jsJobUtilities = newJsJobUtilities;
        }
        LOGGER.debug("objJSJobUtilities = " + jsJobUtilities.getClass().getName());
    }
    
    public void setAnswerXml(String answerXml) {
        this.answerXml = answerXml;
    }

    public void setLiveDirectory(String liveDirectory) {
        this.liveDirectory = liveDirectory;
    }
    
    public void setSupervisorHost(String supervisorHost) {
        this.supervisorHost = supervisorHost;
    }
    
    public void setSupervisorPort(String supervisorPort) {
        this.supervisorPort = supervisorPort;
    }
    
    public void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }

}