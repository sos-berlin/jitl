package com.sos.jitl.splitter;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionStringValueList;

@JSOptionClass(name = "JobChainSplitterOptionsSuperClass", description = "JobChainSplitterOptionsSuperClass")
public class JobChainSplitterOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = -5275742216092117420L;
    private final String conClassName = "JobChainSplitterOptionsSuperClass";

    @JSOptionDefinition(name = "next_state_name", description = "", key = "next_state_name", type = "SOSOptionString", mandatory = false)
    public SOSOptionString nextStateName = new SOSOptionString(this, conClassName + ".next_state_name", "", "", "", false);

    public SOSOptionString getNextStateName() {
        return nextStateName;
    }

    public void setNextStateName(final SOSOptionString nextStateName) {
        this.nextStateName = nextStateName;
    }

    @JSOptionDefinition(name = "state_names", description = "", key = "state_names", type = "SOSOptionString", mandatory = true)
    public SOSOptionStringValueList stateNames = new SOSOptionStringValueList(this, conClassName + ".state_names", "", "", "", true);

    public SOSOptionStringValueList getStateNames() {
        return stateNames;
    }

    public void setStateNames(final SOSOptionStringValueList stateNames) {
        this.stateNames = stateNames;
    }

    @JSOptionDefinition(name = "sync_state_name", description = "", key = "sync_state_name", type = "SOSOptionString", mandatory = false)
    public SOSOptionString syncStateName = new SOSOptionString(this, conClassName + ".sync_state_name", "", "", "", false);

    public SOSOptionString getSyncStateName() {
        return syncStateName;
    }

    public void setSyncStateName(final SOSOptionString syncStateName) {
        this.syncStateName = syncStateName;
    }

    @JSOptionDefinition(name = "join_state_name", description = "", key = "join_state_name", type = "SOSOptionString", mandatory = false)
    public SOSOptionString joinStateName = new SOSOptionString(this, conClassName + ".join_state_name", "", "", "", false);

    public SOSOptionString getJoinStateName() {
        return joinStateName;
    }

    public void setJoinStateName(final SOSOptionString joinStateName) {
        this.joinStateName = joinStateName;
    }

    @JSOptionDefinition(name = "create_sync_session_id", description = "", key = "create_sync_session_id", type = "SOSOptionBoolean",
            mandatory = false)
    public SOSOptionBoolean createSyncSessionId = new SOSOptionBoolean(this, conClassName + ".create_sync_session_id", "", "false", "false", false);

    public SOSOptionBoolean getCreateSyncSessionId() {
        return createSyncSessionId;
    }

    public void setCreateSyncSession_id(final SOSOptionBoolean createSyncSessionId) {
        this.createSyncSessionId = createSyncSessionId;
    }

    @JSOptionDefinition(name = "create_sync_context", description = "", key = "create_sync_context", type = "SOSOptionString", mandatory = false)
    public SOSOptionBoolean createSyncContext = new SOSOptionBoolean(this, conClassName + ".create_sync_context", "", "", "true", false);

    public SOSOptionBoolean getCreateSyncContext() {
        return createSyncContext;
    }

    public void setCreateSyncContext(final SOSOptionBoolean createSyncContext) {
        this.createSyncContext = createSyncContext;
    }

    public JobChainSplitterOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JobChainSplitterOptionsSuperClass(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobChainSplitterOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    @Override
    public void setAllOptions(final HashMap<String, String> pobjJSSettings) {
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
    public void commandLineArgs(final String[] pstrArgs) {
        super.commandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }

}