package org.drools.learner.builder;

import java.util.ArrayList;

import org.drools.learner.DecisionTree;
import org.drools.learner.Instance;
import org.drools.learner.InstanceList;
import org.drools.learner.Stats;
import org.drools.learner.eval.stopping.StoppingCriterion;

public class SingleTreeTester extends Tester{
	
	private DecisionTree single_tree; 
	
	public SingleTreeTester(DecisionTree one_tree) {
		single_tree = one_tree;
	}
	
	/* test the entire set*/
	public Stats test(InstanceList data) {
		
		//flog.debug(Util.ntimes("\n", 2)+Util.ntimes("$", 5)+" TESTING "+Util.ntimes("\n", 2));
		
		Stats evaluation = new Stats(single_tree.getObjClass());
		int i = 0;
		for (Instance instance : data.getInstances()) {
			Object tree_decision = single_tree.vote(instance);
			Integer result = evaluate(single_tree.getTargetDomain(), instance, tree_decision);
			
			//flog.debug(Util.ntimes("#\n", 1)+i+ " <START> TEST: instant="+ instance + " = target "+ result);			
//			if (i%1000 ==0 && slog.stat() !=null)
//				slog.stat().stat(".");

			evaluation.change(result, 1);
			i ++;
		}
		return evaluation;
	}
	
	/* test the entire set*/
	public Integer test(Instance i) {
		
		Object tree_decision = single_tree.vote(i);
		return evaluate(single_tree.getTargetDomain(), i, tree_decision);
	}
	
//	public void printStats(Stats evaluation, String executionSignature, boolean append) {
//		super.printStats(evaluation, executionSignature, append);
//	}

}
