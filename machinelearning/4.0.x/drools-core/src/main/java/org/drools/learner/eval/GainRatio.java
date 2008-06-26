package org.drools.learner.eval;

import org.drools.learner.Domain;
import org.drools.learner.QuantitativeDomain;
import org.drools.learner.tools.Util;

public class GainRatio extends Entropy implements Heuristic{

	public GainRatio() {
		super();
	}


	public double getEval(Domain attr_domain) {
		CondClassDistribution insts_by_attr = super.info_attr(attr_domain);
		double info_gain = super.data_eval - Entropy.calc_info_attr(insts_by_attr);
	
		double split_info = GainRatio.split_info(insts_by_attr);
		
		System.err.println("(GainRatio) info_gain = "+ info_gain + "/"+ split_info);
		return info_gain /split_info;
	}

	public double getEval_cont(Domain attr_domain) {
		
		double attribute_eval= 0.0d, split_info = 1.0d;
		QuantitativeDomain trialDomain = QuantitativeDomain.createFromDomain(attr_domain);

		Categorizer visitor = new Categorizer(insts_by_target);
		visitor.findSplits(trialDomain);

		// trial domain is modified				
		if (trialDomain.getNumIndices() > 1) {
			CondClassDistribution insts_by_attr = super.info_contattr(visitor);
			attribute_eval = super.data_eval - Entropy.calc_info_attr(insts_by_attr);
			
			split_info = GainRatio.split_info(insts_by_attr);
		}
		domain = trialDomain;
		sorted_instances = visitor.getSortedInstances();
		return attribute_eval / split_info;
	}
	
	private static double split_info( CondClassDistribution instances_by_attr) {
		//Collection<Object> attributeValues = instances_by_attr.getAttributes();
		double data_size = instances_by_attr.getTotal();
		double sum = 1.0;
		if (data_size>0) {
			for (int attr_idx = 0; attr_idx < instances_by_attr.getNumCondClasses(); attr_idx++) {
				Object attr_category = instances_by_attr.getCondClass(attr_idx);
				double num_in_attr = instances_by_attr.getTotal_AttrCategory(attr_category);

				if (num_in_attr > 0.0) {
					double prob = num_in_attr / data_size;
					sum -= prob * Util.log2(prob);
				}
			}
		} else {
			System.err.println("????? data_size = "+ data_size);
			System.exit(0);
		}
			
		//flog.debug("\n == "+sum);
		return sum;
	}
	




}
