package com.sos.jitl.jobstreams;

import java.util.Calendar;

import com.sos.hibernate.classes.ClassList;
import com.sos.jitl.eventhandler.handler.EventHandlerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;  

public class Constants {
	private static final Logger LOGGER = LoggerFactory.getLogger(Constants.class);

    public static enum OutConditionEventCommand {
        create, delete
    }

    public static final String DBItemEvent = com.sos.jitl.jobstreams.db.DBItemEvent.class.getSimpleName();
    public static final String EVENTS_TABLE_SEQUENCE = "JSTREAM_EVENTS_ID_SEQ";
    public static final String EVENTS_TABLE = "JSTREAM_EVENTS";

    public static final String DBItemConsumedInCondition = com.sos.jitl.jobstreams.db.DBItemConsumedInCondition.class.getSimpleName();
    public static final String CONSUMED_IN_CONDITIONS_TABLE_SEQUENCE = "JSTREAM_CONSUMED_INCOND_ID_SEQ";
    public static final String CONSUMED_IN_CONDITIONS_TABLE = "JSTREAM_CONSUMED_IN_CONDITIONS";

    public static final String DBItemInCondition = com.sos.jitl.jobstreams.db.DBItemInCondition.class.getSimpleName();
    public static final String IN_CONDITIONS_TABLE_SEQUENCE = "JSTREAM_IN_COND_ID_SEQ";
    public static final String IN_CONDITIONS_TABLE = "JSTREAM_IN_CONDITIONS";

    public static final String DBItemOutConditionEvent = com.sos.jitl.jobstreams.db.DBItemOutConditionEvent.class.getSimpleName();
    public static final String OUT_CONDITION_EVENTS_TABLE_SEQUENCE = "JSTREAM_OUT_COND_EV_ID_SEQ";
    public static final String OUT_CONDITION_EVENTS_TABLE = "JSTREAM_OUT_CONDITION_EVENTS";

    public static final String DBItemOutCondition = com.sos.jitl.jobstreams.db.DBItemOutCondition.class.getSimpleName();
    public static final String OUT_CONDITIONS_TABLE_SEQUENCE = "JSTREAM_OUT_COND_ID_SEQ";
    public static final String OUT_CONDITIONS_TABLE = "JSTREAM_OUT_CONDITIONS";

    public static final String DBItemInConditionCommand = com.sos.jitl.jobstreams.db.DBItemInConditionCommand.class.getSimpleName();
    public static final String IN_CONDITION_COMMANDS_TABLE_SEQUENCE = "JSTREAM_IN_COND_CMD_ID_SEQ";
    public static final String IN_CONDITION_COMMANDS_TABLE = "JSTREAM_IN_CONDITION_COMMANDS";

    public static EventHandlerSettings settings = null;
    public static String baseUrl;   
    public static String periodBegin;

    public static Calendar getSessionCalendar() {
        if (periodBegin == null) {
            periodBegin = "00:00";
        }
        Calendar calendar = Calendar.getInstance();
        String[] period = periodBegin.split(":");
        int hours = 0;
        int minutes = 0;
        if (period.length == 1) {
            try {
                hours = Integer.parseInt(period[0]);
            } catch (NumberFormatException e) {
                LOGGER.warn("Wrong time format for sos.jobstream_period_begin: " + periodBegin);
                hours = 0;
                minutes = 0;
                periodBegin = "00:00";
            }
        }
        if (period.length == 2) {
            try {
                hours = Integer.parseInt(period[0]);
                minutes = Integer.parseInt(period[1]);
            } catch (NumberFormatException e) {
                LOGGER.warn("Wrong time format for sos.jobstream_period_begin: " + periodBegin);
                hours = 0;
                minutes = 0;
                periodBegin = "00:00";
            }
        }
       
        calendar.add(Calendar.HOUR_OF_DAY, hours*-1);
        calendar.add(Calendar.MINUTE, minutes*-1);
        calendar.add(Calendar.DATE,1);

        return calendar;
    }

    public static String getSession() {
        Calendar calendar = getSessionCalendar();
 
        int month = calendar.get(Calendar.MONTH) + 1;
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        LOGGER.debug("Period starts at  " + periodBegin + " (UTC)");
        String result = String.valueOf(month) + "." + String.valueOf(dayOfMonth);
        LOGGER.debug("period session is: " + result);

        return result;
    }

    public static ClassList getConditionsClassMapping() {
        ClassList cl = new ClassList();
        cl.add(com.sos.jitl.jobstreams.db.DBItemOutCondition.class);
        cl.add(com.sos.jitl.jobstreams.db.DBItemOutConditionEvent.class);
        cl.add(com.sos.jitl.jobstreams.db.DBItemInCondition.class);
        cl.add(com.sos.jitl.jobstreams.db.DBItemInConditionCommand.class);
        cl.add(com.sos.jitl.jobstreams.db.DBItemConsumedInCondition.class);
        cl.add(com.sos.jitl.jobstreams.db.DBItemEvent.class);
        return cl;
    }
}
