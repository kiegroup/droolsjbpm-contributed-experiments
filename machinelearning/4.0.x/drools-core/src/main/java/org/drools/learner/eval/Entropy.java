package org.drools.learner.eval;


import java.util.ArrayList;
import java.util.List;

import org.drools.learner.Domain;
import org.drools.learner.Instance;
import org.drools.learner.QuantitativeDomain;
import org.drools.learner.tools.Util;

public class Entropy {
	
	//public Entropy
	/* 
	 * - chooses the best attribute,
	 * - return the choosen attributes evaluation results (information gain and/or gain ratio)
	 * can process categorical, and quantitative attribute domains
	 * 
	 * used by:
	 * c45Alternator, c45Learner, c45Iterator
	 */	
	public static Domain chooseAttribute(InformationContainer eval, InstDistribution insts_by_target, List<Domain> attr_domains) {
									// List<Instance> instances

		double dt_info = calc_info(insts_by_target);
		
		double greatestGain = -1000;
		ArrayList<Instance> sorted_instances = null, best_sorted_instances = null;
		Domain best_attr = attr_domains.get(0);
		for (Domain attr_domain : attr_domains) {
			/* No need to clone the domain as soon as no need to change the domain
			 * All domains are categorical so i will use them the way they are
			 */
			double gain = 0.0;
			if (attr_domain.isCategorical()) {
				gain = dt_info - info_attr(insts_by_target, attr_domain);
			} else {
				// the continuous domain
				
				QuantitativeDomain trialDomain = QuantitativeDomain.createFromDomain(attr_domain);
				
				Categorizer visitor = new Categorizer(insts_by_target);
				visitor.findSplits(trialDomain);
				
				// trial domain modified				
				if (trialDomain.getNumIndices()==1) {
					gain = 0.0;
				} else {
					gain = dt_info - info_contattr(visitor.getInstances(), insts_by_target.getClassDomain(), trialDomain);
				}
				attr_domain = trialDomain;
				sorted_instances = visitor.getInstances();
				
				/*
				if (Util.DEBUG)	{
					int index = 0;
					for (Integer i: split_indices) {
						System.out.print("Split indices:"+ i);
						System.out.print(" domain "+attrDomain.getValues().get(index));
						System.out.print(","+attrDomain.getIndices().get(index));
						System.out.println(" fact "+visitor.getSortedFact(i));
						index++;
					}
				}			
				/* */
			}
			if (Util.DEBUG_ENTROPY)	System.out.println("Attribute: " + attr_domain + " the gain: " + gain);
			if (gain > greatestGain) {
				greatestGain = gain;
				best_attr = attr_domain;
				best_sorted_instances = sorted_instances;
			}
		}
		
		// Clone the best attribute domain cause it is going to be the domain of the treenode
		eval.domain =  best_attr.cheapClone();
		eval.sorted_data = best_sorted_instances;
		eval.gain = greatestGain;
		//eval.gain_ratio = greatestGain/greatestSplitGain;
		return eval.domain;
	}
	
//	/* 
//	 * to choose the best attribute
//	 * can process categorical, and quantitative attribute domains
//	 * used by c45Learner, c45Iterator
//	 */
//	public static Domain chooseAttribute(InstDistribution insts_by_target, List<Domain> attr_domains) {		
//		InformationContainer evals = new InformationContainer();
//		chooseAttribute(evals, insts_by_target, attr_domains);
//		return evals.domain;
//	}

