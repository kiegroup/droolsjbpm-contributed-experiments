package org.drools.jpdl.instance;

import org.drools.jpdl.core.JpdlProcess;
import org.drools.workflow.instance.impl.WorkflowProcessInstanceImpl;

public class JpdlProcessInstance extends WorkflowProcessInstanceImpl {

	private static final long serialVersionUID = 1L;

	public JpdlProcess getJpdlProcess() {
		return (JpdlProcess) getProcess();
	}
	
    public void internalStart() {
        getNodeInstance( getJpdlProcess().getStart() ).trigger( null, null );
    }

}
