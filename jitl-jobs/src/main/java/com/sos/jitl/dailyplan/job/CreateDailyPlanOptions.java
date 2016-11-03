package com.sos.jitl.dailyplan.job;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "CreateDaysPlanOptions", description = "Creating a Daily Plan depending on actual Runtimes")
public class CreateDailyPlanOptions extends CreateDailyPlanOptionsSuperClass {

    private static final long serialVersionUID = 1L;

    public CreateDailyPlanOptions() {
    }

    public CreateDailyPlanOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public CreateDailyPlanOptions(HashMap<String, String> JSSettings) throws Exception {
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