package dt.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import dt.DecisionTree;
import dt.memory.Domain;
import dt.memory.Fact;
import dt.memory.FactDistribution;
import dt.tools.Util;
import dt.memory.NumericDomain;

public class Entropy implements InformationMeasure {
	
	public static Domain<?> chooseContAttribute(DecisionTree dt, List<Fact> facts,
			Hashtable<Object, Integer> facts_in_class, List<String> attrs) {

		double dt_info = calc_info(facts_in_class, facts.size());
		double greatestGain = -100000.0;
		String attributeWithGreatestGain = attrs.get(0);
		Domain attrDomain = dt.getDomain(attributeWithGreatestGain);
		Domain bestDomain = null; 

		Domain<?> targetDomain = dt.getDomain(dt.getTarget());
		for (String attr : attrs) {
			System.out.println("Which attribute to try: "+ attr);
			double gain = 0;
			if (dt.getDomain(attr).isDiscrete()) {
				attrDomain = dt.getDomain(attr).clone();
				for (Object v: dt.getDomain(attr).getValues())
					attrDomain.addValue(v);
				gain = dt_info - info_attr(facts, attrDomain, targetDomain);
			
			} else {
				/* 1. sort the values */
				Collections.sort(facts, facts.get(0).getDomain(attr).factComparator());
				
				List<Fact> splits = getSplitPoints(facts, dt.getTarget());
				
				attrDomain = dt.getDomain(attr).clone();
				attrDomain.addPseudoValue(facts.get(facts.size()-1).getFieldValue(attr));
				System.out.println("entropy.chooseContAttribute(1)*********** hey the new values to split: "+ attrDomain.getValues().get(0));				
				
				gain = dt_info - info_contattr(facts, attrDomain, targetDomain, 
												facts_in_class, splits);
				System.out.println("entropy.chooseContAttribute(2)*********** hey the new values to split: "+ attrDomain.getValues().size());				
			}
			
			if (gain > greatestGain) {				
				greatestGain = gain;
				attributeWithGreatestGain = attr;
				bestDomain = attrDomain;
			}
		}

		return bestDomain;
	}
	
	/*
	 * GLOBAL DISCRETIZATION a a b a b b b b b (target) 1 2 3 4 5 6 7 8 9 (attr
	 * c) 0 0 0 0 1 1 1 1 1 "<5", ">=5" "true" "false"
	 */
	/*
	 * The algorithm is basically (per attribute):
	 * 
	 * 1. Sort the instances on the attribute of interest
	 * 
	 * 2. Look for potential cut-points. Cut points are points in the sorted
	 * list above where the class labels change. Eg. if I had five instances
	 * with values for the attribute of interest and labels (1.0,A), (1.4,A),
	 * (1.7, A), (2.0,B), (3.0, B), (7.0, A), then there are only two cutpoints
	 * of interest: 1.85 and 5 (mid-way between the points where the classes
	 * change from A to B or vice versa).
	 * 
	 * 3. Evaluate your favourite disparity measure (info gain, gain ratio, gini
	 * coefficient, chi-squared test) on each of the cutpoints, and choose the
	 * one with the maximum value (I think Fayyad and Irani used info gain).
	 * 
	 * 4. Repeat recursively in both subsets (the ones less than and greater
	 * than the cutpoint) until either (a) the subset is pure i.e. only contains
	 * instances of a single class or (b) some stopping criterion is reached. I
	 * can't remember what stopping criteria they used.
	 */
	public static double info_contattr(List<Fact> facts,
			Domain splitDomain, Domain<?> targetDomain, 
			Hashtable<Object, Integer> facts_in_class, 
			List<Fact> split_facts) {
		String splitAttr = splitDomain.getName();
		List<?> splitValues = splitDomain.getValues();
		String targetAttr = targetDomain.getName();
		List<?> targetValues = targetDomain.getValues();
		
		System.out.println("What is the attributeToSplit? " + splitAttr);

		if (facts.size() <= 1) {
			System.out
					.println("The size of the fact list is 0 oups??? exiting....");
			System.exit(0);
		}
		if (split_facts.size() < 1) {
			System.out
					.println("The size of the splits is 0 oups??? exiting....");
			System.exit(0);
		}
		
		/* initialize the distribution */
		Object key0 = Integer.valueOf(0);
		Object key1 = Integer.valueOf(1);
		List<Object> keys = new ArrayList<Object>(2);
		keys.add(key0);
		keys.add(key1);
		
		
		FactDistribution facts_at_attribute = new FactDistribution(keys, targetValues);
		facts_at_attribute.setTotal(facts.size());
		facts_at_attribute.setTargetDistForAttr(key1, facts_in_class);
		facts_at_attribute.setSumForAttr(key1, facts.size());
		
		double best_sum = +100000.0;
		Object value_to_split = splitValues.get(0);
		int split_index, index = 1;

		Iterator<Fact> f_ite = facts.iterator();
		Fact f1 = f_ite.next();
		while (f_ite.hasNext()) {/* 2. Look for potential cut-points. */

			Fact f2 = f_ite.next();
			Object targetKey = f2.getFieldValue(targetAttr);
			
			// System.out.println("My key: "+ targetKey.toString());
			//for (Object attr_key : attr_values)
			
			/* every time it change the place in the distribution */
			facts_at_attribute.change(key0, targetKey, +1);
			facts_at_attribute.change(key1, targetKey, -1);
	
			/*
			 * 2.1 Cut points are points in the sorted list above where the class labels change. 
			 * Eg. if I had five instances with values for the attribute of interest and labels 
			 * (1.0,A), (1.4,A), (1.7, A), (2.0,B), (3.0, B), (7.0, A), then there are only
			 * two cutpoints of interest: 1.85 and 5 (mid-way between the points
			 * where the classes change from A to B or vice versa).
			 */
			if (f1.getFieldValue(targetAttr) != f2.getFieldValue(targetAttr)) {
				// the cut point
				Number cp_i = (Number) f1.getFieldValue(splitAttr);
				Number cp_i_next = (Number) f2.getFieldValue(splitAttr);

				Number cut_point = (cp_i.doubleValue() + cp_i_next.doubleValue()) / 2;
				
				/*
				 * 3. Evaluate your favourite disparity measure 
				 * (info gain, gain ratio, gini coefficient, chi-squared test) on the cut point
				 * and calculate its gain 
				 */
				double sum = calc_info_attr(facts_at_attribute);
				
				if (sum < best_sum) {
					best_sum = sum;
					value_to_split = cut_point;
					System.out.println("Entropy.info_contattr() hacking: "+ sum + " best sum "+best_sum + 
							" new split value "+ value_to_split);
					split_index = index;
				}
			} else {}		
			f1 = f2;
			index++;
		}
		splitDomain.addValue(value_to_split);
		
		System.out.println("*********** hey the new values to split: "+ splitValues.size());
		return best_sum;
	}
	
	
	/* 
	 * id3 uses that function because it can not classify continuous attributes
	 */
	public static String chooseAttribute(DecisionTree dt, List<Fact> facts,
			Hashtable<Object, Integer> facts_in_class, List<String> attrs) {

		double dt_info = calc_info(facts_in_class, facts.size());
		double greatestGain = -1000;
		String attributeWithGreatestGain = attrs.get(0);
		String target = dt.getTarget();
		Domain<?> targetDomain = dt.getDomain(target);
		for (String attr : attrs) {
			double gain = 0;
			if (!dt.getDomain(attr).isDiscrete()) {
				System.err.println("Ignoring the attribute:" +attr+ " the id3 can not classify continuous attributes");
				continue;
			} else {
				gain = dt_info - info_attr(facts, dt.getDomain(attr), targetDomain);
			}
			System.out.println("Attribute: " + attr + " the gain: " + gain);
			if (gain > greatestGain) {
				greatestGain = gain;
				attributeWithGreatestGain = attr;
			}

			
		}

		return attributeWithGreatestGain;
	}
	
