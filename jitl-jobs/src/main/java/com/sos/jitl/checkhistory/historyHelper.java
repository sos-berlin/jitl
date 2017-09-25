package com.sos.jitl.checkhistory;

import java.math.BigInteger;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import com.sos.jitl.checkhistory.classes.HistoryInterval;
import sos.util.SOSDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class historyHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(historyHelper.class);

    public String getDuration(LocalDateTime parStart, LocalDateTime parEnd) {
        if (parStart == null || parEnd == null) {
            return "";
        }
        Instant instant = parStart.toInstant(ZoneOffset.UTC);
        Date start = Date.from(instant);
        instant = parEnd.toInstant(ZoneOffset.UTC);
        Date end = Date.from(instant);
        if (start == null || end == null) {
            return "";
        } else {
            Calendar cal_1 = new GregorianCalendar();
            Calendar cal_2 = new GregorianCalendar();
            cal_1.setTime(start);
            cal_2.setTime(end);
            long time = cal_2.getTime().getTime() - cal_1.getTime().getTime();
            time /= 1000;
            long seconds = time % 60;
            time /= 60;
            long minutes = time % 60;
            time /= 60;
            long hours = time % 24;
            time /= 24;
            long days = time;
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, (int) hours);
            calendar.set(Calendar.MINUTE, (int) minutes);
            calendar.set(Calendar.SECOND, (int) seconds);
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            String d = "";
            if (days > 0) {
                d = String.format("%sd " + formatter.format(calendar.getTime()), days);
            } else {
                d = formatter.format(calendar.getTime());
            }
            return d;
        }
    }

    protected String getOrderId(String jobChainAndOrder) {
        return getParameter(jobChainAndOrder);
    }

    protected String getJobChainName(String jobChainAndOrder) {
        return getMethodName(jobChainAndOrder);
    }

    protected String getParameter(String p) {
        p = p.trim();
        String s = "";
        Pattern pattern = Pattern.compile("^.*\\(([^\\)]*)\\)$", Pattern.DOTALL + Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(p);
        if (matcher.find()) {
            s = matcher.group(1).trim();
        }
        return s;
    }

    protected String getMethodName(String p) {
        p = p.trim();
        String s = p;
        Pattern pattern = Pattern.compile("^([^\\(]*)\\(.*\\)$", Pattern.DOTALL + Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(p);
        if (matcher.find()) {
            s = matcher.group(1).trim();
        }
        return s.trim();
    }

    protected boolean isAfter(LocalDateTime timeToTest, String time) {
        if (time.length() == 8) {
            time = "0:" + time;
        }
        if (timeToTest == null) {
            return false;
        }
        JobSchedulerCheckHistoryOptions options = new JobSchedulerCheckHistoryOptions();
        options.start_time.setValue(time);
        ZonedDateTime zdt = ZonedDateTime.of(timeToTest, ZoneId.systemDefault());
        GregorianCalendar cal = GregorianCalendar.from(zdt);
        DateTime limit = new DateTime(options.start_time.getDateObject());
        DateTime ended = new DateTime(cal.getTime());
        return limit.toLocalDateTime().isBefore(ended.toLocalDateTime());
    }

    protected boolean isBefore(LocalDateTime timeToTest, String time) {
        if (time.length() == 8) {
            time = "0:" + time;
        }
        if (timeToTest == null) {
            return false;
        }
        JobSchedulerCheckHistoryOptions options = new JobSchedulerCheckHistoryOptions();
        options.start_time.setValue(time);
        ZonedDateTime zdt = ZonedDateTime.of(timeToTest, ZoneId.systemDefault());
        GregorianCalendar cal = GregorianCalendar.from(zdt);
        DateTime limit = new DateTime(options.start_time.getDateObject());
        DateTime ended = new DateTime(cal.getTime());
        return limit.toLocalDateTime().isAfter(ended.toLocalDateTime());
    }
    
    protected boolean isToday(LocalDateTime d) {
        LocalDateTime today = LocalDateTime.now();
        if (d == null) {
            return false;
        } else {
            return today.getDayOfYear() == d.getDayOfYear();
        }
    }

    protected String getParameter(String defaultValue, String p) {
        String param = getParameter(p);
        if (param.isEmpty()) {
            param = defaultValue;
        }
        return param;
    }

    protected int big2int(BigInteger b) {
        if (b == null) {
            return -1;
        } else {
            return b.intValue();
        }
    }

    public boolean isInTimeLimit(String timeLimit, String endTime) {
        if ("".equals(timeLimit)) {
            return true;
        }
        String localTimeLimit = timeLimit;
        if (!timeLimit.contains("..")) {
            localTimeLimit = ".." + localTimeLimit;
        }
        String from = localTimeLimit.replaceAll("^(.*)\\.\\..*$", "$1");
        String to = localTimeLimit.replaceAll("^.*\\.\\.(.*)$", "$1");
        if ("".equals(from)) {
            from = "00:00:00";
        }
        if (from.length() == 8) {
            from = "0:" + from;
        }
        if (to.length() == 8) {
            to = "0:" + to;
        }
        JobSchedulerCheckHistoryOptions options = new JobSchedulerCheckHistoryOptions();
        options.start_time.setValue(from);
        options.end_time.setValue(to);
        if ("".equals(to)) {
            DateTime fromDate = new DateTime(options.start_time.getDateObject());
            DateTime ended = new DateTime(endTime);
            DateTime toDate = ended;
            return (ended.toLocalDateTime().isEqual(toDate.toLocalDateTime()) || ended.toLocalDateTime().isBefore(toDate.toLocalDateTime()))
                    && (ended.toLocalDateTime().isEqual(fromDate.toLocalDateTime()) || ended.toLocalDateTime().isAfter(fromDate.toLocalDateTime()));
        } else {
            DateTime fromDate = new DateTime(options.start_time.getDateObject());
            DateTime ended = new DateTime(endTime);
            DateTime toDate = new DateTime(options.end_time.getDateObject());
            return (ended.toLocalDateTime().isEqual(toDate.toLocalDateTime()) || ended.toLocalDateTime().isBefore(toDate.toLocalDateTime()))
                    && (ended.toLocalDateTime().isEqual(fromDate.toLocalDateTime()) || ended.toLocalDateTime().isAfter(fromDate.toLocalDateTime()));
        }
    }
    protected LocalDateTime getDateFromString(String inDateTime) throws Exception {
        LocalDateTime dateResult = null;
        Date date = null;
        if (inDateTime != null) {
            if (inDateTime.endsWith("Z")) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'H:mm:ssZ");
                DateTime dateTime = dateTimeFormatter.parseDateTime(inDateTime.replaceFirst("Z", "+00:00"));
                date = dateTime.toDate();
            } else {
                date = SOSDate.getDate(inDateTime, SOSDate.dateTimeFormat);
            }
            dateResult = LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
            return dateResult;
        } else {
            return null;
        }
    }

    private JobSchedulerCheckHistoryOptions getIntervalFromString(String timeLimit) {

        String localTimeLimit = timeLimit;
        if (!timeLimit.contains("..")) {
            localTimeLimit = ".." + localTimeLimit;
        }
        String from = localTimeLimit.replaceAll("^(.*)\\.\\..*$", "$1");
        String to = localTimeLimit.replaceAll("^.*\\.\\.(.*)$", "$1");
        if ("".equals(from)) {
            String[] s = timeLimit.split(":");
            from = "00:00:00";
            if (s.length == 4){
                from = s[0] + ":" + from;
            }
        }
        if (from.length() == 8) {
            from = "0:" + from;
        }
        if (to.length() == 8) {
            to = "0:" + to;
        }

        

        JobSchedulerCheckHistoryOptions options = new JobSchedulerCheckHistoryOptions();
        options.start_time.setValue(from);
        options.end_time.setValue(to);
        return options;
    }
    

    public String normalizePath(String relativePath, String jobName) {
        if (!jobName.startsWith("/") && relativePath != null && !relativePath.isEmpty()) {
            String s = jobName;
            jobName =  Paths.get(relativePath, jobName).toString();
            LOGGER.debug(String.format("Changed job name from %s to %s", s, jobName));
        }
        jobName = ("/" + jobName.replaceAll("\\\\","/").trim());
        jobName = jobName.replaceAll("//+", "/");
        jobName = jobName.replaceFirst("/$", "");
        return jobName;
    }

    public HistoryInterval getUTCIntervalFromTimeLimit(String timeLimit) {
        HistoryInterval jobHistoryInterval = new HistoryInterval();
        JobSchedulerCheckHistoryOptions options = getIntervalFromString(timeLimit);

        DateTime fromDate = new DateTime(options.start_time.getDateObject());
        DateTime toDate = new DateTime(options.end_time.getDateObject());

        jobHistoryInterval.setFrom(fromDate);
        jobHistoryInterval.setTo(toDate);
        
        return jobHistoryInterval;
    }

 
}