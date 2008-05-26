package org.drools.learner.builder;


import org.drools.learner.Memory;

public interface DecisionTreeBuilder {
	
	void build(Memory wm, Learner l);
	
}
