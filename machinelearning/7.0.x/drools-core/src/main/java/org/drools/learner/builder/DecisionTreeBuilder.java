package org.drools.learner.builder;


import org.drools.learner.DecisionTree;
import org.drools.learner.Memory;
import org.drools.learner.builder.SingleTreeTester;
import org.drools.learner.builder.Tester;

public abstract class DecisionTreeBuilder {
	
	//public static final int SINGLE = 1, BAG = 2, BOOST = 3;
	public static enum TreeAlgo { SINGLE, BAG, BOOST, BOOST_K }
	
	public SolutionSet solutions;
	public int best_solution_id;
	
	public SolutionSet build(Memory wm, Learner trainer) {
		solutions = beforeBuild(wm);
		internalBuild(solutions, trainer);
		return solutions;
	}
	
	protected SolutionSet beforeBuild(Memory wm) {
		return new SolutionSet(wm);
	}
	
	protected abstract void internalBuild(SolutionSet sol, Learner trainer);
	
	public abstract TreeAlgo getTreeAlgo();
	
	public abstract Solution getBestSolution();
	
	
	public Tester getTester(DecisionTree dt) {
		return new SingleTreeTester(dt);
	}
	
	
}
