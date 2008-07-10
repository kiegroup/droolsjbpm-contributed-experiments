package org.drools.learner.eval;

import java.util.ArrayList;

import org.drools.learner.DecisionTree;

public interface Estimator {

	public int getEstimatorSize();
	public ArrayList<DecisionTree> getEstimators();
}
