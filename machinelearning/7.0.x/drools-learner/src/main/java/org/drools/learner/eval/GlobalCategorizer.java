package org.drools.learner.eval;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.drools.learner.DecisionTree;
import org.drools.learner.Domain;
import org.drools.learner.Instance;
import org.drools.learner.InstanceList;
import org.drools.learner.Memory;
import org.drools.learner.QuantitativeDomain;
import org.drools.learner.tools.FeatureNotSupported;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalCategorizer {

    protected static final transient Logger log = LoggerFactory.getLogger(GlobalCategorizer.class);

    public GlobalCategorizer(Memory mem) {
        InstanceList classInstances = mem.getInstances();

        if (classInstances.getTargets().size() > 1) {
            //throw new FeatureNotSupported("There is more than 1 target candidates");
            if (log.isErrorEnabled()) {
                log.error("There is more than 1 target candidates");
            }

            System.exit(0);
            // TODO put the feature not supported exception || implement it
        }
        build(classInstances);
    }

    public void build(InstanceList workingInstances) {

        Iterator<String> itTarget = workingInstances.getTargets().iterator();
        String           target   = itTarget.next();

        DecisionTree dt = new DecisionTree(workingInstances.getSchema(), target);

        //log.debug("Num of attributes: "+ dt.getAttrDomains().size());

        InstDistribution statsByClass = new InstDistribution(dt.getTargetDomain());
        statsByClass.calculateDistribution(workingInstances.getInstances());
        dt.FACTS_READ += workingInstances.getSize();//

        categorize(statsByClass, dt.getAttrDomains());
        //log.debug("Result tree\n" + dt);
        return;// dt;
    }

    public void categorize(InstDistribution instsByTarget, List<Domain> attrDomains) {

        for (int attrIdx = 0; attrIdx < attrDomains.size(); attrIdx++) {
            Domain attrDomain = attrDomains.get(attrIdx);
            /*
             * No need to clone the domain as soon as no need to change the
             * domain All domains are categorical so i will use them the way
             * they are
             */
            if (attrDomain.isCategorical()) {
                // do nothing
            } else {
                //				the continuous domain

                QuantitativeDomain trialDomain = QuantitativeDomain.createFromDomain(attrDomain);
                Categorizer        visitor     = new Categorizer(instsByTarget);
                visitor.findSplits(trialDomain);

                //				trial domain is modified				
                if (trialDomain.getNumIndices() == 1) {
                } else {
                }

                InformationContainer categorizedEval = new InformationContainer();
                categorizedEval.domain = trialDomain;
                categorizedEval.sortedData = visitor.getSortedInstances();

                HashMap<Object, InstDistribution> filteredStats = null;
                try {
                    filteredStats = instsByTarget.split(categorizedEval);

                    /*
                     * need to create a fake domain from trial domain with
                     * possible number of values =
                     * trialDomain.trialDomain.getNumIndices();
                     */
                    trialDomain.setFName(attrDomain.getFReferenceName() + "_fake"); // TODO how do we know whose fake copy is that domain
                    trialDomain.setCategorical(true);

                    for (Object category : filteredStats.keySet()) {
                        InstDistribution categoryDist = filteredStats.get(category);

                        for (int targetCat = 0; targetCat < instsByTarget.getClassDomain().getCategoryCount(); targetCat++) {
                            Object targetCategory = instsByTarget.getClassDomain().getCategory(targetCat);
                            for (Instance i : categoryDist.getSupportersFor(targetCategory)) {
                                i.setAttr(attrDomain.getFReferenceName() + "_fake", category);
                            }
                        }
                    }
                    attrDomains.add(trialDomain);
                } catch (FeatureNotSupported e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return;
    }
}
