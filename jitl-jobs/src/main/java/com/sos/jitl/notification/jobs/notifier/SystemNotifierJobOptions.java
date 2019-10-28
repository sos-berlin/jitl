package com.sos.jitl.notification.jobs.notifier;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Options.JSOptionMailOptions;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.jitl.notification.jobs.NotificationJobOptionsSuperClass;

@JSOptionClass(name = "SystemNotifierJobOptions", description = "SystemNotifierJobOptions")
public class SystemNotifierJobOptions extends NotificationJobOptionsSuperClass {

    private static final long serialVersionUID = 1L;
    private final String conClassName = SystemNotifierJobOptions.class.getSimpleName();

    @JSOptionDefinition(name = "schema_configuration_file", description = "", key = "schema_configuration_file", type = "SOSOptionString", mandatory = true)
    public SOSOptionString schema_configuration_file = new SOSOptionString(this, conClassName + ".schema_configuration_file", // HashMap-Key
            "", // Titel
            "", // InitValue
            "", // DefaultValue
            true // isMandatory
    );

    @JSOptionDefinition(name = "system_configuration_file", description = "", key = "system_configuration_file", type = "SOSOptionString", mandatory = true)
    public SOSOptionString system_configuration_file = new SOSOptionString(this, conClassName + ".system_configuration_file", // HashMap-Key
            "", // Titel
            "", // InitValue
            "", // DefaultValue
            false // isMandatory
    );

    @JSOptionDefinition(name = "default_configuration_file", description = "", key = "default_configuration_file", type = "SOSOptionString", mandatory = false)
    public SOSOptionString default_configuration_file = new SOSOptionString(this, conClassName + ".default_configuration_file", // HashMap-Key
            "", // Titel
            "", // InitValue
            "", // DefaultValue
            true // isMandatory
    );

    @JSOptionDefinition(name = "plugin_job_name", description = "", key = "plugin_job_name", type = "SOSOptionString", mandatory = false)
    public SOSOptionString plugin_job_name = new SOSOptionString(this, conClassName + ".plugin_job_name", // HashMap-Key
            "", // Titel
            "", // InitValue
            "", // DefaultValue
            false // isMandatory
    );

    @JSOptionDefinition(name = "scheduler_mail_settings", description = "", key = "scheduler_mail_settings", type = "JSOptionMailOptions", mandatory = false)
    public JSOptionMailOptions scheduler_mail_settings = new JSOptionMailOptions(this, conClassName + ".scheduler_mail_settings", // HashMap-Key
            "", // Titel
            null, // InitValue
            null, // DefaultValue
            false // isMandatory
    );
}