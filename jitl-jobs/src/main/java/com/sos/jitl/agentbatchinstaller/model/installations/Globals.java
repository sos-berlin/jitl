//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.8-b130911.1802 generiert
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// �nderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas
// verloren.
// Generiert: 2015.11.26 um 11:18:13 AM CET
//

package com.sos.jitl.agentbatchinstaller.model.installations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/** <p>
 * Java-Klasse f�r anonymous complex type.
 * 
 * <p>
 * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser
 * Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}install_path" minOccurs="0"/>
 *         &lt;element ref="{}transfer" minOccurs="0"/>
 *         &lt;element ref="{}ssh" minOccurs="0"/>
 *         &lt;element ref="{}postprocessing" minOccurs="0"/>
 *         &lt;element ref="{}installation_file" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre> */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "installPath", "transfer", "ssh", "postprocessing", "installationFile" })
@XmlRootElement(name = "globals")
public class Globals {

    @XmlElement(name = "install_path")
    protected String installPath;
    protected Transfer transfer;
    protected Ssh ssh;
    protected Postprocessing postprocessing;
    @XmlElement(name = "installation_file")
    protected String installationFile;

    /** Ruft den Wert der installPath-Eigenschaft ab.
     * 
     * @return possible object is {@link String } */
    public String getInstallPath() {
        return installPath;
    }

    /** Legt den Wert der installPath-Eigenschaft fest.
     * 
     * @param value allowed object is {@link String } */
    public void setInstallPath(String value) {
        this.installPath = value;
    }

    /** Ruft den Wert der transfer-Eigenschaft ab.
     * 
     * @return possible object is {@link Transfer } */
    public Transfer getTransfer() {
        return transfer;
    }

    /** Legt den Wert der transfer-Eigenschaft fest.
     * 
     * @param value allowed object is {@link Transfer } */
    public void setTransfer(Transfer value) {
        this.transfer = value;
    }

    /** Ruft den Wert der ssh-Eigenschaft ab.
     * 
     * @return possible object is {@link Ssh } */
    public Ssh getSsh() {
        return ssh;
    }

    /** Legt den Wert der ssh-Eigenschaft fest.
     * 
     * @param value allowed object is {@link Ssh } */
    public void setSsh(Ssh value) {
        this.ssh = value;
    }

    /** Ruft den Wert der postprocessing-Eigenschaft ab.
     * 
     * @return possible object is {@link Postprocessing } */
    public Postprocessing getPostprocessing() {
        return postprocessing;
    }

    /** Legt den Wert der postprocessing-Eigenschaft fest.
     * 
     * @param value allowed object is {@link Postprocessing } */
    public void setPostprocessing(Postprocessing value) {
        this.postprocessing = value;
    }

    /** Ruft den Wert der installationFile-Eigenschaft ab.
     * 
     * @return possible object is {@link String } */
    public String getInstallationFile() {
        return installationFile;
    }

    /** Legt den Wert der installationFile-Eigenschaft fest.
     * 
     * @param value allowed object is {@link String } */
    public void setInstallationFile(String value) {
        this.installationFile = value;
    }

}
