package com.sos.jitl.notification.plugins.notifier;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.notification.db.DBItemSchedulerMonChecks;
import com.sos.jitl.notification.db.DBItemSchedulerMonNotifications;
import com.sos.jitl.notification.db.DBItemSchedulerMonSystemNotifications;
import com.sos.jitl.notification.helper.EServiceMessagePrefix;
import com.sos.jitl.notification.helper.EServiceStatus;
import com.sos.jitl.notification.helper.ElementNotificationMonitorJMS;
import com.sos.jitl.notification.helper.ObjectHelper;

import sos.util.SOSString;

public class SystemNotifierSendJMSPlugin extends SystemNotifierCustomPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemNotifierSendJMSPlugin.class);

    private ElementNotificationMonitorJMS config = null;
    private Connection connection = null;
    private Session session = null;
    private String url4log;
    private String userName;
    private String password;

    @Override
    public void onInit() throws Exception {
        config = (ElementNotificationMonitorJMS) getNotificationMonitor().getMonitorInterface();
        createConnection();
    }

    @Override
    public void onNotifySystem(DBItemSchedulerMonSystemNotifications systemNotification, DBItemSchedulerMonNotifications notification,
            DBItemSchedulerMonChecks check, EServiceStatus status, EServiceMessagePrefix prefix) throws Exception {

        MessageProducer producer = createProducer(systemNotification.getServiceName());
        try {
            String msg = resolveAllVars(systemNotification, notification, check, status, prefix, config.getMessage());
            LOGGER.info(String.format("[onNotifySystem][%s][%s]send message: %s", url4log, systemNotification.getServiceName(), msg));
            LOGGER.debug(String.format("[onNotifySystem][priority=%s][deliveryMode=%s][timeToLive=%s]", config.getPriority(), config
                    .getDeliveryMode(), config.getTimeToLive()));

            producer.setPriority(config.getPriority());
            producer.setDeliveryMode(config.getDeliveryMode());
            producer.setTimeToLive(config.getTimeToLive());

            producer.send(session.createTextMessage(msg));
        } catch (Throwable e) {
            LOGGER.error(String.format("[onNotifySystem][%s][%s]exception occurred while trying to send message: %s", url4log, systemNotification
                    .getServiceName(), e.toString()), e);
            throw e;
        } finally {
            if (producer != null) {
                try {
                    producer.close();
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public void onClose() {
        closeConnection();
    }

    private void createConnection() throws Exception {

        ConnectionFactory factory = createFactory();
        try {

            if (SOSString.isEmpty(userName)) {
                LOGGER.debug(String.format("createConnection..."));
                connection = factory.createConnection();
            } else {
                LOGGER.debug(String.format("createConnection[userName=%s, pass=********]...", userName));
                connection = factory.createConnection(userName, password);
            }
            if (!SOSString.isEmpty(config.getClientId())) {
                connection.setClientID(config.getClientId());
            }
            connection.start();
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s]exception occurred while trying to connect: %s", url4log, e.toString()), e);
            throw e;

        }
        try {
            session = connection.createSession(false, config.getAcknowledgeMode());
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s]exception occurred while trying to create Session: %s", url4log, e.toString()), e);
            throw e;
        }
    }

    private ConnectionFactory createFactory() throws Exception {

        if (config.getConnectionFactory() != null) {

            LOGGER.debug(String.format("initialize ConnectionFactory[class=%s]", config.getConnectionFactory().getJavaClass()));
            try {
                userName = config.getConnectionFactory().getUserName();
                password = config.getConnectionFactory().getPassword();
                // TODO
                url4log = "";

                return (ConnectionFactory) ObjectHelper.newInstance(config.getConnectionFactory().getJavaClass(), config.getConnectionFactory()
                        .getConstructorArguments());
            } catch (Throwable e) {
                LOGGER.error(String.format("can't initialize ConnectionFactory[class=%s]: %s", config.getConnectionFactory().getJavaClass(), e
                        .toString()), e);
                throw e;
            }
        } else if (config.getConnectionJNDI() != null) {
            LOGGER.debug(String.format("initialize ConnectionFactory[jndi file=%s, lookupName=%s]", config.getConnectionJNDI().getFile(), config
                    .getConnectionJNDI().getLookupName()));
            try {
                Properties env = loadJndiFile(config.getConnectionJNDI().getFile());
                if (env != null) {
                    url4log = env.getProperty(Context.PROVIDER_URL);
                    userName = env.getProperty(Context.SECURITY_PRINCIPAL);
                    password = env.getProperty(Context.SECURITY_CREDENTIALS);
                }
                Context jndi = new InitialContext(env);
                return (ConnectionFactory) jndi.lookup(config.getConnectionJNDI().getLookupName());
            } catch (Throwable e) {
                LOGGER.error(String.format("can't initialize ConnectionFactory[jndi file=%s, lookupName=%s]: %s", config.getConnectionJNDI()
                        .getFile(), config.getConnectionJNDI().getLookupName(), e.toString()), e);
                throw e;
            }
        } else {
            throw new Exception(String.format("can't initialize ConnectionFactory: connection element not found (%s or %s)",
                    ElementNotificationMonitorJMS.ELEMENT_NAME_CONNECTION_FACTORY, ElementNotificationMonitorJMS.ELEMENT_NAME_CONNECTION_JNDI));
        }
    }

    private Properties loadJndiFile(String fileName) throws Exception {
        InputStream is = null;
        try {
            Properties p = new Properties();
            is = new FileInputStream(fileName);
            p.load(is);
            return p;
        } catch (Throwable e) {
            throw new Exception(String.format("can't load jndi file=%s: %s", fileName, e.toString()), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private void closeConnection() {
        LOGGER.debug(String.format("[%s]closeConnection ...", url4log));

        if (session != null) {
            try {
                session.close();
            } catch (Throwable e) {
            }
        }
        if (connection != null) {
            try {
                connection.stop();
            } catch (Throwable e) {
            }
            try {
                connection.close();
            } catch (Throwable e) {
            }
        }
        session = null;
        connection = null;
    }

    private MessageProducer createProducer(String name) throws Exception {
        Destination destination = null;
        try {
            name = normalizeDestinationName(name);
            LOGGER.debug(String.format("[%s][%s][%s]create Destination...", url4log, config.getDestination(), name));

            if (config.isQueueDestination()) {
                destination = session.createQueue(name);
            } else {
                destination = session.createTopic(name);
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][%s][%s]exception occurred while trying to create Destination: %s", url4log, config.getDestination(),
                    name, e.toString()), e);
            throw e;
        }
        try {
            return session.createProducer(destination);
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][%s][%s]exception occurred while trying to create MessageProducer: %s", url4log, config.getDestination(),
                    name, e.toString()), e);
            throw e;
        }
    }

    private String normalizeDestinationName(String name) {
        name = name.replaceAll("&amp;", "&");
        return name;
    }

}
