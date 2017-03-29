package org.drools.learner.eval.stopping;

import org.drools.learner.eval.InformationContainer;

public interface StoppingCriterion {

    public boolean stop( InformationContainer bestAttrEval );

    public int getNumPruned();

}
