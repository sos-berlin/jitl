package com.sos.jitl.checkhistory.classes;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.sos.hibernate.classes.UtcTimeHelper;

public class HistoryInterval {

    private DateTime from;
    private DateTime to;
    private String utcFrom;
    private String utcTo;
    private DateTimeZone fromZone;

    private String fromTimeZoneString;

    public HistoryInterval() {
        super();
        this.fromZone = DateTimeZone.getDefault();
        this.fromTimeZoneString = fromZone.getID();

    }

    public DateTime getFrom() {
        return from;
    }

    public void setFrom(DateTime from) {
        utcFrom = UtcTimeHelper.convertTimeZonesToString("yyyy-MM-dd'T'HH:mm:ss.SSSZ",fromTimeZoneString, "UTC", from);
        utcFrom = utcFrom.replaceAll("\\+0000", "Z");
        this.from = from;
    }

    public DateTime getTo() {
        return to;
    }

    public void setTo(DateTime to) {
        utcTo = UtcTimeHelper.convertTimeZonesToString("yyyy-MM-dd'T'HH:mm:ss.SSSZ",fromTimeZoneString, "UTC", to);
        utcTo = utcTo.replaceAll("\\+0000", "Z");
        this.to = to;
    }

    public String getUtcTo() {
        if (utcFrom.isEmpty()){
            utcFrom = UtcTimeHelper.convertTimeZonesToString("yyyy-MM-dd'T'HH:mm:ss.SSSZ",fromTimeZoneString, "UTC", new DateTime());
        }
        return utcTo;
    }

    public String getUtcFrom() {
        return utcFrom;
    }

}
