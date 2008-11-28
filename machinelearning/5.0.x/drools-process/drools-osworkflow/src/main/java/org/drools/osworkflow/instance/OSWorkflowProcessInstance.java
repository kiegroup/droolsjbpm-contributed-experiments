package org.drools.osworkflow.instance;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.definition.process.Node;
import org.drools.osworkflow.core.OSWorkflowProcess;
import org.drools.osworkflow.instance.node.StepNodeInstance;
import org.drools.process.core.context.variable.VariableScope;
import org.drools.process.instance.ProcessInstance;
import org.drools.process.instance.context.variable.VariableScopeInstance;
import org.drools.workflow.instance.NodeInstance;
import org.drools.workflow.instance.impl.WorkflowProcessInstanceImpl;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.map.MapPropertySet;
import com.opensymphony.workflow.Condition;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.TypeResolver;
import com.opensymphony.workflow.WorkflowContext;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.ConditionalResultDescriptor;
import com.opensymphony.workflow.loader.ConditionsDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.ResultDescriptor;
import com.opensymphony.workflow.query.WorkflowExpressionQuery;
import com.opensymphony.workflow.query.WorkflowQuery;
import com.opensymphony.workflow.spi.Step;
import com.opensymphony.workflow.spi.WorkflowEntry;
import com.opensymphony.workflow.spi.WorkflowStore;

public class OSWorkflowProcessInstance extends WorkflowProcessInstanceImpl implements WorkflowEntry {

    private static final long serialVersionUID = 1L;
    
    private List<Step> history = new ArrayList<Step>();

    public OSWorkflowProcess getOSWorkflowProcess() {
        return (OSWorkflowProcess) getProcess();
    }
    
    protected void internalStart() {
        Map<String, Object> transientVars = new HashMap<String, Object>();
        for (ActionDescriptor action: getOSWorkflowProcess().getInitialActions()) {
            if (action.getAutoExecute()) {
                executeAction(action, 0, transientVars);
            }
        }
    }

    public void doInitialAction(int actionId, Map inputs) {
        Map<String, Object> transientVars = new HashMap<String, Object>();
        transientVars.put("context", new DummyWorkflowContext());
        ActionDescriptor initialAction = getOSWorkflowProcess().getInitialAction(actionId);
        if (initialAction == null) {
            throw new IllegalArgumentException(
                "Unknown initial action id " + actionId);
        }
        executeAction(initialAction, 0, transientVars);
    }
    
    public void doAction(int actionId, Map inputs) {
        for (org.drools.runtime.process.NodeInstance nodeInstance: getNodeInstances()) {
            StepNodeInstance stepNodeInstance = (StepNodeInstance) nodeInstance;
            if (stepNodeInstance.isAvailableAction(actionId)) {
                stepNodeInstance.doAction(actionId, inputs);
                break;
            }
        }
        checkImplicitFinish();
    }
    
    protected void executeAction(ActionDescriptor action, int currentStepId, Map<String, Object> transientVars) {
        List<FunctionDescriptor> preFunctions = action.getPreFunctions();
        if (preFunctions != null) {
            for (FunctionDescriptor preFunction: preFunctions) {
                executeFunction(preFunction, transientVars);
            }
        }
        boolean executed = false;
        List<ConditionalResultDescriptor> conditionalResults = action.getConditionalResults();
        if (conditionalResults != null) {
            for (ConditionalResultDescriptor conditionalResult: conditionalResults) {
                if (passesConditions(null, conditionalResult.getConditions(), currentStepId)) {
                    executeResult(conditionalResult, transientVars);
                }
            }
        }
        if (!executed) {
            executeResult(action.getUnconditionalResult(), transientVars);
        }
        List<FunctionDescriptor> postFunctions = action.getPostFunctions();
        if (postFunctions != null) {
            for (FunctionDescriptor postFunction: postFunctions) {
                executeFunction(postFunction, transientVars);
            }
        }
        if (action.isFinish()) {
            setState(ProcessInstance.STATE_COMPLETED);
        }
    }
    
