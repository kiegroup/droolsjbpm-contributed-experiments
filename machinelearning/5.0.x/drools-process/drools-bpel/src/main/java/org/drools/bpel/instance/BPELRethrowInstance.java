package org.drools.bpel.instance;

import org.drools.bpel.core.BPELFaultHandlerScope;
import org.drools.process.core.context.exception.ExceptionScope;
import org.drools.process.core.context.variable.VariableScope;
import org.drools.process.instance.ProcessInstance;
import org.drools.process.instance.context.exception.ExceptionScopeInstance;
import org.drools.process.instance.context.variable.VariableScopeInstance;
import org.drools.runtime.process.NodeInstance;
import org.drools.runtime.process.NodeInstanceContainer;
import org.drools.workflow.instance.node.CompositeContextNodeInstance;
import org.drools.workflow.instance.node.FaultNodeInstance;

/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class BPELRethrowInstance extends FaultNodeInstance {

    private static final long serialVersionUID = 400L;
    
    public void internalTrigger(NodeInstance from, String type) {
        if (BPELLinkManager.checkActivityEnabled(this)) {
            super.internalTrigger(from, type);
        }
    }
    
    protected ExceptionScopeInstance getExceptionScopeInstance(String faultName) {
    	VariableScopeInstance variableScopeInstance = (VariableScopeInstance)
    		resolveContextInstance(VariableScope.VARIABLE_SCOPE, BPELFaultHandlerScope.INTERNAL_FAULT_NAME_VARIABLE);
		BPELScopeInstance scopeInstance = (BPELScopeInstance)
			((CompositeContextNodeInstance) variableScopeInstance.getContextInstanceContainer())
				.getNodeInstanceContainer();
		NodeInstanceContainer nodeInstanceContainer = scopeInstance.getNodeInstanceContainer();
		if (nodeInstanceContainer instanceof ProcessInstance) {
			return (ExceptionScopeInstance) 
				((ProcessInstance) nodeInstanceContainer)
					.getContextInstance(ExceptionScope.EXCEPTION_SCOPE);
		}
		if (nodeInstanceContainer instanceof NodeInstance) {
	    	return (ExceptionScopeInstance) 
	    		((org.drools.workflow.instance.NodeInstance) nodeInstanceContainer)
	    			.resolveContextInstance(ExceptionScope.EXCEPTION_SCOPE, faultName);
		}
		throw new IllegalArgumentException(
			"Could not find enclosing exception scope");
    }
    
    protected String getFaultName() {
    	VariableScopeInstance variableScopeInstance = (VariableScopeInstance)
    		resolveContextInstance(VariableScope.VARIABLE_SCOPE, BPELFaultHandlerScope.INTERNAL_FAULT_NAME_VARIABLE);
    	return (String) variableScopeInstance.getVariable(BPELFaultHandlerScope.INTERNAL_FAULT_NAME_VARIABLE);
    }
    
    protected Object getFaultData() {
    	VariableScopeInstance variableScopeInstance = (VariableScopeInstance)
    		resolveContextInstance(VariableScope.VARIABLE_SCOPE, BPELFaultHandlerScope.INTERNAL_FAULT_DATA_VARIABLE);
    	return variableScopeInstance.getVariable(BPELFaultHandlerScope.INTERNAL_FAULT_DATA_VARIABLE);
    }
    
    protected void handleException(String faultName, ExceptionScopeInstance exceptionScopeInstance) {
        super.handleException(faultName, exceptionScopeInstance);
        BPELLinkManager.activateTargetLinks(this);
    }
    
}
