package com.sos.jitl.jobstreams.classes;

import java.util.Date;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.jobstreams.db.DBItemEvent;
import com.sos.jitl.jobstreams.db.DBLayerEvents;
import com.sos.jitl.jobstreams.db.FilterEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sos.util.SOSString;

public class JSEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSEvent.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    DBItemEvent itemEvent;
    private String schedulerId;
    private boolean dbError;

    public DBItemEvent getItemEvent() {
        return itemEvent;
    }

    public String getSchedulerId() {
        return schedulerId;
    }

    public JSEventKey getKey() {
        JSEventKey jsEventKey = new JSEventKey();
        jsEventKey.setSession(itemEvent.getSession());
        String[] s = itemEvent.getEvent().split("\\.");
        if (s.length == 2) {
            jsEventKey.setEvent(s[1]);
            jsEventKey.setJobStream(s[0]);
        }else {
            jsEventKey.setEvent(itemEvent.getEvent());
            jsEventKey.setJobStream(itemEvent.getJobStream());
        }
        jsEventKey.setSchedulerId(schedulerId);
        jsEventKey.setGlobalEvent(itemEvent.getGlobalEvent());
        return jsEventKey;
    }

    public JSEvent() {
        super();
        itemEvent = new DBItemEvent();
    }

    public void setItemEvent(DBItemEvent itemEvent) {
        this.itemEvent = itemEvent;
    }

    public Boolean isGlobalEvent() {
        return itemEvent.getGlobalEvent();
    }

    public Long getId() {
        return itemEvent.getId();
    }

    public String getSession() {
        return itemEvent.getSession();
    }

    public String getEvent() {
        return itemEvent.getEvent();
    }

    public String getJobStream() {
        return itemEvent.getJobStream();
    }

    public Date getCreated() {
        return itemEvent.getCreated();
    }

    public long getOutConditionId() {
        return itemEvent.getOutConditionId();
    }

    public void setCreated(Date created) {
        itemEvent.setCreated(created);
    }

    public void setEvent(String event) {
        itemEvent.setEvent(event);
    }

    public void setGlobalEvent(boolean globalEvent) {
        itemEvent.setGlobalEvent(globalEvent);
    }

    public void setSession(String session) {
        itemEvent.setSession(session);
    }

    public void setJobStream(String jobStream) {
        itemEvent.setJobStream(jobStream);
    }

    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    public void setOutConditionId(Long outConditionId) {
        itemEvent.setOutConditionId(outConditionId);
    }

    public void setJobStreamHistoryId(Long jobStreamHistoryId) {
        itemEvent.setJobStreamHistoryId(jobStreamHistoryId);
    }

    public boolean store(SOSHibernateSession sosHibernateSession) {
        dbError = false;
        DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);
        try {
            sosHibernateSession.beginTransaction();
            dbLayerEvents.store(this);
            sosHibernateSession.commit();
        } catch (SOSHibernateException e) {
            dbError = true;
            try {
                sosHibernateSession.rollback();
            } catch (SOSHibernateException e1) {
                LOGGER.warn("Could not rollback the transaction while storing an event");
            }
            if (isDebugEnabled) {
                LOGGER.debug("Could not store event: " + this.getEvent() + ":" + SOSString.toString(this) + " " + e.getMessage());
            }
        }
        return dbError;
    }

    public boolean deleteEvent(SOSHibernateSession sosHibernateSession) {
        dbError = false;
        DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);

        try {
            sosHibernateSession.beginTransaction();
            FilterEvents filterEvents = new FilterEvents();
            filterEvents.setSchedulerId(this.getSchedulerId());
            filterEvents.setEvent(this.getKey().getEvent());
            filterEvents.setSession(this.getSession());
            filterEvents.setGlobalEvent(this.isGlobalEvent());

            dbLayerEvents.delete(filterEvents);
            sosHibernateSession.commit();

        } catch (SOSHibernateException e) {
            dbError = true;
            LOGGER.error(e.getMessage(), e);
            try {
                sosHibernateSession.rollback();
            } catch (SOSHibernateException e1) {
                LOGGER.warn("Could not rollback the transaction while storing an event");
            }
            if (isDebugEnabled) {
                LOGGER.debug("Could not delete event: " + this.getEvent() + ":" + SOSString.toString(this));
            }
        }
        return dbError;
    }

    public boolean isDbError() {
        return dbError;
    }

    public String toStr() {
        return this.getEvent() + "::" + SOSString.toString(this);
    }

}
