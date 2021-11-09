package com.sos.jitl.restclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.IMonitor_impl;
import sos.spooler.Variable_set;

public class CreateApiAccessToken extends JobSchedulerJobAdapter implements IMonitor_impl {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateApiAccessToken.class);

    @Override
    public boolean spooler_process_before() throws Exception {

        LOGGER.debug("Starting spooler_process_before");
        try {
            JobSchedulerCredentialStoreJOCParameters options = new JobSchedulerCredentialStoreJOCParameters();
            Variable_set v = spooler.create_variable_set();
            v.merge(spooler_task.params());
            
            if (spooler_task.order() != null && spooler_task.order().params() != null) {
                v.merge(spooler_task.order().params());
            }
            
            
            options.setJocUrl(v.value("joc_url"));
            options.setCredentialStoreEntryPath(v.value("credential_store_entry_path"));
            options.setCredentialStoreFile(v.value("credential_store_file"));
            options.setCredentialStoreKeyFile(v.value("credential_store_key_file"));
            options.setCredentialStorePassword(v.value("credential_store_password"));
            options.setKeyStorePath(v.value("keystore_path"));
            options.setKeyStoreType(v.value("keystore_type"));
            options.setKeyStorePassword(v.value("keystore_password"));
            options.setKeyPassword(v.value("key_password"));
            options.setTrustStorePassword(v.value("truststore_password"));
            options.setTrustStorePath(v.value("truststore_path"));
            options.setTrustStoreType(v.value("truststore_type"));
            options.setUser(v.value("user"));
            options.setPassword(v.value("password"));
             
            AccessTokenProvider accessTokenProvider = new AccessTokenProvider(null);
            WebserviceCredentials w = accessTokenProvider.getAccessToken(spooler);
            spooler.variables().set_value("joc_user", w.getUser());

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return true;
    }

}
