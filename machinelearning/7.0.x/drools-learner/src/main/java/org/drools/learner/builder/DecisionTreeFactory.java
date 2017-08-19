package org.drools.learner.builder;

import java.util.ArrayList;
import java.util.List;

import org.drools.learner.DecisionTree;
import org.drools.learner.DecisionTreePruner;
import org.drools.learner.Memory;
import org.drools.learner.MemoryImpl;
import org.drools.learner.Stats;
import org.drools.learner.StatsPrinter;
import org.drools.learner.builder.DecisionTreeBuilder.TreeAlgo;
import org.drools.learner.builder.Learner.DataType;
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

//    public static DecisionTree createSingleID3E(List<Object> objects,
//                                                Class<? extends Object> objClass) throws FeatureNotSupported {
//        return createSingleID3(objects, objClass, HeuristicFactory.Heuristics.ENTROPY.newHeuristic());
//    }
//
//    public static DecisionTree createSingleID3G(List<Object> objects,
//                                                Class<? extends Object> objClass) throws FeatureNotSupported {
//        return createSingleID3(objects, objClass, HeuristicFactory.Heuristics.GAIN_RATIO.newHeuristic());
//    }
//
//    protected static DecisionTree createSingleID3(List<Object> objects, Class<? extends Object> objClass,
//                                                  Heuristic h) throws FeatureNotSupported {
//        /*
//         * Quesitons: 1- which class to work with? : objClass 2- what is its
//         * target attribute? 3- what are the objects
//         */
//        DataType          data          = Learner.DEFAULT_DATA;
//        ID3Learner        learner       = new ID3Learner(h);
//        SingleTreeBuilder singleBuilder = new SingleTreeBuilder();
//
//        //		String algo_suffices = org.drools.learner.deprecated.DecisionTreeFactory.getAlgoSuffices(learner.getDomainAlgo(), single_builder.getTreeAlgo());
//        //		String executionSignature = org.drools.learner.deprecated.DecisionTreeFactory.getSignature(objClass, "", algo_suffices);
//        String algoSuffices       = DecisionTreeFactory.getAlgoSuffices(learner.getDomainAlgo(), singleBuilder.getTreeAlgo());
//        String executionSignature = DecisionTreeFactory.getSignature(objClass, "", algoSuffices);
//
//        /* create the memory */
//        Memory mem = Memory.createFromObjects(objects, objClass, learner.getDomainAlgo(), data);
//        mem.setTrainRatio(Util.DEFAULT_TRAINING_RATIO);
//        mem.setTestRatio(Util.DEFAULT_TESTING_RATIO);
//        mem.processTestSet();
//
//        //Ruler save_me_please = new Ruler()
//
//        SolutionSet product = singleBuilder.build(mem, learner);//objClass, target_attr, working_attr
//        Solution    s1      = product.getSolutions().get(0);
//        Tester      t       = singleBuilder.getTester(s1.getTree());
//        StatsPrinter.printLatex(t.test(s1.getList()), t.test(s1.getTestList()), executionSignature, false);
//        //single_builder.printLatex(executionSignature);
//        //
//
//        //		DecisionTreePruner pruner = new DecisionTreePruner();
//        //		for (Solution sol: product.getSolutions())
//        //			pruner.prun_to_estimate(sol);
//        //		Solution s2 = pruner.getBestSolution();
//        //		Tester t2 = single_builder.getTester(pruner.getBestSolution().getTree());
//        //		StatsPrinter.printLatex(t2.test(s2.getList()), t2.test(s2.getTestList()), executionSignature, false);
//
//        singleBuilder.getBestSolution().getTree().setSignature(executionSignature);
//        return singleBuilder.getBestSolution().getTree();
//    }

    /************************************************************************************************************************
     * Single Tree Builder Algorithms with C4.5
     *
     * @param objects
     * @param objClass
     * @return
     * @throws FeatureNotSupported
     */
    public static DecisionTree createSingleC45E(List<Object> objects,
                                                Class<? extends Object> objClass) throws FeatureNotSupported {
        ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
        return createSingleC45(objects, objClass, new Entropy(), criteria, null);
    }

    public static DecisionTree createSingleC45G(List<Object> objects,
                                                Class<? extends Object> objClass) throws FeatureNotSupported {
        ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
        return createSingleC45(objects, objClass, new GainRatio(), criteria, null);
    }

    public static DecisionTree createSingleC45EWorst(List<Object> objects,
                                                     Class<? extends Object> objClass) throws FeatureNotSupported {
        ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
        return createSingleC45(objects, objClass, new MinEntropy(), criteria, null);
    }

    public static DecisionTree createSingleC45Random(List<Object> objects,
                                                     Class<? extends Object> objClass) throws FeatureNotSupported {
        ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
        return createSingleC45(objects, objClass, new RandomInfo(), criteria, null);
    }

    public static DecisionTree createSingleC45EStop(List<Object> objects,
                                                    Class<? extends Object> objClass) throws FeatureNotSupported {
        ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(3);
        criteria.add(new EstimatedNodeSize(0.5));
        criteria.add(new ImpurityDecrease());
        criteria.add(new MaximumDepth(50));
        return createSingleC45(objects, objClass, new Entropy(), criteria, null);
    }

    public static DecisionTree createSingleC45GStop(List<Object> objects,
                                                    Class<? extends Object> objClass) throws FeatureNotSupported {
        ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(3);
        criteria.add(new EstimatedNodeSize(0.5));
        criteria.add(new ImpurityDecrease());
        criteria.add(new MaximumDepth(50));
        return createSingleC45(objects, objClass, new GainRatio(), criteria, null);
    }

    public static DecisionTree createSingleC45EPrunStop(List<Object> objects,
                                                        Class<? extends Object> objClass) throws FeatureNotSupported {
        DecisionTreePruner           pruner   = new DecisionTreePruner();
        ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
        criteria.add(new EstimatedNodeSize(0.05));
        return createSingleC45(objects, objClass, new Entropy(), criteria, pruner);
    }

    public static DecisionTree createSingleC45GPrunStop(List<Object> objects,
                                                        Class<? extends Object> objClass) throws FeatureNotSupported {
        DecisionTreePruner           pruner   = new DecisionTreePruner();
        ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
        criteria.add(new EstimatedNodeSize(0.05));
        return createSingleC45(objects, objClass, new GainRatio(), criteria, pruner);
    }

    protected static DecisionTree createSingleC45(List<Object> objects, Class<? extends Object> objClass, Heuristic h,
                                                  ArrayList<StoppingCriterion> criteria, DecisionTreePruner pruner)
        throws FeatureNotSupported {
        DataType          data          = Learner.DEFAULT_DATA;
        C45Learner        learner       = new C45Learner(h);
        SingleTreeBuilder singleBuilder = new SingleTreeBuilder();

        String algoSuffices       = DecisionTreeFactory.getAlgoSuffices(singleBuilder.getTreeAlgo());
        String executionSignature = DecisionTreeFactory.getSignature(objClass, "", algoSuffices);

        /* create the memory */
        Memory mem = Memory.createFromObjects(objects, objClass, data);

        for (StoppingCriterion sc : criteria) {
            if (sc instanceof MaximumDepth) {
                int maxDepth = (int) ((mem.getInstances().getSchema().getAttrNames().size() - 1) * 0.70);
                ((MaximumDepth) sc).setDepth(maxDepth);
            }
            learner.addStoppingCriteria(sc);
        }

        SolutionSet product = singleBuilder.build(mem, learner);//objClass, target_attr, working_attr
        Solution    s1      = product.getSolutions().get(0);
        StatsPrinter.printLatexComment("ORIGINAL TREE", executionSignature, false);
        StatsPrinter.printLatexComment(Stats.getErrors(), executionSignature, true);
        StatsPrinter.printLatex(s1.getTrainStats(), s1.getTestStats(), executionSignature, true);

        Tester.printStopping(learner.getStoppingCriteria(), Util.DRL_DIRECTORY + executionSignature);

        if (pruner != null) {
            //			for (Solution sol: product.getSolutions())
            //				pruner.prun_to_estimate(sol);
            pruner.prunToEstimate(product);
            Solution s2 = pruner.getBestSolution();
            Tester   t2 = DecisionTreeBuilder.getTester(pruner.getBestSolution().getTree());
            StatsPrinter.printLatexComment("Pruned TREE", executionSignature, true);
            StatsPrinter.printLatex(t2.test(s2.getList()), t2.test(s2.getTestList()), executionSignature, true);
        }
        product.getBestSolution().getTree().setSignature(executionSignature);
        return product.getBestSolution().getTree();
    }

    /************************************************************************************************************************
     * Bagging Algorithms
     *
     * @param objects
     * @param objClass
     * @return
     * @throws FeatureNotSupported
     */
    public static DecisionTree createBagC45E(List<Object> objects,
                                             Class<? extends Object> objClass) throws FeatureNotSupported {
        ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
        return createBagC45(objects, objClass, new Entropy(), criteria, null);
    }

    public static DecisionTree createBagC45G(List<Object> objects,
                                             Class<? extends Object> objClass) throws FeatureNotSupported {
        ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
        return createBagC45(objects, objClass, new GainRatio(), criteria, null);
    }

    public static DecisionTree createBagC45EStop(List<Object> objects,
                                                 Class<? extends Object> objClass) throws FeatureNotSupported {
        ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(3);
        criteria.add(new EstimatedNodeSize(0.5));
        criteria.add(new ImpurityDecrease());
        criteria.add(new MaximumDepth(50));
        return createBagC45(objects, objClass, new Entropy(), criteria, null);
    }

    public static DecisionTree createBagC45GStop(List<Object> objects,
                                                 Class<? extends Object> objClass) throws FeatureNotSupported {
        ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(3);
        criteria.add(new EstimatedNodeSize(0.5));
        criteria.add(new ImpurityDecrease());
        criteria.add(new MaximumDepth(50));
        return createBagC45(objects, objClass, new GainRatio(), criteria, null);
    }

    public static DecisionTree createBagC45EPrunStop(List<Object> objects,
                                                     Class<? extends Object> objClass) throws FeatureNotSupported {
        DecisionTreePruner           pruner   = new DecisionTreePruner();
        ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
        criteria.add(new EstimatedNodeSize(0.05));
        return createBagC45(objects, objClass, new Entropy(), criteria, pruner);
    }

    public static DecisionTree createBagC45GPrunStop(List<Object> objects,
                                                     Class<? extends Object> objClass) throws FeatureNotSupported {
        DecisionTreePruner           pruner   = new DecisionTreePruner();
        ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
        criteria.add(new EstimatedNodeSize(0.05));
        return createBagC45(objects, objClass, new GainRatio(), criteria, pruner);
    }

    protected static DecisionTree createBagC45(List<Object> objects, Class<? extends Object> objClass, Heuristic h,
                                               ArrayList<StoppingCriterion> criteria, DecisionTreePruner pruner)
        throws FeatureNotSupported {
        DataType      data    = Learner.DEFAULT_DATA;
        C45Learner    learner = new C45Learner(h);
        ForestBuilder forest  = new ForestBuilder();

        String algoSuffices       = DecisionTreeFactory.getAlgoSuffices(forest.getTreeAlgo());
        String executionSignature = DecisionTreeFactory.getSignature(objClass, "", algoSuffices);

        /* create the memory */
        Memory mem = Memory.createFromObjects(objects, objClass, data);

        for (StoppingCriterion sc : criteria) {
            if (sc instanceof MaximumDepth) {
                int maxDepth = (int) ((mem.getInstances().getSchema().getAttrNames().size() - 1) * 0.70);
                ((MaximumDepth) sc).setDepth(maxDepth);
            }
            learner.addStoppingCriteria(sc);
        }

        SolutionSet product = forest.build(mem, learner);//objClass, target_attr, working_attr
        StatsPrinter.printLatexComment("Builder errors", executionSignature, false);
        StatsPrinter.printLatexComment(Stats.getErrors(), executionSignature, true);
        StatsPrinter.printLatex(product.getGlobalTrainStats(), product.getGlobalTestStats(), executionSignature, true);
        StatsPrinter.printLatexComment("Each Original Tree", executionSignature, true);
        for (Solution s : product.getSolutions()) {
            StatsPrinter.printLatex(s.getTrainStats(), s.getTestStats(), executionSignature, true);
        }
        Solution bestS = product.getBestSolution();
        StatsPrinter.printLatexComment("Best Original Tree", executionSignature, true);
        StatsPrinter.printLatex(bestS.getTrainStats(), bestS.getTestStats(), executionSignature, true);
        Tester t = DecisionTreeBuilder.getTester(bestS.getTree());
        StatsPrinter.printLatexComment("Best Original Tree(Global)", executionSignature, true);
        StatsPrinter.printLatex(t.test(product.getTrainSet()), t.test(product.getTestSet()), executionSignature, true);

        Tester.printStopping(learner.getStoppingCriteria(), Util.DRL_DIRECTORY + executionSignature);

        if (pruner != null) {
            //			for (Solution sol: product.getSolutions())
            //				pruner.prun_to_estimate(sol);
            //
            //			Solution s2 = pruner.getBestSolution();
            //			product.setBestSolutionId(s2.getTree().getId());
            //			for (Solution sol: product.getSolutions())
            //			pruner.prun_to_estimate(sol);
            pruner.prunToEstimate(product);
            Solution s2 = pruner.getBestSolution();

            Tester t2 = DecisionTreeBuilder.getTester(s2.getTree());
            StatsPrinter.printLatexComment("Best Pruned Tree", executionSignature, true);
            StatsPrinter.printLatex(t2.test(s2.getList()), t2.test(s2.getTestList()), executionSignature, true);
            Tester t2Global = DecisionTreeBuilder.getTester(s2.getTree());
            StatsPrinter.printLatexComment("Best Original Tree(Global)", executionSignature, true);
            StatsPrinter.printLatex(t2Global.test(product.getTrainSet()), t2Global.test(product.getTestSet()), executionSignature, true);
        }

        product.getBestSolution().getTree().setSignature(executionSignature);
        return product.getBestSolution().getTree();
    }

    /************************************************************************************************************************
     * Boosting Algorithms
     *
     * @param objects
     * @param objClass
     * @return
     * @throws FeatureNotSupported
     */
    public static DecisionTree createBoostC45E(List<Object> objects,
                                               Class<? extends Object> objClass) throws FeatureNotSupported {
        ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
        return createBoostC45(objects, objClass, new Entropy(), criteria, null);
    }

    public static DecisionTree createBoostC45G(List<Object> objects,
                                               Class<? extends Object> objClass) throws FeatureNotSupported {
        ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
        return createBoostC45(objects, objClass, new GainRatio(), criteria, null);
    }

    public static DecisionTree createBoostC45EStop(List<Object> objects,
                                                   Class<? extends Object> objClass) throws FeatureNotSupported {
        ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(3);
        criteria.add(new EstimatedNodeSize(0.5));
        criteria.add(new ImpurityDecrease());
        criteria.add(new MaximumDepth(50));
        return createBoostC45(objects, objClass, new Entropy(), criteria, null);
    }

    public static DecisionTree createBoostC45GStop(List<Object> objects,
                                                   Class<? extends Object> objClass) throws FeatureNotSupported {
        ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(3);
        criteria.add(new EstimatedNodeSize(0.5));
        criteria.add(new ImpurityDecrease());
        criteria.add(new MaximumDepth(50));
        return createBoostC45(objects, objClass, new GainRatio(), criteria, null);
    }

    public static DecisionTree createBoostC45EPrunStop(List<Object> objects,
                                                       Class<? extends Object> objClass) throws FeatureNotSupported {
        DecisionTreePruner           pruner   = new DecisionTreePruner();
        ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
        criteria.add(new EstimatedNodeSize(0.05));
        return createBoostC45(objects, objClass, new Entropy(), criteria, pruner);
    }

    public static DecisionTree createBoostC45GPrunStop(List<Object> objects,
                                                       Class<? extends Object> objClass) throws FeatureNotSupported {
        DecisionTreePruner           pruner   = new DecisionTreePruner();
        ArrayList<StoppingCriterion> criteria = new ArrayList<StoppingCriterion>(1);
        criteria.add(new EstimatedNodeSize(0.05));
        return createBoostC45(objects, objClass, new GainRatio(), criteria, pruner);
    }

    protected static DecisionTree createBoostC45(List<Object> objects, Class<? extends Object> objClass, Heuristic h,
                                                 ArrayList<StoppingCriterion> criteria, DecisionTreePruner pruner)
        throws FeatureNotSupported {
        DataType        data          = Learner.DEFAULT_DATA;
        C45Learner      learner       = new C45Learner(h);
        AdaBoostBuilder boostedForest = new AdaBoostBuilder();

        String algoSuffices       = DecisionTreeFactory.getAlgoSuffices(boostedForest.getTreeAlgo());
        String executionSignature = DecisionTreeFactory.getSignature(objClass, "", algoSuffices);

        /* create the memory */
        Memory mem = Memory.createFromObjects(objects, objClass, data);

        for (StoppingCriterion sc : criteria) {
            if (sc instanceof MaximumDepth) {
                int maxDepth = (int) ((mem.getInstances().getSchema().getAttrNames().size() - 1) * 0.70);
                ((MaximumDepth) sc).setDepth(maxDepth);
            }
            learner.addStoppingCriteria(sc);
        }

        SolutionSet product = boostedForest.build(mem, learner);//objClass, target_attr, working_attr
        StatsPrinter.printLatexComment("Builder errors", executionSignature, false);
        StatsPrinter.printLatexComment(Stats.getErrors(), executionSignature, true);
        StatsPrinter.printLatex(product.getGlobalTrainStats(), product.getGlobalTestStats(), executionSignature, true);
        StatsPrinter.printLatexComment("Each Original Tree", executionSignature, true);
        for (Solution s : product.getSolutions()) {
            StatsPrinter.printLatex(s.getTrainStats(), s.getTestStats(), executionSignature, true);
        }
        Solution bestS = product.getBestSolution();
        StatsPrinter.printLatexComment("Best Original Tree", executionSignature, true);
        StatsPrinter.printLatex(bestS.getTrainStats(), bestS.getTestStats(), executionSignature, true);

        Tester.printStopping(learner.getStoppingCriteria(), Util.DRL_DIRECTORY + executionSignature);

        if (pruner != null) {
            //			for (Solution sol: product.getSolutions())
            //				pruner.prun_to_estimate(sol);
            pruner.prunToEstimate(product);
            Solution s2 = pruner.getBestSolution();
            //			System.out.println(s2.getTree().getId());
            //			product.setBestSolutionId(s2.getTree().getId());
            Tester t2 = DecisionTreeBuilder.getTester(pruner.getBestSolution().getTree());
            StatsPrinter.printLatexComment("Best Pruned Tree", executionSignature, true);
            StatsPrinter.printLatex(t2.test(s2.getList()), t2.test(s2.getTestList()), executionSignature, true);

            Tester t2Global = DecisionTreeBuilder.getTester(s2.getTree());
            StatsPrinter.printLatexComment("Best Original Tree(Global)", executionSignature, true);
            StatsPrinter.printLatex(t2Global.test(product.getTrainSet()), t2Global.test(product.getTestSet()), executionSignature, true);
        }

        product.getBestSolution().getTree().setSignature(executionSignature);
        return product.getBestSolution().getTree();
    }

    public static String getSignature(Class<? extends Object> objClass, String fileName, String suffices) {

        //String fileName = (dataFile == null || dataFile == "") ? this.getRuleClass().getSimpleName().toLowerCase(): dataFile; 			
        String objectSignature = fileName;
        if (objectSignature == null || objectSignature == "") {
            objectSignature = objClass.getSimpleName().toLowerCase();
            if (suffices != null && suffices != "") {
                objectSignature += "_" + suffices;
            }
        }

        String packageFolders     = objClass.getPackage().getName();
        String packageNames       = packageFolders.replace('.', '/');
        String executionSignature = packageNames + "/" + objectSignature; //"src/main/rules/"+

        return executionSignature;
    }

    public static String getAlgoSuffices(TreeAlgo treeA) {
        String suffix = "c45";

        switch (treeA) {
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
                suffix += "_?";
        }
        return suffix;
    }
}
