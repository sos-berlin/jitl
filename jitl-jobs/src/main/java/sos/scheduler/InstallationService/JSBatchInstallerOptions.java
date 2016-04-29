package sos.scheduler.InstallationService;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JSBatchInstallerOptions", description = "Unattended Batch Installation on remote servers")
public class JSBatchInstallerOptions extends JSBatchInstallerOptionsSuperClass {

    private static final long serialVersionUID = 1L;
    
    public JSBatchInstallerOptions() {
    }

    public JSBatchInstallerOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JSBatchInstallerOptions(HashMap<String, String> JSSettings) throws Exception {
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