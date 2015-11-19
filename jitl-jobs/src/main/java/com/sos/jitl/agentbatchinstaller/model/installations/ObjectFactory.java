//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.11.19 um 04:46:51 PM CET 
//


package com.sos.jitl.agentbatchinstaller.model.installations;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.sos.jitl.agentbatchinstaller.model.installations package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _SudoPassword_QNAME = new QName("", "sudo_password");
    private final static QName _InstallPath_QNAME = new QName("", "install_path");
    private final static QName _Dir_QNAME = new QName("", "dir");
    private final static QName _SchedulerWorkDir_QNAME = new QName("", "scheduler_work_dir");
    private final static QName _Protocol_QNAME = new QName("", "protocol");
    private final static QName _Password_QNAME = new QName("", "password");
    private final static QName _FileSpec_QNAME = new QName("", "file_spec");
    private final static QName _SchedulerPidFileDir_QNAME = new QName("", "scheduler_pid_file_dir");
    private final static QName _JavaOptions_QNAME = new QName("", "java_options");
    private final static QName _SchedulerHttpPort_QNAME = new QName("", "scheduler_http_port");
    private final static QName _SchedulerLogDir_QNAME = new QName("", "scheduler_log_dir");
    private final static QName _Host_QNAME = new QName("", "host");
    private final static QName _SchedulerKillScript_QNAME = new QName("", "scheduler_kill_script");
    private final static QName _Settings_QNAME = new QName("", "settings");
    private final static QName _SshAuthFile_QNAME = new QName("", "ssh_auth_file");
    private final static QName _SshAuthMethod_QNAME = new QName("", "ssh_auth_method");
    private final static QName _JavaHome_QNAME = new QName("", "java_home");
    private final static QName _Profile_QNAME = new QName("", "profile");
    private final static QName _Command_QNAME = new QName("", "command");
    private final static QName _AuthMethod_QNAME = new QName("", "auth_method");
    private final static QName _Port_QNAME = new QName("", "port");
    private final static QName _SchedulerIpAddress_QNAME = new QName("", "scheduler_ip_address");
    private final static QName _SchedulerUser_QNAME = new QName("", "scheduler_user");
    private final static QName _Operation_QNAME = new QName("", "operation");
    private final static QName _User_QNAME = new QName("", "user");
    private final static QName _AuthFile_QNAME = new QName("", "auth_file");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.sos.jitl.agentbatchinstaller.model.installations
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Postprocessing }
     * 
     */
    public Postprocessing createPostprocessing() {
        return new Postprocessing();
    }

    /**
     * Create an instance of {@link Globals }
     * 
     */
    public Globals createGlobals() {
        return new Globals();
    }

    /**
     * Create an instance of {@link Transfer }
     * 
     */
    public Transfer createTransfer() {
        return new Transfer();
    }

    /**
     * Create an instance of {@link Source }
     * 
     */
    public Source createSource() {
        return new Source();
    }

    /**
     * Create an instance of {@link Target }
     * 
     */
    public Target createTarget() {
        return new Target();
    }

    /**
     * Create an instance of {@link Ssh }
     * 
     */
    public Ssh createSsh() {
        return new Ssh();
    }

    /**
     * Create an instance of {@link Installations }
     * 
     */
    public Installations createInstallations() {
        return new Installations();
    }

    /**
     * Create an instance of {@link Installation }
     * 
     */
    public Installation createInstallation() {
        return new Installation();
    }

    /**
     * Create an instance of {@link AgentOptions }
     * 
     */
    public AgentOptions createAgentOptions() {
        return new AgentOptions();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "sudo_password")
    public JAXBElement<String> createSudoPassword(String value) {
        return new JAXBElement<String>(_SudoPassword_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "install_path")
    public JAXBElement<String> createInstallPath(String value) {
        return new JAXBElement<String>(_InstallPath_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "dir")
    public JAXBElement<String> createDir(String value) {
        return new JAXBElement<String>(_Dir_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "scheduler_work_dir")
    public JAXBElement<String> createSchedulerWorkDir(String value) {
        return new JAXBElement<String>(_SchedulerWorkDir_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "protocol")
    public JAXBElement<String> createProtocol(String value) {
        return new JAXBElement<String>(_Protocol_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "password")
    public JAXBElement<String> createPassword(String value) {
        return new JAXBElement<String>(_Password_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "file_spec")
    public JAXBElement<String> createFileSpec(String value) {
        return new JAXBElement<String>(_FileSpec_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "scheduler_pid_file_dir")
    public JAXBElement<String> createSchedulerPidFileDir(String value) {
        return new JAXBElement<String>(_SchedulerPidFileDir_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "java_options")
    public JAXBElement<String> createJavaOptions(String value) {
        return new JAXBElement<String>(_JavaOptions_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "scheduler_http_port")
    public JAXBElement<Integer> createSchedulerHttpPort(Integer value) {
        return new JAXBElement<Integer>(_SchedulerHttpPort_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "scheduler_log_dir")
    public JAXBElement<String> createSchedulerLogDir(String value) {
        return new JAXBElement<String>(_SchedulerLogDir_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "host")
    public JAXBElement<String> createHost(String value) {
        return new JAXBElement<String>(_Host_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "scheduler_kill_script")
    public JAXBElement<String> createSchedulerKillScript(String value) {
        return new JAXBElement<String>(_SchedulerKillScript_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "settings")
    public JAXBElement<String> createSettings(String value) {
        return new JAXBElement<String>(_Settings_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ssh_auth_file")
    public JAXBElement<String> createSshAuthFile(String value) {
        return new JAXBElement<String>(_SshAuthFile_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ssh_auth_method")
    public JAXBElement<String> createSshAuthMethod(String value) {
        return new JAXBElement<String>(_SshAuthMethod_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "java_home")
    public JAXBElement<String> createJavaHome(String value) {
        return new JAXBElement<String>(_JavaHome_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "profile")
    public JAXBElement<String> createProfile(String value) {
        return new JAXBElement<String>(_Profile_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "command")
    public JAXBElement<String> createCommand(String value) {
        return new JAXBElement<String>(_Command_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "auth_method")
    public JAXBElement<String> createAuthMethod(String value) {
        return new JAXBElement<String>(_AuthMethod_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "port")
    public JAXBElement<String> createPort(String value) {
        return new JAXBElement<String>(_Port_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "scheduler_ip_address")
    public JAXBElement<String> createSchedulerIpAddress(String value) {
        return new JAXBElement<String>(_SchedulerIpAddress_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "scheduler_user")
    public JAXBElement<String> createSchedulerUser(String value) {
        return new JAXBElement<String>(_SchedulerUser_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "operation")
    public JAXBElement<String> createOperation(String value) {
        return new JAXBElement<String>(_Operation_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "user")
    public JAXBElement<String> createUser(String value) {
        return new JAXBElement<String>(_User_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "auth_file")
    public JAXBElement<String> createAuthFile(String value) {
        return new JAXBElement<String>(_AuthFile_QNAME, String.class, null, value);
    }

}
