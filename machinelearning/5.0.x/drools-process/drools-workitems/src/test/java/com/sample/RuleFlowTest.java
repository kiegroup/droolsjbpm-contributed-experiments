package com.sample;

import java.io.InputStreamReader;
import java.io.Reader;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;
import org.drools.audit.WorkingMemoryFileLogger;
import org.drools.compiler.PackageBuilder;
import org.drools.process.instance.WorkItemHandler;
import org.drools.process.workitem.archive.ArchiveWorkItemHandler;
import org.drools.process.workitem.email.EmailWorkItemHandler;
import org.drools.process.workitem.exec.ExecWorkItemHandler;
import org.drools.process.workitem.finder.FinderWorkItemHandler;
import org.drools.process.workitem.transform.FileTransformer;
import org.drools.process.workitem.transform.TransformWorkItemHandler;
import org.drools.rule.Package;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;

/**
 * This is a sample file to launch a ruleflow.
 */
public class RuleFlowTest {

	public static final void main(String[] args) {
		try {
			
			RuleBase ruleBase = createKnowledgeBase();
			StatefulSession session = ruleBase.newStatefulSession();
			session.getWorkItemManager().registerWorkItemHandler("Finder", new FinderWorkItemHandler());
			session.getWorkItemManager().registerWorkItemHandler("Archive", new ArchiveWorkItemHandler());
			session.getWorkItemManager().registerWorkItemHandler("Exec", new ExecWorkItemHandler());
			session.getWorkItemManager().registerWorkItemHandler("Log", new WorkItemHandler() {
				public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
					System.out.println("Log: " + workItem.getParameter("Message"));
					manager.completeWorkItem(workItem.getId(), null);
				}
				public void abortWorkItem(WorkItem arg0, WorkItemManager arg1) {
				}
			});
			EmailWorkItemHandler emailWorkItemHandler = new EmailWorkItemHandler();
			emailWorkItemHandler.setConnection("mail-out.example.com", "25", null, null);
			session.getWorkItemManager().registerWorkItemHandler("Email", emailWorkItemHandler);
			TransformWorkItemHandler transformWorkItemHandler = new TransformWorkItemHandler();
			transformWorkItemHandler.registerTransformer(FileTransformer.class);
			session.getWorkItemManager().registerWorkItemHandler("Transform", transformWorkItemHandler);
			WorkingMemoryFileLogger logger = new WorkingMemoryFileLogger(session);
			session.startProcess("com.sample.ruleflow");
			logger.writeToDisk();
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private static RuleBase createKnowledgeBase() throws Exception {
		PackageBuilder builder = new PackageBuilder();
		Reader source = new InputStreamReader(
			RuleFlowTest.class.getResourceAsStream("/FileFinder.rf"));
		builder.addProcessFromXml(source);
		Package pkg = builder.getPackage();
		RuleBase ruleBase = RuleBaseFactory.newRuleBase();
		ruleBase.addPackage(pkg);
		return ruleBase;
	}

}