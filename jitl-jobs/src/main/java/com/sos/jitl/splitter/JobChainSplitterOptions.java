package com.sos.jitl.splitter;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JobChainSplitterOptions", description = "Start a parallel processing in a jobchain")
public class JobChainSplitterOptions extends JobChainSplitterOptionsSuperClass {

    private static final long serialVersionUID = 1L;

    public JobChainSplitterOptions() {
    }

    public JobChainSplitterOptions(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobChainSplitterOptions(final HashMap<String, String> JSSettings) throws Exception {
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