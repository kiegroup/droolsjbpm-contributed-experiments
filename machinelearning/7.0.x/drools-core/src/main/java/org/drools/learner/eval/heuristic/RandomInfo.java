package org.drools.learner.eval.heuristic;

import java.util.Random;

import org.drools.learner.Domain;
import org.drools.learner.QuantitativeDomain;
import org.drools.learner.eval.Categorizer;
import org.drools.learner.eval.CondClassDistribution;

public class RandomInfo extends Entropy implements Heuristic{

	private static Random info_number = new Random(System.currentTimeMillis()); 
	public RandomInfo() {
		super();
	}


	public double getEval(Domain attr_domain) {
		CondClassDistribution insts_by_attr = super.info_attr(attr_domain);
		double info_gain = super.data_eval - Entropy.calc_info_attr(insts_by_attr);
	
		return info_number.nextDouble(); //info_gain;// /split_info;
	}

	public double getEval_cont(Domain attr_domain) {
		
		double attribute_eval= 0.0d, split_info = 1.0d;
		QuantitativeDomain trialDomain = QuantitativeDomain.createFromDomain(attr_domain);

		Categorizer visitor = new Categorizer(insts_by_target);
		visitor.findSplits(trialDomain);

		domain = trialDomain;
		sorted_instances = visitor.getSortedInstances();
		return info_number.nextDouble();//attribute_eval / split_info;
	}
	

	




}
