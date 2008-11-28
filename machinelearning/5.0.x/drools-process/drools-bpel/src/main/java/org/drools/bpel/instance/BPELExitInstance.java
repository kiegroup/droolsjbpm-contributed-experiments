package org.drools.bpel.instance;

import org.drools.workflow.instance.NodeInstance;
import org.drools.workflow.instance.node.EndNodeInstance;

public class BPELExitInstance extends EndNodeInstance {

	private static final long serialVersionUID = 1L;

    public void internalTrigger(NodeInstance from, String type) {
        if (BPELLinkManager.checkActivityEnabled(this)) {
            super.internalTrigger(from, type);
        }
    }
    
}
