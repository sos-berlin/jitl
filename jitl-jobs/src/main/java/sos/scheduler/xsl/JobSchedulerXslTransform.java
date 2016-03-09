package sos.scheduler.xsl;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_107;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_110;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_111;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.JSHelper.io.Files.JSXMLFile;

/** @author KB */
public class JobSchedulerXslTransform extends JSJobUtilitiesClass<JobSchedulerXslTransformOptions> {

    protected HashMap<String, String> hsmParameters = null;
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerXslTransform.class);

    public JobSchedulerXslTransform() {
        super(new JobSchedulerXslTransformOptions());
    }

    @Override
    public JobSchedulerXslTransformOptions getOptions() {
        if (objOptions == null) {
            objOptions = new JobSchedulerXslTransformOptions();
        }
        return objOptions;
    }

    public JobSchedulerXslTransform Execute() throws Exception {
        final String methodName = "JobSchedulerXslTransform::Execute";
        LOGGER.debug(JSJ_I_110.get(methodName));
        try {
            getOptions().CheckMandatory();
            LOGGER.debug(getOptions().dirtyString());
            JSXMLFile objXMLFile = new JSXMLFile(getOptions().FileName.Value());
            if (getOptions().XslFileName.IsEmpty() == true) {
                LOGGER.info("no xslt-file specified. copy xml file only");
                String strXML = objXMLFile.getContent();
                JSFile outFile = new JSFile(getOptions().OutputFileName.Value());
                outFile.setCharSet4OutputFile("UTF-8");
                outFile.Write(strXML);
                outFile.close();
            } else {
                objXMLFile.setParameters(hsmParameters);
                objXMLFile.Transform(new File(getOptions().XslFileName.Value()), new File(getOptions().OutputFileName.Value()));
            }
        } catch (Exception e) {
            throw new JobSchedulerException(JSJ_F_107.get(methodName) + ": " + e.getMessage(), e);
        }
        JSJ_I_111.toLog(methodName);
        return this;
    }

    public void init() {
        doInitialize();
    }

    public void setParameters(final HashMap<String, String> pobjHshMap) {
        hsmParameters = pobjHshMap;
    }

    private void doInitialize() {
        // doInitialize
    }

}