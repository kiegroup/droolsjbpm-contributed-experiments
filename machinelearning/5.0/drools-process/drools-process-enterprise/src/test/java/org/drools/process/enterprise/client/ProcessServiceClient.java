package org.drools.process.enterprise.client;

import javax.naming.InitialContext;

import org.drools.process.enterprise.processinstance.ProcessService;

public class ProcessServiceClient {

	public static void main(String[] args) throws Exception {
		InitialContext ctx = new InitialContext();
		ProcessService processService = (ProcessService)
			ctx.lookup("ProcessServiceBean/remote");

		System.out.println(processService.startProcess("com.sample.ruleflow"));
	}

}