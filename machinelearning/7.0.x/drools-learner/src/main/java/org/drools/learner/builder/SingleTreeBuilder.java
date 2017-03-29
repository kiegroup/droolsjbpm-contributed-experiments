package org.drools.learner.builder;

import org.drools.learner.DecisionTree;
import org.drools.learner.Stats;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;

public class SingleTreeBuilder extends DecisionTreeBuilder {

    private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger( SingleTreeBuilder.class, SimpleLogger.DEFAULT_LEVEL );
    private static SimpleLogger slog = LoggerFactory.getSysOutLogger( SingleTreeBuilder.class, SimpleLogger.DEFAULT_LEVEL );

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
    public void internalBuild( SolutionSet sol, Learner trainer ) {

        if ( sol.getTargets().size() > 1 ) {
            //throw new FeatureNotSupported("There is more than 1 target candidates");
            if ( flog.error() != null )
                flog.error().log( "There is more than 1 target candidates" );

            System.exit( 0 );
            // TODO put the feature not supported exception || implement it
        }

        trainer.setInputSpec( sol.getInputSpec() );
        trainer.setTrainingDataSize( sol.getTrainSet().getSize() );
        DecisionTree oneTree = trainer.instantiateTree();
        if ( slog.debug() != null )
            slog.debug().log( "\n" + "Training a tree" + "\n" );
        trainer.trainTree( oneTree, sol.getTrainSet() );
        oneTree.setID( 0 );

        Tester t = getTester( oneTree );
        Stats train = t.test( sol.getTrainSet() );
        Stats test = t.test( sol.getTestSet() );
        Solution best = new Solution( oneTree, sol.getTrainSet() );
        best.setTestList( sol.getTestSet() );
        best.setTrainStats( train );
        best.setTestStats( test );
        sol.addSolution( best );

        return;

    }

    public TreeAlgo getTreeAlgo() {
        return this.algorithm; // default
    }

    public Solution getBestSolution() {
        return solutions.getBestSolution();
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
