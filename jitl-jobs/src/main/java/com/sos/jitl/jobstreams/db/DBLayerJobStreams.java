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
import com.sos.joc.model.jobstreams.JobStream;
import com.sos.joc.model.jobstreams.JobStreamJob;
import com.sos.joc.model.jobstreams.JobStreamStarter;

public class DBLayerJobStreams {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerJobStreams.class);
    private static final String DBItemJobStream = DBItemJobStream.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerJobStreams(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemJobStream getJobStreamsDbItem(final Long id) throws SOSHibernateException {
        return (DBItemJobStream) sosHibernateSession.get(DBItemJobStream.class, id);
    }

    public FilterJobStreams resetFilter() {
        FilterJobStreams filter = new FilterJobStreams();
        filter.setStatus("");
        filter.setJobStream("");
        return filter;
    }

    private String getWhere(FilterJobStreams filter) {
        String where = "1=1";
        String and = " and ";

        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {

            where += and + " schedulerId = :schedulerId";

            and = " and ";
        }

        if (filter.getStatus() != null && !"".equals(filter.getStatus())) {
            where += and + " status = :status";
            and = " and ";
        }

        if (filter.getJobStream() != null && !"".equals(filter.getJobStream())) {
            where += and + " jobstream = :jobstream";
            and = " and ";
        }

        if (filter.getJobStreamId() != null) {
            where += and + " id  = :id";
            and = " and ";
        }

        if (filter.getFolder() != null && !"".equals(filter.getFolder())) {
            where += and + " folder  = :folder";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = " where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterJobStreams filter, Query<T> query) {
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getJobStreamId() != null) {
            query.setParameter("id", filter.getJobStreamId());
        }
        if (filter.getFolder() != null && !"".equals(filter.getFolder())) {
            query.setParameter("folder", filter.getFolder());
        }
        if (filter.getStatus() != null && !"".equals(filter.getStatus())) {
            query.setParameter("status", filter.getStatus());
        }
        if (filter.getJobStream() != null && !"".equals(filter.getJobStream())) {
            query.setParameter("jobstream", filter.getJobStream());
        }

        return query;
    }

    public List<DBItemJobStream> getJobStreamsList(FilterJobStreams filter, final int limit) throws SOSHibernateException {
        String q = "  from " + DBItemJobStream + getWhere(filter);

        Query<DBItemJobStream> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public Integer deleteCascading(FilterJobStreams filter) throws SOSHibernateException {
        int row = 0;
        String hql = "";
        DBLayerJobStreamStarters dbLayerJobStreamStarters = new DBLayerJobStreamStarters(sosHibernateSession);
        DBLayerJobStreamParameters dbLayerJobStreamParameters = new DBLayerJobStreamParameters(sosHibernateSession);
        DBLayerJobStreamsStarterJobs dbLayerJobStreamsStarterJobs = new DBLayerJobStreamsStarterJobs(sosHibernateSession);
        DBLayerInConditions dbLayerInConditions = new DBLayerInConditions(sosHibernateSession);
        DBLayerOutConditions dbLayerOutConditions = new DBLayerOutConditions(sosHibernateSession);
        DBLayerJobStreamHistory dbLayerJobStreamHistory = new DBLayerJobStreamHistory(sosHibernateSession);

        List<DBItemJobStream> lJobStreams = getJobStreamsList(filter, 0);
        for (DBItemJobStream dbItemJobStream : lJobStreams) {
            FilterJobStreamStarterJobs filterJobStreamStarterJobs = new FilterJobStreamStarterJobs();
            FilterJobStreamParameters filterJobStreamParameters = new FilterJobStreamParameters();

            FilterJobStreamHistory filterJobStreamHistory = new FilterJobStreamHistory();
            filterJobStreamHistory.setJobStreamId(dbItemJobStream.getId());
            filterJobStreamHistory.setSchedulerId(filter.getSchedulerId());
            dbLayerJobStreamHistory.deleteCascading(filterJobStreamHistory);

            FilterJobStreamStarters filterJobStreamStarters = new FilterJobStreamStarters();
            filterJobStreamStarters.setJobStreamId(dbItemJobStream.getId());
            List<DBItemJobStreamStarter> lStarters = dbLayerJobStreamStarters.getJobStreamStartersList(filterJobStreamStarters, 0);

            for (DBItemJobStreamStarter dbItemJobStreamStarter : lStarters) {
                filterJobStreamStarterJobs.setJobStreamStarter(dbItemJobStreamStarter.getId());
                dbLayerJobStreamsStarterJobs.delete(filterJobStreamStarterJobs);

                filterJobStreamParameters.setJobStreamStarterId(dbItemJobStreamStarter.getId());
                dbLayerJobStreamParameters.delete(filterJobStreamParameters);

            }

            FilterInConditions filterInConditions = new FilterInConditions();
            filterInConditions.setJobStream(dbItemJobStream.getJobStream());
            filterInConditions.setJobSchedulerId(filter.getSchedulerId());
            dbLayerInConditions.deleteCascading(filterInConditions);

            FilterOutConditions filterOutConditions = new FilterOutConditions();
            filterOutConditions.setJobStream(dbItemJobStream.getJobStream());
            filterOutConditions.setJobSchedulerId(filter.getSchedulerId());
            dbLayerOutConditions.deleteCascading(filterOutConditions);

            dbLayerJobStreamStarters.delete(filterJobStreamStarters);
        }

        hql = "delete from " + DBItemJobStream + getWhere(filter);
        Query<DBItemJobStream> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public Long store(DBItemJobStream jsJobStream) throws SOSHibernateException {
        FilterJobStreams filter = new FilterJobStreams();
        filter.setJobStream(jsJobStream.getJobStream());
        filter.setSchedulerId(jsJobStream.getSchedulerId());
        deleteCascading(filter);
        sosHibernateSession.save(jsJobStream);
        return jsJobStream.getId();

    }

    public void deleteInsert(JobStream jobStream) throws SOSHibernateException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DBLayerJobStreamStarters dbLayerJobStreamStarters = new DBLayerJobStreamStarters(sosHibernateSession);
        DBLayerJobStreamsStarterJobs dbLayerJobStreamsStarterJobs = new DBLayerJobStreamsStarterJobs(sosHibernateSession);
        DBLayerJobStreamParameters dbLayerJobStreamParameters = new DBLayerJobStreamParameters(sosHibernateSession);

        DBItemJobStream dbItemJobStream = new DBItemJobStream();
        dbItemJobStream.setCreated(new Date());
        dbItemJobStream.setJobStream(jobStream.getJobStream());
        dbItemJobStream.setSchedulerId(jobStream.getJobschedulerId());
        dbItemJobStream.setFolder(jobStream.getFolder());
        dbItemJobStream.setState(jobStream.getState());

        Long newId = this.store(dbItemJobStream);
        jobStream.setJobStreamId(newId);
        for (JobStreamStarter jobstreamStarter : jobStream.getJobstreamStarters()) {
            DBItemJobStreamStarter dbItemJobStreamStarter = new DBItemJobStreamStarter();
            dbItemJobStreamStarter.setCreated(new Date());
            dbItemJobStreamStarter.setJobStream(newId);
            dbItemJobStreamStarter.setTitle(jobstreamStarter.getTitle());
            if (jobstreamStarter.getRunTime() != null) {
                dbItemJobStreamStarter.setRunTime(objectMapper.writeValueAsString(jobstreamStarter.getRunTime()));
            }
            dbItemJobStreamStarter.setState(jobstreamStarter.getState());
            Long newStarterId = dbLayerJobStreamStarters.store(dbItemJobStreamStarter);
            jobstreamStarter.setJobStreamStarterId(newStarterId);
            jobstreamStarter.setTitle(jobstreamStarter.getTitle());
            for (JobStreamJob jobStreamJob : jobstreamStarter.getJobs()) {
                DBItemJobStreamStarterJob dbItemJobStreamStarterJob = new DBItemJobStreamStarterJob();
                dbItemJobStreamStarterJob.setCreated(new Date());
                dbItemJobStreamStarterJob.setDelay(jobStreamJob.getStartDelay());
                dbItemJobStreamStarterJob.setJob(jobStreamJob.getJob());
                dbItemJobStreamStarterJob.setJobStreamStarter(newStarterId);
                Long newJobId = dbLayerJobStreamsStarterJobs.store(dbItemJobStreamStarterJob);
                jobStreamJob.setJobId(newJobId);

            }
            for (NameValuePair param : jobstreamStarter.getParams()) {

                DBItemJobStreamParameter dbItemParameter = new DBItemJobStreamParameter();
                dbItemParameter.setCreated(new Date());
                dbItemParameter.setJobStreamStarter(newStarterId);
                dbItemParameter.setName(param.getName());
                dbItemParameter.setValue(param.getValue());
                dbLayerJobStreamParameters.save(dbItemParameter);
            }

        }
    }
}