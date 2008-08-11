package org.drools.learner.eval.stopping;

import org.drools.learner.eval.InformationContainer;


public class ImpurityDecrease implements StoppingCriterion {

	private double beta = 0.1;
	
	public ImpurityDecrease(double _beta) {
		beta = _beta;
	}
	public boolean stop(InformationContainer best_attr_eval) {
		if (best_attr_eval.attribute_eval < beta)
			return true;
		else
			return false;
	}

}
