package sos.scheduler.InstallationService;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

@JSOptionClass(name = "JSBatchInstallerOptions", description = "Unattended Batch Installation on remote servers")
public class JSBatchInstallerOptions extends JSBatchInstallerOptionsSuperClass {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    /**
	 * 
	 */
    // private static final long serialVersionUID = -4977931410612056714L;
    @SuppressWarnings("unused")//$NON-NLS-1$
    private final String conClassName = "JSBatchInstallerOptions";  //$NON-NLS-1$
    @SuppressWarnings("unused")
    private static Logger logger = null; /*
                                          * Logger.getLogger(JSBatchInstallerOptions
                                          * .class);
                                          */

    /** constructors */

    public JSBatchInstallerOptions() {
    } // public JSBatchInstallerOptions

    public JSBatchInstallerOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JSBatchInstallerOptions

    //

    public JSBatchInstallerOptions(HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
        logger = Logger.getLogger(JSBatchInstallerOptions.class);

    } // public JSBatchInstallerOptions (HashMap JSSettings)

    /** \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
     *
     * \details
     * 
     * @throws Exception
     *
     * @throws Exception - wird ausgelöst, wenn eine mandatory-Option keinen
     *             Wert hat */
    @Override
    // JSBatchInstallerOptionsSuperClass
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()
}
