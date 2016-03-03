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

public class JobSchedulerSynchronizeJobChainsJSAdapterClass extends JobSchedulerJobAdapter {

    private static final String SYNC_METHOD_SETBACK = "setback";
    private static final String PARAMETER_SCHEDULER_SYNC_READY = "scheduler_sync_ready";
    private static final String COMMAND_SHOW_JOB = "<show_job job=\"%s\" max_task_history=\"0\" what=\"job_orders job_chains payload\"/>";
    private static final String COMMAND_SHOW_JOB_CHAIN_FOLDERS = "<show_state max_order_history=\"0\" max_orders=\"0\" what=\"job_chains folders\" "
            + "subsystems=\"folder order\"/>";
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerSynchronizeJobChainsJSAdapterClass.class);

    public void init() {
        doInitialize();
    }

    private void doInitialize() {
        // doInitialize
    } 

    @Override
    public boolean spooler_init() {
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() throws Exception {
        try {
            spooler_log.debug3("--->" + spooler_task.order().params().value("scheduler_file_path"));
            if (!"".equals(spooler_task.order().params().value("scheduler_file_path"))) {
                spooler_log.debug3("---> id" + spooler_task.order().id());
                if (!spooler_task.order().id().equals(spooler_task.order().params().value("scheduler_file_path"))) {
                    spooler_log.debug3("---> setze scheduler_file_path");
                    spooler_task.order().params().set_var("scheduler_file_path", "");
                }
            }
            super.spooler_process();
            boolean syncReady = false;
            if (spooler_task.order().params().value(PARAMETER_SCHEDULER_SYNC_READY) != null) {
                syncReady = "true".equals(spooler_task.order().params().value(PARAMETER_SCHEDULER_SYNC_READY));
            }
            if (syncReady) {
                spooler_log.debug("js-461: Sync skipped");
                Order o = spooler_task.order();
                Variable_set resultParameters = spooler.create_variable_set();
                String[] parameterNames = o.params().names().split(";");
                for (int i = 0; i < parameterNames.length; i++) {
                    if (!PARAMETER_SCHEDULER_SYNC_READY.equals(parameterNames[i])) {
                        resultParameters.set_var(parameterNames[i], o.params().value(parameterNames[i]));
                    }
                }
                o.set_params(resultParameters);
                return signalSuccess();
            }
            doProcessing();
        } catch (Exception e) {
            throw new JobSchedulerException("--- Fatal Error: " + e.getMessage(), e);
        }
        return signalSuccess();
    }

    @Override
    public void spooler_exit() {
        super.spooler_exit();
    }

    private void setSetback(JobSchedulerSynchronizeJobChainsOptions objO) {
        spooler_log.debug3(".............");
        if (objO.setback_type.Value().equalsIgnoreCase(SYNC_METHOD_SETBACK)) {
            spooler_log.debug3("Setting setback parameters");
            if (spooler_job.setback_max() <= 0) {
                spooler_log.debug3(String.format("Setting setback_interval=%s", objO.setback_interval.value()));
                spooler_job.set_delay_order_after_setback(1, objO.setback_interval.value());
                if (objO.setback_count.value() > 0) {
                    spooler_log.debug3(String.format("Setting setback_count=%s", objO.setback_count.value()));
                    spooler_job.set_max_order_setbacks(objO.setback_count.value());
                }
            }
        }
    }

    private void doProcessing() throws Exception {
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
        String answer = spooler.execute_xml(COMMAND_SHOW_JOB_CHAIN_FOLDERS);
        objO.jobchains_answer.Value(answer);
        answer = spooler.execute_xml(String.format(COMMAND_SHOW_JOB, jobName));
        objO.orders_answer.Value(answer);
        LOGGER.debug("Checking option ignore_stopped_jobchain");
        if (!objO.ignore_stopped_jobchains.isDirty()) {
            LOGGER.debug(String.format("Value of %s=%s", objO.ignore_stopped_jobchains.getShortKey(), spooler.var(objO.ignore_stopped_jobchains.getShortKey())));
            if (spooler.var(objO.ignore_stopped_jobchains.getShortKey()) != null
                    && !spooler.var(objO.ignore_stopped_jobchains.getShortKey()).trim().isEmpty()) {
                LOGGER.debug(String.format("set ignore_stopped_jobchains=%s from scheduler-variables", spooler.var(objO.ignore_stopped_jobchains.getShortKey())));
                objO.ignore_stopped_jobchains.Value(spooler.var(objO.ignore_stopped_jobchains.getShortKey()));
            }
        } else {
            LOGGER.debug(String.format("set ignore_stopped_jobchains=%s from param", objO.ignore_stopped_jobchains.Value()));
        }
        IJSCommands objJSCommands = this;
        Object objSp = objJSCommands.getSpoolerObject();
        Spooler objSpooler = (Spooler) objSp;
        objO.jobpath.Value("/" + spooler_task.job().name());
        for (final Map.Entry<String, String> element : SchedulerParameters.entrySet()) {
            final String strMapKey = element.getKey().toString();
            String strTemp = "";
            if (element.getValue() != null) {
                strTemp = element.getValue().toString();
                if (strMapKey.contains("password")) {
                    strTemp = "***";
                }
            }
            LOGGER.debug("Key = " + strMapKey + " --> " + strTemp);
        }
        objR.setSchedulerParameters(SchedulerParameters);
        objR.setOrderId(spooler_task.order().id());
        objR.setJobChain((spooler_task.order().job_chain().path()));
        objR.Execute();
        if (objR.syncNodeContainer.isReleased()) {
            while (!objR.syncNodeContainer.eof()) {
                SyncNode objSyncNode = objR.syncNodeContainer.getNextSyncNode();
                Job_chain objJobChain = objSpooler.job_chain(objSyncNode.getSyncNodeJobchainPath());
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
                    if (objO.setback_type.Value().equalsIgnoreCase(SYNC_METHOD_SETBACK)) {
                        strJSCommand = String.format("<modify_order job_chain='%s' order='%s' setback='no'>"
                                + "<params><param name='scheduler_sync_ready' " + "value='true'></param></params></modify_order>", 
                                objSyncNode.getSyncNodeJobchainPath(), objWaitingOrder.getId());
                    } else {
                        if (next_n.job() == null || strNextState.equals(objCurrentNode.state())) {
                            strJSCommand = String.format("<modify_order job_chain='%s' order='%s' suspended='no' %s >"
                                    + "<params><param name='scheduler_sync_ready' " + "value='true'></param></params></modify_order>", 
                                    objSyncNode.getSyncNodeJobchainPath(), objWaitingOrder.getId(), strEndState);
                        } else {
                            strJSCommand = String.format("<modify_order job_chain='%s' order='%s' state='%s' suspended='no' %s />", 
                                    objSyncNode.getSyncNodeJobchainPath(), objWaitingOrder.getId(), strNextState, strEndState);
                        }
                    }
                    LOGGER.debug(strJSCommand);
                    answer = objSpooler.execute_xml(strJSCommand);
                    LOGGER.debug(answer);
                }
            }
        } else {
            SyncNode sn = objR.syncNodeContainer.getNode(spooler_task.order().job_chain().name(), spooler_task.order().state());
            String stateText = "";
            if (sn != null) {
                stateText = String.format("%s required: %s, waiting: %s", objR.syncNodeContainer.getShortSyncNodeContext(), sn.getRequired(), 
                        sn.getSyncNodeWaitingOrderList().size());
                if (sn.isReleased()) {
                    SyncNode notReleased = objR.syncNodeContainer.getFirstNotReleasedNode();
                    String etc = "";
                    if (objR.syncNodeContainer.getNumberOfWaitingNodes() > 1) {
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
            if (SYNC_METHOD_SETBACK.equalsIgnoreCase(objO.setback_type.Value())) {
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