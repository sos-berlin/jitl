

package com.sos.jitl.sync;



import java.util.List;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Job_chain;
import sos.spooler.Job_chain_node;
import sos.spooler.Order;
import sos.spooler.Spooler;
import sos.spooler.Variable_set;

import com.sos.JSHelper.Basics.IJSCommands;
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
public class JobSchedulerSynchronizeJobChainsJSAdapterClass extends JobSchedulerJobAdapter  {
	private static final String COMMAND_SHOW_JOB = "<show_job job=\"%s\" max_task_history=\"0\" what=\"job_orders job_chains payload\"/>";
	private static final String COMMAND_SHOW_JOB_CHAIN_FOLDERS = "<show_state max_order_history=\"0\" max_orders=\"0\" what=\"job_chains folders\" subsystems=\"folder order\"/>";
	private final String					conClassName						= "JobSchedulerSynchronizeJobChainsJSAdapterClass";  //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobSchedulerSynchronizeJobChainsJSAdapterClass.class);

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
			super.spooler_process();

			//Ab hier wegen js-461
	  		 boolean syncReady = false;
	  		 if (spooler_task.order().params().value("scheduler_sync_ready")!=null) {
	  			 syncReady = spooler_task.order().params().value("scheduler_sync_ready").equals("true");
	  		 }
	         if (syncReady) {
	            spooler_log.info("js-461: Sync skipped");
	            Order o = spooler_task.order();
	            Variable_set resultParameters = spooler.create_variable_set();
	            String[] parameterNames = o.params().names().split(";");
	            for(int i=0; i<parameterNames.length; i++) {
	                if (!parameterNames[i].equals("scheduler_sync_ready")) {
	                    resultParameters.set_var(parameterNames[i], o.params().value(parameterNames[i]));
	                }
	            }
	            o.set_params(resultParameters);
	            return true;
	         }
	  		//js-461 Ende


			doProcessing();
		}
		catch (Exception e) {
		     throw e;
   		}
		finally {
		} // finally

		return spooler_task.job().order_queue() != null;

	} // spooler_process


	@Override
	public void spooler_exit() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_exit"; //$NON-NLS-1$
		super.spooler_exit();
	}

	private void doProcessing() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::doProcessing";

		com.sos.jitl.sync.JobSchedulerSynchronizeJobChains objR = new com.sos.jitl.sync.JobSchedulerSynchronizeJobChains();
		JobSchedulerSynchronizeJobChainsOptions objO = objR.Options();
        objR.setJSJobUtilites(this);
		objO.CurrentNodeName(this.getCurrentNodeName());

		objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
		objO.CheckMandatory();

		String jobName = spooler_task.job().name();
		objO.jobpath.Value(jobName);
        objR.setJSJobUtilites(this);

        String answer = spooler.execute_xml(COMMAND_SHOW_JOB_CHAIN_FOLDERS);
       // logger.debug(answer);
        objO.jobchains_answer.Value(answer);
        answer = spooler.execute_xml(String.format(COMMAND_SHOW_JOB,jobName));
       // logger.debug(answer);
        objO.orders_answer.Value(answer);


 		 IJSCommands objJSCommands = this;
         Object objSp = objJSCommands.getSpoolerObject();
         Spooler objSpooler = (Spooler) objSp;

        objO.jobpath.Value("/"+spooler_task.job().name());

  		objR.setSchedulerParameters(SchedulerParameters);

  		objR.Execute();

		if (objR.syncNodeContainer.isReleased()){

	        while (! objR.syncNodeContainer.eof()){
	          SyncNode sn = objR.syncNodeContainer.getNextSyncNode();

 			  List<SyncNodeWaitingOrder> ol = sn.getSyncNodeWaitingOrderList();
  	      	  for( SyncNodeWaitingOrder ow: ol){
  	      		  logger.debug(String.format("Release jobchain=%s order=%s at state %s",sn.getSyncNodeJobchainPath(),ow.getId(),sn.getSyncNodeState()));

 		 	  	  Job_chain j = objSpooler.job_chain(sn.getSyncNodeJobchainPath());
 	              Job_chain_node n = j.node(sn.getSyncNodeState());
 	              Job_chain_node next_n = n.next_node();

 	              String next_state = n.next_state();
 	              if (next_n.job() == null) { //siehe js-461
   	                answer = objSpooler.execute_xml("<modify_order job_chain='" + sn.getSyncNodeJobchainPath() + "' order='" + ow.getId() + "' suspended='no'><params><param name='scheduler_sync_ready' value='true'></param></params></modify_order>");
 	              }else {
 	                answer = objSpooler.execute_xml("<modify_order job_chain='" + sn.getSyncNodeJobchainPath() + "' order='" + ow.getId() + "' state='" + next_state + "' suspended='no'/>");
 	              }
 		      	}
			}

	    }else{
	        if (!spooler_task.order().suspended()) {
                spooler_task.order().set_state(spooler_task.order().state()); //Damit der Suspend auf den sync-Knoten geht und nicht auf den nächsten.
                spooler_task.order().set_suspended(true);
            }
	    }


	} // doProcessing

}


