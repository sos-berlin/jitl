package sos.scheduler.misc;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

public class CopyJob2OrderParameterOptionsJUnitTest extends JSToolBox {

    private final String conClassName = "CopyJob2OrderParameterOptionsJUnitTest"; //$NON-NLS-1$
    private CopyJob2OrderParameter objE = null;

    protected CopyJob2OrderParameterOptions objOptions = null;

    public CopyJob2OrderParameterOptionsJUnitTest() {
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
        objE = new CopyJob2OrderParameter();
        objE.registerMessageListener(this);
        objOptions = objE.Options();
        objOptions.registerMessageListener(this);

        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    @After
    public void tearDown() throws Exception {
    }

    /** \brief testoperation :
     * 
     * \details */
    @Test
    public void testoperation() {  // SOSOptionString
        objOptions.operation.setValue("++copy++");
        assertEquals("", objOptions.operation.getValue(), "++copy++");

    }

} // public class CopyJob2OrderParameterOptionsJUnitTest