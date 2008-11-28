package org.drools.bpel.instance;

import org.drools.process.instance.context.exception.ExceptionScopeInstance;
import org.drools.workflow.instance.NodeInstance;
import org.drools.workflow.instance.node.FaultNodeInstance;

/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class BPELThrowInstance extends FaultNodeInstance {

    private static final long serialVersionUID = 400L;
    
    public void internalTrigger(NodeInstance from, String type) {
        if (BPELLinkManager.checkActivityEnabled(this)) {
            super.internalTrigger(from, type);
        }
    }
    
    protected void handleException(String faultName, ExceptionScopeInstance exceptionScopeInstance) {
        super.handleException(faultName, exceptionScopeInstance);
        BPELLinkManager.activateTargetLinks(this);
    }
}
