package org.drools.learner.eval;

import java.util.ArrayList;

import org.drools.learner.DecisionTree;
import org.drools.learner.InstanceList;

public interface Estimator {

	public int getEstimatorSize();
	public ArrayList<DecisionTree> getEstimators();
	public ArrayList<InstanceList> getFold(int id);
	public int getTrainingDataSize(int i);
	
	public double getErrorEstimate();
	public double getAlphaEstimate();
}
