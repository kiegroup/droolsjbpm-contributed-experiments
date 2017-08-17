package org.drools.learner.eval;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;

import org.drools.learner.Domain;
import org.drools.learner.Instance;
import org.drools.learner.QuantitativeDomain;
import org.drools.learner.tools.FeatureNotSupported;
import org.drools.learner.tools.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* addition to the ClassDistribution 
 * it keeps the instances themselves by their target value in an hashmap */
public class InstDistribution extends ClassDistribution {

    protected static final transient Logger log = LoggerFactory.getLogger(InstDistribution.class);

    private String attSum = Util.sum();
    private HashMap<Object, List<Instance>> instanceByValue;

    //private HashMap<Object, List<Instance>>

    public InstDistribution(Domain targetDomain) {
        super(targetDomain);

        instanceByValue = new HashMap<Object, List<Instance>>(targetDomain.getCategoryCount());
        for (int t = 0; t < targetDomain.getCategoryCount(); t++) {
            // mireynol - FWIW this variable was formerly named obj_t, just guessing at the meaning FIXME
            Object objType = targetDomain.getCategory(t);
            instanceByValue.put(objType, new ArrayList<Instance>());
        }
    }

    public void calculateDistribution(List<Instance> instances) {
        double dataSize = 0.0;
        String tName    = super.getClassDomain().getFReferenceName();
        if (log.isDebugEnabled()) {
            log.debug("tName : " + tName + "\n");
        }
        for (Instance inst : instances) {
            if (log.isDebugEnabled()) {
                log.debug("inst : " + inst + "\n");
            }
            dataSize += inst.getWeight();

            Object targetKey = inst.getAttrValue(tName);
            super.change(targetKey, inst.getWeight()); // add inst.getWeight() vote for the target value of the instance : target_key
            //super.change(attr_sum, inst.getWeight());		// ?????

            this.addSupporter(targetKey, inst);
        }
        //super.change(attr_sum, data_size);	// TODO should i write special function for changing the sum 
        super.setSum(dataSize);
    }

    public List<Instance> getSupportersFor(Object category) {
        return instanceByValue.get(category);
    }

    private void addSupporter(Object targetCategory, Instance inst) {
        this.instanceByValue.get(targetCategory).add(inst);
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
    public HashMap<Object, InstDistribution> split(InformationContainer splitDomainEval) throws FeatureNotSupported {
        Domain                              splitDomain = splitDomainEval.domain;
        HashMap<Object, InstDistribution> instLists   = this.instantiateLists(splitDomain);

        if (splitDomain.isCategorical()) {
            this.splitFromCategorical(splitDomain, instLists);
        } else {
            if (splitDomain instanceof QuantitativeDomain && splitDomainEval.sortedData != null) {
                QuantitativeDomain qSplitDomain = (QuantitativeDomain) splitDomain;

                //Collections.sort(facts, choosenDomain.factComparator()); /* hack*/
                this.splitFromQuantitative(splitDomainEval.sortedData, qSplitDomain, instLists);
            } else {
                throw new FeatureNotSupported("Can not split a quatitative domain if it's object type is not QuantitativeDomain " + splitDomain);
            }
        }

        return instLists;
    }

    public HashMap<Object, InstDistribution> splitFromCategorical(Domain splitDomain,
                                                                    HashMap<Object, InstDistribution> instLists) {
        if (instLists == null) {
            instLists = this.instantiateLists(splitDomain);
        }

        Domain targetDomain = super.getClassDomain();
        String attrName     = splitDomain.getFReferenceName();
        for (int category = 0; category < targetDomain.getCategoryCount(); category++) {
            Object targetCategory = targetDomain.getCategory(category);
            //this.calculateDistribution(this.getSupportersFor(targetCategory));

            for (Instance inst : this.getSupportersFor(targetCategory)) {
                Object value = inst.getAttrValue(attrName);

                instLists.get(value).change(targetCategory, inst.getWeight()); // add one for vote for the target value : target_key
                instLists.get(value).change(attSum, inst.getWeight());
                instLists.get(value).addSupporter(targetCategory, inst);
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

            try {

                //flog.debug("FactProcessor.splitFacts_cont() new category: "+ inst_attr_category+
                //		" ("+start_point+","+integer_index+")");

                List<Instance> dataAtCategory = data.subList(startPoint, integerIndex + 1);
                for (Instance inst : dataAtCategory) {

                    Object targetCategory = inst.getAttrValue(targetName);

                    instLists.get(instAttrCategory).change(targetCategory, inst.getWeight()); // add one for vote for the target value : target_key
                    instLists.get(instAttrCategory).change(attSum, inst.getWeight());
                    instLists.get(instAttrCategory).addSupporter(targetCategory, inst);
                }
                startPoint = integerIndex + 1;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
}
