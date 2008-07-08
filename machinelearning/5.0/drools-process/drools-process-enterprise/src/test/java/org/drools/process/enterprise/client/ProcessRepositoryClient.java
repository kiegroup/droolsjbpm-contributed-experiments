package org.drools.process.enterprise.client;

import javax.naming.InitialContext;

import org.drools.process.enterprise.repository.ProcessRepository;

public class ProcessRepositoryClient {

	public static void main(String[] args) throws Exception {
		InitialContext ctx = new InitialContext();
		ProcessRepository processRepository = (ProcessRepository)
			ctx.lookup("ProcessRepositoryBean/remote");
		String processXML = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<process xmlns=\"http://drools.org/drools-4.0/process\"\n" +
			"         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
			"         xs:schemaLocation=\"http://drools.org/drools-4.0/process drools-processes-4.0.xsd\"\n" +
			"         type=\"RuleFlow\" name=\"ruleflow\" id=\"com.sample.ruleflow\" package-name=\"com.sample\" version=\"1\">\n" +
			"\n" +
			"  <header>\n" +
			"  </header>\n" +
			"\n" +
			"  <nodes>\n" +
			"    <start id=\"1\" name=\"Start\" />\n" +
		    "    <action id=\"2\" name=\"java\" dialect=\"java\" >System.out.println(\"Executing action\");</action>\n" +
		    "	 <split id=\"3\" name=\"Split\" type=\"1\" />\n" +
		    "    <workItem id=\"4\" name=\"Log\" >\n" +
		    "      <work name=\"Log\" >\n" +
		    "        <parameter name=\"Message\" type=\"org.drools.process.core.datatype.impl.type.StringDataType\" >This is a log message !</parameter>\n" +
		    "      </work>\n" +
		    "    </workItem>\n" +
		    "    <workItem id=\"5\" name=\"Human Task\" >\n" +
		    "      <work name=\"Human Task\" >\n" +
		    "        <parameter name=\"Priority\" type=\"org.drools.process.core.datatype.impl.type.StringDataType\" >HIGH</parameter>\n" +
		    "        <parameter name=\"TaskName\" type=\"org.drools.process.core.datatype.impl.type.StringDataType\" >Phone customer</parameter>\n" +
		    "        <parameter name=\"Comment\" type=\"org.drools.process.core.datatype.impl.type.StringDataType\" >This is a comment !</parameter>\n" +
		    "        <parameter name=\"ActorId\" type=\"org.drools.process.core.datatype.impl.type.StringDataType\" />\n" +
		    "      </work>\n" +
		    "    </workItem>\n" +
		    "    <join id=\"6\" name=\"Join\" type=\"1\" />\n" +
		    "    <end id=\"7\" name=\"End\" />\n" +
			"  </nodes>\n" +
			"\n" +
			"  <connections>\n" +
			"    <connection from=\"1\" to=\"2\"/>\n" +
			"    <connection from=\"2\" to=\"3\"/>\n" +
			"    <connection from=\"3\" to=\"4\"/>\n" +
			"    <connection from=\"3\" to=\"5\"/>\n" +
			"    <connection from=\"4\" to=\"6\"/>\n" +
			"    <connection from=\"5\" to=\"6\"/>\n" +
			"    <connection from=\"6\" to=\"7\"/>\n" +
			"  </connections>\n" +
			"\n" +
			"</process>";
		
		processRepository.storeProcess("com.sample.ruleflow", processXML);
	}

}