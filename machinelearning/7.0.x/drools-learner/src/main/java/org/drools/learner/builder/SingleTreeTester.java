package org.drools.learner.builder;

import org.drools.learner.DecisionTree;
import org.drools.learner.Instance;
import org.drools.learner.InstanceList;
import org.drools.learner.Stats;

public class SingleTreeTester extends Tester {

    private DecisionTree singleTree;

    public SingleTreeTester( DecisionTree oneTree ) {
        singleTree = oneTree;
    }

    /* test the entire set */
    public Stats test( InstanceList data ) {

        //flog.debug(Util.ntimes("\n", 2)+Util.ntimes("$", 5)+" TESTING "+Util.ntimes("\n", 2));

        Stats evaluation = new Stats( singleTree.getObjClass() );
        int i = 0;
        for ( Instance instance : data.getInstances() ) {
            Object treeDecision = singleTree.vote( instance );
            Integer result = evaluate( singleTree.getTargetDomain(), instance, treeDecision );

            //flog.debug(Util.ntimes("#\n", 1)+i+ " <START> TEST: instant="+ instance + " = target "+ result);			
            //			if (i%1000 ==0 && slog.stat() !=null)
            //				slog.stat().stat(".");

            evaluation.change( result, 1 );
            i++;
        }
        return evaluation;
    }

    /* test the entire set */
    public Integer test( Instance i ) {

        Object treeDecision = singleTree.vote( i );
        return evaluate( singleTree.getTargetDomain(), i, treeDecision );
    }

    //	public void printStats(Stats evaluation, String executionSignature, boolean append) {
    //		super.printStats(evaluation, executionSignature, append);
    //	}

}
