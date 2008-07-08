package org.drools.osworkflow.instance.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.osworkflow.core.OSWorkflowConnection;
import org.drools.osworkflow.core.node.StepNode;
import org.drools.osworkflow.instance.OSWorkflowProcessInstance;
import org.drools.process.instance.ProcessInstance;
import org.drools.workflow.core.Connection;
import org.drools.workflow.instance.NodeInstance;
import org.drools.workflow.instance.impl.NodeInstanceImpl;

import com.opensymphony.module.propertyset.map.MapPropertySet;
import com.opensymphony.workflow.basic.BasicWorkflowContext;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionalResultDescriptor;
import com.opensymphony.workflow.loader.ConditionsDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.RestrictionDescriptor;
import com.opensymphony.workflow.loader.ResultDescriptor;
import com.opensymphony.workflow.spi.Step;
import com.opensymphony.workflow.util.DefaultVariableResolver;

public class StepNodeInstance extends NodeInstanceImpl implements Step {

    private static final long serialVersionUID = 1L;
    
    private String status;
    private String owner;
    
    public StepNode getStepNode() {
        return (StepNode) getNode();
    }
    
    public OSWorkflowProcessInstance getOSWorkflowProcessInstance() {
        return (OSWorkflowProcessInstance) getProcessInstance();
    }
    
    public int getStepId() {
        return (int) getNodeId();
    }

    public void internalTrigger(NodeInstance from, String type) {
        Map<String, Object> transientVars = new HashMap<String, Object>();
        transientVars.put("context", new BasicWorkflowContext("caller"));
        List<FunctionDescriptor> preFunctions = getStepNode().getPreFunctions();
        if (preFunctions != null) {
            for (FunctionDescriptor preFunction: preFunctions) {
                getOSWorkflowProcessInstance().executeFunction(preFunction, transientVars);
            }
        }
        setStatus(type);
        Collection<ActionDescriptor> actions = getStepNode().getActions();
        for (ActionDescriptor action: actions) {
            if (action.getAutoExecute() && isAvailableAction(action)) {
                doAction(action.getId(), null);
                break;
            }
        }
    }
    
    public void doAction(int actionId, Map inputs) {
        Map<String, Object> transientVars = new HashMap<String, Object>();
        transientVars.put("context", new BasicWorkflowContext("caller"));
        ActionDescriptor action = getStepNode().getAction(actionId);
        if (action == null) {
            throw new IllegalArgumentException(
                "Unknown action id " + actionId);
        }
        List<FunctionDescriptor> preFunctions = action.getPreFunctions();
        if (preFunctions != null) {
            for (FunctionDescriptor preFunction: preFunctions) {
                getOSWorkflowProcessInstance().executeFunction(preFunction, transientVars);
            }
        }
        boolean executed = false;
        List<ConditionalResultDescriptor> conditionalResults = action.getConditionalResults();
        if (conditionalResults != null) {
            for (ConditionalResultDescriptor conditionalResult: conditionalResults) {
                if (getOSWorkflowProcessInstance().passesConditions(null, conditionalResult.getConditions(), (int) getNodeId())) {
                    executeResult(conditionalResult, actionId, transientVars);
                }
            }
        }
        if (!executed) {
            executeResult(action.getUnconditionalResult(), actionId, transientVars);
        }
        List<FunctionDescriptor> postFunctions = action.getPostFunctions();
        if (postFunctions != null) {
            for (FunctionDescriptor postFunction: postFunctions) {
                getOSWorkflowProcessInstance().executeFunction(postFunction, transientVars);
            }
        }
        if (action.isFinish()) {
            getProcessInstance().setState(ProcessInstance.STATE_COMPLETED);
        }
    }
    
    protected void executeResult(ResultDescriptor result, int actionId, Map<String, Object> transientVars) {
        List<FunctionDescriptor> preFunctions = result.getPreFunctions();
        if (preFunctions != null) {
            for (FunctionDescriptor preFunction: preFunctions) {
                getOSWorkflowProcessInstance().executeFunction(preFunction, transientVars);
            }
        }
        String owner = null;
        if (result.getOwner() != null) {
            MapPropertySet ps = new MapPropertySet();
            ps.setMap(new HashMap());
            Object o = new DefaultVariableResolver().translateVariables(result.getOwner(), transientVars, ps);
            owner = (o != null) ? o.toString() : null;
        }
        if (result.getStep() == -1 || result.getStep() == getNodeId()) {
            setOwner(owner);
            setStatus(result.getStatus());
        } else {
            setStatus(result.getOldStatus());
            List<FunctionDescriptor> postFunctions = getStepNode().getPostFunctions();
            if (postFunctions != null) {
                for (FunctionDescriptor postFunction: postFunctions) {
                    getOSWorkflowProcessInstance().executeFunction(postFunction, transientVars);
                }
            }
            getNodeInstanceContainer().removeNodeInstance(this);
            for (Connection connection: getNode().getOutgoingConnections(actionId + "")) {
                NodeInstance nodeInstance = getNodeInstanceContainer().getNodeInstance(connection.getTo());
                if (nodeInstance instanceof StepNodeInstance) {
                    ((StepNodeInstance) nodeInstance).setOwner(owner);
                }
                nodeInstance.trigger(this, connection.getToType());
            }
        }
        List<FunctionDescriptor> postFunctions = result.getPostFunctions();
        if (postFunctions != null) {
            for (FunctionDescriptor postFunction: postFunctions) {
                getOSWorkflowProcessInstance().executeFunction(postFunction, transientVars);
            }
        }
    }
        
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public boolean isAvailableAction(int actionId) {
        return isAvailableAction(getStepNode().getAction(actionId));
    }
    
    private boolean isAvailableAction(ActionDescriptor action) {
        if (action == null) {
            return false;
        }
        RestrictionDescriptor restriction = action.getRestriction();
        if (restriction != null) {
            ConditionsDescriptor conditions = restriction.getConditionsDescriptor();
            if (conditions != null) {
                return getOSWorkflowProcessInstance().passesConditions(
                    conditions.getType(), conditions.getConditions(), (int) getNodeId());
            }
        }
        return true;
    }
    
    public List<Integer> getAvailableActions() {
        List<Integer> ids = new ArrayList<Integer>();
        for (ActionDescriptor action: getStepNode().getActions()) {
           if (isAvailableAction(action)) {
               ids.add(action.getId());
           }
        }
        return ids;
    }

    protected void triggerConnection(Connection connection) {
        Map<String, Object> transientVars = new HashMap<String, Object>();
        if (connection instanceof OSWorkflowConnection) {
            List<FunctionDescriptor> functions = 
                ((OSWorkflowConnection) connection).getPreFunctions();
            if (functions != null) {
                for (FunctionDescriptor function: functions) {
                    getOSWorkflowProcessInstance().executeFunction(function, transientVars);
                }
            }
            super.triggerConnection(connection);
            functions = ((OSWorkflowConnection) connection).getPostFunctions();
            if (functions != null) {
                for (FunctionDescriptor function: functions) {
                    getOSWorkflowProcessInstance().executeFunction(function, transientVars);
                }
            }
        } else {
            super.triggerConnection(connection);
        }
    }
    
    public int getActionId() {
        // TODO
        return 0;
    }

    public String getCaller() {
        // TODO
        return null;
    }

    public Date getDueDate() {
        // TODO
        return null;
    }

    public long getEntryId() {
        // TODO
        return 0;
    }

    public Date getFinishDate() {
        // TODO
        return null;
    }
    
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public long[] getPreviousStepIds() {
        // TODO
        return null;
    }

    public Date getStartDate() {
        // TODO
        return null;
    }

}
