package org.drools.learner.builder;

import org.drools.learner.DecisionTree;
import org.drools.learner.Memory;
import org.drools.learner.Stats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleTreeBuilder implements DecisionTreeBuilder {

    protected static final transient Logger log = LoggerFactory.getLogger(SingleTreeBuilder.class);

    private TreeAlgo algorithm = TreeAlgo.SINGLE; // default bagging, TODO boosting

    //	private Stats train_evaluation, test_evaluation;
    //	private Stats train_evaluation2, test_evaluation2;
    //	private SingleTreeTester tester;

    public SingleTreeBuilder() {//Learner _trainer) {
    }

    /*
     * the memory has the information the instances: the objects which the
     * decision tree will work on the schema: the definition of the object
     * instance (Class<?>) klass, String targetField, List<String>
     * workingAttributes
     */
    public SolutionSet build(Memory wm, Learner trainer) {
        SolutionSet solSet =  new SolutionSet(wm);
        if (solSet.getTargets().size() > 1) {
            //throw new FeatureNotSupported("There is more than 1 target candidates");
            if (log.isErrorEnabled()) {
                log.error("There is more than 1 target candidates");
            }

            System.exit(0);
            // TODO put the feature not supported exception || implement it
        }

        trainer.setInputSpec(solSet.getInputSpec());
        trainer.setTrainingDataSize(solSet.getTrainSet().getSize());
        DecisionTree oneTree = trainer.instantiateTree();
        if (log.isDebugEnabled()) {
            log.debug("\n" + "Training a tree" + "\n");
        }
        trainer.trainTree(oneTree, solSet.getTrainSet());
        oneTree.setID(0);

        Tester   t     = DecisionTreeBuilder.getTester(oneTree);
        Stats    train = t.test(solSet.getTrainSet());
        Stats    test  = t.test(solSet.getTestSet());
        Solution best  = new Solution(oneTree, solSet.getTrainSet());
        best.setTestList(solSet.getTestSet());
        best.setTrainStats(train);
        best.setTestStats(test);
        solSet.addSolution(best);

        return solSet;
    }

    public TreeAlgo getTreeAlgo() {
        return this.algorithm; // default
    }

    //	public void printResults(String executionSignature) {
    //		tester.printStats(getTrainStats(), Util.DRL_DIRECTORY + executionSignature, false);
    //		tester.printStats(getTestStats(), Util.DRL_DIRECTORY + executionSignature, true);
    //	}
    //	
    //	public void printLatex(String executionSignature) {
    //		StringBuffer sb = new StringBuffer();
    //		sb.append("#"+ Stats.getErrors());
    //		sb.append( "\n");
    //		sb.append(getTrainStats().print4Latex() +getTestStats().print4Latex()+"\\\\"+ "\n");
    //		sb.append( "\n");
    //		
    //		StatsPrinter.print2file(sb, Util.DRL_DIRECTORY +executionSignature, true);
    //	}
    //
    //	public void printLatex2(String executionSignature) {
    //		StringBuffer sb = new StringBuffer();
    //		sb.append("#"+ Stats.getErrors());
    //		sb.append( "\n");
    //		sb.append(train_evaluation2.print4Latex() +test_evaluation2.print4Latex()+"\\\\"+ "\n");
    //		sb.append( "\n");
    //		
    //		StatsPrinter.print2file(sb, Util.DRL_DIRECTORY +executionSignature, true);
    //	}
}
