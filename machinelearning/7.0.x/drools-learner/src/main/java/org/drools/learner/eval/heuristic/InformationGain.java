package org.drools.learner.eval.heuristic;

import java.util.ArrayList;

import org.drools.learner.Domain;
import org.drools.learner.Instance;
import org.drools.learner.QuantitativeDomain;
import org.drools.learner.eval.Categorizer;
import org.drools.learner.eval.ClassDistribution;
import org.drools.learner.eval.CondClassDistribution;
import org.drools.learner.eval.InstDistribution;
import org.drools.learner.tools.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InformationGain implements Heuristic {

    protected static double multiplier = 1.0;
    //public Entropy
    /*
     * - chooses the best attribute, - return the choosen attributes evaluation
     * results (calculateInformation gain and/or gain ratio) can process categorical, and
     * quantitative attribute domains
     * 
     * used by: c45Alternator, c45Learner, c45Iterator
     */

    protected static final transient Logger   log = LoggerFactory.getLogger(InformationGain.class);
    protected double              entropyValue;
    protected InstDistribution    instsByTarget;
    protected ArrayList<Instance> sortedInstances;
    protected Domain              domain;

    public InformationGain() {
        //
    }

    public InformationGain(double m) {
        multiplier = m;
    }

    public void init(InstDistribution instsByTarget) {
        this.instsByTarget = instsByTarget;
        entropyValue = calculateEntropy(instsByTarget);
        sortedInstances = null;
        domain = null;
    }

    /*
     * Information from "Information Theory"
     * The weighted averages of all the entropies for a give attribute
     */
    public static double calculateInformation(CondClassDistribution instancesByAttr) {
        //Collection<Object> attributeValues = instances_by_attr.getAttributes();
        double dataSize = instancesByAttr.getTotal();
        double sum      = 0.0;
        if (dataSize > 0) {
            for (int attrIdx = 0; attrIdx < instancesByAttr.getNumCondClasses(); attrIdx++) {
                Object attCategory  = instancesByAttr.getCondClass(attrIdx);
                double totalNumAttr = instancesByAttr.getTotalAttrCategory(attCategory);

                if (totalNumAttr > 0) {
                    double prob = totalNumAttr / dataSize;
                    //log.debug("{("+total_num_attr +"/"+data_size +":"+prob +")* [");
                    double entropy = calculateEntropy(instancesByAttr.getDistributionOf(attCategory));
                    sum += prob * entropy;
                }
            }
        }
        return sum;
    }

    /**
     * it returns the calculateInformation value of facts entropy that characterizes the
     * (im)purity of an arbitrary collection of examples
     * @param quantityByClass the distribution of the instances by the class attribute
     * (target)
     */
    public static double calculateEntropy(ClassDistribution quantityByClass) {

        double dataSize = quantityByClass.getSum();

        double prob, sum    = 0;
        Domain targetDomain = quantityByClass.getClassDomain();
        if (dataSize > 0) {
            for (int category = 0; category < targetDomain.getCategoryCount(); category++) {

                Object targetCategory = targetDomain.getCategory(category);
                double numInClass     = quantityByClass.getVoteFor(targetCategory);

                if (numInClass > 0) {
                    prob = numInClass / dataSize;
                    // TODO what if it is a sooo small number ????
                    sum -= prob * Util.log2(prob);
                }
            }
        }
        return sum;
    }

    public double calculateInformationGain(Domain attrDomain) {
        CondClassDistribution instsByAttr = instsByTarget.categoricalGroupBy(attrDomain);
        return multiplier * (entropyValue - InformationGain.calculateInformation(instsByAttr));
    }

    public double evalCategorical(Domain attrDomain) {
        return calculateInformationGain(attrDomain);
    }



    public double evalContinuous(Domain attrDomain) {

        double             attributeEval = 0.0d;
        QuantitativeDomain trialDomain   = QuantitativeDomain.createFromDomain(attrDomain);

        Categorizer visitor = new Categorizer(instsByTarget);
        visitor.findSplits(trialDomain);

        //	trial domain is modified
        if (trialDomain.getNumIndices() > 1) {
            CondClassDistribution instsByAttr = instsByTarget.continuousGroupBy(visitor); //.getSortedInstances(), trialDomain);
            attributeEval = entropyValue - InformationGain.calculateInformation(instsByAttr);
        }
        domain = trialDomain;
        sortedInstances = visitor.getSortedInstances();
        return multiplier * attributeEval;
    }

    public double getDataEval() {
        return entropyValue;
    }

    public Domain getDomain() {
        return domain;
    }

    public ArrayList<Instance> getSortedInstances() {
        return sortedInstances;
    }

    public double getWorstEval() {
        return -1000.0d;
    }


}
