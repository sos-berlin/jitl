package com.sos.jitl.inventory.helper;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.xml.transform.TransformerException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.exception.SOSInvalidDataException;
import com.sos.exception.SOSMissingDataException;
import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.inventory.db.DBLayerInventory;
import com.sos.jitl.reporting.db.DBItemInventoryClusterCalendar;
import com.sos.jitl.reporting.db.DBItemInventoryClusterCalendarUsage;
import com.sos.jitl.reporting.db.DBItemInventoryJob;
import com.sos.jitl.reporting.db.DBItemInventoryOrder;
import com.sos.jitl.reporting.db.DBItemInventorySchedule;
import com.sos.jitl.reporting.helper.EConfigFileExtensions;
import com.sos.jobscheduler.RuntimeCalendar;
import com.sos.jobscheduler.RuntimeResolver;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.model.calendar.Calendar;
import com.sos.joc.model.calendar.CalendarType;
import com.sos.joc.model.calendar.Calendars;
import com.sos.joc.model.calendar.Dates;
import com.sos.joc.model.calendar.Period;

import sos.xml.SOSXMLXPath;

public class InventoryRuntimeHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryRuntimeHelper.class);
    private static final String RUN_TIME_NODE_NAME = "run_time";
    private static final String CALENDARS_NODE_NAME = "calendars";

    public static void recalculateRuntime(DBLayerInventory inventoryDbLayer, String type, String path, String fileExtension,
            List<DBItemInventoryClusterCalendarUsage> dbCalendarUsages, Path liveDirectory, String timezone) throws Exception {
        recalculateRuntime(inventoryDbLayer, type, path, fileExtension, dbCalendarUsages, liveDirectory, timezone, null, false);
    }

    public static void recalculateRuntime(DBLayerInventory inventoryDbLayer, String type, String path, String fileExtension,
            List<DBItemInventoryClusterCalendarUsage> dbCalendarUsages, Path liveDirectory, String timezone, String calendarsXML,
            boolean forceUpdateCalendarsElem) throws IOException, DocumentException, TransformerException, SOSHibernateException,
            SOSMissingDataException, SOSInvalidDataException {
        ObjectMapper om = new ObjectMapper();
        Long calendarId = null;
        DBItemInventoryClusterCalendar dbCalendar = null;
        Path filePath = liveDirectory.resolve(path.substring(1) + fileExtension);
        if (Files.exists(filePath)) {
            String xml = new String(Files.readAllBytes(filePath));
            org.dom4j.Document document = DocumentHelper.parseText(xml);
            // get collection of all runtimes with calendar attribute
            TreeSet<RuntimeCalendar> xmlRuntimes = RuntimeResolver.getCalendarDatesFromUTCYesterday(document);
            TreeSet<RuntimeCalendar> usageRuntimes = new TreeSet<RuntimeCalendar>();
            if (dbCalendarUsages != null && !dbCalendarUsages.isEmpty()) {
                for (DBItemInventoryClusterCalendarUsage dbCalendarUsage : dbCalendarUsages) {
                    if (dbCalendarUsage.getObjectType().equalsIgnoreCase(type) && dbCalendarUsage.getPath().equalsIgnoreCase(path)) {
                        calendarId = dbCalendarUsage.getCalendarId();
                        Calendar calendarUsage = om.readValue(dbCalendarUsage.getConfiguration(), Calendar.class);
                        RuntimeCalendar rc = null;
                        if (calendarId != null) {
                            dbCalendar = inventoryDbLayer.getCalendar(calendarId);
                            rc = new RuntimeCalendar();
                            if (dbCalendar != null) {
                                rc.setPath(dbCalendar.getName());
                                try {
                                    rc.setType(CalendarType.fromValue(dbCalendar.getType()));
                                } catch (IllegalArgumentException e) {
                                    rc.setType(CalendarType.WORKING_DAYS);
                                    LOGGER.warn("could not determine calendar type, falling back to default type:", e);
                                }
                                if (rc.getType() == CalendarType.WORKING_DAYS) {
                                    if (calendarUsage.getPeriods() == null) {
                                        rc.setPeriods(new ArrayList<Period>());
                                    } else {
                                        rc.setPeriods(calendarUsage.getPeriods());
                                    }
                                }
                                Dates dates = null;
                                if (dbCalendarUsage.getConfiguration() != null && !dbCalendarUsage.getConfiguration().isEmpty()) {
                                    dates = new FrequencyResolver().resolveRestrictionsFromUTCYesterday(om.readValue(dbCalendar.getConfiguration(),
                                            Calendar.class), calendarUsage);
                                } else {
                                    dates = new FrequencyResolver().resolveFromUTCYesterday(om.readValue(dbCalendar.getConfiguration(),
                                            Calendar.class));
                                }
                                if (dates != null && dates.getDates() != null && !dates.getDates().isEmpty()) {
                                    rc.setDates(dates.getDates());
                                    usageRuntimes.add(rc);
                                }
                                if (dbCalendarUsage.getEdited()) {
                                    dbCalendarUsage.setEdited(false);
                                    dbCalendarUsage.setModified(Date.from(Instant.now()));
                                    inventoryDbLayer.getSession().update(dbCalendarUsage);
                                }
                            } else {
                                inventoryDbLayer.getSession().delete(dbCalendarUsage);
                            }
                        }
                    }
                }
            }
            document = updateCalendarsCDATA(document, calendarsXML);
            if (!xmlRuntimes.equals(usageRuntimes)) {
                RuntimeResolver.updateCalendarInRuntimes(document, new FileWriter(filePath.toFile()), usageRuntimes);
            } else if (forceUpdateCalendarsElem) {
                updateCalendarsInXMLOnly(document, new FileWriter(filePath.toFile()));
            }
        }
    }

    public static Document updateCalendarsCDATA(Document doc, String calendarsXML) {
        if (calendarsXML == null) {
            return doc;
        }
        Element rootElement = doc.getRootElement();
        Element run_time = null;
        if ("schedule".equals(rootElement.getName())) {
            run_time = rootElement;
        } else {
            run_time = rootElement.element(RUN_TIME_NODE_NAME);
        }
        Element calendars = null;
        if (run_time != null) {
            calendars = run_time.element(CALENDARS_NODE_NAME);
            if (calendars == null) {
                calendars = run_time.addElement(CALENDARS_NODE_NAME);
            }
        } else {
            Element commands = rootElement.element("commands");
            Element newRuntime = null;
            if (commands != null) {
                newRuntime = DocumentHelper.createElement(RUN_TIME_NODE_NAME);
                @SuppressWarnings("unchecked")
                List<Element> elements = commands.getParent().elements();
                elements.add(elements.indexOf(commands), newRuntime);
                calendars = newRuntime.addElement(CALENDARS_NODE_NAME);
            } else {
                newRuntime = rootElement.addElement(RUN_TIME_NODE_NAME);
                calendars = newRuntime.addElement(CALENDARS_NODE_NAME);
            }
        }
        if (calendars.hasContent()) {
            calendars.content().clear();
        }
        calendars.addCDATA(calendarsXML);
        return doc;
    }

    public static void createOrUpdateCalendarUsage(SOSXMLXPath xPath, List<DBItemInventoryClusterCalendarUsage> dbCalendarUsages, DbItem dbItem,
            String type, DBLayerInventory dbLayer, Path liveDirectory, String schedulerId, String timezone, Boolean isCalendarEvent)
            throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // set only if json should be pretty printed (results in a lot of lines)
        // mapper.enable(SerializationFeature.INDENT_OUTPUT);
        LOGGER.debug("*** Method createOrUpdateCalendarUsage started");
        String calendarUsagesFromConfigFile = xPath.getNodeText(xPath.selectSingleNode("run_time/calendars"));
        LOGGER.debug(calendarUsagesFromConfigFile);
        Calendars jsonCalendarsFromDB = new Calendars();
        List<Calendar> calendarUsageList = new ArrayList<Calendar>();
        // Map to 'json' pojo
        if (dbCalendarUsages == null) {
            dbCalendarUsages = new ArrayList<DBItemInventoryClusterCalendarUsage>();
        }
        List<DBItemInventoryClusterCalendarUsage> deletedUsages = new ArrayList<DBItemInventoryClusterCalendarUsage>();
        for (DBItemInventoryClusterCalendarUsage dbUsages : dbCalendarUsages) {
            if (dbUsages.getConfiguration() != null) {
                Calendar cal = mapper.readValue(dbUsages.getConfiguration(), Calendar.class);
                if (cal != null) {
                    DBItemInventoryClusterCalendar dbCal = dbLayer.getCalendar(dbUsages.getCalendarId());
                    cal.setType(CalendarType.fromValue(dbCal.getType()));
                    cal.setBasedOn(dbCal.getName());
                    calendarUsageList.add(cal);
                }
            } else {
                dbLayer.getSession().delete(dbUsages);
                deletedUsages.add(dbUsages);
            }
        }
        for (DBItemInventoryClusterCalendarUsage deletedUsage : deletedUsages) {
            dbCalendarUsages.remove(deletedUsage);
        }

        if (!calendarUsageList.isEmpty()) {
            jsonCalendarsFromDB.setCalendars(calendarUsageList);
            LOGGER.debug("*** [createOrUpdateCalendarUsage] calendar usages found in DB");
        }
        Calendars calendarsFromXML = null;
        if (calendarUsagesFromConfigFile != null && !calendarUsagesFromConfigFile.isEmpty()) {
            // Map to 'json' pojo for comparison
            calendarsFromXML = mapper.readValue(calendarUsagesFromConfigFile, Calendars.class);
        }

        String path = null;
        String fileExtension = null;
        if ("JOB".equals(type)) {
            fileExtension = EConfigFileExtensions.JOB.extension();
            path = ((DBItemInventoryJob) dbItem).getName();
        } else if ("ORDER".equals(type)) {
            fileExtension = EConfigFileExtensions.ORDER.extension();
            path = ((DBItemInventoryOrder) dbItem).getName();
        } else if ("SCHEDULE".equals(type)) {
            fileExtension = EConfigFileExtensions.SCHEDULE.extension();
            path = ((DBItemInventorySchedule) dbItem).getName();
        }
        
        boolean forceUpdateCalendarsElem = !jsonCalendarsFromDB.equals(calendarsFromXML);

        if (isCalendarEvent != null && isCalendarEvent) {
            LOGGER.debug("*** [createOrUpdateCalendarUsage] processing Calendar event: " + path + fileExtension);
            InventoryRuntimeHelper.recalculateRuntime(dbLayer, type, path, fileExtension, dbCalendarUsages, liveDirectory, timezone, mapper
                    .writeValueAsString(jsonCalendarsFromDB), forceUpdateCalendarsElem);
        } else {

            if (!dbCalendarUsages.isEmpty()) {
                LOGGER.debug("*** [createOrUpdateCalendarUsage] referenced calendar and usage found in DB");
                LOGGER.debug("*** [createOrUpdateCalendarUsage] update metainfo in XML and recalculate runtimes");
                InventoryRuntimeHelper.recalculateRuntime(dbLayer, type, path, fileExtension, dbCalendarUsages, liveDirectory, timezone, mapper
                        .writeValueAsString(jsonCalendarsFromDB), forceUpdateCalendarsElem);
            } else {

                if (calendarsFromXML != null && calendarsFromXML.getCalendars() != null && !calendarsFromXML.getCalendars().isEmpty()) {
                    LOGGER.debug("*** [createOrUpdateCalendarUsage] calendar usages found in XML but not in DB");

                    // new file because of moving/renaming/deploying Order/Job/Schedule

                    for (Calendar calendarFromXML : calendarsFromXML.getCalendars()) {
                        if (calendarFromXML.getBasedOn() != null && !calendarFromXML.getBasedOn().isEmpty()) {
                            DBItemInventoryClusterCalendar dbCal = dbLayer.getCalendar(schedulerId, calendarFromXML.getBasedOn());
                            if (dbCal != null) {
                                LOGGER.debug("*** [createOrUpdateCalendarUsage] referenced calendar found in DB: " + calendarFromXML.getBasedOn());
                                LOGGER.debug("*** [createOrUpdateCalendarUsage] create new calendar usage in DB");
                                DBItemInventoryClusterCalendarUsage newCalendarUsage = new DBItemInventoryClusterCalendarUsage();
                                newCalendarUsage.setConfiguration(mapper.writeValueAsString(calendarFromXML));
                                newCalendarUsage.setSchedulerId(schedulerId);
                                newCalendarUsage.setObjectType(type);
                                newCalendarUsage.setPath(path);
                                newCalendarUsage.setCalendarId(dbCal.getId());
                                newCalendarUsage.setEdited(false);
                                newCalendarUsage.setCreated(Date.from(Instant.now()));
                                newCalendarUsage.setModified(newCalendarUsage.getCreated());
                                dbLayer.getSession().save(newCalendarUsage);
                                LOGGER.debug("*** [createOrUpdateCalendarUsage] new calendar usage for DB saved!");
                                dbCalendarUsages.add(newCalendarUsage);
                                calendarUsageList.add(calendarFromXML);
                            } else {
                                LOGGER.debug("*** [createOrUpdateCalendarUsage] referenced calendar not found in DB: " + calendarFromXML
                                        .getBasedOn());
                                LOGGER.debug("*** [createOrUpdateCalendarUsage] going to delete calendar usage from XML");
                            }
                        }
                    }
                    jsonCalendarsFromDB.setCalendars(calendarUsageList);
                    boolean forceUpdateCalendarsElem2 = !jsonCalendarsFromDB.equals(calendarsFromXML);
                    InventoryRuntimeHelper.recalculateRuntime(dbLayer, type, path, fileExtension, dbCalendarUsages, liveDirectory, timezone, mapper
                            .writeValueAsString(jsonCalendarsFromDB), forceUpdateCalendarsElem2);

                } else {

                    LOGGER.debug("*** [createOrUpdateCalendarUsage] referenced calendar and usage not found in DB!");
                    LOGGER.debug("*** [createOrUpdateCalendarUsage] no metainfo provided in XML. recalculating runtimes without using calendars.");
                    InventoryRuntimeHelper.recalculateRuntime(dbLayer, type, path, fileExtension, dbCalendarUsages, liveDirectory, timezone);
                }
            }
        }
    }

    private static void updateCalendarsInXMLOnly(org.dom4j.Document document, Writer writer) throws IOException {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding(document.getXMLEncoding());
        format.setXHTML(true);
        format.setIndentSize(4);
        format.setExpandEmptyElements(false);
        XMLWriter xmlWriter = null;
        try {
            xmlWriter = new XMLWriter(writer, format);
            xmlWriter.write(document);
            xmlWriter.flush();
            xmlWriter.close();
        } finally {
            if (xmlWriter != null) {
                try {
                    xmlWriter.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
