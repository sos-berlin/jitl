package com.sos.jitl.httppost;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

/** \class JobSchedulerHttpPostJobOptionsJUnitTest - Post files via HTTP
 *
 * \brief */

public class JobSchedulerHttpPostJobOptionsJUnitTest extends JSToolBox {

    private final String conClassName = "JobSchedulerHttpPostJobOptionsJUnitTest"; //$NON-NLS-1$
    @SuppressWarnings("unused")//$NON-NLS-1$
    private static Logger logger = Logger.getLogger(JobSchedulerHttpPostJobOptionsJUnitTest.class);
    private JobSchedulerHttpPostJob objE = null;

    protected JobSchedulerHttpPostJobOptions objOptions = null;

    public JobSchedulerHttpPostJobOptionsJUnitTest() {
        //
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        objE = new JobSchedulerHttpPostJob();
        objE.registerMessageListener(this);
        objOptions = objE.getOptions();
        objOptions.registerMessageListener(this);

        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    @After
    public void tearDown() throws Exception {
    }

    /** \brief testcontent_type :
     * 
     * \details The content type and character set encoding are retrieved from
     * the content of xml and html input files by default. This parameter,
     * however, overwrites any content type given in input files. */
    @Test
    public void testcontent_type() {  // SOSOptionString
        objOptions.content_type.setValue("++----++");
        assertEquals("", objOptions.content_type.getValue(), "++----++");

    }

    /** \brief testencoding :
     * 
     * \details This parameter provides the character set encoding value. By
     * default the encoding of xml and html input files is retrieved from the
     * content of these files. */
    @Test
    public void testencoding() {  // SOSOptionString
        objOptions.encoding.setValue("++----++");
        assertEquals("", objOptions.encoding.getValue(), "++----++");

    }

    /** \brief testinput :
     * 
     * \details This parameter contains a valid directory name or file name. If
     * a directory name is specified, then all files contained in this directory
     * will be posted in indeterminate order. If a file name is given, then only
     * the file with this name will be posted. */
    @Test
    public void testinput() {  // SOSOptionString
        objOptions.input.setValue("++----++");
        assertEquals("", objOptions.input.getValue(), "++----++");

    }

    /** \brief testinput_filespec :
     * 
     * \details A regular expression may be specified as a filter for the input
     * files in a directory. */
    @Test
    public void testinput_filespec() {  // SOSOptionString
        objOptions.input_filespec.setValue("++^(.*)$++");
        assertEquals("", objOptions.input_filespec.getValue(), "++^(.*)$++");

    }

    /** \brief testoutput :
     * 
     * \details This parameter contains a valid directory name or file name to
     * store the output from the URL to which the original input file(s) were
     * posted. If a directory name is given then the output file names will
     * match the input file names. If a file name is given then all output will
     * be stored in this file. */
    @Test
    public void testoutput() {  // SOSOptionString
        objOptions.output.setValue("++----++");
        assertEquals("", objOptions.output.getValue(), "++----++");

    }

    /** \brief testurl :
     * 
     * \details This parameter specifies the URL to which the given files are to
     * be posted. */
    @Test
    public void testurl() {  // SOSOptionString
        objOptions.url.setValue("++----++");
        assertEquals("", objOptions.url.getValue(), "++----++");

    }

} // public class JobSchedulerHttpPostJobOptionsJUnitTest