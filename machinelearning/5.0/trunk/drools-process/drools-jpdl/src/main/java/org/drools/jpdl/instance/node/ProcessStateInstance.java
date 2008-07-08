package org.drools.jpdl.instance.node;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.drools.WorkingMemory;
import org.drools.event.RuleFlowCompletedEvent;
import org.drools.event.RuleFlowEventListener;
import org.drools.event.RuleFlowGroupActivatedEvent;
import org.drools.event.RuleFlowGroupDeactivatedEvent;
import org.drools.event.RuleFlowNodeTriggeredEvent;
import org.drools.event.RuleFlowStartedEvent;
import org.drools.jpdl.core.node.ProcessState;
import org.drools.process.core.context.variable.VariableScope;
import org.drools.process.instance.context.variable.VariableScopeInstance;
import org.drools.workflow.instance.NodeInstance;
import org.jbpm.context.def.VariableAccess;
import org.jbpm.graph.def.Event;

public class ProcessStateInstance extends JpdlNodeInstance implements RuleFlowEventListener {

	private static final long serialVersionUID = 1L;
	
	private long processInstanceId;

	public ProcessState getProcessState() {
		return (ProcessState) getNode();
	}

	public void execute(NodeInstance from, String type) {
		Map<String, Object> parameters = null;
		Set<VariableAccess> variableAccesses = getProcessState().getVariableAccesses();
		if ((variableAccesses != null) && (!variableAccesses.isEmpty())) {
			parameters = new HashMap<String, Object>();
			// TODO: transient variables ?
			for (VariableAccess variableAccess : variableAccesses) {
				if (variableAccess.isReadable()) {
					String variableName = variableAccess.getVariableName();
					VariableScopeInstance variableScopeInstance = (VariableScopeInstance)
						resolveContextInstance(VariableScope.VARIABLE_SCOPE, variableName);
					Object value = variableScopeInstance.getVariable(variableName);
					if (value != null) {
						String mappedName = variableAccess.getMappedName();
						parameters.put(mappedName, value);
					}
				}
			}
		}
		addEventListeners();
		processInstanceId = getProcessInstance().getWorkingMemory()
			.startProcess(getProcessState().getSubProcessName(), parameters).getId();
		fireEvent(Event.EVENTTYPE_SUBPROCESS_CREATED);
	}

    public void addEventListeners() {
        getProcessInstance().getWorkingMemory().addEventListener(this);
    }

    public void removeEventListeners() {
        getProcessInstance().getWorkingMemory().removeEventListener(this);
    }

    public void afterRuleFlowCompleted(RuleFlowCompletedEvent event,
            WorkingMemory workingMemory) {
        if ( event.getProcessInstance().getId() == processInstanceId ) {
            removeEventListeners();
    		Set<VariableAccess> variableAccesses = getProcessState().getVariableAccesses();
    		if ((variableAccesses != null) && (!variableAccesses.isEmpty())) {

    			for (VariableAccess variableAccess: variableAccesses) {
    				if (variableAccess.isWritable()) {
    					String mappedName = variableAccess.getMappedName();
    					VariableScopeInstance variableScopeInstance = (VariableScopeInstance)
							event.getProcessInstance().getContextInstance(VariableScope.VARIABLE_SCOPE);
    					Object value = variableScopeInstance.getVariable(mappedName);
    					if (value != null) {
        					String variableName = variableAccess.getVariableName();
        					variableScopeInstance = (VariableScopeInstance)
								resolveContextInstance(VariableScope.VARIABLE_SCOPE, mappedName);
        					variableScopeInstance.setVariable(variableName, value);
    					}
    				}
    			}
    		}
    		fireEvent(Event.EVENTTYPE_SUBPROCESS_END);
            leave();
        }
    }

    public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event,
            WorkingMemory workingMemory) {
        // Do nothing
    }

    public void afterRuleFlowGroupDeactivated(
            RuleFlowGroupDeactivatedEvent event, WorkingMemory workingMemory) {
        // Do nothing
    }

    public void afterRuleFlowNodeTriggered(RuleFlowNodeTriggeredEvent event,
            WorkingMemory workingMemory) {
        // Do nothing
    }

    public void afterRuleFlowStarted(RuleFlowStartedEvent event,
            WorkingMemory workingMemory) {
        // Do nothing
    }

    public void beforeRuleFlowCompleted(RuleFlowCompletedEvent event,
            WorkingMemory workingMemory) {
        // Do nothing
    }

    public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event,
            WorkingMemory workingMemory) {
        // Do nothing
    }

    public void beforeRuleFlowGroupDeactivated(
            RuleFlowGroupDeactivatedEvent event, WorkingMemory workingMemory) {
        // Do nothing
    }

    public void beforeRuleFlowNodeTriggered(RuleFlowNodeTriggeredEvent event,
            WorkingMemory workingMemory) {
        // Do nothing
    }

    public void beforeRuleFlowStarted(RuleFlowStartedEvent event,
            WorkingMemory workingMemory) {
        // Do nothing
    }

}
