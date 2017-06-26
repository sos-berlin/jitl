package com.sos.jitl.dailyplan.db;

import java.util.Date;

import com.sos.scheduler.model.answers.Calendar;

public class DailyPlanCalendarItem {

    private Date from;
    private Date to;
    private Calendar calendar;

    public DailyPlanCalendarItem(Date from, Date to, Calendar calendar) {
        super();
        this.from = from;
        this.to = to;
        this.calendar = calendar;
    }
    
    public Date getFrom() {
        return from;
    }

    public Date getTo() {
        return to;
    }

    public Calendar getCalendar() {
        return calendar;
    }


}
