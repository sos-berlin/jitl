//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.8-b130911.1802 generiert
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas
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
 * Java-Klasse für anonymous complex type.
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
 *         &lt;element ref="{}user" minOccurs="0"/>
 *         &lt;element ref="{}password" minOccurs="0"/>
 *         &lt;element ref="{}sudo_password" minOccurs="0"/>
 *         &lt;element ref="{}auth_method" minOccurs="0"/>
 *         &lt;element ref="{}auth_file" minOccurs="0"/>
 *         &lt;element ref="{}host" minOccurs="0"/>
 *         &lt;element ref="{}port" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre> */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "user", "password", "sudoPassword", "authMethod", "authFile", "host", "port" })
@XmlRootElement(name = "ssh")
public class Ssh {

    protected String user;
    protected String password;
    @XmlElement(name = "sudo_password")
    protected String sudoPassword;
    @XmlElement(name = "auth_method")
    protected String authMethod;
    @XmlElement(name = "auth_file")
    protected String authFile;
    protected String host;
    protected String port;

    /** Ruft den Wert der user-Eigenschaft ab.
     * 
     * @return possible object is {@link String } */
    public String getUser() {
        return user;
    }

    /** Legt den Wert der user-Eigenschaft fest.
     * 
     * @param value allowed object is {@link String } */
    public void setUser(String value) {
        this.user = value;
    }

    /** Ruft den Wert der password-Eigenschaft ab.
     * 
     * @return possible object is {@link String } */
    public String getPassword() {
        return password;
    }

    /** Legt den Wert der password-Eigenschaft fest.
     * 
     * @param value allowed object is {@link String } */
    public void setPassword(String value) {
        this.password = value;
    }

    /** Ruft den Wert der sudoPassword-Eigenschaft ab.
     * 
     * @return possible object is {@link String } */
    public String getSudoPassword() {
        return sudoPassword;
    }

    /** Legt den Wert der sudoPassword-Eigenschaft fest.
     * 
     * @param value allowed object is {@link String } */
    public void setSudoPassword(String value) {
        this.sudoPassword = value;
    }

    /** Ruft den Wert der authMethod-Eigenschaft ab.
     * 
     * @return possible object is {@link String } */
    public String getAuthMethod() {
        return authMethod;
    }

    /** Legt den Wert der authMethod-Eigenschaft fest.
     * 
     * @param value allowed object is {@link String } */
    public void setAuthMethod(String value) {
        this.authMethod = value;
    }

    /** Ruft den Wert der authFile-Eigenschaft ab.
     * 
     * @return possible object is {@link String } */
    public String getAuthFile() {
        return authFile;
    }

    /** Legt den Wert der authFile-Eigenschaft fest.
     * 
     * @param value allowed object is {@link String } */
    public void setAuthFile(String value) {
        this.authFile = value;
    }

    /** Ruft den Wert der host-Eigenschaft ab.
     * 
     * @return possible object is {@link String } */
    public String getHost() {
        return host;
    }

    /** Legt den Wert der host-Eigenschaft fest.
     * 
     * @param value allowed object is {@link String } */
    public void setHost(String value) {
        this.host = value;
    }

    /** Ruft den Wert der port-Eigenschaft ab.
     * 
     * @return possible object is {@link String } */
    public String getPort() {
        return port;
    }

    /** Legt den Wert der port-Eigenschaft fest.
     * 
     * @param value allowed object is {@link String } */
    public void setPort(String value) {
        this.port = value;
    }

}
