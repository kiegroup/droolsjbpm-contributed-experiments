package org.drools.persistence.session;

import java.util.Collection;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;

import org.drools.RuleBase;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseFactory;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.ProcessBuilder;
import org.drools.process.command.CompleteWorkItemCommand;
import org.drools.process.command.GetProcessInstanceCommand;
import org.drools.process.command.StartProcessCommand;
import org.drools.process.core.Work;
import org.drools.process.core.impl.WorkImpl;
import org.drools.process.core.timer.Timer;
import org.drools.rule.Package;
import org.drools.ruleflow.core.RuleFlowProcess;
import org.drools.ruleflow.instance.RuleFlowProcessInstance;
import org.drools.runtime.process.NodeInstance;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkItem;
import org.drools.workflow.core.Node;
import org.drools.workflow.core.impl.ConnectionImpl;
import org.drools.workflow.core.impl.DroolsConsequenceAction;
import org.drools.workflow.core.node.ActionNode;
import org.drools.workflow.core.node.EndNode;
import org.drools.workflow.core.node.StartNode;
import org.drools.workflow.core.node.SubProcessNode;
import org.drools.workflow.core.node.TimerNode;
import org.drools.workflow.core.node.WorkItemNode;
import org.drools.workflow.instance.node.SubProcessNodeInstance;

public class SingleSessionCommandServiceTest extends TestCase {
	
	private EntityManagerFactory emf;
	
	protected void setUp() {
		emf = Persistence.createEntityManagerFactory("org.drools.persistence.jpa");
	}
	
	protected void tearDown() {
        emf.close();
    }
	
	public void testPersistenceWorkItems() {
		Properties properties = new Properties();
		properties.put(
	        "processInstanceManagerFactory", 
	        "org.drools.persistence.processinstance.JPAProcessInstanceManagerFactory");
		properties.put(
	        "workItemManagerFactory", 
	        "org.drools.persistence.processinstance.JPAWorkItemManagerFactory");
		properties.put(
	        "processSignalManagerFactory", 
	        "org.drools.persistence.processinstance.JPASignalManagerFactory");
        RuleBaseConfiguration conf = new RuleBaseConfiguration(properties);
        RuleBase ruleBase = RuleBaseFactory.newRuleBase(conf);
        Package pkg = getProcessWorkItems();
        ruleBase.addPackage(pkg);

        SingleSessionCommandService service = new SingleSessionCommandService(ruleBase);
        StartProcessCommand startProcessCommand = new StartProcessCommand();
        startProcessCommand.setProcessId("org.drools.test.TestProcess");
        ProcessInstance processInstance = (ProcessInstance) service.execute(startProcessCommand);
        System.out.println("Started process instance " + processInstance.getId());
        
        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        assertNotNull(workItem);
        
        service = new SingleSessionCommandService(ruleBase);
        GetProcessInstanceCommand getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId(processInstance.getId());
        processInstance = (ProcessInstance) service.execute(getProcessInstanceCommand);
        assertNotNull(processInstance);
        
        service = new SingleSessionCommandService(ruleBase);
        CompleteWorkItemCommand completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId(workItem.getId());
        service.execute(completeWorkItemCommand);

        workItem = handler.getWorkItem();
        assertNotNull(workItem);
        
        service = new SingleSessionCommandService(ruleBase);
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId(processInstance.getId());
        processInstance = (ProcessInstance) service.execute(getProcessInstanceCommand);
        assertNotNull(processInstance);
        
        service = new SingleSessionCommandService(ruleBase);
        completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId(workItem.getId());
        service.execute(completeWorkItemCommand);
        
        workItem = handler.getWorkItem();
        assertNotNull(workItem);
        
        service = new SingleSessionCommandService(ruleBase);
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId(processInstance.getId());
        processInstance = (ProcessInstance) service.execute(getProcessInstanceCommand);
        assertNotNull(processInstance);
        
        service = new SingleSessionCommandService(ruleBase);
        completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId(workItem.getId());
        service.execute(completeWorkItemCommand);

        workItem = handler.getWorkItem();
        assertNull(workItem);
        
        service = new SingleSessionCommandService(ruleBase);
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId(processInstance.getId());
        processInstance = (ProcessInstance) service.execute(getProcessInstanceCommand);
        assertNull(processInstance);
	}

