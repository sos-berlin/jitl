package sos.scheduler.misc;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import org.apache.log4j.Logger;
import org.junit.*;

import static org.junit.Assert.assertEquals;

public class CopyJob2OrderParameterOptionsJUnitTest extends JSToolBox {

    private final String conClassName = "CopyJob2OrderParameterOptionsJUnitTest"; //$NON-NLS-1$
    @SuppressWarnings("unused")//$NON-NLS-1$
    private static Logger logger = Logger.getLogger(CopyJob2OrderParameterOptionsJUnitTest.class);
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
        objOptions.operation.Value("++copy++");
        assertEquals("", objOptions.operation.Value(), "++copy++");

    }

} // public class CopyJob2OrderParameterOptionsJUnitTest