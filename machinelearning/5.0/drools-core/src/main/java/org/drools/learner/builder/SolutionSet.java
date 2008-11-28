package org.drools.learner.builder;

import java.util.ArrayList;
import java.util.Collection;

import org.drools.learner.DecisionTree;
import org.drools.learner.InstanceList;
import org.drools.learner.Memory;
import org.drools.learner.Stats;

public class SolutionSet {
	
	private Memory mem;
	private ArrayList<Solution> sol_trees;
	private InstanceList global_train_set, global_test_set;
	private Stats global_train_stats, global_test_stats;
	private int best_solution_id;

	public SolutionSet(Memory _mem) {
		mem = _mem;
		best_solution_id = 0;
		sol_trees = new ArrayList<Solution>(1);
	}
	
	public Collection<String> getTargets() {
		return mem.getClassInstances().getTargets();
	}
	
	public InstanceList getInputSpec() {
		return mem.getClassInstances();
	}
	
//	public InstanceList getValidationSet

	public InstanceList getTrainSet() {
		return mem.getTrainSet();
	}
	
	public InstanceList getTestSet() {
		return mem.getTestSet();
	}
	
	public void addSolution(Solution s) {
		sol_trees.add(s);
	}

	public void addSolution(DecisionTree tree, InstanceList set, InstanceList test_set) {
		Solution x = new Solution(tree, set);
		x.setTestList(test_set);
		addSolution(x);	
	}
	
	public ArrayList<Solution> getSolutions() {
		return sol_trees;
	}
	
	public Stats getGlobalTrainStats() {
		return global_train_stats;
	}
	
	public Stats getGlobalTestStats() {
		return global_test_stats;
	}

	public void setGlobalTrainStats(Stats train) {
		global_train_stats = train;
	}
	
	public void setGlobalTestStats(Stats test) {
		global_test_stats = test;
	}

	public void setBestSolutionId(int best_id) {
		best_solution_id = best_id;
	}
	
	public int getBestSolutionId() {
		return best_solution_id;
	}
	public Solution getBestSolution() {
		return sol_trees.get(best_solution_id);
	}
	
	public int getMinTestId() {
		double min = 1.0;
		int id = -1;
		for (int i=0; i< sol_trees.size(); i++ ) {
			double test_error = sol_trees.get(i).getTestError();
			double train_error = sol_trees.get(i).getTrainError();
			if (test_error < min) {
				min = test_error;
				id = i;
			} else if (test_error == min) {
				double train_old = sol_trees.get(id).getTrainError();
				if (train_error < train_old) {
					min = test_error;
					id = i;
				}
			}
			
		}
		return id;
		
	}

}
