package org.drools.learner.eval;


public class MaximumDepth implements StoppingCriterion {

	private int limit_depth;
	public MaximumDepth(int _depth) {
		limit_depth = _depth;
	}
	public boolean stop(InformationContainer best_attr_eval) {
		if (best_attr_eval.getDepth() <= limit_depth)
			return false;
		else 
			return true;
	}

}
