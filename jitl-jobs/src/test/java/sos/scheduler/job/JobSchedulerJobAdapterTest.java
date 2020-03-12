package sos.scheduler.job;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author KB */
public class JobSchedulerJobAdapterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerJobAdapterTest.class);

    @Test
    public final void testDeleteCurrentNodeNameFromKeys() {
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

}