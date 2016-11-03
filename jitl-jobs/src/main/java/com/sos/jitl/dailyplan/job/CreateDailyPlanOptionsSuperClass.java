package com.sos.jitl.dailyplan.job;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionString;

@JSOptionClass(name = "CreateDailyPlanOptionsSuperClass", description = "CreateDailyPlanOptionsSuperClass")
public class CreateDailyPlanOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private final String conClassName = "CreateDailyPlanOptionsSuperClass";

    @JSOptionDefinition(name = "SchedulerHostName", description = "", key = "SchedulerHostName", type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName SchedulerHostName = new SOSOptionHostName(this, conClassName + ".SchedulerHostName", "", " ", " ", false);

    public SOSOptionHostName getSchedulerHostName() {
        return SchedulerHostName;
    }

    public void setSchedulerHostName(SOSOptionHostName p_SchedulerHostName) {
        this.SchedulerHostName = p_SchedulerHostName;
    }

    @JSOptionDefinition(name = "configuration_file", description = "", key = "configuration_file", type = "SOSOptionString", mandatory = false)
    public SOSOptionString configuration_file = new SOSOptionString(this, conClassName + ".configuration_file", "", " ", " ", false);

    public SOSOptionString getconfiguration_file() {
        return configuration_file;
    }

    public void setconfiguration_file(SOSOptionString p_configuration_file) {
        this.configuration_file = p_configuration_file;
    }

    @JSOptionDefinition(name = "dayOffset", description = "", key = "dayOffset", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger dayOffset = new SOSOptionInteger(this, conClassName + ".dayOffset", "", "0", "0", false);

    public SOSOptionInteger getdayOffset() {
        return dayOffset;
    }

    public void setdayOffset(SOSOptionInteger p_dayOffset) {
        this.dayOffset = p_dayOffset;
    }

    @JSOptionDefinition(name = "scheduler_port", description = "", key = "scheduler_port", type = "SOSOptionPortNumber", mandatory = false)
    public SOSOptionPortNumber scheduler_port = new SOSOptionPortNumber(this, conClassName + ".scheduler_port", "", "4444", "4444", false);
    public SOSOptionPortNumber SchedulerTcpPortNumber = (SOSOptionPortNumber) scheduler_port.setAlias(conClassName + ".SchedulerTcpPortNumber");

    public SOSOptionPortNumber getscheduler_port() {
        return scheduler_port;
    }

    public void setscheduler_port(SOSOptionPortNumber p_scheduler_port) {
        this.scheduler_port = p_scheduler_port;
    }

    public CreateDailyPlanOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public CreateDailyPlanOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public CreateDailyPlanOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    public void setAllOptions(HashMap<String, String> pobjJSSettings) {
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
