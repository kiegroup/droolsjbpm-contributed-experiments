package org.drools.learner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.drools.learner.eval.InstDistribution;
import org.drools.learner.eval.heuristic.Heuristic;

public class FixedHeuristic implements Heuristic {
    InstDistribution instsByTarget;

    Map<Domain, Double> values = new HashMap<Domain, Double>();

    public FixedHeuristic() {

    }


    @Override public void init(InstDistribution instsByTarget) {
        this.instsByTarget = instsByTarget;
    }

    @Override public double evalCategorical(Domain domain) {
        return values.get(domain);
    }

    @Override public double evalContinuous(Domain domain) {
        return values.get(domain);
    }

    @Override public Domain getDomain() {
        return null;
    }

    @Override public ArrayList<Instance> getSortedInstances() {
        return null;
    }

    @Override public double getWorstEval() {
        return 0;
    }
}
