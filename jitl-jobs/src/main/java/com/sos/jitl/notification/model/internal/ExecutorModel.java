package com.sos.jitl.notification.model.internal;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.notification.db.DBItemReportingTaskAndOrder;
import com.sos.jitl.notification.db.DBItemSchedulerMonInternalNotifications;
import com.sos.jitl.notification.db.DBItemSchedulerMonNotifications;
import com.sos.jitl.notification.db.DBItemSchedulerMonSystemNotifications;
import com.sos.jitl.notification.db.DBLayer;
import com.sos.jitl.notification.exceptions.SOSSystemNotifierSendException;
import com.sos.jitl.notification.helper.EServiceMessagePrefix;
import com.sos.jitl.notification.helper.EServiceStatus;
import com.sos.jitl.notification.helper.NotificationMail;
import com.sos.jitl.notification.helper.NotificationXmlHelper;
import com.sos.jitl.notification.helper.elements.monitor.ElementNotificationMonitor;
import com.sos.jitl.notification.helper.elements.objects.internal.ElementInternal;
import com.sos.jitl.notification.helper.elements.objects.internal.ElementMasterMessage;
import com.sos.jitl.notification.helper.elements.objects.internal.ElementTaskIfLongerThan;
import com.sos.jitl.notification.helper.elements.objects.internal.ElementTaskIfShorterThan;
import com.sos.jitl.notification.helper.elements.objects.internal.ElementTaskWarning;
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
    private boolean isNewSystemNotification = false;
    private InternalType internalType = null;

    public ExecutorModel(Path configDir, Path hibernateFile, MailSettings settings) {
        configurationDirectory = configDir;
        hibernateConfiguration = hibernateFile;
        options = new SystemNotifierJobOptions();
        options.scheduler_mail_settings.setValue(NotificationMail.getSchedulerMailOptions(settings));

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s]", configurationDirectory, hibernateConfiguration));
        }
    }

    public boolean process(InternalType type, InternalNotificationSettings settings) {
        String method = "process";
        internalType = type;
        boolean processed = false;
        try {
            if (SOSString.isEmpty(settings.getSchedulerId())) {
                throw new Exception("missing scheduler_id");
            }

            List<File> files = new ArrayList<File>();
            File defaultConfigFile = getDefaultNotificationXml();
            boolean useDefaultConfiguration = false;
            if (defaultConfigFile.exists()) {
                useDefaultConfiguration = true;
                files.add(defaultConfigFile);
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s]use default system configuration file", method, normalizePath(defaultConfigFile)));
                }
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s]default system configuration file not found", method, normalizePath(defaultConfigFile)));
                }
                Path notificationConfig = configurationDirectory.resolve("notification");
                File[] notificationConfigFiles = getDirectoryFiles(notificationConfig.toFile());
                if (notificationConfigFiles != null && notificationConfigFiles.length > 0) {
                    files.addAll(Arrays.asList(notificationConfigFiles));
                    if (isDebugEnabled) {
                        LOGGER.debug((String.format("[%s][%s]found %s file(s)", method, normalizePath(notificationConfig.toFile()), files.size())));
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug((String.format("[%s][%s]files not found", method, normalizePath(notificationConfig.toFile()))));
                    }
                }
            }

            if (files.size() == 0) {
                throw new Exception("missing configuration files");
            }

            Long notificationObjectType = null;
            Long taskId = null;
            switch (type) {
            case TASK_IF_LONGER_THAN:

                taskId = getTaskId(settings.getTaskId());
                notificationObjectType = getTaskNotificationObjectType(taskId, DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_TASK_IF_LONGER_THAN);
                break;

            case TASK_IF_SHORTER_THAN:

                taskId = getTaskId(settings.getTaskId());
                notificationObjectType = getTaskNotificationObjectType(taskId, DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_TASK_IF_SHORTER_THAN);
                break;

            case TASK_WARNING:

                taskId = getTaskId(settings.getTaskId());
                notificationObjectType = getTaskNotificationObjectType(taskId, DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_TASK_WARNING);
                break;

            case MASTER_MESSAGE:
                notificationObjectType = DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_MASTER_MESSAGE;
                break;
            default:
                throw new Exception(String.format("[%s]not implemented yet", type.name()));
            }

            buildFactory();
            for (int i = 0; i < files.size(); i++) {
                File f = files.get(i);
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s]%s", method, (i + 1), type.name(), normalizePath(f)));
                }
                boolean ok = handleConfigFile(useDefaultConfiguration, notificationObjectType, settings, f, taskId);
                if (ok) {
                    processed = true;
                }
            }

        } catch (Throwable ex) {
            LOGGER.error(String.format("[%s][%s]%s", method, type.name(), ex.toString()), ex);
        } finally {
            closeFactory();
        }

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s]processed=%s", method, type.name(), processed));
        }
        return processed;
    }

    private boolean handleConfigFile(boolean useDefaultConfiguration, Long notificationObjectType, InternalNotificationSettings settings,
            File xmlFile, Long taskId) throws Exception {
        String method = "handleConfigFile";

        String xmlFilePath = null;
        SOSXMLXPath xpath = null;
        Node node = null;
        boolean toNotify = false;

        List<ElementInternal> objects = new ArrayList<>();
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
                        ElementTaskIfLongerThan el = new ElementTaskIfLongerThan(monitor, node);
                        if (SystemNotifierModel.checkDoNotifyInternal(0, settings.getSchedulerId(), el)) {
                            objects.add(el);
                        }
                    }
                } else if (notificationObjectType.equals(DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_TASK_IF_SHORTER_THAN)) {
                    node = NotificationXmlHelper.selectNotificationMonitorInternalTaskIfShorterThan(xpath, n);
                    if (node != null) {
                        ElementTaskIfShorterThan el = new ElementTaskIfShorterThan(monitor, node);
                        if (SystemNotifierModel.checkDoNotifyInternal(0, settings.getSchedulerId(), el)) {
                            objects.add(el);
                        }
                    }
                } else if (notificationObjectType.equals(DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_TASK_WARNING)) {
                    node = NotificationXmlHelper.selectNotificationMonitorInternalTaskWarning(xpath, n);
                    if (node != null) {
                        ElementTaskWarning el = new ElementTaskWarning(monitor, node);
                        if (SystemNotifierModel.checkDoNotifyInternal(0, settings.getSchedulerId(), el)) {
                            objects.add(el);
                        }
                    }
                } else if (notificationObjectType.equals(DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_MASTER_MESSAGE)) {
                    node = NotificationXmlHelper.selectNotificationMonitorInternalMasterMessage(xpath, n);
                    if (node != null) {
                        ElementMasterMessage el = new ElementMasterMessage(monitor, node);
                        if (SystemNotifierModel.checkDoNotifyInternal(0, settings.getSchedulerId(), el)) {
                            objects.add(el);
                        }
                    }
                }
            }

            if (objects.size() > 0) {
                String systemId = useDefaultConfiguration ? NotificationModel.DEFAULT_SYSTEM_ID : NotificationXmlHelper
                        .getSystemMonitorNotificationSystemId(xpath);

                LOGGER.info(String.format("[%s][%s][%s][%s]%s definition(s)", method, internalType.name(), xmlFilePath, systemId, objects.size()));
                sendNotifications(settings, systemId, objects, notificationObjectType, taskId);
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

    private void sendNotifications(InternalNotificationSettings settings, String systemId, List<ElementInternal> objects, Long notificationObjectType,
            Long taskId) throws Exception {
        String method = "sendNotifications";

        SOSHibernateSession session = null;
        try {
            session = factory.openStatelessSession();
            createDbLayer(session);

            DBItemSchedulerMonNotifications notification2send = getNotification2Send(settings, notificationObjectType, taskId);

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][notification2send]%s", method, NotificationModel.toString(notification2send)));
            }

            for (int i = 0; i < objects.size(); i++) {
                notify(i + 1, settings, objects.get(i), notificationObjectType, systemId, notification2send);
            }

        } catch (Throwable ex) {
            try {
                if (session != null) {
                    session.rollback();
                }
            } catch (Exception e) {
            }
            throw new Exception(String.format("[%s]%s", method, ex.toString()), ex);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception e) {
            }
        }

    }

    private void notify(int currentCounter, InternalNotificationSettings settings, ElementInternal object, Long notificationObjectType,
            String systemId, DBItemSchedulerMonNotifications notification2send) throws Exception {
        String method = currentCounter + "][notify";

        ElementNotificationMonitor monitor = object.getMonitor();
        String serviceName = monitor.getServiceNameOnError();
        ISystemNotifierPlugin pl = monitor.getOrCreatePluginObject();
        if (pl.hasErrorOnInit()) {
            throw new Exception(String.format("[%s][%s][skip]due plugin init error: %s", method, serviceName, pl.getInitError()));
        }

        DBItemSchedulerMonSystemNotifications sn = getSystemNotification(settings, object, notificationObjectType, systemId, notification2send,
                serviceName);
        if (!isNewSystemNotification && sn.getMaxNotifications()) {
            LOGGER.info(String.format("[%s][%s][skip]maxNotifications=true", method, serviceName));
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s]%s", method, NotificationModel.toString(sn)));
            }
            return;
        }
        if (sn.getCurrentNotification() >= object.getNotifications()) {
            closeSystemNotification(sn);

            LOGGER.info(String.format("[%s][%s][skip][%s]count notifications was reached", method, serviceName, object.getNotifications()));
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s]%s", method, NotificationModel.toString(sn)));
            }
            return;
        }

        boolean originalMaxNotifications = isNewSystemNotification ? false : sn.getMaxNotifications();
        try {
            EServiceStatus serviceStatus = EServiceStatus.CRITICAL;
            EServiceMessagePrefix serviceMessagePrefix = EServiceMessagePrefix.ERROR;
            if (notification2send.getError()) {
                if (sn.getRecovered()) {
                    serviceStatus = EServiceStatus.OK;
                    serviceMessagePrefix = EServiceMessagePrefix.RECOVERED;
                }
            } else {
                serviceStatus = EServiceStatus.OK;
                serviceMessagePrefix = EServiceMessagePrefix.SUCCESS;
            }

            sn.setCurrentNotification(sn.getCurrentNotification() + 1);
            if (sn.getCurrentNotification() >= object.getNotifications() || sn.getAcknowledged()) {
                sn.setMaxNotifications(true);
            }
            sn.setNotifications(object.getNotifications());
            sn.setModified(DBLayer.getCurrentDateTime());

            LOGGER.info(String.format("[%s][%s][%s]notification %s of %s. call plugin %s", method, internalType.name(), serviceName, sn
                    .getCurrentNotification(), sn.getNotifications(), pl.getClass().getSimpleName()));

            pl.notifySystem(null, options, getDbLayer(), notification2send, sn, null, serviceStatus, serviceMessagePrefix);
        } catch (SOSSystemNotifierSendException ex) {
            sn.setCurrentNotification(sn.getCurrentNotification() - 1);
            sn.setMaxNotifications(originalMaxNotifications);
            LOGGER.error(String.format("[%s][%s][error on message sending]%s", method, serviceName, ex.toString()), ex);
        }

        try {
            getDbLayer().getSession().beginTransaction();
            getDbLayer().getSession().update(sn);
            getDbLayer().getSession().commit();
        } catch (Exception ex) {
            try {
                getDbLayer().getSession().rollback();
            } catch (Exception e) {
            }
        }

    }

    private DBItemSchedulerMonNotifications getNotification2Send(InternalNotificationSettings settings, Long notificationObjectType, Long taskId)
            throws Exception {

        if (notificationObjectType.equals(DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_MASTER_MESSAGE)) {
            return getNotification2SendForMasterMessages(settings, notificationObjectType);
        } else {
            return getNotification2SendForTaskMessages(settings, notificationObjectType, taskId);
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

    private DBItemSchedulerMonNotifications getNotification2SendForTaskMessages(InternalNotificationSettings settings, Long notificationObjectType,
            Long taskId) throws Exception {
        String method = "getNotification2SendForTaskMessages";

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

    private DBItemSchedulerMonSystemNotifications getSystemNotification(InternalNotificationSettings settings, ElementInternal object,
            Long notificationObjectType, String systemId, DBItemSchedulerMonNotifications notification2send, String serviceName) throws Exception {

        Long checkId = new Long(0);
        String stepFrom = DBLayer.DEFAULT_EMPTY_NAME;
        String stepTo = DBLayer.DEFAULT_EMPTY_NAME;
        String returnCodeFrom = DBLayer.DEFAULT_EMPTY_NAME;
        String returnCodeTo = DBLayer.DEFAULT_EMPTY_NAME;
        boolean isSuccess = false;

        isNewSystemNotification = false;
        getDbLayer().getSession().beginTransaction();
        DBItemSchedulerMonSystemNotifications sn = getDbLayer().getSystemNotification(systemId, serviceName, notification2send.getId(), checkId,
                notificationObjectType, isSuccess, stepFrom, stepTo, returnCodeFrom, returnCodeTo);
        if (sn == null) {
            sn = getDbLayer().createSystemNotification(systemId, serviceName, notification2send.getId(), checkId, returnCodeFrom, returnCodeTo,
                    notificationObjectType, stepFrom, stepTo, notification2send.getTaskStartTime(), notification2send.getTaskEndTime(), new Long(0),
                    object.getNotifications(), false, false, isSuccess);

            if (!isSuccess) {
                if (!SOSString.isEmpty(settings.getMessageTitle())) {
                    if (settings.getMessageTitle().toLowerCase().contains(" recovered ")) {
                        sn.setRecovered(true);
                    }
                }
            }
            if (!SOSString.isEmpty(settings.getMessageTitle())) {
                String t = settings.getMessageTitle();
                if (t.startsWith("ERROR ")) {
                    t = t.substring(6);
                } else if (t.startsWith("[error] ")) {
                    t = t.substring(8);
                } else if (t.startsWith("[warning] ")) {
                    t = t.substring(10);
                }
                sn.setTitle(t.trim());
            }
            sn.setMaxNotifications(true); // to avoid send by the SystemNotifier Job
            getDbLayer().getSession().save(sn);
            isNewSystemNotification = true;
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

    private Long getTaskId(String id) {
        Long taskId = null;
        if (!SOSString.isEmpty(id)) {
            try {
                taskId = Long.parseLong(id);
            } catch (Throwable ex) {
                LOGGER.warn(String.format("[getTaskId][%s]%s", id, ex.toString()), ex);
            }
        }
        return taskId;
    }

    private Long getTaskNotificationObjectType(Long taskId, Long notificationObjectType) {
        if (taskId == null) {
            LOGGER.info(String.format("switch from %s to %s", internalType.name(), InternalType.MASTER_MESSAGE.name()));

            internalType = InternalType.MASTER_MESSAGE;
            return DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_MASTER_MESSAGE;
        } else {
            return notificationObjectType;
        }
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
        try {
            if (getDbLayer() != null) {
                if (getDbLayer().getSession() != null) {
                    getDbLayer().getSession().close();
                }
            }
        } catch (Throwable e) {
        }
        try {
            if (factory != null) {
                factory.close();
                factory = null;
            }
        } catch (Throwable e) {
        }
    }

}
