package org.drools.learner.eval.heuristic;

import java.util.Random;

import org.drools.learner.Domain;
import org.drools.learner.QuantitativeDomain;
import org.drools.learner.eval.Categorizer;
import org.drools.learner.eval.CondClassDistribution;

public class RandomInfo extends InformationGain implements Heuristic {

    private static Random infoNumber = new Random(System.currentTimeMillis());

    public RandomInfo() {
        super();
    }

    public double evalCategorical(Domain attrDomain) {
        CondClassDistribution instsByAttr = instsByTarget.categoricalGroupBy(attrDomain);
        double                infoGain    = super.entropyValue - InformationGain.calculateInformation(instsByAttr);

        return infoNumber.nextDouble(); //info_gain;// /split_info;
    }

    public double evalContinuous(Domain attrDomain) {

        double             attributeEval = 0.0d, splitInfo = 1.0d;
        QuantitativeDomain trialDomain   = QuantitativeDomain.createFromDomain(attrDomain);

        Categorizer visitor = new Categorizer(instsByTarget);
        visitor.findSplits(trialDomain);

        domain = trialDomain;
        sortedInstances = visitor.getSortedInstances();
        return infoNumber.nextDouble();//attribute_eval / split_info;
    }
}
