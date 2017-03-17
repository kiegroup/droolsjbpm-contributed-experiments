package org.drools.learner.eval;

import java.util.ArrayList;

import org.drools.learner.DecisionTree;
import org.drools.learner.InstanceList;
import org.drools.learner.builder.Learner;

public interface ErrorEstimate {

	public void validate(Learner _trainer, InstanceList _instances);
	
	public int getEstimatorSize();
	public DecisionTree getEstimator(int i);
	public ArrayList<InstanceList> getSets(int id);
	public int getTestDataSize(int i);
	public int getTrainingDataSize(int i);
	
	public double getErrorEstimate();
	public double getAlphaEstimate();

	public void setTrainingDataSize(int trainingDataSize);
}
