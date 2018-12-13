package com.sos.jitl.notification.model.result;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.notification.db.DBItemSchedulerMonNotifications;
import com.sos.jitl.notification.db.DBItemSchedulerMonResults;
import com.sos.jitl.notification.db.DBLayer;
import com.sos.jitl.notification.jobs.result.StoreResultsJobOptions;
import com.sos.jitl.notification.model.INotificationModel;
import com.sos.jitl.notification.model.NotificationModel;

import sos.util.SOSString;

public class StoreResultsModel extends NotificationModel implements INotificationModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreResultsModel.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private StoreResultsJobOptions options;

    public StoreResultsModel(SOSHibernateSession sess, StoreResultsJobOptions opt) throws Exception {
        super(sess);
        options = opt;
    }

    @Override
    public void process() throws Exception {
        ArrayList<String> resultParamsAsList = getResultParamsAsArrayList(options.scheduler_notification_result_parameters.getValue());
        boolean hasResultParams = !resultParamsAsList.isEmpty();
        HashMap<String, String> hm = options.settings();
        HashMap<String, String> hmInsert = new HashMap<String, String>();
        if (hm != null) {
            for (String name : hm.keySet()) {
                if (!hasResultParams || resultParamsAsList.contains(name)) {
                    hmInsert.put(name, hm.get(name));
                }
            }
        }

        LOGGER.info(String.format("[process]params=%s", hmInsert.size()));

        if (!hmInsert.isEmpty()) {
            try {
                getDbLayer().getSession().beginTransaction();
                DBItemSchedulerMonNotifications n = getNotification();
                getDbLayer().getSession().commit();

                getDbLayer().getSession().beginTransaction();
                for (String name : hmInsert.keySet()) {
                    insertParam(n.getId(), name, hmInsert.get(name));
                }
                getDbLayer().getSession().commit();
            } catch (Exception ex) {
                try {
                    getDbLayer().getSession().rollback();
                } catch (Exception x) {
                    // no exception handling for rollback
                }
                throw ex;
            }
        }
    }

    private DBItemSchedulerMonNotifications getNotification() throws Exception {
        String method = "getNotification";

        DBItemSchedulerMonNotifications dbItem = null;
        DBItemSchedulerMonNotifications tmp = new DBItemSchedulerMonNotifications();
        tmp.setSchedulerId(options.mon_results_scheduler_id.getValue());
        tmp.setStandalone(options.mon_results_standalone.value());
        tmp.setTaskId(new Long(options.mon_results_task_id.value()));
        tmp.setOrderStepState(options.mon_results_order_step_state.getValue());
        tmp.setJobChainName(options.mon_results_job_chain_name.getValue());
        tmp.setOrderId(options.mon_results_order_id.getValue());
        tmp.setOrderHistoryId(new Long(options.mon_results_order_history_id.value()));

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][tmp]%s", method, NotificationModel.toString(tmp)));
        }

        List<DBItemSchedulerMonNotifications> dbItems = getDbLayer().getNotificationsByState(tmp.getSchedulerId(), tmp.getStandalone(), tmp
                .getTaskId(), tmp.getOrderHistoryId(), tmp.getOrderStepState());

        if (dbItems == null || dbItems.size() == 0) {
            // JobScheduler versions before 1.11
            tmp.setStep(DBLayer.NOTIFICATION_DUMMY_MAX_STEP);
            // tmp.setOrderStartTime(os.getOrderStartTime());
            // tmp.setOrderEndTime(os.getOrderEndTime());
            // tmp.setOrderStepStartTime(os.getStepStartTime());
            // tmp.setOrderStepEndTime(os.getStepEndTime());
            tmp.setJobName("dummy");
            tmp.setTaskStartTime(new Date());

            dbItem = getDbLayer().createNotification(tmp.getSchedulerId(), tmp.getStandalone(), tmp.getTaskId(), tmp.getStep(), tmp
                    .getOrderHistoryId(), tmp.getJobChainName(), tmp.getJobChainName(), tmp.getOrderId(), tmp.getOrderId(), tmp.getOrderStartTime(),
                    tmp.getOrderEndTime(), tmp.getOrderStepState(), tmp.getOrderStepStartTime(), tmp.getOrderStepEndTime(), tmp.getJobName(), tmp
                            .getJobName(), tmp.getTaskStartTime(), tmp.getTaskEndTime(), tmp.getError(), tmp.getReturnCode(), tmp.getAgentUrl(), tmp
                                    .getClusterMemberId(), tmp.getError(), tmp.getErrorCode(), tmp.getErrorText());
            getDbLayer().getSession().save(dbItem);

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][created]%s", method, NotificationModel.toString(dbItem)));
            }
        } else {
            dbItem = dbItems.get(0);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][found=%s]%s", method, dbItems.size(), NotificationModel.toString(dbItem)));
            }
        }
        return dbItem;
    }

    private DBItemSchedulerMonResults insertParam(Long notificationId, String name, String value) throws Exception {
        DBItemSchedulerMonResults dbItem = getDbLayer().createResult(notificationId, name, value);
        getDbLayer().getSession().save(dbItem);

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[insertParam]%s", NotificationModel.toString(dbItem)));
        }
        return dbItem;
    }

    private ArrayList<String> getResultParamsAsArrayList(String params) {
        ArrayList<String> list = new ArrayList<String>();
        if (!SOSString.isEmpty(params)) {
            String[] arr = params.split(";");
            for (int i = 0; i < arr.length; i++) {
                String val = arr[i].trim();
                if (!val.isEmpty()) {
                    list.add(val);
                }
            }
        }
        return list;
    }

}