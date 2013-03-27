package com.sos.jitl.splitter;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Order;
import sos.spooler.Variable_set;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.jitl.sync.SyncNodeList;

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
	@SuppressWarnings("hiding")
	private static Logger	logger			= Logger.getLogger(JobChainSplitterJSAdapterClass.class);
	private final String	conSVNVersion	= "$Id: JSEventsClient.java 18220 2012-10-18 07:46:10Z kb $";

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
		boolean flgCreateSyncParameter = false;
		boolean flgOverwriteParameters = true;

		if (isOrderJob() == true) {
			logger.info(conSVNVersion);
			JobChainSplitterOptions objO = new JobChainSplitterOptions();

			// TODO make this class available as a monitor class as well
			objO.CurrentNodeName(this.getCurrentNodeName());
			objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
			logger.info(objO.dirtyString());
			objO.CheckMandatory();

//			Order objOrderCurrent = super.getOrder();
			Order objOrderCurrent = spooler_task.order();
			Variable_set objOrderParams = objOrderCurrent.params();
			String strSyncStateName = objO.SyncStateName.Value();
			String strJobChainName = objOrderCurrent.job_chain().name();

			// TODO resolve problem with upper-/lower-case
			for (String strCurrentState : objO.StateNames.getValueList()) {
				if (objOrderCurrent.job_chain().node(strCurrentState) == null) {
					throw new JobSchedulerException(String.format("State '%1$s' in chain '%2$s' not found but mandatory", strCurrentState, strJobChainName));
				}
			}

			int lngNoOfParallelSteps = objO.StateNames.getValueList().length;
			flgCreateSyncParameter = true;
			if (flgCreateSyncParameter == true) {
//				String strSyncParam = strSyncStateName + "/" + strJobChainName + ";" + strSyncStateName + SyncNodeList.CONST_PARAM_PART_REQUIRED_ORDERS;
				String strSyncParam = strJobChainName + SyncNodeList.CHAIN_ORDER_DELIMITER + strSyncStateName + SyncNodeList.CONST_PARAM_PART_REQUIRED_ORDERS;
				objOrderParams.set_var(strSyncParam, Integer.toString(lngNoOfParallelSteps + 1));
				// TODO use global constant
				objOrderParams.set_var("sync_session_id", strJobChainName + "_" + strSyncStateName + "_" + objOrderCurrent.id());
			}

			for (String strCurrentState : objO.StateNames.getValueList()) {
				Order objOrderClone = spooler.create_order();
				objOrderClone.set_state(strCurrentState);
				objOrderClone.set_title(objOrderCurrent.title() + ": " + strCurrentState);
				objOrderClone.set_end_state(strSyncStateName);
				objOrderClone.params().merge(objOrderParams);
				String strOrderCloneName = objOrderCurrent.id() + "_" + strCurrentState;
				objOrderClone.set_id(strOrderCloneName);
				// TODO Parameter start_at  "when_in_period"
				objOrderClone.set_at("now");
				objOrderCurrent.job_chain().add_or_replace_order(objOrderClone);
				logger.info(String.format("Order '%1$s' created and started", strOrderCloneName));
				logger.debug(objOrderClone.xml());
			}
		}
		else {
			throw new JobSchedulerException("This Job can run as an job in a jobchain only");
		}
	} // doProcessing
}
