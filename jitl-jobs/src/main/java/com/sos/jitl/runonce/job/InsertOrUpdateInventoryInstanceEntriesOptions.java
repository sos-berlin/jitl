package com.sos.jitl.runonce.job;

import java.util.HashMap;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;
 
@JSOptionClass(name = "InsertOrUpdateInventoryInstanceEntriesOptions", description = "Insert Or Update InventoryInstance DB Entries")
public class InsertOrUpdateInventoryInstanceEntriesOptions extends InsertOrUpdateInventoryInstanceEntriesOptionsSuperClass {

    private static final long serialVersionUID = 2818463919344867411L;
    private static final Logger LOGGER = Logger.getLogger(InsertOrUpdateInventoryInstanceEntriesOptions.class);
 
	public InsertOrUpdateInventoryInstanceEntriesOptions() {
        // TODO: Implement Constructor here
	}  

	public InsertOrUpdateInventoryInstanceEntriesOptions(JSListener jsListener) {
		this();
		this.registerMessageListener(jsListener);
	} 

	public InsertOrUpdateInventoryInstanceEntriesOptions (HashMap <String, String> jsSettings) throws Exception {
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