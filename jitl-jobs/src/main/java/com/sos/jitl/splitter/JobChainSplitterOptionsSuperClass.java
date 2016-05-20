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
    public SOSOptionString next_state_name = new SOSOptionString(this, conClassName + ".next_state_name", "", "", "", false);

    public SOSOptionString getnext_state_name() {
        return next_state_name;
    }

    public void setnext_state_name(final SOSOptionString p_next_state_name) {
        next_state_name = p_next_state_name;
    }

    @JSOptionDefinition(name = "state_names", description = "", key = "state_names", type = "SOSOptionString", mandatory = true)
    public SOSOptionStringValueList StateNames = new SOSOptionStringValueList(this, conClassName + ".state_names", "", "", "", true);

    public SOSOptionStringValueList getStateNames() {
        return StateNames;
    }

    public void setStateNames(final SOSOptionStringValueList p_state_names) {
        StateNames = p_state_names;
    }

    @JSOptionDefinition(name = "sync_state_name", description = "", key = "sync_state_name", type = "SOSOptionString", mandatory = false)
    public SOSOptionString SyncStateName = new SOSOptionString(this, conClassName + ".sync_state_name", "", "", "", false);

    public SOSOptionString getsync_state_name() {
        return SyncStateName;
    }

    public void setsync_state_name(final SOSOptionString p_sync_state_name) {
        SyncStateName = p_sync_state_name;
    }

    @JSOptionDefinition(name = "create_sync_session_id", description = "", key = "create_sync_session_id", type = "SOSOptionBoolean",
            mandatory = false)
    public SOSOptionBoolean createSyncSessionId = new SOSOptionBoolean(this, conClassName + ".create_sync_context", "", "false", "false", false);

    public SOSOptionBoolean getcreate_sync_session_id() {
        return createSyncSessionId;
    }

    public void setcreate_sync_session_id(final SOSOptionBoolean p_create_sync_session_id) {
        createSyncSessionId = p_create_sync_session_id;
    }

    @JSOptionDefinition(name = "create_sync_context", description = "", key = "create_sync_context", type = "SOSOptionString", mandatory = false)
    public SOSOptionBoolean createSyncContext = new SOSOptionBoolean(this, conClassName + ".create_sync_context", "", "", "true", false);

    public SOSOptionBoolean getcreate_sync_context() {
        return createSyncContext;
    }

    public void setcreate_sync_context(final SOSOptionBoolean p_create_sync_context) {
        createSyncContext = p_create_sync_context;
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
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.setSettings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
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