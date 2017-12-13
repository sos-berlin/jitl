package com.sos.jitl.notification.plugins.notifier;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.notification.db.DBItemSchedulerMonChecks;
import com.sos.jitl.notification.db.DBItemSchedulerMonNotifications;
import com.sos.jitl.notification.db.DBItemSchedulerMonSystemNotifications;
import com.sos.jitl.notification.helper.EServiceMessagePrefix;
import com.sos.jitl.notification.helper.EServiceStatus;
import com.sos.jitl.notification.helper.ElementNotificationMonitorJMS;

public class SystemNotifierSendJMSApacheMQPlugin extends SystemNotifierCustomPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemNotifierSendJMSApacheMQPlugin.class);

    private ElementNotificationMonitorJMS config = null;
    private Connection connection = null;
    private Session session = null;

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
            LOGGER.info(String.format("[onNotifySystem][%s][%s]send message: %s", config.getUri(), systemNotification.getServiceName(), msg));
            producer.send(session.createTextMessage(msg));
        } catch (Throwable e) {
            LOGGER.error(String.format("[onNotifySystem][%s][%s]exception occurred while trying to send message: %s", config.getUri(), systemNotification
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
        LOGGER.debug(String.format("[%s]createConnection ...", config.getUri()));

        ConnectionFactory factory = new ActiveMQConnectionFactory(config.getUri());
        try {
            connection = factory.createConnection();
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s]exception occurred while trying to connect: %s", config.getUri(), e.toString()), e);
            throw e;

        }
        try {
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s]exception occurred while trying to create Session: %s", config.getUri(), e.toString()), e);
            throw e;
        }
    }

    private void closeConnection() {
        LOGGER.debug(String.format("[%s]closeConnection ...", config.getUri()));

        if (session != null) {
            try {
                session.close();
            } catch (Throwable e) {
            }
        }
        if (connection != null) {
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
            LOGGER.error(String.format("[%s][%s]exception occurred while trying to create Queue: %s", config.getUri(), queueName, e.toString()), e);
            throw e;
        }
        try {
            return session.createProducer(queue);
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][%s]exception occurred while trying to create MessageProducer: %s", config.getUri(), queueName, e
                    .toString()), e);
            throw e;
        }
    }

}
