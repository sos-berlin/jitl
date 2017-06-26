package com.sos.jitl.dailyplan.db;

import java.util.Date;

public class DailyPlanInterval {

    private Date from;
    private Date to;

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

}
