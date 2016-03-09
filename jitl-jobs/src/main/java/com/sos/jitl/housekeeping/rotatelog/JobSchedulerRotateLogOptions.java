package com.sos.jitl.housekeeping.rotatelog;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JobSchedulerRotateLogOptions", description = "Rotate compress and delete log files")
public class JobSchedulerRotateLogOptions extends JobSchedulerRotateLogOptionsSuperClass {

    @SuppressWarnings("unused")
    private final String conClassName = "JobSchedulerRotateLogOptions";
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JobSchedulerRotateLogOptions.class);

    /** constructors */

    public JobSchedulerRotateLogOptions() {
    } // public JobSchedulerRotateLogOptions

    public JobSchedulerRotateLogOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JobSchedulerRotateLogOptions

    //

    public JobSchedulerRotateLogOptions(HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    } // public JobSchedulerRotateLogOptions (HashMap JSSettings)

    /** \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
     *
     * \details
     * 
     * @throws Exception
     *
     * @throws Exception - wird ausgelöst, wenn eine mandatory-Option keinen
     *             Wert hat */
    @Override
    // JobSchedulerRotateLogOptions
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
            compress_file_age.adjust2TimeFormat();
            delete_file_age.adjust2TimeFormat();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()
}
