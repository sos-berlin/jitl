package com.sos.jitl.dailyplan.job;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "CheckDailyPlanOptions", description = "Checking a DailyPlan with runs in History")
public class CheckDailyPlanOptions extends CheckDailyPlanOptionsSuperClass {

    private static final long serialVersionUID = 1L;

    public CheckDailyPlanOptions() {
    }

    public CheckDailyPlanOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public CheckDailyPlanOptions(HashMap<String, String> JSSettings) throws Exception {
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

}