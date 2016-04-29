package sos.scheduler.xsl;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JobSchedulerXslTransformationOptions", description = "JobSchedulerXslTransform")
public class JobSchedulerXslTransformOptions extends JobSchedulerXslTransformOptionsSuperClass {

    private static final long serialVersionUID = -5277454257958660505L;

    public JobSchedulerXslTransformOptions() {
    }

    public JobSchedulerXslTransformOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerXslTransformOptions(HashMap<String, String> JSSettings) throws Exception {
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