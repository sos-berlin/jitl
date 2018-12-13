package com.sos.jitl.notification.model.notifier;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.notification.db.DBItemSchedulerMonChecks;
import com.sos.jitl.notification.db.DBItemSchedulerMonNotifications;
import com.sos.jitl.notification.db.DBItemSchedulerMonSystemNotifications;
import com.sos.jitl.notification.db.DBItemSchedulerMonSystemResults;
import com.sos.jitl.notification.db.DBLayer;
import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.helper.CounterSystemNotifier;
import com.sos.jitl.notification.helper.EServiceMessagePrefix;
import com.sos.jitl.notification.helper.EServiceStatus;
import com.sos.jitl.notification.helper.ElementNotificationJob;
import com.sos.jitl.notification.helper.ElementNotificationJobChain;
import com.sos.jitl.notification.helper.ElementNotificationMonitor;
import com.sos.jitl.notification.helper.ElementNotificationTimerRef;
import com.sos.jitl.notification.helper.JobChainNotification;
import com.sos.jitl.notification.helper.NotificationXmlHelper;
import com.sos.jitl.notification.jobs.notifier.SystemNotifierJobOptions;
import com.sos.jitl.notification.model.INotificationModel;
import com.sos.jitl.notification.model.NotificationModel;
import com.sos.jitl.notification.model.history.CheckHistoryModel;
import com.sos.jitl.notification.plugins.notifier.ISystemNotifierPlugin;

import sos.spooler.Spooler;
import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

