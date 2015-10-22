package  com.sos.jitl.checkrunhistory;
 
import sos.scheduler.job.JobSchedulerJobAdapter;
 
public class JobSchedulerCheckRunHistoryJSAdapterClass extends JobSchedulerJobAdapter  {

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
		finally {
		} 
		return (spooler_task.job().order_queue() != null);
	} // spooler_process

	@Override
	public void spooler_exit() {
		super.spooler_exit();
	}

	private void doProcessing() throws Exception {
		JobSchedulerCheckRunHistory jobSchedulerCheckRunHistory = new JobSchedulerCheckRunHistory();
		JobSchedulerCheckRunHistoryOptions jobSchedulerCheckRunHistoryOptions = jobSchedulerCheckRunHistory.options();
		jobSchedulerCheckRunHistoryOptions.CurrentNodeName(getCurrentNodeName());

		jobSchedulerCheckRunHistoryOptions.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
		jobSchedulerCheckRunHistory.setJSJobUtilites(this);
		jobSchedulerCheckRunHistory.setJSCommands(this);
		jobSchedulerCheckRunHistory.Execute();
		if (this.isOrderJob()){
    		spooler_task.order().params().set_var("result",jobSchedulerCheckRunHistoryOptions.result.Value());
		}else{
    		spooler_task.params().set_var("result",jobSchedulerCheckRunHistoryOptions.result.Value());
		}
	} // doProcessing
}
