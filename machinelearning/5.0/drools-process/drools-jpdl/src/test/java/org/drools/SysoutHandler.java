package org.drools;

import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;

public class SysoutHandler implements ActionHandler {

	private static final long serialVersionUID = 1L;

	public void execute(ExecutionContext executionContext) throws Exception {
		System.out.println("Executing");
		executionContext.leaveNode();
	}

}