    private Package getProcessWorkItems() {
    	RuleFlowProcess process = new RuleFlowProcess();
    	process.setId("org.drools.test.TestProcess");
    	process.setName("TestProcess");
    	process.setPackageName("org.drools.test");
    	StartNode start = new StartNode();
    	start.setId(1);
    	start.setName("Start");
    	process.addNode(start);
    	ActionNode actionNode = new ActionNode();
    	actionNode.setId(2);
    	actionNode.setName("Action");
    	DroolsConsequenceAction action = new DroolsConsequenceAction();
    	action.setDialect("java");
    	action.setConsequence("System.out.println(\"Executed action\");");
    	actionNode.setAction(action);
    	process.addNode(actionNode);
    	new ConnectionImpl(start, Node.CONNECTION_DEFAULT_TYPE, actionNode, Node.CONNECTION_DEFAULT_TYPE);
    	WorkItemNode workItemNode = new WorkItemNode();
    	workItemNode.setId(3);
    	workItemNode.setName("WorkItem1");
    	Work work = new WorkImpl();
    	work.setName("MyWork");
    	workItemNode.setWork(work);
    	process.addNode(workItemNode);
    	new ConnectionImpl(actionNode, Node.CONNECTION_DEFAULT_TYPE, workItemNode, Node.CONNECTION_DEFAULT_TYPE);
    	WorkItemNode workItemNode2 = new WorkItemNode();
    	workItemNode2.setId(4);
    	workItemNode2.setName("WorkItem2");
    	work = new WorkImpl();
    	work.setName("MyWork");
    	workItemNode2.setWork(work);
    	process.addNode(workItemNode2);
    	new ConnectionImpl(workItemNode, Node.CONNECTION_DEFAULT_TYPE, workItemNode2, Node.CONNECTION_DEFAULT_TYPE);
    	WorkItemNode workItemNode3 = new WorkItemNode();
    	workItemNode3.setId(5);
    	workItemNode3.setName("WorkItem3");
    	work = new WorkImpl();
    	work.setName("MyWork");
    	workItemNode3.setWork(work);
    	process.addNode(workItemNode3);
    	new ConnectionImpl(workItemNode2, Node.CONNECTION_DEFAULT_TYPE, workItemNode3, Node.CONNECTION_DEFAULT_TYPE);
    	EndNode end = new EndNode();
    	end.setId(6);
    	end.setName("End");
    	process.addNode(end);
    	new ConnectionImpl(workItemNode3, Node.CONNECTION_DEFAULT_TYPE, end, Node.CONNECTION_DEFAULT_TYPE);
    	
    	PackageBuilder packageBuilder = new PackageBuilder();
    	ProcessBuilder processBuilder = new ProcessBuilder(packageBuilder);
    	processBuilder.buildProcess(process, null);
    	return packageBuilder.getPackage();
    }
    
