package dt.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import dt.DecisionTree;
import dt.memory.Domain;
import dt.memory.Fact;
import dt.memory.FactAttrDistribution;
import dt.memory.FactDistribution;
import dt.memory.FactTargetDistribution;
import dt.tools.Util;

public class Entropy implements InformationMeasure {
	
	public static Domain<?> chooseContAttribute(DecisionTree dt, List<Fact> facts,
			FactDistribution facts_in_class, List<String> attrs) {

		double dt_info = calc_info(facts_in_class);
		double greatestGain = -100000.0;
		String attributeWithGreatestGain = attrs.get(0);
		Domain attrDomain = dt.getDomain(attributeWithGreatestGain);
		Domain bestDomain = null; 
		List<Integer> split_indices = null; 
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
//				System.out.println("entropy.chooseContAttribute(1)*********** num of split for "+
//						attr+": "+ attrDomain.getValues().size()+ " ("+ attrDomain.getValues().get(0)+")");				
				split_indices = new ArrayList<Integer>();
				//System.out.println("entropy.chooseContAttribute(BOK) size "+split_indices.size());
				gain = dt_info - info_contattr(facts, attrDomain, targetDomain, 
												facts_in_class, split_indices, splits);
//				System.out.println("entropy.chooseContAttribute(2)*********** num of split for "+
//						attr+": "+ attrDomain.getValues().size());				
			}
			
