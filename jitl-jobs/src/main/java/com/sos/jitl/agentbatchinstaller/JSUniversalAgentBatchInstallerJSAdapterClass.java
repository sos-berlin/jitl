package com.sos.jitl.agentbatchinstaller;
   
import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Variable_set;
 
public class JSUniversalAgentBatchInstallerJSAdapterClass extends JobSchedulerJobAdapter {
 
	@Override
	public boolean spooler_init() {
		return super.spooler_init();
	}

	@Override
	public boolean spooler_process() throws Exception {
		try {
			super.spooler_process();
			doProcessing();
		}
		catch (Exception e) {
			return false;
		}
	  
		return this.isOrderJob();
	} // spooler_process

	@Override
	public void spooler_exit() {	 
		super.spooler_exit();
	}

	private void doProcessing() throws Exception {
		JSUniversalAgentBatchInstaller jsUniversalAgentBatchInstaller = new JSUniversalAgentBatchInstaller();
		JSUniversalAgentBatchInstallerOptions jsUniversalAgentBatchInstallerOptions = jsUniversalAgentBatchInstaller.options();
 		Variable_set parameters = getParameters();

 		jsUniversalAgentBatchInstallerOptions.setAllOptions(getSchedulerParameterAsProperties(parameters));
		jsUniversalAgentBatchInstallerOptions.CheckMandatory();
		jsUniversalAgentBatchInstaller.setJSJobUtilites(this);
 		jsUniversalAgentBatchInstaller.setJSCommands(this);

		jsUniversalAgentBatchInstaller.execute();
	} // doProcessing
}
