package com.sos.jitl.dailyplan.job;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionString;

@JSOptionClass(name = "CheckDailyScheduleOptionsSuperClass", description = "CheckDailyScheduleOptionsSuperClass")
public class CheckDailyPlanOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private final String conClassName = "CheckDailyScheduleOptionsSuperClass";

    @JSOptionDefinition(name = "configuration_file", description = "Die Datei mit den Einstellungen f? Datenbank. Beispiel: <?xml v", key = "configuration_file", type = "SOSOptionString", mandatory = false)
    public SOSOptionString configuration_file = new SOSOptionString(this, conClassName + ".configuration_file",
            "Die Datei mit den Einstellungen f? Datenbank. Beispiel: <?xml v", " ", " ", false);

    public SOSOptionString getconfiguration_file() {
        return configuration_file;
    }

    public void setconfiguration_file(SOSOptionString p_configuration_file) {
        this.configuration_file = p_configuration_file;
    }

    @JSOptionDefinition(name = "dayOffset", description = "", key = "dayOffset", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger dayOffset = new SOSOptionInteger(this, conClassName + ".dayOffset", "", "-1", "-1", false);

    public SOSOptionInteger getdayOffset() {
        return dayOffset;
    }

    public void setdayOffset(SOSOptionInteger p_dayOffset) {
        this.dayOffset = p_dayOffset;
    }

    @JSOptionDefinition(name = "scheduler_id", description = "", key = "scheduler_id", type = "SOSOptionString", mandatory = false)
    public SOSOptionString scheduler_id = new SOSOptionString(this, conClassName + ".scheduler_id", "", "", "", false);

    public SOSOptionString getscheduler_id() {
        return scheduler_id;
    }

    public void setscheduler_id(SOSOptionString p_scheduler_id) {
        this.scheduler_id = p_scheduler_id;
    }

    @JSOptionDefinition(name = "check_all_jobscheduler_instances", description = "", key = "check_all_jobscheduler_instances", type = "SOSOptionBool", mandatory = false)
    public SOSOptionBoolean check_all_jobscheduler_instances = new SOSOptionBoolean(this, conClassName + ".check_all_jobscheduler_instances", "", "",
            "", false);

    public SOSOptionBoolean getcheck_all_jobscheduler_instances() {
        return check_all_jobscheduler_instances;
    }

    public void setcheck_all_jobscheduler_instances(SOSOptionBoolean p_check_all_jobscheduler_instances) {
        this.check_all_jobscheduler_instances = p_check_all_jobscheduler_instances;
    }

    public CheckDailyPlanOptionsSuperClass() {
        currentClass = this.getClass();
    }

    public CheckDailyPlanOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public CheckDailyPlanOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    public void setAllOptions(HashMap<String, String> settings) {
        super.setAllOptions(settings);

    } // public void setAllOptions (HashMap <String, String> JSSettings)

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
        this.setAllOptions(super.getSettings());
    }

}
