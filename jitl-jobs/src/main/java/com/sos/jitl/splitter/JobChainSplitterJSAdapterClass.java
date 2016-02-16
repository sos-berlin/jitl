package com.sos.jitl.splitter;

import org.apache.log4j.Logger;
import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Order;
import sos.spooler.Variable_set;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.jitl.sync.SyncNodeList;
 
 
public class JobChainSplitterJSAdapterClass extends JobSchedulerJobAdapter {
	private static final String PARAMETER_SYNC_SESSION_ID = "sync_session_id";
    private static final String PARAMETER_JOB_CHAIN_STATE2SYNCHRONIZE = "job_chain_state2synchronize";
    private static final String PARAMETER_JOB_CHAIN_NAME2SYNCHRONIZE = "job_chain_name2synchronize";
    private final String	conClassName	= "JobChainSplitterJSAdapterClass";
	@SuppressWarnings("hiding")
	private static Logger	logger			= Logger.getLogger(JobChainSplitterJSAdapterClass.class);

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
			JobChainSplitterOptions objSplitterOptions = new JobChainSplitterOptions();

			// TODO make this class available as a monitor class as well
			objSplitterOptions.CurrentNodeName(this.getCurrentNodeName());
			objSplitterOptions.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
			// JITL-145: commented to prevent logging of passwords, dirtyString-Method of JSOptionClass calls getAllOptionsAsString 
			// which itself aggregates a String with all Options without checking, to log that String can result in clear passwords being logged
//			logger.info(objSplitterOptions.dirtyString());
			objSplitterOptions.CheckMandatory();

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

					
			
			// TODO resolve problem with upper-/lower-case
			for (String strCurrentState : objSplitterOptions.StateNames.getValueList()) {
				if (objOrderCurrent.job_chain().node(strCurrentState) == null) {
					throw new JobSchedulerException(String.format("State '%1$s' in chain '%2$s' not found but mandatory", strCurrentState, strJobChainName));
				}
			}


			int lngNoOfParallelSteps = objSplitterOptions.StateNames.getValueList().length;
		 
	        String jobChainPath = objOrderCurrent.job_chain().path();
		    String strSyncParam = strJobChainName + SyncNodeList.CHAIN_ORDER_DELIMITER + strSyncStateName + SyncNodeList.CONST_PARAM_PART_REQUIRED_ORDERS;
            objOrderParams.set_var(strSyncParam, Integer.toString(lngNoOfParallelSteps + 1));
                
            //Setting the context of the sync job to make the sync job reusable
            if (objSplitterOptions.createSyncContext.value()){
                objOrderParams.set_var(PARAMETER_JOB_CHAIN_NAME2SYNCHRONIZE,jobChainPath);
                objOrderParams.set_var(PARAMETER_JOB_CHAIN_STATE2SYNCHRONIZE,strSyncStateName);
			}
            
            if (objSplitterOptions.createSyncSessionId.value()){
				   objOrderParams.set_var(PARAMETER_SYNC_SESSION_ID, strJobChainName + "_" + strSyncStateName + "_" + objOrderCurrent.id());
            }
		
			for (String strCurrentState : objSplitterOptions.StateNames.getValueList()) {
				Order objOrderClone = spooler.create_order();
				objOrderClone.set_state(strCurrentState);
				objOrderClone.set_title(objOrderCurrent.title() + ": " + strCurrentState);
				objOrderClone.set_end_state(strSyncStateName);
				objOrderClone.params().merge(objOrderParams);
				objOrderClone.set_ignore_max_orders(true); //JS-1103
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
