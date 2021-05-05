package com.sos.jitl.jobstreams.classes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.sos.joc.model.joe.schedule.AbstractSchedule;
import com.sos.joc.model.joe.schedule.Day;
import com.sos.joc.model.joe.schedule.Monthdays;
import com.sos.joc.model.joe.schedule.Period;
import com.sos.joc.model.joe.schedule.Ultimos;
import com.sos.joc.model.joe.schedule.WeekdayOfMonth;
import com.sos.joc.model.joe.schedule.Weekdays;

public class XmlSerializer {

    private static final List<String> falseValues = Arrays.asList("false", "0", "no");
    public static final String xmlHeader = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>" + System.lineSeparator() + System.lineSeparator();

 

    @SuppressWarnings("unchecked")
    public static <T extends AbstractSchedule> T serializeAbstractSchedule(AbstractSchedule runtime) {
        if (runtime.getLetRun() != null && falseValues.contains(runtime.getLetRun())) {
            runtime.setLetRun(null);
        }
        if (runtime.getRunOnce() != null && falseValues.contains(runtime.getRunOnce())) {
            runtime.setRunOnce(null);
        }
        runtime.setPeriods(serializePeriod(runtime.getPeriods()));
        if (runtime.getDates() != null && !runtime.getDates().isEmpty()) {
            runtime.setDates(runtime.getDates().stream().map(item -> {
                item.setPeriods(serializePeriod(item.getPeriods()));
                return item;
            }).collect(Collectors.toList()));
        } else {
            runtime.setDates(null);
        }
        runtime.setWeekdays(serializeWeekdays(runtime.getWeekdays()));
        runtime.setUltimos(serializeUltimos(runtime.getUltimos()));
        runtime.setMonthdays(serializeMonthdays(runtime.getMonthdays()));
        if (runtime.getMonths() != null) {
            runtime.getMonths().stream().map(month -> {
                month.setPeriods(serializePeriod(month.getPeriods()));
                month.setWeekdays(serializeWeekdays(month.getWeekdays()));
                month.setUltimos(serializeUltimos(month.getUltimos()));
                month.setMonthdays(serializeMonthdays(month.getMonthdays()));
                return month;
            }).collect(Collectors.toList());
        }
        if (runtime.getHolidays() != null) {
            if (runtime.getHolidays().getDays() != null && !runtime.getHolidays().getDays().isEmpty()) {
                runtime.getHolidays().setDays(runtime.getHolidays().getDays().stream().map(item -> {
                    item.setPeriods(serializePeriod(item.getPeriods()));
                    return item;
                }).collect(Collectors.toList()));
            } else {
                runtime.getHolidays().setDays(null);
            }
            runtime.getHolidays().setWeekdays(serializeWeekdays(runtime.getHolidays().getWeekdays()));
        }
        if (runtime.getCalendars() != null && runtime.getCalendars().trim().equals("{}")) {
            runtime.setCalendars(null);
        }
        return (T) runtime;
    }

    private static List<Period> serializePeriod(List<Period> periods) {
        if (periods == null || periods.isEmpty()) {
            return null;
        }
        return periods.stream().map(period -> {
            if (period.getLetRun() != null && falseValues.contains(period.getLetRun())) {
                period.setLetRun(null);
            }
            if (period.getRunOnce() != null && falseValues.contains(period.getRunOnce())) {
                period.setRunOnce(null);
            }
            if (period.getWhenHoliday() != null && "suppress".equals(period.getWhenHoliday())) {
                period.setWhenHoliday(null);
            }
            return period;
        }).collect(Collectors.toList());
    }

    private static List<Day> serializeDays(List<Day> days) {
        if (days == null || days.isEmpty()) {
            return null;
        }
        return days.stream().map(item -> {
            item.setPeriods(serializePeriod(item.getPeriods()));
            return item;
        }).collect(Collectors.toList());
    }

    private static Weekdays serializeWeekdays(Weekdays weekdays) {
        if (weekdays == null) {
            return null;
        }
        weekdays.setDays(serializeDays(weekdays.getDays()));
        return weekdays;
    }

    private static Ultimos serializeUltimos(Ultimos ultimos) {
        if (ultimos == null) {
            return null;
        }
        ultimos.setDays(serializeDays(ultimos.getDays()));
        return ultimos;
    }

    private static List<WeekdayOfMonth> serializeWeekdaysOfMonth(List<WeekdayOfMonth> weekdays) {
        if (weekdays == null || weekdays.isEmpty()) {
            return null;
        }
        return weekdays.stream().map(item -> {
            item.setPeriods(serializePeriod(item.getPeriods()));
            return item;
        }).collect(Collectors.toList());
    }

    private static Monthdays serializeMonthdays(Monthdays monthdays) {
        if (monthdays == null) {
            return null;
        }
        monthdays.setDays(serializeDays(monthdays.getDays()));
        monthdays.setWeekdays(serializeWeekdaysOfMonth(monthdays.getWeekdays()));
        return monthdays;
    }

 
  

       
}
