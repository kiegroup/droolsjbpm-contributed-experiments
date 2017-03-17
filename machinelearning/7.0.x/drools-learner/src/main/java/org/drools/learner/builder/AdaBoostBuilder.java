package org.drools.learner.builder;

import java.util.ArrayList;

import org.drools.learner.DecisionTree;
import org.drools.learner.InstanceList;
import org.drools.learner.Memory;
import org.drools.learner.Stats;
import org.drools.learner.StatsPrinter;
import org.drools.learner.builder.BoostedTester;
import org.drools.learner.builder.SingleTreeTester;
import org.drools.learner.builder.Tester;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;
import org.drools.learner.tools.Util;

/*
 * 
 */
public class AdaBoostBuilder extends DecisionTreeBuilder{

	private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(AdaBoostBuilder.class, SimpleLogger.DEFAULT_LEVEL);
	private static SimpleLogger slog = LoggerFactory.getSysOutLogger(AdaBoostBuilder.class, SimpleLogger.DEFAULT_LEVEL);
	
	private TreeAlgo algorithm = TreeAlgo.BOOST; // default bagging, TODO boosting
	
	
	private static int FOREST_SIZE = Util.NUM_TREES;
	private static final double TREE_SIZE_RATIO = 1.0d;
	private static final boolean WITH_REP = false;
	
	private ArrayList<DecisionTree> forest;
	private ArrayList<Double> classifier_accuracy;
	
	private DecisionTreeMerger merger;

	
//	private BoostedTester tester;
//	
//	private ArrayList<Stats> train_evaluation, test_evaluation;
	
