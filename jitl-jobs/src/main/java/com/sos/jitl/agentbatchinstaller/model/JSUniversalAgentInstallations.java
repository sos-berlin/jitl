/*
 * Created on 28.02.2011 To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.sos.jitl.agentbatchinstaller.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.sos.jitl.agentbatchinstaller.model.installations.Installation;
import com.sos.jitl.agentbatchinstaller.model.installations.Installations;

public class JSUniversalAgentInstallations {

    private ArrayList<Installation> listOfInstallations;
    private File installationsDefinitionFile;
    private Iterator<Installation> jsInstallationsIterator;
    private Installations installations;

    public JSUniversalAgentInstallations(File installationsDefinitionFile_) throws Exception {
        super();
        this.installationsDefinitionFile = installationsDefinitionFile_;
        readInstallationDefinitionFile();
    }

    public void reset() {
        jsInstallationsIterator = listOfInstallations.iterator();
    }

    public JSUniversalAgentinstallation next() {
        Installation i = jsInstallationsIterator.next();
        JSUniversalAgentinstallation j = new JSUniversalAgentinstallation();
        j.globals = installations.getGlobals();
        j.setValues(i);
        return j;
    }

    public Installations getInstallations() {
        return installations;
    }

    public Installation nextInstallation() {
        Installation i = jsInstallationsIterator.next();
        return i;
    }

    public boolean eof() {
        return !jsInstallationsIterator.hasNext();
    }

    public void readInstallationDefinitionFile() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Installations.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        installations = (Installations) unmarshaller.unmarshal(installationsDefinitionFile);
        listOfInstallations = (ArrayList<Installation>) installations.getInstallation();
        reset();
    }

    public void writeFile(File output) throws JAXBException, ParseException, FileNotFoundException {
        JAXBContext context = JAXBContext.newInstance(Installations.class);

        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(installations, new FileOutputStream(output));

    }

}
