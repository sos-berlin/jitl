package sos.scheduler.job;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

/** @author KB */
public class JobSchedulerJobAdapterTest {

    private static final Logger LOGGER = Logger.getLogger(JobSchedulerJobAdapterTest.class);

    @Test
    public final void testDeleteCurrentNodeNameFromKeys() {
        JobSchedulerJobAdapter jobschedulerAdapter = new JobSchedulerJobAdapter();
        HashMap<String, String> parameterSet = new HashMap<String, String>();

        parameterSet.put("order_state_for_test/testparam1", "value_of_test_param1");
        HashMap<String, String> newParameterSet = jobschedulerAdapter.testDeleteCurrentNodeNameFromKeys(parameterSet);
        assertEquals("testDeleteCurrentNodeNameFromKeys", "value_of_test_param1", newParameterSet.get("testparam1"));

        parameterSet.put("job::job_name_for_test.order_state_for_test/testparam2", "value_of_test_param2");
        newParameterSet = jobschedulerAdapter.testDeleteCurrentNodeNameFromKeys(parameterSet);
        assertEquals("testDeleteCurrentNodeNameFromKeys", "value_of_test_param2", newParameterSet.get("testparam2"));
        
        parameterSet.put("job::job_name_for_test/testparam3", "value_of_test_param3");
        newParameterSet = jobschedulerAdapter.testDeleteCurrentNodeNameFromKeys(parameterSet);
        assertEquals("testDeleteCurrentNodeNameFromKeys", "value_of_test_param3", newParameterSet.get("testparam3"));
    }

    @Test
    public void testStringSubstitutor() {
        Map<String, String> valuesMap = new HashMap<String, String>();
        valuesMap.put("animal", "quick brown fox");
        valuesMap.put("target", "lazy dog");
        String templateString = "The ${animal} jumped over the ${target}.";
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        LOGGER.info(sub.replace(templateString));
    }

    @Test
    public void testStringSubstitutorPercent() {
        Map<String, String> valuesMap = new HashMap<String, String>();
        valuesMap.put("animal", "quick brown fox");
        valuesMap.put("target", "hallo$p");
        String templateString = "The %animal% jumped over the %target%. Missing number ${missing.numner:-47110815}. %java.version%, %os.name%";
        StrSubstitutor sub = new StrSubstitutor(valuesMap, "%", "%");
        LOGGER.info(sub.replace(templateString));
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
        LOGGER.info(sub.replace(templateString));
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
        LOGGER.info(StrSubstitutor.replaceSystemProperties(templateString));
    } 

    private HashMap<String, String> fillHash(int count) {
        HashMap<String, String> h = new HashMap<String, String>();
        for (int i = 0; i < count; i++) {
            h.put("name_" + i, "value_" + i);
        }
        return h;
    }


}