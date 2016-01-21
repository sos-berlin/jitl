package sos.scheduler.messaging.options;

import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Options.SOSOptionString;


public class MessageProducerOptions extends MessageOptionSuperClass {

    private static final long serialVersionUID = 1L;

    @JSOptionDefinition(name = "message", description = "the message to send to the messaging server", key = "message", type = "SOSOptionString", mandatory = true)
    public SOSOptionString message = new SOSOptionString(this, "message", "the message to send to the messaging server", "", "", true);

    public SOSOptionString getMessage() {
        return message;
    }
    
    public void setMessage(SOSOptionString message) {
        this.message = message;
    }
    
    public void setMessage(String message) {
        this.message = new SOSOptionString(message);
    }
    
    
}
