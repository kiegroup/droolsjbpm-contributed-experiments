package org.drools.learner.eval;

public class ImpurityDecrease implements StoppingCriterion {

    private double beta = 0.1;

    public ImpurityDecrease(double beta) {
        this.beta = beta;
    }

    public boolean stop(InformationContainer bestAttrEval) {
        if (bestAttrEval.attributeEval < beta) {
            return true;
        } else {
            return false;
        }
    }
}