	public void testPersistenceSubProcess() {
		Properties properties = new Properties();
		properties.put(
	        "processInstanceManagerFactory", 
	        "org.drools.persistence.processinstance.JPAProcessInstanceManagerFactory");
		properties.put(
	        "workItemManagerFactory", 
	        "org.drools.persistence.processinstance.JPAWorkItemManagerFactory");
		properties.put(
	        "processSignalManagerFactory", 
	        "org.drools.persistence.processinstance.JPASignalManagerFactory");
        RuleBaseConfiguration conf = new RuleBaseConfiguration(properties);
        RuleBase ruleBase = RuleBaseFactory.newRuleBase(conf);
        Package pkg = getProcessSubProcess();
        ruleBase.addPackage(pkg);

        SingleSessionCommandService service = new SingleSessionCommandService(ruleBase);
        StartProcessCommand startProcessCommand = new StartProcessCommand();
        startProcessCommand.setProcessId("org.drools.test.TestProcess");
        RuleFlowProcessInstance processInstance = (RuleFlowProcessInstance) service.execute(startProcessCommand);
        System.out.println("Started process instance " + processInstance.getId());
        long processInstanceId = processInstance.getId();
        
        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        assertNotNull(workItem);
        
        service = new SingleSessionCommandService(ruleBase);
        GetProcessInstanceCommand getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId(processInstanceId);
        processInstance = (RuleFlowProcessInstance) service.execute(getProcessInstanceCommand);
        assertNotNull(processInstance);
        
        Collection<NodeInstance> nodeInstances = processInstance.getNodeInstances();
        assertEquals(1, nodeInstances.size());
        SubProcessNodeInstance subProcessNodeInstance = (SubProcessNodeInstance) nodeInstances.iterator().next();
        long subProcessInstanceId = subProcessNodeInstance.getProcessInstanceId();
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId(subProcessInstanceId);
        RuleFlowProcessInstance subProcessInstance = (RuleFlowProcessInstance) service.execute(getProcessInstanceCommand);
        assertNotNull(subProcessInstance);

        service = new SingleSessionCommandService(ruleBase);
        CompleteWorkItemCommand completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId(workItem.getId());
        service.execute(completeWorkItemCommand);

        service = new SingleSessionCommandService(ruleBase);

        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId(subProcessInstanceId);
        subProcessInstance = (RuleFlowProcessInstance) service.execute(getProcessInstanceCommand);
        assertNull(subProcessInstance);

        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId(processInstanceId);
        processInstance = (RuleFlowProcessInstance) service.execute(getProcessInstanceCommand);
        assertNull(processInstance);
	}
	
	private Package getProcessSubProcess() {
    	RuleFlowProcess process = new RuleFlowProcess();
    	process.setId("org.drools.test.TestProcess");
    	process.setName("TestProcess");
    	process.setPackageName("org.drools.test");
    	StartNode start = new StartNode();
    	start.setId(1);
    	start.setName("Start");
    	process.addNode(start);
    	ActionNode actionNode = new ActionNode();
    	actionNode.setId(2);
    	actionNode.setName("Action");
    	DroolsConsequenceAction action = new DroolsConsequenceAction();
    	action.setDialect("java");
    	action.setConsequence("System.out.println(\"Executed action\");");
    	actionNode.setAction(action);
    	process.addNode(actionNode);
    	new ConnectionImpl(start, Node.CONNECTION_DEFAULT_TYPE, actionNode, Node.CONNECTION_DEFAULT_TYPE);
    	SubProcessNode subProcessNode = new SubProcessNode();
    	subProcessNode.setId(3);
    	subProcessNode.setName("SubProcess");
    	subProcessNode.setProcessId("org.drools.test.SubProcess");
    	process.addNode(subProcessNode);
    	new ConnectionImpl(actionNode, Node.CONNECTION_DEFAULT_TYPE, subProcessNode, Node.CONNECTION_DEFAULT_TYPE);
    	EndNode end = new EndNode();
    	end.setId(4);
    	end.setName("End");
    	process.addNode(end);
    	new ConnectionImpl(subProcessNode, Node.CONNECTION_DEFAULT_TYPE, end, Node.CONNECTION_DEFAULT_TYPE);
    	
    	PackageBuilder packageBuilder = new PackageBuilder();
    	ProcessBuilder processBuilder = new ProcessBuilder(packageBuilder);
    	processBuilder.buildProcess(process, null);

    	process = new RuleFlowProcess();
    	process.setId("org.drools.test.SubProcess");
    	process.setName("SubProcess");
    	process.setPackageName("org.drools.test");
    	start = new StartNode();
    	start.setId(1);
    	start.setName("Start");
    	process.addNode(start);
    	actionNode = new ActionNode();
    	actionNode.setId(2);
    	actionNode.setName("Action");
    	action = new DroolsConsequenceAction();
    	action.setDialect("java");
    	action.setConsequence("System.out.println(\"Executed action\");");
    	actionNode.setAction(action);
    	process.addNode(actionNode);
    	new ConnectionImpl(start, Node.CONNECTION_DEFAULT_TYPE, actionNode, Node.CONNECTION_DEFAULT_TYPE);
    	WorkItemNode workItemNode = new WorkItemNode();
    	workItemNode.setId(3);
    	workItemNode.setName("WorkItem1");
    	Work work = new WorkImpl();
    	work.setName("MyWork");
    	workItemNode.setWork(work);
    	process.addNode(workItemNode);
    	new ConnectionImpl(actionNode, Node.CONNECTION_DEFAULT_TYPE, workItemNode, Node.CONNECTION_DEFAULT_TYPE);
    	end = new EndNode();
    	end.setId(6);
    	end.setName("End");
    	process.addNode(end);
    	new ConnectionImpl(workItemNode, Node.CONNECTION_DEFAULT_TYPE, end, Node.CONNECTION_DEFAULT_TYPE);

    	processBuilder.buildProcess(process, null);
    	return packageBuilder.getPackage();
    }
    
