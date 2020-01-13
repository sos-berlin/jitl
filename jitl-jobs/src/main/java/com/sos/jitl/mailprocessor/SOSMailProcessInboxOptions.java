
package com.sos.jitl.mailprocessor;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
 
@JSOptionClass(name = "SOSMailProcessInboxOptions", description = "Process email incoming box")
public class SOSMailProcessInboxOptions extends SOSMailProcessInboxOptionsSuperClass {

 
	private static final long serialVersionUID = 1L;
	private static final String CLASSNAME = "SOSMailProcessInboxOptions";
	private static final Logger LOGGER = LoggerFactory.getLogger(SOSMailProcessInboxOptions.class);
 
	public SOSMailProcessInboxOptions() {
        // TODO: Implement Constructor here
	}  

	public SOSMailProcessInboxOptions(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} 

	public SOSMailProcessInboxOptions (HashMap <String, String> jsSettings) throws Exception {
		super(jsSettings);
	}  
 
	@Override   
	public void checkMandatory() {
		try {
			super.checkMandatory();
		} catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	}  

}
