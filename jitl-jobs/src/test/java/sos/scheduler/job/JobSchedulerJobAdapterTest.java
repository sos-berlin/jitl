package sos.scheduler.job;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

/** @author KB */
public class JobSchedulerJobAdapterTest {

    private static final Logger LOGGER = Logger.getLogger(JobSchedulerJobAdapterTest.class);

    public JobSchedulerJobAdapterTest() {
        //
    }

    @Test
    public final void testDeleteCurrentNodeNameFromKeys() {
        JobSchedulerJobAdapter objJA = new JobSchedulerJobAdapter();
        HashMap<String, String> objHM = new HashMap<String, String>();
        objHM.put("node1/scheduler_param_file", "c:/test/1.txt");
        objHM.put("node1/file_path", "%scheduler_param_file%");
        objHM.put("node1/local_dir", "%scheduler_param_file%");
        HashMap<String, String> objHM2 = objJA.DeleteCurrentNodeNameFromKeys(objHM);
        LOGGER.info(objHM2.toString());
        Assert.assertEquals("string must be substituted", "%scheduler_param_file%", objHM2.get("local_dir"));
    }

    @Test
    public void testStringSubstitutor() {
        Map<String, String> valuesMap = new HashMap<String, String>();
        valuesMap.put("animal", "quick brown fox");
        valuesMap.put("target", "lazy dog");
        String templateString = "The ${animal} jumped over the ${target}.";
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        String resolvedString = sub.replace(templateString);
        LOGGER.info(resolvedString);
    }

    @Test
    public void testStringSubstitutorPercent() {
        Map<String, String> valuesMap = new HashMap<String, String>();
        valuesMap.put("animal", "quick brown fox");
        valuesMap.put("target", "hallo$p");
        String templateString = "The %animal% jumped over the %target%. Missing number ${missing.numner:-47110815}. %java.version%, %os.name%";
        StrSubstitutor sub = new StrSubstitutor(valuesMap, "%", "%");
        String resolvedString = sub.replace(templateString);
        LOGGER.info(resolvedString);
    }

    @Test
    public void testStringSubstitutorRecursive() {
        Map<String, String> valuesMap = new HashMap<String, String>();
        valuesMap.put("animal", "quick brown fox");
        valuesMap.put("animal-1", "quick brown fox");
        valuesMap.put("animal-2", "slow yellow fox");
        valuesMap.put("number", "2");
        valuesMap.put("target", "hallo$p");
        String templateString = "The %animal% jumped over the %target%.";
        StrSubstitutor sub = new StrSubstitutor(valuesMap, "%", "%");
        String resolvedString = sub.replace(templateString);
        LOGGER.info(resolvedString);
    }

    @Test
    public void testStringSubstitutorRecursive2() {
        Map<String, String> valuesMap = new HashMap<String, String>();
        valuesMap.put("animal-1", "quick brown fox");
        valuesMap.put("animal-2", "slow yellow fox");
        valuesMap.put("number", "2");
        valuesMap.put("target", "hallo$p");
        String templateString = "The ${animal-${number}} jumped over the ${target}. ${java.version], ${os.name}";
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        sub.setEnableSubstitutionInVariables(true);
        String resolvedString = sub.replace(StrSubstitutor.replaceSystemProperties(templateString));
        LOGGER.info(resolvedString);
        valuesMap.put("number", "1");
        resolvedString = sub.replace(templateString);
        LOGGER.info(resolvedString);
    }

    @Test
    public void testReplaceSystemProperties() {
        Map<String, String> valuesMap = new HashMap<String, String>();
        valuesMap.put("animal-1", "quick brown fox");
        valuesMap.put("animal-2", "slow yellow fox");
        valuesMap.put("number", "2");
        valuesMap.put("target", "hallo$p");
        String templateString = " ${java.version], ${os.name}";
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        String resolvedString = StrSubstitutor.replaceSystemProperties(templateString);
        LOGGER.info(resolvedString);
    }