	public void testPersistenceTimer() throws Exception {
		Properties properties = new Properties();
		properties.put(
	        "processInstanceManagerFactory", 
	        "org.drools.persistence.processinstance.JPAProcessInstanceManagerFactory");
		properties.put(
	        "workItemManagerFactory", 
	        "org.drools.persistence.processinstance.JPAWorkItemManagerFactory");
		properties.put(
	        "processSignalManagerFactory", 
	        "org.drools.persistence.processinstance.JPASignalManagerFactory");
        RuleBaseConfiguration conf = new RuleBaseConfiguration(properties);
        RuleBase ruleBase = RuleBaseFactory.newRuleBase(conf);
        Package pkg = getProcessTimer();
        ruleBase.addPackage(pkg);

        SingleSessionCommandService service = new SingleSessionCommandService(ruleBase);
        StartProcessCommand startProcessCommand = new StartProcessCommand();
        startProcessCommand.setProcessId("org.drools.test.TestProcess");
        ProcessInstance processInstance = (ProcessInstance) service.execute(startProcessCommand);
        System.out.println("Started process instance " + processInstance.getId());
        
        service = new SingleSessionCommandService(ruleBase);
        GetProcessInstanceCommand getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId(processInstance.getId());
        processInstance = (ProcessInstance) service.execute(getProcessInstanceCommand);
        assertNotNull(processInstance);

        service = new SingleSessionCommandService(ruleBase);
        Thread.sleep(2000);
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId(processInstance.getId());
        processInstance = (ProcessInstance) service.execute(getProcessInstanceCommand);
        assertNull(processInstance);
	}

    private Package getProcessTimer() {
    	RuleFlowProcess process = new RuleFlowProcess();
    	process.setId("org.drools.test.TestProcess");
    	process.setName("TestProcess");
    	process.setPackageName("org.drools.test");
    	StartNode start = new StartNode();
    	start.setId(1);
    	start.setName("Start");
    	process.addNode(start);
    	TimerNode timerNode = new TimerNode();
    	timerNode.setId(2);
    	timerNode.setName("Timer");
    	Timer timer = new Timer();
    	timer.setDelay(1000);
    	timerNode.setTimer(timer);
    	process.addNode(timerNode);
    	new ConnectionImpl(start, Node.CONNECTION_DEFAULT_TYPE, timerNode, Node.CONNECTION_DEFAULT_TYPE);
    	ActionNode actionNode = new ActionNode();
    	actionNode.setId(3);
    	actionNode.setName("Action");
    	DroolsConsequenceAction action = new DroolsConsequenceAction();
    	action.setDialect("java");
    	action.setConsequence("System.out.println(\"Executed action\");");
    	actionNode.setAction(action);
    	process.addNode(actionNode);
    	new ConnectionImpl(timerNode, Node.CONNECTION_DEFAULT_TYPE, actionNode, Node.CONNECTION_DEFAULT_TYPE);
    	EndNode end = new EndNode();
    	end.setId(6);
    	end.setName("End");
    	process.addNode(end);
    	new ConnectionImpl(actionNode, Node.CONNECTION_DEFAULT_TYPE, end, Node.CONNECTION_DEFAULT_TYPE);
    	
    	PackageBuilder packageBuilder = new PackageBuilder();
    	ProcessBuilder processBuilder = new ProcessBuilder(packageBuilder);
    	processBuilder.buildProcess(process, null);
    	return packageBuilder.getPackage();
    }
    
