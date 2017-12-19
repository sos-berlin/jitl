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

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.notification.db.DBItemSchedulerMonChecks;
import com.sos.jitl.notification.db.DBItemSchedulerMonNotifications;
import com.sos.jitl.notification.db.DBItemSchedulerMonSystemNotifications;
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
import com.sos.jitl.notification.model.notifier.SystemNotifierResults.SendedError;
import com.sos.jitl.notification.plugins.notifier.ISystemNotifierPlugin;

import sos.spooler.Spooler;
import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

public class SystemNotifierModel extends NotificationModel implements INotificationModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemNotifierModel.class);
    private static final String THREE_PARAMS_LOGGING = "%s:[%s][%s] - %s";
    private static final String CALL_PLUGIN_LOGGING = "%s:[%s][%s][%s]. notification %s of %s. call plugin %s";
    private static final String CREATE_NOTIFICATION_LOGGING = "%s: create system notification: systemId=%s, serviceName=%s, notifications=%s, "
            + "notificationId=%s, checkId=%s, stepFrom=%s, stepTo=%s";
    private static final String SENT_LOGGING = "%s: sended=%s, error=%s, skipped=%s (total checked=%s)";
    private static final String UPDATE_NOTIFICATION_LOGGING = "%s: update system notification: id=%s, systemId=%s, serviceName=%s, notifications=%s, "
            + "notificationId=%s, checkId=%s, stepFrom=%s, stepTo=%s";
    private static final String LAST_STEP_IS_NULL = "lastStepForNotification is NULL";
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
            throw new Exception(String.format("%s: schema file not found: %s", method, schemaFile.getCanonicalPath()));
        }
        systemFile = new File(this.options.system_configuration_file.getValue());
        if (!systemFile.exists()) {
            throw new Exception(String.format("%s: system configuration file not found: %s", method, systemFile.getCanonicalPath()));
        }
        LOGGER.debug(String.format("%s: read configuration file %s", method, systemFile.getCanonicalPath()));
        SOSXMLXPath xpath = new SOSXMLXPath(systemFile.getCanonicalPath());
        initMonitorObjects();
        systemId = NotificationXmlHelper.getSystemMonitorNotificationSystemId(xpath);
        if (SOSString.isEmpty(systemId)) {
            throw new Exception(String.format("systemId is NULL (configured SystemMonitorNotification/@system_id is not found)"));
        }
        LOGGER.info(String.format("%s: systemId=%s (%s)", method, systemId, systemFile.getCanonicalPath()));

        NodeList monitorList = NotificationXmlHelper.selectNotificationMonitorDefinitions(xpath);
        int valide = setMonitorObjects(xpath, monitorList);

        int jobChains = monitorJobChains.size();
        int jobs = monitorJobs.size();
        int timersOnError = monitorOnErrorTimers.size();
        int timersOnSuccess = monitorOnSuccessTimers.size();
        int total = jobChains + jobs + timersOnError + timersOnSuccess;
        String msg = String.format("%s: NotificationMonitors=%s(valide=%s), JobChains=%s, Jobs=%s, TimerRefs[onError=%s, onSuccess=%s]", method,
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

    private void executeNotifyTimer(String systemId, DBItemSchedulerMonChecks check, ElementNotificationTimerRef timer,
            boolean isNotifyOnErrorService) throws Exception {
        // Output indent
        String method = "  executeNotifyTimer";
        String serviceName = (isNotifyOnErrorService) ? timer.getMonitor().getServiceNameOnError() : timer.getMonitor().getServiceNameOnSuccess();

        ISystemNotifierPlugin pl = getOrCreatePluginObject(timer.getMonitor(), method, serviceName);
        if (pl == null) {
            return;
        }

        EServiceStatus pluginStatus = (isNotifyOnErrorService) ? EServiceStatus.CRITICAL : EServiceStatus.OK;
        DBItemSchedulerMonNotifications notification = getDbLayer().getNotification(check.getNotificationId());
        if (notification == null) {
            throw new Exception(String.format("%s: serviceName=%s, notification with check.notificationId=%s not found", method, serviceName, check
                    .getNotificationId()));
        }

        boolean checkSmOnSuccess = false; // timer always error
        String stepFrom = check.getStepFrom();
        String stepTo = check.getStepTo();
        String returnCodeFrom = null;
        String returnCodeTo = null;
        Long notifications = timer.getNotifications();
        if (notifications < 1) {
            counter.addSkip();
            LOGGER.debug(String.format("%s:[skip][name=%s][notifications is %s]serviceName=%s, check.id=%s", method, check.getName(), notifications,
                    serviceName, check.getId()));
            return;
        }
        DBItemSchedulerMonSystemNotifications checkSm = null;
        boolean isNew = false;
        if (timer.getNotifyOnError()) {
            checkSm = getDbLayer().getSystemNotification(systemId, serviceName, notification.getId(), check.getId(), check.getObjectType(),
                    checkSmOnSuccess, stepFrom, stepTo, returnCodeFrom, returnCodeTo);
        } else {
            // find error
            List<DBItemSchedulerMonSystemNotifications> result = getDbLayer().getSystemNotifications(systemId, null, notification.getId());
            LOGGER.debug(String.format("%s: found %s system notifications in the db for systemId=%s, notificationId=%s)", method, result.size(),
                    systemId, notification.getId()));

            Long lastSystemId = new Long(0);
            DBItemSchedulerMonSystemNotifications notCheckSm = null;
            for (int i = 0; i < result.size(); i++) {
                DBItemSchedulerMonSystemNotifications resultSm = result.get(i);
                if (resultSm.getCheckId().equals(new Long(0)) && !resultSm.getObjectType().equals(DBLayer.NOTIFICATION_OBJECT_TYPE_DUMMY)) {
                    if (resultSm.getId() > lastSystemId) {
                        notCheckSm = resultSm;
                        lastSystemId = resultSm.getId();
                    }
                }
                if (resultSm.getCheckId().equals(check.getId())) {
                    checkSm = resultSm;
                }
            }
            if (notCheckSm != null) {
                if (!(notCheckSm.getSuccess() || (!notCheckSm.getSuccess() && notCheckSm.getRecovered()))) {
                    counter.addSkip();
                    LOGGER.debug(String.format(
                            "%s:[skip][name=%s][notification has the error][current serviceName=%s][notCheckSm id=%s, serviceName=%s, success=%s, recovered=%s, "
                                    + "currentNotification=%s]", method, check.getName(), serviceName, notCheckSm.getId(), notCheckSm
                                            .getServiceName(), notCheckSm.getSuccess(), notCheckSm.getRecovered(), notCheckSm
                                                    .getCurrentNotification()));
                    return;
                }
            }
        }
        if (checkSm == null) {
            isNew = true;
            Date startTime = notification.getOrderStartTime();
            Date endTime = notification.getOrderEndTime();
            if (notification.getStandalone()) {
                startTime = notification.getTaskStartTime();
                endTime = notification.getTaskEndTime();
            }
            checkSm = getDbLayer().createSystemNotification(systemId, serviceName, notification.getId(), check.getId(), returnCodeFrom, returnCodeTo,
                    check.getObjectType(), stepFrom, stepTo, startTime, endTime, new Long(0), notifications, false, false, true);
        }
        if (checkSm.getMaxNotifications()) {
            counter.addSkip();
            LOGGER.debug(String.format("%s:[skip][name=%s][count notifications was reached]id=%s, serviceName=%s, notifications=%s, "
                    + "maxNotifictions=%s", method, check.getName(), checkSm.getId(), checkSm.getServiceName(), checkSm.getCurrentNotification(),
                    checkSm.getMaxNotifications()));
            return;
        }
        if (checkSm.getAcknowledged()) {
            counter.addSkip();
            LOGGER.debug(String.format("%s:[skip][name=%s][is acknowledged]id=%s, serviceName=%s, notifications=%s, acknowledged=%s", method, check
                    .getName(), checkSm.getId(), checkSm.getServiceName(), checkSm.getCurrentNotification(), checkSm.getAcknowledged()));
            return;
        }
        if (checkSm.getCurrentNotification() >= notifications) {
            setMaxNotifications(isNew, checkSm);
            counter.addSkip();
            LOGGER.debug(String.format("%s:[skip][name=%s][count notifications was reached]id=%s, serviceName=%s, currentNotification=%s", method,
                    check.getName(), checkSm.getId(), checkSm.getServiceName(), checkSm.getCurrentNotification()));
            return;
        }

        try {
            checkSm.setCurrentNotification(checkSm.getCurrentNotification() + 1);
            if (checkSm.getCurrentNotification() >= notifications || checkSm.getAcknowledged()) {
                checkSm.setMaxNotifications(true);
            }
            checkSm.setSuccess(checkSmOnSuccess);
            checkSm.setModified(DBLayer.getCurrentDateTime());
            checkSm.setNotifications(notifications);
            if (isNew) {
                LOGGER.debug(String.format(CREATE_NOTIFICATION_LOGGING, method, checkSm.getSystemId(), checkSm.getServiceName(), checkSm
                        .getCurrentNotification(), checkSm.getNotificationId(), checkSm.getCheckId(), checkSm.getStepFrom(), checkSm.getStepTo()));
            } else {
                LOGGER.debug(String.format(UPDATE_NOTIFICATION_LOGGING, method, checkSm.getId(), checkSm.getSystemId(), checkSm.getServiceName(),
                        checkSm.getCurrentNotification(), checkSm.getNotificationId(), checkSm.getCheckId(), checkSm.getStepFrom(), checkSm
                                .getStepTo()));
            }
            String name = checkSm.getObjectType().equals(DBLayer.NOTIFICATION_OBJECT_TYPE_JOB) ? "Job " + notification.getJobName() : notification
                    .getJobChainName();
            LOGGER.info(String.format(CALL_PLUGIN_LOGGING, method, "notifyOnTimer", serviceName, name, checkSm.getCurrentNotification(), checkSm
                    .getNotifications(), pl.getClass().getSimpleName()));
            pl.notifySystem(getSpooler(), options, getDbLayer(), notification, checkSm, check, pluginStatus, EServiceMessagePrefix.TIMER);
            getDbLayer().getSession().beginTransaction();
            if (isNew) {
                getDbLayer().getSession().save(checkSm);
            } else {
                getDbLayer().getSession().update(checkSm);
            }
            getDbLayer().getSession().commit();
            counter.addSuccess();
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

    private void setMaxNotifications(boolean isNew, DBItemSchedulerMonSystemNotifications sm) throws Exception {
        if (!isNew) {
            sm.setMaxNotifications(true);
            sm.setModified(DBLayer.getCurrentDateTime());
            getDbLayer().getSession().beginTransaction();
            getDbLayer().getSession().update(sm);
            getDbLayer().getSession().commit();
        }
    }

    private void save(DBItemSchedulerMonSystemNotifications sm) throws Exception {
        sm.setModified(DBLayer.getCurrentDateTime());
        getDbLayer().getSession().beginTransaction();
        getDbLayer().getSession().save(sm);
        getDbLayer().getSession().commit();
    }

    private boolean checkDoNotificationByReturnCodes(DBItemSchedulerMonNotifications notification, String serviceName, String notifyMsg,
            String configuredName, String configuredReturnCodeFrom, String configuredReturnCodeTo) {
        String method = "checkDoNotificationByReturnCodes";

        if (notification.getStandalone()) {
            if (notification.getTaskEndTime() == null) {
                counter.addSkip();
                LOGGER.debug(String.format(
                        "%s:[%s][%s][skip notify standalone][task is not completed-taskEndTime is empty][notification id=%s, jobName=%s]", method,
                        notifyMsg, serviceName, notification.getId(), notification.getJobName()));
                return false;
            }
        } else {
            if (notification.getOrderStepEndTime() == null) {
                counter.addSkip();
                LOGGER.debug(String.format(
                        "%s:[%s][%s][skip notify order][step is not completed-orderStepEndTime is empty][notification id=%s, jobName=%s]", method,
                        notifyMsg, serviceName, notification.getId(), notification.getJobName()));
                return false;
            }
        }

        if (!configuredReturnCodeFrom.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            try {
                Long rc = Long.parseLong(configuredReturnCodeFrom);
                if (notification.getReturnCode() < rc) {
                    LOGGER.debug(String.format(
                            "%s:[%s][%s][skip][return code (%s) less than configured return_code_from (%s)][notification id=%s, step=%s, jobName=%s, jobChainName=%s]",
                            method, notifyMsg, serviceName, notification.getReturnCode(), configuredReturnCodeFrom, notification.getId(), notification
                                    .getOrderStepState(), notification.getJobName(), notification.getJobChainName()));
                    return false;
                }
            } catch (Exception ex) {
                LOGGER.warn(String.format("%s:[%s][%s][%s][skip][configured return_code_from=%s is not a valid integer value]%s", method, notifyMsg,
                        serviceName, configuredName, configuredReturnCodeFrom, ex.getMessage()));
                return false;
            }
        }
        if (!configuredReturnCodeTo.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            try {
                Long rc = Long.parseLong(configuredReturnCodeTo);
                if (notification.getReturnCode() > rc) {
                    LOGGER.debug(String.format(
                            "%s:[%s][%s][skip][return code (%s) greater than configured return_code_to (%s)][notification id=%s, step=%s, jobName=%s, jobChainName=%s]",
                            method, notifyMsg, serviceName, notification.getReturnCode(), configuredReturnCodeTo, notification.getId(), notification
                                    .getOrderStepState(), notification.getJobName(), notification.getJobChainName()));
                    return false;
                }
            } catch (Exception ex) {
                LOGGER.warn(String.format("%s:[%s][%s][%s][skip][configured return_code_to=%s is not a valid integer value]%s", method, notifyMsg,
                        serviceName, configuredName, configuredReturnCodeTo, ex.getMessage()));
                return false;
            }
        }

        return true;
    }

    private boolean checkDoNotificationTimer(DBItemSchedulerMonChecks check, ElementNotificationTimerRef timer) {
        String method = "  checkDoNotificationTimer";
        boolean notify = true;
        String ref = timer.getRef();
        if (!check.getName().equals(ref)) {
            notify = false;
        }
        LOGGER.debug(String.format("%s: %s(name=%s) and configured(ref=%s)", method, notify ? "ok. check db " : "skip. ", check.getName(), ref));
        return notify;
    }

    private boolean checkDoNotification(DBItemSchedulerMonNotifications notification, ElementNotificationJobChain jc) throws Exception {
        String method = "  checkDoNotification";
        boolean notify = true;
        String schedulerId = jc.getSchedulerId();
        String jobChainName = jc.getName();
        if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            try {
                if (!notification.getSchedulerId().matches(schedulerId)) {
                    notify = false;
                }
            } catch (Exception ex) {
                throw new Exception(String.format("%s: check with configured scheduler_id=%s: %s", method, schedulerId, ex));
            }
        }
        if (notify && !jobChainName.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            jobChainName = CheckHistoryModel.normalizeRegex(jobChainName);
            try {
                if (!notification.getJobChainName().matches(jobChainName)) {
                    notify = false;
                }
            } catch (Exception ex) {
                throw new Exception(String.format("%s: check with configured scheduler_id=%s, name=%s: %s", method, schedulerId, jobChainName, ex));
            }
        }
        LOGGER.debug(String.format("%s: %s(schedulerId=%s, jobChain=%s) and configured(schedulerId=%s, jobChain=%s)", method, notify
                ? "ok. do check db " : "skip. ", notification.getSchedulerId(), notification.getJobChainName(), schedulerId, jobChainName));
        return notify;
    }

    private boolean checkDoNotification(DBItemSchedulerMonNotifications notification, ElementNotificationJob job) throws Exception {
        String method = "  checkDoNotification";
        boolean notify = true;
        String schedulerId = job.getSchedulerId();
        String jobName = job.getName();
        if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            try {
                if (!notification.getSchedulerId().matches(schedulerId)) {
                    notify = false;
                }
            } catch (Exception ex) {
                throw new Exception(String.format("%s: check with configured scheduler_id=%s: %s", method, schedulerId, ex));
            }
        }
        if (notify && !jobName.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            jobName = CheckHistoryModel.normalizeRegex(jobName);
            try {
                if (!notification.getJobName().matches(jobName)) {
                    notify = false;
                }
            } catch (Exception ex) {
                throw new Exception(String.format("%s: check with configured scheduler_id=%s, name=%s: %s", method, schedulerId, jobName, ex));
            }
        }
        LOGGER.debug(String.format("%s: %s(schedulerId=%s, jobName=%s) and configured(schedulerId=%s, jobName=%s)", method, notify
                ? "ok. do check db " : "skip. ", notification.getSchedulerId(), notification.getJobName(), schedulerId, jobName));
        return notify;
    }

    private JobChainNotification getJobChainNotification(DBItemSchedulerMonNotifications notification, ElementNotificationJobChain jc)
            throws Exception {
        String method = "getJobChainNotification";

        JobChainNotification jcn = new JobChainNotification();
        String stepFrom = jc.getStepFrom();
        String stepTo = jc.getStepTo();

        DBItemSchedulerMonNotifications stepFromNotification = null;
        DBItemSchedulerMonNotifications stepToNotification = null;
        // stepFrom, stepTo handling
        if (!stepFrom.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) || !stepTo.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) || !jc
                .getExcludedSteps().isEmpty()) {

            Long stepFromIndex = new Long(0);
            Long stepToIndex = new Long(0);
            List<DBItemSchedulerMonNotifications> steps = this.getDbLayer().getOrderNotifications(largeResultFetchSize, notification
                    .getOrderHistoryId());
            if (steps == null || steps.isEmpty()) {
                throw new Exception(String.format("%s: no steps found for orderHistoryId=%s", method, notification.getOrderHistoryId()));
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

                LOGGER.debug(String.format(
                        "%s:[skip][configured stepFrom=%s not founded][notification.getOrderHistoryId=%s, jcn.getStepFromIndex=%s, jcn.getStepToIndex=%s, configured stepTo=%s]",
                        method, stepFrom, notification.getOrderHistoryId(), jcn.getStepFromIndex(), jcn.getStepToIndex(), stepTo));
            } else {
                for (DBItemSchedulerMonNotifications step : jcn.getSteps()) {
                    if (step.getStep() >= jcn.getStepFromIndex() && step.getStep() <= jcn.getStepToIndex()) {
                        jcn.setLastStepForNotification(step);
                    }
                }

                LOGGER.debug(String.format(
                        "%s: notification.getOrderHistoryId=%s, jcn.getSteps().size=%s, jcn.getStepFromIndex=%s, jcn.getStepToIndex=%s, configured stepFrom=%s, configured stepTo=%s",
                        method, notification.getOrderHistoryId(), jcn.getSteps().size(), jcn.getStepFromIndex(), jcn.getStepToIndex(), stepFrom,
                        stepTo));
            }

        } else {
            LOGGER.debug(String.format("%s:  find last step for notification.getOrderHistoryId=%s. notification.id=%s", method, notification
                    .getOrderHistoryId(), notification.getId()));
            jcn.setLastStepForNotification(getDbLayer().getNotificationsLastStep(notification, false));
            jcn.setStepFrom(notification);
            jcn.setStepTo(jcn.getLastStepForNotification());
        }

        return jcn;
    }

    private void executeNotifyJob(DBItemSchedulerMonSystemNotifications sm, String systemId, DBItemSchedulerMonNotifications notification,
            ElementNotificationJob job) throws Exception {
        String method = "executeNotifyJob";
        String serviceNameOnError = job.getMonitor().getServiceNameOnError();
        String serviceNameOnSuccess = job.getMonitor().getServiceNameOnSuccess();

        if (job.getNotifications() < 1) {
            counter.addSkip();
            LOGGER.debug(String.format(
                    "%s: serviceNameOnError=%s, serviceNameOnSuccess=%s. skip notify Job(maxNotifications is %s): notification.id=%s, schedulerId=%s, jobName=%s",
                    method, serviceNameOnError, serviceNameOnSuccess, job.getNotifications(), notification.getId(), notification.getSchedulerId(),
                    notification.getJobName()));
            return;
        }
        if (!SOSString.isEmpty(serviceNameOnError)) {
            executeNotifyJob(sm, systemId, notification, job, true);
        }
        if (!SOSString.isEmpty(serviceNameOnSuccess)) {
            executeNotifyJob(sm, systemId, notification, job, false);
        }
    }

    private void executeNotifyJob(DBItemSchedulerMonSystemNotifications sm, String systemId, DBItemSchedulerMonNotifications notification,
            ElementNotificationJob job, boolean notifyOnError) throws Exception {
        String method = "executeNotifyJob";

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
            serviceMessagePrefix = EServiceMessagePrefix.NONE;
        }

        ISystemNotifierPlugin pl = getOrCreatePluginObject(job.getMonitor(), method, serviceName);
        if (pl == null) {
            return;
        }

        String returnCodeFrom = job.getReturnCodeFrom();
        String returnCodeTo = job.getReturnCodeTo();
        boolean hasReturnCodes = hasReturnCodes(returnCodeFrom, returnCodeTo);
        Long notifications = job.getNotifications();

        Long checkId = new Long(0);
        String stepFrom = notification.getOrderStepState();
        String stepTo = notification.getOrderStepState();

        Date startTime = null;
        Date endTime = null;
        if (notification.getStandalone()) {
            if (notification.getTaskEndTime() == null) {
                counter.addSkip();
                LOGGER.debug(String.format(
                        "%s:[%s][%s][skip notify standalone Job][task is not completed-taskEndTime is empty][notification id=%s, jobName=%s]", method,
                        notifyMsg, serviceName, notification.getId(), notification.getJobName()));
                return;
            }
            startTime = notification.getTaskStartTime();
            endTime = notification.getTaskEndTime();
        } else {
            if (notification.getOrderStepEndTime() == null) {
                counter.addSkip();
                LOGGER.debug(String.format(
                        "%s:[%s][%s][skip notify order Job][step is not completed-orderStepEndTime is empty][notification id=%s, jobName=%s]", method,
                        notifyMsg, serviceName, notification.getId(), notification.getJobName()));
                return;
            }
            startTime = notification.getOrderStepStartTime();
            endTime = notification.getOrderStepEndTime();
        }

        if (hasReturnCodes) {
            if (!checkDoNotificationByReturnCodes(notification, serviceName, notifyMsg, job.getName(), returnCodeFrom, returnCodeTo)) {
                counter.addSkip();
                return;
            }
        } else {
            if (notifyOnError && !notification.getError()) {
                counter.addSkip();
                LOGGER.debug(String.format("%s:[%s][%s][skip][job has no error][notification id=%s, jobName=%s]", method, notifyMsg, serviceName,
                        notification.getId(), notification.getJobName()));
                return;
            } else if (!notifyOnError && notification.getError()) {
                counter.addSkip();
                LOGGER.debug(String.format("%s:[%s][%s][skip][job has error][notification id=%s, jobName=%s, errorText=%s]", method, notifyMsg,
                        serviceName, notification.getId(), notification.getJobName(), notification.getErrorText()));
                return;
            }
        }

        boolean isNew = false;
        if (sm == null) {
            sm = this.getDbLayer().getSystemNotification(systemId, serviceName, notification.getId(), checkId, DBLayer.NOTIFICATION_OBJECT_TYPE_JOB,
                    !notifyOnError, stepFrom, stepTo, returnCodeFrom, returnCodeTo);
        }
        if (sm == null) {
            isNew = true;
            sm = this.getDbLayer().createSystemNotification(systemId, serviceName, notification.getId(), checkId, returnCodeFrom, returnCodeTo,
                    DBLayer.NOTIFICATION_OBJECT_TYPE_JOB, stepFrom, stepTo, startTime, endTime, new Long(0), notifications, false, false,
                    !notifyOnError);

        }
        LOGGER.debug(String.format("%s:[%s][%s]. %s. %s", method, notifyMsg, serviceName, isNew ? "new system notification"
                : "old system notification", sm.toString()));

        if (sm.getMaxNotifications()) {
            counter.addSkip();
            LOGGER.debug(String.format("%s:[%s][%s][skip][count notifications was reached][sm id=%s, currentNotification=%s, maxNotifictions=%s]",
                    method, notifyMsg, serviceName, sm.getId(), sm.getCurrentNotification(), sm.getMaxNotifications()));
            return;
        }
        if (sm.getAcknowledged()) {
            counter.addSkip();
            LOGGER.debug(String.format("%s:[%s][%s][skip][is acknowledged][sm.id=%s, currentNotification=%s, acknowledged=%s]", method, notifyMsg,
                    serviceName, sm.getId(), sm.getCurrentNotification(), sm.getAcknowledged()));
            return;
        }
        if (sm.getCurrentNotification() >= notifications) {
            this.setMaxNotifications(isNew, sm);
            counter.addSkip();
            LOGGER.debug(String.format(
                    "%s:[%s][%s][skip][count notifications was reached][sm.id=%s, sm.currentNotification=%s, configured notifications=%s]", method,
                    notifyMsg, serviceName, sm.getId(), sm.getCurrentNotification(), notifications));
            return;
        }

        try {
            sm.setStepFromStartTime(startTime);
            sm.setStepToEndTime(endTime);
            sm.setCurrentNotification(sm.getCurrentNotification() + 1);
            if (sm.getCurrentNotification() >= notifications || sm.getAcknowledged()) {
                sm.setMaxNotifications(true);
            }
            sm.setNotifications(notifications);
            sm.setModified(DBLayer.getCurrentDateTime());
            if (isNew) {
                LOGGER.debug(String.format(CREATE_NOTIFICATION_LOGGING, method, sm.getSystemId(), sm.getServiceName(), sm.getCurrentNotification(), sm
                        .getNotificationId(), sm.getCheckId(), sm.getStepFrom(), sm.getStepTo()));
            } else {
                LOGGER.debug(String.format(UPDATE_NOTIFICATION_LOGGING, method, sm.getId(), sm.getSystemId(), sm.getServiceName(), sm
                        .getCurrentNotification(), sm.getNotificationId(), sm.getCheckId(), sm.getStepFrom(), sm.getStepTo()));
            }
            LOGGER.debug(String.format("%s:[%s][%s][notification id=%s, jobName=%s]", method, notifyMsg, serviceName, notification.getId(),
                    notification.getJobName()));

            LOGGER.info(String.format(CALL_PLUGIN_LOGGING, method, notifyMsg, serviceName, notification.getJobName(), sm.getCurrentNotification(), sm
                    .getNotifications(), pl.getClass().getSimpleName()));
            pl.notifySystem(this.getSpooler(), this.options, this.getDbLayer(), notification, sm, null, serviceStatus, serviceMessagePrefix);

            getDbLayer().getSession().beginTransaction();
            if (isNew) {
                getDbLayer().getSession().save(sm);
            } else {
                getDbLayer().getSession().update(sm);
            }
            getDbLayer().getSession().commit();
            counter.addSuccess();
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

    private void executeNotifyJobChain(DBItemSchedulerMonSystemNotifications sm, String systemId, DBItemSchedulerMonNotifications notification,
            ElementNotificationJobChain jobChain) throws Exception {
        String method = "executeNotifyJobChain";
        String serviceNameOnError = jobChain.getMonitor().getServiceNameOnError();
        String serviceNameOnSuccess = jobChain.getMonitor().getServiceNameOnSuccess();

        if (jobChain.getNotifications() < 1) {
            counter.addSkip();
            LOGGER.debug(String.format(
                    "%s: serviceNameOnError=\"%s\", serviceNameOnSuccess=\"%s\". skip notify JobChain(maxNotifications is %s): notification.id=%s, schedulerId=%s, jobName=%s",
                    method, serviceNameOnError, serviceNameOnSuccess, jobChain.getNotifications(), notification.getId(), notification
                            .getSchedulerId(), notification.getJobName()));
            return;
        }

        LOGGER.debug(String.format(
                "%s: serviceNameOnError=\"%s\", serviceNameOnSuccess=\"%s\". notification.id=%s, schedulerId=%s, notification.jobChainName=%s",
                method, serviceNameOnError, serviceNameOnSuccess, notification.getId(), notification.getSchedulerId(), notification
                        .getJobChainName()));

        JobChainNotification jcn = getJobChainNotification(notification, jobChain);
        if (!SOSString.isEmpty(serviceNameOnError)) {
            if (jcn.getLastStepForNotification() == null) {
                counter.addSkip();
            } else {
                executeNotifyJobChain(sm, systemId, notification, jobChain, jcn, true);
            }
        }

        if (!SOSString.isEmpty(serviceNameOnSuccess)) {
            if (jcn.getLastStepForNotification() == null) {
                counter.addSkip();
            } else {
                executeNotifyJobChain(sm, systemId, notification, jobChain, jcn, false);
            }
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

    private void executeNotifyJobChain(DBItemSchedulerMonSystemNotifications sm, String systemId, DBItemSchedulerMonNotifications notification,
            ElementNotificationJobChain jobChain, JobChainNotification jcn, boolean notifyOnError) throws Exception {
        String method = "executeNotifyJobChain";

        if (jcn.getLastStepForNotification() == null) {
            throw new Exception(String.format(LAST_STEP_IS_NULL));
        }

        LOGGER.debug(String.format("%s: notification=%s", method, SOSHibernateFactory.toString(notification)));
        LOGGER.debug(String.format("%s: jcn.getLastStepForNotification=%s", method, SOSHibernateFactory.toString(jcn.getLastStepForNotification())));

        boolean hasReturnCodes = hasReturnCodes(jobChain.getReturnCodeFrom(), jobChain.getReturnCodeTo());
        String notifyMsg = notifyOnError ? "notifyOnError" : "notifyOnSuccess";
        String serviceName = notifyOnError ? jobChain.getMonitor().getServiceNameOnError() : jobChain.getMonitor().getServiceNameOnSuccess();

        ISystemNotifierPlugin pl = getOrCreatePluginObject(jobChain.getMonitor(), method, serviceName);
        if (pl == null) {
            return;
        }

        if (notifyOnError) {
            if (!hasReturnCodes && !jcn.getLastStepForNotification().getError() && !notification.getError()) {
                counter.addSkip();
                LOGGER.debug(String.format("%s: [%s][%s][skip][step=%s has no error][jcn.getLastStepForNotification() id=%s, jobChainName=%s]",
                        method, notifyMsg, serviceName, jcn.getLastStepForNotification().getOrderStepState(), jcn.getLastStepForNotification()
                                .getId(), jcn.getLastStepForNotification().getJobChainName()));
                return;
            }

        } else {
            if (!hasReturnCodes && jcn.getLastStepForNotification().getError()) {
                counter.addSkip();
                LOGGER.debug(String.format(
                        "%s: [%s][%s][skip][last step=%s ends with error][jcn.getLastStepForNotification() id=%s, .jobChainName=%s]", method,
                        notifyMsg, serviceName, jcn.getLastStepForNotification().getOrderStepState(), jcn.getLastStepForNotification().getId(), jcn
                                .getLastStepForNotification().getJobChainName()));
                return;
            }

            if (jcn.getLastStepForNotification().getOrderEndTime() == null) {
                counter.addSkip();
                LOGGER.debug(String.format("%s:[%s][%s][skip][order is not yet to end][notification id=%s, jobChainName=%s]", method, notifyMsg,
                        serviceName, notification.getId(), notification.getJobChainName()));
                return;
            }
        }

        if (hasReturnCodes && !checkDoNotificationByReturnCodes(jcn.getLastStepForNotification(), serviceName, notifyMsg, jobChain.getName(), jobChain
                .getReturnCodeFrom(), jobChain.getReturnCodeTo())) {
            counter.addSkip();
            return;
        }

        if (jobChain.getExcludedSteps().contains(jcn.getLastStepForNotification().getOrderStepState())) {
            if (notifyOnError) {
                if (jcn.getLastStepForNotification().getOrderEndTime() != null && jcn.getLastStepForNotification().getOrderStepState().equals(jcn
                        .getLastStep().getOrderStepState())) {
                    LOGGER.debug(String.format("%s: [%s][%s]. order is completed and error step state equals config step=%s and this is "
                            + "the last order step.  create and do notify system: notification.id=%s", method, notifyMsg, serviceName, jcn
                                    .getLastStepForNotification().getOrderStepState(), notification.getId()));
                } else {
                    counter.addSkip();
                    LOGGER.info(String.format("%s:[%s][%s][skip][order is not completed or error step equals config step=%s and this is "
                            + "not the last order step][notification id=%s]", method, notifyMsg, serviceName, jcn.getLastStepForNotification()
                                    .getOrderStepState(), notification.getId()));
                    return;
                }
            } else {
                counter.addSkip();
                LOGGER.info(String.format("%s:[%s][%s][skip][step=%s is configured as excluded][notification id=%s]" + "serviceName=%s. ", method,
                        notifyMsg, serviceName, jcn.getLastStepForNotification().getOrderStepState(), notification.getId()));
                return;
            }
        }

        // boolean isNew = false;
        if (sm == null) {
            sm = getDbLayer().getSystemNotification(systemId, serviceName, notification.getId(), new Long(0),
                    DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN, !notifyOnError, jobChain.getStepFrom(), jobChain.getStepTo(), jobChain
                            .getReturnCodeFrom(), jobChain.getReturnCodeTo());

            if (sm == null) {
                sm = getDbLayer().createSystemNotification(systemId, serviceName, notification.getId(), new Long(0), jobChain.getReturnCodeFrom(),
                        jobChain.getReturnCodeTo(), DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN, jobChain.getStepFrom(), jobChain.getStepTo(),
                        notification.getOrderStartTime(), notification.getOrderEndTime(), new Long(0), jobChain.getNotifications(), false, false,
                        !notifyOnError);
                save(sm);
            }
        }

        if (sm.getAcknowledged()) {
            counter.addSkip();
            LOGGER.debug(String.format("%s: [%s][%s][skip][is acknowledged][sm id=%s, currentNotification=%s, acknowledged=%s]", method, notifyMsg,
                    serviceName, sm.getId(), sm.getCurrentNotification(), sm.getAcknowledged()));
            return;
        }

        if (sm.getMaxNotifications()) {
            counter.addSkip();
            LOGGER.debug(String.format("%s:[%s][%s][skip][count notifications was reached][sm id=%s, currentNotification=%s, maxNotifications=%s]",
                    method, notifyMsg, serviceName, sm.getId(), sm.getCurrentNotification(), sm.getMaxNotifications()));
            return;
        }

        SystemNotifierResults results = null;
        boolean maxNotifications = false;
        if (notifyOnError) {
            if (sm.getRecovered()) {
                counter.addSkip();
                LOGGER.debug(String.format("%s: [%s][%s][skip][notifications already recovered][sm id=%s, currentNotification=%s, recovered=%s]",
                        method, notifyMsg, serviceName, sm.getId(), sm.getCurrentNotification(), sm.getRecovered()));
                return;
            }
            results = new SystemNotifierResults(sm.getNotificationResults());
            if (results.getSendedErrors().size() == 0 && !jcn.getLastStepForNotification().getError() && jcn.getLastStepForNotification()
                    .getStep() > notification.getStep()) {
                if (jcn.getLastStepForNotification().getOrderEndTime() == null) {

                    List<DBItemSchedulerMonNotifications> errNotifications = getDbLayer().getPreviousErrorNotifications(jcn
                            .getLastStepForNotification());
                    if (errNotifications != null) {
                        DBItemSchedulerMonNotifications lastErrorNotification = null;
                        for (DBItemSchedulerMonNotifications err : errNotifications) {
                            if (lastErrorNotification == null) {
                                lastErrorNotification = err;
                            } else if (lastErrorNotification.getStep() < err.getStep()) {
                                lastErrorNotification = err;
                            }
                        }
                        if (lastErrorNotification != null) {
                            jcn.setLastStepForNotification(lastErrorNotification);

                            LOGGER.debug(String.format("%s: set to lastErrorNotification jcn.getLastStepForNotification=%s", method,
                                    SOSHibernateFactory.toString(jcn.getLastStepForNotification())));
                        }
                    }
                }
            }

            String lastState4Results = results.normalizeState(jcn.getLastStepForNotification().getOrderStepState());

            if (jcn.getLastStepForNotification().getError()) {
                results.updateState(lastState4Results);
            }
            boolean doSend = true;
            Long lastSendedErrorCounter = new Long(0);

            for (SendedError sendedError : results.getSendedErrors()) {
                if (sendedError.getStateStep() != null && !sendedError.isRecovered() && sendedError.getStateStep() < jcn.getLastStepForNotification()
                        .getStep()) {
                    if (lastState4Results.equals(sendedError.getState())) {
                        if (!jcn.getLastStepForNotification().getError() && jcn.getLastStepForNotification().getOrderStepEndTime() != null) {
                            sendedError.setRecovered(true);
                            DBItemSchedulerMonNotifications not = getDbLayer().getNotificationByStep(jcn.getLastStepForNotification()
                                    .getOrderHistoryId(), sendedError.getStateStep());
                            if (not == null) {
                                not = jcn.getLastStepForNotification();
                            }
                            sendJobChainRecovery(pl, sm, jobChain, jcn, not, results, notifyMsg, serviceName, maxNotifications);
                            counter.addSuccess();
                        }
                    } else {
                        sendedError.setRecovered(true);
                        DBItemSchedulerMonNotifications not = getDbLayer().getNotificationByStep(jcn.getLastStepForNotification().getOrderHistoryId(),
                                sendedError.getStateStep());
                        if (not == null) {
                            not = jcn.getLastStepForNotification();
                        }
                        sendJobChainRecovery(pl, sm, jobChain, jcn, not, results, notifyMsg, serviceName, maxNotifications);
                        counter.addSuccess();
                    }
                }

                LOGGER.debug(String.format("%s: [%s][%s]sendedError=%s", method, notifyMsg, serviceName, sendedError.toString()));

                if (sendedError.isRecovered()) {
                    LOGGER.debug(String.format("%s: [%s][%s][skip][sendedError][%s]already recovered", method, notifyMsg, serviceName, sendedError
                            .toString()));

                    doSend = false;
                    continue;
                }

                lastSendedErrorCounter = sendedError.getCounter();
                if (sendedError.getCounter() >= jobChain.getNotifications()) {
                    LOGGER.debug(String.format("%s: [%s][%s][skip][sendedError][%s]counter %s >= configured notifications %s", method, notifyMsg,
                            serviceName, sendedError.toString(), sendedError.getCounter(), jobChain.getNotifications()));

                    doSend = false;
                    continue;
                }
                doSend = true;
            }

            LOGGER.debug(String.format("%s: [%s][%s]doSend=%s", method, notifyMsg, serviceName, doSend));

            if (jcn.getLastStepForNotification().getError()) {
                if (!doSend) {
                    if (jcn.getLastStepForNotification().getOrderEndTime() != null && lastSendedErrorCounter >= jobChain.getNotifications()) {
                        counter.addSkip();
                        setMaxNotifications(false, sm);
                        LOGGER.debug(String.format("%s: [%s][%s][skip][order is completed]lastSendedErrorCounter %s >= configured notifications %s",
                                method, notifyMsg, serviceName, lastSendedErrorCounter, jobChain.getNotifications()));
                    }

                    return;
                }
                results.update(jcn.getLastStepForNotification().getId(), lastState4Results, jcn.getLastStepForNotification().getStep());
            } else {
                if (jcn.getLastStepForNotification().getOrderEndTime() == null) {
                    counter.addSkip();
                    LOGGER.debug(String.format("%s: [%s][%s][skip][notification without error and order is not completed]%s", method, notifyMsg,
                            serviceName, SOSHibernateFactory.toString(jcn.getLastStepForNotification())));
                    return;
                } else {
                    sm.setRecovered(true);
                    if (jcn.getStepTo() != null) {
                        sm.setStepToEndTime(jcn.getLastStepForNotification().getOrderStepEndTime());
                    }
                    setMaxNotifications(false, sm);
                    counter.addSkip();
                    LOGGER.debug(String.format("%s: [%s][%s][skip][notifications already recovered][sm id=%s, currentNotification=%s, recovered=%s]",
                            method, notifyMsg, serviceName, sm.getId(), sm.getCurrentNotification(), sm.getRecovered()));
                    return;
                }
            }
            LOGGER.debug(String.format("%s: [results]%s", method, results.toString()));
        }// notifyOnError
        else {
            if (sm.getCurrentNotification() >= jobChain.getNotifications()) {
                setMaxNotifications(false, sm);
                counter.addSkip();
                LOGGER.debug(String.format(
                        "%s: [%s][%s][skip][count notifications was reached][sm.id=%s, sm.currentNotification=%s, configured notifications=%s]",
                        method, notifyMsg, serviceName, sm.getId(), sm.getCurrentNotification(), jobChain.getNotifications()));
                return;
            }

            if (sm.getCurrentNotification() + 1 >= jobChain.getNotifications()) {
                maxNotifications = true;
            }
        }

        try {
            if (notifyOnError) {
                sendJobChainError(pl, sm, jobChain, jcn, results, notifyMsg, serviceName, maxNotifications);
            } else {
                sendJobChainSuccess(pl, sm, jobChain, jcn, notifyMsg, serviceName, maxNotifications);
            }

            getDbLayer().getSession().beginTransaction();
            getDbLayer().getSession().update(sm);
            getDbLayer().getSession().commit();
            counter.addSuccess();
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

    private void sendJobChainError(ISystemNotifierPlugin pl, DBItemSchedulerMonSystemNotifications sm, ElementNotificationJobChain jobChain,
            JobChainNotification jcn, SystemNotifierResults results, String notifyMsg, String serviceName, boolean maxNotifications)
            throws Exception {
        String method = "sendJobChainError";

        if (jcn.getStepFrom() != null) {
            sm.setStepFromStartTime(jcn.getStepFrom().getOrderStepStartTime());
        }
        if (jcn.getStepTo() != null) {
            sm.setStepToEndTime(jcn.getStepTo().getOrderStepEndTime());
        }
        sm.setCurrentNotification(sm.getCurrentNotification() + 1);
        sm.setMaxNotifications(maxNotifications);
        sm.setNotifications(jobChain.getNotifications());

        sm.setSuccess(false);
        sm.setNotificationResults(results.toString());
        sm.setModified(DBLayer.getCurrentDateTime());

        String jobChainInfo = jcn.getLastStepForNotification().getJobChainName() + "-" + jcn.getLastStepForNotification().getOrderId();
        LOGGER.info(String.format(CALL_PLUGIN_LOGGING, method, notifyMsg, serviceName, jobChainInfo, sm.getCurrentNotification(), sm
                .getNotifications(), pl.getClass().getSimpleName()));
        pl.notifySystem(getSpooler(), options, getDbLayer(), jcn.getLastStepForNotification(), sm, null, EServiceStatus.CRITICAL,
                EServiceMessagePrefix.ERROR);

    }

    private void sendJobChainRecovery(ISystemNotifierPlugin pl, DBItemSchedulerMonSystemNotifications sm, ElementNotificationJobChain jobChain,
            JobChainNotification jcn, DBItemSchedulerMonNotifications notification, SystemNotifierResults results, String notifyMsg,
            String serviceName, boolean maxNotifications) throws Exception {
        String method = "sendJobChainRecovery";

        if (jcn.getStepFrom() != null) {
            sm.setStepFromStartTime(jcn.getStepFrom().getOrderStepStartTime());
        }
        if (jcn.getStepTo() != null) {
            sm.setStepToEndTime(jcn.getStepTo().getOrderStepEndTime());
        }
        sm.setCurrentNotification(sm.getCurrentNotification() + 1);
        sm.setMaxNotifications(maxNotifications);
        sm.setNotifications(jobChain.getNotifications());

        sm.setSuccess(false);
        sm.setNotificationResults(results.toString());
        sm.setModified(DBLayer.getCurrentDateTime());

        String jobChainInfo = jcn.getLastStepForNotification().getJobChainName() + "-" + jcn.getLastStepForNotification().getOrderId();
        LOGGER.info(String.format(CALL_PLUGIN_LOGGING, method, notifyMsg, serviceName, jobChainInfo, sm.getCurrentNotification(), sm
                .getNotifications(), pl.getClass().getSimpleName()));
        pl.notifySystem(getSpooler(), options, getDbLayer(), notification, sm, null, EServiceStatus.OK, EServiceMessagePrefix.RECOVERED);

        getDbLayer().getSession().beginTransaction();
        getDbLayer().getSession().update(sm);
        getDbLayer().getSession().commit();

    }

    private void sendJobChainSuccess(ISystemNotifierPlugin pl, DBItemSchedulerMonSystemNotifications sm, ElementNotificationJobChain jobChain,
            JobChainNotification jcn, String notifyMsg, String serviceName, boolean maxNotifications) throws Exception {
        String method = "sendJobChainSuccess";

        if (jcn.getStepFrom() != null) {
            sm.setStepFromStartTime(jcn.getStepFrom().getOrderStepStartTime());
        }
        if (jcn.getStepTo() != null) {
            sm.setStepToEndTime(jcn.getStepTo().getOrderStepEndTime());
        }
        sm.setCurrentNotification(sm.getCurrentNotification() + 1);
        sm.setMaxNotifications(maxNotifications);
        sm.setNotifications(jobChain.getNotifications());

        sm.setSuccess(true);
        sm.setModified(DBLayer.getCurrentDateTime());

        String jobChainInfo = jcn.getLastStepForNotification().getJobChainName() + "-" + jcn.getLastStepForNotification().getOrderId();
        LOGGER.info(String.format(CALL_PLUGIN_LOGGING, method, notifyMsg, serviceName, jobChainInfo, sm.getCurrentNotification(), sm
                .getNotifications(), pl.getClass().getSimpleName()));
        pl.notifySystem(getSpooler(), options, getDbLayer(), jcn.getLastStepForNotification(), sm, null, EServiceStatus.OK,
                EServiceMessagePrefix.NONE);
    }

    private void insertDummySysNotification4NotifyNew(String systemId, Long notificationId) {
        String method = "insertDummySysNotification4NotifyNew";
        try {
            LOGGER.debug(String.format("%s", method));
            getDbLayer().getSession().beginTransaction();
            getDbLayer().deleteDummySystemNotification(systemId);

            DBItemSchedulerMonSystemNotifications sm = getDbLayer().createDummySystemNotification(systemId, notificationId);
            getDbLayer().getSession().save(sm);
            getDbLayer().getSession().commit();
        } catch (Exception ex) {
            LOGGER.warn(String.format("%s:%s", method, ex.toString()));
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
            LOGGER.info(String.format("%s: skip notify timer. found 0 Timer definitions", method));
        }
    }

    /** only service_on_success defined -> send to service_on_success */
    /** only service_on_error defined -> send to service_on_error */
    /** service_on_success and service_on_error defined -> send to service_on_error */
    private void notifyTimer(String systemId, ArrayList<ElementNotificationTimerRef> timersOnSuccess,
            ArrayList<ElementNotificationTimerRef> timersOnError) throws Exception {
        String method = "notifyTimer";
        List<DBItemSchedulerMonChecks> result = getDbLayer().getChecksForNotifyTimer(largeResultFetchSize);
        LOGGER.info(String.format(
                "%s: found %s \"service_name_on_success\" and %s \"service_name_on_error\" timer definitions and %s checks for timers in the db",
                method, timersOnSuccess.size(), timersOnError.size(), result.size()));
        initSendCounters();
        for (DBItemSchedulerMonChecks check : result) {
            LOGGER.debug(String.format("%s: notify timer \"service_name_on_success\"", method));
            for (int i = 0; i < timersOnSuccess.size(); i++) {
                counter.addTotal();
                ElementNotificationTimerRef t = timersOnSuccess.get(i);

                if (!SOSString.isEmpty(t.getMonitor().getServiceNameOnError()) && !SOSString.isEmpty(t.getMonitor().getServiceNameOnSuccess())) {
                    counter.addSkip();
                    LOGGER.debug(String.format("%s:[skip notify on success][check %s later for serviceName=%s]", method, t.getRef(), t.getMonitor()
                            .getServiceNameOnError()));
                    continue;
                }

                if (checkDoNotificationTimer(check, t)) {
                    executeNotifyTimer(systemId, check, t, false);
                } else {
                    counter.addSkip();
                }
            }
            LOGGER.debug(String.format("%s: notify timer \"service_name_on_error\"", method));
            for (int i = 0; i < timersOnError.size(); i++) {
                counter.addTotal();
                ElementNotificationTimerRef t = timersOnError.get(i);
                if (checkDoNotificationTimer(check, t)) {
                    executeNotifyTimer(systemId, check, t, true);
                } else {
                    counter.addSkip();
                }
            }
        }
        LOGGER.info(String.format(SENT_LOGGING, method, counter.getSuccess(), counter.getError(), counter.getSkip(), counter.getTotal()));
    }

    private void notifyAgain(String systemId) throws Exception {
        String method = "notifyAgain";

        List<DBItemSchedulerMonSystemNotifications> result = getDbLayer().getSystemNotifications4NotifyAgain(systemId);
        LOGGER.info(String.format("%s: [%s] found %s system notifications in the db for notify again", method, systemId, result.size()));
        initSendCounters();
        for (DBItemSchedulerMonSystemNotifications systemNotification : result) {
            counter.addTotal();
            DBItemSchedulerMonNotifications notification = getDbLayer().getNotification(systemNotification.getNotificationId());
            if (notification == null) {
                counter.addSkip();
                continue;
            }

            if (!systemNotification.getCheckId().equals(new Long(0))) {
                // timer
                counter.addSkip();
                continue;
            }
            if (systemNotification.getObjectType().equals(DBLayer.NOTIFICATION_OBJECT_TYPE_DUMMY)) {
                // dummy for max notification
                counter.addSkip();
                continue;
            } else if (systemNotification.getObjectType().equals(DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN)) {
                Long currentNotificationBefore = systemNotification.getCurrentNotification();
                for (int i = 0; i < monitorJobChains.size(); i++) {
                    counter.addTotal();
                    ElementNotificationJobChain jc = monitorJobChains.get(i);
                    if (checkDoNotification(notification, jc)) {
                        String serviceNameOnError = SOSString.isEmpty(jc.getMonitor().getServiceNameOnError()) ? "" : jc.getMonitor()
                                .getServiceNameOnError();
                        String serviceNameOnSuccess = SOSString.isEmpty(jc.getMonitor().getServiceNameOnSuccess()) ? "" : jc.getMonitor()
                                .getServiceNameOnSuccess();
                        if (systemNotification.getServiceName().equalsIgnoreCase(serviceNameOnError) || systemNotification.getServiceName()
                                .equalsIgnoreCase(serviceNameOnSuccess)) {
                            executeNotifyJobChain(systemNotification, systemId, notification, jc);
                        } else {
                            counter.addSkip();
                        }
                    } else {
                        counter.addSkip();
                    }
                }
                Long currentNotificationAfter = systemNotification.getCurrentNotification();
                if (currentNotificationAfter.equals(currentNotificationBefore) && systemNotification.getStepToEndTime() != null) {
                    // LOGGER.debug(String.format("%s: [%s] disable notify again (system notification(id = %s) was not sent. maybe the JobChain configuration
                    // was changed)",method,systemId,systemNotification.getId()));
                    // setMaxNotifications(false,systemNotification);
                }

            } else if (systemNotification.getObjectType().equals(DBLayer.NOTIFICATION_OBJECT_TYPE_JOB)) {
                for (int i = 0; i < monitorJobs.size(); i++) {
                    counter.addTotal();
                    ElementNotificationJob job = monitorJobs.get(i);
                    if (checkDoNotification(notification, job)) {
                        String serviceNameOnError = SOSString.isEmpty(job.getMonitor().getServiceNameOnError()) ? "" : job.getMonitor()
                                .getServiceNameOnError();
                        String serviceNameOnSuccess = SOSString.isEmpty(job.getMonitor().getServiceNameOnSuccess()) ? "" : job.getMonitor()
                                .getServiceNameOnSuccess();

                        if ((systemNotification.getServiceName().equalsIgnoreCase(serviceNameOnError) || systemNotification.getServiceName()
                                .equalsIgnoreCase(serviceNameOnSuccess))) {
                            executeNotifyJob(systemNotification, systemId, notification, job);
                        } else {
                            counter.addSkip();
                        }
                    } else {
                        counter.addSkip();
                    }
                }
            }
        }
        LOGGER.info(String.format("%s: sended=%s, error=%s, skipped=%s (total checked=%s)", method, counter.getSuccess(), counter.getError(), counter
                .getSkip(), counter.getTotal()));
    }

    private void notifyNew(String systemId) throws Exception {
        String method = "notifyNew";

        List<DBItemSchedulerMonNotifications> result = getDbLayer().getNotifications4NotifyNew(systemId);
        LOGGER.info(String.format("%s: [%s] found %s new notifications in the db", method, systemId, result.size()));
        initSendCounters();

        ArrayList<String> checkedJobchans = new ArrayList<String>();
        Long maxNotificationId = new Long(0);
        for (DBItemSchedulerMonNotifications notification : result) {
            counter.addTotal();

            if (notification.getId() > maxNotificationId) {
                maxNotificationId = notification.getId();
            }

            if (!notification.getStandalone()) {
                String identifier = notification.getOrderHistoryId().toString();
                if (notification.getStep().equals(new Long(1))) {// only for 1st
                                                                 // step
                    if (!checkedJobchans.contains(identifier)) {
                        checkedJobchans.add(identifier);
                        for (int i = 0; i < monitorJobChains.size(); i++) {
                            counter.addTotal();

                            ElementNotificationJobChain jc = monitorJobChains.get(i);
                            if (checkDoNotification(notification, jc)) {
                                executeNotifyJobChain(null, systemId, notification, jc);
                            } else {
                                counter.addSkip();
                            }
                        }
                    }
                } else {
                    if (checkedJobchans.contains(identifier)) {
                        counter.addSkip();
                        LOGGER.debug(String.format(
                                "%s: [%s][skip analyze JobChain notification][step greater than 1][notification id=%s, jobChainName=%s, step=%s, orderStepState=%s]",
                                method, systemId, notification.getId(), notification.getJobChainName(), notification.getStep(), notification
                                        .getOrderStepState()));
                    } else {
                        checkedJobchans.add(identifier);
                        DBItemSchedulerMonNotifications n = getDbLayer().getNotificationFirstStep(notification);
                        if (n != null) {
                            for (int i = 0; i < monitorJobChains.size(); i++) {
                                counter.addTotal();

                                ElementNotificationJobChain jc = monitorJobChains.get(i);
                                if (checkDoNotification(n, jc)) {
                                    executeNotifyJobChain(null, systemId, n, jc);
                                } else {
                                    counter.addSkip();
                                }
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < monitorJobs.size(); i++) {
                counter.addTotal();

                ElementNotificationJob job = monitorJobs.get(i);
                if (checkDoNotification(notification, job)) {
                    executeNotifyJob(null, systemId, notification, job);
                } else {
                    counter.addSkip();
                }
            }
        }

        LOGGER.info(String.format("%s: sended=%s, error=%s, skipped=%s (total checked=%s)", method, counter.getSuccess(), counter.getError(), counter
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
