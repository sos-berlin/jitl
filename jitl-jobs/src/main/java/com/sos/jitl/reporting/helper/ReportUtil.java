package com.sos.jitl.reporting.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormat;

import com.sos.jitl.reporting.db.DBLayer;

import sos.util.SOSDate;
import sos.util.SOSString;

public class ReportUtil {

    public static String normalizePath(String path) {
        return path.replace('\\', '/');
    }

    public static String normalizeDbItemPath(String s) {
        return ("/" + s).replaceAll("//+", "/");
    }

    public static String normalizeFilePath2SchedulerPath(File file, int rootPathLen) throws IOException {
        return normalizePath(file.getCanonicalPath().substring(rootPathLen));
    }

    public static String getNameFromPath(String path, EConfigFileExtensions extension) {
        return path.substring(0, path.lastIndexOf(extension.extension()));
    }

    public static String getBasenameFromName(String name) {
        int li = name.lastIndexOf("/");
        return li > -1 ? name.substring(li + 1) : name;
    }

    public static String getFolderFromName(String name) {
        int li = name.lastIndexOf("/");
        return li > -1 ? name.substring(0, li) : name;
    }

    public static int resolveAge2Minutes(String age) throws Exception {
        if (SOSString.isEmpty(age)) {
            throw new Exception("age is empty");
        }
        int minutes = 0;
        String[] arr = age.trim().split(" ");
        for (String s : arr) {
            s = s.trim().toLowerCase();
            if (!SOSString.isEmpty(s)) {
                String sub = s;
                try {
                    if (s.endsWith("w")) {
                        sub = s.substring(0, s.length() - 1);
                        minutes += 60 * 24 * 7 * Integer.parseInt(sub);
                    } else if (s.endsWith("d")) {
                        sub = s.substring(0, s.length() - 1);
                        minutes += 60 * 24 * Integer.parseInt(sub);
                    } else if (s.endsWith("h")) {
                        sub = s.substring(0, s.length() - 1);
                        minutes += 60 * Integer.parseInt(sub);
                    } else if (s.endsWith("m")) {
                        sub = s.substring(0, s.length() - 1);
                        minutes += Integer.parseInt(sub);
                    } else {
                        minutes += Integer.parseInt(sub);
                    }
                } catch (Exception ex) {
                    throw new Exception(String.format("invalid integer value = %s (%s) : %s", sub, s, ex.toString()));
                }
            }
        }
        return minutes;
    }

    public static String getDuration(DateTime startTime, DateTime endTime) {
        Duration duration = new Duration(startTime, endTime);
        Period period = duration.toPeriod().normalizedStandard(PeriodType.time());
        return PeriodFormat.wordBased(Locale.ENGLISH).print(period);
    }

    public static int getBatchSize(int[] arr) {
        int count = 0;
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                count += arr[i];
            }
        }
        return count;
    }

    public static Date getCurrentDateTime() {
        return new DateTime(DateTimeZone.UTC).toLocalDateTime().toDate();
    }

    public static Long getDateAsMinutes(Date d) {
        return d.getTime() / 1000 / 60;
    }

    public static Long getDateAsSeconds(Date d) {
        return d.getTime() / 1000;
    }

    public static String getDateAsString(Date d) {
        if (d == null) {
            return "";
        }
        try {
            return SOSDate.getTimeAsString(d, DBLayer.DATETIME_FORMAT);
        } catch (Throwable t) {
            return "";
        }
    }

    public static Date getDateFromString(String d) throws Exception {
        return SOSDate.getTime(d, DBLayer.DATETIME_FORMAT);
    }

    public static Date getDateTimeMinusMinutes(Date date, Long minutes) {
        return new DateTime(date).minusMinutes(minutes.intValue()).toDate();
    }

    public static Long getDayOfMonth(DateTime dt) {
        return new Long(dt.getDayOfMonth());
    }

    public static Long getWeekOfWeekyear(DateTime dt) {
        return new Long(dt.getWeekOfWeekyear());
    }

    public static Long getMonthOfYear(DateTime dt) {
        return new Long(dt.getMonthOfYear());
    }

    public static Long getQuarterOfYear(DateTime dt) {
        return new Long(((dt.getMonthOfYear() - 1) / 3) + 1);
    }

    public static Long getYear(DateTime dt) {
        return new Long(dt.getYear());
    }

    public static Date convertFileTime2UTC(FileTime fileTime) throws Exception {
        if (fileTime == null) {
            return null;
        }
        return new DateTime(fileTime.toMillis(), DateTimeZone.UTC).toLocalDateTime().toDate();
    }

    public static Date convertFileTime2Local(FileTime fileTime) throws Exception {
        if (fileTime == null) {
            return null;
        }
        return new DateTime(fileTime.toMillis()).toLocalDateTime().toDate();
    }

    public static Instant eventId2Instant(Long eventId) {
        return Instant.ofEpochMilli(eventId / 1000);
    }

    public static Instant timestamp2Instant(Long timestamp) {
        return Instant.ofEpochMilli(timestamp);
    }

    public static Date getEventIdAsDate(Long eventId) {
        return eventId == null ? null : Date.from(eventId2Instant(eventId));
    }

    public static Date getTimestampAsDate(Long timestamp) {
        return timestamp == null ? null : Date.from(timestamp2Instant(timestamp));
    }

}