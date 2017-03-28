package org.drools.learner.eval;

public class MaximumDepth implements StoppingCriterion {

    private int limitDepth;

    public MaximumDepth( int depth ) {
        limitDepth = depth;
    }

    public boolean stop( InformationContainer bestAttrEval ) {
        if ( bestAttrEval.getDepth() <= limitDepth )
            return false;
        else
            return true;
    }

}
