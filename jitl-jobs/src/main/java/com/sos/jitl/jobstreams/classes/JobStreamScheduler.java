package com.sos.jitl.jobstreams.classes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.sos.jobscheduler.RuntimeResolver;
import com.sos.joc.model.calendar.Period;
import com.sos.joc.model.joe.schedule.RunTime;

import sos.xml.SOSXMLXPath;

public class JobStreamScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobStreamScheduler.class);
    private com.sos.joc.model.plan.RunTime plan;
    private List<Long> listOfStartTimes;
    private String timeZoneId;

    public JobStreamScheduler(String timeZoneId) {
        super();
        this.timeZoneId = timeZoneId;
    }

    public void schedule(Date from, Date to, RunTime runTime, boolean resolve) throws JsonProcessingException, Exception {
        ObjectMapper xmlMapper = new XmlMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
                SerializationFeature.INDENT_OUTPUT, true);
        String fromDate = dateAsString(from);
        String toDate = dateAsString(to);
        SOSXMLXPath xml = new SOSXMLXPath(new StringBuffer(xmlMapper.writeValueAsString(XmlSerializer.serializeAbstractSchedule(runTime))));
        RuntimeResolver r = new RuntimeResolver();
        LOGGER.trace("------->get plan: from " + from +  " to " + to + " timezone: " + timeZoneId);
        plan = r.resolve(xml, fromDate, toDate, timeZoneId);
        if (resolve) {
            PeriodResolver periodResolver = new PeriodResolver();
            for (Period p : plan.getPeriods()) {
                LOGGER.trace("-------> period" + p.getAbsoluteRepeat() + " " + p.getSingleStart());
                periodResolver.addStartTimes(p); 
            }

            listOfStartTimes = periodResolver.getStartTimes();
            listOfStartTimes.sort(null);
        }
    }

    private String dateAsString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateS = formatter.format(date);
        return dateS;
    }

    public com.sos.joc.model.plan.RunTime getPlan() {
        return plan;
    }

    public List<Long> getListOfStartTimes() {
        return listOfStartTimes;
    }

}
