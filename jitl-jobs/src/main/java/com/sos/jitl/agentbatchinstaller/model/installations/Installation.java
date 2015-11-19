//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.11.19 um 04:46:51 PM CET 
//


package com.sos.jitl.agentbatchinstaller.model.installations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *         &lt;element ref="{}install_path" minOccurs="0"/>
 *         &lt;element ref="{}agent_options" minOccurs="0"/>
 *         &lt;element ref="{}transfer" minOccurs="0"/>
 *         &lt;element ref="{}ssh" minOccurs="0"/>
 *         &lt;element ref="{}postprocessing" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="lastRun" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "installPath",
    "agentOptions",
    "transfer",
    "ssh",
    "postprocessing"
})
@XmlRootElement(name = "installation")
public class Installation {

    @XmlElement(name = "install_path")
    protected String installPath;
    @XmlElement(name = "agent_options")
    protected AgentOptions agentOptions;
    protected Transfer transfer;
    protected Ssh ssh;
    protected Postprocessing postprocessing;
    @XmlAttribute(name = "lastRun", required = true)
    protected String lastRun;

    /**
     * Ruft den Wert der installPath-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInstallPath() {
        return installPath;
    }

    /**
     * Legt den Wert der installPath-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInstallPath(String value) {
        this.installPath = value;
    }

    /**
     * Ruft den Wert der agentOptions-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AgentOptions }
     *     
     */
    public AgentOptions getAgentOptions() {
        return agentOptions;
    }

    /**
     * Legt den Wert der agentOptions-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AgentOptions }
     *     
     */
    public void setAgentOptions(AgentOptions value) {
        this.agentOptions = value;
    }

    /**
     * Ruft den Wert der transfer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Transfer }
     *     
     */
    public Transfer getTransfer() {
        return transfer;
    }

    /**
     * Legt den Wert der transfer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Transfer }
     *     
     */
    public void setTransfer(Transfer value) {
        this.transfer = value;
    }

    /**
     * Ruft den Wert der ssh-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Ssh }
     *     
     */
    public Ssh getSsh() {
        return ssh;
    }

    /**
     * Legt den Wert der ssh-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Ssh }
     *     
     */
    public void setSsh(Ssh value) {
        this.ssh = value;
    }

    /**
     * Ruft den Wert der postprocessing-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Postprocessing }
     *     
     */
    public Postprocessing getPostprocessing() {
        return postprocessing;
    }

    /**
     * Legt den Wert der postprocessing-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Postprocessing }
     *     
     */
    public void setPostprocessing(Postprocessing value) {
        this.postprocessing = value;
    }

    /**
     * Ruft den Wert der lastRun-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastRun() {
        return lastRun;
    }

    /**
     * Legt den Wert der lastRun-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastRun(String value) {
        this.lastRun = value;
    }

}
