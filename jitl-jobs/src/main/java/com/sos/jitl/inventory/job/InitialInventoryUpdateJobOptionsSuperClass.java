package com.sos.jitl.inventory.job;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionUrl;

@JSOptionClass(name = "InsertOrUpdateInventoryInstanceEntriesOptionsSuperClass",
    description = "InsertOrUpdateInventoryInstanceEntriesOptionsSuperClass")
public class InitialInventoryUpdateJobOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = -3912396349868169557L;

    @JSOptionDefinition(name = "inventory_hibernate_configuration_file", description = "",
            key = "inventory_hibernate_configuration_file", type = "SOSOptionString", mandatory = true)
    public SOSOptionString inventoryHibernateConfigurationFile = new SOSOptionString(this,
            "inventory_hibernate_configuration_file", "", "config/hibernate.cfg.xml", "config/hibernate.cfg.xml", true);

    public SOSOptionString getInventoryHibernateConfigurationFile() {
        return inventoryHibernateConfigurationFile;
    }

    public void setInventoryHibernateConfigurationFile(SOSOptionString hibernateConfigurationFile) {
        this.inventoryHibernateConfigurationFile = hibernateConfigurationFile;
    }

    @JSOptionDefinition(name = "scheduler_hibernate_configuration_file", description = "",
            key = "scheduler_hibernate_configuration_file", type = "SOSOptionString", mandatory = false)
    public SOSOptionString schedulerHibernateConfigurationFile = new SOSOptionString(this,
            "scheduler_hibernate_configuration_file", "", "config/hibernate.cfg.xml", "config/hibernate.cfg.xml", false);

    public SOSOptionString getSchedulerHibernateConfigurationFile() {
        return schedulerHibernateConfigurationFile;
    }
    
    public void setSchedulerHibernateConfigurationFile(SOSOptionString hibernateConfigurationFile) {
        this.schedulerHibernateConfigurationFile = hibernateConfigurationFile;
    }

    @JSOptionDefinition(name = "connection_transaction_isolation", description = "", key = "connection_transaction_isolation",
            type = "SOSOptionInterval", mandatory = false)
    public SOSOptionInteger connectionTransactionIsolation = new SOSOptionInteger(this, ".connection_transaction_isolation", "",
            "2", "2", false);

    public SOSOptionInteger getConnectionTransactionIsolation() {
        return connectionTransactionIsolation;
    }

    public void setConnectionTransactionIsolation(SOSOptionInteger connectionTransactionIsolation) {
        this.connectionTransactionIsolation = connectionTransactionIsolation;
    }

    @JSOptionDefinition(name = "connection_autocommit", description = "", key = "connection_autocommit", type = "SOSOptionBoolean",
            mandatory = false)
    public SOSOptionBoolean connectionAutocommit = new SOSOptionBoolean(this, "connection_autocommit", "", "false", "false", false);

    public SOSOptionBoolean getConnectionAutocommit() {
        return connectionAutocommit;
    }

    public void setConnectionAutocommit(SOSOptionBoolean connectionAutocommit) {
        this.connectionAutocommit = connectionAutocommit;
    }

    @JSOptionDefinition(name = "large_result_fetch_size", description = "", key = "large_result_fetch_size", type = "SOSOptionInteger",
            mandatory = false)
    public SOSOptionInteger largeResultFetchSize = new SOSOptionInteger(this, "large_result_fetch_size", "", "-1", "-1", false);

    public SOSOptionInteger getLargeResultFetchSize() {
        return largeResultFetchSize;
    }

    public void setLargeResultFetchSize(SOSOptionInteger largeResultFetchSize) {
        this.largeResultFetchSize = largeResultFetchSize;
    }
    
    @JSOptionDefinition(name = "proxy_url", description = "", key = "proxy_url", type = "SOSOptionUrl", mandatory = false)
    public SOSOptionUrl proxyUrl = new SOSOptionUrl(this, "proxy_url", "", "", "", false);

    public SOSOptionUrl getProxyUrl() {
        return proxyUrl;
    }

    public void setProxyUrl(SOSOptionUrl proxyUrl) {
        this.proxyUrl = proxyUrl;
    }
    
    public InitialInventoryUpdateJobOptionsSuperClass() {
        objParentClass = this.getClass();
    }
    
    public InitialInventoryUpdateJobOptionsSuperClass(JSListener jsListener) {
        this();
        this.registerMessageListener(jsListener);
    }

    public InitialInventoryUpdateJobOptionsSuperClass(HashMap<String, String> jsSettings) throws Exception {
        this();
        this.setAllOptions(jsSettings);
    }

    @Override
    public void checkMandatory() throws JSExceptionMandatoryOptionMissing, Exception {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.getMessage());
        }
    }

    @Override
    public void commandLineArgs(String[] args) {
        super.commandLineArgs(args);
        this.setAllOptions(super.objSettings);
    }

}