	/* 
	 * to choose the best attribute
	 * can process only the categorical attribute domains
	 * used by id3Learner
	 */
	public static Domain chooseAttributeAsCategorical(InstDistribution insts_by_target, List<Domain> attr_domains) {

		double dt_info = calc_info(insts_by_target);
		double greatestGain = -1000;
		Domain best_attr = attr_domains.get(0);
		for (Domain attr_domain : attr_domains) {
			/* No need to clone the domain as soon as no need to change the domain
			 * All domains are categorical so i will use them the way they are
			 */
			double gain = dt_info - info_attr(insts_by_target, attr_domain);
			//if (Util.DEBUG)	System.out.println("Attribute: " + attr + " the gain: " + gain);
			if (gain > greatestGain) {
				greatestGain = gain;
				best_attr = attr_domain;
			}
		}
		// Clone the best attribute domain cause it is going to be the domain of the treenode
		return best_attr.cheapClone();
	}
	
	
	public static double info_attr(InstDistribution insts_by_target, Domain attr_domain) {
		
		Domain target_domain = insts_by_target.getClassDomain();
		
		if (Util.DEBUG_ENTROPY) {
			System.out.println("What is the attributeToSplit? " + attr_domain);
		}

		/* initialize the hashtable */
		CondClassDistribution insts_by_attr = new CondClassDistribution(attr_domain, target_domain);
		insts_by_attr.setTotal(insts_by_target.getSum());
		
		if (Util.DEBUG_ENTROPY) {
			System.out.println("Cond distribution for "+ attr_domain + " \n"+ insts_by_attr);
		}
		
		for (int category = 0; category<target_domain.getCategoryCount(); category++) {
			Object targetCategory = target_domain.getCategory(category); 
			
			for (Instance inst: insts_by_target.getSupportersFor(targetCategory)) {
				Object inst_attr_category = inst.getAttrValue(attr_domain.getFName());
				
				Object inst_class = inst.getAttrValue(target_domain.getFName());
				
				if (!targetCategory.equals(inst_class)) {
					System.out.println("How the fuck they are not the same ? "+ targetCategory + " " + inst_class);
					System.exit(0);
				}
				insts_by_attr.change(inst_attr_category, targetCategory, +1);
				
			}
		}
		double sum = calc_info_attr(insts_by_attr);
		return sum;
	}
	
	
	/* calculates the information of a quantitative domain given the split indexes of instances 
	 * a wrapper for the quantitative domain to be able to calculate the stats
	 * */
	public static double info_contattr(List<Instance> data, Domain targetDomain, QuantitativeDomain splitDomain) {

		String targetAttr = targetDomain.getFName();
		
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
			instances_by_attr.change(attr_key, targetKey, +1);

			
			index++;
		}
		double sum = calc_info_attr(instances_by_attr);
		return sum;
		
	}
	
	/*
	 * for both 
	 */
	public static double calc_info_attr( CondClassDistribution instances_by_attr) {
		//Collection<Object> attributeValues = instances_by_attr.getAttributes();
		int data_size = instances_by_attr.getTotal();
		double sum = 0.0;
		if (data_size>0)
			for (int attr_idx=0; attr_idx<instances_by_attr.getNumCondClasses(); attr_idx++) {
				Object attr_category = instances_by_attr.getCondClass(attr_idx);
				int total_num_attr = instances_by_attr.getTotal_AttrCategory(attr_category);

				if (total_num_attr > 0) {
					double prob = (double) total_num_attr / (double) data_size;
					if (Util.DEBUG_ENTROPY)	{
						System.out.print("{("+total_num_attr +"/"+data_size +":"+prob +")* [");
					}
					double info =  calc_info(instances_by_attr.getDistributionOf(attr_category));

					sum += prob * info;
					if (Util.DEBUG_ENTROPY)	{
						System.out.print("]} ");
					}
				}
			}
		if (Util.DEBUG_ENTROPY)	{
			System.out.println("\n == "+sum);
		}
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
		
		int data_size = quantity_by_class.getSum();
		
		double prob, sum = 0;
		String out =" ";
		Domain target_domain = quantity_by_class.getClassDomain();
		for (int category = 0; category<target_domain.getCategoryCount(); category++) {
			
			Object targetCategory = target_domain.getCategory(category);
			int num_in_class = quantity_by_class.getVoteFor(targetCategory);

			if (num_in_class > 0) {
				prob = (double) num_in_class / (double) data_size;
				/* TODO what if it is a sooo small number ???? */
				if (Util.DEBUG_ENTROPY)	{
					out += "("+num_in_class+ "/"+data_size+":"+prob+")" +"*"+ Util.log2(prob) + " + ";
				}
				sum -=  prob * Util.log2(prob);
			}
		}
		if (Util.DEBUG_ENTROPY)	{
			System.out.print(out +"= " +sum);
		}
		return sum;
	}


}
