package com.sos.jitl.dailyplan.db;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.UtcTimeHelper;

public class DailyPlanInterval {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanInterval.class);
    private Date from;
    private Date to;
    private Date convertedFrom;
    private Date convertedTo;

    public DailyPlanInterval(Date from, Date to) {
        super();
        this.from = from;
        this.to = to;
    }

    public Date getFrom() {
        return from;
    }

    public Date getTo() {
        return to;
    }

    public Date getConvertedFrom() {
        if (convertedFrom == null){
            convert2Utc();
        }
        return convertedFrom;
    }

    public Date getConvertedTo() {
        if (convertedTo == null){
            convert2Utc();
        }
        return convertedTo;
    }

    private void convert2Utc() {
        String toTimeZoneString = "UTC";

        String fromTimeZoneString = DateTimeZone.getDefault().getID();
        LOGGER.debug("fromTimeZone:" + fromTimeZoneString);
        LOGGER.debug("toTimeZone:" + toTimeZoneString);
        LOGGER.debug("intervall from:" + this.getFrom());
        LOGGER.debug("intervall to:" + this.getTo());
        DateTime utcFrom = new DateTime(this.getFrom()).withZone(DateTimeZone.UTC);
        utcFrom = utcFrom.withZone(DateTimeZone.UTC);
        DateTime utcTo = new DateTime(this.getTo()).withZone(DateTimeZone.UTC);
        utcTo = utcTo.withZone(DateTimeZone.UTC);

        convertedFrom = UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, utcFrom);
        convertedTo = UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, new DateTime(utcTo));
        LOGGER.debug("converted intervall from:" + convertedFrom);
        LOGGER.debug("convertet intervall to:" + convertedTo);
    }

}
