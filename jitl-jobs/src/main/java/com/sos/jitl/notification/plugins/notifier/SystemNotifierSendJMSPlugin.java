package com.sos.jitl.notification.plugins.notifier;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Queue;
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

import sos.util.SOSString;

public class SystemNotifierSendJMSPlugin extends SystemNotifierCustomPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemNotifierSendJMSPlugin.class);

    private ElementNotificationMonitorJMS config = null;
    private Connection connection = null;
    private Session session = null;
    private String url;

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
            LOGGER.info(String.format("[onNotifySystem][%s][%s]send message: %s", url, systemNotification.getServiceName(), msg));
            LOGGER.debug(String.format("[onNotifySystem][priority=%s][deliveryMode=%s][timeToLive=%s]", config.getPriority(), config
                    .getDeliveryMode(), config.getTimeToLive()));

            producer.setPriority(config.getPriority());
            producer.setDeliveryMode(config.getDeliveryMode());
            producer.setTimeToLive(config.getTimeToLive());

            producer.send(session.createTextMessage(msg));
        } catch (Throwable e) {
            LOGGER.error(String.format("[onNotifySystem][%s][%s]exception occurred while trying to send message: %s", url, systemNotification
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

            connection = factory.createConnection();
            if (!SOSString.isEmpty(config.getClientId())) {
                connection.setClientID(config.getClientId());
            }
            connection.start();
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s]exception occurred while trying to connect: %s", url, e.toString()), e);
            throw e;

        }
        try {
            session = connection.createSession(false, config.getAcknowledgeMode());
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s]exception occurred while trying to create Session: %s", url, e.toString()), e);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private ConnectionFactory createFactory() throws Exception {

        if (config.getConnectionFactory() != null) {
            url = config.getConnectionFactory().getProviderUrl();

            LOGGER.debug(String.format("initialize ConnectionFactory[class=%s][constructor userName=%s, pass=********, url=%s]", config
                    .getConnectionFactory().getFactory(), config.getConnectionFactory().getUserName(), url));
            try {
                Class<ConnectionFactory> clazz = (Class<ConnectionFactory>) Class.forName(config.getConnectionFactory().getFactory());
                return clazz.getConstructor(String.class, String.class, String.class).newInstance(config.getConnectionFactory().getUserName(), config
                        .getConnectionFactory().getPassword(), url);
            } catch (Throwable e) {
                LOGGER.error(String.format("can't initialize ConnectionFactory[class=%s][constructor userName=%s, pass=********, url=%s]", config
                        .getConnectionFactory(), config.getConnectionFactory().getUserName(), url, e.toString()), e);
                throw e;
            }
        } else if (config.getJNDI() != null) {
            LOGGER.debug(String.format("initialize ConnectionFactory[jndi file=%s, lookupName=%s]", config.getJNDI().getFile(), config.getJNDI()
                    .getLookupName()));
            try {
                Properties env = loadJndiFile(config.getJNDI().getFile());
                if (env != null) {
                    // TODO check url
                    url = env.getProperty("java.naming.provider.url");
                }
                Context jndi = new InitialContext(env);
                return (ConnectionFactory) jndi.lookup(config.getJNDI().getLookupName());
            } catch (Throwable e) {
                LOGGER.error(String.format("can't initialize ConnectionFactory[jndi file=%s, lookupName=%s]: %s", config.getJNDI().getFile(), config
                        .getJNDI().getLookupName(), e.toString()), e);
                throw e;
            }
        } else {
            throw new Exception("can't initialize ConnectionFactory: child elements not found");
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
        LOGGER.debug(String.format("[%s]closeConnection ...", url));

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

    private MessageProducer createProducer(String queueName) throws Exception {
        Queue queue = null;
        try {
            queue = session.createQueue(queueName);

        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][%s]exception occurred while trying to create Queue: %s", url, queueName, e.toString()), e);
            throw e;
        }
        try {
            return session.createProducer(queue);
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][%s]exception occurred while trying to create MessageProducer: %s", url, queueName, e.toString()), e);
            throw e;
        }
    }

}
