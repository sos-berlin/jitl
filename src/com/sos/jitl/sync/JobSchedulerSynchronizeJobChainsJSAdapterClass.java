package com.sos.jitl.sync;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Job_chain;
import sos.spooler.Job_chain_node;
import sos.spooler.Order;
import sos.spooler.Spooler;
import sos.spooler.Variable_set;

import com.sos.JSHelper.Basics.IJSCommands;
import com.sos.JSHelper.Exceptions.JobSchedulerException;

// Super-Class for JobScheduler Java-API-Jobs

/**
 * \class 		JobSchedulerSynchronizeJobChainsJSAdapterClass - JobScheduler Adapter for "Synchronize Job Chains"
 *
 * \brief AdapterClass of JobSchedulerSynchronizeJobChains for the SOSJobScheduler
 *
 * This Class JobSchedulerSynchronizeJobChainsJSAdapterClass works as an adapter-class between the SOS
 * JobScheduler and the worker-class JobSchedulerSynchronizeJobChains.
 *

 *
 *
 * \verbatim ;
 * mechanicaly created by C:\ProgramData\sos-berlin.com\jobscheduler\scheduler_ur\config\JOETemplates\java\xsl\JSJobDoc2JSAdapterClass.xsl from http://www.sos-berlin.com at 20121217120436
 * \endverbatim
 */
public class JobSchedulerSynchronizeJobChainsJSAdapterClass extends JobSchedulerJobAdapter {
	private static final String SYNC_METHOD_SETBACK = "setback";
    private static final String	conParameterSCHEDULER_SYNC_READY	= "scheduler_sync_ready";
	private static final String	COMMAND_SHOW_JOB					= "<show_job job=\"%s\" max_task_history=\"0\" what=\"job_orders job_chains payload\"/>";
	private static final String	COMMAND_SHOW_JOB_CHAIN_FOLDERS		= "<show_state max_order_history=\"0\" max_orders=\"0\" what=\"job_chains folders\" subsystems=\"folder order\"/>";
	private final String		conClassName						= "JobSchedulerSynchronizeJobChainsJSAdapterClass";																//$NON-NLS-1$
	@SuppressWarnings("hiding")
	private static Logger		logger								= Logger.getLogger(JobSchedulerSynchronizeJobChainsJSAdapterClass.class);

