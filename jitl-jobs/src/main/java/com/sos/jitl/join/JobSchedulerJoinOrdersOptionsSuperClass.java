
package com.sos.jitl.join;

import java.util.HashMap;
import com.sos.JSHelper.Options.*;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JobSchedulerJoinOrdersOptionsSuperClass", description = "JobSchedulerJoinOrdersOptionsSuperClass")
public class JobSchedulerJoinOrdersOptionsSuperClass extends JSOptionsClass {

    static final long serialVersionUID = 1L;
    private static final String CLASSNAME = "JobSchedulerJoinOrdersOptionsSuperClass";

    public JobSchedulerJoinOrdersOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JobSchedulerJoinOrdersOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerJoinOrdersOptionsSuperClass(HashMap<String, String> jsSettings) throws Exception {
        this();
        this.setAllOptions(jsSettings);
    }

    @JSOptionDefinition(name = "required_orders", description = "", key = "required_orders", type = "SOSOptionInteger",
            mandatory = false)
    public SOSOptionInteger required_orders = new SOSOptionInteger(this, conClassName + ".required_orders", "", "1", "1", false);

    public SOSOptionInteger getRequired_orders() {
        return required_orders;
    }

    public void setRequired_orders(final SOSOptionInteger p_required_orders) {
        required_orders = p_required_orders;
    }
    

    @JSOptionDefinition(name = "join_session_id", description = "", key = "join_session_id", type = "SOSOptionString", mandatory = false)
    public SOSOptionString joinSessionId = new SOSOptionString(this, CLASSNAME + ".join_session_id", ""," ", " ", false);

    public SOSOptionString getJoinSessionId() {
        return joinSessionId;
    }

    public void setJoinSessionId(SOSOptionString join_session_id) {
        this.joinSessionId = join_session_id;
    }

    @JSOptionDefinition(name = "show_join_order_list", description = "", key = "show_join_order_list", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean showJoinOrderList = new SOSOptionBoolean(this, conClassName + ".show_join_order_list","", "false", "false", false);

    public SOSOptionBoolean getShowJoinOrderList() {
        return showJoinOrderList;
    }

    public void setShowJoinOrderList(SOSOptionBoolean showJoinOrderList) {
        this.showJoinOrderList = showJoinOrderList;
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
