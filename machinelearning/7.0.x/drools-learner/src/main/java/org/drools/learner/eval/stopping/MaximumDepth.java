package org.drools.learner.eval.stopping;

import org.drools.learner.eval.InformationContainer;

public class MaximumDepth implements StoppingCriterion {

    private int limitDepth;
    private int numPrunned;

    public MaximumDepth(int depth) {
        limitDepth = depth;
        numPrunned = 0;
    }

    public boolean stop(InformationContainer bestAttrEval) {
        if (bestAttrEval.getDepth() <= limitDepth) {
            return false;
        } else {
            numPrunned++;
            return true;
        }
    }

    public int getNumPruned() {
        return numPrunned;
    }

    public void setDepth(int maxDepth) {
        limitDepth = maxDepth;
    }
}
