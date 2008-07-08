package org.drools.jpdl.core;

import org.drools.jpdl.core.node.StartState;
import org.drools.process.core.context.variable.VariableScope;
import org.drools.workflow.core.Node;
import org.drools.workflow.core.impl.WorkflowProcessImpl;

public class JpdlProcess extends WorkflowProcessImpl {

    public static final String JPDL_TYPE = "jPDL";

	private static final long serialVersionUID = 1L;
	
	private Node startState;

    public JpdlProcess() {
        setType(JPDL_TYPE);
        VariableScope variableScope = new VariableScope();
        addContext(variableScope);
        setDefaultContext(variableScope);
    }
    
    public void setStartState(Node startState) {
        this.startState = startState;
    }
    
    public Node getStart() {
        if (startState != null) {
            return startState;
        }
        Node[] nodes = getNodes();
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] instanceof StartState) {
                return (StartState) nodes[i];
            }
        }
        return null;
    }

    public VariableScope getVariableScope() {
        return (VariableScope) getDefaultContext(VariableScope.VARIABLE_SCOPE);
    }

}
