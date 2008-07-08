package org.drools.osworkflow.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.process.core.context.variable.VariableScope;
import org.drools.workflow.core.impl.WorkflowProcessImpl;

import com.opensymphony.workflow.loader.ActionDescriptor;

public class OSWorkflowProcess extends WorkflowProcessImpl {

    public static final String OSWORKFLOW_TYPE = "OSWorkflow";

    private static final long serialVersionUID = 1L;
    
    public Map<Integer, ActionDescriptor> initialActions = 
        new HashMap<Integer, ActionDescriptor>();
    
    public OSWorkflowProcess() {
        setType(OSWORKFLOW_TYPE);
        VariableScope variableScope = new VariableScope();
        addContext(variableScope);
        setDefaultContext(variableScope);
    }

    public void addInitialAction(ActionDescriptor action) {
        this.initialActions.put(action.getId(), action);
    }
    
    public void setInitialActions(List<ActionDescriptor> actions) {
        for (ActionDescriptor action: actions) {
            addInitialAction(action);
        }
    }
    
    public Collection<ActionDescriptor> getInitialActions() {
        return initialActions.values();
    }
    
    public ActionDescriptor getInitialAction(int id) {
        return initialActions.get(id);
    }

}
