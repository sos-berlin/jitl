
package com.sos.jitl.inventory.job;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
@JSOptionClass(name = "InventoryEventUpdateJobOptions", description = "Updates JobScheduler Objects in DB if an change event is fired")
public class InventoryEventUpdateJobOptions extends InventoryEventUpdateJobOptionsSuperClass {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryEventUpdateJobOptions.class);
 
	public InventoryEventUpdateJobOptions() {
        // TODO: Implement Constructor here
	}  

	public InventoryEventUpdateJobOptions(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} 

	public InventoryEventUpdateJobOptions (HashMap <String, String> jsSettings) throws Exception {
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