package com.sos.jitl.jobstreams.db;

import java.util.Date;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.joc.model.jobstreams.OutCondition;
import com.sos.joc.model.jobstreams.OutConditionEvent;

public class DBLayerOutConditionEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerOutConditionEvents.class);
    private static final String DBItemOutConditionEvent = DBItemOutConditionEvent.class.getSimpleName();
    private static final String DBItemOutCondition = DBItemOutCondition.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerOutConditionEvents(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemOutConditionEvent getOutConditionEventdDbItem(final Long id) throws Exception {
        return (DBItemOutConditionEvent) sosHibernateSession.get(DBItemOutConditionEvent.class, id);
    }

    public FilterOutConditionEvents resetFilter() {
        FilterOutConditionEvents filter = new FilterOutConditionEvents();
        filter.setEvent("");
        return filter;
    }

    private String getWhere(FilterOutConditionEvents filter) {
        String where = "";
        String and = "";

        if (filter.getEvent() != null && !"".equals(filter.getEvent())) {
            where += and + " event = :event";
            and = " and ";
        }

        if (filter.getJobStream() != null && !"".equals(filter.getJobStream())) {
            where += and + " jobStream = :jobstream";
            and = " and ";
        }

        if (filter.getFolder() != null && !"".equals(filter.getFolder())) {
            where += and + " folder = :folder";
            and = " and ";
        }

        if (filter.getCommand() != null && !"".equals(filter.getCommand())) {
            where += and + " command = :command";
            and = " and ";
        }

        if (filter.getOutConditionId() != null) {
            where += and + " outConditionId = :outConditionId";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = " where " + where;
        }
        return where;
    }
    

   

    private <T> Query<T> bindParameters(FilterOutConditionEvents filter, Query<T> query) {
        if (filter.getCommand() != null && !"".equals(filter.getCommand())) {
            query.setParameter("command", filter.getCommand());
        }
        if (filter.getEvent() != null && !"".equals(filter.getEvent())) {
            query.setParameter("event", filter.getEvent());
        }
        if (filter.getJobStream() != null && !"".equals(filter.getJobStream())) {
            query.setParameter("jobstream", filter.getJobStream());
        }
        if (filter.getFolder() != null && !"".equals(filter.getFolder())) {
            query.setParameter("folder", filter.getFolder());
        }
        if (filter.getOutConditionId() != null) {
            query.setParameter("outConditionId", filter.getOutConditionId());
        }
        if (filter.getJob() != null) {
            query.setParameter("job", filter.getJob());
        }

        return query;

    }

    public int delete(FilterOutConditionEvents filterConditionEvents) throws SOSHibernateException {
        String hql = "delete from " + DBItemOutConditionEvent + " i " + getWhere(filterConditionEvents);
        int row = 0;
        Query<DBItemOutConditionEvent> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filterConditionEvents, query);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public void deleteInsert(DBItemOutCondition dbItemOutCondition, OutCondition outCondition) throws SOSHibernateException {
        FilterOutConditionEvents filterOutConditionEvents = new FilterOutConditionEvents();
        filterOutConditionEvents.setOutConditionId(outCondition.getId());
        if (outCondition.getId() != null) {
            delete(filterOutConditionEvents);
        }
        for (OutConditionEvent outConditionEvent : outCondition.getOutconditionEvents()) {
            DBItemOutConditionEvent dbItemOutConditionEvent = new DBItemOutConditionEvent();
            dbItemOutConditionEvent.setOutConditionId(dbItemOutCondition.getId());
            dbItemOutConditionEvent.setEvent(outConditionEvent.getEvent());
            dbItemOutConditionEvent.setCommand(outConditionEvent.getCommand());
            if (outConditionEvent.getGlobalEvent() == null) {
                outConditionEvent.setGlobalEvent(false);
            }
            dbItemOutConditionEvent.setGlobalEvent(outConditionEvent.getGlobalEvent());
            dbItemOutConditionEvent.setCreated(new Date());

            sosHibernateSession.save(dbItemOutConditionEvent);
        }

    }

    public int deleteByJobstream(FilterOutConditionEvents filterOutConditionEvents) throws SOSHibernateException {
        FilterOutConditionEvents filter = new FilterOutConditionEvents();
        filter.setJobStream(filterOutConditionEvents.getJobStream());
        filter.setFolder(filterOutConditionEvents.getFolder());
        String select = "select id from " + DBItemOutCondition + getWhere(filter);
        String hql = "delete from " + DBItemOutConditionEvent + " where outConditionId in ( " + select + ")";
        Query<DBItemOutConditionEvent> query = sosHibernateSession.createQuery(hql);
        bindParameters(filter, query);
        int row = sosHibernateSession.executeUpdate(query);
        return row;

    }

}