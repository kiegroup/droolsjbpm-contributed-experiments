package org.drools.jpdl.core.node;

import java.util.Set;

import org.jbpm.context.def.VariableAccess;

public class ProcessState extends JpdlNode {

	private static final long serialVersionUID = 1L;
	
	private Set<VariableAccess> variableAccesses;
	private String subProcessName;
	
	public Set<VariableAccess> getVariableAccesses() {
		return variableAccesses;
	}
	
	public void setVariableAccesses(Set<VariableAccess> variableAccesses) {
		this.variableAccesses = variableAccesses;
	}
	
	public String getSubProcessName() {
		return subProcessName;
	}
	
	public void setSubProcessName(String subProcessName) {
		this.subProcessName = subProcessName;
	}

}
