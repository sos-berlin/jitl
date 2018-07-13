package com.sos.jitl.eventing.db;

import java.io.StringReader;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.persistence.TemporalType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.log4j.Logger;
import org.hibernate.query.Query;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import com.sos.jitl.eventing.evaluate.BooleanExp;
import com.sos.joc.model.order.OrderPath;
import sos.scheduler.job.JobSchedulerEventJob;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.classes.CustomEventsUtil;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.hibernate.layer.SOSHibernateDBLayer;

public class SchedulerEventDBLayer extends SOSHibernateDBLayer {

	private static final String ADD = "add";
	private static final String REMOVE = "remove";
	private static final Logger LOGGER = Logger.getLogger(SchedulerEventDBLayer.class);
	private static final String SchedulerEventDBItem = SchedulerEventDBItem.class.getName();

	private String notifyCommand;

	public SchedulerEventDBLayer(final String configurationFilename) throws Exception {
		super();
		this.setConfigurationFileName(configurationFilename);
		this.createStatefullConnection(this.getConfigurationFileName());
	}

	public SchedulerEventDBLayer(SOSHibernateSession session) throws Exception {
		super();
		this.setConfigurationFileName(session.getFactory().getConfigFile().get().toFile().getAbsolutePath());
		this.sosHibernateSession = session;
	}

	public void beginTransaction() throws Exception {
		this.sosHibernateSession.beginTransaction();
	}

	public void rollback() throws Exception {
		try {
			this.sosHibernateSession.rollback();
		} catch (Exception e) {
		}
	}

	public void commit() throws Exception {
		this.sosHibernateSession.commit();
	}

	public SchedulerEventDBItem getEvent(final Long id) throws Exception {
		if (sosHibernateSession == null) {
			this.createStatefullConnection(this.getConfigurationFileName());
		}
		return (SchedulerEventDBItem) (sosHibernateSession.get(SchedulerEventDBItem.class, id));
	}

	private Query<SchedulerEventDBItem> bindParameters(String hql, SchedulerEventFilter filter)
			throws SOSHibernateException {
		Query<SchedulerEventDBItem> query = sosHibernateSession.createQuery(hql);

		if (filter.hasEventIds()) {
			query.setParameterList("eventId", filter.getListOfEventIds());
		}
		if (filter.hasJobs()) {
			query.setParameterList("job", filter.getListOfJobs());
		}
		if (filter.hasExitCodes()) {
			query.setParameterList("exitCode", filter.getListOfExitCodes());
		}
		if (filter.hasEventClasses()) {
			query.setParameterList("eventClass", filter.getListOfEventClasses());
		}
		if (filter.hasIds()) {
			query.setParameterList("ids", filter.getListOfIds());
		}

		if (filter.getSchedulerId() != null && !filter.getSchedulerId().isEmpty()) {
			query.setParameter("schedulerId", filter.getSchedulerId());
		}
		if (filter.getRemoteUrl() != null && !filter.getRemoteUrl().isEmpty()) {
			query.setParameter("remoteUrl", filter.getRemoteUrl());
		}
		if (filter.getRemoteSchedulerHost() != null && !filter.getRemoteSchedulerHost().isEmpty()) {
			query.setParameter("remoteSchedulerHost", filter.getRemoteSchedulerHost());
		}
		if (filter.getRemoteSchedulerPort() != null) {
			query.setParameter("remoteSchedulerPort", filter.getRemoteSchedulerPort());
		}
		if (filter.getJobChain() != null && !filter.getJobChain().isEmpty()) {
			query.setParameter("jobChain", filter.getJobChain());
		}
		if (filter.getOrderId() != null && !filter.getOrderId().isEmpty()) {
			query.setParameter("orderId", filter.getOrderId());
		}
		if (filter.getJobName() != null && !filter.getJobName().isEmpty()) {
			query.setParameter("jobName", filter.getJobName());
		}
		if (filter.getEventClass() != null && !filter.getEventClass().isEmpty()) {
			query.setParameter("eventClass", filter.getEventClass());
		}
		if (filter.getEventId() != null && !filter.getEventId().isEmpty()) {
			query.setParameter("eventId", filter.getEventId());
		}
		if (filter.getExitCode() != null) {
			query.setParameter("exitCode", filter.getExitCode());
		}
		if (filter.getExpiresFrom() != null) {
			query.setParameter("expiresFrom", filter.getExpiresFrom(), TemporalType.TIMESTAMP);
		}
		if (filter.getExpiresTo() != null) {
			query.setParameter("expiresTo", filter.getExpiresTo(), TemporalType.TIMESTAMP);
		}
		return query;
	}

