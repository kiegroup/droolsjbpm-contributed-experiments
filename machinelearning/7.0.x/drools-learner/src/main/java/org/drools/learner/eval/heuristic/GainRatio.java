package org.drools.learner.eval.heuristic;

import org.drools.learner.Domain;
import org.drools.learner.QuantitativeDomain;
import org.drools.learner.eval.Categorizer;
import org.drools.learner.eval.CondClassDistribution;
import org.drools.learner.tools.Util;

public class GainRatio extends InformationGain implements Heuristic {

    public GainRatio() {
        super();
    }

    public static double splitInfo(CondClassDistribution instancesByAttr) {
        //Collection<Object> attributeValues = instances_by_attr.getAttributes();
        double dataSize = instancesByAttr.getTotal();
        double sum      = 0;
        if (dataSize == 0) {
            throw new RuntimeException("Cannot split a data size of 0");
        }

        for (int attrIdx = 0; attrIdx < instancesByAttr.getNumCondClasses(); attrIdx++) {
            Object attrCategory = instancesByAttr.getCondClass(attrIdx);
            double numInAttr    = instancesByAttr.getTotalAttrCategory(attrCategory);

            if (numInAttr > 0.0) {
                double prob = numInAttr / dataSize;
                sum -= (prob * Util.log2(prob));
            }
        }

        //flog.debug("\n == "+sum);
        return sum;
    }

    public double evalCategorical(Domain attrDomain) {
        return calculateCategoricalGainRatio(attrDomain);
    }

    public double calculateCategoricalGainRatio(Domain attrDomain) {
        CondClassDistribution instsByAttr = instsByTarget.categoricalGroupBy(attrDomain);
        double                infoGain    = calculateInformationGain(attrDomain);//super.entropyValue - InformationGain.calculateInformation(instsByAttr);

        double splitInfo = GainRatio.splitInfo(instsByAttr);

        System.err.println("(GainRatio) info_gain = " + infoGain + "/" + splitInfo);
        return infoGain / splitInfo;
    }

    public double evalContinuous(Domain attrDomain) {

        return calculateContinuiousGainRatio(attrDomain);
    }

    public double calculateContinuiousGainRatio(Domain attrDomain) {
        double             infoGain = 0.0d, splitInfo = 1.0d;
        QuantitativeDomain trialDomain   = QuantitativeDomain.createFromDomain(attrDomain);

        Categorizer visitor = new Categorizer(instsByTarget);
        visitor.findSplits(trialDomain);

        domain = trialDomain;
        sortedInstances = visitor.getSortedInstances();

        // trial domain is modified
        if (trialDomain.getNumIndices() > 1) {
            CondClassDistribution instsByAttr = instsByTarget.continuousGroupBy(visitor);
            infoGain = calculateInformationGain(attrDomain);//super.entropyValue - InformationGain.calculateInformation(instsByAttr);

            splitInfo = GainRatio.splitInfo(instsByAttr);
        }

        return infoGain / splitInfo;
    }
}
