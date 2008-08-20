package org.drools.learner.eval;


import java.util.ArrayList;
import java.util.List;

import org.drools.learner.Domain;
import org.drools.learner.Instance;
import org.drools.learner.QuantitativeDomain;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;
import org.drools.learner.tools.Util;

public class Entropy implements Heuristic{
	
	private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(Entropy.class, SimpleLogger.DEFAULT_LEVEL);
	//public Entropy
	/* 
	 * - chooses the best attribute,
	 * - return the choosen attributes evaluation results (information gain and/or gain ratio)
	 * can process categorical, and quantitative attribute domains
	 * 
	 * used by:
	 * c45Alternator, c45Learner, c45Iterator
	 */
	
	protected static double multiplier = 1.0;
	protected double data_eval; 
	protected InstDistribution insts_by_target;
	protected ArrayList<Instance> sorted_instances;
	protected Domain domain;

	public Entropy() {
		//
	}
	
	public void init(InstDistribution _insts_by_target) {
		insts_by_target = _insts_by_target;
		data_eval = calc_info(insts_by_target);
		sorted_instances = null;
		domain = null;
	}
	
	
	public double getEval(Domain attr_domain) {
		CondClassDistribution insts_by_attr = info_attr(attr_domain);	
		return multiplier *(data_eval - Entropy.calc_info_attr(insts_by_attr));
	}
	
	public double getEval_cont(Domain attr_domain) {
		
		double attribute_eval= 0.0d;
		QuantitativeDomain trialDomain = QuantitativeDomain.createFromDomain(attr_domain);

		Categorizer visitor = new Categorizer(insts_by_target);
		visitor.findSplits(trialDomain);

		//	trial domain is modified				
		if (trialDomain.getNumIndices() > 1) {
			CondClassDistribution insts_by_attr = info_contattr(visitor); //.getSortedInstances(), trialDomain);
			attribute_eval = data_eval - Entropy.calc_info_attr(insts_by_attr);
		}
		domain = trialDomain;
		sorted_instances = visitor.getSortedInstances();
		return multiplier *attribute_eval;
	}
	
	public double getDataEval() {
		return data_eval;
	}
	
	public Domain getDomain() {
		return domain;
	}
	
	public ArrayList<Instance> getSortedInstances() {
		return sorted_instances;
	}

	public double getWorstEval() {
		return multiplier -1000;
	}
	
	public CondClassDistribution info_attr(Domain attr_domain) {
		
		Domain target_domain = insts_by_target.getClassDomain();
		
		//flog.debug("What is the attributeToSplit? " + attr_domain);

		/* initialize the hashtable */
		CondClassDistribution insts_by_attr = new CondClassDistribution(attr_domain, target_domain);
		insts_by_attr.setTotal(insts_by_target.getSum());
		
		//flog.debug("Cond distribution for "+ attr_domain + " \n"+ insts_by_attr);
		
		for (int category = 0; category<target_domain.getCategoryCount(); category++) {
			Object targetCategory = target_domain.getCategory(category); 
			
			for (Instance inst: insts_by_target.getSupportersFor(targetCategory)) {
				Object inst_attr_category = inst.getAttrValue(attr_domain.getFReferenceName());
				
				Object inst_class = inst.getAttrValue(target_domain.getFReferenceName());
				
				if (!targetCategory.equals(inst_class)) {
					if (flog.error() != null)
						flog.error().log("How the fuck they are not the same ? "+ targetCategory + " " + inst_class);
					System.exit(0);
				}
				insts_by_attr.change(inst_attr_category, targetCategory, inst.getWeight()); //+1
				
			}
		}
		
		return insts_by_attr;
	}
	
	
	/* calculates the information of a quantitative domain given the split indexes of instances 
	 * a wrapper for the quantitative domain to be able to calculate the stats
	 * */
	//public static double info_contattr(InstanceList data, Domain targetDomain, QuantitativeDomain splitDomain) {
	public CondClassDistribution info_contattr(Categorizer visitor) {
		
		List<Instance> data = visitor.getSortedInstances();
		QuantitativeDomain splitDomain = visitor.getSplitDomain();
		Domain targetDomain = insts_by_target.getClassDomain();
		String targetAttr = targetDomain.getFReferenceName();
		
		CondClassDistribution instances_by_attr = new CondClassDistribution(splitDomain, targetDomain);
		instances_by_attr.setTotal(data.size());
		
		int index = 0;
		int split_index = 0;
		Object attr_key = splitDomain.getCategory(split_index);
		for (Instance i : data) {
			
			if (index == splitDomain.getSplit(split_index).getIndex()+1 ) {
				attr_key = splitDomain.getCategory(split_index+1);
				split_index++;	
			}
			Object targetKey = i.getAttrValue(targetAttr);
			instances_by_attr.change(attr_key, targetKey, i.getWeight());	//+1
			
			index++;
		}
		
		return instances_by_attr;
//		double sum = calc_info_attr(instances_by_attr);
//		return sum;
		
	}
	
	/*
	 * for both 
	 */
	public static double calc_info_attr( CondClassDistribution instances_by_attr) {
		//Collection<Object> attributeValues = instances_by_attr.getAttributes();
		double data_size = instances_by_attr.getTotal();
		double sum = 0.0;
		if (data_size>0)
			for (int attr_idx=0; attr_idx<instances_by_attr.getNumCondClasses(); attr_idx++) {
				Object attr_category = instances_by_attr.getCondClass(attr_idx);
				double total_num_attr = instances_by_attr.getTotal_AttrCategory(attr_category);

				if (total_num_attr > 0) {
					double prob = total_num_attr / data_size;
					//flog.debug("{("+total_num_attr +"/"+data_size +":"+prob +")* [");
					double info =  calc_info(instances_by_attr.getDistributionOf(attr_category));

					sum += prob * info;
					//flog.debug("]} ");
				}
			}
		//flog.debug("\n == "+sum);
		return sum;
	}
	
	/* you can calculate this before */
	/**
	 * it returns the information value of facts entropy that characterizes the
	 * (im)purity of an arbitrary collection of examples
	 * 
	 * @param quantity_by_class the distribution of the instances by the class attribute (target)
	 */
	public static double calc_info(ClassDistribution quantity_by_class) {
		
		double data_size = quantity_by_class.getSum();
		
		double prob, sum = 0;
		Domain target_domain = quantity_by_class.getClassDomain();
		if (data_size > 0)
		for (int category = 0; category<target_domain.getCategoryCount(); category++) {
			
			Object targetCategory = target_domain.getCategory(category);
			double num_in_class = quantity_by_class.getVoteFor(targetCategory);

			if (num_in_class > 0) {
				prob = num_in_class / data_size;
				/* TODO what if it is a sooo small number ???? */
				//flog.debug("("+num_in_class+ "/"+data_size+":"+prob+")" +"*"+ Util.log2(prob) + " + ");
				sum -=  prob * Util.log2(prob);
			}
		}
		//flog.debug("= " +sum);
		return sum;
	}
}
