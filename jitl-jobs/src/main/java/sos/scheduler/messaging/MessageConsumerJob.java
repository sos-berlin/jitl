package sos.scheduler.messaging;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import sos.scheduler.messaging.options.MessageConsumerOptions;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;


public class MessageConsumerJob extends JSJobUtilitiesClass<MessageConsumerOptions> {
    private static final Logger LOGGER = Logger.getLogger(MessageConsumerJob.class);
//    private static final String DEFAULT_CONNECTION_URI = "tcp://galadriel.sos:61616";
    private static final String DEFAULT_QUEUE_NAME = "JobChainQueue";
//    private static final String HOST = "sp.sos";
//    private static final int PORT = 4111;
    private static final String DEFAULT_PROTOCOL = "tcp";
//    private static final int TIMEOUT = 5;
    private String messageXml; 

    public MessageConsumerJob() {
        super(new MessageConsumerOptions());
    }

    public MessageConsumerJob execute() throws Exception {
        String protocol = objOptions.getMessagingProtocol().Value();
        if(protocol == null || (protocol != null && protocol.isEmpty())){
            protocol = DEFAULT_PROTOCOL;
        }
        String messageHost = objOptions.getMessagingServerHostName().Value();
        String messagePort = objOptions.getMessagingServerPort().Value();
        String queueName = objOptions.getMessagingQueueName().Value();
        if(queueName == null || (queueName != null && queueName.isEmpty())){
            queueName = DEFAULT_QUEUE_NAME;
        }
        String connectionUrl = createConnectionUrl(protocol, messageHost, messagePort);
        Connection jmsConnection = createConnection(connectionUrl);
        messageXml = read(jmsConnection, queueName);
        return this;
    }

    public MessageConsumerOptions getOptions() {
        if (objOptions == null) {
            objOptions = new MessageConsumerOptions();
        }
        return objOptions;
    }

    private String createConnectionUrl (String protocol, String hostName, String port){
        StringBuilder strb = new StringBuilder();
        strb.append(protocol).append("://").append(hostName).append(":").append(port);
        return strb.toString();
    }

    private Connection createConnection(String uri) {
        ConnectionFactory factory = new ActiveMQConnectionFactory(uri);
        Connection connection = null;
        try {
            connection = factory.createConnection();
        } catch (JMSException e) {
            LOGGER.error("JMSException occurred while trying to connect: ", e);
        }
        return connection;
    }

    private Session createSession(Connection connection) {
        Session session = null;
        try {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            LOGGER.error("JMSException occurred while trying to create Session: ", e);
        }
        return session;
    }

    private Destination createDestination(Session session, String queueName) {
        Destination destination = null;
        try {
            destination = session.createQueue(queueName);
        } catch (JMSException e) {
            LOGGER.error("JMSException occurred while trying to create Destination: ", e);
        }
        return destination;
    }

    private MessageConsumer createMessageConsumer(Session session, Destination destination) {
        MessageConsumer consumer = null;
        try {
            consumer = session.createConsumer(destination);
        } catch (JMSException e) {
            LOGGER.error("JMSException occurred while trying to create MessageConsumer: ", e);
        }
        return consumer;
    }

    private String read(Connection jmsConnection, String queueName) {
        TextMessage message = null;
        String textMessage = null;
        try {
            Session session = createSession(jmsConnection);
            Destination destination = createDestination(session, queueName);
            jmsConnection.start();
            MessageConsumer consumer = createMessageConsumer(session, destination);
            while (true) {
                Message receivedMessage = consumer.receive(1);
                if (receivedMessage != null) {
                    if (receivedMessage instanceof TextMessage) {
                        message = (TextMessage) receivedMessage;
                        textMessage = message.getText();
                        LOGGER.debug("Reading message from queue: " + textMessage);
                        break;
                    } else {
                        break;
                    }
                }
            }
        } catch (JMSException e) {
            LOGGER.error("JMSException occurred while trying to read from Destination: ", e);
        } finally {
            if (jmsConnection != null) {
                try {
                    jmsConnection.close();
                } catch (JMSException e) {
                    LOGGER.error("JMSException occurred while trying to close the connection: " , e);
                }
            }
         }
        return textMessage;
    }

//    public String receiveFromQueue(Connection jmsConnection, String queueName) {
//        String message = null;
//        try {
//            message = read(jmsConnection, queueName);
//        } catch (Exception e) {
//            LOGGER.error("Error occured while reading from queue: " + queueName, e);
//        }
//        return message;
//    }

    public String getMessageXml() {
        return messageXml;
    }

}
