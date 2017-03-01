package org.drools.learner.builder;

import java.util.ArrayList;

import org.drools.core.WorkingMemory;
import org.drools.learner.DecisionTree;
import org.drools.learner.DecisionTreePruner;
import org.drools.learner.Memory;
import org.drools.learner.Stats;
import org.drools.learner.StatsPrinter;
import org.drools.learner.builder.Learner.DataType;
import org.drools.learner.builder.Learner.DomainAlgo;
import org.drools.learner.builder.BoostedTester;
import org.drools.learner.builder.ForestTester;
import org.drools.learner.builder.SingleTreeTester;
import org.drools.learner.builder.Tester;
import org.drools.learner.builder.DecisionTreeBuilder.TreeAlgo;
import org.drools.learner.eval.CrossValidation;
import org.drools.learner.eval.ErrorEstimate;
import org.drools.learner.eval.TestSample;
import org.drools.learner.eval.heuristic.Entropy;
import org.drools.learner.eval.heuristic.GainRatio;
import org.drools.learner.eval.heuristic.Heuristic;
import org.drools.learner.eval.heuristic.MinEntropy;
import org.drools.learner.eval.heuristic.RandomInfo;
import org.drools.learner.eval.stopping.EstimatedNodeSize;
import org.drools.learner.eval.stopping.ImpurityDecrease;
import org.drools.learner.eval.stopping.MaximumDepth;
import org.drools.learner.eval.stopping.StoppingCriterion;
import org.drools.learner.tools.FeatureNotSupported;
import org.drools.learner.tools.Util;


// uses the static functions from the deprecated class DecisionTreeFactory
public class DecisionTreeFactory {
	
	public static DecisionTree createSingleID3E(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		return createSingleID3(wm, obj_class, new Entropy());
	}
	
	public static DecisionTree createSingleID3G(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		return createSingleID3(wm, obj_class, new GainRatio());
	}
	
	protected static DecisionTree createSingleID3(WorkingMemory wm, Class<? extends Object> obj_class, Heuristic h) throws FeatureNotSupported {	
		/*
		 * Quesitons:
		 * 1- which class to work with? : obj_class
		 * 2- what is its target attribute?
		 * 3- what are the objects
		 */ 
		DataType data = Learner.DEFAULT_DATA;
		ID3Learner learner = new ID3Learner(h);
		SingleTreeBuilder single_builder = new SingleTreeBuilder();
		
//		String algo_suffices = org.drools.learner.deprecated.DecisionTreeFactory.getAlgoSuffices(learner.getDomainAlgo(), single_builder.getTreeAlgo());
//		String executionSignature = org.drools.learner.deprecated.DecisionTreeFactory.getSignature(obj_class, "", algo_suffices);
		String algo_suffices = DecisionTreeFactory.getAlgoSuffices(learner.getDomainAlgo(), single_builder.getTreeAlgo());
		String executionSignature = DecisionTreeFactory.getSignature(obj_class, "", algo_suffices);
		
		/* create the memory */
		Memory mem = Memory.createFromWorkingMemory(wm, obj_class, learner.getDomainAlgo(), data);
		mem.setTrainRatio(Util.DEFAULT_TRAINING_RATIO);
		mem.setTestRatio(Util.DEFAULT_TESTING_RATIO);
		mem.processTestSet();
		
		//Ruler save_me_please = new Ruler()
		
		SolutionSet product = single_builder.build(mem, learner);//obj_class, target_attr, working_attr
		Solution s1 = product.getSolutions().get(0);
		Tester t = single_builder.getTester(s1.getTree());
		StatsPrinter.printLatex(t.test(s1.getList()), t.test(s1.getTestList()), executionSignature, false);
		//single_builder.printLatex(executionSignature);
		//
		
//		DecisionTreePruner pruner = new DecisionTreePruner();
//		for (Solution sol: product.getSolutions())
//			pruner.prun_to_estimate(sol);
//		Solution s2 = pruner.getBestSolution();
//		Tester t2 = single_builder.getTester(pruner.getBestSolution().getTree());		
//		StatsPrinter.printLatex(t2.test(s2.getList()), t2.test(s2.getTestList()), executionSignature, false);
		
		single_builder.getBestSolution().getTree().setSignature(executionSignature);
		return single_builder.getBestSolution().getTree();
	}
	
