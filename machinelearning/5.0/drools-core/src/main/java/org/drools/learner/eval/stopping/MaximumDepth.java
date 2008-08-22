package org.drools.learner.eval.stopping;

import org.drools.learner.eval.InformationContainer;


public class MaximumDepth implements StoppingCriterion {

	private int limit_depth;
	private int num_prunned;
	public MaximumDepth(int _depth) {
		limit_depth = _depth;
		num_prunned = 0;
	}
	public boolean stop(InformationContainer best_attr_eval) {
		if (best_attr_eval.getDepth() <= limit_depth)
			return false;
		else {
			num_prunned++;
			return true;
		}
	}
	public int getNumPruned() {
		return num_prunned;
	}
	public void setDepth(int max_depth) {
		limit_depth = max_depth;
	}

}
