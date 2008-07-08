package org.drools;

import junit.framework.TestCase;

import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;

public class SimpleProcessTest extends TestCase {

	public void testSimpleProcess() throws Exception {

		// Extract a process definition from the processdefinition.xml file.
		ProcessDefinition processDefinition = ProcessDefinition.parseXmlResource("simple/processdefinition.xml");
		assertNotNull("Definition should not be null", processDefinition);

		// Create an instance of the process definition.
		ProcessInstance instance = new ProcessInstance(processDefinition);
		assertEquals(
				"Instance is in start state", 
				instance.getRootToken().getNode().getName(), 
				"start");
		assertNull(
				"Message variable should not exist yet", 
				instance.getContextInstance().getVariable("message"));

		instance.signal();
		assertEquals(
				"Instance is in node1", 
				instance.getRootToken().getNode().getName(), 
				"node1");
		
		instance.signal();
		assertEquals(
				"Instance is in end state", 
				instance.getRootToken().getNode().getName(), 
				"end");
		assertTrue("Instance has ended", instance.hasEnded());
	}

}
