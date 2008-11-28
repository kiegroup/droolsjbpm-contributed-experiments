package org.drools.jpdl.core.node;

import org.jbpm.graph.action.Script;

public class Fork extends JpdlNode {

    private static final long serialVersionUID = 1L;
    
    private Script script;

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }
    
}
