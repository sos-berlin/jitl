package com.sos.jitl.sync;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_107;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_110;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_111;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JobSchedulerSynchronizeJobChains {

    private final String conClassName = "JobSchedulerSynchronizeJobChains";
    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerSynchronizeJobChains.class);

    protected SyncNodeContainer syncNodeContainer;
    protected HashMap<String, String> schedulerParameters = new HashMap<String, String>();

    private String orderId = "";
    private String jobChain = "";

    public JobSchedulerSynchronizeJobChains execute(JobSchedulerSynchronizeJobChainsOptions jobSchedulerSynchronizeJobChainsOptions) throws Exception {
        final String conMethodName = conClassName + "::Execute"; //$NON-NLS-1$

        JSJ_I_110.toLog(conMethodName);

        try {
            jobSchedulerSynchronizeJobChainsOptions.checkMandatory();
            LOGGER.debug(jobSchedulerSynchronizeJobChainsOptions.dirtyString());

            syncNodeContainer = new SyncNodeContainer();

            syncNodeContainer.setIgnoreStoppedJobChains(jobSchedulerSynchronizeJobChainsOptions.ignore_stopped_jobchains.value());
            syncNodeContainer.setJobpath(jobSchedulerSynchronizeJobChainsOptions.jobpath.getValue());

            if (jobSchedulerSynchronizeJobChainsOptions.disable_sync_context.value()) {
                LOGGER.debug("Disable sync context");
                syncNodeContainer.setSyncNodeContext("", "");
            } else {
                LOGGER.debug(String.format("Set sync context: %s,%s", jobSchedulerSynchronizeJobChainsOptions.job_chain_name2synchronize.getValue(),
                        jobSchedulerSynchronizeJobChainsOptions.job_chain_state2synchronize.getValue()));
                syncNodeContainer.setSyncNodeContext(jobSchedulerSynchronizeJobChainsOptions.job_chain_name2synchronize.getValue(),
                        jobSchedulerSynchronizeJobChainsOptions.job_chain_state2synchronize.getValue());
            }

            String syncId = jobSchedulerSynchronizeJobChainsOptions.getsync_session_id().getValue();
            syncNodeContainer.getNodes(jobSchedulerSynchronizeJobChainsOptions.jobchains_answer.getValue());

            syncNodeContainer.getOrders(jobChain, orderId, syncId, jobSchedulerSynchronizeJobChainsOptions.orders_answer.getValue());
            syncNodeContainer.setRequiredOrders(schedulerParameters);

            if (syncNodeContainer.isReleased()) {
                LOGGER.debug("Release all orders");
            } else {
                LOGGER.debug("Suspending all orders");
            }

        } catch (Exception e) {
            throw new JobSchedulerException(JSJ_F_107.get(conMethodName) + ":" + e.getMessage(), e);
        }

        JSJ_I_111.toLog(conMethodName);
        return this;
    }

   
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setJobChain(String jobChain) {
        this.jobChain = jobChain;
    }

}  