
package com.sos.jitl.latecomers;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Options.*;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JobSchedulerStartLatecomersOptionsSuperClass", description = "JobSchedulerStartLatecomersOptionsSuperClass")
public class JobSchedulerStartLatecomersOptionsSuperClass extends JSOptionsClass {
	private static final String CLASSNAME = "JobSchedulerStartLatecomersOptionsSuperClass";
	private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerStartLatecomersOptionsSuperClass.class);

	public JobSchedulerStartLatecomersOptionsSuperClass() {
		objParentClass = this.getClass();
	}

	public JobSchedulerStartLatecomersOptionsSuperClass(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	}

	public JobSchedulerStartLatecomersOptionsSuperClass(HashMap<String, String> jsSettings) throws Exception {
		this();
		this.setAllOptions(jsSettings);
	}

	@JSOptionDefinition(name = "day_offset", description = "Specify the number of days to look in the past. Example: 10d looks te", key = "day_offset", type = "SOSOptionString", mandatory = false)
	public SOSOptionString dayOffset = new SOSOptionString(this, CLASSNAME + ".day_offset",
			"Specify the number of days to look in the past. Example: 10d looks te",
			// InitValue, DefaultValue, isMandatory
			"0d", "0d", false);

	public SOSOptionString getDayOffset() {
		return dayOffset;
	}

	public void setDayOffset(SOSOptionString dayOffset) {
		this.dayOffset = dayOffset;
	}

	@JSOptionDefinition(name = "ignore_folder_list", description = "A comma seperated list of folders. These folders will be ignored by t", key = "ignore_folder_list", type = "SOSOptionString", mandatory = false)
	public SOSOptionString ignoreFolderList = new SOSOptionString(this, CLASSNAME + ".ignore_folder_list",
			"A comma seperated list of folders. These folders will be ignored by t",
			// InitValue, DefaultValue, isMandatory
			" ", " ", false);

	public SOSOptionString getIgnoreFolderList() {
		return ignoreFolderList;
	}

	public void setIgnoreFolderList(SOSOptionString ignoreFolderList) {
		this.ignoreFolderList = ignoreFolderList;
	}

	@JSOptionDefinition(name = "ignore_job_list", description = "A comma seperated list of jobs. Then name can contain wildcards % whi", key = "ignore_job_list", type = "SOSOptionString", mandatory = false)
	public SOSOptionString ignoreJobList = new SOSOptionString(this, CLASSNAME + ".ignore_job_list",
			"A comma seperated list of jobs. Then name can contain wildcards % whi",
			// InitValue, DefaultValue, isMandatory
			" ", " ", false);

	public SOSOptionString getIgnoreJobList() {
		return ignoreJobList;
	}

	public void setIgnoreJobList(SOSOptionString ignoreJobList) {
		this.ignoreJobList = ignoreJobList;
	}

	@JSOptionDefinition(name = "ignore_order_list", description = "A comma seperated list of orders. Then name can contain wildcards", key = "ignore_order_list", type = "SOSOptionString", mandatory = false)
	public SOSOptionString ignoreOrderList = new SOSOptionString(this, CLASSNAME + ".ignore_order_list",
			"A comma seperated list of orders. Then name can contain wildcards",
			// InitValue, DefaultValue, isMandatory
			" ", " ", false);

	public SOSOptionString getIgnoreOrderList() {
		return ignoreOrderList;
	}

	public void setIgnoreOrderList(SOSOptionString ignoreOrderList) {
		this.ignoreOrderList = ignoreOrderList;
	}

	@JSOptionDefinition(name = "jobs", description = "A comma seperated list of jobs. If this parameter is set, only those", key = "jobs", type = "SOSOptionString", mandatory = false)
	public SOSOptionString jobs = new SOSOptionString(this, CLASSNAME + ".jobs",
			"A comma seperated list of jobs. If this parameter is set, only those",
			// InitValue, DefaultValue, isMandatory
			" ", " ", false);

	public SOSOptionString getJobs() {
		return jobs;
	}

	public void setJobs(SOSOptionString jobs) {
		this.jobs = jobs;
	}

	@JSOptionDefinition(name = "only_report", description = "If true no job (orders) will be startet but just listed in the log.", key = "only_report", type = "SOSOptionString", mandatory = false)
	public SOSOptionBoolean onlyReport = new SOSOptionBoolean(this, CLASSNAME + ".only_report",
			"If true no job (orders) will be startet but just listed in the log.",
			// InitValue, DefaultValue, isMandatory
			"false", "false", false);

	public SOSOptionBoolean getOnlyReport() {
		return onlyReport;
	}

	public void SOSOptionBoolean(SOSOptionBoolean onlyReport) {
		this.onlyReport = onlyReport;
	}

	@JSOptionDefinition(name = "orders", description = "A comma seperated list of orders. If this parameter is set, only thos", key = "orders", type = "SOSOptionString", mandatory = false)
	public SOSOptionString orders = new SOSOptionString(this, CLASSNAME + ".orders",
			"A comma seperated list of orders. If this parameter is set, only thos",
			// InitValue, DefaultValue, isMandatory
			" ", " ", false);

	public SOSOptionString getOrders() {
		return orders;
	}

	public void setOrders(SOSOptionString orders) {
		this.orders = orders;
	}

	private String getAllOptionsAsString() {
		final String METHODNAME = CLASSNAME + "::getAllOptionsAsString";
		String strT = CLASSNAME + "\n";
		strT += this.toString();
		return strT;
	}

	public void setAllOptions(HashMap<String, String> pobjJSSettings) {
		final String METHODNAME = CLASSNAME + "::setAllOptions";
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
	public void commandLineArgs(String[] pstrArgs) {
		super.commandLineArgs(pstrArgs);
		this.setAllOptions(super.objSettings);
	}

}