    protected void executeResult(ResultDescriptor result, Map<String, Object> transientVars) {
        List<FunctionDescriptor> preFunctions = result.getPreFunctions();
        if (preFunctions != null) {
            for (FunctionDescriptor preFunction: preFunctions) {
                executeFunction(preFunction, transientVars);
            }
        }
        int nodeId = 0;
        String type = null;
        if (result.getSplit() != 0) {
            nodeId = result.getSplit();
            type = org.drools.workflow.core.Node.CONNECTION_DEFAULT_TYPE;
        } else if (result.getJoin() != 0) {
            nodeId = result.getJoin();
            type = org.drools.workflow.core.Node.CONNECTION_DEFAULT_TYPE;
        } else {
            nodeId = result.getStep();
            type = result.getStatus();
        }
        Node node = getNodeContainer().getNode(nodeId);
        if (node == null) {
            throw new IllegalArgumentException(
                "Unknown node id " + nodeId);
        }
        NodeInstance nodeInstance = getNodeInstance(node);
        if (result.getOwner() != null && nodeInstance instanceof StepNodeInstance) {
            ((StepNodeInstance) nodeInstance).setOwner(result.getOwner());
        }
        nodeInstance.trigger(null, type);
        List<FunctionDescriptor> postFunctions = result.getPostFunctions();
        if (postFunctions != null) {
            for (FunctionDescriptor postFunction: postFunctions) {
                executeFunction(postFunction, transientVars);
            }
        }
    }
    
    public void executeFunction(FunctionDescriptor function, Map<String, Object> transientVars) {
        Map<String, Object> params = createParams(function.getArgs());
        try {
            FunctionProvider provider = TypeResolver.getResolver().getFunction(function.getType(), params);
            if (provider == null) {
                throw new WorkflowException("Could not load FunctionProvider class");
            }
            MapPropertySet ps = new MapPropertySet();
            ps.setMap(new HashMap());
            provider.execute(transientVars, params, ps);
        } catch (WorkflowException e) {
            throw new RuntimeException("WorkflowException when executing function", e);
        }

    }
    
    protected boolean passesCondition(ConditionDescriptor conditionDesc, int currentStepId) {
        try {
            String type = conditionDesc.getType();
            Map<String, Object> params = createParams(conditionDesc.getArgs());
            if (currentStepId != -1) {
                Object stepId = params.get("stepId");
                if ((stepId != null) && stepId.equals("-1")) {
                    params.put("stepId", String.valueOf(currentStepId));
                }
            }
            Condition condition = TypeResolver.getResolver().getCondition(type, params);
            if (condition == null) {
                throw new WorkflowException("Could not load condition");
            }
            try {
                Map<String, Object> transientVars = new HashMap<String, Object>();
                transientVars.put("entry", this);
                transientVars.put("context", new DummyWorkflowContext());
                transientVars.put("store", new DummyWorkflowStore());
                PropertySet ps = new MapPropertySet();
                boolean passed = condition.passesCondition(transientVars, params, ps);
                if (conditionDesc.isNegate()) {
                    passed = !passed;
                }
                return passed;
            } catch (Exception e) {
                if (e instanceof WorkflowException) {
                    throw (WorkflowException) e;
                }
                throw new WorkflowException("Unknown exception encountered when checking condition " + condition, e);
            }
        } catch (WorkflowException e) {
            throw new RuntimeException("WorkflowException when checking conditions", e);
        }
    }

