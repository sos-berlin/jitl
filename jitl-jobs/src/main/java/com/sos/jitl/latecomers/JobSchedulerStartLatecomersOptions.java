
package com.sos.jitl.latecomers;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JobSchedulerStartLatecomersOptions", description = "Starting late comers")
public class JobSchedulerStartLatecomersOptions extends JobSchedulerStartLatecomersOptionsSuperClass {

	public JobSchedulerStartLatecomersOptions() {
		// TODO: Implement Constructor here
	}

	public JobSchedulerStartLatecomersOptions(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	}

	public JobSchedulerStartLatecomersOptions(HashMap<String, String> jsSettings) throws Exception {
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
