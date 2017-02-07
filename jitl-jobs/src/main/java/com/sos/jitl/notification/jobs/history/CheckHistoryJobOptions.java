package com.sos.jitl.notification.jobs.history;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.jitl.notification.jobs.NotificationJobOptionsSuperClass;

@JSOptionClass(name = "CheckHistoryJobOptions", description = "CheckHistoryJobOptions")
public class CheckHistoryJobOptions extends NotificationJobOptionsSuperClass {

	private static final long serialVersionUID = 1L;
	private final String conClassName = CheckHistoryJobOptions.class.getSimpleName();

	@JSOptionDefinition(name = "plugins", description = "", key = "plugins", type = "SOSOptionString", mandatory = false)
	public SOSOptionString plugins = new SOSOptionString(this, conClassName + ".plugins", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	public SOSOptionString getplugins() {
		return plugins;
	}

	public void setplugins(SOSOptionString val) {
		this.plugins = val;
	}

	@JSOptionDefinition(name = "schema_configuration_file", description = "", key = "schema_configuration_file", type = "SOSOptionString", mandatory = false)
	public SOSOptionString schema_configuration_file = new SOSOptionString(this,
			conClassName + ".schema_configuration_file", // HashMap-Key
			"", // Titel
			" ", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	public SOSOptionString getschema_configuration_file() {
		return schema_configuration_file;
	}

	public void setschema_configuration_file(SOSOptionString val) {
		this.schema_configuration_file = val;
	}

	@JSOptionDefinition(name = "configuration_dir", description = "", key = "configuration_dir", type = "SOSOptionString", mandatory = false)
	public SOSOptionString configuration_dir = new SOSOptionString(this, conClassName + ".configuration_dir", // HashMap-Key
			"", // Titel
			" ", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	public SOSOptionString getconfiguration_dir() {
		return configuration_dir;
	}

	public void setconfiguration_dir(SOSOptionString val) {
		this.configuration_dir = val;
	}
}