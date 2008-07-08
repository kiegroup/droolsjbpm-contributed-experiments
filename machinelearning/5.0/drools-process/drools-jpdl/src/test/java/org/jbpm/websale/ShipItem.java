package org.jbpm.websale;

import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;

public class ShipItem implements ActionHandler {

    private static final long serialVersionUID = 1L;
  
    String swimlaneName;
    String msg;

    public void execute(ExecutionContext executionContext) throws Exception {
        // TODO get real name of assigned person?
        String actorId = "Shipper";
        String displayMsg = replace(msg, "${"+swimlaneName+"}", actorId);
        displayMsg = replace(displayMsg, "${item}", (String) executionContext.getVariable("item")); 
        displayMsg = replace(displayMsg, "${address}", (String) executionContext.getVariable("address"));
    
        System.out.println("###############################################");
        System.out.println("### " + displayMsg);
        System.out.println("###############################################");
    
        executionContext.leaveNode();
    }

    private static String replace(String msg, String pattern, String replacement) {
        String replaced = null;
        int pos = msg.indexOf(pattern);
        if (pos!=-1) {
            replaced = msg.substring(0,pos) + replacement
                 + msg.substring(pos + pattern.length());
        }
        return replaced;
    }
}
