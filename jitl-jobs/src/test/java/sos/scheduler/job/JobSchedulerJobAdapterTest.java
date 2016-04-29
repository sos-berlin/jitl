package sos.scheduler.job;

import static org.junit.Assert.*;

import org.junit.*;
import sos.scheduler.misc.ParameterSubstitutor;
import java.util.HashMap;
 
public class JobSchedulerJobAdapterTest {

 
    public JobSchedulerJobAdapterTest() {
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
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testSpooler_init() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testSpooler_process() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testJobSchedulerJobAdapter() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testDeleteCurrentNodeNameFromKeys() {
        JobSchedulerJobAdapter objJA = new JobSchedulerJobAdapter();
        HashMap<String, String> objHM = new HashMap<String, String>();
        objHM.put("node1/scheduler_param_file", "c:/test/1.txt");
        objHM.put("node1/file_path", "%scheduler_param_file%");
        objHM.put("node1/local_dir", "%scheduler_param_file%");
        HashMap<String, String> objHM2 = objJA.DeleteCurrentNodeNameFromKeys(objHM);
        System.out.println(objHM2.toString());
        Assert.assertEquals("string must be substituted", "%scheduler_param_file%", objHM2.get("local_dir"));
    }

    @Test
    public void testStringSubstitutor() {
        ParameterSubstitutor parameterSubstitutor = new ParameterSubstitutor();
        parameterSubstitutor.addKey("animal", "quick brown fox");
        parameterSubstitutor.addKey("targeT", "lazy dog");
        String templateString = "The ${Animal} jumped over the ${Target}.";
        String resolvedString = parameterSubstitutor.replace(templateString);
        System.out.println(resolvedString);
    }

    @Test
    public void testStringSubstitutorPercent() {
        ParameterSubstitutor parameterSubstitutor = new ParameterSubstitutor("%", "%");
        parameterSubstitutor.addKey("animal", "quick brown fox");
        parameterSubstitutor.addKey("target", "hallo$p");
        String templateString = "The %animal% jumped over the %target%. Missing number ${missing.numner:-47110815}";
        String resolvedString = parameterSubstitutor.replace(templateString);
        System.out.println(resolvedString);
    }

    @Test
    public void testStringSubstitutorRecursive() {
        ParameterSubstitutor parameterSubstitutor = new ParameterSubstitutor("%", "%");
        parameterSubstitutor.addKey("animal", "quick brown fox");
        parameterSubstitutor.addKey("animal-1", "quick brown fox");
        parameterSubstitutor.addKey("animal-2", "slow yellow fox");
        parameterSubstitutor.addKey("number", "2");
        parameterSubstitutor.addKey("target", "hallo%number%");

        String templateString = "The %animal% jumped over the %target%.";
        String resolvedString = parameterSubstitutor.replace(templateString);
        System.out.println(resolvedString);
    }

   

    @Test
    public void testReplaceSystemProperties() {
        ParameterSubstitutor parameterSubstitutor = new ParameterSubstitutor();

        parameterSubstitutor.addKey("animal-1", "quick brown fox");
        parameterSubstitutor.addKey("animal-2", "slow yellow fox");
        parameterSubstitutor.addKey("number", "2");
        parameterSubstitutor.addKey("target", "hallo$p");

        String templateString = " ${java.version}, ${os.name}";
        String resolvedString = parameterSubstitutor.replaceSystemProperties(templateString);
        System.out.println(resolvedString);
    }

   
    private HashMap<String, String> fillHash(int count) {
        HashMap<String, String> h = new HashMap<String, String>();
        for (int i = 0; i < count; i++) {
            h.put("name_" + i, "value_" + i);
        }
        return h;
    }

    @Test
    public final void testReplaceVars() {

        long time = -System.currentTimeMillis();

        JobSchedulerJobAdapter objJA = new JobSchedulerJobAdapter();

        objJA.schedulerParameters.put("scheduler_param_file", "c:/test/1.txt");
        objJA.schedulerParameters.put("file_path", "${Scheduler_Param_File}");
        
        for (String key : objJA.schedulerParameters.keySet()) {
            String value = objJA.schedulerParameters.get(key);
            if (value != null) {
                String replacedValue = objJA.replaceSchedulerVars(value);

                if (replacedValue.equalsIgnoreCase(value) == false) {
                    objJA.schedulerParameters.put(key, replacedValue);
                }
            }
        }
        String strT = objJA.schedulerParameters.get("file_path");

        assertEquals("string must be substituted", "c:/test/1.txt", strT);
        System.out.println(time + System.currentTimeMillis() + "ms");

    }

    @Test
    public final void testMyReplaceVars() {
        long time = -System.currentTimeMillis();

        JobSchedulerJobAdapter objJA = new JobSchedulerJobAdapter();

        objJA.schedulerParameters.put("scheduler_param_file", "c:/test/1.txt");
        objJA.schedulerParameters.put("file2", "c:/test/2.txt");
        objJA.schedulerParameters.put("file_path1", "${scheduler_param_file}");
        objJA.schedulerParameters.put("file_path4", "${file2}");
        objJA.schedulerParameters.put("file_path7", "${file2}");

        for (String key : objJA.schedulerParameters.keySet()) {
            String value = objJA.schedulerParameters.get(key);
            if (value != null) {
                String replacedValue = objJA.replaceSchedulerVars(value);

                if (replacedValue.equalsIgnoreCase(value) == false) {
                    objJA.schedulerParameters.put(key, replacedValue);
                }
            }
        }
        String strT = objJA.schedulerParameters.get("file_path1");
        Assert.assertEquals("string must be substituted", "c:/test/1.txt", strT);

        strT = objJA.schedulerParameters.get("file_path4");
        Assert.assertEquals("string must be substituted", "c:/test/2.txt", strT);

        strT = objJA.schedulerParameters.get("file_path7");
        Assert.assertEquals("string must be substituted", "c:/test/2.txt", strT);


        System.out.println(time + System.currentTimeMillis() + "ms");

    }

    @Test
    public final void testReplaceNonExistentVars() {

        JobSchedulerJobAdapter objJA = new JobSchedulerJobAdapter();

        objJA.schedulerParameters.put("scheduler_param_file", "c:/test/1.txt");
        String textToReplace = "%Y%m%d - ${scheduler_param_file} ${not_valid}";
        String textExpected = "%Y%m%d - c:/test/1.txt ${not_valid}";
        objJA.schedulerParameters.put("date_time", "%Y%m%d");
        String strT = objJA.replaceSchedulerVars(textToReplace);
        Assert.assertEquals("string should not be different", textExpected, strT);

    }

    @Test
    public final void testGetJobOrOrderParameters() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testGetParameters() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testSetParameters() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testSetJSParamStringString() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testSetJSParamStringStringBuffer() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testReplaceSchedulerVars() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testMyReplaceAll() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testStackTrace2String() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testGetJSJobUtilities() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testSetJSJobUtilites() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testGetCurrentNodeName() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testGetSpoolerObject() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testExecuteXML() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testIsOrderJob() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testSetNextNodeState() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testIsJobchain() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testSetOrderParameter() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testIsNotNull() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testIsNull() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testHasOrderParameters() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testSignalSuccess() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testSignalFailure() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testIsNotEmpty() {
        // fail("Not yet implemented")
    }

    @Test
    public final void testMapToProperties() {
        // fail("Not yet implemented")
    }
}
