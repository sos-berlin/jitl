package com.sos.jitl.jobstreams.classes;

import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.jitl.jobstreams.db.DBItemJobStreamStarter;
import com.sos.joc.model.joe.schedule.RunTime;

public class JSStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSStarter.class);
    private DBItemJobStreamStarter itemJobStreamStarter;
    private Date nextStart;
    private JobStreamScheduler jobStreamScheduler;
    private ObjectMapper objectMapper;
 

    public JSStarter(ObjectMapper objectMapper) {
        super();
        this.objectMapper = objectMapper;
    }

    public DBItemJobStreamStarter getItemJobStreamStarter() {
        return itemJobStreamStarter;
    }

    public RunTime getRunTime() throws JsonParseException, JsonMappingException, IOException {
         if (itemJobStreamStarter.getRunTime() != null) {
            return objectMapper.readValue(itemJobStreamStarter.getRunTime(), RunTime.class);
        }
        return null;
    }

    public void setItemJobStreamStarter(Date from, Date to, DBItemJobStreamStarter itemJobStreamStarter, String timeZoneId) throws Exception {
        this.itemJobStreamStarter = itemJobStreamStarter;
        jobStreamScheduler = new JobStreamScheduler(timeZoneId);
        RunTime runtime = this.getRunTime();
        if (runtime != null) {
            LOGGER.debug("schedule for:" + this.itemJobStreamStarter.getStarterName());
            jobStreamScheduler.schedule(from,to,runtime, false);
        }
    }
  
    public JobStreamScheduler getJobStreamScheduler() {
        return jobStreamScheduler;
    }

    public void setNextStart(Date nextStart) {
        this.nextStart = nextStart;
    }

    public Date getNextStart() {
        return nextStart;
    }
 

}
