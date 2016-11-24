

package com.sos.jitl.inventory.job;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;

 
@JSOptionClass(name = "InventoryEventUpdateJobOptionsSuperClass", description = "InventoryEventUpdateJobOptionsSuperClass")
public class InventoryEventUpdateJobOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryEventUpdateJobOptionsSuperClass.class);

	public InventoryEventUpdateJobOptionsSuperClass() {
		objParentClass = this.getClass();
	}

	public InventoryEventUpdateJobOptionsSuperClass(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} 

	public InventoryEventUpdateJobOptionsSuperClass (HashMap <String, String> jsSettings) throws Exception {
		this();
		this.setAllOptions(jsSettings);
	}
 
	public void setAllOptions(HashMap <String, String> pobjJSSettings) {
		objSettings = pobjJSSettings;
		super.setAllOptions(pobjJSSettings);
	} 
 
	@Override
	public void checkMandatory() throws JSExceptionMandatoryOptionMissing, Exception {
		try {
			super.checkMandatory();
		} catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} 
 
	@Override
	public void commandLineArgs(String[] pstrArgs)   {
		super.commandLineArgs(pstrArgs);
		this.setAllOptions(super.objSettings);
	}

}	