package org.drools.learner.eval;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;

import org.drools.learner.Domain;
import org.drools.learner.Instance;
import org.drools.learner.QuantitativeDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* addition to the ClassDistribution 
 * it keeps the instances themselves by their target value in an hashmap */
public class InstDistribution extends ClassDistribution {

    protected static final transient Logger log = LoggerFactory.getLogger(InstDistribution.class);

    private HashMap<Object, List<Instance>> instanceByValue;

    public InstDistribution(Domain targetDomain) {
        super(targetDomain);

        instanceByValue = new HashMap<Object, List<Instance>>(targetDomain.getCategoryCount());
        for (int t = 0; t < targetDomain.getCategoryCount(); t++) {
            Object category = targetDomain.getCategory(t);
            instanceByValue.put(category, new ArrayList<Instance>());
        }
    }

    public InstDistribution(Domain targetDomain, List<Instance> instances) {
        this(targetDomain);

        String tName    = targetDomain.getFReferenceName();
        if (log.isDebugEnabled()) {
            log.debug("tName : {}", tName);
        }
        for (Instance inst : instances) {
            if (log.isDebugEnabled()) {
                log.debug("inst : {}", inst);
            }

            Object targetKey = inst.getAttrValue(tName);
            addSupporter(targetKey, inst); // add inst.getWeight() vote for the target value of the instance : target_key

        }
    }

    public List<Instance> getSupportersFor(Object category) {
        return instanceByValue.get(category);
    }

    private void addSupporter(Object targetCategory, Instance inst) {
        this.instanceByValue.get(targetCategory).add(inst);
        super.change(targetCategory, inst.getWeight());
    }

    private HashMap<Object, InstDistribution> instantiateLists(Domain splitDomain) {
        Domain targetDomain = super.getClassDomain();
        HashMap<Object, InstDistribution> instLists  = new HashMap<Object, InstDistribution>(splitDomain.getCategoryCount());
        for (int c = 0; c < splitDomain.getCategoryCount(); c++) {
            Object category = splitDomain.getCategory(c);
            instLists.put(category, new InstDistribution(targetDomain));
        }
        return instLists;
    }

    /* spliting during the training for C45TreeIterator */
    public HashMap<Object, InstDistribution> split(InformationContainer splitDomainEval) {
        Domain                            splitDomain = splitDomainEval.getDomain();
        HashMap<Object, InstDistribution> instLists   = this.instantiateLists(splitDomain);

        if (splitDomain.isCategorical()) {
            this.splitFromCategorical(splitDomain, instLists);
        } else {
            if (splitDomain instanceof QuantitativeDomain && splitDomainEval.getSortedData() != null) {
                QuantitativeDomain qSplitDomain = (QuantitativeDomain) splitDomain;

                //Collections.sort(facts, choosenDomain.factComparator()); /* hack*/
                this.splitFromQuantitative(splitDomainEval.getSortedData(), qSplitDomain, instLists);
            } else {
                throw new RuntimeException("Can not split a quatitative domain if it's object type is not QuantitativeDomain " + splitDomain);
            }
        }

        return instLists;
    }

    private HashMap<Object, InstDistribution> splitFromCategorical(Domain splitDomain,
                                                                  HashMap<Object, InstDistribution> instLists) {
        Domain targetDomain = super.getClassDomain();
        String attrName     = splitDomain.getFReferenceName();
        for (int category = 0; category < targetDomain.getCategoryCount(); category++) {
            Object targetCategory = targetDomain.getCategory(category);
            //this.calculateDistribution(this.getSupportersFor(targetCategory));

            for (Instance inst : this.getSupportersFor(targetCategory)) {
                Object value = inst.getAttrValue(attrName);
                instLists.get(value).addSupporter(targetCategory, inst);  // add one for vote for the target value : target_key
            }
        }
        return instLists;
    }

    private void splitFromQuantitative(ArrayList<Instance> data, QuantitativeDomain attributeDomain,
                                       HashMap<Object, InstDistribution> instLists) {

        String attributeName = attributeDomain.getFName();
        String targetName    = super.getClassDomain().getFReferenceName();

        //flog.debug("FactProcessor.splitFacts_cont() attr_split "+ attributeName);

        int startPoint = 0;
        for (int index = 0; index < attributeDomain.getNumIndices(); index++) {
            int integerIndex = attributeDomain.getSplit(index).getIndex(); //splits_it.next().intValue();

            Object instAttrCategory = attributeDomain.getSplit(index).getValue(); //splitValues.get(index);
            //System.out.println("FactProcessor.splitFacts_cont() new category: "+ category);

            //flog.debug("FactProcessor.splitFacts_cont() new category: "+ inst_attr_category+
            //		" ("+start_point+","+integer_index+")");

            List<Instance> dataAtCategory = data.subList(startPoint, integerIndex + 1);
            for (Instance inst : dataAtCategory) {

                Object targetCategory = inst.getAttrValue(targetName);

                instLists.get(instAttrCategory).addSupporter(targetCategory, inst); // add one for vote for the target value : target_key
            }
            startPoint = integerIndex + 1;
        }
    }

    public void missClassifiedInstances(HashSet<Instance> missclassification) {
        Object winner = super.getWinnerClass();

        for (int idx = 0; idx < super.targetAttr.getCategoryCount(); idx++) {
            Object looser  = super.targetAttr.getCategory(idx);
            double numSupp = this.getVoteFor(looser);

            if ((numSupp > 0) && !winner.equals(looser)) {

                //				System.out.println(Util.ntimes("DANIEL", 2)+ " one looser ? "+looser + " num of sup="+num_supp);
                //				System.out.println(" the num of supporters = "+ this.getVoteFor(looser));
                //				System.out.println(" but the guys "+ this.getSupportersFor(looser));
                //				System.out.println("How many bok: "+this.getSupportersFor(looser).size());
                missclassification.addAll(getSupportersFor(looser));
            } else {
                //				System.out.println(Util.ntimes("DANIEL", 5)+ "how many times matching?? not a looser "+ looser );
            }
        }
    }

    public CondClassDistribution categoricalGroupBy(Domain attrDomain) {

        Domain targetDomain = getClassDomain();

        //log.debug("What is the attributeToSplit? " + attr_domain);

        /* initialize the hashtable */
        CondClassDistribution instsByAttr = new CondClassDistribution(attrDomain, targetDomain);
        instsByAttr.setTotal(getSum());

        //log.debug("Cond distribution for "+ attr_domain + " \n"+ insts_by_attr);

        for (int category = 0; category < targetDomain.getCategoryCount(); category++) {
            Object targetCategory = targetDomain.getCategory(category);

            for (Instance inst : getSupportersFor(targetCategory)) {
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
     * calculates the calculateInformation of a quantitative domain given the split
     * indexes of instances a wrapper for the quantitative domain to be able to
     * calculate the stats
     */
    //public static double info_contattr(InstanceList data, Domain targetDomain, QuantitativeDomain splitDomain) {
    public CondClassDistribution continuousGroupBy(Categorizer visitor) {

        List<Instance>     data         = visitor.getSortedInstances();
        QuantitativeDomain splitDomain  = visitor.getSplitDomain();
        Domain             targetDomain = getClassDomain();
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
