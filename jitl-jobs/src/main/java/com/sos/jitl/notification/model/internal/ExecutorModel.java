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
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.notification.db.DBItemSchedulerMonNotifications;
import com.sos.jitl.notification.db.DBItemSchedulerMonSystemNotifications;
import com.sos.jitl.notification.db.DBLayer;
import com.sos.jitl.notification.helper.EServiceMessagePrefix;
import com.sos.jitl.notification.helper.EServiceStatus;
import com.sos.jitl.notification.helper.ElementNotificationInternal;
import com.sos.jitl.notification.helper.ElementNotificationInternalTaskIfLongerThan;
import com.sos.jitl.notification.helper.ElementNotificationInternalTaskIfShorterThan;
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
        TASK_IF_LONGER_THAN, TASK_IF_SHORTER_THAN
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorModel.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private final Path configurationDirectory;
    private final Path hibernateConfiguration;
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

            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                LOGGER.info(String.format("[%s][%s]%s", method, (i + 1), f.getCanonicalPath()));
                boolean ok = handleConfigFile(type, settings, f);
                if (ok) {
                    toNotify = true;
                }

            }
        } catch (Throwable ex) {
            LOGGER.error(String.format("[%s]%s", method, ex.toString()), ex);
        }

        return toNotify;
    }

    private boolean handleConfigFile(InternalType type, InternalNotificationSettings settings, File xmlFile) throws Exception {
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
                    LOGGER.warn(String.format("[%s][%s][%s][skip]missing Notification element", method, xmlFilePath, type.name()));
                    continue;
                }
                if (SOSString.isEmpty(monitor.getServiceNameOnError())) {
                    LOGGER.warn(String.format("[%s][%s][%s][skip]missing service_name_on_error", method, xmlFilePath, type.name()));
                    continue;
                }

                switch (type) {
                case TASK_IF_LONGER_THAN:
                    node = NotificationXmlHelper.selectNotificationMonitorInternalTaskIfLongerThan(xpath, n);
                    if (node != null) {
                        ElementNotificationInternalTaskIfLongerThan el = new ElementNotificationInternalTaskIfLongerThan(monitor, node);
                        if (SystemNotifierModel.checkDoNotifyInternal(0, settings.getSchedulerId(), el)) {
                            objects.add(el);
                        }
                    }
                    break;
                case TASK_IF_SHORTER_THAN:
                    node = NotificationXmlHelper.selectNotificationMonitorInternalTaskIfShorterThan(xpath, n);
                    if (node != null) {
                        ElementNotificationInternalTaskIfShorterThan el = new ElementNotificationInternalTaskIfShorterThan(monitor, node);
                        if (SystemNotifierModel.checkDoNotifyInternal(0, settings.getSchedulerId(), el)) {
                            objects.add(el);
                        }
                    }
                    break;
                default:
                    throw new Exception(String.format("[%s]not implemented yet", type.name()));
                }
            }

            if (objects.size() > 0) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s]found %s definitions", method, xmlFilePath, type.name(), objects.size()));
                }
                String systemId = NotificationXmlHelper.getSystemMonitorNotificationSystemId(xpath);
                sendNotifications(settings, systemId, objects);
                toNotify = true;
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s]not found definitions", method, xmlFilePath, type.name()));
                }
            }

        } catch (Exception e) {
            throw new Exception(String.format("[%s][%s]%s", method, xmlFilePath, e.toString()), e);
        }
        return toNotify;
    }

    private void sendNotifications(InternalNotificationSettings settings, String systemId, List<ElementNotificationInternal> objects)
            throws Exception {
        String method = "sendNotifications";
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;

        try {

            for (int i = 0; i < objects.size(); i++) {
                ElementNotificationInternal object = objects.get(i);

                ElementNotificationMonitor monitor = object.getMonitor();
                LOGGER.info(systemId + "=" + monitor.getMonitorInterface());

                ISystemNotifierPlugin pl = monitor.getOrCreatePluginObject();
                if (pl.hasErrorOnInit()) {
                    throw new Exception(String.format("[%s][skip]due plugin init error: %s", method, pl.getInitError()));
                }

                DBItemSchedulerMonSystemNotifications sn = new DBItemSchedulerMonSystemNotifications();
                sn.setId(new Long(0));
                sn.setNotificationId(sn.getId());
                sn.setCheckId(sn.getId());
                sn.setSystemId(systemId);
                sn.setServiceName(monitor.getServiceNameOnError());
                sn.setObjectType(DBLayer.NOTIFICATION_OBJECT_TYPE_INTERNAL_TASK_IF_LONGER_THAN);
                sn.setNotificationId(object.getNotifications());
                sn.setSuccess(false);
                sn.setCreated(DBLayer.getCurrentDateTime());
                sn.setModified(sn.getCreated());

                DBItemSchedulerMonNotifications notification2send = new DBItemSchedulerMonNotifications();
                notification2send.setError(true);
                notification2send.setErrorText(settings.getMessage());

                pl.notifySystem(null, options, getDbLayer(), notification2send, sn, null, EServiceStatus.CRITICAL, EServiceMessagePrefix.ERROR);

            }

            // factory = buildFactory();
            // session = factory.openStatelessSession();

        } catch (Throwable ex) {
            throw new Exception(String.format("[%s]%s", method, ex.toString()), ex);
        } finally {
            // closeFactory(factory, session);
        }

    }

    private SOSHibernateFactory buildFactory() throws Exception {

        SOSHibernateFactory factory = new SOSHibernateFactory(hibernateConfiguration);
        factory.setIdentifier("internal");
        factory.setAutoCommit(false);
        factory.addClassMapping(DBLayer.getNotificationClassMapping());
        factory.build();

        return factory;
    }

    private void closeFactory(SOSHibernateFactory factory, SOSHibernateSession session) {
        if (session != null) {
            session.close();
        }
        if (factory != null) {
            factory.close();
        }
    }

}