	public AdaBoostBuilder() {
		//this.trainer = _trainer;
		merger = new DecisionTreeMerger();
//		train_evaluation = new ArrayList<Stats>(FOREST_SIZE);
//		test_evaluation = new ArrayList<Stats>(FOREST_SIZE);
	}
	
	
	public void internalBuild(SolutionSet sol_set, Learner _trainer) {
		_trainer.setInputSpec(sol_set.getInputSpec());
		if (sol_set.getTargets().size()>1) {
			//throw new FeatureNotSupported("There is more than 1 target candidates");
			if (flog.error() !=null)
				flog.error().log("There is more than 1 target candidates");
			
			System.exit(0);
			// TODO put the feature not supported exception || implement it
		} else if (_trainer.getTargetDomain().getCategoryCount() >2) {
			if (slog.error() !=null)
				slog.error().log("The target domain is not binary!!!\n");
			System.exit(0);
		}
		
		
		int N = sol_set.getTrainSet().getSize();
		int NUM_DATA = (int)(TREE_SIZE_RATIO * N);	// TREE_SIZE_RATIO = 1.0, all training data is used to train the trees again again
		_trainer.setTrainingDataSizePerTree(NUM_DATA);
		/* tree_capacity number of data fed to each tree, there are FOREST_SIZE trees*/
		_trainer.setTrainingDataSize(NUM_DATA); 

		
		forest = new ArrayList<DecisionTree> (FOREST_SIZE);
		classifier_accuracy = new ArrayList<Double>(FOREST_SIZE);
		// weight for each instance - the higher the weight, the more the instance influences the classifier learned.
		double [] weights = new double [NUM_DATA];
		for (int index_i=0; index_i<NUM_DATA; index_i++) {		
			weights[index_i] = 1.0d/(double)NUM_DATA;
//			class_instances.getInstance(index_i).setWeight(weights[index_i] * (double)NUM_DATA);
			sol_set.getTrainSet().getInstance(index_i).setWeight(weights[index_i] * (double)NUM_DATA);
			if (slog.debug() != null)
				slog.debug().log(index_i+" new weight:"+sol_set.getTrainSet().getInstance(index_i).getWeight()+ "\n");
		}
		
		int i = 0;
//			int[] bag;		
		while (i++ < FOREST_SIZE ) {
//			if (WITH_REP)
//				bag = Util.bag_w_rep(NUM_DATA, N);
//			else
//				bag = Util.bag_wo_rep(NUM_DATA, N);	
			
			InstanceList working_instances = sol_set.getTrainSet();//class_instances; //.getInstances(bag);			
			DecisionTree dt = _trainer.train_tree(working_instances);
			dt.setID(i);
			
			double error = 0.0, sum_weight = 0.0;
			SingleTreeTester single_tester= new SingleTreeTester(dt);
			for (int index_i = 0; index_i < NUM_DATA; index_i++) {
//				Integer result = t.test(class_instances.getInstance(index_i));
				Integer result = single_tester.test(working_instances.getInstance(index_i));
				sum_weight += weights[index_i];
				if (result == Stats.INCORRECT) {
					error += weights[index_i];
					if (slog.debug() != null)
						slog.debug().log("[e:"+error+" w:"+weights[index_i]+ "] ");
				}
			}
			
			error = error / sum_weight; // forgotton
			if (error > 0.0f) {
				double alpha = Util.ln( (1.0d-error)/error ) / 2.0d;
				
				if (error < 0.5d) {
					// The classification accuracy of the weak classifier
					classifier_accuracy.add(alpha);
					
					double norm_fact= 0.0d;
					// Boosting the missclassified instances
					for (int index_i = 0; index_i < NUM_DATA; index_i++) {
//						Integer result = t.test(class_instances.getInstance(index_i));//TODO dont need to test two times
//						Integer result = t.test(class_instances.getInstance(index_i));
						Integer result = single_tester.test(working_instances.getInstance(index_i));
						switch (result) {
						case Stats.INCORRECT:
							weights[index_i] = weights[index_i] * Util.exp(alpha);
							break;
						case Stats.CORRECT:	// if it is correct do not update
							//weights[index_i] = weights[index_i] * Util.exp(-1.0d * alpha);
							break;
						case Stats.UNKNOWN:
							if (slog.error() !=null)
								slog.error().log("Unknown situation bok\n");
							System.exit(0);
							break;
						}
						norm_fact += weights[index_i];
					}
					// Normalization of the weights
					for (int index_i = 0; index_i < NUM_DATA; index_i++) {
						weights[index_i] = weights[index_i] / norm_fact;
						//class_instances.getInstance(index_i).setWeight(weights[index_i] * (double)NUM_DATA);
						working_instances.getInstance(index_i).setWeight(weights[index_i] * (double)NUM_DATA);
					}
				} else {
					if (slog.debug() != null)
						slog.debug().log("The error="+error+" alpha:"+alpha+ "\n");
					if (slog.error() != null)
						slog.error().log("error:"+error + " alpha will be negative and the weights of the training samples will be updated in the wrong direction"+"\n");
					FOREST_SIZE = i-1;//ignore the current tree
					break;
				}
				
			} else {
				if (slog.stat() != null) {
					slog.stat().log("\n Boosting ends: ");
					slog.stat().log("All instances classified correctly TERMINATE, forest size:"+i+ "\n");
				}
				// What to do here??
				FOREST_SIZE = i;
				classifier_accuracy.add(10.0); // TODO add a very big number
				
				
			}
	
			
			forest.add(dt);
			// the DecisionTreeMerger will visit the decision tree and add the paths that have not been seen yet to the list
			merger.add(dt);	

			// adding to the set of solutions
			Tester t = getTester(dt);
			Stats train = t.test(sol_set.getTrainSet());
			Stats test = t.test(sol_set.getTestSet());
			Solution one = new Solution(dt, sol_set.getTrainSet());
			one.setTestList(sol_set.getTestSet());
			one.setTrainStats(train);
			one.setTestStats(test);
			sol_set.addSolution(one);

			if (slog.stat() !=null)
				slog.stat().stat(".");

		}
		
		
		
		Tester global_tester = getTester(forest, getAccuracies());
		Stats train = global_tester.test(sol_set.getTrainSet());
		Stats test = global_tester.test(sol_set.getTestSet());
		sol_set.setGlobalTrainStats(train);
		sol_set.setGlobalTestStats(test);
		
		//System.exit(0);
		// TODO how to compute a best tree from the forest
		int best_id = sol_set.getMinTestId();
//		best = merger.getBest();
//		if (best == null)
//			best = forest.get(best_id);
		
//		best_solution
		sol_set.setBestSolutionId(best_id);
		
		return;
		
	}

//	public void build(Memory mem, Learner _trainer) {
//		
//		final InstanceList class_instances = mem.getClassInstances();
//		_trainer.setInputData(class_instances);
//		
//		
//		if (class_instances.getTargets().size()>1 ) {
//			//throw new FeatureNotSupported("There is more than 1 target candidates");
//			if (slog.error() !=null)
//				slog.error().log("There is more than 1 target candidates\n");
//			System.exit(0);
//			// TODO put the feature not supported exception || implement it
//		} else if (_trainer.getTargetDomain().getCategoryCount() >2) {
//			if (slog.error() !=null)
//				slog.error().log("The target domain is not binary!!!\n");
//			System.exit(0);
//		}
//		
//		int split_idx =  (int)(trainRatio * class_instances.getSize());
//		int split_idx2 =  split_idx + (int)(testRatio * class_instances.getSize());
//		
//		InstanceList train_instances = class_instances.subList(0, split_idx);
//		InstanceList test_instances = class_instances.subList(split_idx, split_idx2);//class_instances.getSize());
//		
//	
//		int N = train_instances.getSize();
//		int NUM_DATA = (int)(TREE_SIZE_RATIO * N);	// TREE_SIZE_RATIO = 1.0, all training data is used to train the trees again again
//		_trainer.setTrainingDataSizePerTree(NUM_DATA);
//		/* tree_capacity number of data fed to each tree, there are FOREST_SIZE trees*/
//		_trainer.setTrainingDataSize(NUM_DATA); 
//		
////		int N = class_instances.getSize();
////		int NUM_DATA = (int)(TREE_SIZE_RATIO * N);	// TREE_SIZE_RATIO = 1.0, all data is used to train the trees again again
////		_trainer.setTrainingDataSizePerTree(NUM_DATA);
////		
////		/* all data fed to each tree, the same data?? */
////		_trainer.setTrainingDataSize(NUM_DATA); // TODO????
//
//		
//		forest = new ArrayList<DecisionTree> (FOREST_SIZE);
//		classifier_accuracy = new ArrayList<Double>(FOREST_SIZE);
//		// weight for each instance - the higher the weight, the more the instance influences the classifier learned.
//		double [] weights = new double [NUM_DATA];
//		for (int index_i=0; index_i<NUM_DATA; index_i++) {		
//			weights[index_i] = 1.0d/(double)NUM_DATA;
////			class_instances.getInstance(index_i).setWeight(weights[index_i] * (double)NUM_DATA);
//			train_instances.getInstance(index_i).setWeight(weights[index_i] * (double)NUM_DATA);
//			if (slog.debug() != null)
//				slog.debug().log(index_i+" new weight:"+train_instances.getInstance(index_i).getWeight()+ "\n");
//		}
//		
//		int i = 0;
////			int[] bag;		
//		while (i++ < FOREST_SIZE ) {
////			if (WITH_REP)
////				bag = Util.bag_w_rep(NUM_DATA, N);
////			else
////				bag = Util.bag_wo_rep(NUM_DATA, N);	
//			
//			InstanceList working_instances = train_instances;//class_instances; //.getInstances(bag);			
//			DecisionTree dt = _trainer.train_tree(working_instances);
//			dt.setID(i);
//			
//			double error = 0.0, sum_weight = 0.0;
//			SingleTreeTester single_tester= new SingleTreeTester(dt);
//			for (int index_i = 0; index_i < NUM_DATA; index_i++) {
////				Integer result = t.test(class_instances.getInstance(index_i));
//				Integer result = single_tester.test(working_instances.getInstance(index_i));
//				sum_weight += weights[index_i];
//				if (result == Stats.INCORRECT) {
//					error += weights[index_i];
//					if (slog.debug() != null)
//						slog.debug().log("[e:"+error+" w:"+weights[index_i]+ "] ");
//				}
//			}
//			
//			error = error / sum_weight; // forgotton
//			if (error > 0.0f) {
//				double alpha = Util.ln( (1.0d-error)/error ) / 2.0d;
//				
//				if (error < 0.5d) {
//					// The classification accuracy of the weak classifier
//					classifier_accuracy.add(alpha);
//					
//					double norm_fact= 0.0d;
//					// Boosting the missclassified instances
//					for (int index_i = 0; index_i < NUM_DATA; index_i++) {
////						Integer result = t.test(class_instances.getInstance(index_i));//TODO dont need to test two times
////						Integer result = t.test(class_instances.getInstance(index_i));
//						Integer result = single_tester.test(working_instances.getInstance(index_i));
//						switch (result) {
//						case Stats.INCORRECT:
//							weights[index_i] = weights[index_i] * Util.exp(alpha);
//							break;
//						case Stats.CORRECT:	// if it is correct do not update
//							//weights[index_i] = weights[index_i] * Util.exp(-1.0d * alpha);
//							break;
//						case Stats.UNKNOWN:
//							if (slog.error() !=null)
//								slog.error().log("Unknown situation bok\n");
//							System.exit(0);
//							break;
//						}
//						norm_fact += weights[index_i];
//					}
//					// Normalization of the weights
//					for (int index_i = 0; index_i < NUM_DATA; index_i++) {
//						weights[index_i] = weights[index_i] / norm_fact;
//						//class_instances.getInstance(index_i).setWeight(weights[index_i] * (double)NUM_DATA);
//						working_instances.getInstance(index_i).setWeight(weights[index_i] * (double)NUM_DATA);
//					}
//				} else {
//					if (slog.debug() != null)
//						slog.debug().log("The error="+error+" alpha:"+alpha+ "\n");
//					if (slog.error() != null)
//						slog.error().log("error:"+error + " alpha will be negative and the weights of the training samples will be updated in the wrong direction"+"\n");
//					FOREST_SIZE = i-1;//ignore the current tree
//					break;
//				}
//				
//			} else {
//				if (slog.stat() != null) {
//					slog.stat().log("\n Boosting ends: ");
//					slog.stat().log("All instances classified correctly TERMINATE, forest size:"+i+ "\n");
//				}
//				// What to do here??
//				FOREST_SIZE = i;
//				classifier_accuracy.add(10.0); // TODO add a very big number
//				
//				
//			}
//			train_evaluation.add(single_tester.test(train_instances));
//			test_evaluation.add(single_tester.test(test_instances));
//			
//			forest.add(dt);
//			// the DecisionTreeMerger will visit the decision tree and add the paths that have not been seen yet to the list
//			merger.add(dt);	
//
//			if (slog.stat() !=null)
//				slog.stat().stat(".");
//
//		}
//		
//		tester = new BoostedTester(forest, getAccuracies());
//		train_evaluation.add(tester.test(train_instances));
//		test_evaluation.add(tester.test(test_instances));
//		// TODO how to compute a best tree from the forest
//		int best_id = getMinTestId();
//		best = merger.getBest();
//		if (best == null)
//			best = forest.get(best_id);
//		
//		train_evaluation.add(train_evaluation.get(best_id));
//		test_evaluation.add(test_evaluation.get(best_id));
//		
//		//_trainer.setBestTree(forest.get(0));
//		//this.c45 = dt;
//	}

