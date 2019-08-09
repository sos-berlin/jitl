package com.sos.jitl.eventing.eventhandler;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;

import org.junit.Test;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.eventing.db.SchedulerEventDBItem;
import com.sos.jitl.eventing.db.SchedulerEventDBLayer;
import com.sos.jitl.eventing.db.SchedulerEventFilter;
import com.sos.jitl.reporting.db.DBLayer;

public class XmlEventHandlerTest {

	private SOSHibernateSession getSession(String confFile) throws Exception {
		SOSHibernateFactory sosHibernateFactory = new SOSHibernateFactory(confFile);
		sosHibernateFactory.addClassMapping(DBLayer.getReportingClassMapping());
		sosHibernateFactory.build();
		return sosHibernateFactory.openStatelessSession();
	}

	@Test
	public void testgetListOfCommands() throws Exception {
		String confFile = "D:/documents/sos-berlin.com/scheduler_joc_cockpit/config/hibernate.cfg.xml";
		SOSHibernateSession session;
		SchedulerEventDBLayer schedulerEventDBLayer;
		SchedulerEventFilter filter;
		Collection<Object> eventHandlerResultedCommands = new Vector<Object>();
		List<SchedulerEventDBItem> listOfEvents;
		String eventHandlerFilepath = "D:/documents/sos-berlin.com/scheduler_joc_cockpit/config/events";

		session = getSession(confFile);
		schedulerEventDBLayer = new SchedulerEventDBLayer(session);

		filter = new SchedulerEventFilter();

		listOfEvents = schedulerEventDBLayer.getSchedulerEventList(filter);
		eventHandlerResultedCommands = new LinkedHashSet<Object>();
		XmlEventHandler xmlEventHandler = new XmlEventHandler(eventHandlerResultedCommands, listOfEvents,
				eventHandlerFilepath, filter.getJobChain(), filter.getEventClass());
		xmlEventHandler.getListOfCommands();
		System.out.println(eventHandlerResultedCommands.size());

	}

}
