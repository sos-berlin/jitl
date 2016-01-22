package sos.scheduler.messaging.options;

import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionString;


public class MessageProducerOptions extends MessageOptionSuperClass {

    private static final long serialVersionUID = 1L;

    @JSOptionDefinition(name = "message", 
            description = "the message to send to the messaging server", 
            key = "message", type = "SOSOptionString", mandatory = false)
    public SOSOptionString message = new SOSOptionString(this, 
            "message", 
            "the message to send to the messaging server", 
            "", 
            "", 
            false);

    @JSOptionDefinition(name = "sendXml", 
            description = "this parameter determines if the message should contain an JobScheduler XML command", 
            key = "sendXml", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean sendXml = new SOSOptionBoolean(this, 
            "sendXml", 
            "this parameter determines if the message should contain an JobScheduler XML command", 
            "", 
            "", 
            true);
    
    @JSOptionDefinition(name = "sendJobParameters", 
            description = "this parameter determines if the message should contain job parameters", 
            key = "sendJobParameters", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean sendJobParameters = new SOSOptionBoolean(this, 
            "sendJobParameters", 
            "this parameter determines if the message should contain job parameters", 
            "", 
            "", 
            false);
    
    public SOSOptionString getMessage() {
        return message;
    }
    
    public void setMessage(SOSOptionString message) {
        this.message = message;
    }
    
    public void setMessage(String message) {
        this.message = new SOSOptionString(message);
    }

    public SOSOptionBoolean getSendXml() {
        return sendXml;
    }
    
    public void setSendXml(SOSOptionBoolean sendXml) {
        this.sendXml = sendXml;
    }
    
    public void setSendXml(Boolean sendXml) {
        this.sendXml = new SOSOptionBoolean(this, 
                "sendXml", 
                "this parameter determines if the message should contain an JobScheduler XML command", 
                sendXml.toString(), 
                sendXml.toString(), 
                true);
    }
    
    public SOSOptionBoolean getSendJobParameters() {
        return sendJobParameters;
    }

    public void setSendJobParameters(SOSOptionBoolean sendJobParameters) {
        this.sendJobParameters = sendJobParameters;
    }
    
    public void setSendJobParameters(Boolean sendJobParameters) {
        this.sendJobParameters = new SOSOptionBoolean(this, 
                "sendJobParameters", 
                "this parameter determines if the message should contain job parameters", 
                sendJobParameters.toString(), 
                sendJobParameters.toString(), 
                false);
    }
    
}
