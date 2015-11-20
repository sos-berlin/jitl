package com.sos.jitl.sync;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_107;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_110;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_111;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
 
 
public class JobSchedulerSynchronizeJobChains extends JSJobUtilitiesClass<JobSchedulerSynchronizeJobChainsOptions> {
	private final String								conClassName		= "JobSchedulerSynchronizeJobChains";
	private static Logger								logger				= Logger.getLogger(JobSchedulerSynchronizeJobChains.class);

	protected SyncNodeContainer							syncNodeContainer;
	protected HashMap<String, String>					SchedulerParameters	= new HashMap<String, String>();

    private String orderId = "";
    private String jobChain = "";
 
	public JobSchedulerSynchronizeJobChains() {
		super(new JobSchedulerSynchronizeJobChainsOptions());
	}

 
	@Override
	public JobSchedulerSynchronizeJobChainsOptions getOptions() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new JobSchedulerSynchronizeJobChainsOptions();
		}
		return objOptions;
	}

	 
	public JobSchedulerSynchronizeJobChains Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute"; //$NON-NLS-1$

		JSJ_I_110.toLog(conMethodName );

		try {
			getOptions().CheckMandatory();
			logger.debug(getOptions().dirtyString());


			syncNodeContainer = new SyncNodeContainer();
			
			syncNodeContainer.setIgnoreStoppedJobChains(getOptions().ignore_stopped_jobchains.value());	 
            syncNodeContainer.setJobpath(getOptions().jobpath.Value());
           
            if (getOptions().disable_sync_context.value()) {
                logger.debug("Disable sync context");
                syncNodeContainer.setSyncNodeContext("","");
            }else {
                logger.debug(String.format("Set sync context: %s,%s",getOptions().job_chain_name2synchronize.Value(),getOptions().job_chain_state2synchronize.Value()));
                syncNodeContainer.setSyncNodeContext(getOptions().job_chain_name2synchronize.Value(),getOptions().job_chain_state2synchronize.Value());
            }			
            
            String syncId = getOptions().getsync_session_id().Value();
            syncNodeContainer.getNodes(getOptions().jobchains_answer.Value());
		
            syncNodeContainer.getOrders(jobChain,orderId,syncId, getOptions().orders_answer.Value());
			syncNodeContainer.setRequiredOrders(SchedulerParameters);

			if (syncNodeContainer.isReleased()) {
				logger.debug("Release all orders");
			}
			else {
				logger.debug("Suspending all orders");
			}

		}
		catch (Exception e) {
			throw new JobSchedulerException(JSJ_F_107.get(conMethodName) + ":" + e.getMessage(), e);
		}

		JSJ_I_111.toLog(conMethodName);
		return this;
	}

	public void init() throws RuntimeException, Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
		doInitialize();
	}

	private void doInitialize() throws RuntimeException, Exception {

	} // doInitialize

	public void setSchedulerParameters(final HashMap<String, String> schedulerParameters) {
		SchedulerParameters = schedulerParameters;
	}

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setJobChain(String jobChain) {
        this.jobChain = jobChain;
    }

} // class JobSchedulerSynchronizeJobChains