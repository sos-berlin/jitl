package sos.scheduler.misc;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionString;

@JSOptionClass(name = "CopyJob2OrderParameterOptionsSuperClass", description = "CopyJob2OrderParameterOptionsSuperClass")
public class CopyJob2OrderParameterOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private final String conClassName = "CopyJob2OrderParameterOptionsSuperClass";

    @JSOptionDefinition(name = "operation", description = "", key = "operation", type = "SOSOptionString", mandatory = false)
    public SOSOptionString operation = new SOSOptionString(this, conClassName + ".operation", "", "copy", "copy", false);

    public SOSOptionString getoperation() {
        return operation;
    }

    public void setoperation(SOSOptionString p_operation) {
        this.operation = p_operation;
    }

    public CopyJob2OrderParameterOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public CopyJob2OrderParameterOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public CopyJob2OrderParameterOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    public void setAllOptions(HashMap<String, String> settings) {
        super.setAllOptions(settings);
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
        this.setAllOptions(super.getSettings());
    }

}