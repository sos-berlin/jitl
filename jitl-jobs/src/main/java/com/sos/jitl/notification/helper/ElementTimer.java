package com.sos.jitl.notification.helper;

import java.util.ArrayList;
import java.util.Date;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ElementTimer {

    private Node xml;

    private String name;
    private ArrayList<ElementTimerJobChain> jobChains;
    private ArrayList<ElementTimerJob> jobs;
    private ElementTimerScript minimum;
    private ElementTimerScript maximum;
    private TimerResult result;

    public ElementTimer(Node timer) throws Exception {

        xml = timer;
        Element el = (Element) this.xml;
        name = NotificationXmlHelper.getTimerName(el);

        jobChains = new ArrayList<ElementTimerJobChain>();
        NodeList nlJobChains = NotificationXmlHelper.selectTimerJobChains(xml);
        for (int i = 0; i < nlJobChains.getLength(); i++) {
            jobChains.add(new ElementTimerJobChain(this, nlJobChains.item(i)));
        }

        jobs = new ArrayList<ElementTimerJob>();
        NodeList nlJobs = NotificationXmlHelper.selectTimerJobs(xml);
        for (int i = 0; i < nlJobs.getLength(); i++) {
            jobs.add(new ElementTimerJob(this, nlJobs.item(i)));
        }

        XPath xPath = XPathFactory.newInstance().newXPath();
        maximum = NotificationXmlHelper.getTimerMaximum(xPath, xml, name);
        minimum = NotificationXmlHelper.getTimerMinimum(xPath, xml, name);

    }

    public Node getXml() {
        return this.xml;
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<ElementTimerJobChain> getJobChains() {
        return this.jobChains;
    }

    public ArrayList<ElementTimerJob> getJobs() {
        return this.jobs;
    }

    public ElementTimerScript getMinimum() {
        return this.minimum;
    }

    public ElementTimerScript getMaximum() {
        return this.maximum;
    }

    public TimerResult getTimerResult() {
        return result;
    }

    public void createTimerResult() {
        this.result = new TimerResult();
    }

    public void resetTimerResult() {
        this.result = null;
    }

    public class TimerResult {

        private Double minimum = null;
        private Double maximum = null;
        private StringBuffer resultIds = null;
        Date startTime = null;
        Date endTime = null;
        EStartTimeType startTimeType = null;
        EEndTimeType endTimeType = null;

        public Double getMinimum() {
            return minimum;
        }

        public void setMinimum(Double val) {
            this.minimum = val;
        }

        public Double getMaximum() {
            return maximum;
        }

        public void setMaximum(Double val) {
            this.maximum = val;
        }

        public StringBuffer getResultIds() {
            return resultIds;
        }

        public void setResultIds(StringBuffer val) {
            this.resultIds = val;
        }

        public Date getStartTime() {
            return startTime;
        }

        public void setStartTime(Date val) {
            this.startTime = val;
        }

        public Date getEndTime() {
            return endTime;
        }

        public void setEndTime(Date val) {
            this.endTime = val;
        }

        public EStartTimeType getStartTimeType() {
            return startTimeType;
        }

        public void setStartTimeType(EStartTimeType val) {
            this.startTimeType = val;
        }

        public EEndTimeType getEndTimeType() {
            return endTimeType;
        }

        public void setEndTimeType(EEndTimeType val) {
            this.endTimeType = val;
        }

        public Long getTimeDifferenceInSeconds() {
            if (startTime == null || endTime == null) {
                return new Long(-1);
            }
            return endTime.getTime() / 1000 - startTime.getTime() / 1000;
        }
    }

}