	public void init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
		doInitialize();
	}

	private void doInitialize() {
	} // doInitialize

	@Override
	public boolean spooler_init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_init"; //$NON-NLS-1$
		return super.spooler_init();
	}

	@Override
	public boolean spooler_process() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_process"; //$NON-NLS-1$

		try {
			
            spooler_log.debug3("--->" + spooler_task.order().params().value("scheduler_file_path"));
            // to avoid this generated order going to blacklist
            if (!spooler_task.order().params().value("scheduler_file_path").equals("")) {
                spooler_log.debug3("---> id" + spooler_task.order().id());
                if (!spooler_task.order().id().equals(spooler_task.order().params().value("scheduler_file_path"))){
                    spooler_log.debug3("---> setze scheduler_file_path");
                   spooler_task.order().params().set_var("scheduler_file_path", "");  
                }
            }		    
		    
		    super.spooler_process();

			//Ab hier wegen http://www.sos-berlin.com/jira/browse/JS-461
			boolean syncReady = false;
			if (spooler_task.order().params().value(conParameterSCHEDULER_SYNC_READY) != null) {
				syncReady = spooler_task.order().params().value(conParameterSCHEDULER_SYNC_READY).equals("true");
			}
			if (syncReady) {
				spooler_log.info("js-461: Sync skipped");
				Order o = spooler_task.order();
				Variable_set resultParameters = spooler.create_variable_set();
				String[] parameterNames = o.params().names().split(";");
				for (int i = 0; i < parameterNames.length; i++) {
					if (!parameterNames[i].equals(conParameterSCHEDULER_SYNC_READY)) {
						resultParameters.set_var(parameterNames[i], o.params().value(parameterNames[i]));
					}
				}
				o.set_params(resultParameters);
		 	        
				return signalSuccess();
			}
			//js-461 Ende

			doProcessing();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			throw new JobSchedulerException("--- Fatal Error: " + e.getLocalizedMessage(), e);
		}
		finally {
		} // finally

		return signalSuccess();

	} // spooler_process

	@Override
	public void spooler_exit() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_exit"; //$NON-NLS-1$
		super.spooler_exit();
	}

	private void setSetback(JobSchedulerSynchronizeJobChainsOptions objO) {
	    // by precedence we use the configuration specified by <delay_order_after_setback/>
	    spooler_log.debug3(".............");
	    if (objO.setback_type.Value().equalsIgnoreCase(SYNC_METHOD_SETBACK)) {
	        spooler_log.debug3("Setting setback parameters");
            if (spooler_job.setback_max() <= 0) {
                spooler_log.debug3(String.format("Setting setback_interval=%s",objO.setback_interval.value()));
                spooler_job.set_delay_order_after_setback(1, objO.setback_interval.value());
                if (objO.setback_count.value() > 0) {
                    spooler_log.debug3(String.format("Setting setback_count=%s",objO.setback_count.value()));
                    spooler_job.set_max_order_setbacks(objO.setback_count.value());
                }
            }
	    }
	}
	 

	private void doProcessing() throws Exception {
       
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::doProcessing";

		JobSchedulerSynchronizeJobChains objR = new JobSchedulerSynchronizeJobChains();
		JobSchedulerSynchronizeJobChainsOptions objO = objR.Options();
		objR.setJSJobUtilites(this);
		objO.CurrentNodeName(this.getCurrentNodeName());
		
	
		SchedulerParameters = getSchedulerParameterAsProperties(getJobOrOrderParameters());

		objO.setAllOptions(SchedulerParameters);
        setSetback(objO);
		objO.CheckMandatory();

		String jobName = spooler_task.job().name();
		objO.jobpath.Value(jobName);
		objR.setJSJobUtilites(this);

	/*	String jobchain = spooler_task.order().job_chain().name();
        if (objO.sync_node_context.Value().equals("_jobchain")) {
            logger.debug(String.format("Setting sync_node_context:%s",jobchain));
            objO.sync_node_context.Value(jobchain);
       }

       if (objO.sync_node_context.Value().startsWith("_jobchain,")) {
            String s = objO.sync_node_context.Value();
            String s_value[]  = s.split(",");
            logger.debug(String.format("Setting sync_node_context:%s,%s",jobchain,s_value[1]));
            objO.sync_node_context.Value(jobchain + "," + s_value[1]);
       }
		
		*/
		String answer = spooler.execute_xml(COMMAND_SHOW_JOB_CHAIN_FOLDERS);
		// logger.debug(answer);
		objO.jobchains_answer.Value(answer);
		answer = spooler.execute_xml(String.format(COMMAND_SHOW_JOB, jobName));
		// logger.debug(answer);
		objO.orders_answer.Value(answer);

		IJSCommands objJSCommands = this;
		Object objSp = objJSCommands.getSpoolerObject();
		Spooler objSpooler = (Spooler) objSp;

		objO.jobpath.Value("/" + spooler_task.job().name());

		for (final Map.Entry <String,String> element : SchedulerParameters.entrySet()) {
			final String strMapKey = element.getKey().toString();
			String strTemp = "";
			if (element.getValue() != null) {
				strTemp = element.getValue().toString();
				if (strMapKey.contains("password")) {
					strTemp = "***";
				}
			}
			logger.info("Key = " + strMapKey + " --> " + strTemp);
		}

		objR.setSchedulerParameters(SchedulerParameters);

		objR.Execute();

		if (objR.syncNodeContainer.isReleased()) {
			while (!objR.syncNodeContainer.eof()) {
				SyncNode objSyncNode = objR.syncNodeContainer.getNextSyncNode();
				Job_chain objJobChain = objSpooler.job_chain(objSyncNode.getSyncNodeJobchainPath());
				Job_chain_node objCurrentNode = objJobChain.node(objSyncNode.getSyncNodeState());

				 

				List<SyncNodeWaitingOrder> lstWaitingOrders = objSyncNode.getSyncNodeWaitingOrderList();
				for (SyncNodeWaitingOrder objWaitingOrder : lstWaitingOrders) {
					String strEndState = objWaitingOrder.getEndState();
					logger.debug(String.format("Release jobchain=%s order=%s at state %s, endstate=%s", objSyncNode.getSyncNodeJobchainPath(),
							objWaitingOrder.getId(), objSyncNode.getSyncNodeState(), strEndState));

					Job_chain_node next_n = objCurrentNode.next_node();

					//Den Fall behandeln, dass der Syncknoten gleich dem Endknoten des Auftrages ist
					//Wenn das der Fall ist, wird der Job nochmal ausgeführt, damit der Auftrag im Sync-Knoten verbleibt.
					//Es wird in dem Fall scheduler_sync_ready=true gesetzt. Dann liefert der Job true und der Auftrag ist beendent.
					String strNextState = objCurrentNode.next_state();
					if (objCurrentNode.state().equalsIgnoreCase(strEndState)) {
 						strNextState = objCurrentNode.state();
					}
 
					if (strEndState.length() > 0) {
					  strEndState = " end_state='" + strEndState + "' ";
					}
					// TODO Why not using the Internal API?
					// Antwort: Weil das nicht geht. http://www.sos-berlin.com/jira/browse/JS-578
					// TODO Why repeated code?
					String strJSCommand = "";
					
					if (objO.setback_type.Value().equalsIgnoreCase(SYNC_METHOD_SETBACK) ) {
					    strJSCommand = String.format("<modify_order job_chain='%s' order='%s' setback='no'>" +
					    		"<params><param name='scheduler_sync_ready' " +
                                "value='true'></param></params></modify_order>", 
                                objSyncNode.getSyncNodeJobchainPath(),objWaitingOrder.getId());
					}else {
    					if (next_n.job() == null || strNextState.equals(objCurrentNode.state())) { //siehe http://www.sos-berlin.com/jira/browse/JS-461
    						strJSCommand = String.format(
    						        "<modify_order job_chain='%s' order='%s' suspended='no' %s >" +
    								"<params><param name='scheduler_sync_ready' " +
    				     		    "value='true'></param></params></modify_order>", 
    								objSyncNode.getSyncNodeJobchainPath(),objWaitingOrder.getId(),strEndState);
    					}
    					else {
    						strJSCommand = String.format(
    						        "<modify_order job_chain='%s' order='%s' state='%s' suspended='no' %s />",objSyncNode.getSyncNodeJobchainPath(),objWaitingOrder.getId(),strNextState, strEndState);
    					}
					}
					logger.debug(strJSCommand);
					answer = objSpooler.execute_xml(strJSCommand);
					logger.debug(answer);
				}
			}
		}
		else {
		    SyncNode sn = objR.syncNodeContainer.getNode(spooler_task.order().job_chain().name(),spooler_task.order().state());
		    String stateText = "";
		    if (sn != null){
	            stateText = String.format("%s required: %s, waiting: %s", objR.syncNodeContainer.getShortSyncNodeContext(), sn.getRequired(),sn.getSyncNodeWaitingOrderList().size());
		    
    		    if(sn.isReleased()) {
    		        SyncNode notReleased = objR.syncNodeContainer.getFirstNotReleasedNode();
    		        String etc = "";
    		        if (objR.syncNodeContainer.getNumberOfWaitingNodes() > 1) {
    		            etc = "...";
    		        }
    		        if (notReleased != null) {
    		          stateText = String.format("%s --> released. waiting for %s/%s %s",stateText,notReleased.getSyncNodeJobchainName(),notReleased.getSyncNodeState(),etc);
    		        }else {
                      stateText = String.format("%s --> released",stateText);
    		        }
    		    }
		    }
		    logger.debug("...stateText:" + stateText);
		    spooler_task.order().set_state_text(stateText);
		    
		    if (objO.setback_type.Value().equalsIgnoreCase(SYNC_METHOD_SETBACK) ) {
		        spooler_task.order().setback();
		    }else {
    			if (!spooler_task.order().suspended()) {
    				spooler_task.order().set_state(spooler_task.order().state()); //Damit der Suspend auf den sync-Knoten geht und nicht auf den nächsten.
    				spooler_task.order().set_suspended(true);
    			}
		    }
		}

	} // doProcessing

}