	public static double info_attr(List<Fact> facts, 
			 Domain<?> splitDomain, Domain<?> targetDomain) {
		String attributeToSplit = splitDomain.getName();
		List<?> attributeValues = splitDomain.getValues();
		String target = targetDomain.getName();
		List<?> targetValues = targetDomain.getValues();
		
		System.out.println("What is the attributeToSplit? " + attributeToSplit);

		/* initialize the hashtable */
		FactDistribution facts_at_attribute = new FactDistribution(attributeValues, targetValues);
		facts_at_attribute.setTotal(facts.size());
		
		for (Fact f : facts) {
			Object targetKey = f.getFieldValue(target);
			// System.out.println("My key: "+ targetKey.toString());

			Object attr_key = f.getFieldValue(attributeToSplit);
			facts_at_attribute.change(attr_key, targetKey, +1);

			// System.out.println("getGain of "+attributeToSplit+
			// ": total_num "+ facts_of_attribute.get(attr_key).get(attr_sum) +
			// " and "+facts_of_attribute.get(attr_key).get(targetKey) +
			// " at attr=" + attr_key + " of t:"+targetKey);
		}
		double sum = calc_info_attr(facts_at_attribute);
		return sum;
	}
	
	/*
	 * for both 
	 */
	private static double calc_info_attr( FactDistribution facts_of_attribute) {
		Collection<Object> attributeValues = facts_of_attribute.getAttributes();
		int fact_size = facts_of_attribute.getTotal();
		double sum = 0.0;
		for (Object attr : attributeValues) {
			int total_num_attr = facts_of_attribute.getSumForAttr(attr);
			//double sum_attr = 0.0;
			if (total_num_attr > 0) {
				sum += ((double) total_num_attr / (double) fact_size) * 
					calc_info(facts_of_attribute.getAttrFor(attr), total_num_attr);
			}
		}
		return sum;
	}

	/*
	 * 
	 */
	public static double calc_info(Hashtable<Object, Integer> facts_in_class,
			int total_num_facts) {
		Collection<Object> targetValues = facts_in_class.keySet();
		double prob, sum = 0;
		for (Object key : targetValues) {
			int num_in_class = facts_in_class.get(key).intValue();
			// System.out.println("num_in_class : "+ num_in_class + " key "+ key+ " and the total num "+ total_num_facts);
			
			if (num_in_class > 0) {
				prob = (double) num_in_class / (double) total_num_facts;
				/* TODO what if it is a sooo small number ???? */
				sum +=  -1 * prob * Util.log2(prob);
			// System.out.println("prob "+ prob +" and the plog(p)"+plog2p+"where the sum: "+sum);
			}
		}
		return sum;
	}
	
	private static List<Fact> getSplitPoints(List<Fact> facts, String target) {
		List<Fact> splits = new ArrayList<Fact>();
		Iterator<Fact> it_f = facts.iterator();
		Fact f1 = it_f.next();
		int index = 0;
		while(it_f.hasNext()){
			Fact f2 = it_f.next();
			if (f1.getFieldValue(target) != f2.getFieldValue(target))
				splits.add(f2);
			
			f1= f2;
			index++; 
		}
		return splits;
	}

}
