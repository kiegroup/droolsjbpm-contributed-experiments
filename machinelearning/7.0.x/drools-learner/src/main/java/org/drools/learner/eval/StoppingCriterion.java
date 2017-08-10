package org.drools.learner.eval;

public interface StoppingCriterion {

    public boolean stop(InformationContainer bestAttrEval);
}
