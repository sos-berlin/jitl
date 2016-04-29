package sos.scheduler.file;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

@JSOptionClass(name = "JSExistsFileOptions", description = "check wether a file exist")
public class JSExistsFileOptions extends JSExistsFileOptionsSuperClass {

    private static final long serialVersionUID = 1731085195875420660L;

    public JSExistsFileOptions() {
    }

    public JSExistsFileOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }
    
    public JSExistsFileOptions(HashMap<String, String> JSSettings) throws Exception {
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