			if (gain > greatestGain) {				
				greatestGain = gain;
				attributeWithGreatestGain = attr;
				bestDomain = attrDomain;
				if (!bestDomain.isDiscrete())
					bestDomain.setIndices(split_indices);
			}
		}

		return bestDomain;
	}
	public static double info_contattr(List<Fact> facts,
			Domain splitDomain, Domain<?> targetDomain, 
			FactTargetDistribution facts_in_class, 
			List<Integer> split_indices,
			List<Fact> split_facts) {
	
		String splitAttr = splitDomain.getName();
		List<?> splitValues = splitDomain.getValues();
		String targetAttr = targetDomain.getName();
		List<?> targetValues = targetDomain.getValues();
		if (Util.DEBUG) {
			System.out.println("entropy.info_cont() attributeToSplit? " + splitAttr);
			int f_i=0;
			for(Fact f: facts) {
				System.out.println("entropy.info_cont() SORTING: "+f_i+" attr "+splitAttr+ " "+ f );
				f_i++;
			}
		}

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
		
		
		FactAttrDistribution facts_at_attribute = new FactAttrDistribution(keys, targetDomain);
		facts_at_attribute.setTotal(facts.size());
		facts_at_attribute.setTargetDistForAttr(key1, facts_in_class);
		facts_at_attribute.setSumForAttr(key1, facts.size());
		
		double best_sum = -100000.0;
		Object value_to_split = splitValues.get(0);
		int split_index =1, index = 1;
		Iterator<Fact> f_ite = facts.iterator();
		Fact f1 = f_ite.next();
		Comparator<Fact> targetComp = f1.getDomain(targetAttr).factComparator();
		if (Util.DEBUG)	System.out.println("\nentropy.info_cont() SEARCHING: "+split_index+" attr "+splitAttr+ " "+ f1 );
		while (f_ite.hasNext()) {/* 2. Look for potential cut-points. */

			Fact f2 = f_ite.next();
			if (Util.DEBUG) System.out.print("entropy.info_cont() SEARCHING: "+(index+1)+" attr "+splitAttr+ " "+ f2 );
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
			
			if ( targetComp.compare(f1, f2)!=0) {
				// the cut point
				Number cp_i = (Number) f1.getFieldValue(splitAttr);
				Number cp_i_next = (Number) f2.getFieldValue(splitAttr);

				Number cut_point = (Double)(cp_i.doubleValue() + cp_i_next.doubleValue()) / 2;
				
				/*
				 * 3. Evaluate your favourite disparity measure 
				 * (info gain, gain ratio, gini coefficient, chi-squared test) on the cut point
				 * and calculate its gain 
				 */
				double sum = calc_info_attr(facts_at_attribute);
				//System.out.println("**entropy.info_contattr() FOUND: "+ sum + " best sum "+best_sum + 
				if (Util.DEBUG) System.out.println("  **Try "+ sum + " best sum "+best_sum + 
				" value ("+ f1.getFieldValue(splitAttr) +"-|"+ value_to_split+"|-"+ f2.getFieldValue(splitAttr)+")");
				
				if (sum > best_sum) {
					best_sum = sum;
					value_to_split = cut_point;
					if (Util.DEBUG) System.out.println(Util.ntimes("?", 10)+"** FOUND: target ("+ f1.getFieldValue(targetAttr) +"-|T|-"+ f2.getFieldValue(targetAttr)+")");
					split_index = index;
				}
			} else {}		
			f1 = f2;
			index++;
		}
		splitDomain.addPseudoValue(value_to_split);
		Util.insert(split_indices, Integer.valueOf(split_index));
		if (Util.DEBUG) {
			System.out.println("entropy.info_contattr(BOK_last) split_indices.size "+split_indices.size());
			for(Integer i : split_indices)
				System.out.println("entropy.info_contattr(FOUNDS) split_indices "+i + " the fact "+facts.get(i));
			System.out.println("entropy.chooseContAttribute(1.5)*********** num of split for "+
					splitAttr+": "+ splitDomain.getValues().size());
		}
		return best_sum;
	}
	
	public static double info_contattr_rec(List<Fact> facts,
			Domain splitDomain, Domain<?> targetDomain, 
			FactTargetDistribution facts_in_class, 
			List<Integer> split_indices,
			List<Fact> split_facts) {
	
		String splitAttr = splitDomain.getName();
		List<?> splitValues = splitDomain.getValues();
		String targetAttr = targetDomain.getName();
		List<?> targetValues = targetDomain.getValues();
		if (Util.DEBUG) {
			System.out.println("entropy.info_cont() attributeToSplit? " + splitAttr);
			int f_i=0;
			for(Fact f: facts) {
				System.out.println("entropy.info_cont() SORTING: "+f_i+" attr "+splitAttr+ " "+ f );
				f_i++;
			}
		}

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
		
		
		FactAttrDistribution facts_at_attribute = new FactAttrDistribution(keys, targetDomain);
		facts_at_attribute.setTotal(facts.size());
		facts_at_attribute.setTargetDistForAttr(key1, facts_in_class);
		facts_at_attribute.setSumForAttr(key1, facts.size());
		
		double best_sum = -100000.0;
		Object value_to_split = splitValues.get(0);
		int split_index =1, index = 1;
		FactAttrDistribution best_distribution = null;
		Iterator<Fact> f_ite = facts.iterator();
		Fact f1 = f_ite.next();
		Comparator<Fact> targetComp = f1.getDomain(targetAttr).factComparator();
		if (Util.DEBUG)	System.out.println("\nentropy.info_cont() SEARCHING: "+split_index+" attr "+splitAttr+ " "+ f1 );
		while (f_ite.hasNext()) {/* 2. Look for potential cut-points. */

			Fact f2 = f_ite.next();
			if (Util.DEBUG) System.out.print("entropy.info_cont() SEARCHING: "+(index+1)+" attr "+splitAttr+ " "+ f2 );
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
			
			if ( targetComp.compare(f1, f2)!=0) {
				// the cut point
				Number cp_i = (Number) f1.getFieldValue(splitAttr);
				Number cp_i_next = (Number) f2.getFieldValue(splitAttr);

				Number cut_point = (Double)(cp_i.doubleValue() + cp_i_next.doubleValue()) / 2;
				
				/*
				 * 3. Evaluate your favourite disparity measure 
				 * (info gain, gain ratio, gini coefficient, chi-squared test) on the cut point
				 * and calculate its gain 
				 */
				double sum = calc_info_attr(facts_at_attribute);
				//System.out.println("**entropy.info_contattr() FOUND: "+ sum + " best sum "+best_sum + 
				if (Util.DEBUG) System.out.println("  **Try "+ sum + " best sum "+best_sum + 
				" value ("+ f1.getFieldValue(splitAttr) +"-|"+ value_to_split+"|-"+ f2.getFieldValue(splitAttr)+")");
				
				if (sum > best_sum) {
					best_sum = sum;
					value_to_split = cut_point;
					if (Util.DEBUG) System.out.println(Util.ntimes("?", 10)+"** FOUND: target ("+ f1.getFieldValue(targetAttr) +"-|T|-"+ f2.getFieldValue(targetAttr)+")");
					split_index = index;
					best_distribution = facts_at_attribute.clone();
				}
			} else {}		
			f1 = f2;
			index++;
		}
		splitDomain.addPseudoValue(value_to_split);
		Util.insert(split_indices, Integer.valueOf(split_index));
		/*
		 * info_contattr_rec(List<Fact> facts,
			Domain splitDomain, Domain<?> targetDomain, 
			FactTargetDistribution facts_in_class, 
			List<Integer> split_indices,
			List<Fact> split_facts)
		 */
		info_contattr_rec(facts.subList(0, split_index),
				splitDomain, targetDomain, 
				best_distribution.getAttrFor(key0), 
				split_indices,
				split_facts);
		
		
		if (Util.DEBUG) {
			System.out.println("entropy.info_contattr(BOK_last) split_indices.size "+split_indices.size());
			for(Integer i : split_indices)
				System.out.println("entropy.info_contattr(FOUNDS) split_indices "+i + " the fact "+facts.get(i));
			System.out.println("entropy.chooseContAttribute(1.5)*********** num of split for "+
					splitAttr+": "+ splitDomain.getValues().size());
		}
		return best_sum;
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

	/* 
	 * id3 uses that function because it can not classify continuous attributes
	 */
	public static String chooseAttribute(DecisionTree dt, List<Fact> facts,
			FactDistribution facts_in_class, List<String> attrs) {

		double dt_info = calc_info(facts_in_class);//, facts.size()
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
			if (Util.DEBUG)	System.out.println("Attribute: " + attr + " the gain: " + gain);
			if (gain > greatestGain) {
				greatestGain = gain;
				attributeWithGreatestGain = attr;
			}

			
		}

		return attributeWithGreatestGain;
	}
	
	public static double info_attr(List<Fact> facts, Domain<?> splitDomain, Domain<?> targetDomain) {
		String attributeToSplit = splitDomain.getName();
		List<?> attributeValues = splitDomain.getValues();
		String target = targetDomain.getName();
		//List<?> targetValues = targetDomain.getValues();
		
		if (Util.DEBUG) System.out.println("What is the attributeToSplit? " + attributeToSplit);

		/* initialize the hashtable */
		FactAttrDistribution facts_at_attribute = new FactAttrDistribution(attributeValues, targetDomain);
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
	private static double calc_info_attr( FactAttrDistribution facts_of_attribute) {
		Collection<Object> attributeValues = facts_of_attribute.getAttributes();
		int fact_size = facts_of_attribute.getTotal();
		double sum = 0.0;
		for (Object attr : attributeValues) {
			int total_num_attr = facts_of_attribute.getSumForAttr(attr);
			//double sum_attr = 0.0;
			if (total_num_attr > 0) {
				sum += ((double) total_num_attr / (double) fact_size) * 
					calc_info(facts_of_attribute.getAttrFor(attr));
			}
		}
		return sum;
	}

	/* you can calculate this before */
	/**
	 * it returns the information value of facts entropy that characterizes the
	 * (im)purity of an arbitrary collection of examples
	 * 
	 * @param facts
	 *            list of facts
	 */
	public static double calc_info(FactTargetDistribution facts_in_class) {
		
		int total_num_facts = facts_in_class.getSum();
		Collection<?> targetValues = facts_in_class.getTargetClasses();
		double prob, sum = 0;
		for (Object key : targetValues) {
			int num_in_class = facts_in_class.getVoteFor(key);
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