public class SystemNotifierModel extends NotificationModel implements INotificationModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemNotifierModel.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final String THREE_PARAMS_LOGGING = "[%s][%s][%s]-%s";
    private static final String CALL_PLUGIN_LOGGING = "[%s][%s][%s][%s]notification %s of %s. call plugin %s";
    private static final String SENT_LOGGING = "[%s]sended=%s, error=%s, skipped=%s (total checked=%s)";
    private Spooler spooler;
    private SystemNotifierJobOptions options;
    private String systemId;
    private File systemFile;
    private ArrayList<ElementNotificationMonitor> monitors;
    private ArrayList<ElementNotificationJob> monitorJobs;
    private ArrayList<ElementNotificationJobChain> monitorJobChains;
    private ArrayList<ElementNotificationTimerRef> monitorOnErrorTimers;
    private ArrayList<ElementNotificationTimerRef> monitorOnSuccessTimers;
    private Optional<Integer> largeResultFetchSize = Optional.empty();
    private CounterSystemNotifier counter;
    private ArrayList<Long> handledByNotifyAgain;

    public SystemNotifierModel(SOSHibernateSession sess, SystemNotifierJobOptions opt, Spooler sp) throws Exception {
        super(sess);
        options = opt;
        spooler = sp;
        try {
            int fetchSize = options.large_result_fetch_size.value();
            if (fetchSize != -1) {
                largeResultFetchSize = Optional.of(fetchSize);
            }
        } catch (Exception ex) {
            // no exception handling
        }
    }

    private void initMonitorObjects() {
        monitors = new ArrayList<ElementNotificationMonitor>();
        monitorJobs = new ArrayList<ElementNotificationJob>();
        monitorJobChains = new ArrayList<ElementNotificationJobChain>();
        monitorOnErrorTimers = new ArrayList<ElementNotificationTimerRef>();
        monitorOnSuccessTimers = new ArrayList<ElementNotificationTimerRef>();
    }

    private void initSendCounters() {
        counter = new CounterSystemNotifier();
    }

    private boolean initConfig() throws Exception {
        String method = "initConfig";
        File schemaFile = new File(options.schema_configuration_file.getValue());
        if (!schemaFile.exists()) {
            throw new Exception(String.format("[%s][schema file not found]%s", method, schemaFile.getCanonicalPath()));
        }
        systemFile = new File(this.options.system_configuration_file.getValue());
        if (!systemFile.exists()) {
            throw new Exception(String.format("[%s][system configuration file not found]%s", method, systemFile.getCanonicalPath()));
        }
        SOSXMLXPath xpath = new SOSXMLXPath(systemFile.getCanonicalPath());
        initMonitorObjects();
        systemId = NotificationXmlHelper.getSystemMonitorNotificationSystemId(xpath);
        if (SOSString.isEmpty(systemId)) {
            throw new Exception(String.format("systemId is NULL (configured SystemMonitorNotification/@system_id is not found)"));
        }

        NodeList monitorList = NotificationXmlHelper.selectNotificationMonitorDefinitions(xpath);
        int valide = setMonitorObjects(xpath, monitorList);

        int jobChains = monitorJobChains.size();
        int jobs = monitorJobs.size();
        int timersOnError = monitorOnErrorTimers.size();
        int timersOnSuccess = monitorOnSuccessTimers.size();
        int total = jobChains + jobs + timersOnError + timersOnSuccess;
        String msg = String.format("[%s][NotificationMonitors=%s(valide=%s)][JobChains=%s][Jobs=%s][TimerRefs onError=%s, onSuccess=%s]", method,
                monitorList.getLength(), valide, jobChains, jobs, timersOnError, timersOnSuccess);
        if (total > 0) {
            LOGGER.info(msg);
        } else {
            LOGGER.warn(msg);
        }
        return total > 0;
    }

    private int setMonitorObjects(SOSXMLXPath xpath, NodeList monitorList) throws Exception {
        int counter = 0;
        for (int i = 0; i < monitorList.getLength(); i++) {
            Node n = monitorList.item(i);
            ElementNotificationMonitor monitor = new ElementNotificationMonitor(n, options);
            if (monitor.getMonitorInterface() == null) {
                LOGGER.warn(String.format(
                        "skip NotificationMonitor[service_name_on_error=%s, service_name_on_success=%s]: missing child Notification... element",
                        monitor.getServiceNameOnError(), monitor.getServiceNameOnSuccess()));
                continue;
            }
            counter++;

            monitors.add(monitor);

            NodeList objects = NotificationXmlHelper.selectNotificationMonitorNotificationObjects(xpath, n);
            for (int j = 0; j < objects.getLength(); j++) {
                Node object = objects.item(j);
                if ("Job".equalsIgnoreCase(object.getNodeName())) {
                    monitorJobs.add(new ElementNotificationJob(monitor, object));
                } else if ("JobChain".equalsIgnoreCase(object.getNodeName())) {
                    monitorJobChains.add(new ElementNotificationJobChain(monitor, object));
                } else if ("TimerRef".equalsIgnoreCase(object.getNodeName())) {
                    if (!SOSString.isEmpty(monitor.getServiceNameOnError())) {
                        monitorOnErrorTimers.add(new ElementNotificationTimerRef(monitor, object));
                    }
                    if (!SOSString.isEmpty(monitor.getServiceNameOnSuccess())) {
                        monitorOnSuccessTimers.add(new ElementNotificationTimerRef(monitor, object));
                    }
                }
            }
        }
        return counter;
    }

    private void executeNotifyTimer(int currentCounter, String systemId, DBItemSchedulerMonChecks check, ElementNotificationTimerRef timer,
            boolean isNotifyOnErrorService) throws Exception {
        // Output indent
        String method = "    [" + currentCounter + "][executeNotifyTimer]";
        String serviceName = (isNotifyOnErrorService) ? timer.getMonitor().getServiceNameOnError() : timer.getMonitor().getServiceNameOnSuccess();
        EServiceStatus pluginStatus = (isNotifyOnErrorService) ? EServiceStatus.CRITICAL : EServiceStatus.OK;

        DBItemSchedulerMonNotifications notification = getDbLayer().getNotification(check.getNotificationId());
        if (notification == null) {
            counter.addSkip();
            LOGGER.info(String.format("%s[%s][skip]notification id=%s not found", method, serviceName, check.getNotificationId()));
            return;
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s %s", method, NotificationModel.toString(notification)));
        }

        boolean checkSmOnSuccess = false; // timer always error
        String stepFrom = check.getStepFrom();
        String stepTo = check.getStepTo();
        String returnCodeFrom = null;
        String returnCodeTo = null;
        Long notifications = timer.getNotifications();
        if (notifications < 1) {
            counter.addSkip();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[%s][skip]notifications %s < 1", method, serviceName, notifications));
            }
            return;
        }

        DBItemSchedulerMonSystemNotifications sn = getDbLayer().getSystemNotification(systemId, serviceName, notification.getId(), check.getId(),
                check.getObjectType(), checkSmOnSuccess, stepFrom, stepTo, returnCodeFrom, returnCodeTo);
        boolean isNew = false;
        if (sn == null) {
            isNew = true;
            Date startTime = notification.getOrderStartTime();
            Date endTime = notification.getOrderEndTime();
            if (notification.getStandalone()) {
                startTime = notification.getTaskStartTime();
                endTime = notification.getTaskEndTime();
            }
            try {
                getDbLayer().getSession().beginTransaction();
                sn = getDbLayer().createSystemNotification(systemId, serviceName, notification.getId(), check.getId(), returnCodeFrom, returnCodeTo,
                        check.getObjectType(), stepFrom, stepTo, startTime, endTime, new Long(0), notifications, false, false, checkSmOnSuccess);
                getDbLayer().getSession().save(sn);
                getDbLayer().getSession().commit();
            } catch (Exception ex) {
                try {
                    getDbLayer().getSession().rollback();
                } catch (Exception exx) {
                }
                throw ex;
            }
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[notify_on_error=%s][isNew=%s]%s", method, timer.getNotifyOnError(), isNew, NotificationModel.toString(
                    sn)));
        }

        if (sn.getMaxNotifications()) {
            counter.addSkip();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[skip]maxNotifications=true", method));
            }
            return;
        }
        if (sn.getAcknowledged()) {
            counter.addSkip();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[skip]acknowledged=true", method));
            }
            return;
        }
        if (sn.getCurrentNotification() >= notifications) {
            closeSystemNotification(sn, notification.getOrderEndTime());
            counter.addSkip();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[skip][close][%s]count notifications was reached", method, notifications));
            }
            return;
        }

        if (!timer.getNotifyOnError()) {
            if (check.getObjectType().equals(DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN)) {
                List<DBItemSchedulerMonSystemResults> sendedErrorNotifications = getDbLayer().getSystemResults(notification, systemId);
                if (sendedErrorNotifications != null && sendedErrorNotifications.size() > 0) {
                    closeSystemNotification(sn, notification.getOrderEndTime());
                    counter.addSkip();
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[notify_on_error=false][skip][close]following errors were sent:", method));
                        int e = 0;
                        for (DBItemSchedulerMonSystemResults r : sendedErrorNotifications) {
                            e++;
                            LOGGER.debug(String.format("%s   [%s]%s", method, e, NotificationModel.toString(r)));
                        }
                    }
                    return;
                }
            } else if (check.getObjectType().equals(DBLayer.NOTIFICATION_OBJECT_TYPE_JOB)) {
                if (notification.getError()) {
                    closeSystemNotification(sn, notification.getOrderEndTime());
                    counter.addSkip();
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[notify_on_error=false][skip][close]notification has an error", method));
                    }
                    return;
                }
            }
        }

        ISystemNotifierPlugin pl = getOrCreatePluginObject(timer.getMonitor(), method, serviceName);
        if (pl == null) {
            return;
        }

        try {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[before]%s", method, NotificationModel.toString(sn)));
            }

            sn.setCurrentNotification(sn.getCurrentNotification() + 1);
            if (sn.getCurrentNotification() >= notifications || sn.getAcknowledged()) {
                sn.setMaxNotifications(true);
            }
            sn.setSuccess(checkSmOnSuccess);
            sn.setModified(DBLayer.getCurrentDateTime());
            sn.setNotifications(notifications);

            String name = sn.getObjectType().equals(DBLayer.NOTIFICATION_OBJECT_TYPE_JOB) ? "Job " + notification.getJobName() : notification
                    .getJobChainName();
            LOGGER.info("----------------------------------------------------------------");
            LOGGER.info(String.format(CALL_PLUGIN_LOGGING, method, "notifyOnTimer", serviceName, name, sn.getCurrentNotification(), sn
                    .getNotifications(), pl.getClass().getSimpleName()));
            pl.notifySystem(getSpooler(), options, getDbLayer(), notification, sn, check, pluginStatus, EServiceMessagePrefix.TIMER);
            getDbLayer().getSession().beginTransaction();
            getDbLayer().getSession().update(sn);
            getDbLayer().getSession().commit();
            counter.addSuccess();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[after]%s", method, NotificationModel.toString(sn)));
            }
            LOGGER.info("----------------------------------------------------------------");
        } catch (Exception ex) {
            try {
                getDbLayer().getSession().rollback();
            } catch (Exception e) {
                // no exception handling for rollback
            }
            LOGGER.warn(String.format(THREE_PARAMS_LOGGING, method, "notifyOnTimer", serviceName, ex.getMessage()));
            counter.addError();
        }
    }

    private void closeSystemNotification(DBItemSchedulerMonSystemNotifications sm, Date stepToEndTime) throws Exception {
        sm.setStepToEndTime(stepToEndTime);
        sm.setMaxNotifications(true);
        sm.setModified(DBLayer.getCurrentDateTime());
        try {
            getDbLayer().getSession().beginTransaction();
            getDbLayer().getSession().update(sm);
            getDbLayer().getSession().commit();
        } catch (Exception ex) {
            try {
                getDbLayer().getSession().rollback();
            } catch (Exception exx) {
            }
            throw ex;
        }
    }

    private boolean checkDoNotifyByReturnCodes(DBItemSchedulerMonNotifications notification, String serviceName, String notifyMsg,
            String configuredName, String configuredReturnCodeFrom, String configuredReturnCodeTo) {
        String method = "checkDoNotifyByReturnCodes";

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s][%s]%s", method, notifyMsg, serviceName, NotificationModel.toString(notification)));
        }

        if (notification.getStandalone()) {
            if (notification.getTaskEndTime() == null) {
                counter.addSkip();

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][skip][standalone]task is not completed - task end time is empty", method, notifyMsg,
                            serviceName));
                }
                return false;
            }
        } else {
            if (notification.getOrderStepEndTime() == null) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][skip][order]step is not completed - step end time is empty", method, notifyMsg,
                            serviceName));
                }
                return false;
            }
        }

        if (!configuredReturnCodeFrom.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            try {
                Long rc = Long.parseLong(configuredReturnCodeFrom);
                if (notification.getReturnCode() < rc) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][%s][skip]return code (%s) less than configured return_code_from (%s)", method, notifyMsg,
                                serviceName, notification.getReturnCode(), configuredReturnCodeFrom));
                    }
                    return false;
                }
            } catch (Exception ex) {
                LOGGER.warn(String.format("[%s][%s][%s][%s][skip][configured return_code_from \"%s\" is not a valid integer value]%s", method,
                        notifyMsg, serviceName, configuredName, configuredReturnCodeFrom, ex.getMessage()));
                return false;
            }
        }
        if (!configuredReturnCodeTo.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            try {
                Long rc = Long.parseLong(configuredReturnCodeTo);
                if (notification.getReturnCode() > rc) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][%s][skip]return code (%s) greater than configured return_code_to (%s)", method,
                                notifyMsg, serviceName, notification.getReturnCode(), configuredReturnCodeTo));
                    }
                    return false;
                }
            } catch (Exception ex) {
                LOGGER.warn(String.format("[%s][%s][%s][%s][skip][configured return_code_to \"%s\" is not a valid integer value]%s", method,
                        notifyMsg, serviceName, configuredName, configuredReturnCodeTo, ex.getMessage()));
                return false;
            }
        }

        return true;
    }

    private boolean checkDoNotifyTimer(int currentCounter, DBItemSchedulerMonChecks check, ElementNotificationTimerRef timer) {
        String method = "  [" + currentCounter + "][checkDoNotifyTimer]";
        boolean notify = true;
        String ref = timer.getRef();
        if (!check.getName().equals(ref)) {
            notify = false;
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[notify=%s]%s", method, notify, ref));
        }
        return notify;
    }

    private boolean checkDoNotify(int currentCounter, DBItemSchedulerMonNotifications notification, ElementNotificationJobChain jc) throws Exception {
        String method = "  [" + currentCounter + "][checkDoNotify]";
        boolean notify = true;
        String schedulerId = jc.getSchedulerId();
        String jobChainName = jc.getName();
        if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            try {
                if (!notification.getSchedulerId().matches(schedulerId)) {
                    notify = false;

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[notify=%s][schedulerId not match][%s][%s]", method, notify, notification.getSchedulerId(),
                                schedulerId));
                    }

                }
            } catch (Exception ex) {
                throw new Exception(String.format("%s[check with configured scheduler_id=%s]%s", method, schedulerId, ex));
            }
        }
        if (notify && !jobChainName.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            jobChainName = CheckHistoryModel.normalizeRegex(jobChainName);
            try {
                if (!notification.getJobChainName().matches(jobChainName)) {
                    notify = false;

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[notify=%s][not match][%s][%s]", method, notify, notification.getJobChainName(), jobChainName));
                    }

                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[notify=%s][match][%s][%s][%s][%s]", method, notify, notification.getSchedulerId(), schedulerId,
                                notification.getJobChainName(), jobChainName));
                    }
                }
            } catch (Exception ex) {
                throw new Exception(String.format("%s[check with configured scheduler_id=%s, name=%s]%s", method, schedulerId, jobChainName, ex));
            }
        }
        return notify;
    }

    private boolean checkDoNotify(int currentCounter, DBItemSchedulerMonNotifications notification, ElementNotificationJob job) throws Exception {
        String method = "  [" + currentCounter + "][checkDoNotify]";
        boolean notify = true;
        String schedulerId = job.getSchedulerId();
        String jobName = job.getName();
        if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            try {
                if (!notification.getSchedulerId().matches(schedulerId)) {
                    notify = false;

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[notify=%s][schedulerId not match][%s][%s]", method, notify, notification.getSchedulerId(),
                                schedulerId));
                    }
                }
            } catch (Exception ex) {
                throw new Exception(String.format("%s[check with configured scheduler_id=%s]%s", method, schedulerId, ex));
            }
        }
        if (notify && !jobName.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            jobName = CheckHistoryModel.normalizeRegex(jobName);
            try {
                if (!notification.getJobName().matches(jobName)) {
                    notify = false;

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[notify=%s][not match][%s][%s]", method, notify, notification.getJobName(), jobName));
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[notify=%s][match][%s][%s][%s][%s]", method, notify, notification.getSchedulerId(), schedulerId,
                                notification.getJobName(), jobName));
                    }
                }
            } catch (Exception ex) {
                throw new Exception(String.format("%s[check with configured scheduler_id=%s, name=%s]%s", method, schedulerId, jobName, ex));
            }
        }
        return notify;
    }

    private JobChainNotification getJobChainNotification(int currentCounter, DBItemSchedulerMonNotifications notification,
            ElementNotificationJobChain jc) throws Exception {
        String method = "    [" + currentCounter + "][getJobChainNotification]";

        JobChainNotification jcn = new JobChainNotification();
        String stepFrom = jc.getStepFrom();
        String stepTo = jc.getStepTo();

        DBItemSchedulerMonNotifications stepFromNotification = null;
        DBItemSchedulerMonNotifications stepToNotification = null;
        // stepFrom, stepTo handling
        if (!stepFrom.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) || !stepTo.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s analyze configured step_from, step_to", method));
            }
            Long stepFromIndex = new Long(0);
            Long stepToIndex = new Long(0);
            List<DBItemSchedulerMonNotifications> steps = getDbLayer().getOrderNotifications(largeResultFetchSize, notification);
            if (steps == null || steps.isEmpty()) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[no steps found for orderHistoryId=%s]%s", method, notification.getOrderHistoryId(),
                            NotificationModel.toString(notification)));
                }
                throw new Exception(String.format("%sno steps found for orderHistoryId=%s", method, notification.getOrderHistoryId()));
            }
            for (DBItemSchedulerMonNotifications step : steps) {
                if (stepFrom != null && step.getOrderStepState().equalsIgnoreCase(stepFrom) && stepFromIndex.equals(new Long(0))) {
                    stepFromIndex = step.getStep();
                    stepFromNotification = step;
                }
                if (stepTo != null && step.getOrderStepState().equalsIgnoreCase(stepTo)) {
                    stepToIndex = step.getStep();
                    stepToNotification = step;
                }
                jcn.setLastStep(step);
            }
            if (stepToIndex.equals(new Long(0))) {
                stepToIndex = jcn.getLastStep().getStep();
            }
            jcn.setSteps(steps);
            jcn.setStepFromIndex(stepFromIndex);
            jcn.setStepToIndex(stepToIndex);
            jcn.setStepFrom(stepFromNotification);
            jcn.setStepTo(stepToNotification);

            if (stepFrom != null && !stepFrom.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && jcn.getStepFrom() == null) {
                jcn.setSteps(null);
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[setLastStepForNotification][configured step_from \"%s\" not found]%s", method, stepFrom,
                            NotificationModel.toString(notification)));
                }

            } else {
                for (DBItemSchedulerMonNotifications step : jcn.getSteps()) {
                    if (step.getStep() >= jcn.getStepFromIndex() && step.getStep() <= jcn.getStepToIndex()) {
                        jcn.setLastStepForNotification(step);
                    }
                }
            }

        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s getNotificationMaxStep", method));
            }

            jcn.setLastStepForNotification(getDbLayer().getNotificationMaxStep(notification));
            jcn.setStepFrom(notification);
            if (jcn.getLastStepForNotification() != null) {
                jcn.setStepTo(jcn.getLastStepForNotification());
                jcn.setStepToIndex(jcn.getStepTo().getStep());
            }
        }

        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[stepFromIndex=%s][stepToIndex=%s]", method, jcn.getStepFromIndex(), jcn.getStepToIndex()));
            LOGGER.debug(String.format("%s[stepFrom][%s]", method, NotificationModel.toString(jcn.getStepFrom())));
            LOGGER.debug(String.format("%s[stepTo][%s]", method, NotificationModel.toString(jcn.getStepTo())));
        }

        return jcn;
    }

    private void executeNotifyJob(int currentCounter, DBItemSchedulerMonSystemNotifications sm, String systemId,
            DBItemSchedulerMonNotifications notification, ElementNotificationJob job) throws Exception {
        String method = "    [" + currentCounter + "][executeNotifyJob]";
        String serviceNameOnError = job.getMonitor().getServiceNameOnError();
        String serviceNameOnSuccess = job.getMonitor().getServiceNameOnSuccess();

        if (job.getNotifications() < 1) {
            counter.addSkip();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][skip][notifications < 1]%s", method, NotificationModel.toString(job)));
            }
            return;
        }
        if (!SOSString.isEmpty(serviceNameOnError)) {
            executeNotifyJob(currentCounter, sm, systemId, notification, job, true);
        }
        if (!SOSString.isEmpty(serviceNameOnSuccess)) {
            executeNotifyJob(currentCounter, sm, systemId, notification, job, false);
        }
    }

    private void executeNotifyJob(int currentCounter, DBItemSchedulerMonSystemNotifications sn, String systemId,
            DBItemSchedulerMonNotifications notification, ElementNotificationJob job, boolean notifyOnError) throws Exception {
        String method = "    [" + currentCounter + "][executeNotifyJob]";

        String notifyMsg = null;
        String serviceName = null;
        EServiceStatus serviceStatus = null;
        EServiceMessagePrefix serviceMessagePrefix = null;

        if (notifyOnError) {
            notifyMsg = "notifyOnError";
            serviceName = job.getMonitor().getServiceNameOnError();
            serviceStatus = EServiceStatus.CRITICAL;
            serviceMessagePrefix = EServiceMessagePrefix.ERROR;
        } else {
            notifyMsg = "notifyOnSuccess";
            serviceName = job.getMonitor().getServiceNameOnSuccess();
            serviceStatus = EServiceStatus.OK;
            serviceMessagePrefix = EServiceMessagePrefix.SUCCESS;
        }

        String returnCodeFrom = job.getReturnCodeFrom();
        String returnCodeTo = job.getReturnCodeTo();
        boolean hasReturnCodes = hasReturnCodes(returnCodeFrom, returnCodeTo);
        Long notifications = job.getNotifications();

        Long checkId = new Long(0);
        String stepFrom = notification.getOrderStepState();
        String stepTo = notification.getOrderStepState();

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s][%s]%s", method, notifyMsg, serviceName, NotificationModel.toString(notification)));
        }

        Date startTime = null;
        Date endTime = null;
        if (notification.getStandalone()) {
            if (notification.getTaskEndTime() == null) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][skip][standalone]task is not completed - taskEndTime is null", method, notifyMsg,
                            serviceName));
                }
                return;
            }
            startTime = notification.getTaskStartTime();
            endTime = notification.getTaskEndTime();
        } else {
            if (notification.getOrderStepEndTime() == null) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][skip][order]step is not completed - orderStepEndTime is null", method, notifyMsg,
                            serviceName));
                }
                return;
            }
            startTime = notification.getOrderStepStartTime();
            endTime = notification.getOrderStepEndTime();
        }

        if (hasReturnCodes) {
            if (!checkDoNotifyByReturnCodes(notification, serviceName, notifyMsg, job.getName(), returnCodeFrom, returnCodeTo)) {
                counter.addSkip();

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][skip][notifyOnError=%s]checkDoNotifyByReturnCodes=false", method, notifyMsg, serviceName,
                            notifyOnError));
                }

                return;
            }
        } else {
            if (notifyOnError && !notification.getError()) {
                counter.addSkip();

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][skip][notifyOnError=%s]notification has no error", method, notifyMsg, serviceName,
                            notifyOnError));
                }
                return;
            } else if (!notifyOnError && notification.getError()) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][skip][notifyOnError=%s]job has an error", method, notifyMsg, serviceName,
                            notifyOnError));
                }
                return;
            }
        }

        boolean isNew = false;
        if (sn == null) {
            sn = this.getDbLayer().getSystemNotification(systemId, serviceName, notification.getId(), checkId, DBLayer.NOTIFICATION_OBJECT_TYPE_JOB,
                    !notifyOnError, stepFrom, stepTo, returnCodeFrom, returnCodeTo);
        }
        if (sn == null) {
            isNew = true;
            sn = this.getDbLayer().createSystemNotification(systemId, serviceName, notification.getId(), checkId, returnCodeFrom, returnCodeTo,
                    DBLayer.NOTIFICATION_OBJECT_TYPE_JOB, stepFrom, stepTo, startTime, endTime, new Long(0), notifications, false, false,
                    !notifyOnError);

        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s][%s][isNew=%s]%s", method, notifyMsg, serviceName, isNew, NotificationModel.toString(sn)));
        }

        if (sn.getMaxNotifications()) {
            counter.addSkip();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s][skip]maxNotifications=true", method, notifyMsg, serviceName));
            }
            return;
        }
        if (sn.getAcknowledged()) {
            counter.addSkip();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s][skip]acknowledged=true", method, notifyMsg, serviceName));
            }
            return;
        }
        if (sn.getCurrentNotification() >= notifications) {
            if (!isNew) {
                closeSystemNotification(sn, notification.getOrderEndTime());
            }
            counter.addSkip();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s][skip][%s]count notifications was reached", method, notifyMsg, serviceName, notifications));
            }
            return;
        }

        ISystemNotifierPlugin pl = getOrCreatePluginObject(job.getMonitor(), method, serviceName);
        if (pl == null) {
            return;
        }

        try {
            sn.setStepFromStartTime(startTime);
            sn.setStepToEndTime(endTime);
            sn.setCurrentNotification(sn.getCurrentNotification() + 1);
            if (sn.getCurrentNotification() >= notifications || sn.getAcknowledged()) {
                sn.setMaxNotifications(true);
            }
            sn.setNotifications(notifications);
            sn.setModified(DBLayer.getCurrentDateTime());

            LOGGER.info("----------------------------------------------------------------");
            LOGGER.info(String.format(CALL_PLUGIN_LOGGING, method, notifyMsg, serviceName, notification.getJobName(), sn.getCurrentNotification(), sn
                    .getNotifications(), pl.getClass().getSimpleName()));
            pl.notifySystem(this.getSpooler(), this.options, this.getDbLayer(), notification, sn, null, serviceStatus, serviceMessagePrefix);

            getDbLayer().getSession().beginTransaction();
            if (isNew) {
                getDbLayer().getSession().save(sn);
            } else {
                getDbLayer().getSession().update(sn);
            }
            getDbLayer().getSession().commit();
            counter.addSuccess();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s][isNew=%s]%s", method, notifyMsg, serviceName, isNew, NotificationModel.toString(sn)));
            }
            LOGGER.info("----------------------------------------------------------------");
        } catch (Exception ex) {
            try {
                getDbLayer().getSession().rollback();
            } catch (Exception e) {
                // no exception handling
            }
            LOGGER.warn(String.format(THREE_PARAMS_LOGGING, method, notifyMsg, serviceName, ex.getMessage()));
            counter.addError();
        }
    }

    private boolean hasReturnCodes(String returnCodeFrom, String returnCodeTo) {
        return !(returnCodeFrom.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && returnCodeTo.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME));
    }

    private void executeNotifyJobChain(int currentCounter, DBItemSchedulerMonSystemNotifications sm, String systemId,
            DBItemSchedulerMonNotifications notification, ElementNotificationJobChain jobChain) throws Exception {
        String method = "    [" + currentCounter + "][executeNotifyJobChain]";

        if (jobChain.getNotifications() < 1) {
            counter.addSkip();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[skip][notifications < 1]%s", method, NotificationModel.toString(jobChain)));
            }
            return;
        }

        String serviceNameOnError = jobChain.getMonitor().getServiceNameOnError();
        String serviceNameOnSuccess = jobChain.getMonitor().getServiceNameOnSuccess();
        if (!SOSString.isEmpty(serviceNameOnError)) {
            executeNotifyJobChain(currentCounter, sm, systemId, notification, jobChain, true);
        }
        if (!SOSString.isEmpty(serviceNameOnSuccess)) {
            executeNotifyJobChain(currentCounter, sm, systemId, notification, jobChain, false);
        }
    }

    private void executeNotifyJobChain(int currentCounter, DBItemSchedulerMonSystemNotifications sn, String systemId,
            DBItemSchedulerMonNotifications notification, ElementNotificationJobChain jobChain, boolean notifyOnError) throws Exception {
        String method = "    [" + currentCounter + "][executeNotifyJobChain]";

        String notifyMsg = null;
        String serviceName = null;
        EServiceStatus serviceStatus = null;
        EServiceMessagePrefix serviceMessagePrefix = null;
        boolean recovered = false;
        boolean maxNotifications = false;

        Long checkId = new Long(0);
        String stepFrom = jobChain.getStepFrom();
        String stepTo = jobChain.getStepTo();
        String returnCodeFrom = jobChain.getReturnCodeFrom();
        String returnCodeTo = jobChain.getReturnCodeTo();
        boolean hasReturnCodes = hasReturnCodes(returnCodeFrom, returnCodeTo);
        Long notifications = jobChain.getNotifications();
        boolean isNotifyAgain = sn != null;
        boolean isNew = false;

        if (notifyOnError) {
            notifyMsg = "notifyOnError";
            serviceName = jobChain.getMonitor().getServiceNameOnError();
            serviceStatus = EServiceStatus.CRITICAL;
            serviceMessagePrefix = EServiceMessagePrefix.ERROR;
        } else {
            notifyMsg = "notifyOnSuccess";
            serviceName = jobChain.getMonitor().getServiceNameOnSuccess();
            serviceStatus = EServiceStatus.OK;
            serviceMessagePrefix = EServiceMessagePrefix.SUCCESS;
        }

        if (isNotifyAgain) {
            if (!handledByNotifyAgain.contains(sn.getId())) {
                handledByNotifyAgain.add(sn.getId());
            }
            if (notifyOnError && sn.getSuccess()) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s][%s][notifyAgain][skip]is notifyOnError but system notification has succes=1", method,
                            notifyMsg, serviceName));
                }
                return;
            }

            if (!notifyOnError && !sn.getSuccess()) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s][%s][notifyAgain][skip]is notifyOnSuccess but system notification has succes=0", method,
                            notifyMsg, serviceName));
                }
                return;
            }

        }

        if (sn == null) {
            try {
                getDbLayer().getSession().beginTransaction();

                sn = getDbLayer().getSystemNotification(systemId, serviceName, notification.getId(), checkId,
                        DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN, !notifyOnError, stepFrom, stepTo, returnCodeFrom, returnCodeTo);

                if (sn == null) {
                    isNew = true;
                    sn = getDbLayer().createSystemNotification(systemId, serviceName, notification.getId(), checkId, returnCodeFrom, returnCodeTo,
                            DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN, stepFrom, stepTo, notification.getOrderStartTime(), notification
                                    .getOrderEndTime(), new Long(0), notifications, false, false, !notifyOnError);
                    getDbLayer().getSession().save(sn);
                }
                getDbLayer().getSession().commit();
            } catch (Exception ex) {
                try {
                    getDbLayer().getSession().rollback();
                } catch (Exception exx) {
                }
                LOGGER.error(String.format("%s %s", method, ex.toString()), ex);
                return;
            }
        }

        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[%s][%s][isNew=%s][isNotifyAgain=%s]%s", method, notifyMsg, serviceName, isNew, isNotifyAgain,
                    NotificationModel.toString(sn)));
        }

        if (!isNotifyAgain) {
            if (handledByNotifyAgain.contains(sn.getId())) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s][%s][skip]already handled by notifyAgain", method, notifyMsg, serviceName));
                }
                return;
            }
        }

        if (sn.getMaxNotifications()) {
            counter.addSkip();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[%s][%s][skip]maxNotifications=true", method, notifyMsg, serviceName));
            }
            return;
        }
        if (sn.getAcknowledged()) {
            counter.addSkip();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[%s][%s][skip]acknowledged=true", method, notifyMsg, serviceName));
            }
            return;
        }

        JobChainNotification jcn = getJobChainNotification(currentCounter, notification, jobChain);
        if (jcn.getLastStepForNotification() == null) {
            counter.addSkip();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[%s][%s][skip][isNew=%s]getLastStepForNotification=null", method, notifyMsg, serviceName, isNew));
            }
            return;
        }

        DBItemSchedulerMonNotifications notification2send = null;
        DBItemSchedulerMonNotifications notificationLastStep = jcn.getLastStepForNotification();
        DBItemSchedulerMonSystemResults lastErrorSended = null;

        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[%s][%s][isNew=%s][notificationLastStep]%s", method, notifyMsg, serviceName, isNew, NotificationModel
                    .toString(notificationLastStep)));
        }

        if (notifyOnError) {
            lastErrorSended = getDbLayer().getSystemResultMaxStep(sn.getId());

            if (notificationLastStep.getError()) {
                if (lastErrorSended != null) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[%s][%s][onError][lastErrorSended]%s", method, notifyMsg, serviceName, NotificationModel
                                .toString(lastErrorSended)));
                    }

                    if (lastErrorSended.getOrderStepState().equals(notificationLastStep.getOrderStepState())) {
                        if (lastErrorSended.getCurrentNotification() >= notifications) {
                            counter.addSkip();

                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("%s[%s][%s][onError][skip][%s][count notifications for this state was reached]%s of %s",
                                        method, notifyMsg, serviceName, lastErrorSended.getOrderStepState(), lastErrorSended.getCurrentNotification(),
                                        notifications));
                            }

                            if (notificationLastStep.getOrderEndTime() != null) {
                                closeSystemNotification(sn, notificationLastStep.getOrderEndTime());
                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format(
                                            "%s[%s][%s][onError][close]count notifications was reached and orderEndTime is not null", method,
                                            notifyMsg, serviceName));
                                }
                            }
                            return;
                        }
                    } else {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[%s][%s][onError][state changed]%s -> %s", method, notifyMsg, serviceName, lastErrorSended
                                    .getOrderStepState(), notificationLastStep.getOrderStepState()));
                        }
                        lastErrorSended = null;
                    }
                }
                notification2send = notificationLastStep;
            } else {
                // check for recovery
                if (lastErrorSended != null) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[%s][%s][onNoError][lastErrorSended]%s", method, notifyMsg, serviceName, NotificationModel
                                .toString(lastErrorSended)));
                    }
                    if (lastErrorSended.getRecovered()) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[%s][%s][onNoError][skip][recovery]recovery already sended", method, notifyMsg,
                                    serviceName));
                        }
                    } else {
                        if (notificationLastStep.getId() > lastErrorSended.getNotificationId()) {

                            boolean setRecovery = true;
                            if (lastErrorSended.getOrderStepState().equals(notificationLastStep.getOrderStepState())) {
                                if (notificationLastStep.getOrderStepEndTime() == null) {
                                    if (isDebugEnabled) {
                                        LOGGER.debug(String.format("%s[%s][%s][onNoError][skip][recovery][%s]state rerun is not completed", method,
                                                notifyMsg, serviceName, lastErrorSended.getOrderStepState()));
                                    }
                                    setRecovery = false;
                                }
                            }
                            if (setRecovery) {
                                // send recovery
                                notification2send = getDbLayer().getNotification(lastErrorSended.getNotificationId());
                                lastErrorSended.setRecovered(true);
                                recovered = true;
                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format("%s[%s][%s][onNoError][found][recovery]%s", method, notifyMsg, serviceName,
                                            NotificationModel.toString(notification2send)));
                                }
                            }

                        } else {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("%s[%s][%s][onNoError][skip][recovery]lastStep %s <= lastErrorSended %s", method,
                                        notifyMsg, serviceName, notificationLastStep.getId(), lastErrorSended.getNotificationId()));
                            }
                        }
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[%s][%s][onNoError][skip][recovery]lastErrorSended not found", method, notifyMsg, serviceName));
                        if (notificationLastStep.getOrderStepEndTime() == null) {
                            LOGGER.debug(String.format("%s[%s][%s][onNoError][skip][details]notificationLastStep is not completed", method, notifyMsg,
                                    serviceName));
                        }
                    }
                }
            }

            if (notification2send == null) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s][%s][skip]notification2send is null", method, notifyMsg, serviceName));
                }

                if (isNotifyAgain) {
                    if (notificationLastStep.getOrderEndTime() != null) {
                        closeSystemNotification(sn, notificationLastStep.getOrderEndTime());
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[%s][%s][notifyAgain][close]orderEndTime is not null", method, notifyMsg, serviceName));
                        }
                    } else {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[%s][%s][notifyAgain][not close]orderEndTime is null", method, notifyMsg, serviceName));
                        }
                    }
                }
                return;
            }

            if (recovered) {
                serviceStatus = EServiceStatus.OK;
                serviceMessagePrefix = EServiceMessagePrefix.RECOVERED;
            } else {
                if (jobChain.getExcludedSteps().contains(notification2send.getOrderStepState())) {
                    counter.addSkip();
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[%s][%s][skip][%s][step is excluded]%s", method, notifyMsg, serviceName, notification2send
                                .getOrderStepState(), NotificationModel.toString(notification2send)));
                    }
                    // close
                    return;
                }

                if (hasReturnCodes && !checkDoNotifyByReturnCodes(notification2send, serviceName, notifyMsg, jobChain.getName(), returnCodeFrom,
                        returnCodeTo)) {
                    counter.addSkip();

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[%s][%s][skip][checkDoNotifyByReturnCodes=false][%s]", method, notifyMsg, serviceName,
                                NotificationModel.toString(notification2send)));
                    }

                    // close
                    return;
                }
            }

            if (lastErrorSended == null) {
                lastErrorSended = getDbLayer().getSystemResult(sn.getId(), notification2send.getId());
                if (lastErrorSended == null) {
                    lastErrorSended = getDbLayer().createSystemResult(sn, notification2send);
                }
            }
            lastErrorSended.setCurrentNotification(lastErrorSended.getCurrentNotification() + 1);
            sn.setCurrentNotification(sn.getCurrentNotification() + 1);

            if (notificationLastStep.getOrderEndTime() != null) {
                if (lastErrorSended != null) {
                    if (lastErrorSended.getCurrentNotification() >= notifications) {
                        maxNotifications = true;
                    }
                }
            }

        }// notifyOnError
        else {// success
            lastErrorSended = null;
            if (notificationLastStep.getOrderEndTime() == null) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s][%s][skip]notification orderEndTime is null", method, notifyMsg, serviceName));
                }
                return;
            } else {
                if (notificationLastStep.getError()) {
                    counter.addSkip();
                    closeSystemNotification(sn, notificationLastStep.getOrderEndTime());
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[%s][%s][skip][close][notificationLastStep ends with an error]orderEndTime is not null", method,
                                notifyMsg, serviceName));
                    }
                    return;
                }
            }
            notification2send = notificationLastStep;

            if (hasReturnCodes && !checkDoNotifyByReturnCodes(notification2send, serviceName, notifyMsg, jobChain.getName(), returnCodeFrom,
                    returnCodeTo)) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s][%s][skip][checkDoNotifyByReturnCodes=false][%s]", method, notifyMsg, serviceName,
                            NotificationModel.toString(notification2send)));
                }
                // close
                return;
            }

            sn.setCurrentNotification(sn.getCurrentNotification() + 1);
            if (sn.getCurrentNotification() >= notifications) {
                maxNotifications = true;
            }
        }

        ISystemNotifierPlugin pl = getOrCreatePluginObject(jobChain.getMonitor(), method, serviceName);
        if (pl == null) {
            return;
        }

        try {
            if (jcn.getStepFrom() != null) {
                sn.setStepFromStartTime(jcn.getStepFrom().getOrderStepStartTime());
            }
            if (jcn.getStepTo() != null) {
                sn.setStepToEndTime(jcn.getStepTo().getOrderStepEndTime());
            }
            // sm.setCurrentNotification(sm.getCurrentNotification() + 1);
            sn.setMaxNotifications(maxNotifications);
            sn.setNotifications(notifications);
            sn.setModified(DBLayer.getCurrentDateTime());
            sn.setSuccess(!notifyOnError);
            sn.setRecovered(recovered);

            String jobChainInfo = notification2send.getJobChainName() + "-" + notification2send.getOrderId();
            LOGGER.info("----------------------------------------------------------------");
            LOGGER.info(String.format("[executeNotifyJobChain][%s][%s][%s]notification %s of %s. call plugin %s", notifyMsg, serviceName,
                    jobChainInfo, sn.getCurrentNotification(), sn.getNotifications(), pl.getClass().getSimpleName()));
            pl.notifySystem(this.getSpooler(), this.options, getDbLayer(), notification2send, sn, null, serviceStatus, serviceMessagePrefix);

            getDbLayer().getSession().beginTransaction();
            getDbLayer().getSession().update(sn);
            if (lastErrorSended != null) {
                if (lastErrorSended.getId() == null) {
                    getDbLayer().getSession().save(lastErrorSended);
                } else {
                    lastErrorSended.setModified(DBLayer.getCurrentDateTime());
                    getDbLayer().getSession().update(lastErrorSended);
                }
            }

            getDbLayer().getSession().commit();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[%s][%s][isNew=%s][send]%s", method, notifyMsg, serviceName, isNew, NotificationModel.toString(sn)));
                LOGGER.debug(String.format("%s[%s][%s][isNew=%s][send]%s", method, notifyMsg, serviceName, isNew, NotificationModel.toString(
                        notification2send)));
                if (lastErrorSended != null) {
                    LOGGER.debug(String.format("%s[%s][%s][isNew=%s][send]%s", method, notifyMsg, serviceName, isNew, NotificationModel.toString(
                            lastErrorSended)));
                }
            }

            counter.addSuccess();
            LOGGER.info("----------------------------------------------------------------");
        } catch (Exception ex) {
            try {
                getDbLayer().getSession().rollback();
            } catch (Exception e) {
                // no exception handling
            }
            LOGGER.warn(String.format(THREE_PARAMS_LOGGING, method, notifyMsg, serviceName, ex.getMessage()), ex);
            counter.addError();
        }
    }

    private ISystemNotifierPlugin getOrCreatePluginObject(ElementNotificationMonitor monitor, String method, String serviceName) {
        ISystemNotifierPlugin pl = null;

        try {
            pl = monitor.getOrCreatePluginObject();
            if (pl.hasErrorOnInit()) {
                counter.addSkip();
                LOGGER.warn(String.format("[%s][%s][skip] due plugin init error: %s", method, serviceName, pl.getInitError()));
                return null;
            }
        } catch (Exception e) {
            counter.addError();
            pl = null;
            LOGGER.error(String.format("[%s][%s]%s", method, serviceName, e.toString()), e);
        }
        return pl;
    }

    private void insertDummySysNotification4NotifyNew(String systemId, Long notificationId) {
        String method = "insertDummySysNotification4NotifyNew";
        try {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s]notificationId=%s", method, notificationId));
            }
            getDbLayer().getSession().beginTransaction();
            getDbLayer().deleteDummySystemNotification(systemId);

            DBItemSchedulerMonSystemNotifications sm = getDbLayer().createDummySystemNotification(systemId, notificationId);
            getDbLayer().getSession().save(sm);
            getDbLayer().getSession().commit();
        } catch (Exception ex) {
            LOGGER.warn(String.format("[%s]%s", method, ex.toString()));
            try {
                getDbLayer().getSession().rollback();
            } catch (Exception e) {
            }
        }
    }

    private void notifyTimer(String systemId) throws Exception {
        String method = "notifyTimer";
        if (!monitorOnSuccessTimers.isEmpty() || !monitorOnErrorTimers.isEmpty()) {
            notifyTimer(systemId, monitorOnSuccessTimers, monitorOnErrorTimers);
        } else {
            LOGGER.info(String.format("[%s][skip]found 0 Timer definitions", method));
        }
    }

    /** only service_on_success defined -> send to service_on_success */
    /** only service_on_error defined -> send to service_on_error */
    /** service_on_success and service_on_error defined -> send to service_on_error */
    private void notifyTimer(String systemId, ArrayList<ElementNotificationTimerRef> timersOnSuccess,
            ArrayList<ElementNotificationTimerRef> timersOnError) throws Exception {
        String method = "notifyTimer";
        List<DBItemSchedulerMonChecks> result = getDbLayer().getChecksForNotifyTimer(largeResultFetchSize);
        LOGGER.info(String.format("[%s][service_name_on_success=%s][service_name_on_error=%s]found %s checks for timers in the db", method,
                timersOnSuccess.size(), timersOnError.size(), result.size()));
        initSendCounters();
        int currentCounter = 0;
        for (DBItemSchedulerMonChecks check : result) {
            currentCounter++;
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s]%s", method, currentCounter, NotificationModel.toString(check)));
            }
            for (int i = 0; i < timersOnSuccess.size(); i++) {
                counter.addTotal();
                ElementNotificationTimerRef t = timersOnSuccess.get(i);

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][service_name_on_success][%s][%s]%s", method, currentCounter, i, t.getMonitor()
                            .getServiceNameOnSuccess(), t.getRef()));
                }

                if (!SOSString.isEmpty(t.getMonitor().getServiceNameOnError()) && !SOSString.isEmpty(t.getMonitor().getServiceNameOnSuccess())) {
                    counter.addSkip();
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format(
                                "[%s][%s][skip][service_name_on_success]check %s later on service_name_on_error for serviceName=%s", method,
                                currentCounter, t.getRef(), t.getMonitor().getServiceNameOnError()));
                    }
                    continue;
                }

                if (checkDoNotifyTimer(currentCounter, check, t)) {
                    executeNotifyTimer(currentCounter, systemId, check, t, false);
                } else {
                    counter.addSkip();
                }
            }

            for (int i = 0; i < timersOnError.size(); i++) {
                counter.addTotal();
                ElementNotificationTimerRef t = timersOnError.get(i);

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][service_name_on_error][%s][%s]%s", method, currentCounter, i, t.getMonitor()
                            .getServiceNameOnError(), t.getRef()));
                }

                if (checkDoNotifyTimer(currentCounter, check, t)) {
                    executeNotifyTimer(currentCounter, systemId, check, t, true);
                } else {
                    counter.addSkip();
                }
            }
        }
        LOGGER.info(String.format(SENT_LOGGING, method, counter.getSuccess(), counter.getError(), counter.getSkip(), counter.getTotal()));
    }

    private void notifyAgain(String systemId) throws Exception {
        String method = "notifyAgain";

        handledByNotifyAgain = new ArrayList<Long>();

        List<DBItemSchedulerMonSystemNotifications> result = getDbLayer().getSystemNotifications4NotifyAgain(systemId);
        LOGGER.info(String.format("[%s]found=%s", method, result.size()));

        initSendCounters();
        int c = 0;
        for (DBItemSchedulerMonSystemNotifications systemNotification : result) {
            c++;
            counter.addTotal();

            String notifyMsg = systemNotification.getSuccess() ? "notifyOnSuccess" : "notifyOnError";
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s]%s", c, method, notifyMsg, NotificationModel.toString(systemNotification)));
            }
            if (!systemNotification.getCheckId().equals(new Long(0))) {
                // timer
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][skip]is timer system notifier", c, method, notifyMsg));
                }
                counter.addSkip();
                continue;
            }

            DBItemSchedulerMonNotifications notification = getDbLayer().getNotification(systemNotification.getNotificationId());
            if (notification == null) {
                counter.addSkip();

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][skip]not foud notification", c, method, notifyMsg));
                }
                continue;
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s]%s", c, method, notifyMsg, NotificationModel.toString(notification)));
            }

            if (systemNotification.getObjectType().equals(DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN)) {
                Long currentNotificationBefore = systemNotification.getCurrentNotification();
                boolean matches = false;
                for (int i = 0; i < monitorJobChains.size(); i++) {
                    ElementNotificationJobChain jc = monitorJobChains.get(i);
                    if (checkDoNotify(c, notification, jc)) {
                        matches = true;
                        String serviceNameOnError = SOSString.isEmpty(jc.getMonitor().getServiceNameOnError()) ? "" : jc.getMonitor()
                                .getServiceNameOnError();
                        String serviceNameOnSuccess = SOSString.isEmpty(jc.getMonitor().getServiceNameOnSuccess()) ? "" : jc.getMonitor()
                                .getServiceNameOnSuccess();
                        if (systemNotification.getServiceName().equalsIgnoreCase(serviceNameOnError) || systemNotification.getServiceName()
                                .equalsIgnoreCase(serviceNameOnSuccess)) {
                            executeNotifyJobChain(c, systemNotification, systemId, notification, jc);
                        } else {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s][%s][%s][skip]service names not match", c, method, notifyMsg));
                            }

                        }
                    }
                }

                if (!matches) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][%s][skip][JobChain]not match the current configuration", c, method, notifyMsg));
                    }
                    // removeSystemNotification(systemNotification);
                    counter.addSkip();
                    continue;
                }

                Long currentNotificationAfter = systemNotification.getCurrentNotification();
                if (currentNotificationAfter.equals(currentNotificationBefore) && systemNotification.getStepToEndTime() != null) {
                    // LOGGER.debug(String.format("%s: [%s] disable notify again (system notification(id = %s) was not sent. maybe the JobChain configuration
                    // was changed)",method,systemId,systemNotification.getId()));
                    // setMaxNotifications(false,systemNotification);
                }

            } else if (systemNotification.getObjectType().equals(DBLayer.NOTIFICATION_OBJECT_TYPE_JOB)) {
                boolean matches = false;
                for (int i = 0; i < monitorJobs.size(); i++) {
                    ElementNotificationJob job = monitorJobs.get(i);
                    if (checkDoNotify(c, notification, job)) {
                        matches = true;
                        String serviceNameOnError = SOSString.isEmpty(job.getMonitor().getServiceNameOnError()) ? "" : job.getMonitor()
                                .getServiceNameOnError();
                        String serviceNameOnSuccess = SOSString.isEmpty(job.getMonitor().getServiceNameOnSuccess()) ? "" : job.getMonitor()
                                .getServiceNameOnSuccess();

                        if ((systemNotification.getServiceName().equalsIgnoreCase(serviceNameOnError) || systemNotification.getServiceName()
                                .equalsIgnoreCase(serviceNameOnSuccess))) {
                            executeNotifyJob(c, systemNotification, systemId, notification, job);
                        } else {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s][%s][skip]service names not match", method, c));
                            }
                        }
                    } else {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][skip]checkDoNotify=false", method, c));
                        }
                    }
                }

                if (!matches) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][%s][skip][Job]not match the current configuration", c, method, notifyMsg));
                    }
                    // removeSystemNotification(systemNotification);
                    counter.addSkip();
                    continue;
                }
            } else {
                // dummy for max notification
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][skip]dummy notification...", c, method, notifyMsg));
                }
            }
        }
        LOGGER.info(String.format("[%s]sended=%s, error=%s, skipped=%s (total checked=%s)", method, counter.getSuccess(), counter.getError(), counter
                .getSkip(), counter.getTotal()));
    }

    private void notifyNew(String systemId) throws Exception {
        String method = "notifyNew";

        Long maxNotificationId = new Long(0);

        List<DBItemSchedulerMonNotifications> result = getDbLayer().getNotifications4NotifyNew(systemId);
        LOGGER.info(String.format("[%s]found=%s", method, result.size()));
        initSendCounters();

        ArrayList<String> checkedJobchans = new ArrayList<String>();
        int c = 0;
        for (DBItemSchedulerMonNotifications notification : result) {
            c++;
            counter.addTotal();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s]%s", method, c, NotificationModel.toString(notification)));
            }

            if (notification.getId() > maxNotificationId) {
                maxNotificationId = notification.getId();
            }

            if (!notification.getStandalone()) {
                String identifier = notification.getOrderHistoryId().toString();
                if (notification.getStep().equals(new Long(1))) {// only for 1st
                    if (!checkedJobchans.contains(identifier)) {
                        checkedJobchans.add(identifier);
                        for (int i = 0; i < monitorJobChains.size(); i++) {
                            ElementNotificationJobChain jc = monitorJobChains.get(i);
                            if (checkDoNotify(c, notification, jc)) {
                                executeNotifyJobChain(c, null, systemId, notification, jc);
                            }
                        }
                    } else {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][skip][step=1]order history id=%s already processed", method, c, notification
                                    .getStep(), identifier));
                        }
                    }
                } else {
                    if (checkedJobchans.contains(identifier)) {
                        counter.addSkip();
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][skip][step %s > 1]order history id=%s already processed", method, c, notification
                                    .getStep(), identifier));
                        }
                    } else {
                        checkedJobchans.add(identifier);

                        DBItemSchedulerMonNotifications n = null;
                        for (int i = 0; i < monitorJobChains.size(); i++) {
                            // counter.addTotal();

                            ElementNotificationJobChain jc = monitorJobChains.get(i);
                            if (checkDoNotify(c, notification, jc)) {
                                if (n == null) {
                                    n = getDbLayer().getNotificationFirstStep(notification);
                                    if (n == null) {
                                        if (isDebugEnabled) {
                                            LOGGER.debug(String.format("[%s][%s][first step not found in the database]try to find a min step. %s",
                                                    method, c, NotificationModel.toString(notification)));
                                        }
                                        n = getDbLayer().getNotificationMinStep(notification);
                                    }
                                }
                                if (n == null) {
                                    counter.addSkip();
                                    LOGGER.info(String.format("[%s][%s][skip][!!!first step and min step not found in the database]%s", method, c,
                                            NotificationModel.toString(notification)));
                                    break;
                                } else {
                                    executeNotifyJobChain(c, null, systemId, n, jc);
                                }
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < monitorJobs.size(); i++) {
                ElementNotificationJob job = monitorJobs.get(i);
                if (checkDoNotify(c, notification, job)) {
                    executeNotifyJob(c, null, systemId, notification, job);
                }
            }
        }

        LOGGER.info(String.format("[%s]sended=%s, error=%s, skipped=%s (total checked=%s)", method, counter.getSuccess(), counter.getError(), counter
                .getSkip(), counter.getTotal()));

        if (maxNotificationId > 0 && counter.getError() == 0) {
            insertDummySysNotification4NotifyNew(systemId, maxNotificationId);
        }
    }

    private void closePlugins() {
        if (monitors != null) {
            for (ElementNotificationMonitor monitor : monitors) {
                if (monitor.getPluginObject() != null) {
                    try {
                        monitor.getPluginObject().close();
                    } catch (Throwable e) {
                    }
                }
            }
        }
    }

    @Override
    public void process() throws Exception {
        if (initConfig()) {
            try {
                notifyAgain(systemId);
                notifyNew(systemId);
                notifyTimer(systemId);
            } catch (Throwable e) {
                throw e;
            } finally {
                closePlugins();
            }
        }
    }

    public Spooler getSpooler() {
        return spooler;
    }

}
