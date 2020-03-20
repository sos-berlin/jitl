package sos.scheduler.file;

import static com.sos.scheduler.messages.JSMessages.JSJ_E_0020;
import static com.sos.scheduler.messages.JSMessages.JSJ_E_0041;
import static com.sos.scheduler.messages.JSMessages.JSJ_E_0120;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_0017;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_0018;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_0019;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_0090;

import java.io.File;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Variable_set;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** @author KB */
public class JSExistsFileJSAdapterClass extends JobSchedulerJobAdapter {

    private static final String SCHEDULER_FILE_PATH = "scheduler_file_path";
    private static final Logger LOGGER = LoggerFactory.getLogger(JSExistsFileJSAdapterClass.class);
    private JSExistsFileOptions objO = null;

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
            super.spooler_process();
            return doProcessing();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new JobSchedulerException(e);
        }
    }

    @Override
    public void spooler_exit() {
        super.spooler_exit();
    }

    private boolean doProcessing() throws Exception {
        JSExistsFile objR = new JSExistsFile();
        objO = objR.getOptions();
        objO.setAllOptions(getSchedulerParameterAsProperties(getSpoolerProcess().getOrder()));
        if (!objO.file_spec.isDirty() && !objO.file.isDirty()) {
            String filename = spooler_task.order().params().value(SCHEDULER_FILE_PATH);
            if (filename != null && !filename.isEmpty()) {
                File file = new File(filename);
                objO.file_spec.setValue("^" + file.getName().replaceAll(".", "\\.") + "$");
                objO.file.setValue(file.getParent());
            }
        }
        objR.setJSJobUtilites(this);
        boolean flgResult = objR.Execute();
        Vector<File> lstResultList = objR.getResultList();
        int intNoOfHitsInResultSet = lstResultList.size();
        String strOrderJobChainName = null;
        boolean flgCreateOrders4AllFiles = false;
        boolean count_files = objO.count_files.value();
        if (spooler_task.order() != null) {
            Variable_set orderParams = spooler_task.order().params();
            if (orderParams != null) {
                if (count_files) {
                    orderParams.set_var(objO.count_files.getKey(), String.valueOf(intNoOfHitsInResultSet));
                }

                String strT = "";
                for (File objFile : lstResultList) {
                    strT += objFile.getAbsolutePath() + ";";
                }
                orderParams.set_var(objO.scheduler_sosfileoperations_resultset.getKey(), strT);
                orderParams.set_var(objO.scheduler_sosfileoperations_resultsetsize.getKey(), String.valueOf(intNoOfHitsInResultSet));
            }
            String strOnEmptyResultSet = objO.on_empty_result_set.getValue();
            if (isNotEmpty(strOnEmptyResultSet) && intNoOfHitsInResultSet <= 0) {
                LOGGER.info(JSJ_I_0090.params(strOnEmptyResultSet));
                spooler_task.order().set_state(strOnEmptyResultSet);
            }
        } else {
            if (count_files) {
                LOGGER.warn(JSJ_E_0120.params(objO.count_files.getKey()));
            }
        }
        if (flgResult) {
            flgCreateOrders4AllFiles = objO.create_orders_for_all_files.value();
            boolean flgCreateOrder = objO.create_order.value();
            if (flgCreateOrder == true && intNoOfHitsInResultSet > 0) {
                strOrderJobChainName = objO.order_jobchain_name.getValue();
                if (isNull(strOrderJobChainName)) {
                    throw new JobSchedulerException(JSJ_E_0020.params(objO.order_jobchain_name.getKey()));
                }
                if (!spooler.job_chain_exists(strOrderJobChainName)) {
                    throw new JobSchedulerException(JSJ_E_0041.params(strOrderJobChainName));
                }
                for (File objFile : lstResultList) {
                    createOrder(objFile.getAbsolutePath(), strOrderJobChainName);
                    if (!flgCreateOrders4AllFiles) {
                        break;
                    }
                }
            }
        }
        return setReturnResult(flgResult);
    }

    private void createOrder(final String pstrOrder4FileName, final String pstrOrderJobChainName) {
        final String methodName = "JSExistsFileJSAdapterClass::createOrder";
        Order objOrder = spooler.create_order();
        Variable_set objOrderParams = spooler.create_variable_set();
        objOrderParams.set_value(objO.scheduler_file_path.getKey(), pstrOrder4FileName);
        objOrderParams.set_value(objO.scheduler_file_parent.getKey(), new File(pstrOrder4FileName).getParent());
        objOrderParams.set_value(objO.scheduler_file_name.getKey(), new File(pstrOrder4FileName).getName());
        String strNextState = objO.next_state.getValue();
        if (isNotEmpty(strNextState)) {
            objOrder.set_state(strNextState);
        }
        objOrder.set_params(objOrderParams);
        objOrder.set_id(pstrOrder4FileName);
        objOrder.set_title(JSJ_I_0017.params(methodName));
        Job_chain objJobchain = spooler.job_chain(pstrOrderJobChainName);
        objJobchain.add_order(objOrder);
        String strT = JSJ_I_0018.params(pstrOrder4FileName, pstrOrderJobChainName);
        if (isNotEmpty(strNextState)) {
            strT += " " + JSJ_I_0019.params(strNextState);
        }
        LOGGER.info(strT);
    }

    public boolean setReturnResult(final boolean pflgResult) {
        boolean rc1 = pflgResult;
        if (!rc1 && objO.gracious.isGraciousAll()) {
            return getSpoolerProcess().isOrderJob();
        } else {
            if (!rc1 && objO.gracious.isGraciousTrue()) {
                return false;
            } else {
                if (rc1) {
                    return getSpoolerProcess().isOrderJob();
                } else {
                    return false;
                }
            }
        }
    }

}
