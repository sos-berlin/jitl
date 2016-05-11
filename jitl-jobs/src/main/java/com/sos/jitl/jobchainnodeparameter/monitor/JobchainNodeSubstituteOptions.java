

package com.sos.jitl.jobchainnodeparameter.monitor;

import java.util.HashMap;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

 
@JSOptionClass(name = "ConfigurationMonitorOptions", description = "Configuration Monitor")
public class JobchainNodeSubstituteOptions extends JobchainNodeSubstituteOptionsSuperClass {
	@SuppressWarnings("unused")
	private final String CLASSNAME = "ConfigurationMonitorOptions";
	@SuppressWarnings("unused")
	private final static Logger LOGGER = Logger.getLogger(JobchainNodeSubstituteOptions.class);
 

	public JobchainNodeSubstituteOptions() {
	} // public ConfigurationMonitorOptions

	public JobchainNodeSubstituteOptions(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public ConfigurationMonitorOptions

		//

	public JobchainNodeSubstituteOptions (HashMap <String, String> JSSettings) throws Exception {
		super(JSSettings);
	} // public ConfigurationMonitorOptions (HashMap JSSettings)
 
	@Override  // ConfigurationMonitorOptionsSuperClass
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()
}

