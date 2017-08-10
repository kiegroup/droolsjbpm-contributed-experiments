package org.drools.learner.eval;

import org.drools.learner.Domain;
import org.drools.learner.QuantitativeDomain;
import org.drools.learner.tools.Util;

public class GainRatio extends Entropy implements Heuristic {

    public GainRatio() {
        super();
    }

    private static double splitFnfo(CondClassDistribution instancesByAttr) {
        //Collection<Object> attributeValues = instances_by_attr.getAttributes();
        double dataSize = instancesByAttr.getTotal();
        double sum      = 1.0;
        if (dataSize > 0) {
            for (int attrIdx = 0; attrIdx < instancesByAttr.getNumCondClasses(); attrIdx++) {
                Object attrCategory = instancesByAttr.getCondClass(attrIdx);
                double numInAttr    = instancesByAttr.getTotalAttrCategory(attrCategory);

                if (numInAttr > 0.0) {
                    double prob = numInAttr / dataSize;
                    sum -= prob * Util.log2(prob);
                }
            }
        } else {
            System.err.println("????? data_size = " + dataSize);
            System.exit(0);
        }

        //flog.debug("\n == "+sum);
        return sum;
    }

    public double getEval(Domain attrDomain) {
        CondClassDistribution instsByAttr = super.infoAttr(attrDomain);
        double                infoHain    = super.dataEval - Entropy.calcInfoAttr(instsByAttr);

        double splitInfo = GainRatio.splitFnfo(instsByAttr);

        System.err.println("(GainRatio) info_gain = " + infoHain + "/" + splitInfo);
        return infoHain / splitInfo;
    }

    public double getEvalCont(Domain attrDomain) {

        double             attributeEval = 0.0d, splitInfo = 1.0d;
        QuantitativeDomain trialDomain   = QuantitativeDomain.createFromDomain(attrDomain);

        Categorizer visitor = new Categorizer(instsByTarget);
        visitor.findSplits(trialDomain);

        // trial domain is modified
        if (trialDomain.getNumIndices() > 1) {
            CondClassDistribution instsByAttr = super.infoContattr(visitor);
            attributeEval = super.dataEval - Entropy.calcInfoAttr(instsByAttr);

            splitInfo = GainRatio.splitFnfo(instsByAttr);
        }
        domain = trialDomain;
        sortedInstances = visitor.getSortedInstances();
        return attributeEval / splitInfo;
    }
}
