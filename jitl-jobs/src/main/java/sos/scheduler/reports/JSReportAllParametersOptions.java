package sos.scheduler.reports;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JSReportAllParametersOptions", description = "Report all Parameters")
public class JSReportAllParametersOptions extends JSReportAllParametersOptionsSuperClass {

    private static final long serialVersionUID = 1L;

    public JSReportAllParametersOptions() {
    }

    public JSReportAllParametersOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JSReportAllParametersOptions(HashMap<String, String> JSSettings) throws Exception {
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