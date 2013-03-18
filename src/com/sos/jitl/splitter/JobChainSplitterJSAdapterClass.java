package com.sos.jitl.splitter;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Order;
import sos.spooler.Variable_set;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

// Super-Class for JobScheduler Java-API-Jobs

/**
 * \class 		JobChainSplitterJSAdapterClass - JobScheduler Adapter for "Start a parallel processing in a jobchain"
 *
 * \brief AdapterClass of JobChainSplitter for the SOSJobScheduler
 *
 * This Class JobChainSplitterJSAdapterClass works as an adapter-class between the SOS
 * JobScheduler and the worker-class JobChainSplitter.
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-121986169113382203.html for more details.
 *
 * \verbatim ;
 * mechanicaly created by C:\ProgramData\sos-berlin.com\jobscheduler\latestscheduler_4446\config\JOETemplates\java\xsl\JSJobDoc2JSAdapterClass.xsl from http://www.sos-berlin.com at 20130315155436
 * \endverbatim
 */
public class JobChainSplitterJSAdapterClass extends JobSchedulerJobAdapter {
	private final String	conClassName	= "JobChainSplitterJSAdapterClass";
	private static Logger	logger			= Logger.getLogger(JobChainSplitterJSAdapterClass.class);
	private final String					conSVNVersion	= "$Id: JSEventsClient.java 18220 2012-10-18 07:46:10Z kb $";

	public void init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init";
		doInitialize();
	}

	private void doInitialize() {
	} // doInitialize

	@Override
	public boolean spooler_init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_init";
		return super.spooler_init();
	}

	@Override
	public boolean spooler_process() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_process";

		try {
			super.spooler_process();
			doProcessing();
		}
		catch (Exception e) {
			throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
		}
		finally {
		} // finally
		return signalSuccess();

	} // spooler_process

	@Override
	public void spooler_exit() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_exit";
		super.spooler_exit();
	}

	private void doProcessing() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::doProcessing";

		if (isOrderJob() == true) {
			logger.info(conSVNVersion);
			JobChainSplitterOptions objO = new JobChainSplitterOptions();

			objO.CurrentNodeName(this.getCurrentNodeName());
			objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
			logger.info(objO.dirtyString());
			objO.CheckMandatory();

//			Order objOrderCurrent = super.getOrder();
			Order objOrderCurrent = spooler_task.order();
			Variable_set objOrderParams = objOrderCurrent.params();

			for (String strCurrentState : objO.StateNames.getValueList()) {
				Order objOrderClone = spooler.create_order();
				objOrderClone.set_state(strCurrentState);
				objOrderClone.set_title(objOrderCurrent.title());
				objOrderClone.set_end_state(objO.SyncStateName.Value());
				objOrderClone.params().merge(objOrderParams);
				String strOrderCloneName = objOrderCurrent.id() + "_" + strCurrentState;
				objOrderClone.set_id(strOrderCloneName);
				objOrderCurrent.job_chain().add_or_replace_order(objOrderClone);
				logger.info(String.format("Order %1$s created and started", strOrderCloneName));
			}
		}
		else {
			throw new JobSchedulerException("This Job can run as an job in a jobchain only");
		}
	} // doProcessing
}
