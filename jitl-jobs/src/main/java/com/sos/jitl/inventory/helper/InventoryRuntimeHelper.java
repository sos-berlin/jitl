package com.sos.jitl.inventory.helper;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import org.dom4j.DocumentHelper;
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
import com.sos.joc.model.calendar.Dates;

public class InventoryRuntimeHelper {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryRuntimeHelper.class);
    
    public static void recalculateRuntime(DBLayerInventory inventoryDbLayer, DbItem item, List<DBItemInventoryClusterCalendarUsage> dbCalendarUsages,
            Path liveDirectory, String timezone) throws Exception {
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
        String xml = new String(Files.readAllBytes(filePath));
        org.dom4j.Document document = DocumentHelper.parseText(xml);
        // get collection of all runtimes with calendar attribute
        TreeSet<RuntimeCalendar> xmlRuntimes = RuntimeResolver.getCalendarDatesFromToday(document, timezone);
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
                                rc.setPeriods(calendarUsage.getPeriods());
                            }
                            Dates dates = null;
                            if (dbCalendarUsage.getConfiguration() != null && !dbCalendarUsage.getConfiguration().isEmpty()) {
                                dates = new FrequencyResolver().resolveRestrictionsFromToday(om.readValue(dbCalendar.getConfiguration(), Calendar.class), calendarUsage);
                            } else {
                                dates = new FrequencyResolver().resolveFromToday(om.readValue(dbCalendar.getConfiguration(), Calendar.class));
                            }
                            if (dates != null) {
                                rc.setDates(dates.getDates());
                            }
                            usageRuntimes.add(rc);
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
        if (Files.exists(filePath)) {
            if (!xmlRuntimes.equals(usageRuntimes)) {
                RuntimeResolver.updateCalendarInRuntimes(document, new FileWriter(filePath.toFile()), usageRuntimes);
            }
        }
    }
    
}
