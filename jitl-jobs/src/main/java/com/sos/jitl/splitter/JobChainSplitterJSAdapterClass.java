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
            JobChainSplitterOptions objSplitterOptions = new JobChainSplitterOptions();
            objSplitterOptions.CurrentNodeName(this.getCurrentNodeName());
            objSplitterOptions.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
            objSplitterOptions.checkMandatory();
            Order objOrderCurrent = spooler_task.order();
            Variable_set objOrderParams = objOrderCurrent.params();
            String strSyncStateName = objSplitterOptions.SyncStateName.Value();
            if (strSyncStateName.isEmpty()) {
                strSyncStateName = objOrderCurrent.job_chain_node().next_state();
            }
            LOGGER.debug(String.format("SyncStateName = '%1$s'", strSyncStateName));
            String strJobChainName = objOrderCurrent.job_chain().name();
            for (String strCurrentState : objSplitterOptions.StateNames.getValueList()) {
                if (objOrderCurrent.job_chain().node(strCurrentState) == null) {
                    throw new JobSchedulerException(String.format("State '%1$s' in chain '%2$s' not found but mandatory", strCurrentState,
                            strJobChainName));
                }
            }
            int lngNoOfParallelSteps = objSplitterOptions.StateNames.getValueList().length;
            String jobChainPath = objOrderCurrent.job_chain().path();
            String strSyncParam =
                    strJobChainName + SyncNodeList.CHAIN_ORDER_DELIMITER + strSyncStateName + SyncNodeList.CONST_PARAM_PART_REQUIRED_ORDERS;
            objOrderParams.set_var(strSyncParam, Integer.toString(lngNoOfParallelSteps + 1));
            if (objSplitterOptions.createSyncContext.value()) {
                objOrderParams.set_var(PARAMETER_JOB_CHAIN_NAME2SYNCHRONIZE, jobChainPath);
                objOrderParams.set_var(PARAMETER_JOB_CHAIN_STATE2SYNCHRONIZE, strSyncStateName);
            }
            if (objSplitterOptions.createSyncSessionId.value()) {
                objOrderParams.set_var(PARAMETER_SYNC_SESSION_ID, strJobChainName + "_" + strSyncStateName + "_" + objOrderCurrent.id());
            }
            for (String strCurrentState : objSplitterOptions.StateNames.getValueList()) {
                Order objOrderClone = spooler.create_order();
                objOrderClone.set_state(strCurrentState);
                objOrderClone.set_title(objOrderCurrent.title() + ": " + strCurrentState);
                objOrderClone.set_end_state(strSyncStateName);
                objOrderClone.params().merge(objOrderParams);
                objOrderClone.set_ignore_max_orders(true); // JS-1103
                String strOrderCloneName = objOrderCurrent.id() + "_" + strCurrentState;
                objOrderClone.set_id(strOrderCloneName);
                objOrderClone.set_at("now");
                objOrderCurrent.job_chain().add_or_replace_order(objOrderClone);
                LOGGER.info(String.format("Order '%1$s' created and started", strOrderCloneName));
                LOGGER.debug(objOrderClone.xml());
            }
        } else {
            throw new JobSchedulerException("This Job can run as an job in a jobchain only");
        }
    }

}