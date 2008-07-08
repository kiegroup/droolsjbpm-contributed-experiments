package org.drools.process.enterprise.processinstance;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.drools.RuleBase;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseFactory;
import org.drools.common.InternalWorkingMemory;
import org.drools.compiler.PackageBuilder;
import org.drools.process.enterprise.repository.ProcessInfo;
import org.drools.process.enterprise.repository.ProcessRepository;
import org.drools.process.instance.ProcessInstance;
import org.drools.process.instance.WorkItem;
import org.drools.process.instance.WorkItemHandler;
import org.drools.process.instance.WorkItemManager;
import org.drools.process.instance.impl.WorkItemImpl;
import org.drools.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.drools.rule.Package;

@Stateless
public class ProcessServiceBean implements ProcessService {

	private @PersistenceContext EntityManager manager;
	private @EJB ProcessRepository processRepository;
	   
	public long startProcess(String processId) {
		System.out.println("Starting process " + processId);
		InternalWorkingMemory workingMemory = createWorkingMemory();
		// start process
		long start = System.nanoTime();
		ProcessInstance processInstance = workingMemory.startProcess(processId);
		persistProcessInstance(processInstance);
		System.out.println("Time to execute: " + (System.nanoTime() - start));
		return processInstance.getId();
	}

	public void completeWorkItem(long workItemId, Map<String, Object> results) {
		InternalWorkingMemory workingMemory = createWorkingMemory();
		WorkItemInfo workItemInfo = manager.find(WorkItemInfo.class, workItemId);
		if (workItemInfo == null) {
			throw new IllegalArgumentException(
				"Could not find work item " + workItemId);
		}
		// TODO: should load other work items as well ?
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setId(workItemInfo.getWorkItemId());
		workItem.setProcessInstanceId(workItemInfo.getProcessInstanceId());
		workItem.setName(workItemInfo.getName());
		workingMemory.getWorkItemManager().internalAddWorkItem(workItem);
		manager.remove(workItemInfo);
		// TODO: should load other process instances as well
		workingMemory.getWorkItemManager().completeWorkItem(workItemId, results);
        System.out.println("Executed work item " + workItem.getName() + " [" + workItem.getId() + "]");
		persistProcessInstance(workItemInfo.getProcessInstanceId());
	}
	
	private InternalWorkingMemory createWorkingMemory() {
		// TODO: share rule base
		List<ProcessInfo> processInfos = processRepository.getProcessInfos();
		PackageBuilder builder = new PackageBuilder();
		for (ProcessInfo processInfo: processInfos) {
			builder.addRuleFlow(new StringReader(processInfo.getProcessXML()));
		}
		Package pkg = builder.getPackage();
		Properties properties = new Properties();
		properties.put(
	        "processInstanceManager", 
	        "org.drools.process.enterprise.processinstance.EJB3ProcessInstanceManager");
        RuleBaseConfiguration conf = new RuleBaseConfiguration(properties);
		RuleBase ruleBase = RuleBaseFactory.newRuleBase(conf);
		ruleBase.addPackage(pkg);
		// create new working memory
		InternalWorkingMemory workingMemory = (InternalWorkingMemory) ruleBase.newStatefulSession();
		// TODO auto-register work item handlers (e.g. based on config)
		workingMemory.getWorkItemManager().registerWorkItemHandler("Log", new SystemOutWorkItemHandler());
		workingMemory.getWorkItemManager().registerWorkItemHandler("Human Task", new EnterpriseWorkItemHandler());
//		workingMemory.addEventListener(new DefaultRuleFlowEventListener() {
//            public void afterRuleFlowCompleted(RuleFlowCompletedEvent event,
//                    WorkingMemory workingMemory) {
//                System.out.println("Process instance completed: " + 
//                    event.getProcessInstance().getProcessId() + 
//                    "[" + event.getProcessInstance().getId() + "]");
//            }
//            public void afterRuleFlowNodeTriggered(
//                    RuleFlowNodeTriggeredEvent event,
//                    WorkingMemory workingMemory) {
//                System.out.println("Node completed: " + 
//                    event.getRuleFlowNodeInstance().getNode().getName() +
//                    "[" + event.getRuleFlowNodeInstance().getNodeId() + "] for process instance " +
//                    event.getProcessInstance().getProcessId() + 
//                    "[" + event.getProcessInstance().getId() + "]");
//            }
//            public void afterRuleFlowStarted(RuleFlowStartedEvent event,
//                    WorkingMemory workingMemory) {
//                System.out.println("Process instance started: " + 
//                    event.getProcessInstance().getProcessId() + 
//                    "[" + event.getProcessInstance().getId() + "]");
//            }
//		});
		// TODO
		EJB3ProcessInstanceManager processInstanceManager = 
		    (EJB3ProcessInstanceManager)((InternalWorkingMemory) workingMemory).getProcessInstanceManager();
		processInstanceManager.setEntityManager(manager);
		processInstanceManager.setWorkingMemory(workingMemory);
		return workingMemory;
	}
	
	private void persistProcessInstance(long processInstanceId) {
	    
	}
	
	private void persistProcessInstance(ProcessInstance processInstance) {
		ProcessInstanceInfo processInstanceInfo = manager.find(ProcessInstanceInfo.class, processInstance.getId());
		if (processInstanceInfo == null) {
			processInstanceInfo = new ProcessInstanceInfo();
		}
		processInstanceInfo.setProcessInstance(processInstance);
        manager.merge(processInstanceInfo);
        manager.flush();
		System.out.println("Persisted process instance " + processInstanceInfo.getId());
		// TODO remove process instances that are not longer there
		// TODO make smarter, e.g. by registering a listener
		// before starting execution and only updating changed instances
	}

	public class EnterpriseWorkItemHandler implements WorkItemHandler {

		public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
			WorkItemInfo workItemInfo = new WorkItemInfo();
			workItemInfo.setWorkItemId(workItem.getId());
			workItemInfo.setProcessInstanceId(workItem.getProcessInstanceId());
			workItemInfo.setName(workItem.getName());
			workItemInfo.setParameters(workItem.getParameters());
			ProcessServiceBean.this.manager.persist(workItemInfo);
			System.out.println("Persisted work item " + workItem.getName() + " [" + workItem.getId() + "]");
			for (Map.Entry<String, Object> entry: workItem.getParameters().entrySet()) {
				System.out.println("  " + entry.getKey() + " = " + entry.getValue());
			}
		}

		public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
			WorkItemInfo workItemInfo = ProcessServiceBean.this.manager.find(WorkItemInfo.class, workItem.getId());
			ProcessServiceBean.this.manager.remove(workItemInfo);
			System.out.println("Aborted work item " + workItem.getName() + " [" + workItem.getId() + "]");
		}

	}

}
