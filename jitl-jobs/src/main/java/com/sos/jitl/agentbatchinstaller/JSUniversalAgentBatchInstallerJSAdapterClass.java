package com.sos.jitl.agentbatchinstaller;

import java.util.HashMap;

import sos.scheduler.job.JobSchedulerJobAdapter;

public class JSUniversalAgentBatchInstallerJSAdapterClass extends JobSchedulerJobAdapter {

    @Override
    public boolean spooler_init() {
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() throws Exception {
        super.spooler_process();
        doProcessing();
        return spooler_task.job().order_queue() != null;
    } // spooler_process

    @Override
    public void spooler_exit() {
        super.spooler_exit();
    }

    private void doProcessing() throws Exception {
        JSUniversalAgentBatchInstaller jsUniversalAgentBatchInstaller = new JSUniversalAgentBatchInstaller();
        JSUniversalAgentBatchInstallerOptions jsUniversalAgentBatchInstallerOptions = jsUniversalAgentBatchInstaller.options();
        HashMap<String, String> parameters = getParameters();

        jsUniversalAgentBatchInstallerOptions.setAllOptions(getSchedulerParameterAsProperties(parameters));
        jsUniversalAgentBatchInstallerOptions.checkMandatory();
        jsUniversalAgentBatchInstaller.setJSJobUtilites(this);
        jsUniversalAgentBatchInstaller.setJSCommands(this);

        jsUniversalAgentBatchInstaller.execute();
    } // doProcessing
}
