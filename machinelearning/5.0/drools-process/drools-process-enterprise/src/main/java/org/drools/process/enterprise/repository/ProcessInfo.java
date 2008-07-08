package org.drools.process.enterprise.repository;

import java.io.IOException;
import java.io.StringReader;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.process.core.Process;
import org.drools.xml.XmlProcessReader;
import org.xml.sax.SAXException;

@Entity
public class ProcessInfo {

	private @Id String processId;
	private @Lob String processXML;
	private @Transient Process process;
	
	public String getProcessId() {
		return processId;
	}
	
	public void setProcessXML(String processId, String processXML) {
		this.processId = processId;
		this.processXML = processXML;
	}
	
	public Process getProcess() {
		if (process == null) {
	        try {
	        	PackageBuilderConfiguration configuration = new PackageBuilderConfiguration();
				process = new XmlProcessReader(configuration.getSemanticModules()).read(new StringReader(processXML));
	        } catch (IOException e) {
	        	throw new IllegalArgumentException(
					"IOException while loading process: " + e.getMessage());
	        } catch (SAXException e) {
	        	throw new IllegalArgumentException(
					"SAXException while loading process: " + e.getMessage());
	        }
		}
		return process;
	}
	
	public String getProcessXML() {
		return processXML;
	}
	
}
