//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.11.26 um 11:18:13 AM CET 
//


package com.sos.jitl.agentbatchinstaller.model.installations;

import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element ref="{}globals"/>
 *         &lt;element ref="{}installation" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="lastRun" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "globals",
    "installation"
})
@XmlRootElement(name = "installations")
public class Installations {

    @XmlElement(required = true)
    protected Globals globals;
    @XmlElement(required = true)
    protected List<Installation> installation;
    @XmlAttribute(name = "lastRun")
    protected String lastRun;

    /**
     * Ruft den Wert der globals-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Globals }
     *     
     */
    public Globals getGlobals() {
        return globals;
    }

    /**
     * Legt den Wert der globals-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Globals }
     *     
     */
    public void setGlobals(Globals value) {
        this.globals = value;
    }

    /**
     * Gets the value of the installation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the installation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInstallation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Installation }
     * 
     * 
     */
    public List<Installation> getInstallation() {
        if (installation == null) {
            installation = new ArrayList<Installation>();
        }
        return this.installation;
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
