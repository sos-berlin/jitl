package com.sos.jitl.eventing.db;

import java.util.ArrayList;

public class SchedulerEventsCollection extends ArrayList<SchedulerEventDBItem> {

    private static final long serialVersionUID = -3518670127890478966L;

    public String asString() {
        return toString();
    }

    public boolean hasElements() {
        return !isEmpty();
    }

}
