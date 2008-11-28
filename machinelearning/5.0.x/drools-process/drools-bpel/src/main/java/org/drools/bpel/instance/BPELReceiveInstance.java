package org.drools.bpel.instance;

import org.drools.bpel.core.BPELReceive;
import org.drools.process.core.context.variable.VariableScope;
import org.drools.process.instance.context.variable.VariableScopeInstance;
import org.drools.runtime.process.NodeInstance;
import org.drools.workflow.core.Node;
import org.drools.workflow.instance.impl.NodeInstanceImpl;
import org.drools.workflow.instance.node.EventNodeInstanceInterface;

/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class BPELReceiveInstance extends NodeInstanceImpl implements EventNodeInstanceInterface {

    private static final long serialVersionUID = 400L;

    private boolean triggered = false;
    private boolean event = false;
    private String message;
    
    public BPELReceive getBPELReceive() {
        return (BPELReceive) getNode();
    }
    
    public void internalTrigger(NodeInstance from, String type) {
        if (BPELLinkManager.checkActivityEnabled(this)) {
            triggered = true;
            if (triggered && event) {
            	triggerCompleted();
            }
        }
    }
    
    public void triggerCompleted() {
        String variable = getBPELReceive().getVariable();
        if (variable != null) {
            VariableScopeInstance variableScope = (VariableScopeInstance) resolveContextInstance(VariableScope.VARIABLE_SCOPE, variable);
            if (variableScope == null) {
                throw new IllegalArgumentException(
                    "Variable " + variable + " not found!");
            }
            variableScope.setVariable(variable, message);
        }
        triggerCompleted(Node.CONNECTION_DEFAULT_TYPE, true);
        BPELLinkManager.activateTargetLinks(this);
    }

	public void signalEvent(String type, Object event) {
		this.event = true;
		message = ((String[]) event)[3];
		if (this.event && triggered) {
			triggerCompleted();
		}
	}

}
