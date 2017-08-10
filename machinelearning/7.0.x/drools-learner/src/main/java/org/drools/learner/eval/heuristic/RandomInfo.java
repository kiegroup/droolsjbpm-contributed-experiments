package org.drools.learner.eval.heuristic;

import java.util.Random;

import org.drools.learner.Domain;
import org.drools.learner.QuantitativeDomain;
import org.drools.learner.eval.Categorizer;
import org.drools.learner.eval.CondClassDistribution;

public class RandomInfo extends Entropy implements Heuristic {

    private static Random infoNumber = new Random(System.currentTimeMillis());

    public RandomInfo() {
        super();
    }

    public double getEval(Domain attrDomain) {
        CondClassDistribution instsByAttr = super.infoAttr(attrDomain);
        double                infoGain    = super.dataEval - Entropy.calcInfoAttr(instsByAttr);

        return infoNumber.nextDouble(); //info_gain;// /split_info;
    }

    public double getEvalCont(Domain attrDomain) {

        double             attributeEval = 0.0d, splitInfo = 1.0d;
        QuantitativeDomain trialDomain   = QuantitativeDomain.createFromDomain(attrDomain);

        Categorizer visitor = new Categorizer(instsByTarget);
        visitor.findSplits(trialDomain);

        domain = trialDomain;
        sortedInstances = visitor.getSortedInstances();
        return infoNumber.nextDouble();//attribute_eval / split_info;
    }
}