	public ArrayList<DecisionTree> getTrees() {
		return forest;
	}
	public ArrayList<Double> getAccuracies() {
		return classifier_accuracy;
	}
	public TreeAlgo getTreeAlgo() {
		return algorithm; //TreeAlgo.BAG; // default
	}
	public void clearForest(int j) {
		//forest = null;
		for (int i = forest.size()-1; i >j; i--)
			forest.remove(i);
		
	}
	
	public Tester getTester(ArrayList<DecisionTree> boosted_forest, ArrayList<Double> acc) {
		return new BoostedTester(boosted_forest, acc);
	}
	public Solution getBestSolution() {
		return solutions.getBestSolution();
	}
	
//	public void printResults(String executionSignature) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("#"+ Stats.getErrors());
//		for (int i =0 ; i<FOREST_SIZE; i++) {
//			sb.append(train_evaluation.get(i).print2string() + "\n");
//		}
//		sb.append( "\n\n");
//		for (int i =0 ; i<FOREST_SIZE; i++) {
//			sb.append(test_evaluation.get(i).print2string() + "\n");
//		}
//		sb.append( "\n");
//		sb.append(train_evaluation.get(FOREST_SIZE).print2string() + "\n");
//		sb.append( "\n");
//		sb.append(test_evaluation.get(FOREST_SIZE).print2string() + "\n");
//		
//		sb.append( "\n");
//		sb.append(train_evaluation.get(FOREST_SIZE+1).print2string() + "\n");
//		sb.append( "\n");
//		sb.append(test_evaluation.get(FOREST_SIZE+1).print2string() + "\n");
//		
//		StatsPrinter.print2file(sb, Util.DRL_DIRECTORY +executionSignature, false);
//	}
//	
//	public void printLatex(String executionSignature) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("#"+ Stats.getErrors());
//		for (int i =0 ; i<FOREST_SIZE; i++) {
//			sb.append(train_evaluation.get(i).print4Latex() +test_evaluation.get(i).print4Latex() + "\\\\"+"\n");
//		}
//
//		sb.append( "\n");
//		sb.append(train_evaluation.get(FOREST_SIZE).print4Latex() +test_evaluation.get(FOREST_SIZE).print4Latex() +"\\\\"+"\n");
//		
//		sb.append( "\n");
//		sb.append(train_evaluation.get(FOREST_SIZE+1).print4Latex() +test_evaluation.get(FOREST_SIZE+1).print4Latex()+"\\\\"+ "\n");
//		sb.append( "\n");
//		
//		StatsPrinter.print2file(sb, Util.DRL_DIRECTORY +executionSignature, true);
//	}
}
