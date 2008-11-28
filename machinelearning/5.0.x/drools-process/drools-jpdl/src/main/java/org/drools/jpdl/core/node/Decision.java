package org.drools.jpdl.core.node;

import java.util.List;

import org.jbpm.graph.node.DecisionCondition;
import org.jbpm.instantiation.Delegation;

public class Decision extends JpdlNode {

	private static final long serialVersionUID = 1L;

	private List<DecisionCondition> decisionConditions;
	private Delegation decisionDelegation;
	private String decisionExpression;

	public List<DecisionCondition> getDecisionConditions() {
		return decisionConditions;
	}

	public void setDecisionConditions(List<DecisionCondition> decisionConditions) {
		this.decisionConditions = decisionConditions;
	}

	public Delegation getDecisionDelegation() {
		return decisionDelegation;
	}

	public void setDecisionDelegation(Delegation decisionDelegation) {
		this.decisionDelegation = decisionDelegation;
	}

	public String getDecisionExpression() {
		return decisionExpression;
	}

	public void setDecisionExpression(String decisionExpression) {
		this.decisionExpression = decisionExpression;
	}

}