    public boolean passesConditions(String conditionType, List<AbstractDescriptor> conditions, int currentStepId) {
        if ((conditions == null) || (conditions.size() == 0)) {
            return true;
        }
        boolean and = "AND".equals(conditionType);
        boolean or = !and;
        for (AbstractDescriptor descriptor: conditions) {
            boolean result;
            if (descriptor instanceof ConditionsDescriptor) {
                ConditionsDescriptor conditionsDescriptor = (ConditionsDescriptor) descriptor;
                result = passesConditions(conditionsDescriptor.getType(), conditionsDescriptor.getConditions(), currentStepId);
            } else {
                result = passesCondition((ConditionDescriptor) descriptor, currentStepId);
            }
            if (and && !result) {
                return false;
            } else if (or && result) {
                return true;
            }
        }
        if (and) {
            return true;
        } else if (or) {
            return false;
        } else {
            return false;
        }
    }
    
    protected Map<String, Object> createParams(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<String, Object>(params);
        for (String paramName : result.keySet()) {
            VariableScopeInstance variableScopeInstance = (VariableScopeInstance)
                getContextInstance(VariableScope.VARIABLE_SCOPE);
            Object value = variableScopeInstance.getVariable(paramName);
            if (value != null) {
                result.put(paramName, value);
            }
        }
        return result;
    }

    public List<Step> getCurrentSteps() {
        List<Step> result = new ArrayList<Step>();
        for (org.drools.runtime.process.NodeInstance nodeInstance: getNodeInstances()) {
            if (nodeInstance instanceof StepNodeInstance) {
                result.add((StepNodeInstance) nodeInstance);
            }
        }
        return result;
    }
    
    public void removeNodeInstance(final NodeInstance nodeInstance) {
        super.removeNodeInstance(nodeInstance);
        if (nodeInstance instanceof StepNodeInstance) {
            history.add(0, (StepNodeInstance) nodeInstance);
        }
    }
    
    private void checkImplicitFinish() {
        boolean finished = true;
        for (org.drools.runtime.process.NodeInstance nodeInstance: getNodeInstances()) {
            if (nodeInstance instanceof StepNodeInstance) {
                if (!((StepNodeInstance) nodeInstance).getAvailableActions().isEmpty()) {
                    finished = false;
                    break;
                }
            }
        }
        if (finished) {
            setState(ProcessInstance.STATE_COMPLETED);
        }
    }
    
    public List<Step> getHistorySteps() {
        return history;
    }

    public String getWorkflowName() {
        return getProcess().getName();
    }

    public boolean isInitialized() {
        return true;
    }
    
    public class DummyWorkflowStore implements WorkflowStore {

        public Step createCurrentStep(long entryId, int stepId, String owner,
                Date startDate, Date dueDate, String status, long[] previousIds)
                throws StoreException {
            // TODO Auto-generated method stub
            return null;
        }

        public WorkflowEntry createEntry(String workflowName)
                throws StoreException {
            // TODO Auto-generated method stub
            return null;
        }

        public List findCurrentSteps(long entryId) throws StoreException {
            if (entryId == getId()) {
                return getCurrentSteps();
            }
            return null;
        }

        public WorkflowEntry findEntry(long entryId) throws StoreException {
            // TODO Auto-generated method stub
            return null;
        }

        public List findHistorySteps(long entryId) throws StoreException {
            // TODO Auto-generated method stub
            return null;
        }

        public PropertySet getPropertySet(long entryId) throws StoreException {
            // TODO Auto-generated method stub
            return null;
        }

        public void init(Map props) throws StoreException {
            // TODO Auto-generated method stub
            
        }

        public Step markFinished(Step step, int actionId, Date finishDate,
                String status, String caller) throws StoreException {
            // TODO Auto-generated method stub
            return null;
        }

        public void moveToHistory(Step step) throws StoreException {
            // TODO Auto-generated method stub
            
        }

        public List query(WorkflowQuery query) throws StoreException {
            // TODO Auto-generated method stub
            return null;
        }

        public List query(WorkflowExpressionQuery query) throws StoreException {
            // TODO Auto-generated method stub
            return null;
        }

        public void setEntryState(long entryId, int state)
                throws StoreException {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    public class DummyWorkflowContext implements WorkflowContext {

        public String getCaller() {
            // TODO
            return "caller";
        }

        public void setRollbackOnly() {
            // TODO
        }
        
    }
    
}
