package org.drools.osworkflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.osworkflow.core.OSWorkflowProcess;
import org.drools.osworkflow.instance.OSWorkflowProcessInstance;
import org.drools.osworkflow.instance.node.StepNodeInstance;
import org.drools.process.instance.ProcessInstance;
import org.drools.rule.Package;
import org.drools.workflow.instance.NodeInstance;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.InvalidActionException;
import com.opensymphony.workflow.InvalidEntryStateException;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.InvalidRoleException;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.config.Configuration;
import com.opensymphony.workflow.config.DefaultConfiguration;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.query.WorkflowExpressionQuery;
import com.opensymphony.workflow.query.WorkflowQuery;
import com.opensymphony.workflow.spi.Step;
import com.opensymphony.workflow.spi.WorkflowEntry;

public class DroolsWorkflow implements Workflow {

    private OSWorkflowParser parser = new OSWorkflowParser();
	private WorkingMemory workingMemory;
	private Configuration configuration;
	private Map<Long, OSWorkflowProcessInstance> processInstances = new HashMap<Long, OSWorkflowProcessInstance>();
	
	public long initialize(String workflowName, int initialAction, Map inputs)
			throws InvalidRoleException, InvalidInputException,
			WorkflowException, InvalidEntryStateException,
			InvalidActionException {
		RuleBase ruleBase = getWorkingMemory().getRuleBase();
		Package p = ruleBase.getPackage("org.drools.osworkflow");
		if (p.getRuleFlows().get(workflowName) == null) {
			WorkflowDescriptor descriptor = getConfiguration().getWorkflow(workflowName);
			if (descriptor == null) {
				throw new IllegalArgumentException(
					"Could not find process with name " + workflowName);
			}
			Package newPackage = new Package("org.drools.osworkflow");
			if (descriptor.getName() == null) {
			    descriptor.setName(workflowName);
			}
			OSWorkflowProcess process = parser.parseOSWorkflow(descriptor);
			newPackage.addProcess(process);
			ruleBase.addPackage(newPackage);
		}
		OSWorkflowProcessInstance processInstance = (OSWorkflowProcessInstance)
		    getWorkingMemory().startProcess(workflowName);
		processInstance.doInitialAction(initialAction, inputs);
		processInstances.put(processInstance.getId(), processInstance);
		return processInstance.getId();
	}

	public void doAction(long id, int actionId, Map inputs)
			throws InvalidInputException, WorkflowException {
		findProcessInstance(id).doAction(actionId, inputs);
	}
	
	private OSWorkflowProcessInstance findProcessInstance(long id) {
	    OSWorkflowProcessInstance processInstance = processInstances.get(id);
		if (processInstance == null) {
			throw new IllegalArgumentException(
				"Could not find process instance with id " + id);
		}
		return processInstance;
	}
	
	public int[] getAvailableActions(long id) {
	    return getAvailableActions(id, null);
	}

	public int[] getAvailableActions(long id, Map inputs) {
        List<Integer> ids = new ArrayList<Integer>();
        OSWorkflowProcessInstance processInstance = findProcessInstance(id);
        for (NodeInstance nodeInstance: processInstance.getNodeInstances()) {
            if (nodeInstance instanceof StepNodeInstance) {
                StepNodeInstance stepNodeInstance = (StepNodeInstance) nodeInstance;
                ids.addAll(stepNodeInstance.getAvailableActions());
            }
        }
        int[] result = new int[ids.size()];
        int i = 0;
        for (int actionId : ids) {
            result[i++] = actionId;
        }
        return result;
	}

	public boolean canInitialize(String workflowName, int initialStep) {
		return canInitialize(workflowName, initialStep, null);
	}

	public boolean canInitialize(String workflowName, int initialAction, Map inputs) {
		// TODO
		return false;
	}

	public boolean canModifyEntryState(long id, int newState) {
		// TODO
		return false;
	}

	public void changeEntryState(long id, int newState) throws WorkflowException {
		// TODO
	}

	public void executeTriggerFunction(long id, int triggerId) throws WorkflowException {
		// TODO
	}

	public List<Step> getCurrentSteps(long id) {
	    return findProcessInstance(id).getCurrentSteps();
	}

	public int getEntryState(long id) {
	    int state = findProcessInstance(id).getState();
	    switch (state) {
        case ProcessInstance.STATE_PENDING:
            return WorkflowEntry.CREATED;
        case ProcessInstance.STATE_ACTIVE:
            return WorkflowEntry.ACTIVATED;
        case ProcessInstance.STATE_COMPLETED:
            return WorkflowEntry.COMPLETED;
        case ProcessInstance.STATE_ABORTED:
            return WorkflowEntry.KILLED;
        case ProcessInstance.STATE_SUSPENDED:
            return WorkflowEntry.SUSPENDED;
        default:
            return WorkflowEntry.UNKNOWN;
        }
	}

	public List getHistorySteps(long id) {
	    OSWorkflowProcessInstance processInstance = findProcessInstance(id);
		return processInstance.getHistorySteps();
	}

	public PropertySet getPropertySet(long id) {
		// TODO
		return null;
	}

	public List getSecurityPermissions(long id) {
		// TODO
		return null;
	}

	public List getSecurityPermissions(long id, Map inputs) {
		// TODO
		return null;
	}

	public WorkflowDescriptor getWorkflowDescriptor(String workflowName) {
		// TODO
		return null;
	}

	public String getWorkflowName(long id) {
		// TODO
		return null;
	}

	public String[] getWorkflowNames() {
		// TODO
		return null;
	}

	public List query(WorkflowQuery query) throws WorkflowException {
		// TODO
		return null;
	}

	public List query(WorkflowExpressionQuery query) throws WorkflowException {
		// TODO
		return null;
	}

	public boolean removeWorkflowDescriptor(String workflowName) throws FactoryException {
		// TODO
		return false;
	}

	public boolean saveWorkflowDescriptor(String workflowName, 
			WorkflowDescriptor descriptor, boolean replace)
			throws FactoryException {
		// TODO
		return false;
	}
	
	protected WorkingMemory getWorkingMemory() {
	    if (workingMemory == null) {
            RuleBase ruleBase = RuleBaseFactory.newRuleBase();
            Package p = new Package("org.drools.osworkflow");
            ruleBase.addPackage(p);
            workingMemory = ruleBase.newStatefulSession();
	    }
	    return workingMemory;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
	private Configuration getConfiguration() {
        Configuration config = (configuration != null) ? configuration : DefaultConfiguration.INSTANCE;
        if (!config.isInitialized()) {
            try {
                config.load(null);
            } catch (FactoryException e) {
            	e.printStackTrace();
                return null;
            }
        }
        return config;
	}

}
