package com.sos.jitl.checkrunhistory;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.i18n.annotation.I18NMessage;
import com.sos.i18n.annotation.I18NMessages;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
@JSOptionClass(name = "JobSchedulerCheckRunHistoryOptions", description = "Check the last job run")
public class JobSchedulerCheckRunHistoryOptions extends JobSchedulerCheckRunHistoryOptionsSuperClass {

    private static final long serialVersionUID = -3625891732295134070L;
    private final String conClassName = "JobSchedulerCheckRunHistoryOptions";
    @I18NMessages(value = {
            @I18NMessage("The Job Scheduler communication port"),
            @I18NMessage(value = "The Job Scheduler communication port", locale = "en_UK", explanation = "The Job Scheduler communication port"),
            @I18NMessage(value = "JobScheduler TCP-Port Nummer", locale = "de",
                    explanation = "Mit diesem Port kommuniziert der JobScheduler über TCP") }, msgnum = "JSJ_CRH_0010", msgurl = "msgurl")
    public static final String JSJ_CRH_0010 = "JSJ_CRH_0010";
    @I18NMessages(
            value = {
                    @I18NMessage("The name of the Job Scheduler host"),
                    @I18NMessage(value = "The name of the Job Scheduler host", locale = "en_UK", explanation = "The name of the Job Scheduler host"),
                    @I18NMessage(value = "Der Name oder die IP des JobScheduler Servers", locale = "de",
                            explanation = "The name of the Job Scheduler host") }, msgnum = "JSJ_CRH_0020", msgurl = "msgurl")
    public static final String JSJ_CRH_0020 = "JSJ_CRH_0020";

    public JobSchedulerCheckRunHistoryOptions() {
    }

    public JobSchedulerCheckRunHistoryOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerCheckRunHistoryOptions(HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    }

    @Override
    public void checkMandatory() {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @JSOptionDefinition(name = "schedulerPort", description = "The Job Scheduler communication port", key = "schedulerPort",
            type = "SOSOptionPortNumber", mandatory = false)
    public SOSOptionPortNumber schedulerPort = new SOSOptionPortNumber(this, conClassName + ".scheduler_port",
            "The Job Scheduler communication port", "0", "4444", false);
    public SOSOptionPortNumber schedulerTcpPortNumber = (SOSOptionPortNumber) schedulerPort.SetAlias(conClassName + ".SchedulerTcpPortNumber");
    public SOSOptionPortNumber portNumber = (SOSOptionPortNumber) schedulerPort.SetAlias(conClassName + ".PortNumber");

    public SOSOptionPortNumber getPortNumber() {
        return schedulerPort;
    }

    public void setPortNumber(SOSOptionPortNumber p_PortNumber) {
        this.schedulerPort = p_PortNumber;
    }

    @JSOptionDefinition(name = "schedulerHostName", description = "The name of the Job Scheduler host", key = "schedulerHostName",
            type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName schedulerHostName = new SOSOptionHostName(this, conClassName + ".SchedulerHostName",
            "The name of the Job Scheduler host", "", "localhost", false);

    public SOSOptionHostName getSchedulerHostName() {
        return schedulerHostName;
    }

    public void setHostName(SOSOptionHostName p_SchedulerHostName) {
        this.schedulerHostName = p_SchedulerHostName;
    }

}