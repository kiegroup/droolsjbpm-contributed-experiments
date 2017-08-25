package org.drools.learner.eval.stopping;

import org.drools.learner.eval.InformationContainer;

public class ImpurityDecrease implements StoppingCriterion {

    private double beta = 0.00001;
    private int numPrunned;

    public ImpurityDecrease() {
        numPrunned = 0;
    }

    public ImpurityDecrease(double beta) {
        numPrunned = 0;
        this.beta = beta;
    }

    public boolean stop(InformationContainer bestAttrEval) {
        if (bestAttrEval.getAttributeEval() < beta) {
            numPrunned++;
            return true;
        } else {
            return false;
        }
    }

    public int getNumPruned() {
        return numPrunned;
    }
}
