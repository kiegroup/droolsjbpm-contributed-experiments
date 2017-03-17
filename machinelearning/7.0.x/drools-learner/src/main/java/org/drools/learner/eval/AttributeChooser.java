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
	
	public AttributeChooser(Heuristic _function) {
		function = _function;
	}

	public Domain chooseAttribute(InformationContainer eval, InstDistribution insts_by_target, List<Domain> attr_domains) {
		// List<Instance> instances
		
//		double data_eval = function.calc_info(insts_by_target);
		function.init(insts_by_target);

		double greatestEval = function.getWorstEval();//-1000;
//		ArrayList<Instance> sorted_instances = null, best_sorted_instances = null;
//		Domain best_attr = attr_domains.get(0);
		
		InformationContainer container = new InformationContainer(); 
		InformationContainer best = new InformationContainer(); 
		for (Domain attr_domain : attr_domains) {
			/* No need to clone the domain as soon as no need to change the domain
			 * All domains are categorical so i will use them the way they are
			 */
			double attribute_eval = 0.0;
			if (attr_domain.isCategorical()) {
				attribute_eval = function.getEval(attr_domain);//data_eval - function.info_attr(insts_by_target, attr_domain);
				container.attribute_eval = attribute_eval;
				container.domain = attr_domain;
				if (slog.debug() != null)
					slog.debug().log("CatAttribute: " + container.domain + " the gain: " + attribute_eval + " greatest "+ greatestEval+ "\n");
			
				
			} else {
//				the continuous domain
				attribute_eval = function.getEval_cont(attr_domain);
				container.attribute_eval = attribute_eval;
				container.domain = function.getDomain();
				container.sorted_data = function.getSortedInstances();
				if (slog.debug() != null)
					slog.debug().log("ContAttribute: " + container.domain + " the gain: " + attribute_eval + " greatest "+ greatestEval+ "\n");
			
//				attr_domain = function.getDomain();
//				sorted_instances = visitor.getSortedInstances();
				
				
				
			}
			if (slog.warn() != null)
				slog.warn().log("Attribute: " + container.domain + " the gain: " + attribute_eval + " greatest "+ greatestEval+ "\n");
			if (attribute_eval > greatestEval) {// TODO implement a comparator
				greatestEval = attribute_eval;
				best.domain = container.domain;
				best.sorted_data = container.sorted_data;
			}
		}

//		Clone the best attribute domain cause it is going to be the domain of the treenode
		eval.domain =  best.domain.cheapClone();
		eval.sorted_data = best.sorted_data;
		eval.attribute_eval = greatestEval;
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
	 * to choose the best attribute
	 * can process only the categorical attribute domains
	 * used by id3Learner
	 */
	public Domain chooseAttributeAsCategorical(InstDistribution insts_by_target, List<Domain> attr_domains) {

		//double dt_info = function.calc_info(insts_by_target);
		function.init(insts_by_target);
		double greatestEval = function.getWorstEval(); //-1000;
		Domain best_attr = attr_domains.get(0);
		for (Domain attr_domain : attr_domains) {
			/* No need to clone the domain as soon as no need to change the domain
			 * All domains are categorical so i will use them the way they are
			 */
			//double attribute_eval = dt_info - function.info_attr(insts_by_target, attr_domain);
			double attribute_eval = function.getEval(attr_domain);
//			flog.debug("Attribute: " + attr_domain.getFName() + " the gain: " + gain);
			if (attribute_eval > greatestEval) {
				greatestEval = attribute_eval;
				best_attr = attr_domain;
			}
		}
//		Clone the best attribute domain cause it is going to be the domain of the treenode
		return best_attr.cheapClone();
	}

}
