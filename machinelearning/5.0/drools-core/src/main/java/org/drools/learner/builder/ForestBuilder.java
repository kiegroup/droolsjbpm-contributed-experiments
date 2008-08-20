package org.drools.learner.builder;

import java.util.ArrayList;

import org.drools.learner.DecisionTree;
import org.drools.learner.InstanceList;
import org.drools.learner.Memory;
import org.drools.learner.Stats;
import org.drools.learner.StatsPrinter;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;
import org.drools.learner.tools.Util;

public class ForestBuilder implements DecisionTreeBuilder{

	private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(ForestBuilder.class, SimpleLogger.DEFAULT_LEVEL);
	private static SimpleLogger slog = LoggerFactory.getSysOutLogger(ForestBuilder.class, SimpleLogger.DEFAULT_LEVEL);
	
	private TreeAlgo algorithm = TreeAlgo.BAG; // default bagging, TODO boosting
	
	private double trainRatio = Util.TRAINING_RATIO, testRatio = Util.TESTING_RATIO;
	
	private static final int FOREST_SIZE = Util.NUM_TREES;
	private static final double TREE_SIZE_RATIO = 0.9;
	private static final boolean WITH_REP = true;
	
	private ArrayList<DecisionTree> forest;
	//private Learner trainer;
	
	private DecisionTree best;
	
	private DecisionTreeMerger merger;
	private ForestTester tester;
//	private SingleTreeTester single_tester;
	
	private ArrayList<Stats> train_evaluation, test_evaluation;
	
	public ForestBuilder() {
		//this.trainer = _trainer;
		merger = new DecisionTreeMerger();
		train_evaluation = new ArrayList<Stats>(FOREST_SIZE);
		test_evaluation = new ArrayList<Stats>(FOREST_SIZE);
	}
	
	public void setTrainRatio(double ratio) {
		trainRatio = ratio;
	}
	public void setTestRatio(double ratio) {
		testRatio = ratio;
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
	
		int split_idx =  (int)(trainRatio * class_instances.getSize());
		int split_idx2 =  split_idx + (int)(testRatio * class_instances.getSize());
		
		InstanceList train_instances = class_instances.subList(0, split_idx);
		InstanceList test_instances = class_instances.subList(split_idx, split_idx2);//class_instances.getSize());
		
		
		int N = train_instances.getSize();
		int tree_capacity = (int)(TREE_SIZE_RATIO * N);
		_trainer.setTrainingDataSizePerTree(tree_capacity);
		/* tree_capacity number of data fed to each tree, there are FOREST_SIZE trees*/
		_trainer.setTrainingDataSize(tree_capacity * FOREST_SIZE); 
		
//		int N = class_instances.getSize();
//		// _trainer.setTrainingDataSize(N); => wrong
//		int tree_capacity = (int)(TREE_SIZE_RATIO * N);
//		_trainer.setTrainingDataSizePerTree(tree_capacity);
//		
//		/* tree_capacity number of data fed to each tree, there are FOREST_SIZE trees*/
//		_trainer.setTrainingDataSize(tree_capacity * FOREST_SIZE); 

		
		forest = new ArrayList<DecisionTree> (FOREST_SIZE);
		int i = 0;
		int[] bag;		
		while (i++ < FOREST_SIZE) {
			if (WITH_REP)
				bag = Util.bag_w_rep(tree_capacity, N);
			else
				bag = Util.bag_wo_rep(tree_capacity, N);
			
			//InstanceList working_instances = class_instances.getInstances(bag);	
			InstanceList working_instances = train_instances.getInstances(bag);	
			
			DecisionTree dt = _trainer.instantiate_tree();
			if (slog.debug() != null)
				slog.debug().log("\n"+"Training a tree"+"\n");
			_trainer.train_tree(dt, working_instances);
			if (slog.debug() != null)
				slog.debug().log("\n"+"the end"+ "\n");
			dt.setID(i);
			forest.add(dt);
			// the DecisionTreeMerger will visit the decision tree and add the paths that have not been seen yet to the list
			merger.add(dt);	

			if (slog.stat() !=null)
				slog.stat().stat(".");
			SingleTreeTester single_tester = new SingleTreeTester(dt);
			
			train_evaluation.add(single_tester.test(train_instances));
			test_evaluation.add(single_tester.test(test_instances));

		}
		
		tester = new ForestTester(forest);
		train_evaluation.add(tester.test(train_instances));
		test_evaluation.add(tester.test(test_instances));
		
		//System.exit(0);
		// TODO how to compute a best tree from the forest
		int best_id = getMinTestId();
		best = merger.getBest();
		if (best == null)
			best = forest.get(best_id);
		
		train_evaluation.add(train_evaluation.get(best_id));
		test_evaluation.add(test_evaluation.get(best_id));
		//_trainer.setBestTree(best);// forest.get(0));
		
		//this.c45 = dt;
	}

