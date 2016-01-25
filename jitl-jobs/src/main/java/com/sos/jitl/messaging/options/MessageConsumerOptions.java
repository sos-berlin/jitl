package com.sos.jitl.messaging.options;

import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionString;


public class MessageConsumerOptions extends MessageOptionSuperClass {

    private static final long serialVersionUID = 1L;

    @JSOptionDefinition(name = "targetJobChainName", 
            description = "the name of the target JobChain(s) to receive the created order, if more than one JobChain should receive the order"
                    + " the names of the JobChains can be separated with the [paramKeyValueDelimiter]", 
            key = "targetJobChainName", type = "SOSOptionString", mandatory = false)
    public SOSOptionString targetJobChainName = new SOSOptionString(this, 
            "targetJobChainName", 
            "the name of the target JobChain to receive the created order", 
            "", 
            "", 
            false);
    
    @JSOptionDefinition(name = "executeXml", 
            description = "this parameter determines if the received message should be interpreted as an JobScheduler XML command", 
            key = "executeXml", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean executeXml = new SOSOptionBoolean(this, 
            "executeXml", 
            "this parameter determines if the received message should be interpreted as an JobScheduler XML command", 
            "true", 
            "true", 
            false);
    
    @JSOptionDefinition(name = "jobParameters", 
            description = "this parameter determines if the received message should be interpreted as job parameters", 
            key = "jobParameters", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean jobParameters = new SOSOptionBoolean(this, 
            "jobParameters", 
            "this parameter determines if the received message should be interpreted as job parameters", 
            "", 
            "", 
            false);
    
    @JSOptionDefinition(name = "lastReceiver", 
            description = "this parameter determines if this consumer is the last to receive the message", 
            key = "lastReceiver", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean lastReceiver = new SOSOptionBoolean(this, 
            "lastReceiver", 
            "this parameter determines if this consumer is the last to receive the message", 
            "false", 
            "false", 
            false);
    
    public SOSOptionString getTargetJobChainName() {
        return targetJobChainName;
    }
   
    public void setTargetJobChainName(SOSOptionString targetJobChainName) {
        this.targetJobChainName = targetJobChainName;
    }

    public void setTargetJobChainName(String targetJobChainName) {
        this.targetJobChainName = new SOSOptionString(targetJobChainName);
    }
    
    public SOSOptionBoolean getExecuteXml() {
        return executeXml;
    }
    
    public void setExecuteXml(SOSOptionBoolean executeXml) {
        this.executeXml = executeXml;
    }

    public void setExecuteXml(Boolean executeXml) {
        this.executeXml = new SOSOptionBoolean(this, 
                "executeXml", 
                "this parameter determines if the received message should be interpreted as an JobScheduler XML command", 
                executeXml.toString(), 
                executeXml.toString(), 
                false);
    }
 
    public SOSOptionBoolean getJobParameters() {
        return jobParameters;
    }
    
    public void setJobParameters(SOSOptionBoolean jobParameters) {
        this.jobParameters = jobParameters;
    }

    public void setJobParameters(Boolean jobParameters) {
        this.jobParameters = new SOSOptionBoolean(this, 
                "jobParameters", 
                "this parameter determines if the received message should be interpreted as job parameters", 
                jobParameters.toString(), 
                jobParameters.toString(), 
                false);
    }

    public SOSOptionBoolean getLastReceiver() {
        return lastReceiver;
    }
    
    public void setLastReceiver(SOSOptionBoolean lastReceiver) {
        this.lastReceiver = lastReceiver;
    }
    
    public void setLastReceiver(Boolean lastReceiver) {
        this.lastReceiver = new SOSOptionBoolean(this, 
                "lastReceiver", 
                "this parameter determines if this consumer is the last to receive the message", 
                lastReceiver.toString(), 
                lastReceiver.toString(), 
                false);
    }
    
}
