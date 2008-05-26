package org.drools.learner.builder;

import org.drools.learner.AttributeValueComparator;
import org.drools.learner.DecisionTree;
import org.drools.learner.Domain;
import org.drools.learner.Instance;
import org.drools.learner.InstanceList;
import org.drools.learner.Memory;
import org.drools.learner.Stats;
import org.drools.learner.tools.Util;

public class SingleTreeBuilder implements DecisionTreeBuilder{
	
	DecisionTree one_tree;
	
	private int DOMAIN_TYPE;
	private int TREE_SET = Util.SINGLE; // default bagging, TODO boosting
	
	/* 
	 * the memory has the information 
	 * the instances: the objects which the decision tree will work on
	 * the schema: the definition of the object instance 
	 * 			(Class<?>) klass, String targetField, List<String> workingAttributes
	 */
	public void build(Memory mem, Learner trainer) {
		DOMAIN_TYPE = trainer.getDomainType();
		
		
		InstanceList class_instances = mem.getClassInstances();
		trainer.setInputData(class_instances);
		if (class_instances.getTargets().size()>1) {
			//throw new FeatureNotSupported("There is more than 1 target candidates");
			System.out.println("There is more than 1 target candidates");
			System.exit(0);
			// TODO put the feature not supported exception || implement it
		}
		
		if (Util.DEBUG_LEARNER)
			for (Instance inst: class_instances.getInstances()) {
				System.out.println("Inst: "+ inst);
			}
		
		trainer.setDataSize(class_instances.getSize());
		one_tree = trainer.train_tree(class_instances);
		trainer.setBestTree(one_tree);
	}
	
	/* test the entire set*/
	public void test(InstanceList data) {
		if (this.one_tree == null) {
			System.out.println("The tree is not created");
			System.exit(0);
		}
		
		if (Util.DEBUG_TEST) {
			System.out.println(Util.ntimes("\n", 2)+Util.ntimes("$", 5)+" TESTING "+Util.ntimes("\n", 2));
		}
		
		Stats evaluation = new Stats(this.one_tree.getObjClass());
		int i = 0;
		for (Instance instance : data.getInstances()) {
			Object tree_decision = this.one_tree.vote(instance);
			Integer result = evaluate(this.one_tree.getTargetDomain(), instance, tree_decision);
			if (Util.DEBUG_TEST) {
				System.out.println(Util.ntimes("#\n", 1)+i+ " <START> TEST: instant="+ instance + " = target "+ result);
			} else {
				if (i%1000 ==0)	System.out.print(".");
			}
			evaluation.change(result, 1);
			i ++;
		}
		printStats(evaluation);
	}
	
	private static Integer evaluate (Domain targetDomain, Instance i, Object tree_decision) {	
		String targetFName = targetDomain.getFName();
		
		Object tattr_value = i.getAttrValue(targetFName);
		Object i_category = targetDomain.getCategoryOf(tattr_value);
		
		if (AttributeValueComparator.instance.compare(i_category, tree_decision) == 0) {
			return Integer.valueOf(1); 	//correct
		} else {
			return Integer.valueOf(0);	// mistake
		} 
	}
	
	private void printStats(Stats evaluation) {
		if (Util.PRINT_STATS) {
			if (Util.DEBUG_TEST) {
				evaluation.print2out();
			}
			evaluation.print2file("", DOMAIN_TYPE, TREE_SET);
		}
	}

}
