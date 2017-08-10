package org.drools.learner.eval;

import java.util.List;

import org.drools.learner.Domain;
import org.drools.learner.eval.heuristic.Heuristic;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;

public class AttributeChooser {

    private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(AttributeChooser.class, SimpleLogger.DEFAULT_LEVEL);
    private static SimpleLogger slog = LoggerFactory.getSysOutLogger(AttributeChooser.class, SimpleLogger.DEFAULT_LEVEL);

    private Heuristic function;

    public AttributeChooser(Heuristic function) {
        this.function = function;
    }

    public Domain chooseAttribute(InformationContainer eval, InstDistribution instsByTarget, List<Domain> attrDomains) {
        // List<Instance> instances

        //		double data_eval = function.calc_info(insts_by_target);
        function.init(instsByTarget);

        double greatestEval = function.getWorstEval();//-1000;
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
                attributeEval = function.getEval(attrDomain);//data_eval - function.info_attr(insts_by_target, attr_domain);
                container.attributeEval = attributeEval;
                container.domain = attrDomain;
                if (slog.debug() != null) {
                    slog.debug().log("CatAttribute: " + container.domain + " the gain: " + attributeEval + " greatest " + greatestEval + "\n");
                }
            } else {
                //				the continuous domain
                attributeEval = function.getEvalCont(attrDomain);
                container.attributeEval = attributeEval;
                container.domain = function.getDomain();
                container.sortedData = function.getSortedInstances();
                if (slog.debug() != null) {
                    slog.debug().log("ContAttribute: " + container.domain + " the gain: " + attributeEval + " greatest " + greatestEval + "\n");
                }

                //				attr_domain = function.getDomain();
                //				sorted_instances = visitor.getSortedInstances();

            }
            if (slog.warn() != null) {
                slog.warn().log("Attribute: " + container.domain + " the gain: " + attributeEval + " greatest " + greatestEval + "\n");
            }
            if (attributeEval > greatestEval) {// TODO implement a comparator
                greatestEval = attributeEval;
                best.domain = container.domain;
                best.sortedData = container.sortedData;
            }
        }

        //		Clone the best attribute domain cause it is going to be the domain of the treenode
        eval.domain = best.domain.cheapClone();
        eval.sortedData = best.sortedData;
        eval.attributeEval = greatestEval;
        return eval.domain;
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

        //double dt_info = function.calc_info(insts_by_target);
        function.init(instsByTarget);
        double greatestEval = function.getWorstEval(); //-1000;
        Domain bestAttr     = attrDomains.get(0);
        for (Domain attrDomain : attrDomains) {
            /*
             * No need to clone the domain as soon as no need to change the
             * domain All domains are categorical so i will use them the way
             * they are
             */
            //double attribute_eval = dt_info - function.info_attr(insts_by_target, attr_domain);
            double attributeEval = function.getEval(attrDomain);
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
