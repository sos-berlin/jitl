package com.sos.jitl.notification.model.internal;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.jitl.notification.db.DBItemReportingTaskAndOrder;
import com.sos.jitl.notification.db.DBItemSchedulerMonInternalNotifications;
import com.sos.jitl.notification.db.DBItemSchedulerMonNotifications;
import com.sos.jitl.notification.db.DBItemSchedulerMonSystemNotifications;
import com.sos.jitl.notification.db.DBLayer;
import com.sos.jitl.notification.helper.EServiceMessagePrefix;
import com.sos.jitl.notification.helper.EServiceStatus;
import com.sos.jitl.notification.helper.ElementNotificationInternal;
import com.sos.jitl.notification.helper.ElementNotificationInternalMasterMessage;
import com.sos.jitl.notification.helper.ElementNotificationInternalTaskIfLongerThan;
import com.sos.jitl.notification.helper.ElementNotificationInternalTaskIfShorterThan;
import com.sos.jitl.notification.helper.ElementNotificationInternalTaskWarning;
import com.sos.jitl.notification.helper.ElementNotificationMonitor;
import com.sos.jitl.notification.helper.NotificationMail;
import com.sos.jitl.notification.helper.NotificationXmlHelper;
import com.sos.jitl.notification.helper.settings.InternalNotificationSettings;
import com.sos.jitl.notification.helper.settings.MailSettings;
import com.sos.jitl.notification.jobs.notifier.SystemNotifierJobOptions;
import com.sos.jitl.notification.model.NotificationModel;
import com.sos.jitl.notification.model.notifier.SystemNotifierModel;
import com.sos.jitl.notification.plugins.notifier.ISystemNotifierPlugin;

import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

public class ExecutorModel extends NotificationModel {

    public enum InternalType {
        TASK_IF_LONGER_THAN, TASK_IF_SHORTER_THAN, TASK_WARNING, MASTER_MESSAGE
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorModel.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private final Path configurationDirectory;
    private final Path hibernateConfiguration;
    private SOSHibernateFactory factory;
    private SystemNotifierJobOptions options = null;

    public ExecutorModel(Path configDir, Path hibernateFile, MailSettings settings) {
        configurationDirectory = configDir;
        hibernateConfiguration = hibernateFile;
        options = new SystemNotifierJobOptions();
        options.scheduler_mail_settings.setValue(NotificationMail.getSchedulerMailOptions(settings));
    }