    // FIXME mdp
	public void FIXME_testPersistenceTimer2() throws Exception {
		Properties properties = new Properties();
		properties.put(
	        "processInstanceManagerFactory", 
	        "org.drools.persistence.processinstance.JPAProcessInstanceManagerFactory");
		properties.put(
	        "workItemManagerFactory", 
	        "org.drools.persistence.processinstance.JPAWorkItemManagerFactory");
		properties.put(
	        "processSignalManagerFactory", 
	        "org.drools.persistence.processinstance.JPASignalManagerFactory");
        RuleBaseConfiguration conf = new RuleBaseConfiguration(properties);
        RuleBase ruleBase = RuleBaseFactory.newRuleBase(conf);
        Package pkg = getProcessTimer2();
        ruleBase.addPackage(pkg);

        SingleSessionCommandService service = new SingleSessionCommandService(ruleBase);
        StartProcessCommand startProcessCommand = new StartProcessCommand();
        startProcessCommand.setProcessId("org.drools.test.TestProcess");
        ProcessInstance processInstance = (ProcessInstance) service.execute(startProcessCommand);
        System.out.println("Started process instance " + processInstance.getId());
        
        service = new SingleSessionCommandService(ruleBase);
        GetProcessInstanceCommand getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId(processInstance.getId());
        processInstance = (ProcessInstance) service.execute(getProcessInstanceCommand);
        assertNotNull(processInstance);

        service = new SingleSessionCommandService(ruleBase);
        Thread.sleep(2000);
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId(processInstance.getId());
        processInstance = (ProcessInstance) service.execute(getProcessInstanceCommand);
        assertNull(processInstance);
	}

    private Package getProcessTimer2() {
    	RuleFlowProcess process = new RuleFlowProcess();
    	process.setId("org.drools.test.TestProcess");
    	process.setName("TestProcess");
    	process.setPackageName("org.drools.test");
    	StartNode start = new StartNode();
    	start.setId(1);
    	start.setName("Start");
    	process.addNode(start);
    	TimerNode timerNode = new TimerNode();
    	timerNode.setId(2);
    	timerNode.setName("Timer");
    	Timer timer = new Timer();
    	timer.setDelay(0);
    	timerNode.setTimer(timer);
    	process.addNode(timerNode);
    	new ConnectionImpl(start, Node.CONNECTION_DEFAULT_TYPE, timerNode, Node.CONNECTION_DEFAULT_TYPE);
    	ActionNode actionNode = new ActionNode();
    	actionNode.setId(3);
    	actionNode.setName("Action");
    	DroolsConsequenceAction action = new DroolsConsequenceAction();
    	action.setDialect("java");
    	action.setConsequence("try { Thread.sleep(1000); } catch (Throwable t) {} System.out.println(\"Executed action\");");
    	actionNode.setAction(action);
    	process.addNode(actionNode);
    	new ConnectionImpl(timerNode, Node.CONNECTION_DEFAULT_TYPE, actionNode, Node.CONNECTION_DEFAULT_TYPE);
    	EndNode end = new EndNode();
    	end.setId(6);
    	end.setName("End");
    	process.addNode(end);
    	new ConnectionImpl(actionNode, Node.CONNECTION_DEFAULT_TYPE, end, Node.CONNECTION_DEFAULT_TYPE);
    	
    	PackageBuilder packageBuilder = new PackageBuilder();
    	ProcessBuilder processBuilder = new ProcessBuilder(packageBuilder);
    	processBuilder.buildProcess(process, null);
    	return packageBuilder.getPackage();
    }
    
}
