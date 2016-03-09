package com.sos.jitl.operations.criticalpath.job;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Robert Ehrlich */
@JSOptionClass(name = "UncriticalJobNodesJobOptions", description = "UncriticalJobNodes")
public class UncriticalJobNodesJobOptions extends UncriticalJobNodesJobOptionsSuperClass {

    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private final static String conClassName = UncriticalJobNodesJobOptions.class.getSimpleName();
    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(UncriticalJobNodesJobOptions.class);

    /**
	 * 
	 */
    public UncriticalJobNodesJobOptions() {
    }

    /** @param pobjListener */
    public UncriticalJobNodesJobOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    /** @param JSSettings
     * @throws Exception */
    public UncriticalJobNodesJobOptions(HashMap<String, String> JSSettings) throws Exception {
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