    @Test
    public final void testGetSchedulerParameterAsProperties() {
        JobSchedulerJobAdapter objJA = new JobSchedulerJobAdapter();
        HashMap<String, String> objHM = new HashMap<String, String>();
        objHM.put("scheduler_param_file", "c:/test/1.txt");
        objHM.put("file_path", "%scheduler_param_file%");
        objHM.put("local_dir", "%scheduler_param_file%");
        objHM.put("user", "hallo$p");
        objHM.put("withDollar", "%user%");
        HashMap<String, String> objHM2 = objJA.getSchedulerParameterAsProperties(objHM);
        LOGGER.info(objHM2.toString());
        Assert.assertEquals("string must be substituted", "c:/test/1.txt", objHM2.get("local_dir"));
        Assert.assertEquals("string must be substituted", "c:/test/1.txt", objHM2.get("localdir"));
        Assert.assertEquals("withDollar", "hallo$p", objHM2.get("withDollar"));
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public final void testGetSchedulerParameterAsProperties2() {
        JobSchedulerJobAdapter objJA = new JobSchedulerJobAdapter();
        HashMap objHM = new HashMap();
        objHM.put("scheduler_param_file", "c:/test/1.txt");
        objHM.put("file_path", "%scheduler_param_file%");
        objHM.put("local_dir", "%scheduler_param_file%");
        objHM.put("int_var", 4711);
        String val = "";
        String valInt = "";
        Set<Map.Entry<String, String>> set = objHM.entrySet();
        for (Map.Entry<String, String> entry : set) {
            String key = entry.getKey();
            Object objO = entry.getValue();
            if (objO instanceof String) {
                val = entry.getValue().toString();
            }
            if (objO instanceof Integer) {
                Integer intI = (Integer) objO;
                valInt = intI.toString();
            }
            String strR = objJA.replaceVars(objHM, key, val);
            if (strR.equalsIgnoreCase(val) == false) {
                objHM.put(key, strR);
            }
        }
        Assert.assertEquals("string must be substituted", "c:/test/1.txt", objHM.get("local_dir"));
        Assert.assertEquals("Integer value", 0, valInt);
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
        LOGGER.info("Expensive Method Call....");
        HashMap<String, String> objHM = fillHash(200);
        objHM.put("scheduler_param_file", "c:/test/1.txt");
        objHM.put("file_path", "%scheduler_param_file%");
        JobSchedulerJobAdapter objJA = new JobSchedulerJobAdapter();
        for (String key : objHM.keySet()) {
            String value = objHM.get(key);
            if (value != null) {
                String replacedValue = objJA.replaceVars(objHM, key, value);
                if (!replacedValue.equalsIgnoreCase(value)) {
                    objHM.put(key, replacedValue);
                }
            }
        }
        String strT = objHM.get("file_path");
        Assert.assertEquals("string must be substituted", "c:/test/1.txt", strT);
        LOGGER.info(time + System.currentTimeMillis() + "ms");
    }

    @Test
    public final void testMyReplaceVars() {
        long time = -System.currentTimeMillis();
        LOGGER.info("Expensive Method Call....");
        HashMap<String, String> objHM = fillHash(200);
        objHM.put("scheduler_param_file", "c:/test/1.txt");
        objHM.put("file2", "c:/test/2.txt");
        objHM.put("file_path1", "%scheduler_param_file%");
        objHM.put("file_path2", "${scheduler_param_file}");
        objHM.put("file_path3", "§{scheduler_param_file}");
        objHM.put("file_path4", "%file2%");
        objHM.put("file_path5", "${file2}");
        objHM.put("file_path6", "§{file2}");
        objHM.put("file_path7", "%scheduler_param_file2%");
        objHM.put("file_path8", "${scheduler_param_file2}");
        objHM.put("file_path9", "§{scheduler_param_file2}");
        JobSchedulerJobAdapter objJA = new JobSchedulerJobAdapter();
        for (String key : objHM.keySet()) {
            String value = objHM.get(key);
            if (value != null) {
                String replacedValue = objJA.replaceSchedulerVarsInString(objHM, value);
                if (!replacedValue.equalsIgnoreCase(value)) {
                    objHM.put(key, replacedValue);
                }
            }
        }
        String strT = objHM.get("file_path1");
        Assert.assertEquals("string must be substituted", "c:/test/1.txt", strT);
        strT = objHM.get("file_path2");
        Assert.assertEquals("string must be substituted", "c:/test/1.txt", strT);
        strT = objHM.get("file_path3");
        Assert.assertEquals("string must be substituted", "c:/test/1.txt", strT);
        strT = objHM.get("file_path4");
        Assert.assertEquals("string must be substituted", "c:/test/2.txt", strT);
        strT = objHM.get("file_path5");
        Assert.assertEquals("string must be substituted", "c:/test/2.txt", strT);
        strT = objHM.get("file_path6");
        Assert.assertEquals("string must be substituted", "c:/test/2.txt", strT);
        strT = objHM.get("file_path7");
        Assert.assertEquals("string must be substituted", "c:/test/2.txt", strT);
        strT = objHM.get("file_path8");
        Assert.assertEquals("string must be substituted", "c:/test/2.txt", strT);
        strT = objHM.get("file_path9");
        Assert.assertEquals("string must be substituted", "c:/test/2.txt", strT);
        LOGGER.info(time + System.currentTimeMillis() + "ms");
    }

    @Test
    public final void testReplaceNonExistentVars() {
        HashMap<String, String> objHM = new HashMap<String, String>();
        objHM.put("scheduler_param_file", "c:\\test\\1.txt");
        String textToReplace = "%Y%m%d - %scheduler_param_file% %not_valid%";
        String textExpected = "%Y%m%d - c:/test/1.txt %not_valid%";
        objHM.put("date_time", "%Y%m%d");
        JobSchedulerJobAdapter objJA = new JobSchedulerJobAdapter();
        String strT = objJA.replaceVars(objHM, "scheduler_param_file", textToReplace);
        Assert.assertEquals("string should not be different", textExpected, strT);
    }

}