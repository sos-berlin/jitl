package com.sos.jitl.messaging.options;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionString;


@JSOptionClass(name = "MessageOptionSuperClass", description = "Option-Class for a message queue connections")
public class MessageOptionSuperClass extends JSOptionsClass{

    private static final long serialVersionUID = 1L;

    @JSOptionDefinition(name = "messagingServerHostName", 
            description = "Name of the host of the Messaging Server", 
            key = "messagingServerHostName", type = "SOSOptionHostName", mandatory = true)
    public SOSOptionHostName messagingServerHostName = new SOSOptionHostName(this, 
            "messagingServerHostName", 
            "Name of the host of the Messaging Server", 
            "", 
            "", 
            true);

    @JSOptionDefinition(name = "messagingServerPort", 
            description = "Port of the Message server", 
            key = "messagingServerPort", type = "SOSOptionPortNumber", mandatory = true)
    public SOSOptionPortNumber messagingServerPort = new SOSOptionPortNumber(this, 
            "messagingServerPort", 
            "Port of the Message server", 
            "", 
            "", 
            true);
    
    @JSOptionDefinition(name = "messagingQueueName", 
            description = "Name of the queue to connect with", 
            key = "messagingQueueName", type = "SOSOptionString", mandatory = true)
    public SOSOptionString messagingQueueName = new SOSOptionString(this, 
            "messagingQueueName", 
            "Name of the queue to connect with", 
            "JobChainQueue", 
            "JobChainQueue", 
            true);
    
    @JSOptionDefinition(name = "messagingProtocol", 
            description = "protocol name to connect to the messaging server", 
            key = "messagingProtocol", type = "SOSOptionString", mandatory = true)
    public SOSOptionString messagingProtocol = new SOSOptionString(this, 
            "messagingProtocol", 
            "protocol name to connect to the messaging server", 
            "tcp", 
            "tcp", 
            true);
    
    @JSOptionDefinition(name = "paramPairDelimiter", 
            description = "this parameter determines which delimiter separates the key-value-pairs", 
            key = "paramPairDelimiter", type = "SOSOptionString", mandatory = false)
    public SOSOptionString paramPairDelimiter = new SOSOptionString(this, 
            "paramDelimiter", 
            "this parameter determines which delimiter separates the key-value-pairs", 
            "|", 
            "|", 
            false);

    @JSOptionDefinition(name = "paramKeyValueDelimiter", 
            description = "this parameter determines which delimiter separates key and value", 
            key = "paramKeyValueDelimiter", type = "SOSOptionString", mandatory = false)
    public SOSOptionString paramKeyValueDelimiter = new SOSOptionString(this, 
            "paramDelimiter", 
            "this parameter determines which delimiter separates key and value", 
            ",", 
            ",", 
            false);

    public SOSOptionHostName getMessagingServerHostName() {
        return messagingServerHostName;
    }
    
    public void setMessagingServerHostName(SOSOptionHostName messagingServerHostName) {
        this.messagingServerHostName = messagingServerHostName;
    }
    
    public void setMessagingServerHostName(String messagingServerHostName) {
        this.messagingServerHostName = new SOSOptionHostName(messagingServerHostName);
    }
    
    public SOSOptionPortNumber getMessagingServerPort() {
        return messagingServerPort;
    }
    
    public void setMessagingServerPort(SOSOptionPortNumber messagingServerPort) {
        this.messagingServerPort = messagingServerPort;
    }
    
    public void setMessagingServerPort(String messagingServerPort) {
        this.messagingServerPort = new SOSOptionPortNumber(messagingServerPort);
    }

    public SOSOptionString getMessagingQueueName() {
        return messagingQueueName;
    }
    
    public void setMessagingQueueName(SOSOptionString messagingQueueName) {
        this.messagingQueueName = messagingQueueName;
    }
    
    public void setMessagingQueueName(String messagingQueueName) {
        this.messagingQueueName = new SOSOptionString(messagingQueueName);
    }
    
    public SOSOptionString getMessagingProtocol() {
        return messagingProtocol;
    }
    
    public void setMessagingProtocol(SOSOptionString messagingProtocol) {
        this.messagingProtocol = messagingProtocol;
    }
    
    public void setMessagingProtocol(String messagingProtocol) {
        this.messagingProtocol = new SOSOptionString(messagingProtocol);
    }
    
    public SOSOptionString getParamPairDelimiter() {
        return paramPairDelimiter;
    }
    
    public void setParamPairDelimiter(SOSOptionString paramPairDelimiter) {
        this.paramPairDelimiter = paramPairDelimiter;
    }
    
    public void setParamPairDelimiter(String paramPairDelimiter) {
        this.paramPairDelimiter = new SOSOptionString(paramPairDelimiter);
    }
    
    public SOSOptionString getParamKeyValueDelimiter() {
        return paramKeyValueDelimiter;
    }
    
    public void setParamKeyValueDelimiter(SOSOptionString paramKeyValueDelimiter) {
        this.paramKeyValueDelimiter = paramKeyValueDelimiter;
    }

    public void setParamKeyValueDelimiter(String paramKeyValueDelimiter) {
        this.paramKeyValueDelimiter = new SOSOptionString(paramKeyValueDelimiter);
    }

}
