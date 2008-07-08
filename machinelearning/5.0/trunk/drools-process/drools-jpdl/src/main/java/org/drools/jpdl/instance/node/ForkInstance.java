package org.drools.jpdl.instance.node;

import java.util.Collection;
import java.util.Map;

import org.drools.jpdl.core.node.Fork;
import org.drools.workflow.instance.NodeInstance;
import org.jbpm.JbpmException;
import org.jbpm.graph.action.Script;

public class ForkInstance extends JpdlNodeInstance {

    private static final long serialVersionUID = 1L;
    
    public Fork getFork() {
        return (Fork) getNode();
    }

    public void execute(NodeInstance from, String type) {
        Collection<String> transitionNames = null;
        Script script = getFork().getScript();
        if (script == null) {
            transitionNames = getNode().getOutgoingConnections().keySet();
        } else {
            Map<String, Object> outputMap = null;
            try {
                outputMap = script.eval(new JpdlExecutionContext());
            } catch (Exception e) {
                this.raiseException(e);
            }
            if (outputMap.size() == 1) {
                Object result = outputMap.values().iterator().next();
                if (result instanceof Collection) {
                    transitionNames = (Collection<String>) result;
                }
            }
            if (transitionNames == null) {
                throw new JbpmException(
                    "script for fork '" + getNode().getName() +
                    "' should produce one collection (in one writable variable): " +
                    transitionNames);
            }
        }
        for (String transitionName: transitionNames) {
            leave(transitionName);
        }
    }
}
