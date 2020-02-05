package com.sos.jitl.messaging;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.messaging.options.MessageProducerOptions;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.JMS.SOSVfsJms;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;

public class MessageProducerJob extends JSJobUtilitiesClass<MessageProducerOptions> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProducerJob.class);
    private static final String DEFAULT_QUEUE_NAME = "JobChainQueue";
    private static final String DEFAULT_PROTOCOL = "tcp";
    private boolean sentSuccesfull = false;
    private Map<String, String> allParams = new HashMap<String, String>();
    private ISOSVFSHandler vfsHandler;

    public MessageProducerJob() {
        super(new MessageProducerOptions());
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

    public MessageProducerJob execute() throws Exception {
        vfsHandler.setJSJobUtilites(objJSJobUtilities);
        String protocol = objOptions.getMessagingProtocol().getValue();
        if (protocol == null || (protocol != null && protocol.isEmpty())) {
            protocol = DEFAULT_PROTOCOL;
        }
        String messageHost = objOptions.getMessagingServerHostName().getValue();
        String messagePort = objOptions.getMessagingServerPort().getValue();
        String message = objOptions.getMessage().getValue();
        String queueName = objOptions.getMessagingQueueName().getValue();
        boolean executeXml = objOptions.getSendXml().value();
        boolean jobParams = objOptions.getSendJobParameters().value();
        if (queueName == null || (queueName != null && queueName.isEmpty())) {
            queueName = DEFAULT_QUEUE_NAME;
        }
        String connectionUrl = ((SOSVfsJms) vfsHandler).createConnectionUrl(protocol, messageHost, messagePort);
        LOGGER.debug("*************Message from Option: " + message);
        if (!vfsHandler.isConnected()) {
            this.connect();
        }
        if (!executeXml && jobParams && (message == null || message.isEmpty())) {
            message = createParameterMessage();
            LOGGER.debug("*************Message dynamic created from params: " + message);
        }
        if (message != null && !message.isEmpty()) {
            ((SOSVfsJms) vfsHandler).write(message, connectionUrl, queueName);
            sentSuccesfull = true;
        } else {
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

    public boolean isSentSuccesfull() {
        return sentSuccesfull;
    }

    private String createParameterMessage() {
        boolean first = true;
        StringBuilder strb = new StringBuilder();
        if (!allParams.isEmpty()) {
            for (String key : allParams.keySet()) {
                if (allParams.get(key) != null && !allParams.get(key).isEmpty()) {
                    if (!first) {
                        strb.append(objOptions.getParamPairDelimiter().getValue());
                    } else {
                        first = false;
                    }
                    strb.append(key).append(objOptions.getParamKeyValueDelimiter().getValue()).append(allParams.get(key));
                }
            }
        }
        return strb.toString();
    }

    public Map<String, String> getAllParams() {
        return allParams;
    }

    public void setAllParams(Map<String, String> allParams) {
        this.allParams = allParams;
    }

    public MessageProducerJob connect() {
        SOSConnection2OptionsAlternate alternateOptions = getAlternateOptions();
        try {
            getOptions().checkMandatory();
            vfsHandler.connect(alternateOptions);
            LOGGER.debug("connection established");
        } catch (Exception e) {
            LOGGER.error("Error occurred trying to connect to VFS: ", e);
        }
        return this;
    }

    public SOSConnection2OptionsAlternate getAlternateOptions() {
        SOSConnection2OptionsAlternate alternateOptions = new SOSConnection2OptionsAlternate();
        alternateOptions.host.setValue(objOptions.getMessagingServerHostName().getValue());
        alternateOptions.port.value(objOptions.getMessagingServerPort().value());
        return alternateOptions;
    }

}
