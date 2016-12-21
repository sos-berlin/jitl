/*
 * Created on 13.10.2008 To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.sos.jitl.eventing.eventhandler;

import java.util.Iterator;
import java.util.LinkedHashSet;

import com.sos.jitl.eventing.evaluate.BooleanExp;

public class SOSEventGroups {

    protected String condition = "";
    protected String group = "";
    protected String event_class = "";
    protected LinkedHashSet<SchedulerEvent> listOfEvents = null;

    public SOSEventGroups(final String group) {
        super();
        this.group = group;
        listOfEvents = new LinkedHashSet<SchedulerEvent>();
    }

    public boolean isActiv(final LinkedHashSet<SchedulerEvent> listOfActiveEvents) {
        boolean erg = false;
        if (condition.isEmpty()) {
            condition = "or";
        }
        Iterator<SchedulerEvent> i = listOfEvents.iterator();
        if ("or".equalsIgnoreCase(condition)) {
            while (i.hasNext() && !erg) {
                SchedulerEvent e = i.next();
                erg = e.isIn(listOfActiveEvents);
            }
        } else {
            if ("and".equalsIgnoreCase(condition)) {
                erg = true;
                while (i.hasNext() && erg) {
                    SchedulerEvent e = (SchedulerEvent) i.next();
                    erg = erg && e.isIn(listOfActiveEvents);
                }
            } else {
                BooleanExp exp = new BooleanExp(condition);
                while (i.hasNext()) {
                    SchedulerEvent e = (SchedulerEvent) i.next();
                    exp.replace(e.getEvent_name(), exp.trueFalse(e.isIn(listOfActiveEvents)));
                }
                erg = exp.evaluateExpression();
            }
        }
        return erg;
    }

    public String getGroup() {
        return group;
    }

    public LinkedHashSet<SchedulerEvent> getListOfEvents() {
        return listOfEvents;
    }

    public String getCondition() {
        return condition;
    }

}