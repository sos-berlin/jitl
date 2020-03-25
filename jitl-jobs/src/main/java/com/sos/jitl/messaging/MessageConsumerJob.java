package com.sos.jitl.messaging;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.messaging.options.MessageConsumerOptions;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.JMS.SOSVfsJms;
import com.sos.VirtualFileSystem.Options.SOSDestinationOptions;

public class MessageConsumerJob extends JSJobUtilitiesClass<MessageConsumerOptions> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageConsumerJob.class);
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
        String protocol = objOptions.getMessagingProtocol().getValue();
        if (protocol == null || (protocol != null && protocol.isEmpty())) {
            protocol = DEFAULT_PROTOCOL;
        }
        String messageHost = objOptions.getMessagingServerHostName().getValue();
        String messagePort = objOptions.getMessagingServerPort().getValue();
        String queueName = objOptions.getMessagingQueueName().getValue();
        boolean executeXml = objOptions.getExecuteXml().value();
        boolean jobParams = objOptions.getJobParameters().value();
        boolean lastConsumer = objOptions.getLastReceiver().value();
        if (queueName == null || (queueName != null && queueName.isEmpty())) {
            queueName = DEFAULT_QUEUE_NAME;
        }
        String connectionUrl = ((SOSVfsJms) vfsHandler).createConnectionUrl(protocol, messageHost, messagePort);
        if (!vfsHandler.isConnected()) {
            this.connect();
        }
        Connection jmsConnection = ((SOSVfsJms) vfsHandler).createConnection(connectionUrl);
        if (executeXml) {
            messageXml = ((SOSVfsJms) vfsHandler).read(jmsConnection, queueName, lastConsumer);
        } else if (jobParams) {
            String message = ((SOSVfsJms) vfsHandler).read(jmsConnection, queueName, lastConsumer);
            if (message != null && !message.isEmpty()) {
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

    private Map<String, String> createParamMap(String message) {
        LOGGER.debug("************************Received Message: " + message);
        Map<String, String> paramMap = new HashMap<String, String>();
        String[] paramPairs = message.split("[" + objOptions.getParamPairDelimiter().getValue() + "]");
        LOGGER.debug("************************KeyValuePairs count: " + paramPairs.length);
        for (String keyValue : paramPairs) {
            String[] params = keyValue.split(objOptions.getParamKeyValueDelimiter().getValue());
            paramMap.put(params[0], params[1]);
        }
        return paramMap;
    }

    private void logReceivedParams(String message) {
        Map<String, String> params = createParamMap(message);
        LOGGER.debug("****Example Output of Params received from message!****");
        for (String key : params.keySet()) {
            LOGGER.debug("KEY: " + key + " VALUE: " + params.get(key));
        }
    }

    public MessageConsumerJob connect() {
        SOSDestinationOptions alternateOptions = getAlternateOptions();
        try {
            getOptions().checkMandatory();
            vfsHandler.connect(alternateOptions);
            LOGGER.debug("connection established");
        } catch (Exception e) {
            LOGGER.error("Error occurred trying to connect to VFS: ", e);
        }
        return this;
    }

    public SOSDestinationOptions getAlternateOptions() {
        SOSDestinationOptions alternateOptions = new SOSDestinationOptions();
        alternateOptions.host.setValue(objOptions.getMessagingServerHostName().getValue());
        alternateOptions.port.value(objOptions.getMessagingServerPort().value());
        return alternateOptions;
    }

}
