package com.sos.jitl.splitter;

import static com.sos.scheduler.messages.JSMessages.JSJ_I_0010;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_0020;

import java.io.ByteArrayInputStream;
 
import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Job;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.xml.SOSXMLXPath;

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
	private static final String PARAMETER_SYNC_SESSION_ID = "sync_session_id";
    private static final String PARAMETER_JOB_CHAIN_STATE2SYNCHRONIZE = "job_chain_state2synchronize";
    private static final String PARAMETER_JOB_CHAIN_NAME2SYNCHRONIZE = "job_chain_name2synchronize";
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
//		boolean flgOverwriteParameters = true;

		if (isOrderJob() == true) {
			logger.info(conSVNVersion);
			JobChainSplitterOptions objSplitterOptions = new JobChainSplitterOptions();

			// TODO make this class available as a monitor class as well
			objSplitterOptions.CurrentNodeName(this.getCurrentNodeName());
			objSplitterOptions.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
			logger.info(objSplitterOptions.dirtyString());
			objSplitterOptions.CheckMandatory();

//			Order objOrderCurrent = super.getOrder();
			Order objOrderCurrent = spooler_task.order();
			Variable_set objOrderParams = objOrderCurrent.params();
			String strSyncStateName = objSplitterOptions.SyncStateName.Value();
			if (strSyncStateName.length() <= 0) {
				/**
				 * If the SyncStateName is not specified the name of the next_state is used for the sync state
				 */
				strSyncStateName = objOrderCurrent.job_chain_node().next_state();
			}
			logger.debug(String.format("SyncStateName = '%1$s'", strSyncStateName));
			String strJobChainName = objOrderCurrent.job_chain().name();

					// <show_job_chain job_chain="name" what="source" max_orders="0" max_order_history="0"/>
					/**
					 * Answer:
					 *  ...
						<source>
							<job_chain orders_recoverable="yes" visible="yes" title="ParallelChain" max_orders="30">
								<job_chain_node state="JobChainStart" job="JobChainStart" next_state="A" error_state="A"/>
								<job_chain_node state="A" job="ParallelExample" next_state="split_B" error_state="!A"/>
								<job_chain_node state="split_B" job="JobChainSplitter1" next_state="sync_B" error_state="!split_B"/>
								<job_chain_node state="C" job="ParallelExample" next_state="C1" error_state="!C"/>
								<job_chain_node state="C1" job="ParallelExample" next_state="sync_B" error_state="!C1"/>
								<job_chain_node state="D" job="ParallelExample" next_state="sync_B" error_state="!D"/>
								<job_chain_node state="E" job="ParallelExample" next_state="sync_B" error_state="!E"/>
								<job_chain_node state="G" job="ParallelExample" next_state="sync_B" error_state="!finished"/>
								<job_chain_node state="H" job="ParallelExample" next_state="sync_B" error_state="!finished"/>
								<job_chain_node state="I" job="ParallelExample" next_state="sync_B" error_state="!finished"/>
								<job_chain_node state="J" job="ParallelExample" next_state="sync_B" error_state="!finished"/>
								<job_chain_node state="K" job="ParallelExample" next_state="sync_B" error_state="!finished"/>
								<job_chain_node state="L" job="ParallelExample" next_state="sync_B" error_state="!finished"/>
								<job_chain_node state="M" job="ParallelExample" next_state="sync_B" error_state="!finished"/>
								<job_chain_node state="N" job="ParallelExample" next_state="sync_B" error_state="!finished"/>
								<job_chain_node state="sync_B" job="SyncJob2" next_state="F" error_state="!sync_B"/>
								<job_chain_node state="F" job="ParallelExample" next_state="JobChainEnd" error_state="!F"/>
								<job_chain_node state="JobChainEnd" job="JobChainEnd" next_state="#finished" error_state="!finished"/>
								<job_chain_node state="!A"/>
								<job_chain_node state="!split_B"/>
								<job_chain_node state="!C"/>
								<job_chain_node state="!D"/>
								<job_chain_node state="!E"/>
								<job_chain_node state="!sync_B"/>
								<job_chain_node state="!F"/>
								<job_chain_node state="!C1"/>
								<job_chain_node state="#finished"/>
								<job_chain_node state="!finished"/>
							</job_chain>
						</source>
						...
					 *
					 */
			
			// TODO resolve problem with upper-/lower-case
			for (String strCurrentState : objSplitterOptions.StateNames.getValueList()) {
				if (objOrderCurrent.job_chain().node(strCurrentState) == null) {
					throw new JobSchedulerException(String.format("State '%1$s' in chain '%2$s' not found but mandatory", strCurrentState, strJobChainName));
				}
			}

			if (objSplitterOptions.StateNames.isDirty() == false) {
//				objOrderCurrent.job_chain().n
			}
			int lngNoOfParallelSteps = objSplitterOptions.StateNames.getValueList().length;
			flgCreateSyncParameter = true;
			if (flgCreateSyncParameter == true) {
			    
                //The api does not return the job chain path. Only the name: JS-472
			    String orderXML = this.spooler_task.order().xml();
	            SOSXMLXPath xp = new SOSXMLXPath(new ByteArrayInputStream(orderXML.getBytes("UTF-8")));
	            
	            String jobChainPath = xp.selectSingleNodeValue("/order/@job_chain");
 			    
			    
//				String strSyncParam = strSyncStateName + "/" + strJobChainName + ";" + strSyncStateName + SyncNodeList.CONST_PARAM_PART_REQUIRED_ORDERS;
				String strSyncParam = strJobChainName + SyncNodeList.CHAIN_ORDER_DELIMITER + strSyncStateName + SyncNodeList.CONST_PARAM_PART_REQUIRED_ORDERS;
                objOrderParams.set_var(strSyncParam, Integer.toString(lngNoOfParallelSteps + 1));
                
                //Setting the context of the sync job to make the sync job reusable
                objOrderParams.set_var(PARAMETER_JOB_CHAIN_NAME2SYNCHRONIZE,jobChainPath);
                objOrderParams.set_var(PARAMETER_JOB_CHAIN_STATE2SYNCHRONIZE,strSyncStateName);
				// TODO use global constant
				objOrderParams.set_var(PARAMETER_SYNC_SESSION_ID, strJobChainName + "_" + strSyncStateName + "_" + objOrderCurrent.id());
			}

			for (String strCurrentState : objSplitterOptions.StateNames.getValueList()) {
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

	protected String getNextStateNodeName() {
		final String conMethodName = conClassName + "::getNextStateNodeName";
		String lstrNextStateNodeName = "";
		if (spooler_task != null) {
			Order objCurrentOrder = spooler_task.order();
			if (isNotNull(objCurrentOrder)) {
				lstrNextStateNodeName = objCurrentOrder.job_chain_node().next_state();
				JSJ_I_0020.toLog(conMethodName, lstrNextStateNodeName);
			}
			else {
				Job objCurrentJob = getJob();
				lstrNextStateNodeName = objCurrentJob.name();
				JSJ_I_0010.toLog(conMethodName, lstrNextStateNodeName);
			}
		}
		return lstrNextStateNodeName;
	} // public String getNodeName


}
