package sos.scheduler.messaging.options;

import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionString;


public class MessageConsumerOptions extends MessageOptionSuperClass {

    private static final long serialVersionUID = 1L;

    @JSOptionDefinition(name = "targetJobChainName", description = "the name of the target JobChain to receive the created order", key = "targetJobChainName", type = "SOSOptionString", mandatory = true)
    public SOSOptionString targetJobChainName = new SOSOptionString(this, "targetJobChainName", "the name of the target JobChain to receive the created order", "", "", true);
    
    @JSOptionDefinition(name = "executeXml", description = "this parameter determines if the received message should be interpreted as an JobScheduler XML command", key = "executeXml", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean executeXml = new SOSOptionBoolean(this, "executeXml", "this parameter determines if the received message should be interpreted as an JobScheduler XML command", "true", "true", false);
    
    @JSOptionDefinition(name = "jobParameters", description = "this parameter determines if the received message should be interpreted as job parameters", key = "jobParameters", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean jobParameters = new SOSOptionBoolean(this, "jobParameters", "this parameter determines if the received message should be interpreted as job parameters", "", "", false);
    
    @JSOptionDefinition(name = "paramPairDelimiter", description = "this parameter determines which delimiter separates the key-value-pairs", key = "paramPairDelimiter", type = "SOSOptionString", mandatory = false)
    public SOSOptionString paramPairDelimiter = new SOSOptionString(this, "paramDelimiter", "this parameter determines which delimiter separates the key-value-pairs", "|", "|", false);

    @JSOptionDefinition(name = "paramKeyValueDelimiter", description = "this parameter determines which delimiter separates key and value", key = "paramKeyValueDelimiter", type = "SOSOptionString", mandatory = false)
    public SOSOptionString paramKeyValueDelimiter = new SOSOptionString(this, "paramDelimiter", "this parameter determines which delimiter separates key and value", ",", ",", false);

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
        this.executeXml = new SOSOptionBoolean(this, "executeXml", "this parameter determines if the received message should be interpreted as an JobScheduler XML command", executeXml.toString(), executeXml.toString(), false);
    }
 
    public SOSOptionBoolean getJobParameters() {
        return jobParameters;
    }
    
    public void setJobParameters(SOSOptionBoolean jobParameters) {
        this.jobParameters = jobParameters;
    }

    public void setJobParameters(Boolean jobParameters) {
        this.jobParameters = new SOSOptionBoolean(this, "jobParameters", "this parameter determines if the received message should be interpreted as job parameters", jobParameters.toString(), jobParameters.toString(), false);
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
