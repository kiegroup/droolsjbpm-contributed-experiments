package org.drools.bpel.instance;

import org.drools.bpel.core.BPELFaultHandler;
import org.drools.bpel.core.BPELFaultHandlerScope;
import org.drools.process.core.context.exception.ExceptionHandler;
import org.drools.process.core.context.variable.VariableScope;
import org.drools.process.instance.context.exception.ExceptionHandlerInstance;
import org.drools.process.instance.context.variable.VariableScopeInstance;
import org.drools.workflow.core.Node;
import org.drools.workflow.instance.NodeInstanceContainer;
import org.drools.workflow.instance.impl.NodeInstanceImpl;
import org.drools.workflow.instance.node.CompositeContextNodeInstance;

public class BPELFaultHandlerInstance implements ExceptionHandlerInstance {

    private BPELFaultHandler faultHandler;
    private NodeInstanceContainer nodeInstanceContainer; 
    
    public BPELFaultHandler getFaultHandler() {
        return faultHandler;
    }

    public void setExceptionHandler(ExceptionHandler handler) {
        this.faultHandler = (BPELFaultHandler) handler;
    }

    public NodeInstanceContainer getNodeInstanceContainer() {
        return nodeInstanceContainer;
    }

    public void setNodeInstanceContainer(NodeInstanceContainer nodeInstanceContainer) {
        this.nodeInstanceContainer = nodeInstanceContainer;
    }

    public void handleException(String exception, Object param) {
        if (exception == null) {
            throw new IllegalArgumentException(
                "Exception is null!");
        }
        if (!(faultHandler.getFaultName() == null || exception.equals(faultHandler.getFaultName()))) {
            throw new IllegalArgumentException(
                "Cannot handle exception: " + exception);
        }
        CompositeContextNodeInstance nodeInstance = (CompositeContextNodeInstance)
        	nodeInstanceContainer.getNodeInstance((BPELFaultHandlerScope)
    			faultHandler.getActivity().getNodeContainer());
        VariableScopeInstance variableScopeInstance =
        	((VariableScopeInstance) nodeInstance.resolveContextInstance(
    			VariableScope.VARIABLE_SCOPE, BPELFaultHandlerScope.INTERNAL_FAULT_NAME_VARIABLE));
    	variableScopeInstance.setVariable(BPELFaultHandlerScope.INTERNAL_FAULT_NAME_VARIABLE, exception);
    	variableScopeInstance.setVariable(BPELFaultHandlerScope.INTERNAL_FAULT_DATA_VARIABLE, param);
        String faultVariable = faultHandler.getFaultVariable();
        if (faultVariable != null) {
            variableScopeInstance = (VariableScopeInstance)
                ((NodeInstanceImpl) nodeInstance).resolveContextInstance(VariableScope.VARIABLE_SCOPE, faultHandler.getFaultVariable());
            if (variableScopeInstance != null) {
                variableScopeInstance.setVariable(faultVariable, (String) param);
            } else {
                System.err.println("Could not find variable scope for variable " + faultVariable);
                System.err.println("when trying handle fault " + exception);
                System.err.println("Continuing without setting variable.");
            }
        }
        nodeInstance.trigger(null, Node.CONNECTION_DEFAULT_TYPE);
    }

}
