package com.sos.jitl.jobstreams.classes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.exception.SOSInvalidDataException;
import com.sos.joc.model.calendar.Period;

public class PeriodResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeriodResolver.class);
    private List<Long> listOfStartTimes;
    private Map<Long, Period> listOfPeriods;
    private Date now;
    private SimpleDateFormat dateFormat;

    public PeriodResolver() {
        super();
        listOfStartTimes = new ArrayList<Long>();
        listOfPeriods = new HashMap<Long, Period>();
        dateFormat = new SimpleDateFormat("yyyy-M-dd'T'HH:mm:ssX");
    }

    private Date getDate(String date) throws ParseException {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.parse(date);
    }

    private void logPeriod(Period p) {
        LOGGER.info(p.getBegin() + " - " + p.getEnd());
        LOGGER.info("Single Start: " + p.getSingleStart());
        LOGGER.info("Repeat: " + p.getRepeat());
        LOGGER.info("Absolute Repeat: " + p.getAbsoluteRepeat());
    }

    private void add(Long start, Period period) {
        if (now == null) {
            now = new Date();
        }
        Date startDate = new Date(start);
        if (startDate.after(now) || (startDate.compareTo(now) == 0)) {

            Period p = listOfPeriods.get(start);
            if (p == null) {
                listOfPeriods.put(start, period);
            } else {
                LOGGER.info("Overlapping period for start time: " + start);
                logPeriod(p);
                logPeriod(period);
            }
        }
    }

    private void addRepeat(Period period) throws ParseException {

        if (period.getAbsoluteRepeat() != null && !period.getAbsoluteRepeat().isEmpty() && !"00:00:00".equals(period.getAbsoluteRepeat())) {
            Long start = getDate(period.getBegin()).getTime();
            Long end = getDate(period.getEnd()).getTime();

            Date repeat = getDate("2001-01-01T" + period.getAbsoluteRepeat() + "Z");
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(repeat);
            long offset = 1000 * (calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 + calendar.get(Calendar.MINUTE) * 60 + calendar.get(Calendar.SECOND));
            while (offset > 0 && start < end) {
                add(start, period);
                start = start + offset;
            }
        }
    }

    private Period normalizePeriod(Period p) throws SOSInvalidDataException {

        if (p.getBegin() == null || p.getBegin().isEmpty()) {
            p.setBegin("00:00:00");
        }
        if (p.getEnd() == null || p.getEnd().isEmpty()) {
            p.setEnd("24:00:00");
        }

        if (p.getAbsoluteRepeat() == null || p.getAbsoluteRepeat().isEmpty()) {
            p.setAbsoluteRepeat("00:00:00");
        } else {
            String d = p.getAbsoluteRepeat() + ":00:00:00";
            d = d.replaceAll("::", ":").substring(0, 8);
            p.setAbsoluteRepeat(d);
        }

        return p;
    }

    public void addStartTimes(Period period) throws ParseException, SOSInvalidDataException {
        period = normalizePeriod(period);
        if (period.getSingleStart() != null && !period.getSingleStart().isEmpty()) {
            Long start = getDate(period.getSingleStart()).getTime();
            add(start, period);
        } else {
            addRepeat(period);
        }
    }

    public List<Long> getStartTimes() throws ParseException {
        listOfStartTimes = new ArrayList<Long>();
        for (Entry<Long, Period> period : listOfPeriods.entrySet()) {
            listOfStartTimes.add(period.getKey());
        }
        return listOfStartTimes;
    }
}
