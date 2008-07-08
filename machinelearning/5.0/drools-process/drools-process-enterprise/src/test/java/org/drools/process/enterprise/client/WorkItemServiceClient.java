package org.drools.process.enterprise.client;

import javax.naming.InitialContext;

import org.drools.process.enterprise.processinstance.ProcessService;

public class WorkItemServiceClient {

	public static void main(String[] args) throws Exception {
		InitialContext ctx = new InitialContext();
		ProcessService processService = (ProcessService)
			ctx.lookup("ProcessServiceBean/remote");

		processService.completeWorkItem(2, null);
	}

}