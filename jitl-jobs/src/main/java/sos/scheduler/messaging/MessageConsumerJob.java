package sos.scheduler.messaging;

import java.util.HashMap;
import java.util.Map;

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
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.JMS.SOSVfsJms;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;


public class MessageConsumerJob extends JSJobUtilitiesClass<MessageConsumerOptions> {
    private static final Logger LOGGER = Logger.getLogger(MessageConsumerJob.class);
    private static final String DEFAULT_QUEUE_NAME = "JobChainQueue";
    private static final String DEFAULT_PROTOCOL = "tcp";
    private String messageXml; 
    private ISOSVFSHandler vfsHandler;

    public MessageConsumerJob() {
        super(new MessageConsumerOptions());
        getVFS();
    }

    public ISOSVFSHandler getVFS() {
        try {
            vfsHandler = VFSFactory.getHandler("mq");
        } catch (Exception e) {
            throw new JobSchedulerException("SOS-VFS-E-0010: unable to initialize VFS", e);
        }
        return vfsHandler;
    }

    public MessageConsumerJob execute() throws Exception {
        String protocol = objOptions.getMessagingProtocol().Value();
        if(protocol == null || (protocol != null && protocol.isEmpty())){
            protocol = DEFAULT_PROTOCOL;
        }
        String messageHost = objOptions.getMessagingServerHostName().Value();
        String messagePort = objOptions.getMessagingServerPort().Value();
        String queueName = objOptions.getMessagingQueueName().Value();
        boolean executeXml = objOptions.getExecuteXml().value();
        boolean jobParams = objOptions.getJobParameters().value();
        if(queueName == null || (queueName != null && queueName.isEmpty())){
            queueName = DEFAULT_QUEUE_NAME;
        }
        String connectionUrl = ((SOSVfsJms)vfsHandler).createConnectionUrl(protocol, messageHost, messagePort);
        if (!vfsHandler.isConnected()) {
            this.connect();
        }
        Connection jmsConnection = ((SOSVfsJms)vfsHandler).createConnection(connectionUrl);
        if(executeXml){
            messageXml = ((SOSVfsJms)vfsHandler).read(jmsConnection, queueName);
        } else if(jobParams) {
            String message = ((SOSVfsJms)vfsHandler).read(jmsConnection, queueName);
            if(message != null && !message.isEmpty()){
                logReceivedParams(message);
            }
        }
        return this;
    }

    public MessageConsumerOptions getOptions() {
        if (objOptions == null) {
            objOptions = new MessageConsumerOptions();
        }
        return objOptions;
    }

    public String getMessageXml() {
        return messageXml;
    }
    
    private Map<String, String> createParamMap (String message){
        LOGGER.debug("************************Received Message: " + message);
        Map<String, String> paramMap = new HashMap<String, String>();
        String[] paramPairs = message.split("[" + objOptions.getParamPairDelimiter().Value() + "]");
        LOGGER.debug("************************KeyValuePairs count: " + paramPairs.length);
        for(String keyValue : paramPairs){
            String[] params = keyValue.split(objOptions.getParamKeyValueDelimiter().Value());
            paramMap.put(params[0], params[1]);
        }
        return paramMap;
    }
    
    private void logReceivedParams(String message){
        Map<String, String> params = createParamMap(message);
        LOGGER.debug("****Example Output of Params received from message!****");
        for(String key : params.keySet()){
            LOGGER.debug("KEY: " + key + " VALUE: " + params.get(key));
        }
    }

    public MessageConsumerJob connect() {
        SOSConnection2OptionsAlternate alternateOptions = getAlternateOptions();
        try {
            getOptions().CheckMandatory();
            vfsHandler.Connect(alternateOptions);
            LOGGER.debug("connection established");
        } catch (Exception e) {
            LOGGER.error("Error occurred trying to connect to VFS: ", e);
        }
        return this;
    }

    public SOSConnection2OptionsAlternate getAlternateOptions() {
        SOSConnection2OptionsAlternate alternateOptions = new SOSConnection2OptionsAlternate();
        alternateOptions.host.Value(objOptions.getMessagingServerHostName().Value());
        alternateOptions.port.value(objOptions.getMessagingServerPort().value());
        return alternateOptions;
    }

}