	public int delete(SchedulerEventFilter filter) throws Exception {
		int row = 0;
		String hql = "delete from " + SchedulerEventDBItem + " " + getWhere(filter);
		LOGGER.debug("delete:" + hql);

		Query<SchedulerEventDBItem> query = bindParameters(hql, filter);
		row = sosHibernateSession.executeUpdate(query);
		notifyWebservices(REMOVE);
		return row;
	}

	private String getOrderClause(OrderPath order) {
		if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
			return "(jobChain=" + order.getJobChain() + ")";
		} else {
			if (order.getJobChain() == null || order.getJobChain().isEmpty()) {
				return "(orderId=" + order.getOrderId() + ")";
			} else {
				return "(orderId = " + order.getOrderId() + " and jobChain=" + order.getJobChain() + ")";
			}
		}

	}

	private String getWhere(SchedulerEventFilter filter) {
		String where = "";
		String and = "";
		if (filter.hasIds()) {
			where += and + " id in ( :ids )";
			and = " and ";
		}
		if (filter.hasEventIds()) {
			where += and + " eventId in ( :eventId )";
			and = " and ";
		}
		if (filter.hasJobs()) {
			where += and + " job in ( :job )";
			and = " and ";
		}
		if (filter.hasEventClasses()) {
			where += and + " eventClass in ( :eventClass )";
			and = " and ";
		}
		if (filter.hasExitCodes()) {
			where += and + " exitCode in ( :exitCode )";
			and = " and ";
		}
		if (filter.hasOrders()) {
			where += and + "(";
			for (OrderPath order : filter.getListOfOrders()) {
				where += getOrderClause(order) + " or ";
			}
			where += " 1=0)";
			and = " and ";
		}
		if (filter.getRemoteUrl() != null && !filter.getRemoteUrl().isEmpty()) {
			where += and + " remoteUrl=:remoteUrl";
			and = " and ";
		}

		if (filter.getRemoteSchedulerPort() != null) {
			where += and + " remoteSchedulerPort = :remoteSchedulerPort";
			and = " and ";
		}
		if (filter.getRemoteSchedulerHost() != null && !filter.getRemoteSchedulerHost().isEmpty()) {
			where += and + " remoteSchedulerHost = :remoteSchedulerHost";
			and = " and ";
		}
		if (filter.isSchedulerIdEmpty()) {
			if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
				where += and + " (schedulerId is null or schedulerId='' or schedulerId=:schedulerId)";
				and = " and ";
			} else {
				where += and + " (schedulerId is null or schedulerId='')";
				and = " and ";
			}
		} else {
			if (filter.getSchedulerId() != null && !filter.getSchedulerId().isEmpty()) {
				where += and + " schedulerId=:schedulerId";
				and = " and ";
			}
		}
		if (filter.getJobChain() != null && !filter.getJobChain().isEmpty()) {
			where += and + " jobChain = :jobChain";
			and = " and ";
		}
		if (filter.getJobName() != null && !filter.getJobName().isEmpty()) {
			where += and + " jobName = :jobName";
			and = " and ";
		}
		if (filter.getOrderId() != null && !filter.getOrderId().isEmpty()) {
			where += and + " orderId = :orderId";
			and = " and ";
		}
		if (filter.getEventId() != null && !filter.getEventId().isEmpty()) {
			where += and + " eventId = :eventId";
			and = " and ";
		}
		if (filter.getEventClass() != null && !filter.getEventClass().isEmpty()) {
			where += and + " eventClass = :eventClass";
			and = " and ";
		}
		if (filter.getExitCode() != null) {
			where += and + " exitCode = :exitCode";
			and = " and ";
		}
		if (filter.getExpiresFrom() != null) {
			where += and + " expires >= :expiresFrom";
			and = " and ";
		}
		if (filter.getExpiresTo() != null) {
			where += and + " expires <= :expiresTo";
			and = " and ";
		}
		if (!"".equals(where.trim())) {
			where = "where " + where;
		}
		return where;
	}

	public List<SchedulerEventDBItem> getSchedulerEventList(SchedulerEventFilter filter) throws Exception {
		List<SchedulerEventDBItem> listOfCustomEvents = null;
		Query<SchedulerEventDBItem> query = bindParameters(String.format("from %s %s %s %s", SchedulerEventDBItem,
				getWhere(filter), filter.getOrderCriteria(), filter.getSortMode()), filter);

		if (filter.getLimit() > 0) {
			query.setMaxResults(filter.getLimit());
		}
		listOfCustomEvents = sosHibernateSession.getResultList(query);
		return listOfCustomEvents;
	}

	public SchedulerEventDBItem getEventItem(SchedulerEventFilter filter) throws Exception {
		filter.setLimit(1);
		List<SchedulerEventDBItem> list = getSchedulerEventList(filter);
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

	public boolean checkEventExists(SchedulerEventFilter filter) throws Exception {
		filter.setLimit(1);
		return !getSchedulerEventList(filter).isEmpty();
	}

	public boolean checkEventExists(String condition) throws Exception {
		SchedulerEventFilter filter = new SchedulerEventFilter();
		List<SchedulerEventDBItem> listOfActiveEvents = getSchedulerEventList(filter);
		Iterator<SchedulerEventDBItem> iExit = listOfActiveEvents.iterator();
		BooleanExp exp = new BooleanExp(condition);
		while (iExit.hasNext()) {
			SchedulerEventDBItem e = iExit.next();
			exp.replace(e.getEventName() + ":" + e.getExitCode(), "true");
			exp.replace(e.getEventId() + ":" + e.getExitCode(), "true");
			LOGGER.debug(exp.getBoolExp());
		}
		Iterator<SchedulerEventDBItem> iClass = listOfActiveEvents.iterator();
		while (iClass.hasNext()) {
			SchedulerEventDBItem e = iClass.next();
			exp.replace(e.getEventName(), "true");
			LOGGER.debug(exp.getBoolExp());
		}
		Iterator<SchedulerEventDBItem> iEventId = listOfActiveEvents.iterator();
		while (iEventId.hasNext()) {
			SchedulerEventDBItem e = iEventId.next();
			exp.replace(e.getEventId(), "true");
			LOGGER.debug(exp.getBoolExp());
		}
		LOGGER.debug("--------->" + exp.getBoolExp());
		return exp.evaluateExpression();
	}

	public boolean checkEventExists(String condition, String eventClass) throws Exception {
		SchedulerEventFilter filter = new SchedulerEventFilter();
		filter.setEventClass(eventClass);
		LOGGER.debug("eventClass:" + eventClass);
		List<SchedulerEventDBItem> listOfActiveEvents = getSchedulerEventList(filter);
		Iterator<SchedulerEventDBItem> iExit = listOfActiveEvents.iterator();
		BooleanExp exp = new BooleanExp(condition);
		while (iExit.hasNext()) {
			SchedulerEventDBItem e = iExit.next();
			exp.replace(e.getEventId() + ":" + e.getExitCode(), "true");
		}
		Iterator<SchedulerEventDBItem> iEventId = listOfActiveEvents.iterator();
		while (iEventId.hasNext()) {
			SchedulerEventDBItem e = iEventId.next();
			exp.replace(e.getEventId(), "true");
		}
		return exp.evaluateExpression();
	}

	public void insertItem(SchedulerEventDBItem schedulerEventDBItem2) throws SOSHibernateException {
		this.sosHibernateSession.save(schedulerEventDBItem2);
		try {
			notifyWebservices(ADD);
		} catch (JsonProcessingException e) {
			LOGGER.warn("Could not create notification command for add");
		}
	}

	public void updateItem(SchedulerEventDBItem schedulerEventDBItem2) throws SOSHibernateException {
		this.sosHibernateSession.update(schedulerEventDBItem2);
		try {
			notifyWebservices(ADD);
		} catch (JsonProcessingException e) {
			LOGGER.warn("Could not create notification command for add");
		}
	}

	public void deleteItem(SchedulerEventDBItem schedulerEventDBItem2) throws SOSHibernateException {
		this.sosHibernateSession.delete(schedulerEventDBItem2);
		try {
			notifyWebservices(REMOVE);
		} catch (JsonProcessingException e) {
			LOGGER.warn("Could not create notification command for remove");
		}

	}

	private void notifyWebservices(String action) throws JsonProcessingException {
		CustomEventsUtil customEventsUtil = new CustomEventsUtil(JobSchedulerEventJob.class.getName());
		if (ADD.equalsIgnoreCase(action)) {
			customEventsUtil.addEvent("CustomEventAdded");
		} else if (REMOVE.equalsIgnoreCase(action)) {
			customEventsUtil.addEvent("CustomEventDeleted");
		}
		this.notifyCommand = customEventsUtil.getEventCommandAsXml();
	}

	public void addEvent(SchedulerEventFilter filter) throws Exception {
		try {
			SchedulerEventDBItem schedulerEventDBItem = new SchedulerEventDBItem();
			SchedulerEventFilter uniqueFilter = new SchedulerEventFilter();
			uniqueFilter.setSchedulerId(filter.getSchedulerId());
			uniqueFilter.setEventClass(filter.getEventClass());
			uniqueFilter.setEventId(filter.getEventId());
			uniqueFilter.setExitCode(filter.getExitCode());

			SchedulerEventDBItem schedulerEventDBItem2 = getEventItem(uniqueFilter);
			if (schedulerEventDBItem2 != null) {
				schedulerEventDBItem = schedulerEventDBItem2;
			}

			LOGGER.debug(".. constructing event: schedulerId=" + filter.getSchedulerId() + ", eventClass="
					+ filter.getEventClass() + ", eventId=" + filter.getEventId());
			schedulerEventDBItem.setSchedulerId(filter.getSchedulerId());
			schedulerEventDBItem.setEventClass(filter.getEventClass());
			schedulerEventDBItem.setEventId(filter.getEventId());
			if (filter.getExitCode() == null) {
				schedulerEventDBItem.setExitCode(0);
			} else {
				schedulerEventDBItem.setExitCode(filter.getExitCode());
			}

			schedulerEventDBItem.setCreated(new Date());
			if (filter.getExpiresTo() == null) {
				if (filter.getExpirationDate() == null) {
					filter.calculateExpirationDate();
				} else {
					filter.setExpires(filter.getExpirationDate().getTime());
				}
				schedulerEventDBItem.setExpires(filter.getExpirationDate().getTime());
			} else {
				schedulerEventDBItem.setExpires(filter.getExpiresTo());
			}
			schedulerEventDBItem.setJobChain(filter.getJobChain());
			schedulerEventDBItem.setJobName(filter.getJobName());

			schedulerEventDBItem.setParameters(filter.getParametersAsString());
			schedulerEventDBItem.setOrderId(filter.getOrderId());
			schedulerEventDBItem.setRemoteSchedulerHost(filter.getRemoteSchedulerHost());
			schedulerEventDBItem.setRemoteSchedulerPort(filter.getRemoteSchedulerPort());

			LOGGER.info(".. adding event ...: scheduler id=" + schedulerEventDBItem.getSchedulerId() + ", event class="
					+ schedulerEventDBItem.getEventClass() + ", event id=" + schedulerEventDBItem.getEventId()
					+ ", exit code=" + schedulerEventDBItem.getExitCodeAsString() + ", job chain="
					+ schedulerEventDBItem.getJobChain() + ", order id=" + schedulerEventDBItem.getOrderId() + ", job="
					+ schedulerEventDBItem.getJobName());

			if (schedulerEventDBItem.getEventId() == null || schedulerEventDBItem.getEventId().isEmpty()) {
				throw new Exception("Empty event_id is not allowed.");
			}

			if (schedulerEventDBItem2 == null) {
				insertItem(schedulerEventDBItem);
			} else {
				updateItem(schedulerEventDBItem);
			}
			notifyWebservices(ADD);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw e;
		}
	}

	public int removeEvent(SchedulerEventFilter filter) throws Exception {
		try {
			LOGGER.debug(".. removing event: schedulerId=" + filter.getSchedulerId() + ", eventClass="
					+ filter.getEventClass() + ", eventId=" + filter.getEventId());
			int rows = delete(filter);
			if (rows > 0) {
			    notifyWebservices(REMOVE);
			}
			return rows;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw e;
		}
	}

	public String getNotifyCommand() {
		return notifyCommand;
	}

	private Document getEventsAsXmlFromList(String schedulerId, List<SchedulerEventDBItem> listOfEvents,
			SchedulerEventFilter filter) throws Exception {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document eventDocument = docBuilder.newDocument();
		eventDocument.appendChild(eventDocument.createElement("events"));

		if (schedulerId != null && !schedulerId.isEmpty()) {
			filter.setSchedulerIdEmpty(true);
			filter.setSchedulerId(schedulerId);
		}
		filter.setExpires("now_utc");
		boolean saveAutoCommit = this.getSession().isAutoCommit();
		this.getSession().setAutoCommit(false);
		this.beginTransaction();
		this.delete(filter);
		filter.setExpiresTo(null);

		List<SchedulerEventDBItem> eventList = null;
		if (listOfEvents == null) {
			eventList = this.getSchedulerEventList(filter);
		} else {
			eventList = listOfEvents;
		}
		this.commit();
		this.getSession().setAutoCommit(saveAutoCommit);

		for (SchedulerEventDBItem eventItem : eventList) {
			Element event = eventDocument.createElement("event");
			event.setAttribute("scheduler_id", eventItem.getSchedulerId());
			event.setAttribute("remote_scheduler_host", eventItem.getRemoteSchedulerHost());
			event.setAttribute("remote_scheduler_port", String.valueOf(eventItem.getRemoteSchedulerPort()));
			event.setAttribute("job_chain", eventItem.getJobChain());
			event.setAttribute("order_id", eventItem.getOrderId());
			event.setAttribute("job_name", eventItem.getJobName());
			event.setAttribute("event_class", eventItem.getEventClass());
			event.setAttribute("event_id", eventItem.getEventId());
			event.setAttribute("exit_code", eventItem.getExitCodeAsString());
			event.setAttribute("expires", eventItem.getExpiresAsString());
			event.setAttribute("created", eventItem.getCreatedAsString());
			if (eventItem.getParameters() != null && !eventItem.getParameters().isEmpty()) {
				DocumentBuilderFactory docFactoryParam = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilderParam = docFactoryParam.newDocumentBuilder();
				Document eventParameters = docBuilderParam
						.parse(new InputSource(new StringReader(eventItem.getParameters())));
				LOGGER.debug("Importing params node...");
				Node impParameters = eventDocument.importNode(eventParameters.getDocumentElement(), true);
				LOGGER.debug("appending params child...");
				event.appendChild(impParameters);
			}
			eventDocument.getLastChild().appendChild(event);
		}
		LOGGER.info(eventList.size() + " events readed from database");
		return eventDocument;

	}

	public Document getEventsAsXml(String schedulerId) throws Exception {
		SchedulerEventFilter filter = new SchedulerEventFilter();
		return getEventsAsXmlFromList(schedulerId, null, filter);
	}

	public Document getEventsAsXml(String schedulerId, List<SchedulerEventDBItem> listOfActiveEvents) throws Exception {
		SchedulerEventFilter filter = new SchedulerEventFilter();
		return getEventsAsXmlFromList(schedulerId, listOfActiveEvents, filter);
	}

}