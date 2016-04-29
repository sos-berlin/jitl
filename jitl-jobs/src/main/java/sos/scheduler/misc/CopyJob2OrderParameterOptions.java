package sos.scheduler.misc;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "CopyJob2OrderParameterOptions", description = "CopyJob2OrderParameter")
public class CopyJob2OrderParameterOptions extends CopyJob2OrderParameterOptionsSuperClass {

    private static final long serialVersionUID = -2512565386435668056L;

    public CopyJob2OrderParameterOptions() {
    }

    public CopyJob2OrderParameterOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public CopyJob2OrderParameterOptions(HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    }

    @Override
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

}