	/************************************************************************************************************************
	 * Single Tree Builder Algorithms with C4.5 
	 * @param wm
	 * @param obj_class
	 * @return
	 * @throws FeatureNotSupported
	 */	
	public static DecisionTree createSingleC45E(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
		return createSingleC45(wm, obj_class, new Entropy(), criteria, null);
	}
	
	public static DecisionTree createSingleC45G(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
		return createSingleC45(wm, obj_class, new GainRatio(), criteria, null);
	}
	
	public static DecisionTree createSingleC45E_worst(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
		return createSingleC45(wm, obj_class, new MinEntropy(), criteria, null);
	}
	
	public static DecisionTree createSingleC45Random(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
		return createSingleC45(wm, obj_class, new RandomInfo(), criteria, null);
	}
	
	public static DecisionTree createSingleC45E_Stop(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(3);
		criteria.add(new EstimatedNodeSize(0.5));
		criteria.add(new ImpurityDecrease());
		criteria.add(new MaximumDepth(50));
		return createSingleC45(wm, obj_class, new Entropy(), criteria, null);
	}
	
	public static DecisionTree createSingleC45G_Stop(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(3);
		criteria.add(new EstimatedNodeSize(0.5));
		criteria.add(new ImpurityDecrease());
		criteria.add(new MaximumDepth(50));
		return createSingleC45(wm, obj_class, new GainRatio(), criteria, null);
	}
	
	public static DecisionTree createSingleC45E_PrunStop(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		DecisionTreePruner pruner = new DecisionTreePruner();
		ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
		criteria.add(new EstimatedNodeSize(0.05));
		return createSingleC45(wm, obj_class, new Entropy(), criteria, pruner);
	}
	
	public static DecisionTree createSingleC45G_PrunStop(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		DecisionTreePruner pruner = new DecisionTreePruner();
		ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
		criteria.add(new EstimatedNodeSize(0.05));
		return createSingleC45(wm, obj_class, new GainRatio(), criteria ,pruner);
	}
	
	protected static DecisionTree createSingleC45(WorkingMemory wm, Class<? extends Object> obj_class, 
													Heuristic h, 
													ArrayList<StoppingCriterion> criteria,
													DecisionTreePruner pruner) throws FeatureNotSupported {
		DataType data = Learner.DEFAULT_DATA;
		C45Learner learner = new C45Learner(h);
		SingleTreeBuilder single_builder = new SingleTreeBuilder();
		
		String algo_suffices = DecisionTreeFactory.getAlgoSuffices(learner.getDomainAlgo(), single_builder.getTreeAlgo());
		String executionSignature = DecisionTreeFactory.getSignature(obj_class, "", algo_suffices);
		
		/* create the memory */
		Memory mem = Memory.createFromWorkingMemory(wm, obj_class, learner.getDomainAlgo(), data);
//		mem.setTrainRatio(Util.DEFAULT_TRAINING_RATIO);
//		mem.setTestRatio(Util.DEFAULT_TESTING_RATIO);
		mem.processTestSet();
		
		for (StoppingCriterion sc: criteria) {	
			if (sc instanceof MaximumDepth) {
				int max_depth = (int)((mem.getClassInstances().getSchema().getAttrNames().size() - 1)*0.70 );
				((MaximumDepth)sc).setDepth(max_depth);
			}
			learner.addStoppingCriteria(sc);
		}
		
		SolutionSet product = single_builder.build(mem, learner);//obj_class, target_attr, working_attr
		Solution s1 = product.getSolutions().get(0);
		StatsPrinter.printLatexComment("ORIGINAL TREE", executionSignature, false);
		StatsPrinter.printLatexComment(Stats.getErrors(), executionSignature, true);
		StatsPrinter.printLatex(s1.getTrainStats(), s1.getTestStats(), executionSignature, true);

		
		Tester.printStopping(learner.getStoppingCriteria(), Util.DRL_DIRECTORY +executionSignature);
		
		if (pruner != null) {
//			for (Solution sol: product.getSolutions())
//				pruner.prun_to_estimate(sol);
			pruner.prun_to_estimate(product);
			Solution s2 = pruner.getBestSolution();
			Tester t2 = single_builder.getTester(pruner.getBestSolution().getTree());	
			StatsPrinter.printLatexComment("Pruned TREE", executionSignature, true);
			StatsPrinter.printLatex(t2.test(s2.getList()), t2.test(s2.getTestList()), executionSignature, true);
		}
		single_builder.getBestSolution().getTree().setSignature(executionSignature);
		return single_builder.getBestSolution().getTree();
	}
	
	
	
	
	/************************************************************************************************************************
	 * Bagging Algorithms
	 * @param wm
	 * @param obj_class
	 * @return
	 * @throws FeatureNotSupported
	 */	
	public static DecisionTree createBagC45E(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
		return createBagC45(wm, obj_class, new Entropy(), criteria, null);
	}
	
