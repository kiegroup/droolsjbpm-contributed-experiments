package org.drools.learner.builder;


import java.util.ArrayList;

import org.drools.learner.DecisionTree;
import org.drools.learner.DecisionTreePruner;
import org.drools.learner.InstanceList;
import org.drools.learner.Memory;
import org.drools.learner.Stats;
import org.drools.learner.StatsPrinter;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;
import org.drools.learner.tools.Util;

public class SingleTreeBuilder implements DecisionTreeBuilder{
	
	private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(SingleTreeBuilder.class, SimpleLogger.DEFAULT_LEVEL);
	private static SimpleLogger slog = LoggerFactory.getSysOutLogger(SingleTreeBuilder.class, SimpleLogger.DEFAULT_LEVEL);
	private boolean prune = false;
	private TreeAlgo algorithm = TreeAlgo.SINGLE; // default bagging, TODO boosting
	
	private DecisionTree one_tree;
	
	private Stats train_evaluation, test_evaluation;
	private Stats train_evaluation2, test_evaluation2;

	private double trainRatio = Util.TRAINING_RATIO;
	private double testRatio = Util.TESTING_RATIO;
	
	private SingleTreeTester tester;
	
	public SingleTreeBuilder() {//Learner _trainer) {
		//this.trainer = _trainer;
		//dom_type = trainer.getDomainType();
	}
	
	public void setTrainRatio(double ratio) {
		trainRatio = ratio;
	}
	
	public void setTestRatio(double ratio) {
		testRatio = ratio;
	}
	
	/* 
	 * the memory has the information 
	 * the instances: the objects which the decision tree will work on
	 * the schema: the definition of the object instance 
	 * 			(Class<?>) klass, String targetField, List<String> workingAttributes
	 */
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
		
		_trainer.setTrainingDataSize(train_instances.getSize());
		_trainer.setTrainingDataSizePerTree(train_instances.getSize());
		
		one_tree = _trainer.instantiate_tree();
		if (slog.debug() != null)
			slog.debug().log("\n"+"Training a tree"+"\n");
		_trainer.train_tree(one_tree, train_instances);
		one_tree.setTrainingDataSize(train_instances.getSize());
		one_tree.setTestingDataSize(test_instances.getSize());
		one_tree.setTrain(train_instances);
		one_tree.setTest(test_instances);

		tester = new SingleTreeTester(one_tree);
		
		train_evaluation = tester.test(train_instances);
		test_evaluation = tester.test(test_instances);
		one_tree.setValidationError(Util.division(test_evaluation.getResult(Stats.INCORRECT), test_instances.getSize()));
		one_tree.setTrainingError(Util.division(train_evaluation.getResult(Stats.INCORRECT), train_instances.getSize()));
		
		//if (prunner != null) {
		if (prune) {
			DecisionTreePruner pruner = new DecisionTreePruner();
			ArrayList<DecisionTree> dts = new ArrayList<DecisionTree>(1);
			dts.add(one_tree);
			pruner.prun_to_estimate(dts);
//			SingleTreeTester tester2 = new SingleTreeTester(one_tree);
			
			train_evaluation2 = tester.test(train_instances);
			test_evaluation2 = tester.test(test_instances);
			
		}
		

		// must be deleted, goes to builder	
		//_trainer.setBestTree(one_tree);
	}
	
	public Stats getTrainStats() {
		return train_evaluation;
	}
	
	public Stats getTestStats() {
		return test_evaluation;
	}
	
	public TreeAlgo getTreeAlgo() {
		return this.algorithm; // default
	}
	
	public DecisionTree getTree() {
		return one_tree;
	}

	public void printResults(String executionSignature) {
		tester.printStats(getTrainStats(), Util.DRL_DIRECTORY + executionSignature, false);
		tester.printStats(getTestStats(), Util.DRL_DIRECTORY + executionSignature, true);
	}
	
	public void printLatex(String executionSignature) {
		StringBuffer sb = new StringBuffer();
		sb.append("#"+ Stats.getErrors());
		sb.append( "\n");
		sb.append(getTrainStats().print4Latex() +getTestStats().print4Latex()+"\\\\"+ "\n");
		sb.append( "\n");
		
		StatsPrinter.print2file(sb, Util.DRL_DIRECTORY +executionSignature, true);
	}

	public void printLatex2(String executionSignature) {
		StringBuffer sb = new StringBuffer();
		sb.append("#"+ Stats.getErrors());
		sb.append( "\n");
		sb.append(train_evaluation2.print4Latex() +test_evaluation2.print4Latex()+"\\\\"+ "\n");
		sb.append( "\n");
		
		StatsPrinter.print2file(sb, Util.DRL_DIRECTORY +executionSignature, true);
	}
}
