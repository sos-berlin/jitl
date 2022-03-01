package com.sos.jitl.sync;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_0060;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

import sos.spooler.Job_chain;
import sos.spooler.Job_chain_node;
import sos.spooler.Job_impl;
import sos.spooler.Order;
import sos.spooler.Variable_set;

public class JobSchedulerSynchronizeJobChainsJSAdapterClass extends Job_impl {

    private static final int MAX_LENGTH_OF_STATUSTEXT = 100;
    private static final String SYNC_METHOD_SETBACK = "setback";
    private static final String PARAMETER_SCHEDULER_SYNC_READY = "scheduler_sync_ready";
    private static final String COMMAND_SHOW_JOB = "<show_job job=\"%s\" max_task_history=\"0\" what=\"job_orders job_chains payload\"/>";
    private static final String COMMAND_SHOW_JOB_CHAIN_FOLDERS = "<show_state max_order_history=\"0\" max_orders=\"0\" what=\"job_chains folders\" "
            + "subsystems=\"folder order\"/>";
    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerSynchronizeJobChainsJSAdapterClass.class);

    @Override
    public boolean spooler_process() throws Exception {
        Order order = spooler_task.order();
        try {
            spooler_task.order().set_state_text("");
            spooler_log.debug3("--->" + spooler_task.order().params().value("scheduler_file_path"));
            if (!"".equals(spooler_task.order().params().value("scheduler_file_path"))) {
                spooler_log.debug3("---> id" + spooler_task.order().id());
                if (!spooler_task.order().id().equals(spooler_task.order().params().value("scheduler_file_path"))) {
                    spooler_log.debug3("---> setze scheduler_file_path");
                    spooler_task.order().params().set_var("scheduler_file_path", "");
                }
            }
 
            boolean syncReady = false;
            if (order.params().value(PARAMETER_SCHEDULER_SYNC_READY) != null) {
                syncReady = "true".equals(spooler_task.order().params().value(PARAMETER_SCHEDULER_SYNC_READY));
            }
            if (syncReady) {
                spooler_log.debug("js-461: Sync skipped");
                Variable_set resultParameters = spooler.create_variable_set();
                String[] parameterNames = order.params().names().split(";");
                for (int i = 0; i < parameterNames.length; i++) {
                    if (!PARAMETER_SCHEDULER_SYNC_READY.equals(parameterNames[i])) {
                        resultParameters.set_var(parameterNames[i], order.params().value(parameterNames[i]));
                    }
                }
                order.set_params(resultParameters);
                return true;
            }
            doProcessing();
        } catch (Exception e) {
            throw new JobSchedulerException("--- Fatal Error: " + e.getMessage(), e);
        }
        return true;
    }

    private void setSetback(JobSchedulerSynchronizeJobChainsOptions jobSchedulerSynchronizeJobChainsOptions) {
        spooler_log.debug3(".............");
        if (jobSchedulerSynchronizeJobChainsOptions.setback_type.getValue().equalsIgnoreCase(SYNC_METHOD_SETBACK)) {
            spooler_log.debug3("Setting setback parameters");
            if (spooler_job.setback_max() <= 0) {
                spooler_log.debug3(String.format("Setting setback_interval=%s", jobSchedulerSynchronizeJobChainsOptions.setback_interval.value()));
                spooler_job.set_delay_order_after_setback(1, jobSchedulerSynchronizeJobChainsOptions.setback_interval.value());
                if (jobSchedulerSynchronizeJobChainsOptions.setback_count.value() > 0) {
                    spooler_log.debug3(String.format("Setting setback_count=%s", jobSchedulerSynchronizeJobChainsOptions.setback_count.value()));
                    spooler_job.set_max_order_setbacks(jobSchedulerSynchronizeJobChainsOptions.setback_count.value());
                }
            }
        }
    }
    
    private void setStateText(final String state) {
        if (state != null) {
            String stateText = state;
            if (stateText.length() > MAX_LENGTH_OF_STATUSTEXT) {
                stateText = stateText.substring(0, MAX_LENGTH_OF_STATUSTEXT - 3) + "...";
            }
            Order order = spooler_task.order();
            if (order != null) {
                try {
                    order.set_state_text(stateText);
                } catch (Exception e) {
                    //
                }
            }
            if (spooler_job != null) {
                spooler_job.set_state_text(stateText);
            }
        }
    }

    protected HashMap<String, String> convertVariableSet2HashMap(final Variable_set params) {
        HashMap<String, String> result = new HashMap<String, String>();
        try {
            if (params != null) {
                String[] names = params.names().split(";");
                String value = "";
                for (String key : names) {
                    if (!"".equals(key)) {
                        value = params.var(key);
                        result.put(key, value);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new JobSchedulerException(JSJ_F_0060.params(e.toString()), e);
        }
    }
    
    private void doProcessing() throws Exception {
        
        Variable_set params = spooler.create_variable_set();
        params.merge(spooler_task.params());
        params.merge(spooler_task.order().params());
        
        JobSchedulerSynchronizeJobChains jobSchedulerSynchronizeJobChains = new JobSchedulerSynchronizeJobChains();
        JobSchedulerSynchronizeJobChainsOptions jobSchedulerSynchronizeJobChainsOptions = new JobSchedulerSynchronizeJobChainsOptions();
      
        jobSchedulerSynchronizeJobChainsOptions.setback_count.setValue(params.value("setback_count"));
        jobSchedulerSynchronizeJobChainsOptions.setback_interval.setValue(params.value("setback_interval"));
        jobSchedulerSynchronizeJobChainsOptions.setback_type.setValue(params.value("setback_type"));
        jobSchedulerSynchronizeJobChainsOptions.ignore_stopped_jobchains.setValue(params.value("ignore_stopped_jobchains"));;
        jobSchedulerSynchronizeJobChainsOptions.job_chain_name2synchronize.setValue(params.value("job_chain_name2synchronize"));
        jobSchedulerSynchronizeJobChainsOptions.job_chain_state2synchronize.setValue(params.value("job_chain_state2synchronize"));
        jobSchedulerSynchronizeJobChainsOptions.job_chain_required_orders.setValue(params.value("job_chain_required_orders"));
        jobSchedulerSynchronizeJobChainsOptions.job_chain_state_required_orders.setValue(params.value("job_chain_state_required_orders"));
        jobSchedulerSynchronizeJobChainsOptions.required_orders.setValue(params.value("required_orders"));
        jobSchedulerSynchronizeJobChainsOptions.sync_session_id.setValue(params.value("sync_session_id"));
        jobSchedulerSynchronizeJobChainsOptions.jobpath.setValue(params.value("sync_session_id"));
        jobSchedulerSynchronizeJobChainsOptions.disable_sync_context.setValue(params.value("disable_sync_context"));
        
        setSetback(jobSchedulerSynchronizeJobChainsOptions);
        jobSchedulerSynchronizeJobChainsOptions.checkMandatory();
        String jobName = spooler_task.job().name();
        jobSchedulerSynchronizeJobChainsOptions.jobpath.setValue(jobName);
        String answer = spooler.execute_xml(COMMAND_SHOW_JOB_CHAIN_FOLDERS);
        jobSchedulerSynchronizeJobChainsOptions.jobchains_answer.setValue(answer);
        answer = spooler.execute_xml(String.format(COMMAND_SHOW_JOB, jobName));
        jobSchedulerSynchronizeJobChainsOptions.orders_answer.setValue(answer);
        LOGGER.debug("Checking option ignore_stopped_jobchain");
        if (!jobSchedulerSynchronizeJobChainsOptions.ignore_stopped_jobchains.isDirty()) {
            LOGGER.debug(String.format("Value of %s=%s", jobSchedulerSynchronizeJobChainsOptions.ignore_stopped_jobchains.getShortKey(), spooler.var(jobSchedulerSynchronizeJobChainsOptions.ignore_stopped_jobchains
                    .getShortKey())));
            if (spooler.var(jobSchedulerSynchronizeJobChainsOptions.ignore_stopped_jobchains.getShortKey()) != null && !spooler.var(jobSchedulerSynchronizeJobChainsOptions.ignore_stopped_jobchains.getShortKey()).trim()
                    .isEmpty()) {
                LOGGER.debug(String.format("set ignore_stopped_jobchains=%s from scheduler-variables", spooler.var(jobSchedulerSynchronizeJobChainsOptions.ignore_stopped_jobchains
                        .getShortKey())));
                jobSchedulerSynchronizeJobChainsOptions.ignore_stopped_jobchains.setValue(spooler.var(jobSchedulerSynchronizeJobChainsOptions.ignore_stopped_jobchains.getShortKey()));
            }
        } else {
            LOGGER.debug(String.format("set ignore_stopped_jobchains=%s from param", jobSchedulerSynchronizeJobChainsOptions.ignore_stopped_jobchains.getValue()));
        }

        jobSchedulerSynchronizeJobChainsOptions.jobpath.setValue("/" + spooler_task.job().name());
 
        jobSchedulerSynchronizeJobChains.setOrderId(spooler_task.order().id());
        jobSchedulerSynchronizeJobChains.setJobChain(spooler_task.order().job_chain().path());
         
        jobSchedulerSynchronizeJobChains.schedulerParameters = convertVariableSet2HashMap(params);
        jobSchedulerSynchronizeJobChains.execute(jobSchedulerSynchronizeJobChainsOptions);
        if (jobSchedulerSynchronizeJobChains.syncNodeContainer.isReleased()) {
            while (!jobSchedulerSynchronizeJobChains.syncNodeContainer.eof()) {
                SyncNode objSyncNode = jobSchedulerSynchronizeJobChains.syncNodeContainer.getNextSyncNode();
                Job_chain objJobChain = spooler.job_chain(objSyncNode.getSyncNodeJobchainPath());
                Job_chain_node objCurrentNode = objJobChain.node(objSyncNode.getSyncNodeState());
                List<SyncNodeWaitingOrder> lstWaitingOrders = objSyncNode.getSyncNodeWaitingOrderList();
                for (SyncNodeWaitingOrder objWaitingOrder : lstWaitingOrders) {
                    String strEndState = objWaitingOrder.getEndState();
                    LOGGER.debug(String.format("Release jobchain=%s order=%s at state %s, endstate=%s", objSyncNode.getSyncNodeJobchainPath(),
                            objWaitingOrder.getId(), objSyncNode.getSyncNodeState(), strEndState));
                    Job_chain_node next_n = objCurrentNode.next_node();
                    String strNextState = objCurrentNode.next_state();
                    if (objCurrentNode.state().equalsIgnoreCase(strEndState)) {
                        strNextState = objCurrentNode.state();
                    }
                    if (!strEndState.isEmpty()) {
                        strEndState = " end_state='" + strEndState + "' ";
                    }
                    String strJSCommand = "";
                    if (jobSchedulerSynchronizeJobChainsOptions.setback_type.getValue().equalsIgnoreCase(SYNC_METHOD_SETBACK)) {
                        strJSCommand = String.format("<modify_order job_chain='%s' order='%s' setback='no'>"
                                + "<params><param name='scheduler_sync_ready' " + "value='true'></param></params></modify_order>", objSyncNode
                                        .getSyncNodeJobchainPath(), objWaitingOrder.getId());
                    } else {
                        if (next_n.job() == null || strNextState.equals(objCurrentNode.state())) {
                            strJSCommand = String.format("<modify_order job_chain='%s' order='%s' suspended='no' %s >"
                                    + "<params><param name='scheduler_sync_ready' " + "value='true'></param></params></modify_order>", objSyncNode
                                            .getSyncNodeJobchainPath(), objWaitingOrder.getId(), strEndState);
                        } else {
                            strJSCommand = String.format("<modify_order job_chain='%s' order='%s' state='%s' suspended='no' %s />", objSyncNode
                                    .getSyncNodeJobchainPath(), objWaitingOrder.getId(), strNextState, strEndState);
                        }
                    }
                    LOGGER.debug(strJSCommand);
                    answer = spooler.execute_xml(strJSCommand);
                    LOGGER.debug(answer);
                }
            }
        } else {
            SyncNode sn = jobSchedulerSynchronizeJobChains.syncNodeContainer.getNode(spooler_task.order().job_chain().name(), spooler_task.order().state());
            String stateText = "";
            if (sn != null) {
                stateText = String.format("%s required: %s, waiting: %s", jobSchedulerSynchronizeJobChains.syncNodeContainer.getShortSyncNodeContext(), sn.getRequired(), sn
                        .getSyncNodeWaitingOrderList().size());
                if (sn.isReleased()) {
                    SyncNode notReleased = jobSchedulerSynchronizeJobChains.syncNodeContainer.getFirstNotReleasedNode();
                    String etc = "";
                    if (jobSchedulerSynchronizeJobChains.syncNodeContainer.getNumberOfWaitingNodes() > 1) {
                        etc = "...";
                    }
                    if (notReleased != null) {
                        stateText = String.format("%s --> released. waiting for %s/%s %s", stateText, notReleased.getSyncNodeJobchainName(),
                                notReleased.getSyncNodeState(), etc);
                    } else {
                        stateText = String.format("%s --> released", stateText);
                    }
                }
            }
            LOGGER.debug("...stateText:" + stateText);
            this.setStateText(stateText);
            if (SYNC_METHOD_SETBACK.equalsIgnoreCase(jobSchedulerSynchronizeJobChainsOptions.setback_type.getValue())) {
                spooler_task.order().setback();
            } else {
                if (!spooler_task.order().suspended()) {
                    spooler_task.order().set_state(spooler_task.order().state());
                    spooler_task.order().set_suspended(true);
                }
            }
        }
    }

}