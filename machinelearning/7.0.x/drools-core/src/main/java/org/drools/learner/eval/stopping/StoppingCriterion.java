package org.drools.learner.eval.stopping;

import org.drools.learner.eval.InformationContainer;

public interface StoppingCriterion {
	
	public boolean stop(InformationContainer best_attr_eval);
	public int getNumPruned();

}
