package com.sos.jitl.splitter;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JobChainSplitterOptions", description = "Start a parallel processing in a jobchain")
public class JobChainSplitterOptions extends JobChainSplitterOptionsSuperClass {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private final String conClassName = "JobChainSplitterOptions";
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JobChainSplitterOptions.class);

    /** constructors */

    public JobChainSplitterOptions() {
    } // public JobChainSplitterOptions

    public JobChainSplitterOptions(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JobChainSplitterOptions

    //

    public JobChainSplitterOptions(final HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    } // public JobChainSplitterOptions (HashMap JSSettings)

    /** \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
     *
     * \details
     * 
     * @throws Exception
     *
     * @throws Exception - wird ausgelöst, wenn eine mandatory-Option keinen
     *             Wert hat */
    @Override
    // JobChainSplitterOptionsSuperClass
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()
}
