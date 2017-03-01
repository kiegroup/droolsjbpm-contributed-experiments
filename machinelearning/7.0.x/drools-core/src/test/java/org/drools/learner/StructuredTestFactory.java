package org.drools.learner;

import org.drools.core.WorkingMemory;
import org.drools.learner.builder.C45Learner;
import org.drools.learner.builder.Learner;
import org.drools.learner.builder.SingleTreeBuilder;
import org.drools.learner.builder.Learner.DataType;
import org.drools.learner.builder.SingleTreeTester;
import org.drools.learner.builder.DecisionTreeFactory;
import org.drools.learner.eval.heuristic.Entropy;
import org.drools.learner.tools.FeatureNotSupported;

public class StructuredTestFactory extends DecisionTreeFactory{

	
	public static DecisionTree testStructuredMemory(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		
		DataType data = Learner.DEFAULT_DATA;
		Entropy heuristic = new Entropy();
		C45Learner learner = new C45Learner(heuristic);
		
		SingleTreeBuilder single_builder = new SingleTreeBuilder();
		
		String algo_suffices = DecisionTreeFactory.getAlgoSuffices(learner.getDomainAlgo(), single_builder.getTreeAlgo());
		String executionSignature = DecisionTreeFactory.getSignature(obj_class, "", algo_suffices);
		
		/* create the memory */
		Memory mem = Memory.createFromWorkingMemory(wm, obj_class, learner.getDomainAlgo(), data);
		boolean BUILD_TREE = false;
		if (BUILD_TREE) {
			single_builder.build(mem, learner);//obj_class, target_attr, working_attr

			SingleTreeTester tester = new SingleTreeTester(single_builder.getBestSolution().getTree());
			//tester.printStats(tester.test(mem.getClassInstances()), Util.DRL_DIRECTORY + executionSignature);
			//Tester.test(c45, mem.getClassInstances());

			single_builder.getBestSolution().getTree().setSignature(executionSignature);
		}
		return single_builder.getBestSolution().getTree();
	}
}
