package com.sos.jitl.splitter;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

/** \class JobChainSplitterOptions - Start a parallel processing in a jobchain
 *
 * \brief An Options as a container for the Options super class. The Option
 * class will hold all the things, which would be otherwise overwritten at a
 * re-creation of the super-class.
 *
 *
 * 
 *
 * see \see
 * C:\Users\KB\AppData\Local\Temp\scheduler_editor-121986169113382203.html for
 * (more) details.
 *
 * \verbatim ; mechanicaly created by JobDocu2OptionsClass.xslt from
 * http://www.sos-berlin.com at 20130315155436 \endverbatim */
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