	public static DecisionTree createBagC45G(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
		return createBagC45(wm, obj_class, new GainRatio(), criteria, null);
	}
	
	public static DecisionTree createBagC45E_Stop(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(3);
		criteria.add(new EstimatedNodeSize(0.5));
		criteria.add(new ImpurityDecrease());
		criteria.add(new MaximumDepth(50));
		return createBagC45(wm, obj_class, new Entropy(), criteria, null);
	}
	
	public static DecisionTree createBagC45G_Stop(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(3);
		criteria.add(new EstimatedNodeSize(0.5));
		criteria.add(new ImpurityDecrease());
		criteria.add(new MaximumDepth(50));
		return createBagC45(wm, obj_class, new GainRatio(), criteria, null);
	}
	
	public static DecisionTree createBagC45E_PrunStop(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		DecisionTreePruner pruner = new DecisionTreePruner();
		ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
		criteria.add(new EstimatedNodeSize(0.05));
		return createBagC45(wm, obj_class, new Entropy(), criteria ,pruner);
	}
	
	public static DecisionTree createBagC45G_PrunStop(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		DecisionTreePruner pruner = new DecisionTreePruner();
		ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
		criteria.add(new EstimatedNodeSize(0.05));
		return createBagC45(wm, obj_class, new GainRatio(), criteria ,pruner);
	}
	
