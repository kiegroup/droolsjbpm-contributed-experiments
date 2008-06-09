package org.drools.learner.builder;


import org.drools.learner.Memory;

public interface DecisionTreeBuilder {
	
	//public static final int SINGLE = 1, BAG = 2, BOOST = 3;
	public static enum TreeAlgo { SINGLE, BAG, BOOST, BOOST_K }
	
	void build(Memory wm, Learner trainer);
	
//	public Learner getLearner();
	
	public TreeAlgo getTreeAlgo();
	
}
