
package com.sos.jitl.join;

import java.util.HashMap;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JobSchedulerJoinOrdersOptions", description = "Join Orders coming from a split")
public class JobSchedulerJoinOrdersOptions extends JobSchedulerJoinOrdersOptionsSuperClass {

    private static final long serialVersionUID = 1L; 
	public JobSchedulerJoinOrdersOptions() {
	}  

	public JobSchedulerJoinOrdersOptions(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} 

	public JobSchedulerJoinOrdersOptions (HashMap <String, String> jsSettings) throws Exception {
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
