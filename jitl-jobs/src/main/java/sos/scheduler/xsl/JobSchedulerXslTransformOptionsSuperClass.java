package sos.scheduler.xsl;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionOutFileName;

@JSOptionClass(name = "JobSchedulerXslTransformationOptionsSuperClass", description = "JobSchedulerXslTransformationOptionsSuperClass")
public class JobSchedulerXslTransformOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = -4196969125579402246L;
    private final String conClassName = "JobSchedulerXslTransformationOptionsSuperClass";

    @JSOptionDefinition(name = "FileName", description = "", key = "FileName", type = "SOSOptionString", mandatory = true)
    public SOSOptionInFileName FileName = new SOSOptionInFileName(this, conClassName + ".FileName", "", "", "", true);
    public SOSOptionInFileName XMLFileName = (SOSOptionInFileName) FileName.setAlias("xml_file_name");

    public SOSOptionInFileName getFileName() {
        return FileName;
    }

    public void setFileName(final SOSOptionInFileName p_FileName) {
        FileName = p_FileName;
    }

    @JSOptionDefinition(name = "OutputFileName", description = "", key = "OutputFileName", type = "SOSOptionString", mandatory = true)
    public SOSOptionOutFileName OutputFileName = new SOSOptionOutFileName(this, conClassName + ".OutputFileName", "", "", "", true);

    public SOSOptionOutFileName getOutputFileName() {
        return OutputFileName;
    }

    public void setOutputFileName(final SOSOptionOutFileName p_OutputFileName) {
        OutputFileName = p_OutputFileName;
    }

    @JSOptionDefinition(name = "XslFileName", description = "", key = "XslFileName", type = "SOSOptionString", mandatory = true)
    public SOSOptionInFileName XslFileName = new SOSOptionInFileName(this, conClassName + ".XslFileName", "", "", "", true);

    public SOSOptionInFileName getXslFileName() {
        return XslFileName;
    }

    public void setXslFileName(final SOSOptionInFileName p_XslFileName) {
        XslFileName = p_XslFileName;
    }

    public JobSchedulerXslTransformOptionsSuperClass() {
        currentClass = this.getClass();
    }

    public JobSchedulerXslTransformOptionsSuperClass(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerXslTransformOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    @Override
    public void setAllOptions(HashMap<String, String> settings) {
        super.setAllOptions(settings);
    }

    @Override
    public void checkMandatory() throws JSExceptionMandatoryOptionMissing, Exception {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void commandLineArgs(final String[] pstrArgs) {
        super.commandLineArgs(pstrArgs);
        this.setAllOptions(super.getSettings());
    }

}