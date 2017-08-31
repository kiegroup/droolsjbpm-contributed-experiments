package org.drools.learner.eval;

import java.util.ArrayList;
import java.util.Collections;

import org.drools.learner.Domain;
import org.drools.learner.Instance;
import org.drools.learner.InstanceComparator;
import org.drools.learner.QuantitativeDomain;
import org.drools.learner.eval.heuristic.InformationGain;
import org.drools.learner.tools.Util;
import org.slf4j.Logger;

public class Categorizer {

    /* TODO make it singleton */
    private static final             Object key0         = Integer.valueOf(0);
    private static final             Object key1         = Integer.valueOf(1);
    private static final             Domain binaryDomain = initBinaryDomain();
    protected static final transient Logger log          = org.slf4j.LoggerFactory.getLogger(Categorizer.class);
    private ArrayList<Instance> data;
    private QuantitativeDomain  splitDomain;
    private Domain              targetDomain;
    private InstanceComparator targetComp, attrComp;
    private ClassDistribution distribution;
    /* TODO put this into globals file */
    private int maxDepth = 1;

    public Categorizer(InstDistribution dataInClass) {
        //List<Instance> _instancese
        this.data = new ArrayList<Instance>((int) dataInClass.getSum());
        this.distribution = dataInClass;
        this.targetDomain = dataInClass.getClassDomain();

        for (int category = 0; category < targetDomain.getCategoryCount(); category++) {
            Object targetCategory = targetDomain.getCategory(category);
            data.addAll(dataInClass.getSupportersFor(targetCategory));
        }

        this.targetComp = new InstanceComparator(targetDomain.getFReferenceName());
    }

    public static Domain initBinaryDomain() {
        //if (binaryDomain == null) {
        Domain bD = new Domain(Object.class, "binary", Integer.class); //splitDomain.cheapClone();
        bD.addCategory(key0); //addSplitPoint(new SplitPoint(0, key0));
        bD.addCategory(key1); //addSplitPoint(new SplitPoint(1, key1));
        //}
        return bD;
    }

    public void findSplits(QuantitativeDomain attrDomain) {
        this.splitDomain = attrDomain;
        this.attrComp = new InstanceComparator(attrDomain.getFReferenceName());

        // this is doing nothing FIXME mdp
        initBinaryDomain();

        // CATEGORIZATION 1. sort the values
        Collections.sort(this.data, this.attrComp);

        int    lastIndex = this.data.size() - 1;
        Object lastValue = data.get(lastIndex).getAttrValue(this.splitDomain.getFReferenceName());
        this.splitDomain.addSplitPoint(new SplitPoint(lastIndex, lastValue));

        findASplit(0, this.data.size(), this.maxDepth, this.distribution);
        return;
    }

    public ArrayList<Instance> getSortedInstances() {
        return data;
    }

    public QuantitativeDomain getSplitDomain() {
        return this.splitDomain;
    }