	protected static DecisionTree createBagC45(WorkingMemory wm, Class<? extends Object> obj_class, 
													Heuristic h, 
													ArrayList<StoppingCriterion> criteria,
													DecisionTreePruner pruner) throws FeatureNotSupported {
		DataType data = Learner.DEFAULT_DATA;
		C45Learner learner = new C45Learner(h);	
		ForestBuilder forest = new ForestBuilder();
		
		String algo_suffices = DecisionTreeFactory.getAlgoSuffices(learner.getDomainAlgo(), forest.getTreeAlgo());
		String executionSignature = DecisionTreeFactory.getSignature(obj_class, "", algo_suffices);
		
		/* create the memory */
		Memory mem = Memory.createFromWorkingMemory(wm, obj_class, learner.getDomainAlgo(), data);
		mem.setTrainRatio(Util.DEFAULT_TRAINING_RATIO);
		mem.setTestRatio(Util.DEFAULT_TESTING_RATIO);
		mem.processTestSet();
		
		for (StoppingCriterion sc: criteria) {	
			if (sc instanceof MaximumDepth) {
				int max_depth = (int)((mem.getClassInstances().getSchema().getAttrNames().size() - 1)*0.70 );
				((MaximumDepth)sc).setDepth(max_depth);
			}
			learner.addStoppingCriteria(sc);
		}
		
		
		SolutionSet product = forest.build(mem, learner);//obj_class, target_attr, working_attr
		StatsPrinter.printLatexComment("Builder errors", executionSignature, false);
		StatsPrinter.printLatexComment(Stats.getErrors(), executionSignature, true);
		StatsPrinter.printLatex(product.getGlobalTrainStats(), product.getGlobalTestStats(), executionSignature, true);
		StatsPrinter.printLatexComment("Each Original Tree", executionSignature, true);
		for (Solution s: product.getSolutions()) {
			StatsPrinter.printLatex(s.getTrainStats(), s.getTestStats(), executionSignature, true);
		}
		Solution best_s = product.getBestSolution();
		StatsPrinter.printLatexComment("Best Original Tree", executionSignature, true);
		StatsPrinter.printLatex(best_s.getTrainStats(), best_s.getTestStats(), executionSignature, true);
		Tester t = forest.getTester(best_s.getTree());
		StatsPrinter.printLatexComment("Best Original Tree(Global)", executionSignature, true);
		StatsPrinter.printLatex(t.test(product.getTrainSet()), t.test(product.getTestSet()), executionSignature, true);

		Tester.printStopping(learner.getStoppingCriteria(), Util.DRL_DIRECTORY +executionSignature);
		
		
		if (pruner != null) {
//			for (Solution sol: product.getSolutions())
//				pruner.prun_to_estimate(sol);
//			
//			Solution s2 = pruner.getBestSolution();
//			product.setBestSolutionId(s2.getTree().getId());
//			for (Solution sol: product.getSolutions())
//			pruner.prun_to_estimate(sol);
			pruner.prun_to_estimate(product);
			Solution s2 = pruner.getBestSolution();

			Tester t2 = forest.getTester(s2.getTree());
			StatsPrinter.printLatexComment("Best Pruned Tree", executionSignature, true);
			StatsPrinter.printLatex(t2.test(s2.getList()), t2.test(s2.getTestList()), executionSignature, true);
			Tester t2_global = forest.getTester(s2.getTree());
			StatsPrinter.printLatexComment("Best Original Tree(Global)", executionSignature, true);
			StatsPrinter.printLatex(t2_global.test(product.getTrainSet()), t2_global.test(product.getTestSet()), executionSignature, true);
		}

		forest.getBestSolution().getTree().setSignature(executionSignature);
		return forest.getBestSolution().getTree();
	}
	
	
	
	
	/************************************************************************************************************************
	 * Boosting Algorithms
	 * @param wm
	 * @param obj_class
	 * @return
	 * @throws FeatureNotSupported
	 */	
	public static DecisionTree createBoostC45E(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
		return createBoostC45(wm, obj_class, new Entropy(), criteria, null);
	}
	
	public static DecisionTree createBoostC45G(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
		return createBoostC45(wm, obj_class, new GainRatio(), criteria, null);
	}
	
	public static DecisionTree createBoostC45E_Stop(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(3);
		criteria.add(new EstimatedNodeSize(0.5));
		criteria.add(new ImpurityDecrease());
		criteria.add(new MaximumDepth(50));
		return createBoostC45(wm, obj_class, new Entropy(), criteria, null);
	}
	
	public static DecisionTree createBoostC45G_Stop(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(3);
		criteria.add(new EstimatedNodeSize(0.5));
		criteria.add(new ImpurityDecrease());
		criteria.add(new MaximumDepth(50));
		return createBoostC45(wm, obj_class, new GainRatio(), criteria, null);
	}
	
	public static DecisionTree createBoostC45E_PrunStop(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		DecisionTreePruner pruner = new DecisionTreePruner();
		ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
		criteria.add(new EstimatedNodeSize(0.05));
		return createBoostC45(wm, obj_class, new Entropy(), criteria ,pruner);
	}
	
	public static DecisionTree createBoostC45G_PrunStop(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		DecisionTreePruner pruner = new DecisionTreePruner();
		ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
		criteria.add(new EstimatedNodeSize(0.05));
		return createBoostC45(wm, obj_class, new GainRatio(), criteria ,pruner);
	}
	
