package com.sos.jitl.jobstreams.db;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.query.Query;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernate;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.jobstreams.classes.JSEvent;

public class DBLayerEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerEvents.class);
    private static final String DBItemEvents = DBItemEvent.class.getSimpleName();
    private static final String DBItemOutCondition = DBItemOutCondition.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerEvents(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemEvent getEventsDbItem(final Long id) throws SOSHibernateException {
        return (DBItemEvent) sosHibernateSession.get(DBItemEvent.class, id);
    }

    public FilterEvents resetFilter() {
        FilterEvents filter = new FilterEvents();
        filter.setEvent("");
        filter.setSession("");
        filter.setJobStream("");
        return filter;
    }

    public String getContextListSql(List<String> list) {
        StringBuilder sql = new StringBuilder();
        sql.append("e.session in (");
        for (String s : list) {
            sql.append("'" + s + "',");
        }
        String s = sql.toString();
        s = s.substring(0, s.length() - 1);
        s = s + ")";

        return " (" + s + ") ";
    }

    private String getWhere(FilterEvents filter) {
        String where = "";
        String and = "";

        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            if (filter.getIncludingGlobalEvent()) {
                where += and + " (o.schedulerId = :schedulerId or e.globalEvent=true)";
            } else {
                where += and + " o.schedulerId = :schedulerId";
            }
            and = " and ";
        }

        if (filter.getSession() != null && !"".equals(filter.getSession())) {
            where += and + " e.session = :session";
            and = " and ";
        }

        if (filter.getEvent() != null && !"".equals(filter.getEvent())) {
            where += and + " e.event = :event";
            and = " and ";
        }

        if (filter.getGlobalEvent() != null) {
            where += and + " e.globalEvent = :globalEvent";
            and = " and ";
        }

        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            where += and + " o.job = :job";
            and = " and ";
        }

        if (filter.getJobStream() != null && !"".equals(filter.getJobStream())) {
            where += and + " e.jobStream = :jobStream";
            and = " and ";
        }

        if (filter.getOutConditionId() != null) {
            where += and + " e.outConditionId = :outConditionId";
            and = " and ";
        }

        if (filter.getJobStreamHistoryId() != null) {
            where += and + " e.jobStreamHistoryId = :jobStreamHistoryId";
            and = " and ";
        }
        if (filter.getListOfSession() != null && filter.getListOfSession().size() > 0) {
            where += and + getContextListSql(filter.getListOfSession());
            and = " and ";
        }
        if (filter.getJoin() != null && !"".equals(filter.getJoin())) {
            where += and + filter.getJoin();
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = "where " + where;
        }
        return where;
    }

    private String getDeleteWhere(FilterEvents filter) {
        String where = "";
        String and = "";

        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            where += and + " schedulerId = :schedulerId";
            and = " and ";
        }

        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            where += and + " job = :job";
            and = " and ";
        }

        if (filter.getJobStream() != null && !"".equals(filter.getJobStream())) {
            where += and + " jobStream = :jobStream";
            and = " and ";
        }

        if (filter.getJobStreamHistoryId() != null) {
            where += and + " e.jobStreamHistoryId = :jobStreamHistoryId";
            and = " and ";
        }

        where = " where " + where;
        return where;
    }

    private <T> Query<T> bindParameters(FilterEvents filter, Query<T> query) {
        if (filter.getEvent() != null && !"".equals(filter.getEvent())) {
            query.setParameter("event", filter.getEvent());
        }
        if (filter.getGlobalEvent() != null) {
            query.setParameter("globalEvent", filter.getGlobalEvent());
        }
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getSession() != null && !"".equals(filter.getSession())) {
            query.setParameter("session", filter.getSession());
        }
        if (filter.getJobStream() != null && !"".equals(filter.getJobStream())) {
            query.setParameter("jobStream", filter.getJobStream());
        }
        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            query.setParameter("job", filter.getJob());
        }
        if (filter.getOutConditionId() != null) {
            query.setParameter("outConditionId", filter.getOutConditionId());
        }
        if (filter.getJobStreamHistoryId() != null) {
            query.setParameter("jobStreamHistoryId", filter.getJobStreamHistoryId());
        }
        return query;
    }

    public List<DBItemOutConditionWithEvent> executeGetEventsList(FilterEvents filter, final int limit) throws SOSHibernateException {
        filter.setJoin("e.outConditionId=o.id");
        String q = "select e.id as eventId,e.outConditionId as outConditionId,"
                + "e.jobStreamHistoryId as jobStreamHistoryId,e.session as session,e.event as event, e.created as created, "
                + "e.jobStream as jobStream,e.globalEvent as globalEvent,o.schedulerId as jobSchedulerId " + " from " + DBItemEvents + " e, "
                + DBItemOutCondition + " o " + getWhere(filter);
        Query<DBItemOutConditionWithEvent> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        query.setResultTransformer(Transformers.aliasToBean(DBItemOutConditionWithEvent.class));

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public List<DBItemOutConditionWithEvent> getEventsList(FilterEvents filter, final int limit) throws SOSHibernateException {
        filter.setJoin("e.outConditionId=o.id");

        if (filter.getListOfSession() != null) {
            List<DBItemOutConditionWithEvent> resultList = new ArrayList<DBItemOutConditionWithEvent>();
            int size = filter.getListOfSession().size();
            if (size > SOSHibernate.LIMIT_IN_CLAUSE) {
                ArrayList<String> copy = (ArrayList<String>) filter.getListOfSession().stream().collect(Collectors.toList());
                for (int i = 0; i < size; i += SOSHibernate.LIMIT_IN_CLAUSE) {
                    if (size > i + SOSHibernate.LIMIT_IN_CLAUSE) {
                        filter.setListOfSession(copy.subList(i, (i + SOSHibernate.LIMIT_IN_CLAUSE)));
                    } else {
                        filter.setListOfSession(copy.subList(i, size));
                    }
                    resultList.addAll(executeGetEventsList(filter, limit));
                }
                return resultList;
            } else {
                return executeGetEventsList(filter, limit);
            }
        } else {
            return executeGetEventsList(filter, limit);
        }
     }

    public Integer delete(FilterEvents filter) throws SOSHibernateException {

        int row = 0;
        String hql = "";
        String schedulerId = filter.getSchedulerId();

        if (filter.getGlobalEvent() != null && filter.getGlobalEvent()) {
            filter.setSchedulerId("");
            hql = "delete from " + DBItemEvents + " e " + getWhere(filter);
        } else {
            if (filter.getSchedulerId() == null || filter.getSchedulerId().isEmpty()) {
                hql = "delete from " + DBItemEvents + " e " + getWhere(filter);
            } else {
                filter.setSchedulerId("");
                String select = "select id from " + DBItemOutCondition + " where schedulerId = :schedulerId";
                hql = "delete from " + DBItemEvents + " e " + getWhere(filter) + " and e.outConditionId in ( " + select + ")";
                filter.setSchedulerId(schedulerId);
            }
        }
        Query<DBItemEvent> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);

        row = sosHibernateSession.executeUpdate(query);
        return row;

    }

    public void store(JSEvent event) throws SOSHibernateException {
        FilterEvents filterEvents = new FilterEvents();
        filterEvents.setEvent(event.getEvent());
        filterEvents.setJobStream(event.getJobStream());
        filterEvents.setOutConditionId(event.getOutConditionId());
        filterEvents.setSession(event.getSession());
        filterEvents.setSchedulerId(event.getSchedulerId());
        filterEvents.setGlobalEvent(event.isGlobalEvent());
        delete(filterEvents);
        sosHibernateSession.save(event.getItemEvent());
    }

    public int updateEvents(Long oldId, Long newId) throws SOSHibernateException {
        String hql = "update " + DBItemEvents + " set outConditionId=" + newId + " where outConditionId=:oldId";
        int row = 0;
        Query<DBItemEvent> query = sosHibernateSession.createQuery(hql);
        query.setParameter("oldId", oldId);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public int updateEventsWithJobStream(String oldJobStream, String newJobStream) throws SOSHibernateException {
        String hql = "update " + DBItemEvents + " set jobStream='" + newJobStream + "' where jobStream=:oldJobStream";
        int row = 0;
        Query<DBItemJobStreamHistory> query = sosHibernateSession.createQuery(hql);
        query.setParameter("oldJobStream", oldJobStream);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

}