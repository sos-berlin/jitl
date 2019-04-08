package com.sos.jitl.notification.model.internal;

import java.io.File;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.notification.db.DBLayer;
import com.sos.jitl.notification.helper.NotificationXmlHelper;
import com.sos.jitl.notification.model.NotificationModel;

import sos.xml.SOSXMLXPath;

public class ExecutorModel extends NotificationModel {

    public enum InternalType {
        TASK_IF_LONGER_THAN, TASK_IF_SHORTER_THAN
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorModel.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private final Path configurationDirectory;
    private final Path hibernateConfiguration;

    public ExecutorModel(Path configDir, Path hibernateFile) {
        configurationDirectory = configDir;
        hibernateConfiguration = hibernateFile;
    }

    public boolean process(InternalType type, Settings settings) {
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

    private boolean handleConfigFile(InternalType type, Settings settings, File xmlFile) throws Exception {
        String method = "handleConfigFile";

        String xmlFilePath = null;
        SOSXMLXPath xpath = null;
        NodeList nl = null;
        boolean toNotify = false;
        try {
            xmlFilePath = xmlFile.getCanonicalPath();
            xpath = new SOSXMLXPath(xmlFilePath);

            switch (type) {
            case TASK_IF_LONGER_THAN:
                nl = NotificationXmlHelper.selectNotificationInternalTaskIfLongerThanDefinitions(xpath);
                break;
            case TASK_IF_SHORTER_THAN:
                nl = NotificationXmlHelper.selectNotificationInternalTaskIfShorterThanDefinitions(xpath);
                break;
            default:
                throw new Exception(String.format("[%s]not implemented yet", type.name()));
            }

            if (nl != null && nl.getLength() > 0) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s]found %s definitions", method, xmlFilePath, type.name(), nl.getLength()));
                }
                createNotification(type, settings);
                toNotify = true;
            }

        } catch (Exception e) {
            throw new Exception(String.format("[%s][%s]%s", method, xmlFilePath, e.toString()), e);
        }
        return toNotify;
    }

    private void createNotification(InternalType type, Settings settings) throws Exception {
        String method = "createNotification";
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = buildFactory();
            session = factory.openStatelessSession();

        } catch (Throwable ex) {
            throw new Exception(String.format("[%s]%s", method, ex.toString()), ex);
        } finally {
            closeFactory(factory, session);
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