	protected static DecisionTree createBoostC45(WorkingMemory wm, Class<? extends Object> obj_class, 
													Heuristic h, 
													ArrayList<StoppingCriterion> criteria,
													DecisionTreePruner pruner) throws FeatureNotSupported {
		DataType data = Learner.DEFAULT_DATA;
		C45Learner learner = new C45Learner(h);	
		AdaBoostBuilder boosted_forest = new AdaBoostBuilder();
		
		String algo_suffices = DecisionTreeFactory.getAlgoSuffices(learner.getDomainAlgo(), boosted_forest.getTreeAlgo());
		String executionSignature = DecisionTreeFactory.getSignature(obj_class, "", algo_suffices);
		
		/* create the memory */
		Memory mem = Memory.createFromWorkingMemory(wm, obj_class, learner.getDomainAlgo(), data);
		mem.setTrainRatio(Util.DEFAULT_TRAINING_RATIO);
		mem.setTestRatio(Util.DEFAULT_TESTING_RATIO);
		mem.processTestSet();
		
		for (StoppingCriterion sc: criteria) {	
			if (sc instanceof MaximumDepth) {
				int max_depth = (int)((mem.getClassInstances().getSchema().getAttrNames().size() - 1)*0.70 );
				((MaximumDepth)sc).setDepth(max_depth);
			}
			learner.addStoppingCriteria(sc);
		}
		
		
		SolutionSet product = boosted_forest.build(mem, learner);//obj_class, target_attr, working_attr
		StatsPrinter.printLatexComment("Builder errors", executionSignature, false);
		StatsPrinter.printLatexComment(Stats.getErrors(), executionSignature, true);
		StatsPrinter.printLatex(product.getGlobalTrainStats(), product.getGlobalTestStats(), executionSignature, true);
		StatsPrinter.printLatexComment("Each Original Tree", executionSignature, true);
		for (Solution s: product.getSolutions()) {
			StatsPrinter.printLatex(s.getTrainStats(), s.getTestStats(), executionSignature, true);
		}
		Solution best_s = product.getBestSolution();
		StatsPrinter.printLatexComment("Best Original Tree", executionSignature, true);
		StatsPrinter.printLatex(best_s.getTrainStats(), best_s.getTestStats(), executionSignature, true);

		Tester.printStopping(learner.getStoppingCriteria(), Util.DRL_DIRECTORY +executionSignature);
		
		
		if (pruner != null) {
//			for (Solution sol: product.getSolutions())
//				pruner.prun_to_estimate(sol);
			pruner.prun_to_estimate(product);
			Solution s2 = pruner.getBestSolution();
//			System.out.println(s2.getTree().getId());
//			product.setBestSolutionId(s2.getTree().getId());
			Tester t2 = boosted_forest.getTester(pruner.getBestSolution().getTree());
			StatsPrinter.printLatexComment("Best Pruned Tree", executionSignature, true);
			StatsPrinter.printLatex(t2.test(s2.getList()), t2.test(s2.getTestList()), executionSignature, true);
			
			Tester t2_global = boosted_forest.getTester(s2.getTree());
			StatsPrinter.printLatexComment("Best Original Tree(Global)", executionSignature, true);
			StatsPrinter.printLatex(t2_global.test(product.getTrainSet()), t2_global.test(product.getTestSet()), executionSignature, true);
		
			
		}

		boosted_forest.getBestSolution().getTree().setSignature(executionSignature);
		return boosted_forest.getBestSolution().getTree();
	}
	


	
	public static String getSignature(Class<? extends Object> obj_class, String fileName, String suffices) {
		
		//String fileName = (dataFile == null || dataFile == "") ? this.getRuleClass().getSimpleName().toLowerCase(): dataFile; 			
		String objectSignature = fileName;
		if (objectSignature == null || objectSignature =="") {
			objectSignature = obj_class.getSimpleName().toLowerCase();
			if (suffices != null && suffices != "")
				objectSignature += "_"+ suffices;
		}
		
		String packageFolders = obj_class.getPackage().getName();
		String _packageNames = packageFolders.replace('.', '/');
		String executionSignature = _packageNames+"/"+ objectSignature; //"src/main/rules/"+
		
		return executionSignature;
		
	}
	
	public static String getAlgoSuffices(DomainAlgo domain_a, TreeAlgo tree_a) {
		String suffix = "";
		switch (domain_a) {
		case CATEGORICAL:
			suffix += "id3" ;
			break;
		case QUANTITATIVE:
			suffix += "c45";
			break;
		default:
			suffix += "?" ;
		
		}
		
		switch (tree_a) {
		case SINGLE:
			suffix += "_one";
			break;
		case BAG:
			suffix += "_bag";
			break;
		case BOOST:
			suffix += "_boost";
			break;
		default:
			suffix += "_?" ;
		
		}
		return suffix;
	}
}
