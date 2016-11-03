package com.sos.jitl.inventory.job;

import java.util.HashMap;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;
 
@JSOptionClass(name = "InsertOrUpdateInventoryInstanceEntriesOptions", description = "Insert Or Update InventoryInstance DB Entries")
public class InitialInventoryUpdateJobOptions extends InitialInventoryUpdateJobOptionsSuperClass {

    private static final long serialVersionUID = 2818463919344867411L;
    private static final Logger LOGGER = Logger.getLogger(InitialInventoryUpdateJobOptions.class);
 
	public InitialInventoryUpdateJobOptions() {
        // TODO: Implement Constructor here
	}  

	public InitialInventoryUpdateJobOptions(JSListener jsListener) {
		this();
		this.registerMessageListener(jsListener);
	} 

	public InitialInventoryUpdateJobOptions (HashMap <String, String> jsSettings) throws Exception {
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