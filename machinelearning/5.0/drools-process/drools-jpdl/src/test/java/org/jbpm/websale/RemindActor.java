package org.jbpm.websale;

import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;

public class RemindActor implements ActionHandler {

    private static final long serialVersionUID = 1L;
  
    String swimlaneName;

    public void execute(ExecutionContext executionContext) throws Exception {
        System.out.println("###############################################");
        System.out.println("### Task is waiting.");
        System.out.println("###############################################");
    }

}
