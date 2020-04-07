package com.sos.jitl.jobstreams.db;

import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.joc.model.common.NameValuePair;
import com.sos.joc.model.jobstreams.JobStreamJob;
import com.sos.joc.model.jobstreams.JobStreamStarter;
import com.sos.joc.model.jobstreams.JobStreamStarters;

public class DBLayerJobStreamStarters {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerJobStreamStarters.class);
    private static final String DBItemJobStreamStarter = DBItemJobStreamStarter.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerJobStreamStarters(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemJobStreamStarter getJobStreamStartersDbItem(final Long id) throws SOSHibernateException {
        return (DBItemJobStreamStarter) sosHibernateSession.get(DBItemJobStreamStarter.class, id);
    }

    public FilterJobStreamStarters resetFilter() {
        FilterJobStreamStarters filter = new FilterJobStreamStarters();
        filter.setStatus("");
        return filter;
    }

    private String getWhere(FilterJobStreamStarters filter) {
        String where = "1=1";
        String and = " and ";

        if (filter.getStatus() != null && !"".equals(filter.getStatus())) {
            where += and + " status = :status";
            and = " and ";
        }

        if (filter.getId() != null) {
            where += and + " id  = :id";
            and = " and ";
        }
        if (filter.getJobStreamId() != null) {
            where += and + " jobStream  = :jobStream";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterJobStreamStarters filter, Query<T> query) {
        if (filter.getId() != null) {
            query.setParameter("id", filter.getId());
        }

        if (filter.getStatus() != null && !"".equals(filter.getStatus())) {
            query.setParameter("status", filter.getStatus());
        }
        if (filter.getJobStreamId() != null) {
            query.setParameter("jobStream", filter.getJobStreamId());
        }

        return query;
    }

    public List<DBItemJobStreamStarter> getJobStreamStartersList(FilterJobStreamStarters filter, final int limit) throws SOSHibernateException {
        String q = "  from " + DBItemJobStreamStarter + getWhere(filter);

        Query<DBItemJobStreamStarter> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public Integer delete(FilterJobStreamStarters filter) throws SOSHibernateException {

        int row = 0;
        String hql = "";

        hql = "delete from " + DBItemJobStreamStarter + getWhere(filter);
        Query<DBItemJobStreamStarter> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public void save(DBItemJobStreamStarter jsJobStreamStarter) throws SOSHibernateException {
        sosHibernateSession.save(jsJobStreamStarter);
    }

    public Long store(DBItemJobStreamStarter dbItemJobStreamStarter) throws SOSHibernateException {
        FilterJobStreamStarters filterJobStreamStarters = new FilterJobStreamStarters();
        filterJobStreamStarters.setId(dbItemJobStreamStarter.getJobStream());
        delete(filterJobStreamStarters);
        sosHibernateSession.save(dbItemJobStreamStarter);
        return dbItemJobStreamStarter.getId();
    }

    public void deleteInsert(JobStreamStarters jobStreamStarters) throws SOSHibernateException, JsonProcessingException {
        DBLayerJobStreamParameters dbLayerJobStreamParameters = new DBLayerJobStreamParameters(sosHibernateSession);
        DBLayerJobStreamsStarterJobs dbLayerJobStreamsStarterJobs = new DBLayerJobStreamsStarterJobs(sosHibernateSession);
        FilterJobStreamStarters filterJobStreamStarters = new FilterJobStreamStarters();
        filterJobStreamStarters.setJobStreamId(jobStreamStarters.getJobStream());

        List<DBItemJobStreamStarter> lStarters = getJobStreamStartersList(filterJobStreamStarters, 0);

        for (DBItemJobStreamStarter dbItemJobStreamStarter : lStarters) {
            FilterJobStreamStarterJobs filterJobStreamStarterJobs = new FilterJobStreamStarterJobs();
            filterJobStreamStarterJobs.setJobStreamStarter(dbItemJobStreamStarter.getId());
            dbLayerJobStreamsStarterJobs.delete(filterJobStreamStarterJobs);
        }

        for (DBItemJobStreamStarter dbItemJobStreamStarter : lStarters) {
            FilterJobStreamParameters filterJobStreamParameters = new FilterJobStreamParameters();
            filterJobStreamParameters.setJobStreamStarterId(dbItemJobStreamStarter.getId());
            dbLayerJobStreamParameters.delete(filterJobStreamParameters);
        }

        this.delete(filterJobStreamStarters);

        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        for (JobStreamStarter jobStreamStarter : jobStreamStarters.getJobstreamStarters()) {

            DBItemJobStreamStarter dbItemJobStreamStarter = new DBItemJobStreamStarter();
            dbItemJobStreamStarter.setCreated(new Date());
            dbItemJobStreamStarter.setJobStream(jobStreamStarters.getJobStream());
            dbItemJobStreamStarter.setTitle(jobStreamStarter.getTitle());
            dbItemJobStreamStarter.setState(jobStreamStarter.getState());
            if (jobStreamStarter.getRunTime() != null) {
                dbItemJobStreamStarter.setRunTime(objectMapper.writeValueAsString(jobStreamStarter.getRunTime()));
            }
            sosHibernateSession.save(dbItemJobStreamStarter);
            jobStreamStarter.setJobStreamStarterId(dbItemJobStreamStarter.getId());
            
            for (JobStreamJob jobStreamJob : jobStreamStarter.getJobs()) {
                DBItemJobStreamStarterJob dbItemJobStreamStarterJob = new DBItemJobStreamStarterJob();
                dbItemJobStreamStarterJob.setCreated(new Date());
                dbItemJobStreamStarterJob.setDelay(jobStreamJob.getStartDelay());
                dbItemJobStreamStarterJob.setJob(jobStreamJob.getJob());
                dbItemJobStreamStarterJob.setJobStreamStarter(dbItemJobStreamStarter.getId());
                Long newJobId = dbLayerJobStreamsStarterJobs.store(dbItemJobStreamStarterJob);
                jobStreamJob.setJobId(newJobId);
            }
            
            
            for (NameValuePair param : jobStreamStarter.getParams()) {
                DBItemJobStreamParameter dbItemJobStreamParameter = new DBItemJobStreamParameter();
                dbItemJobStreamParameter.setCreated(new Date());
                dbItemJobStreamParameter.setJobStreamStarter(dbItemJobStreamStarter.getId());
                if (param.getName() != null) {
                    dbItemJobStreamParameter.setName(param.getName());
                    if (param.getValue() == null) {
                        param.setValue("");
                    }
                    dbItemJobStreamParameter.setValue(param.getValue());
                    sosHibernateSession.save(dbItemJobStreamParameter);
                }
            }
        }
    }

}