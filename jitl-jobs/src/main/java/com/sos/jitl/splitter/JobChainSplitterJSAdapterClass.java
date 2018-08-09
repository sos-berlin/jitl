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
    private static final Logger LOGGER = Logger.getLogger(JobChainSplitterJSAdapterClass.class);

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        }
        return signalSuccess();
    }

    private void doProcessing() throws Exception {
        if (isOrderJob()) {
            JobChainSplitterOptions jobChainSplitterOptions = new JobChainSplitterOptions();
            jobChainSplitterOptions.setCurrentNodeName(this.getCurrentNodeName());
            jobChainSplitterOptions.setAllOptions(getSchedulerParameterAsProperties());
            jobChainSplitterOptions.checkMandatory();
            Order currentOrder = spooler_task.order();
            Variable_set orderParams = currentOrder.params();
            String syncStateName = jobChainSplitterOptions.syncStateName.getValue();
            if (syncStateName.isEmpty()) {
                syncStateName =  jobChainSplitterOptions.joinStateName.getValue();
            }
            if (syncStateName.isEmpty()) {
                syncStateName = currentOrder.job_chain_node().next_state();
            }
            LOGGER.debug(String.format("SyncStateName = '%1$s'", syncStateName));
            String jobChainName = currentOrder.job_chain().name();
            for (String currentState : jobChainSplitterOptions.stateNames.getValueList()) {
                if (currentOrder.job_chain().node(currentState) == null) {
                    throw new JobSchedulerException(String.format("State '%1$s' in chain '%2$s' not found but mandatory", currentState,
                            jobChainName));
                }
            }
            int lngNoOfParallelSteps = jobChainSplitterOptions.stateNames.getValueList().length;
            String jobChainPath = currentOrder.job_chain().path();
            String syncParam =
                    jobChainName + SyncNodeList.CHAIN_ORDER_DELIMITER + syncStateName + SyncNodeList.CONST_PARAM_PART_REQUIRED_ORDERS;
            orderParams.set_var(syncParam, Integer.toString(lngNoOfParallelSteps + 1));
            if (jobChainSplitterOptions.createSyncContext.value()) {
                orderParams.set_var(PARAMETER_JOB_CHAIN_NAME2SYNCHRONIZE, jobChainPath);
                orderParams.set_var(PARAMETER_JOB_CHAIN_STATE2SYNCHRONIZE, syncStateName);
            }
            if (jobChainSplitterOptions.createSyncSessionId.value()) {
                orderParams.set_var(PARAMETER_SYNC_SESSION_ID, jobChainName + "_" + syncStateName + "_" + currentOrder.id());
            }
            for (String strCurrentState : jobChainSplitterOptions.stateNames.getValueList()) {
                Order orderClone = spooler.create_order();
                orderClone.set_state(strCurrentState);
                orderClone.set_title(currentOrder.title() + ": " + strCurrentState);
                orderClone.set_end_state(syncStateName);
                orderClone.params().merge(orderParams);
                orderClone.params().set_value("join_session_id", spooler_task.order().id());
                orderClone.set_xml_payload(spooler_task.order().xml_payload());
                orderClone.set_ignore_max_orders(true); // JS-1103
                String orderCloneName = currentOrder.id() + "-+" + currentOrder.history_id() + "+-" + strCurrentState;
                orderClone.set_id(orderCloneName);
                orderClone.set_at("now");
                currentOrder.job_chain().add_or_replace_order(orderClone);
                LOGGER.info(String.format("Order '%1$s' created and started", orderCloneName));
                LOGGER.debug(orderClone.xml());
            }
        } else {
            throw new JobSchedulerException("This Job can run as an job in a jobchain only");
        }
    }

}