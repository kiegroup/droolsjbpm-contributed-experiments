package org.drools.learner.eval.stopping;

import org.drools.learner.eval.InformationContainer;


public class ImpurityDecrease implements StoppingCriterion {

	private double beta = 0.00001;
	private int num_prunned;
	
	public ImpurityDecrease() {
		num_prunned = 0;
	}
	
	public ImpurityDecrease(double _beta) {
		num_prunned = 0;
		beta = _beta;
	}
	public boolean stop(InformationContainer best_attr_eval) {
		if (best_attr_eval.attribute_eval < beta) {
			num_prunned ++;
			return true;
		} else
			return false;
	
	}
	
	public int getNumPruned(){
		return num_prunned;
	}

}
