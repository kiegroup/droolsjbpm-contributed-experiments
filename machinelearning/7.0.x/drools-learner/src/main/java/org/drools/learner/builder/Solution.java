package org.drools.learner.builder;

import org.drools.learner.DecisionTree;
import org.drools.learner.InstanceList;
import org.drools.learner.Stats;

public class Solution {
	
	private DecisionTree dt;
	private InstanceList training_list;
	
	private InstanceList test_list;
	private Stats train_stats, test_stats;

	public Solution(DecisionTree _dt, InstanceList list) {
		dt = _dt;
		training_list = list;
	}

	
	public DecisionTree getTree() {
		return dt;
	}

	public InstanceList getList() {
		return training_list;
	}
	
	public InstanceList getTestList() {
		return test_list;
	}

	public void setTestList(InstanceList test) {
		test_list = test;
	}

	public void setTrainStats(Stats train) {
		train_stats = train;
	}
	
	public void setTestStats(Stats test) {
		test_stats = test;
	}
	
	public Stats getTrainStats() {
		return train_stats;
	}
	
	public Stats getTestStats() {
		return test_stats;
	}

	public double getTrainError() {
		System.out.println("Total Train"+ train_stats.getTotal()+ ", size "+ training_list.getSize());
		return (double)train_stats.getResult(Stats.INCORRECT)/(double)train_stats.getTotal();
	}

	public double getTestError() {
		System.out.println("Total Test"+ test_stats.getTotal()+ ", size "+ test_list.getSize());
		return (double)test_stats.getResult(Stats.INCORRECT)/(double)test_stats.getTotal();
	}

	public void changeTrainError(int change) {		
		train_stats.change(Stats.INCORRECT, change);
		train_stats.change(Stats.CORRECT, -1*change);	
	}
	
	public void setError(int change) {		
		// TODO test_stats.	
		System.out.println("Solution setError() doing nothing");
		System.exit(0);
	}
}
