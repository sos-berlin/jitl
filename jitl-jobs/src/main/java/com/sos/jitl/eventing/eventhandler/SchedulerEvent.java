package com.sos.jitl.eventing.eventhandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class SchedulerEvent {

    private String event_name;
    private String event_title;
    private String event_class;
    private String event_id;
    private String job_name;
    private String job_chain;
    private String order_id;
    private String exit_code;
    private String created;
    private String expires;
    private String remote_scheduler_host;
    private String remote_scheduler_port;
    private String scheduler_id;
    private String condition = "";
    private String comment = "";

    public String getEvent_title() {
        return event_title;
    }

    public String getEvent_class() {
        return event_class;
    }

    public String getEvent_id() {
        return event_id;
    }

    public String getJob_name() {
        return job_name;
    }

    public String getJob_chain() {
        return job_chain;
    }

    public String getOrder_id() {
        return order_id;
    }

    public String getExit_code() {
        return exit_code;
    }

    public String getCreated() {
        return created;
    }

    public String getExpires() {
        return expires;
    }

    public String getRemote_scheduler_host() {
        return remote_scheduler_host;
    }

    public String getRemote_scheduler_port() {
        return remote_scheduler_port;
    }

    public String getScheduler_id() {
        return scheduler_id;
    }

    private String getText(final Node n) {
        if (n != null) {
            return n.getNodeValue();
        } else {
            return "";
        }
    }

    private String getTextDefault(final String d, final Node n) {
        if (n != null) {
            return n.getNodeValue();
        } else {
            return d;
        }
    }

    public void setProperties(NamedNodeMap attr) {
        event_class = getTextDefault("", attr.getNamedItem("event_class"));
        event_id = getText(attr.getNamedItem("event_id"));
        job_name = getText(attr.getNamedItem("job_name"));
        job_chain = getText(attr.getNamedItem("job_chain"));
        order_id = getText(attr.getNamedItem("order_id"));
        exit_code = getText(attr.getNamedItem("exit_code"));
        created = getText(attr.getNamedItem("created"));
        expires = getText(attr.getNamedItem("expires"));
        comment = getText(attr.getNamedItem("comment"));
        scheduler_id = getText(attr.getNamedItem("scheduler_id"));
        remote_scheduler_host = getText(attr.getNamedItem("remote_scheduler_host"));
        remote_scheduler_port = getText(attr.getNamedItem("remote_scheduler_port"));
    }

    public void setEventClassIfBlank(String eClass) {
        if ("".equals(event_class)) {
            event_class = eClass;
        }
    }

    public String getCondition() {
        return condition;
    }

    private HashMap<String, String> properties() {
        HashMap<String, String> attr = new HashMap<String, String>();
        attr.put("event_title", event_title);
        attr.put("event_class", event_class);
        attr.put("event_id", event_id);
        attr.put("job_name", job_name);
        attr.put("job_chain", job_chain);
        attr.put("order_id", order_id);
        attr.put("exit_code", exit_code);
        attr.put("remote_scheduler_host", remote_scheduler_host);
        attr.put("remote_scheduler_port", remote_scheduler_port);
        attr.put("scheduler_id", scheduler_id);
        return attr;
    }

    public boolean isEqual(SchedulerEvent eActive) {
        boolean erg = true;
        Iterator<String> iProperties = properties().keySet().iterator();
        while (iProperties.hasNext()) {
            String trigger = iProperties.next().toString();
            if (!"".equals(properties().get(trigger)) && eActive.properties().get(trigger) != null && !"expires".equalsIgnoreCase(trigger) 
                    && !"created".equalsIgnoreCase(trigger) && !eActive.properties().get(trigger).equals(properties().get(trigger))) {
                erg = false;
            }
        }
        return erg;
    }

    public boolean isIn(LinkedHashSet listOfActiveEvents) {
        boolean erg = false;
        Iterator i = listOfActiveEvents.iterator();
        while (i.hasNext() && !erg) {
            if (this.isEqual((SchedulerEvent) i.next())) {
                erg = true;
            }
        }
        return erg;
    }

    public void setEvent_name(String event_name) {
        this.event_name = event_name;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getComment() {
        return comment;
    }

    public String getEvent_name() {
        if ("".equals(this.event_name)) {
            if ("".equals(this.event_class)) {
                return this.event_id;
            } else {
                return this.event_class + "." + this.event_id;
            }
        } else {
            return event_name;
        }
    }

    public void setEvent_class(String event_class) {
        this.event_class = event_class;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

}