    public boolean process(InternalType type, InternalNotificationSettings settings) {
        String method = "process";

        boolean toNotify = false;
        try {
            File dir = new File(configurationDirectory.toFile().getCanonicalPath(), "notification");
            if (!dir.exists()) {
                throw new Exception(String.format("[%s][%s]directory not exists", method, dir));
            }

            File[] files = getAllConfigurationFiles(dir);
            if (files.length == 0) {
                throw new Exception(String.format("[%s][configuration files not found]%s", method, dir.getCanonicalPath()));
            }

            Long notificationObjectType = null;
            switch (type) {
            case TASK_IF_LONGER_THAN:
                notificationObjectType = DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_TASK_IF_LONGER_THAN;
                break;
            case TASK_IF_SHORTER_THAN:
                notificationObjectType = DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_TASK_IF_SHORTER_THAN;
                break;
            case TASK_WARNING:
                notificationObjectType = DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_TASK_WARNING;
                break;
            case MASTER_MESSAGE:
                notificationObjectType = DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_MASTER_MESSAGE;
                break;
            default:
                throw new Exception(String.format("[%s]not implemented yet", type.name()));

            }

            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s]%s", method, (i + 1), type.name(), f.getCanonicalPath()));
                }
                boolean ok = handleConfigFile(notificationObjectType, settings, f);
                if (ok) {
                    toNotify = true;
                }

            }
        } catch (Throwable ex) {
            LOGGER.error(String.format("[%s]%s", method, ex.toString()), ex);
        }

        return toNotify;
    }

    private boolean handleConfigFile(Long notificationObjectType, InternalNotificationSettings settings, File xmlFile) throws Exception {
        String method = "handleConfigFile";

        String xmlFilePath = null;
        SOSXMLXPath xpath = null;
        Node node = null;
        boolean toNotify = false;

        List<ElementNotificationInternal> objects = new ArrayList<>();
        try {
            xmlFilePath = xmlFile.getCanonicalPath();
            xpath = new SOSXMLXPath(xmlFilePath);

            NodeList monitorList = NotificationXmlHelper.selectNotificationMonitorDefinitions(xpath);
            for (int i = 0; i < monitorList.getLength(); i++) {
                Node n = monitorList.item(i);

                ElementNotificationMonitor monitor = new ElementNotificationMonitor(n, options);
                if (monitor.getMonitorInterface() == null) {
                    LOGGER.warn(String.format("[%s][%s][skip]missing Notification element", method, xmlFilePath));
                    continue;
                }
                if (SOSString.isEmpty(monitor.getServiceNameOnError())) {
                    LOGGER.warn(String.format("[%s][%s][skip]missing service_name_on_error", method, xmlFilePath));
                    continue;
                }

                if (notificationObjectType.equals(DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_TASK_IF_LONGER_THAN)) {
                    node = NotificationXmlHelper.selectNotificationMonitorInternalTaskIfLongerThan(xpath, n);
                    if (node != null) {
                        ElementNotificationInternalTaskIfLongerThan el = new ElementNotificationInternalTaskIfLongerThan(monitor, node);
                        if (SystemNotifierModel.checkDoNotifyInternal(0, settings.getSchedulerId(), el)) {
                            objects.add(el);
                        }
                    }
                } else if (notificationObjectType.equals(DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_TASK_IF_SHORTER_THAN)) {
                    node = NotificationXmlHelper.selectNotificationMonitorInternalTaskIfShorterThan(xpath, n);
                    if (node != null) {
                        ElementNotificationInternalTaskIfShorterThan el = new ElementNotificationInternalTaskIfShorterThan(monitor, node);
                        if (SystemNotifierModel.checkDoNotifyInternal(0, settings.getSchedulerId(), el)) {
                            objects.add(el);
                        }
                    }
                } else if (notificationObjectType.equals(DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_TASK_WARNING)) {
                    node = NotificationXmlHelper.selectNotificationMonitorInternalTaskWarning(xpath, n);
                    if (node != null) {
                        ElementNotificationInternalTaskWarning el = new ElementNotificationInternalTaskWarning(monitor, node);
                        if (SystemNotifierModel.checkDoNotifyInternal(0, settings.getSchedulerId(), el)) {
                            objects.add(el);
                        }
                    }
                } else if (notificationObjectType.equals(DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_MASTER_MESSAGE)) {
                    node = NotificationXmlHelper.selectNotificationMonitorInternalMasterMessage(xpath, n);
                    if (node != null) {
                        ElementNotificationInternalMasterMessage el = new ElementNotificationInternalMasterMessage(monitor, node);
                        if (SystemNotifierModel.checkDoNotifyInternal(0, settings.getSchedulerId(), el)) {
                            objects.add(el);
                        }
                    }
                }
            }

            if (objects.size() > 0) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s]found %s definitions", method, xmlFilePath, objects.size()));
                }
                String systemId = NotificationXmlHelper.getSystemMonitorNotificationSystemId(xpath);
                sendNotifications(settings, systemId, objects, notificationObjectType);
                toNotify = true;
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s]not found definitions", method, xmlFilePath));
                }
            }

        } catch (Exception e) {
            throw new Exception(String.format("[%s][%s]%s", method, xmlFilePath, e.toString()), e);
        }
        return toNotify;
    }

    private void sendNotifications(InternalNotificationSettings settings, String systemId, List<ElementNotificationInternal> objects,
            Long notificationObjectType) throws Exception {
        String method = "sendNotifications";

        try {
            buildFactory();
            setConnection(factory.openStatelessSession());

            DBItemSchedulerMonNotifications notification2send = getNotification2Send(settings, notificationObjectType);
            for (int i = 0; i < objects.size(); i++) {
                notify(objects.get(i), notificationObjectType, systemId, notification2send);
            }

        } catch (Throwable ex) {
            try {
                if (getDbLayer().getSession() != null) {
                    getDbLayer().getSession().rollback();
                }
            } catch (Exception e) {
            }
            throw new Exception(String.format("[%s]%s", method, ex.toString()), ex);
        } finally {
            closeFactory();
        }

    }

    private void notify(ElementNotificationInternal object, Long notificationObjectType, String systemId,
            DBItemSchedulerMonNotifications notification2send) throws Exception {
        String method = "notify";

        ElementNotificationMonitor monitor = object.getMonitor();
        String serviceName = monitor.getServiceNameOnError();
        ISystemNotifierPlugin pl = monitor.getOrCreatePluginObject();
        if (pl.hasErrorOnInit()) {
            throw new Exception(String.format("[%s][skip]due plugin init error: %s", method, pl.getInitError()));
        }

        DBItemSchedulerMonSystemNotifications sn = getSystemNotification(object, notificationObjectType, systemId, notification2send, serviceName);
        if (sn.getMaxNotifications()) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][skip]maxNotifications=true", method, serviceName));
            }
            return;
        }
        if (sn.getCurrentNotification() >= object.getNotifications()) {
            closeSystemNotification(sn);

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][skip][%s]count notifications was reached", method, serviceName, object.getNotifications()));
            }
            return;
        }

        sn.setCurrentNotification(sn.getCurrentNotification() + 1);
        if (sn.getCurrentNotification() >= object.getNotifications() || sn.getAcknowledged()) {
            sn.setMaxNotifications(true);
        }
        sn.setNotifications(object.getNotifications());
        sn.setModified(DBLayer.getCurrentDateTime());

        try {
            EServiceStatus serviceStatus = EServiceStatus.CRITICAL;
            EServiceMessagePrefix serviceMessagePrefix = EServiceMessagePrefix.ERROR;
            if (!notification2send.getError()) {
                serviceStatus = EServiceStatus.OK;
                serviceMessagePrefix = EServiceMessagePrefix.SUCCESS;
            }

            pl.notifySystem(null, options, getDbLayer(), notification2send, sn, null, serviceStatus, serviceMessagePrefix);

            getDbLayer().getSession().beginTransaction();
            getDbLayer().getSession().update(sn);
            getDbLayer().getSession().commit();
        } catch (Exception ex) {
            try {
                getDbLayer().getSession().rollback();
            } catch (Exception e) {
            }

            LOGGER.error(String.format("[%s][error on message sending]%s", method, ex.toString()), ex);
        }

    }

    private DBItemSchedulerMonNotifications getNotification2Send(InternalNotificationSettings settings, Long notificationObjectType)
            throws Exception {

        if (notificationObjectType.equals(DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_MASTER_MESSAGE)) {
            return getNotification2SendForMasterMessages(settings, notificationObjectType);
        } else {
            return getNotification2SendForTaskMessages(settings, notificationObjectType);
        }
    }

    private DBItemSchedulerMonNotifications getNotification2SendForMasterMessages(InternalNotificationSettings settings, Long notificationObjectType)
            throws Exception {

        DBItemSchedulerMonNotifications notification2send = new DBItemSchedulerMonNotifications();
        notification2send.setSchedulerId(settings.getSchedulerId());
        notification2send.setError(true);
        notification2send.setErrorCode(settings.getMessageCode());
        notification2send.setErrorText(settings.getMessage());
        notification2send.setCreated(DBLayer.getCurrentDateTime());
        notification2send.setModified(notification2send.getCreated());

        getDbLayer().getSession().beginTransaction();
        DBItemSchedulerMonInternalNotifications internalNotification = createInternalNotification(notification2send, notificationObjectType);
        getDbLayer().getSession().save(internalNotification);
        getDbLayer().getSession().commit();

        notification2send.setId(internalNotification.getId());

        return notification2send;
    }

    private DBItemSchedulerMonNotifications getNotification2SendForTaskMessages(InternalNotificationSettings settings, Long notificationObjectType)
            throws Exception {
        String method = "getNotification2SendForTaskMessages";

        Long taskId = Long.parseLong(settings.getTaskId());

        DBItemSchedulerMonNotifications notification2send = new DBItemSchedulerMonNotifications();
        notification2send.setSchedulerId(settings.getSchedulerId());
        notification2send.setTaskId(taskId);
        notification2send.setError(true);
        notification2send.setErrorCode(settings.getMessageCode());
        notification2send.setErrorText(settings.getMessage());
        notification2send.setCreated(DBLayer.getCurrentDateTime());
        notification2send.setModified(notification2send.getCreated());

        getDbLayer().getSession().beginTransaction();
        List<DBItemReportingTaskAndOrder> results = getDbLayer().getReportingTaskAndOrder(settings.getSchedulerId(), taskId);
        for (int i = 0; i < results.size(); i++) {
            DBItemReportingTaskAndOrder item = results.get(i);

            if (i > 0) {
                if (item.getOrderHistoryId() < notification2send.getOrderHistoryId()) {
                    continue;
                }
            }

            notification2send.setStandalone(!item.getIsOrder());

            notification2send.setJobChainName(item.getJobChainName());
            notification2send.setJobChainTitle(item.getJobChainTitle());

            notification2send.setOrderHistoryId(item.getOrderHistoryId());
            notification2send.setOrderId(item.getOrderId());
            notification2send.setOrderTitle(item.getOrderTitle());
            notification2send.setOrderStartTime(item.getOrderStartTime());
            notification2send.setOrderEndTime(item.getOrderEndTime());

            notification2send.setStep(item.getOrderStep());
            notification2send.setOrderStepState(item.getOrderStepState());
            notification2send.setOrderStepStartTime(item.getOrderStepStartTime());
            notification2send.setOrderStepEndTime(item.getOrderStepEndTime());

            notification2send.setJobName(item.getJobName());
            notification2send.setJobTitle(item.getJobTitle());
            notification2send.setTaskStartTime(item.getTaskStartTime());
            notification2send.setTaskEndTime(item.getTaskEndTime());
            notification2send.setReturnCode(new Long(item.getExitCode() == null ? 0 : item.getExitCode().intValue()));
            notification2send.setAgentUrl(item.getAgentUrl());
            notification2send.setClusterMemberId(item.getClusterMemberId());
        }
        if (results.size() == 0) {
            LOGGER.warn(String.format("[%s][schedulerId=%s][taskId=%s]not found entries", method, settings.getSchedulerId(), taskId));
        }

        DBItemSchedulerMonInternalNotifications internalNotification = getDbLayer().getInternalNotificationByTaskId(notification2send
                .getSchedulerId(), notification2send.getTaskId(), notificationObjectType);
        if (internalNotification == null) {
            internalNotification = createInternalNotification(notification2send, notificationObjectType);
            getDbLayer().getSession().save(internalNotification);
        } else {
            if (!isEquals(internalNotification, notification2send)) {
                internalNotification = updateInternalNotification(internalNotification, notification2send);
                getDbLayer().getSession().update(internalNotification);
            }
        }
        getDbLayer().getSession().commit();

        notification2send.setId(internalNotification.getId());

        return notification2send;
    }

    private DBItemSchedulerMonSystemNotifications getSystemNotification(ElementNotificationInternal object, Long notificationObjectType,
            String systemId, DBItemSchedulerMonNotifications notification2send, String serviceName) throws Exception {

        Long checkId = new Long(0);
        String stepFrom = DBLayer.DEFAULT_EMPTY_NAME;
        String stepTo = DBLayer.DEFAULT_EMPTY_NAME;
        String returnCodeFrom = DBLayer.DEFAULT_EMPTY_NAME;
        String returnCodeTo = DBLayer.DEFAULT_EMPTY_NAME;
        boolean isSuccess = false;

        getDbLayer().getSession().beginTransaction();
        DBItemSchedulerMonSystemNotifications sn = getDbLayer().getSystemNotification(systemId, serviceName, notification2send.getId(), checkId,
                notificationObjectType, isSuccess, stepFrom, stepTo, returnCodeFrom, returnCodeTo);
        if (sn == null) {
            sn = getDbLayer().createSystemNotification(systemId, serviceName, notification2send.getId(), checkId, returnCodeFrom, returnCodeTo,
                    notificationObjectType, stepFrom, stepTo, notification2send.getTaskStartTime(), notification2send.getTaskEndTime(), new Long(0),
                    object.getNotifications(), false, false, isSuccess);
            getDbLayer().getSession().save(sn);
        }
        getDbLayer().getSession().commit();
        return sn;
    }

    private void closeSystemNotification(DBItemSchedulerMonSystemNotifications sn) throws Exception {
        getDbLayer().getSession().beginTransaction();
        getDbLayer().getSession().update(sn);
        getDbLayer().getSession().commit();
    }

    private boolean isEquals(DBItemSchedulerMonInternalNotifications internalNotification, DBItemSchedulerMonNotifications notification) {
        if (!internalNotification.getOrderHistoryId().equals(notification.getOrderHistoryId())) {
            return false;
        }
        return true;
    }

    private DBItemSchedulerMonInternalNotifications updateInternalNotification(DBItemSchedulerMonInternalNotifications internalNotification,
            DBItemSchedulerMonNotifications notification) {
        internalNotification.setStandalone(notification.getStandalone());

        internalNotification.setJobChainName(notification.getJobChainName());
        internalNotification.setJobChainTitle(notification.getJobChainTitle());

        internalNotification.setOrderHistoryId(notification.getOrderHistoryId());
        internalNotification.setOrderId(notification.getOrderId());
        internalNotification.setOrderTitle(notification.getOrderTitle());
        internalNotification.setOrderStartTime(notification.getOrderStartTime());
        internalNotification.setOrderEndTime(notification.getOrderEndTime());

        internalNotification.setStep(notification.getStep());
        internalNotification.setOrderStepState(notification.getOrderStepState());
        internalNotification.setOrderStepStartTime(notification.getOrderStepStartTime());
        internalNotification.setOrderStepEndTime(notification.getOrderStepEndTime());

        internalNotification.setJobName(notification.getJobName());
        internalNotification.setJobTitle(notification.getJobTitle());
        internalNotification.setTaskStartTime(notification.getTaskStartTime());
        internalNotification.setTaskEndTime(notification.getTaskEndTime());
        internalNotification.setReturnCode(notification.getReturnCode());
        internalNotification.setAgentUrl(notification.getAgentUrl());
        internalNotification.setClusterMemberId(notification.getClusterMemberId());

        internalNotification.setMessageCode(notification.getErrorCode());
        internalNotification.setMessage(notification.getErrorText());

        internalNotification.setModified(notification.getModified());
        return internalNotification;
    }

    private DBItemSchedulerMonInternalNotifications createInternalNotification(DBItemSchedulerMonNotifications notification,
            Long notificationObjectType) {
        DBItemSchedulerMonInternalNotifications internalNotification = new DBItemSchedulerMonInternalNotifications();
        internalNotification.setSchedulerId(notification.getSchedulerId());
        internalNotification.setObjectType(notificationObjectType);
        internalNotification.setStandalone(notification.getStandalone());
        internalNotification.setTaskId(notification.getTaskId());
        internalNotification.setStep(notification.getStep());
        internalNotification.setOrderHistoryId(notification.getOrderHistoryId());
        internalNotification.setJobChainName(notification.getJobChainName());
        internalNotification.setJobChainTitle(notification.getJobChainTitle());
        internalNotification.setOrderId(notification.getOrderId());
        internalNotification.setOrderTitle(notification.getOrderTitle());
        internalNotification.setOrderStartTime(notification.getOrderStartTime());
        internalNotification.setOrderEndTime(notification.getOrderEndTime());
        internalNotification.setOrderStepState(notification.getOrderStepState());
        internalNotification.setOrderStepStartTime(notification.getOrderStepStartTime());
        internalNotification.setOrderStepEndTime(notification.getOrderStepEndTime());
        internalNotification.setJobName(notification.getJobName());
        internalNotification.setJobTitle(notification.getJobTitle());
        internalNotification.setTaskStartTime(notification.getTaskStartTime());
        internalNotification.setTaskEndTime(notification.getTaskEndTime());
        internalNotification.setReturnCode(notification.getReturnCode());
        internalNotification.setAgentUrl(notification.getAgentUrl());
        internalNotification.setClusterMemberId(notification.getClusterMemberId());
        internalNotification.setError(notification.getError());
        internalNotification.setMessageCode(notification.getErrorCode());
        internalNotification.setMessage(notification.getErrorText());
        internalNotification.setCreated(notification.getCreated());
        internalNotification.setModified(notification.getModified());
        return internalNotification;
    }

    private void buildFactory() throws Exception {
        if (factory == null) {
            factory = new SOSHibernateFactory(hibernateConfiguration);
            factory.setIdentifier("notification_internal");
            factory.setAutoCommit(false);
            factory.addClassMapping(DBLayer.getNotificationClassMapping());
            factory.addClassMapping(com.sos.jitl.reporting.db.DBLayer.getReportingClassMapping());
            factory.build();
        }
    }

    private void closeFactory() {
        if (getDbLayer().getSession() != null) {
            getDbLayer().getSession().close();
        }
        if (factory != null) {
            factory.close();
        }
    }

}
