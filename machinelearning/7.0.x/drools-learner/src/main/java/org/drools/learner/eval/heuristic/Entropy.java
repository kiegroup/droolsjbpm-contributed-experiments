package org.drools.learner.eval.heuristic;

import java.util.ArrayList;
import java.util.List;

import org.drools.core.phreak.RuleExecutor;
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

public class Entropy implements Heuristic {

    protected static double multiplier = 1.0;
    //public Entropy
    /*
     * - chooses the best attribute, - return the choosen attributes evaluation
     * results (information gain and/or gain ratio) can process categorical, and
     * quantitative attribute domains
     * 
     * used by: c45Alternator, c45Learner, c45Iterator
     */

    protected static final transient Logger   log = LoggerFactory.getLogger(Entropy.class);
    protected double              dataEval;
    protected InstDistribution    instsByTarget;
    protected ArrayList<Instance> sortedInstances;
    protected Domain              domain;

    public Entropy() {
        //
    }

    public Entropy(double m) {
        multiplier = m;
    }

    public void init(InstDistribution instsByTarget) {
        this.instsByTarget = instsByTarget;
        dataEval = calcInfo(instsByTarget);
        sortedInstances = null;
        domain = null;
    }

    /*
     * for both
     */
    public static double calcInfoAttr(CondClassDistribution instancesByAttr) {
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
                    double info = calcInfo(instancesByAttr.getDistributionOf(attCategory));

                    sum += prob * info;
                    //log.debug("]} ");
                }
            }
        }
        //log.debug("\n == "+sum);
        return sum;
    }

    /**
     * it returns the information value of facts entropy that characterizes the
     * (im)purity of an arbitrary collection of examples
     * @param quantityByClass the distribution of the instances by the class attribute
     * (target)
     */
    public static double calcInfo(ClassDistribution quantityByClass) {

        double dataSize = quantityByClass.getSum();

        double prob, sum    = 0;
        Domain targetDomain = quantityByClass.getClassDomain();
        if (dataSize > 0) {
            for (int category = 0; category < targetDomain.getCategoryCount(); category++) {

                Object targetCategory = targetDomain.getCategory(category);
                double numInClass     = quantityByClass.getVoteFor(targetCategory);

                if (numInClass > 0) {
                    prob = numInClass / dataSize;
                    /* TODO what if it is a sooo small number ???? */
                    //log.debug("("+num_in_class+ "/"+data_size+":"+prob+")" +"*"+ Util.log2(prob) + " + ");
                    sum -= prob * Util.log2(prob);
                }
            }
        }
        //log.debug("= " +sum);
        return sum;
    }

    public double getEval(Domain attrDomain) {
        CondClassDistribution instsByTarget = infoAttr(attrDomain);
        return multiplier * (dataEval - Entropy.calcInfoAttr(instsByTarget));
    }

    public double getEvalCont(Domain attrDomain) {

        double             attributeEval = 0.0d;
        QuantitativeDomain trialDomain   = QuantitativeDomain.createFromDomain(attrDomain);

        Categorizer visitor = new Categorizer(instsByTarget);
        visitor.findSplits(trialDomain);

        //	trial domain is modified
        if (trialDomain.getNumIndices() > 1) {
            CondClassDistribution instsByAttr = infoContattr(visitor); //.getSortedInstances(), trialDomain);
            attributeEval = dataEval - Entropy.calcInfoAttr(instsByAttr);
        }
        domain = trialDomain;
        sortedInstances = visitor.getSortedInstances();
        return multiplier * attributeEval;
    }

    public double getDataEval() {
        return dataEval;
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

    public CondClassDistribution infoAttr(Domain attrDomain) {

        Domain targetDomain = instsByTarget.getClassDomain();

        //log.debug("What is the attributeToSplit? " + attr_domain);

        /* initialize the hashtable */
        CondClassDistribution instsByAttr = new CondClassDistribution(attrDomain, targetDomain);
        instsByAttr.setTotal(instsByTarget.getSum());

        //log.debug("Cond distribution for "+ attr_domain + " \n"+ insts_by_attr);

        for (int category = 0; category < targetDomain.getCategoryCount(); category++) {
            Object targetCategory = targetDomain.getCategory(category);

            for (Instance inst : instsByTarget.getSupportersFor(targetCategory)) {
                Object instAttrCategory = inst.getAttrValue(attrDomain.getFReferenceName());

                Object instClass = inst.getAttrValue(targetDomain.getFReferenceName());

                if (!targetCategory.equals(instClass)) {
                    log.error("How the fuck they are not the same ? {} {}", targetCategory, instClass);
                    System.exit(0);
                }
                instsByAttr.change(instAttrCategory, targetCategory, inst.getWeight()); //+1
            }
        }

        return instsByAttr;
    }

    /* you can calculate this before */

    /*
     * calculates the information of a quantitative domain given the split
     * indexes of instances a wrapper for the quantitative domain to be able to
     * calculate the stats
     */
    //public static double info_contattr(InstanceList data, Domain targetDomain, QuantitativeDomain splitDomain) {
    public CondClassDistribution infoContattr(Categorizer visitor) {

        List<Instance>     data         = visitor.getSortedInstances();
        QuantitativeDomain splitDomain  = visitor.getSplitDomain();
        Domain             targetDomain = instsByTarget.getClassDomain();
        String             targetAttr   = targetDomain.getFReferenceName();

        CondClassDistribution instancesByAttr = new CondClassDistribution(splitDomain, targetDomain);
        instancesByAttr.setTotal(data.size());

        int    index      = 0;
        int    splitIndex = 0;
        Object attrKey    = splitDomain.getCategory(splitIndex);
        for (Instance i : data) {

            if (index == splitDomain.getSplit(splitIndex).getIndex() + 1) {
                attrKey = splitDomain.getCategory(splitIndex + 1);
                splitIndex++;
            }
            Object targetKey = i.getAttrValue(targetAttr);
            instancesByAttr.change(attrKey, targetKey, i.getWeight()); //+1

            index++;
        }

        return instancesByAttr;
        //		double sum = calc_info_attr(instances_by_attr);
        //		return sum;

    }
}
