package sos.scheduler.messaging;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import sos.scheduler.messaging.options.MessageProducerOptions;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;


public class MessageProducerJob extends JSJobUtilitiesClass<MessageProducerOptions> {
    private static final Logger LOGGER = Logger.getLogger(MessageProducerJob.class);
    private static final String DEFAULT_QUEUE_NAME = "JobChainQueue";
    private static final String DEFAULT_PROTOCOL = "tcp";
    private boolean sentSuccesfull = false;

    public MessageProducerJob() {
        super(new MessageProducerOptions());
    }

    public MessageProducerJob execute() throws Exception {
        String protocol = objOptions.getMessagingProtocol().Value();
        if(protocol == null || (protocol != null && protocol.isEmpty())){
            protocol = DEFAULT_PROTOCOL;
        }
        String messageHost = objOptions.getMessagingServerHostName().Value();
        String messagePort = objOptions.getMessagingServerPort().Value();
        String message = objOptions.getMessage().Value();
        String queueName = objOptions.getMessagingQueueName().Value();
        if(queueName == null || (queueName != null && queueName.isEmpty())){
            queueName = DEFAULT_QUEUE_NAME;
        }
        String connectionUrl = createConnectionUrl(protocol, messageHost, messagePort);
        if(message != null && !message.isEmpty()){
            write(message, connectionUrl, queueName);
            sentSuccesfull = true;
        } else{
            sentSuccesfull = false;
            throw new JobSchedulerException("Message is empty, nothing to send to message server");
        }
        return this;
    }

    public MessageProducerOptions getOptions() {
        if (objOptions == null) {
            objOptions = new MessageProducerOptions();
        }
        return objOptions;
    }
    
    private String createConnectionUrl (String protocol, String hostName, String port){
        StringBuilder strb = new StringBuilder();
        strb.append(protocol).append("://").append(hostName).append(":").append(port);
        return strb.toString();
    }

    private Connection createConnection(String uri){
        ConnectionFactory factory = new ActiveMQConnectionFactory(uri);
        Connection jmsConnection = null;
        try {
            jmsConnection = factory.createConnection();
        } catch (JMSException e) {
            LOGGER.error("JMSException occurred while trying to connect: " , e);
        }
        return jmsConnection;
    }
    
    private Session createSession(Connection jmsConnection){
        Session session = null;
        try {
            session = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            LOGGER.error("JMSException occurred while trying to create Session: " , e);
        }
        return session;
    }
    
    private Destination createDestination(Session session, String queueName){
        Destination destination = null;
        try {
            destination = session.createQueue(queueName);
        } catch (JMSException e) {
            LOGGER.error("JMSException occurred while trying to create Destination: " , e);
        }
        return destination;
    }
    
    private MessageProducer createMessageProducer(Session session, Destination destination){
        MessageProducer producer = null;
        try {
            producer = session.createProducer(destination);
        } catch (JMSException e) {
            LOGGER.error("JMSException occurred while trying to create MessageProducer: " , e);
        }        
        return producer;
    }
    
    public void write(String text, String connectionUrl, String queueName){
        Connection jmsConnection = createConnection(connectionUrl);
        Session session = createSession(jmsConnection);
        Destination destination = createDestination(session, queueName);
        MessageProducer producer = createMessageProducer(session, destination);
        Message message = null;
        try {
            message = session.createTextMessage(text);
            producer.send(message);
        } catch (JMSException e) {
            LOGGER.error("JMSException occurred in ProducerJob while trying to write Message to Destination: " , e);
        } finally {
            if (jmsConnection != null) {
                try {
                    jmsConnection.close();
                } catch (JMSException e) {
                    LOGGER.error("JMSException occurred in ProducerJob while trying to close the connection: " , e);
                }
            }
        }
    }

    
    public boolean isSentSuccesfull() {
        return sentSuccesfull;
    }

}
