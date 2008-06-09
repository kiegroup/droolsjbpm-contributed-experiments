package org.drools.learner.builder;

import java.util.ArrayList;

import org.drools.learner.DecisionTree;
import org.drools.learner.InstanceList;
import org.drools.learner.Memory;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;
import org.drools.learner.tools.Util;

public class ForestBuilder implements DecisionTreeBuilder{

	private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(ForestBuilder.class, SimpleLogger.DEFAULT_LEVEL);
	private static SimpleLogger slog = LoggerFactory.getSysOutLogger(ForestBuilder.class, SimpleLogger.DEFAULT_LEVEL);
	
	private TreeAlgo algorithm = TreeAlgo.BAG; // default bagging, TODO boosting
	
	private static final int FOREST_SIZE = 50;
	private static final double TREE_SIZE_RATIO = 0.9;
	private static final boolean WITH_REP = true;
	
	private ArrayList<DecisionTree> forest;
	//private Learner trainer;
	
	public ForestBuilder() {
		//this.trainer = _trainer;
	}
	public void build(Memory mem, Learner _trainer) {
		
		final InstanceList class_instances = mem.getClassInstances();
		_trainer.setInputData(class_instances);
		
		if (class_instances.getTargets().size()>1) {
			//throw new FeatureNotSupported("There is more than 1 target candidates");
			if (flog.error() !=null)
				flog.error().log("There is more than 1 target candidates");
			System.exit(0);
			// TODO put the feature not supported exception || implement it
		}
	
		int N = class_instances.getSize();
		int tree_capacity = (int)(TREE_SIZE_RATIO * N);
		_trainer.setDataSizePerTree(tree_capacity);

		
		forest = new ArrayList<DecisionTree> (FOREST_SIZE);
		int i = 0;
		int[] bag;		
		while (i++ < FOREST_SIZE) {
			if (WITH_REP)
				bag = Util.bag_w_rep(tree_capacity, N);
			else
				bag = Util.bag_wo_rep(tree_capacity, N);
			
			InstanceList working_instances = class_instances.getInstances(bag);			
			DecisionTree dt = _trainer.train_tree(working_instances);
			
			dt.setID(i);
			forest.add(dt);

			if (slog.stat() !=null)
				slog.stat().stat(".");

		}
		// TODO how to compute a best tree from the forest
		_trainer.setBestTree(forest.get(0));
		
		//this.c45 = dt;
	}
	
	public TreeAlgo getTreeAlgo() {
		return algorithm; //TreeAlgo.BAG; // default
	}
	
	public ArrayList<DecisionTree> getTrees() {
		return forest;
	}
}
