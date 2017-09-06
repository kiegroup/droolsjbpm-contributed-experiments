package org.drools.learner.eval;

import java.util.List;

import org.drools.learner.Domain;
import org.drools.learner.eval.heuristic.Heuristic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttributeChooser {
    protected static final transient Logger   log = LoggerFactory.getLogger(AttributeChooser.class);

    private Heuristic heuristic;

    public AttributeChooser(Heuristic heuristic) {
        this.heuristic = heuristic;
    }

    public void chooseAttribute(InformationContainer eval, InstDistribution instsByTarget, List<Domain> attrDomains) {
        // List<Instance> instances

        //		double data_eval = heuristic.calc_info(insts_by_target);
        heuristic.init(instsByTarget);

        double greatestEval = heuristic.getWorstEval();//-1000;
        //		ArrayList<Instance> sorted_instances = null, best_sorted_instances = null;
        //		Domain best_attr = attr_domains.get(0);

        InformationContainer container = new InformationContainer();
        InformationContainer best      = new InformationContainer();
        for (Domain attrDomain : attrDomains) {
            /*
             * No need to clone the domain as soon as no need to change the
             * domain All domains are categorical so i will use them the way
             * they are
             */
            double attributeEval = 0.0;
            if (attrDomain.isCategorical()) {
                attributeEval = heuristic.evalCategorical(attrDomain);//data_eval - heuristic.info_attr(insts_by_target, attr_domain);
                container.setAttributeEval(attributeEval);
                container.setDomain(attrDomain);
                if (log.isDebugEnabled()) {
                    log.debug("CatAttribute: " + container.getDomain() + " the gain: " + attributeEval + " greatest " + greatestEval + "\n");
                }
            } else {
                //				the continuous domain
                attributeEval = heuristic.evalContinuous(attrDomain);
                container.setAttributeEval(attributeEval);
                container.setDomain(heuristic.getDomain());
                container.setSortedData(heuristic.getSortedInstances());
                if (log.isDebugEnabled()) {
                    log.debug("ContAttribute: " + container.getDomain() + " the gain: " + attributeEval + " greatest " + greatestEval + "\n");
                }

                //				attr_domain = heuristic.getDomain();
                //				sorted_instances = visitor.getSortedInstances();

            }
            if (log.isWarnEnabled()) {
                log.warn("Attribute: " + container.getDomain() + " the gain: " + attributeEval + " greatest " + greatestEval + "\n");
            }
            if (attributeEval > greatestEval) {// TODO implement a comparator
                greatestEval = attributeEval;
                best.setDomain(container.getDomain());
                best.setSortedData(container.getSortedData());
            }
        }

        //		Clone the best attribute domain cause it is going to be the domain of the treenode
        eval.setDomain(best.getDomain().cheapClone());
        eval.setSortedData(best.getSortedData());
        eval.setAttributeEval(greatestEval);
    }

    //	/* 
    //	* to choose the best attribute
    //	* can process categorical, and quantitative attribute domains
    //	* used by c45Learner, c45Iterator
    //	*/
    //	public static Domain chooseAttribute(InstDistribution insts_by_target, List<Domain> attr_domains) {		
    //	InformationContainer evals = new InformationContainer();
    //	chooseAttribute(evals, insts_by_target, attr_domains);
    //	return evals.domain;
    //	}

    /*
     * to choose the best attribute can process only the categorical attribute
     * domains used by id3Learner
     */
    public Domain chooseAttributeAsCategorical(InstDistribution instsByTarget, List<Domain> attrDomains) {

        //double dt_info = heuristic.calc_info(insts_by_target);
        heuristic.init(instsByTarget);
        double greatestEval = heuristic.getWorstEval(); //-1000;
        Domain bestAttr     = attrDomains.get(0);
        for (Domain attrDomain : attrDomains) {
            /*
             * No need to clone the domain as soon as no need to change the
             * domain All domains are categorical so i will use them the way
             * they are
             */
            //double attribute_eval = dt_info - heuristic.info_attr(insts_by_target, attr_domain);
            double attributeEval = heuristic.evalCategorical(attrDomain);
            //			flog.debug("Attribute: " + attr_domain.getFName() + " the gain: " + gain);
            if (attributeEval > greatestEval) {
                greatestEval = attributeEval;
                bestAttr = attrDomain;
            }
        }
        //		Clone the best attribute domain cause it is going to be the domain of the treenode
        return bestAttr.cheapClone();
    }
}
