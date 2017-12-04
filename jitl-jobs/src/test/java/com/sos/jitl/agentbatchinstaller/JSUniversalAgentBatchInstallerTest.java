package com.sos.jitl.agentbatchinstaller;

import static org.junit.Assert.*;

import org.junit.Test;

public class JSUniversalAgentBatchInstallerTest {

    @Test
    public void execteTest() throws Exception {
        JSUniversalAgentBatchInstaller jsUniversalAgentBatchInstaller = new JSUniversalAgentBatchInstaller();
        JSUniversalAgentBatchInstallerOptions jsUniversalAgentBatchInstallerOptions = jsUniversalAgentBatchInstaller.options();
        jsUniversalAgentBatchInstallerOptions.installation_definition_file.setValue("C:/Users/ur/Documents/sos-berlin.com/jobscheduler/scheduler_joc_cockpit/config/batch_install_acc/batch_installer_config_file.xml");
        jsUniversalAgentBatchInstallerOptions.installation_job_chain.setValue("/batch_install_universal_agent/universal_agent_installer");
        jsUniversalAgentBatchInstallerOptions.update.value(true);
       
        jsUniversalAgentBatchInstallerOptions.checkMandatory();

        jsUniversalAgentBatchInstaller.execute();    }

}
