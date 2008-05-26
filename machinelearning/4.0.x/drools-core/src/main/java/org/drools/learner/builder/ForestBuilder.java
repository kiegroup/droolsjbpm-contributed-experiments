package org.drools.learner.builder;

import java.util.ArrayList;

import org.drools.learner.AttributeValueComparator;
import org.drools.learner.DecisionTree;
import org.drools.learner.Domain;
import org.drools.learner.Instance;
import org.drools.learner.InstanceList;
import org.drools.learner.Memory;
import org.drools.learner.Stats;
import org.drools.learner.eval.ClassDistribution;
import org.drools.learner.tools.Util;

public class ForestBuilder {
	
	private int DOMAIN_TYPE;
	private int TREE_SET = Util.BAG; // default bagging, TODO boosting
	
	private static final int FOREST_SIZE = 10;
	private static final double TREE_SIZE_RATIO = 0.9;
	private static final boolean BAGGING_WITH_REP = true;
	
	private ArrayList<DecisionTree> forest;
	
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
		
		if (Util.DEBUG_LEARNER) {
			for (Instance inst: class_instances.getInstances()) {
				System.out.println("Inst: "+ inst);
			}
		}
		
		int N = class_instances.getSize();
		int tree_capacity = (int)(TREE_SIZE_RATIO * N);
		trainer.setDataSize(tree_capacity);

		
		forest = new ArrayList<DecisionTree> (FOREST_SIZE);
		int i = 0;
		int[] bag;		
		while (i++ < FOREST_SIZE) {
			if (BAGGING_WITH_REP)
				bag = Util.bag_w_rep(tree_capacity, N);
			else
				bag = Util.bag_wo_rep(tree_capacity, N);
			
			InstanceList working_instances = class_instances.getInstances(bag);			
			DecisionTree dt = trainer.train_tree(working_instances);
			
			dt.setID(i);
			forest.add(dt);
			System.out.print('.');
		}
		// TODO how to compute a best tree from the forest
		trainer.setBestTree(forest.get(0));
		//this.c45 = dt;
	}

	public Object voteOn(Instance i) {
		
		Domain target_domain = forest.get(0).getTargetDomain();	// all must have the same target domain
		ClassDistribution classification = new ClassDistribution(target_domain);

		for (int j = 0; j< forest.size() ; j ++) {
			Object vote = forest.get(j).vote(i);
			if (vote != null)
				classification.change(vote, 1);
			else {
				// TODO add an unknown value
				//classification.change(-1, 1);
				System.out.println(Util.ntimes("\n", 10)+"Unknown situation at tree: " + j + " for fact "+ i);
				System.exit(0);
			}
		}
		classification.evaluateMajority();
		Object winner = classification.get_winner_class();
		
		
		double ratio = 0.0;
		if (classification.get_num_ideas() == 1) {
			//100 %
			ratio = 1;
			return winner;
		} else {
			int num_votes = classification.getVoteFor(winner);
			ratio = ((double) num_votes/(double) forest.size());
			// TODO if the ratio is smaller than some number => reject
		}
		return winner;
		
	}
	
	public void test(InstanceList data) {

		Stats evaluation = new Stats(data.getSchema().getObjectClass()) ; //represent.getObjClass());
		Domain targetDomain = forest.get(0).getTargetDomain();
		int i = 0;
		for (Instance instance : data.getInstances()) {
			Object forest_decision = this.voteOn(instance);
			Integer result = evaluate(targetDomain, instance, forest_decision);
			
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