    private double findASplit(int beginIndex, int size, int depth, ClassDistribution factsInClass) {

        if (log.isDebugEnabled()) {
            log.debug("./n");
        }
        if (data.size() <= 1 || (size - beginIndex) < 2) {
            if (log.isWarnEnabled()) {
                log.warn("fact.size <=1 returning 0.0....");
            }
            return 0.0;
        }
        //		if (facts_in_class.getSum() == 0) {
        //			return 0.0;	// there is no one in it
        //		}
        factsInClass.evaluateMajority();
        if (factsInClass.getNumberOfSupportedCategories() == 1) {
            if (log.isWarnEnabled()) {
                log.warn("getNum_supported_target_classes=1 returning 0.0....");
            }
            return 0.0; //?
        }

        if (depth == 0) {
            if (log.isWarnEnabled()) {
                log.warn("depth == 0  returning 0.0....");
            }
            return 0.0;
        }

        /* initialize the distribution */
        CondClassDistribution instancesByAttr = new CondClassDistribution(binaryDomain, this.targetDomain);
        instancesByAttr.setDistForAttrValue(key1, factsInClass);
        instancesByAttr.setTotal(factsInClass.getSum());

        double bestSum = 100000.0;

        int                   numSplitPoints   = 0, splitIndex = beginIndex + 1;
        int                   lastIndex        = size - 1;
        Object                lastValue        = data.get(lastIndex).getAttrValue(this.splitDomain.getFReferenceName());
        SplitPoint            bestPoint        = new SplitPoint(lastIndex, lastValue);
        CondClassDistribution bestDistribution = null;//instances_by_attr;
        Instance              i1               = data.get(beginIndex), i2;

        if (log.isDebugEnabled()) {
            log.debug("\nentropy.info_cont() SEARCHING: " + beginIndex + " until " + size + " attr " + this.splitDomain.getFName() + " " + i1);
        }
        for (int index = beginIndex + 1; index < size; index++) {
            i2 = data.get(index);

            /*
             * every time i read a new instance and change the place in the
             * distribution
             */
            instancesByAttr.change(key0, i1.getAttrValue(this.targetDomain.getFReferenceName()), +1.0d * i1.getWeight()); //+1
            instancesByAttr.change(key1, i1.getAttrValue(this.targetDomain.getFReferenceName()), -1.0d * i1.getWeight()); //-1

            if (log.isDebugEnabled()) {
                log.debug("Instances " + i1 + " vs " + i2);
            }
            /*
             * CATEGORIZATION 2.1. Cut points are points in the sorted list
             * above where the class labels change. Eg. if I had five instances
             * with values for the attribute of interest and labels (1.0,A),
             * (1.4,A), (1.7, A), (2.0,B), (3.0, B), (7.0, A), then there are
             * only two cutpoints of interest: 1.85 and 5 (mid-way between the
             * points where the classes change from A to B or vice versa).
             */
            if (targetComp.compare(i1, i2) != 0 && attrComp.compare(i1, i2) != 0) {
                numSplitPoints++;

                if (log.isDebugEnabled()) {
                    log.debug("entropy.info_cont() SEARCHING: " + (index) + " attr " + this.splitDomain.getFName() + " " + i2);
                }
                // the cut point
                Object cpI     = i1.getAttrValue(this.splitDomain.getFReferenceName());
                Object cpINext = i2.getAttrValue(this.splitDomain.getFReferenceName());

                Class<?> fClass = this.splitDomain.getFType();

                Object cutPoint = Util.calculateMidPoint(fClass, cpI, cpINext);

                /*
                 * CATEGORIZATION 3. Evaluate your favourite disparity measure
                 * (info gain, gain ratio, gini coefficient, chi-squared test)
                 * on the cut point and calculate its gain
                 */
                double sum = InformationGain.calculateInformation(instancesByAttr);
                if (sum < bestSum) {
                    bestSum = sum;
                    splitIndex = index;

                    if (log.isDebugEnabled()) {
                        log.debug(Util.ntimes("?", 10) + "** FOUND: @" + (index) + " target (" + i1.getAttrValue(this.targetDomain.getFName()) + "-|T|-"
                                             + i2.getAttrValue(this.targetDomain.getFName()) + ")");
                    }
                    bestPoint = new SplitPoint(index - 1, cutPoint);
                    bestPoint.setInformationValue(bestSum);

                    //					if (best_distribution != null)
                    //						best_distribution.clear();
                    bestDistribution = new CondClassDistribution(instancesByAttr);
                }
            }
            i1 = i2;
        }
        if (bestDistribution != null) {
            if (log.isDebugEnabled()) {
                log.debug("bp:" + bestPoint);
            }

            this.splitDomain.addSplitPoint(bestPoint);

            /*
             * TODO : can we put the conditional class distribution to its
             * correct place instead of the split
             */

            if (log.isDebugEnabled()) {
                log.debug("bd:" + bestDistribution.getNumCondClasses());
            }
            double sum1 = findASplit(beginIndex, splitIndex, depth - 1, bestDistribution.getDistributionOf(key0));

            double sum2 = findASplit(splitIndex, size, depth - 1, bestDistribution.getDistributionOf(key1));

            return sum1 + sum2;
        } else {
            return 0.0;
        }

        /*
         * return best_sum; TODO find the correct formula of the sumi's for the
         * return
         */

    }
}
