//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.11.12 um 03:09:08 PM CET 
//


package com.sos.jitl.agentbatchinstaller.model.installations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}java_home" minOccurs="0"/>
 *         &lt;element ref="{}java_options" minOccurs="0"/>
 *         &lt;element ref="{}scheduler_home" minOccurs="0"/>
 *         &lt;element ref="{}scheduler_http_port" minOccurs="0"/>
 *         &lt;element ref="{}scheduler_ip_address" minOccurs="0"/>
 *         &lt;element ref="{}scheduler_user" minOccurs="0"/>
 *         &lt;element ref="{}scheduler_log_dir" minOccurs="0"/>
 *         &lt;element ref="{}scheduler_kill_script" minOccurs="0"/>
 *         &lt;element ref="{}scheduler_pid_file_dir" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "javaHome",
    "javaOptions",
    "schedulerHome",
    "schedulerHttpPort",
    "schedulerIpAddress",
    "schedulerUser",
    "schedulerLogDir",
    "schedulerKillScript",
    "schedulerPidFileDir"
})
@XmlRootElement(name = "agent_options")
public class AgentOptions {

    @XmlElement(name = "java_home")
    protected String javaHome;
    @XmlElement(name = "java_options")
    protected String javaOptions;
    @XmlElement(name = "scheduler_home")
    protected String schedulerHome;
    @XmlElement(name = "scheduler_http_port")
    protected Integer schedulerHttpPort;
    @XmlElement(name = "scheduler_ip_address")
    protected String schedulerIpAddress;
    @XmlElement(name = "scheduler_user")
    protected String schedulerUser;
    @XmlElement(name = "scheduler_log_dir")
    protected String schedulerLogDir;
    @XmlElement(name = "scheduler_kill_script")
    protected String schedulerKillScript;
    @XmlElement(name = "scheduler_pid_file_dir")
    protected String schedulerPidFileDir;

    /**
     * Ruft den Wert der javaHome-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJavaHome() {
        return javaHome;
    }

    /**
     * Legt den Wert der javaHome-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJavaHome(String value) {
        this.javaHome = value;
    }

    /**
     * Ruft den Wert der javaOptions-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJavaOptions() {
        return javaOptions;
    }

    /**
     * Legt den Wert der javaOptions-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJavaOptions(String value) {
        this.javaOptions = value;
    }

    /**
     * Ruft den Wert der schedulerHome-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchedulerHome() {
        return schedulerHome;
    }

    /**
     * Legt den Wert der schedulerHome-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchedulerHome(String value) {
        this.schedulerHome = value;
    }

    /**
     * Ruft den Wert der schedulerHttpPort-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSchedulerHttpPort() {
        return schedulerHttpPort;
    }

    /**
     * Legt den Wert der schedulerHttpPort-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSchedulerHttpPort(Integer value) {
        this.schedulerHttpPort = value;
    }

    /**
     * Ruft den Wert der schedulerIpAddress-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchedulerIpAddress() {
        return schedulerIpAddress;
    }

    /**
     * Legt den Wert der schedulerIpAddress-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchedulerIpAddress(String value) {
        this.schedulerIpAddress = value;
    }

    /**
     * Ruft den Wert der schedulerUser-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchedulerUser() {
        return schedulerUser;
    }

    /**
     * Legt den Wert der schedulerUser-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchedulerUser(String value) {
        this.schedulerUser = value;
    }

    /**
     * Ruft den Wert der schedulerLogDir-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchedulerLogDir() {
        return schedulerLogDir;
    }

    /**
     * Legt den Wert der schedulerLogDir-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchedulerLogDir(String value) {
        this.schedulerLogDir = value;
    }

    /**
     * Ruft den Wert der schedulerKillScript-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchedulerKillScript() {
        return schedulerKillScript;
    }

    /**
     * Legt den Wert der schedulerKillScript-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchedulerKillScript(String value) {
        this.schedulerKillScript = value;
    }

    /**
     * Ruft den Wert der schedulerPidFileDir-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchedulerPidFileDir() {
        return schedulerPidFileDir;
    }

    /**
     * Legt den Wert der schedulerPidFileDir-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchedulerPidFileDir(String value) {
        this.schedulerPidFileDir = value;
    }

}
