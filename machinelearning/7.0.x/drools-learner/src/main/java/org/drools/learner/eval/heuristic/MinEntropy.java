package org.drools.learner.eval.heuristic;

public class MinEntropy extends InformationGain implements Heuristic {

    public MinEntropy() {
        super();
        super.multiplier = -1.0;
    }
}
