package org.jbpm.websale;

import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;

public class UpdateBooks implements ActionHandler {
  
    String msg;

    private static final long serialVersionUID = 1L;

    public void execute(ExecutionContext executionContext) throws Exception {
        System.out.println("###############################################");
        System.out.println("### updating the accounting books");
        System.out.println("###############################################");
        executionContext.leaveNode();
    }

}
