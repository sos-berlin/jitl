package com.sos.jitl.jobstreams.db;

import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.dailyplan.db.Calendar2DB;
import com.sos.jitl.dailyplan.db.DailyPlanDBLayer;
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
        String where = " ";
        String and = " ";

        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            where += and + " schedulerId = :schedulerId";
            and = " and ";
        }

        if (filter.getStatus() != null && !"".equals(filter.getStatus())) {
            where += and + " state = :state";
            and = " and ";
        }

        if (filter.getJobStream() != null && !"".equals(filter.getJobStream())) {
            where += and + " jobStream = :jobStream";
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

        if (filter.getFolderItem() != null) {
            if (filter.getFolderItem().getRecursive()) {
                where += and + " folder like :folder";
            } else {
                where += and + " folder  = :folder";
            }
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
        if (filter.getFolderItem() != null) {
            if (filter.getFolderItem().getRecursive()) {
                query.setParameter("folder", filter.getFolderItem().getFolder() + "%");
            } else {
                query.setParameter("folder", filter.getFolderItem().getFolder());
            }
        }

        if (filter.getStatus() != null && !"".equals(filter.getStatus())) {
            query.setParameter("state", filter.getStatus());
        }
        if (filter.getJobStream() != null && !"".equals(filter.getJobStream())) {
            query.setParameter("jobStream", filter.getJobStream());
        }

        return query;
    }

    public Long getJobStreamCount() throws SOSHibernateException {
        String q = "select count(*) from " + DBItemJobStream;
        Query<?> query = sosHibernateSession.createQuery(q);
        Long count = (Long) sosHibernateSession.getResultList(query).get(0);
        return count;
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

    public Integer deleteCascading(FilterJobStreams filter, Boolean withConditionsAndStarters) throws SOSHibernateException {
        int row = 0;
        String hql = "";
        DBLayerJobStreamStarters dbLayerJobStreamStarters = new DBLayerJobStreamStarters(sosHibernateSession);
        DBLayerJobStreamParameters dbLayerJobStreamParameters = new DBLayerJobStreamParameters(sosHibernateSession);
        DBLayerJobStreamsStarterJobs dbLayerJobStreamsStarterJobs = new DBLayerJobStreamsStarterJobs(sosHibernateSession);
        DBLayerInConditions dbLayerInConditions = new DBLayerInConditions(sosHibernateSession);
        DBLayerOutConditions dbLayerOutConditions = new DBLayerOutConditions(sosHibernateSession);
        DBLayerJobStreamHistory dbLayerJobStreamHistory = new DBLayerJobStreamHistory(sosHibernateSession);
        DailyPlanDBLayer dailyPlanDBLayer = new DailyPlanDBLayer(sosHibernateSession);

        List<DBItemJobStream> lJobStreams = getJobStreamsList(filter, 0);
        for (DBItemJobStream dbItemJobStream : lJobStreams) {
            FilterJobStreamStarterJobs filterJobStreamStarterJobs = new FilterJobStreamStarterJobs();
            FilterJobStreamParameters filterJobStreamParameters = new FilterJobStreamParameters();

            if (filter.getJobStreamId() != null) {
                filter.setFolder(dbItemJobStream.getFolder());
            }

            FilterJobStreamStarters filterJobStreamStarters = new FilterJobStreamStarters();
            filterJobStreamStarters.setJobStreamId(dbItemJobStream.getId());
            List<DBItemJobStreamStarter> lStarters = dbLayerJobStreamStarters.getJobStreamStartersList(filterJobStreamStarters, 0);

            for (DBItemJobStreamStarter dbItemJobStreamStarter : lStarters) {
                filterJobStreamStarterJobs.setJobStreamStarter(dbItemJobStreamStarter.getId());
                dbLayerJobStreamsStarterJobs.delete(filterJobStreamStarterJobs);

                filterJobStreamParameters.setJobStreamStarterId(dbItemJobStreamStarter.getId());
                dbLayerJobStreamParameters.delete(filterJobStreamParameters);
            }

            if (withConditionsAndStarters) {
                FilterJobStreamHistory filterJobStreamHistory = new FilterJobStreamHistory();
                filterJobStreamHistory.setJobStreamId(dbItemJobStream.getId());
                filterJobStreamHistory.setSchedulerId(filter.getSchedulerId());
                dbLayerJobStreamHistory.deleteCascading(filterJobStreamHistory);
                dailyPlanDBLayer.getFilter().setJobStream(filter.getJobStream());
                if (filter.getJobStreamId() != null && (filter.getJobStream() == null || "".equals(filter.getJobStream()))) {
                    DBItemJobStream jobstream = this.getJobStreamsDbItem(filter.getJobStreamId());
                    if (jobstream != null) {
                        dailyPlanDBLayer.getFilter().setJobStream(jobstream.getJobStream());
                    }
                }
                dailyPlanDBLayer.delete(true);

            }

            if (withConditionsAndStarters) {

                FilterInConditions filterInConditions = new FilterInConditions();
                filterInConditions.setJobStream(dbItemJobStream.getJobStream());
                filterInConditions.setJobSchedulerId(filter.getSchedulerId());
                filterInConditions.setFolder(filter.getFolder());
                dbLayerInConditions.deleteCascading(filterInConditions);

                FilterOutConditions filterOutConditions = new FilterOutConditions();
                filterOutConditions.setJobStream(dbItemJobStream.getJobStream());
                filterOutConditions.setJobSchedulerId(filter.getSchedulerId());
                filterOutConditions.setFolder(filter.getFolder());
                dbLayerOutConditions.deleteCascading(filterOutConditions);

                dbLayerJobStreamStarters.delete(filterJobStreamStarters);
            }
        }

        hql = "delete from " + DBItemJobStream + getWhere(filter);
        Query<DBItemJobStream> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public Long store(DBItemJobStream dbItemJobStream) throws SOSHibernateException {
        DBLayerJobStreamHistory dbLayerJobStreamHistory = new DBLayerJobStreamHistory(sosHibernateSession);
        FilterJobStreams filter = new FilterJobStreams();
        filter.setFolder(dbItemJobStream.getFolder());
        filter.setJobStream(dbItemJobStream.getJobStream());
        filter.setSchedulerId(dbItemJobStream.getSchedulerId());
        List<DBItemJobStream> listOfJobStreams = getJobStreamsList(filter, 1);
        deleteCascading(filter, false);
        sosHibernateSession.save(dbItemJobStream);
        if (listOfJobStreams.size() > 0) {
            Long oldId = listOfJobStreams.get(0).getId();
            Long newId = dbItemJobStream.getId();
            if (oldId != newId) {
                dbLayerJobStreamHistory.updateHistoryWithJobStream(oldId, newId);
            }
        }

        return dbItemJobStream.getId();

    }

    public Long deleteInsert(JobStream jobStream, String timezone) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DBLayerJobStreamStarters dbLayerJobStreamStarters = new DBLayerJobStreamStarters(sosHibernateSession);
        DBLayerJobStreamsStarterJobs dbLayerJobStreamsStarterJobs = new DBLayerJobStreamsStarterJobs(sosHibernateSession);
        DBLayerJobStreamParameters dbLayerJobStreamParameters = new DBLayerJobStreamParameters(sosHibernateSession);
        Calendar2DB calendar2Db = new Calendar2DB(sosHibernateSession, jobStream.getJobschedulerId());

        DBItemJobStream dbItemJobStream = new DBItemJobStream();
        dbItemJobStream.setCreated(new Date());
        dbItemJobStream.setJobStream(jobStream.getJobStream());
        dbItemJobStream.setSchedulerId(jobStream.getJobschedulerId());
        dbItemJobStream.setFolder(jobStream.getFolder());
        dbItemJobStream.setState(jobStream.getState());

        Long newId = this.store(dbItemJobStream);
        LOGGER.debug("Store Jobstream with new id:" + newId);
        jobStream.setJobStreamId(newId);
        for (JobStreamStarter jobstreamStarter : jobStream.getJobstreamStarters()) {

            Long oldId = jobstreamStarter.getJobStreamStarterId();

            boolean isNew = false;

            DBItemJobStreamStarter dbItemJobStreamStarter = dbLayerJobStreamStarters.getJobStreamStartersDbItem(oldId);
            if (dbItemJobStreamStarter == null) {
                LOGGER.debug("new Jobstream starter found");

                isNew = true;
                dbItemJobStreamStarter = new DBItemJobStreamStarter();
                dbItemJobStreamStarter.setCreated(new Date());
            }

            dbItemJobStreamStarter.setJobStream(newId);
            dbItemJobStreamStarter.setEndOfJobStream(jobstreamStarter.getEndOfJobStream());
            dbItemJobStreamStarter.setRequiredJob(jobstreamStarter.getRequiredJob());
            dbItemJobStreamStarter.setStarterName(jobstreamStarter.getStarterName());
            dbItemJobStreamStarter.setTitle(jobstreamStarter.getTitle());
            dbItemJobStreamStarter.setState(jobstreamStarter.getState());

            if (jobstreamStarter.getRunTime() != null) {
                dbItemJobStreamStarter.setRunTime(objectMapper.writeValueAsString(jobstreamStarter.getRunTime()));
            }
            dbItemJobStreamStarter.setNextStart(dbLayerJobStreamStarters.getNextStartTime(objectMapper, timezone, dbItemJobStreamStarter
                    .getRunTime()));

            if (isNew) {
                LOGGER.debug("save jobstream starter with jobstream id: " + dbItemJobStreamStarter.getJobStream());
                dbLayerJobStreamStarters.save(dbItemJobStreamStarter);
            } else {
                LOGGER.debug("update jobstream starter with jobstream id: " + dbItemJobStreamStarter.getJobStream());
                dbLayerJobStreamStarters.update(dbItemJobStreamStarter);
            }

            jobstreamStarter.setJobStreamStarterId(dbItemJobStreamStarter.getId());

            for (JobStreamJob jobStreamJob : jobstreamStarter.getJobs()) {
                DBItemJobStreamStarterJob dbItemJobStreamStarterJob = new DBItemJobStreamStarterJob();
                dbItemJobStreamStarterJob.setCreated(new Date());
                dbItemJobStreamStarterJob.setDelay(jobStreamJob.getStartDelay());
                dbItemJobStreamStarterJob.setJob(jobStreamJob.getJob());
                dbItemJobStreamStarterJob.setJobStreamStarter(dbItemJobStreamStarter.getId());
                if (jobStreamJob.getSkipOutCondition() == null) {
                    dbItemJobStreamStarterJob.setSkipOutCondition(false);
                } else {
                    dbItemJobStreamStarterJob.setSkipOutCondition(jobStreamJob.getSkipOutCondition());
                }
                Long newJobId = dbLayerJobStreamsStarterJobs.store(dbItemJobStreamStarterJob);
                jobStreamJob.setJobId(newJobId);
            }

            for (NameValuePair param : jobstreamStarter.getParams()) {

                if (param.getName() != null && !param.getName().isEmpty()) {
                    DBItemJobStreamParameter dbItemParameter = new DBItemJobStreamParameter();
                    dbItemParameter.setCreated(new Date());
                    dbItemParameter.setJobStreamStarter(dbItemJobStreamStarter.getId());
                    dbItemParameter.setName(param.getName());
                    dbItemParameter.setValue(param.getValue());
                    dbLayerJobStreamParameters.save(dbItemParameter);
                }
            }

            if (!isNew) {
                DailyPlanDBLayer dailyPlanDBLayer = new DailyPlanDBLayer(sosHibernateSession);
                dailyPlanDBLayer.getFilter().setJobStreamStarterId(oldId);
                dailyPlanDBLayer.delete(false);
            }

            FilterJobStreams filterJobStreams = new FilterJobStreams();
            filterJobStreams.setJobStreamId(jobStream.getJobStreamId());
            calendar2Db.processJobStreamStarterFilter(filterJobStreams, timezone);

        }

        return newId;
    }
}