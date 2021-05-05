package com.sos.jitl.messaging;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.vfs.jms.SOSJMS;
import com.sos.vfs.common.options.SOSProviderOptions;
import com.sos.jitl.messaging.options.MessageConsumerOptions;

public class MessageConsumerJob extends JSJobUtilitiesClass<MessageConsumerOptions> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageConsumerJob.class);
    private static final String DEFAULT_QUEUE_NAME = "JobChainQueue";
    private static final String DEFAULT_PROTOCOL = "tcp";
    private String messageXml;
    private SOSJMS handler;

    public MessageConsumerJob() {
        super(new MessageConsumerOptions());
        handler = new SOSJMS();
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
        String connectionUrl = handler.createConnectionUrl(protocol, messageHost, messagePort);
        if (!handler.isConnected()) {
            this.connect();
        }
        Connection jmsConnection = handler.createConnection(connectionUrl);
        if (executeXml) {
            messageXml = handler.read(jmsConnection, queueName, lastConsumer);
        } else if (jobParams) {
            String message = handler.read(jmsConnection, queueName, lastConsumer);
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
        SOSProviderOptions alternateOptions = getAlternateOptions();
        try {
            getOptions().checkMandatory();
            handler.connect(alternateOptions);
            LOGGER.debug("connection established");
        } catch (Exception e) {
            LOGGER.error("Error occurred trying to connect to VFS: ", e);
        }
        return this;
    }

    public SOSProviderOptions getAlternateOptions() {
        SOSProviderOptions alternateOptions = new SOSProviderOptions();
        alternateOptions.host.setValue(objOptions.getMessagingServerHostName().getValue());
        alternateOptions.port.value(objOptions.getMessagingServerPort().value());
        return alternateOptions;
    }

}
