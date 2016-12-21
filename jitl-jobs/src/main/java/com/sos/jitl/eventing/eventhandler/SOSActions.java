/*
 * Created on 13.10.2008 To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.sos.jitl.eventing.eventhandler;

import java.util.Iterator;
import java.util.LinkedHashSet;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.jitl.eventing.evaluate.BooleanExp;

public class SOSActions {

    protected String name = "";
    protected String condition = "";
    protected LinkedHashSet<SOSEventGroups> listOfEventGroups = null;
    protected LinkedHashSet<SOSEventCommand> listOfCommands = null;
    protected NodeList commandNodes = null;
    protected Node commands = null;

    public SOSActions(final String name) {
        super();
        this.name = name;
        listOfEventGroups = new LinkedHashSet<SOSEventGroups>();
        listOfCommands = new LinkedHashSet<SOSEventCommand>();
    }

    public boolean isActive(final LinkedHashSet<SchedulerEvent> listOfActiveEvents) {
        String tmp = condition;
        if (condition.isEmpty() || "or".equalsIgnoreCase(condition)) {
            condition = "";
            Iterator<SOSEventGroups> i = listOfEventGroups.iterator();
            while (i.hasNext()) {
                SOSEventGroups evg = i.next();
                condition += evg.group + " or ";
            }
            condition += " false";
        }
        if ("and".equalsIgnoreCase(condition)) {
            condition = "";
            Iterator<SOSEventGroups> i = listOfEventGroups.iterator();
            while (i.hasNext()) {
                SOSEventGroups evg = i.next();
                condition += evg.group + " and ";
            }
            condition += " true";
        }
        BooleanExp exp = new BooleanExp(condition);
        Iterator<SOSEventGroups> i = listOfEventGroups.iterator();
        while (i.hasNext()) {
            SOSEventGroups evg = i.next();
            exp.replace(evg.group, exp.trueFalse(evg.isActiv(listOfActiveEvents)));
        }
        condition = tmp;
        return exp.evaluateExpression();
    }

    public LinkedHashSet<SOSEventCommand> getListOfCommands() {
        return listOfCommands;
    }

    public String getName() {
        return name;
    }

    public String getCondition() {
        return condition;
    }

    public LinkedHashSet<SOSEventGroups> getListOfEventGroups() {
        return listOfEventGroups;
    }

    public Node getCommands() {
        return commands;
    }

    public NodeList getCommandNodes() {
        return commandNodes;
    }

}