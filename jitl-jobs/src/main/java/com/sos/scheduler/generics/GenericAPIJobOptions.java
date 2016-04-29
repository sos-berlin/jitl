package com.sos.scheduler.generics;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "GenericAPIJobOptions", description = "A generic internal API job")
public class GenericAPIJobOptions extends GenericAPIJobOptionsSuperClass {

    private static final long serialVersionUID = 1L;

    public GenericAPIJobOptions() {
    }

    public GenericAPIJobOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public GenericAPIJobOptions(HashMap<String, String> JSSettings) throws Exception {
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