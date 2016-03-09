package com.sos.scheduler.generics;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

@JSOptionClass(name = "GenericAPIJobOptions", description = "A generic internal API job")
public class GenericAPIJobOptions extends GenericAPIJobOptionsSuperClass {

    @SuppressWarnings("unused")//$NON-NLS-1$
    private final String conClassName = "GenericAPIJobOptions";  //$NON-NLS-1$
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(GenericAPIJobOptions.class);

    /** constructors */

    public GenericAPIJobOptions() {
    } // public GenericAPIJobOptions

    public GenericAPIJobOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public GenericAPIJobOptions

    //

    public GenericAPIJobOptions(HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    } // public GenericAPIJobOptions (HashMap JSSettings)

    /** \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
     *
     * \details
     * 
     * @throws Exception
     *
     * @throws Exception - wird ausgelöst, wenn eine mandatory-Option keinen
     *             Wert hat */
    @Override
    // GenericAPIJobOptionsSuperClass
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()
}