	public TreeAlgo getTreeAlgo() {
		return algorithm; //TreeAlgo.BAG; // default
	}
	
	public ArrayList<DecisionTree> getTrees() {
		return forest;
	}
	
	public DecisionTree getTree() {
		return best;
	}
	
	public int getMinTestId() {
		double min = 1.0;
		int id = -1;
		for (int i=0; i< FOREST_SIZE; i++ ) {
			Stats test_s=test_evaluation.get(i);
			Stats train_s=train_evaluation.get(i);
			double test_error = ((double)test_s.getResult(Stats.INCORRECT)/(double)test_s.getTotal());
			double train_error = ((double)train_s.getResult(Stats.INCORRECT)/(double)train_s.getTotal());
			if (test_error < min) {
				min = test_error;
				id = i;
			} else if (test_error == min) {
				Stats old = train_evaluation.get(id);
				double train_old = ((double)old.getResult(Stats.INCORRECT)/(double)old.getTotal());
				if (train_error < train_old) {
					min = test_error;
					id = i;
				}
			}
			
		}
		return id;
		
	}
	
	public void printResults(String executionSignature) {
		StringBuffer sb = new StringBuffer();
		sb.append("#"+ Stats.getErrors());
		for (int i =0 ; i<FOREST_SIZE; i++) {
			sb.append(train_evaluation.get(i).print2string() + "\n");
		}
		sb.append( "\n\n");
		for (int i =0 ; i<FOREST_SIZE; i++) {
			sb.append(test_evaluation.get(i).print2string() + "\n");
		}
		sb.append( "\n");
		sb.append(train_evaluation.get(FOREST_SIZE).print2string() + "\n");
		sb.append( "\n");
		sb.append(test_evaluation.get(FOREST_SIZE).print2string() + "\n");
		
		sb.append( "\n");
		sb.append(train_evaluation.get(FOREST_SIZE+1).print2string() + "\n");
		sb.append( "\n");
		sb.append(test_evaluation.get(FOREST_SIZE+1).print2string() + "\n");
		
		StatsPrinter.print2file(sb, Util.DRL_DIRECTORY +executionSignature, false);
	}
	
	public void printLatex(String executionSignature) {
		StringBuffer sb = new StringBuffer();
		sb.append("#"+ Stats.getErrors());
		for (int i =0 ; i<FOREST_SIZE; i++) {
			sb.append(train_evaluation.get(i).print4Latex() +test_evaluation.get(i).print4Latex() + "\\\\"+"\n");
		}

		sb.append( "\n");
		sb.append(train_evaluation.get(FOREST_SIZE).print4Latex() +test_evaluation.get(FOREST_SIZE).print4Latex() +"\\\\"+"\n");
		
		sb.append( "\n");
		sb.append(train_evaluation.get(FOREST_SIZE+1).print4Latex() +test_evaluation.get(FOREST_SIZE+1).print4Latex()+"\\\\"+ "\n");
		sb.append( "\n");
		
		StatsPrinter.print2file(sb, Util.DRL_DIRECTORY +executionSignature, true);
	}
}


