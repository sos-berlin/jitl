package com.sos.jitl.inventory.helper;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.hibernate.classes.DbItem;
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
    
    public static void recalculateRuntime(DBLayerInventory inventoryDbLayer, DbItem item, List<DBItemInventoryClusterCalendarUsage> dbCalendarUsages,
            Path liveDirectory, String timezone) throws Exception {
        recalculateRuntime(inventoryDbLayer, item, dbCalendarUsages, liveDirectory, timezone, null);
    }
    
    public static void recalculateRuntime(DBLayerInventory inventoryDbLayer, DbItem item, List<DBItemInventoryClusterCalendarUsage> dbCalendarUsages,
            Path liveDirectory, String timezone, String calendarsXML) throws Exception {
        ObjectMapper om = new ObjectMapper();
        Long calendarId = null;
        DBItemInventoryClusterCalendar dbCalendar = null;
        String objectType = null;
        String path = null;
        String fileExtension = null;
        // determine object type
        if (item instanceof DBItemInventoryJob) {
            objectType = ObjectType.JOB.name();
            fileExtension = EConfigFileExtensions.JOB.extension();
            path = ((DBItemInventoryJob) item).getName();
        } else if (item instanceof DBItemInventoryOrder) {
            objectType = ObjectType.ORDER.name();
            fileExtension = EConfigFileExtensions.ORDER.extension();
            path = ((DBItemInventoryOrder) item).getName();
        } else if (item instanceof DBItemInventorySchedule) {
            objectType = ObjectType.SCHEDULE.name();
            fileExtension = EConfigFileExtensions.SCHEDULE.extension();
            path = ((DBItemInventorySchedule) item).getName();
        }
        Path filePath = Paths.get(path + fileExtension);
        filePath = liveDirectory.resolve(filePath.toString().substring(1));
        if (Files.exists(filePath)) {
            String xml = new String(Files.readAllBytes(filePath));
            org.dom4j.Document document = DocumentHelper.parseText(xml);
            // get collection of all runtimes with calendar attribute
            TreeSet<RuntimeCalendar> xmlRuntimes = RuntimeResolver.getCalendarDatesFromUTCYesterday(document);
            TreeSet<RuntimeCalendar> usageRuntimes = new TreeSet<RuntimeCalendar>();
            if (dbCalendarUsages != null && !dbCalendarUsages.isEmpty()) {
                for (DBItemInventoryClusterCalendarUsage dbCalendarUsage : dbCalendarUsages) {
                    if (dbCalendarUsage.getObjectType().equalsIgnoreCase(objectType) && dbCalendarUsage.getPath().equalsIgnoreCase(path)) {
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
                                    dates = new FrequencyResolver().resolveFromUTCYesterday(om.readValue(dbCalendar.getConfiguration(), Calendar.class));
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
            if (!xmlRuntimes.equals(usageRuntimes)) {
                if (calendarsXML != null) {
                    document = updateCalendarsCDATA(document, calendarsXML);
                }
                RuntimeResolver.updateCalendarInRuntimes(document, new FileWriter(filePath.toFile()), usageRuntimes);
            } else if (calendarsXML != null) {
                updateCalendarsInXMLOnly(calendarsXML, item, liveDirectory);
            }
        }
    }
    
    public static Document updateCalendarsCDATA(Document doc, String calendarsXML) {
        Element run_time = doc.getRootElement().element(RUN_TIME_NODE_NAME);
        Element calendars = null;
        if (run_time != null) {
            calendars = run_time.element(CALENDARS_NODE_NAME);
            if (calendars != null) {
                run_time.remove(calendars);
            }
            calendars = run_time.addElement(CALENDARS_NODE_NAME);
        } else {
            Element commands = doc.getRootElement().element("commands");
            Element newRuntime = null;
            if (commands != null) {
                newRuntime = DocumentHelper.createElement(RUN_TIME_NODE_NAME);
                List<Element> elements = commands.getParent().elements();
                elements.add(elements.indexOf(commands), newRuntime);
                calendars = newRuntime.addElement(CALENDARS_NODE_NAME);
            } else {
                newRuntime = doc.getRootElement().addElement(RUN_TIME_NODE_NAME);
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
            String type, DBLayerInventory dbLayer, Path liveDirectory, String schedulerId, String timezone) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // set only if json should be pretty printed (results in a lot of lines)
//        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        LOGGER.debug("*** Method createOrUpdateCalendarUsage started");
        String calendarUsagesFromConfigFile = xPath.getNodeText(xPath.selectSingleNode("run_time/calendars"));
        Calendars jsonCalendarsFromDB = new Calendars();
        List<Calendar> calendarUsageList = new ArrayList<Calendar>();
        // Map to 'json' pojo
        for (DBItemInventoryClusterCalendarUsage dbUsages : dbCalendarUsages) {
            Calendar cal = mapper.readValue(dbUsages.getConfiguration(), Calendar.class);
            DBItemInventoryClusterCalendar dbCal = dbLayer.getCalendar(dbUsages.getCalendarId());
            cal.setType(CalendarType.fromValue(dbCal.getType()));
            cal.setBasedOn(dbCal.getName());
            if (cal != null) {
                calendarUsageList.add(cal);
            }
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
        if (calendarsFromXML != null && calendarsFromXML.getCalendars() != null && !calendarsFromXML.getCalendars().isEmpty()) {
            for (Calendar calendarFromXML : calendarsFromXML.getCalendars()) {
                LOGGER.debug("*** [createOrUpdateCalendarUsage] calendar usages found in XML");
                if (jsonCalendarsFromDB.getCalendars() != null) {
                    LOGGER.debug("*** [createOrUpdateCalendarUsage] recalculate runtimes started");
                    String metaInfoFromDB = mapper.writeValueAsString(jsonCalendarsFromDB);
                    String metaInfoFromFile = mapper.writeValueAsString(calendarsFromXML);
                    if (metaInfoFromFile.equals(metaInfoFromDB)) {
                        InventoryRuntimeHelper.recalculateRuntime(dbLayer, dbItem, dbCalendarUsages, liveDirectory, timezone);
                    }else {
                        InventoryRuntimeHelper.recalculateRuntime(dbLayer, dbItem, dbCalendarUsages, liveDirectory, timezone, metaInfoFromDB);
                    }
                    LOGGER.debug("*** [createOrUpdateCalendarUsage] recalculate runtimes started");
                } else {
                    if (calendarFromXML.getBasedOn() != null && !calendarFromXML.getBasedOn().isEmpty()) {
                        DBItemInventoryClusterCalendar dbCal = dbLayer.getCalendar(schedulerId, calendarFromXML.getBasedOn());
                        if (dbCal != null) {
                            LOGGER.debug("*** [createOrUpdateCalendarUsage] referenced calendar found in DB");
                            LOGGER.debug("*** [createOrUpdateCalendarUsage] create new calendar usage for DB");
                            DBItemInventoryClusterCalendarUsage newCalendarUsage = new DBItemInventoryClusterCalendarUsage();
                            newCalendarUsage.setConfiguration(mapper.writeValueAsString(calendarFromXML));
                            newCalendarUsage.setSchedulerId(schedulerId);
                            newCalendarUsage.setObjectType(type); 
                            if ("ORDER".equals(type)) {
                                newCalendarUsage.setPath(((DBItemInventoryOrder)dbItem).getName());
                            } else if ("JOB".equals(type)) {
                                newCalendarUsage.setPath(((DBItemInventoryJob)dbItem).getName());
                            } else if ("SCHEDULE".equals(type)) {
                                newCalendarUsage.setPath(((DBItemInventorySchedule)dbItem).getName());
                            }
                            newCalendarUsage.setCalendarId(dbCal.getId());
                            newCalendarUsage.setEdited(false);
                            newCalendarUsage.setCreated(Date.from(Instant.now()));
                            newCalendarUsage.setModified(newCalendarUsage.getCreated());
                            dbLayer.getSession().save(newCalendarUsage);
                            LOGGER.debug("*** [createOrUpdateCalendarUsage] new calendar usage for DB saved!");
                            dbCalendarUsages.add(newCalendarUsage);
                            InventoryRuntimeHelper.recalculateRuntime(dbLayer, dbItem, dbCalendarUsages, liveDirectory, timezone);
                        } else {
                            LOGGER.debug("*** [createOrUpdateCalendarUsage] referenced calendar not found in DB");
                            LOGGER.debug("*** [createOrUpdateCalendarUsage] going to delete calendar usage from XML");
                            Calendars calendarsToUpdate = new Calendars();
                            calendarsToUpdate.setCalendars(new ArrayList<Calendar>());
                            calendarsToUpdate.getCalendars().addAll(calendarsFromXML.getCalendars());
                            calendarsToUpdate.getCalendars().remove(calendarFromXML);
                            String metaInfo = mapper.writeValueAsString(calendarsToUpdate);
                            updateCalendarsInXMLOnly(metaInfo, dbItem, liveDirectory);
                        }
                    }
                }
            }
        } else if (jsonCalendarsFromDB != null && jsonCalendarsFromDB.getCalendars() != null && !jsonCalendarsFromDB.getCalendars().isEmpty()) {
            LOGGER.debug("*** [createOrUpdateCalendarUsage] referenced calendar and usage found in DB");
            LOGGER.debug("*** [createOrUpdateCalendarUsage] update metainfo in XML and recalculate runtimes");
            String metaInfo = mapper.writeValueAsString(jsonCalendarsFromDB);
            InventoryRuntimeHelper.recalculateRuntime(dbLayer, dbItem, dbCalendarUsages, liveDirectory, timezone, metaInfo);
        } else if ((calendarUsagesFromConfigFile == null || calendarUsagesFromConfigFile.isEmpty()) && (jsonCalendarsFromDB == null || jsonCalendarsFromDB.getCalendars() == null)) {
            LOGGER.debug("*** [createOrUpdateCalendarUsage] referenced calendar and usage not found in DB!");
            LOGGER.debug("*** [createOrUpdateCalendarUsage] no metainfo provided in XML. recalculating runtimes without using calendars.");
            InventoryRuntimeHelper.recalculateRuntime(dbLayer, dbItem, dbCalendarUsages, liveDirectory, timezone);
        }
    }
    
    private static void updateCalendarsInXMLOnly(String metaInfo, DbItem item, Path liveDirectory) {
        String path = null;
        String fileExtension = null;
        if (item instanceof DBItemInventoryJob) {
            fileExtension = EConfigFileExtensions.JOB.extension();
            path = ((DBItemInventoryJob) item).getName();
        } else if (item instanceof DBItemInventoryOrder) {
            fileExtension = EConfigFileExtensions.ORDER.extension();
            path = ((DBItemInventoryOrder) item).getName();
        } else if (item instanceof DBItemInventorySchedule) {
            fileExtension = EConfigFileExtensions.SCHEDULE.extension();
            path = ((DBItemInventorySchedule) item).getName();
        }
        XMLWriter xmlWriter = null;
        Writer writer = null;
        try {
            Path filePath = liveDirectory.resolve(path.substring(1) + fileExtension);
            if (Files.exists(filePath)) {
                String xml = null;
                try {
                    xml = new String(Files.readAllBytes(filePath));
                } catch (IOException e) {
                    LOGGER.debug(String.format("Error: %1$s - occurred during reading file %2$s%3$s!", e.getMessage(), path, fileExtension));
                }
                if (!xml.isEmpty()) {
                    org.dom4j.Document document = DocumentHelper.parseText(xml);
                    document = InventoryRuntimeHelper.updateCalendarsCDATA(document, metaInfo);
                    OutputFormat format = OutputFormat.createPrettyPrint();
                    format.setEncoding(document.getXMLEncoding());
                    format.setXHTML(true);
                    format.setIndentSize(4);
                    format.setExpandEmptyElements(false);
                    try {
                        writer = new FileWriter(filePath.toFile());
                        xmlWriter = new XMLWriter(writer, format);
                        xmlWriter.write(document);
                        xmlWriter.flush();
                        xmlWriter.close();
                    } catch (IOException e) {
                        LOGGER.debug(String.format("Error: %1$s - occurred during update of file %2$s%3$s, file not updated!",
                                e.getMessage(), path, fileExtension));
                    } finally {
                        if (xmlWriter != null) {
                            try {
                                xmlWriter.close();
                            } catch (IOException e) {}
                        }
                    }
                }
            }
        } catch (DocumentException e1) {
            LOGGER.debug(String.format("Error: %1$s - occurred parsing configuration of file %2$s%3$s!", e1.getMessage(), path, fileExtension));
        } finally {
            try {
                if (xmlWriter != null) {
                    xmlWriter.close();
                } else {
                    writer.close();
                }
            } catch (Exception e) {
            }
        }

    }
}
