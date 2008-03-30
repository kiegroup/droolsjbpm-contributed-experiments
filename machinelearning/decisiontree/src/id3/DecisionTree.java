package id3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DecisionTree {

	public long FACTS_READ = 0;

	/* set of the attributes, their types */
	private Hashtable<String, Domain<?>> domainSet;

	/* the class of the objects */
	private String className;

	/* the target attribute */
	private String target;

	private TreeNode root;

	/* all attributes that can be used during classification */
	private ArrayList<String> attrsToClassify;

	DecisionTree(String klass) {
		this.className = klass;
		this.domainSet = new Hashtable<String, Domain<?>>();
		this.attrsToClassify = new ArrayList<String>();
	}

	private Object getConsensus(List<Fact> facts) {
		List<?> targetValues = getPossibleValues(this.target);
		Hashtable<Object, Integer> facts_in_class = getStatistics(facts, target);
		// , targetValues

		int winner_vote = 0;
		Object winner = null;
		for (Object key : targetValues) {

			int num_in_class = facts_in_class.get(key).intValue();
			if (num_in_class > winner_vote) {
				winner_vote = num_in_class;
				winner = key;
			}
		}
		return winner;
	}

	// *OPT* public double calculateGain(List<FactSet> facts, String
	// attributeName) {
	// I dont use
	public double calculateGain(List<Fact> facts,
			Hashtable<Object, Integer> facts_in_class, String attributeName) {

		return getInformation(facts_in_class, facts.size())
				- getGain(facts, attributeName);
	}

	// *OPT* public double getGain(List<FactSet> facts, String attributeToSplit)
	// {
	public double getGain(List<Fact> facts, String attributeToSplit) {
		System.out.println("What is the attributeToSplit? " + attributeToSplit);
		List<?> attributeValues = getPossibleValues(attributeToSplit);

		String attr_sum = "sum";

		List<?> targetValues = getPossibleValues(getTarget());
		// Hashtable<Object, Integer> facts_in_class = new Hashtable<Object,
		// Integer>(targetValues.size());

		/* initialize the hashtable */
		Hashtable<Object, Hashtable<Object, Integer>> facts_of_attribute = new Hashtable<Object, Hashtable<Object, Integer>>(
				attributeValues.size());
		for (Object attr : attributeValues) {
			facts_of_attribute.put(attr, new Hashtable<Object, Integer>(
					targetValues.size() + 1));
			for (Object t : targetValues) {
				facts_of_attribute.get(attr).put(t, 0);
			}
			facts_of_attribute.get(attr).put(attr_sum, 0);
		}

		int total_num_facts = 0;
		// *OPT* for (FactSet fs: facts) {
		// *OPT* for (Fact f: fs.getFacts()) {
		for (Fact f : facts) {
			total_num_facts++;
			Object targetKey = f.getFieldValue(target);
			// System.out.println("My key: "+ targetKey.toString());

			Object attr_key = f.getFieldValue(attributeToSplit);
			int num = facts_of_attribute.get(attr_key).get(targetKey)
					.intValue();
			num++;
			facts_of_attribute.get(attr_key).put(targetKey, num);

			int total_num = facts_of_attribute.get(attr_key).get(attr_sum)
					.intValue();
			total_num++;
			facts_of_attribute.get(attr_key).put(attr_sum, total_num);

			// System.out.println("getGain of "+attributeToSplit+
			// ": total_num "+ facts_of_attribute.get(attr_key).get(attr_sum) +
			// " and "+facts_of_attribute.get(attr_key).get(targetKey) +
			// " at attr=" + attr_key + " of t:"+targetKey);
		}
		FACTS_READ += facts.size();
		// *OPT* }
		// *OPT* }
		double sum = getAttrInformation(facts_of_attribute, total_num_facts);
//		for (Object attr : attributeValues) {
//			int total_num_attr = facts_of_attribute.get(attr).get(attr_sum)
//					.intValue();
//
//			double sum_attr = 0.0;
//			if (total_num_attr > 0)
//				for (Object t : targetValues) {
//					int num_attr_target = facts_of_attribute.get(attr).get(t)
//							.intValue();
//
//					double prob = (double) num_attr_target / total_num_attr;
//					// System.out.println("prob "+ prob);
//					sum_attr += (prob == 0.0) ? 0.0 : (-1 * prob * Util
//							.log2(prob));
//				}
//			sum += ((double) total_num_attr / (double) total_num_facts)
//					* sum_attr;
//		}
		return sum;
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

	// *OPT* public double getGain(List<FactSet> facts, String attributeToSplit)
	public double getContinuousGain(List<Fact> facts,
			List<Integer> split_facts, int begin_index, int end_index,
			Hashtable<Object, Integer> facts_in_class, String attributeToSplit) {
		
		System.out.println("What is the attributeToSplit? " + attributeToSplit);

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

		String targetAttr = getTarget();
		List<?> targetValues = getPossibleValues(getTarget());
		List<?> boundaries = getPossibleValues(attributeToSplit);

		// Fact split_point = facts.get(facts.size() / 2);
		// a b a a b
		// 1 2 3 4 5
		// 1.5
		// 2.5
		// 3.5
		// 0.00001 0.00002 1 100
		// 0.000015

		// < 50 >
		// 25 75
		// HashTable<Boolean>

		String attr_sum = Util.getSum();

		

		/* initialize the hashtable */
		Hashtable<Object, Hashtable<Object, Integer>> facts_of_attribute = 
			new Hashtable<Object, Hashtable<Object, Integer>>(Util.getDividingSize());
		// attr_0 bhas nothing everything inside attr_1
		Object cut_point; //attr_0
		Object last_poit = facts.get(facts.size()-1).getFieldValue(attributeToSplit);
		for (int i = 0; i < 2; i++) {
			facts_of_attribute.put(Integer.valueOf(i),
					new Hashtable<Object, Integer>(targetValues.size() + 1));
			//Hashtable<Object, Integer> facts_in_class
			if (i == 1) {
				for (Object t : targetValues) {
					facts_of_attribute.get(Integer.valueOf(i)).put(t,
							facts_in_class.get(t));
				}
				facts_of_attribute.get(Integer.valueOf(i)).put(attr_sum,
						facts.size());
			} else {
				for (Object t : targetValues) {
					facts_of_attribute.get(Integer.valueOf(i)).put(t, 0);
				}
				facts_of_attribute.get(Integer.valueOf(i)).put(attr_sum, 0);
			}
		}
		
		/*
		 * 2. Look for potential cut-points. 
		 */

		int split_index = 1;
		int last_index = facts.size();
		Iterator<Fact> f_ite = facts.iterator();
		Fact f1 = f_ite.next();
		while (f_ite.hasNext()) {

			Fact f2 = f_ite.next();
			
			// everytime it is not a split change the place in the distribution
			
			Object targetKey = f2.getFieldValue(target);
			
			// System.out.println("My key: "+ targetKey.toString());
			
			//for (Object attr_key : attr_values)

			Object attr_key_1 = Integer.valueOf(0);
			int num_1 = facts_of_attribute.get(attr_key_1).get(targetKey).intValue();
			num_1++;
			facts_of_attribute.get(attr_key_1).put(targetKey, num_1);
			
			int total_num_1 = facts_of_attribute.get(attr_key_1).get(attr_sum).intValue();
			total_num_1++;
			facts_of_attribute.get(attr_key_1).put(attr_sum, total_num_1);
			
			Object attr_key_2= Integer.valueOf(1);
			int num_2 = facts_of_attribute.get(attr_key_2).get(targetKey).intValue();
			num_2--;
			facts_of_attribute.get(attr_key_2).put(targetKey, num_2);
			
			int total_num_2 = facts_of_attribute.get(attr_key_2).get(attr_sum).intValue();
			total_num_2++;
			facts_of_attribute.get(attr_key_2).put(attr_sum, total_num_2);

			/*
			 * 2.1 Cut points are points in the sorted list above where the class labels change. 
			 * Eg. if I had five instances with values for the attribute of interest and labels 
			 * (1.0,A), (1.4,A), (1.7, A), (2.0,B), (3.0, B), (7.0, A), then there are only
			 * two cutpoints of interest: 1.85 and 5 (mid-way between the points
			 * where the classes change from A to B or vice versa).
			 */
			if (f1.getFieldValue(targetAttr) != f2.getFieldValue(targetAttr)) {
				// the cut point
				Number cp_i = (Number) f1.getFieldValue(attributeToSplit);
				Number cp_i_next = (Number) f2.getFieldValue(attributeToSplit);

				cut_point = (cp_i.doubleValue() + cp_i_next
						.doubleValue()) / 2;
				
				/*
				 * 3. Evaluate your favourite disparity measure 
				 * (info gain, gain ratio, gini coefficient, chi-squared test) on the cut point
				 * and calculate its gain 
				 */
				double sum = getAttrInformation(facts_of_attribute, facts.size());
//				
//				double sum = 0.0;
//				// for (Object attr : attributeValues) {
//				for (int i = 0; i < 2; i++) {
//
//					int total_num_attr = facts_of_attribute.get(Integer.valueOf(i)).get(attr_sum).intValue();
//
//					double sum_attr = 0.0;
//					if (total_num_attr > 0)
//						for (Object t : targetValues) {
//							int num_attr_target = facts_of_attribute.get(Integer.valueOf(i)).get(t).intValue();
//
//							double prob = (double) num_attr_target / total_num_attr;
//							// System.out.println("prob "+ prob);
//							sum_attr += (prob == 0.0) ? 0.0 : (-1 * prob * Util.log2(prob));
//						}
//					sum += ((double) total_num_attr / (double) facts.size())* sum_attr;
//				}
	

			} else {}

//			 getContinuousGain(facts, split_facts.subList(0,
//			 split_index+1), 0, split_index+1,
//			 facts_in_class1, attributeToSplit);
//								
//			 getContinuousGain(facts, split_facts.subList(split_index+1,
//			 last_index), split_index+1, last_index,
//			 facts_in_class2, attributeToSplit);
		
			f1 = f2;
			split_index ++;
		}

		return 1.0;
	}

	public double getContinuousGain_(List<Fact> facts,
			List<Integer> split_facts, int begin_index, int end_index,
			Hashtable<Object, Integer> facts_in_class, String attributeToSplit) {
		System.out.println("What is the attributeToSplit? " + attributeToSplit);

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

		String targetAttr = getTarget();
		List<?> boundaries = getPossibleValues(attributeToSplit);

		// Fact split_point = facts.get(facts.size() / 2);
		// a b a a b
		// 1 2 3 4 5
		// 1.5
		// 2.5
		// 3.5
		// 0.00001 0.00002 1 100
		// 0.000015

		// < 50 >
		// 25 75
		// HashTable<Boolean>

		String attr_sum = "sum";

		/*
		 * 2. Look for potential cut-points. Cut points are points in the sorted
		 * list above where the class labels change. Eg. if I had five instances
		 * with values for the attribute of interest and labels (1.0,A),
		 * (1.4,A), (1.7, A), (2.0,B), (3.0, B), (7.0, A), then there are only
		 * two cutpoints of interest: 1.85 and 5 (mid-way between the points
		 * where the classes change from A to B or vice versa).
		 */

		/* initialize the hashtable */
		// Hashtable<Object, Hashtable<Object, Integer>> facts_of_attribute =
		// new Hashtable<Object, Hashtable<Object,
		// Integer>>(Util.getDividingSize());
		// for (Object attr : attributeValues) {
		// facts_of_attribute.put(attr, new Hashtable<Object, Integer>(
		// targetValues.size() + 1));
		// for (Object t : targetValues) {
		// facts_of_attribute.get(attr).put(t, 0);
		// }
		// facts_of_attribute.get(attr).put(attr_sum, 0);
		// }
		//		
		int split_index = 0;
		Iterator<Integer> split_ite = split_facts.iterator();
		int f1_index = split_ite.next().intValue();
		Fact f1 = facts.get(f1_index);
		while (split_ite.hasNext()) {
			int f2_index = f1_index + 1;
			Fact f2 = facts.get(f2_index);

			if (f1.getFieldValue(targetAttr) == f2.getFieldValue(targetAttr)) {
				// the cut point
				System.out
						.println("Bok i have splited what the fuck is happening f1:"
								+ f1 + " f2:" + f2);
				System.exit(0);

			}
			Number cp_i = (Number) f1.getFieldValue(attributeToSplit);
			Number cp_i_next = (Number) f2.getFieldValue(attributeToSplit);

			Object cut_point = (cp_i.doubleValue() + cp_i_next.doubleValue()) / 2;
			// calculate the gain of the cut point

			/*
			 * 3. Evaluate your favourite disparity measure (info gain, gain
			 * ratio, gini coefficient, chi-squared test) on each of the
			 * cutpoints, and choose the one with the maximum value (I think
			 * Fayyad and Irani used info gain).
			 */
			// double sum = 0.0;
			// //for (Object attr : attributeValues) {
			// for (int i = 1; i<2; i++) {
			//				 
			// int total_num_attr =
			// facts_of_attribute.get(attr).get(attr_sum).intValue();
			//			
			// double sum_attr = 0.0;
			// if (total_num_attr > 0)
			// for (Object t : targetValues) {
			// int num_attr_target =
			// facts_of_attribute.get(attr).get(t).intValue();
			//			
			// double prob = (double) num_attr_target/ total_num_attr;
			// // System.out.println("prob "+ prob);
			// sum_attr += (prob == 0.0) ? 0.0 : (-1 * prob * Util.log2(prob));
			// }
			// sum += ((double) total_num_attr / (double) total_num_facts)*
			// sum_attr;
			// }
			// getContinuousGain(facts, split_facts.subList(fromIndex,
			// centerIndex), begin_index, middle_index,
			// facts_in_class1, attributeToSplit);
			//					
			// getContinuousGain(facts, split_facts.subList(centerIndex,
			// toIndex), middle_index+1, end_index,
			// facts_in_class2, attributeToSplit);
			f1_index = split_ite.next().intValue();
			f1 = facts.get(f1_index);
		}

		List<?> targetValues = getPossibleValues(target);
		// Hashtable<Object, Integer> facts_in_class = new Hashtable<Object,
		// Integer>(targetValues.size());

		return 1.0;
	}

	// *OPT* public double getInformation(List<FactSet> facts) {
	Hashtable<Object, Integer> getStatistics(List<Fact> facts, String target) {

		List<?> targetValues = getPossibleValues(this.target);
		Hashtable<Object, Integer> facts_in_class = new Hashtable<Object, Integer>(
				targetValues.size());

		for (Object t : targetValues) {
			facts_in_class.put(t, 0);
		}

		int total_num_facts = 0;
		// *OPT* for (FactSet fs: facts) {
		// *OPT* for (Fact f: fs.getFacts()) {
		for (Fact f : facts) {
			total_num_facts++;
			Object key = f.getFieldValue(target);
			// System.out.println("My key: "+ key.toString());
			facts_in_class.put(key, facts_in_class.get(key).intValue() + 1); // bocuk
			// kafa
			// :P
		}
		FACTS_READ += facts.size();
		// *OPT* }
		// *OPT* }
		return facts_in_class;
	}

	// *OPT* public double getInformation(List<FactSet> facts) {
	/**
	 * it returns the information value of facts entropy that characterizes the
	 * (im)purity of an arbitrary collection of examples
	 * 
	 * @param facts
	 *            list of facts
	 */
	public double getInformation_old(List<Fact> facts) {

		List<?> targetValues = getPossibleValues(this.target);
		Hashtable<Object, Integer> facts_in_class = getStatistics(facts,
				getTarget()); // , targetValues)
		// Hashtable<Object, Integer> facts_in_class = getStatistics(facts,
		// getTarget(), targetValues);
		int total_num_facts = facts.size();
		double sum = 0;
		for (Object key : targetValues) {
			int num_in_class = facts_in_class.get(key).intValue();
			// System.out.println("num_in_class : "+ num_in_class + " key "+ key
			// + " and the total num "+ total_num_facts);
			double prob = (double) num_in_class / (double) total_num_facts;

			// double log2= Util.log2(prob);
			// double plog2p= prob*log2;
			sum += (prob == 0.0) ? 0.0 : -1 * prob * Util.log2(prob);
			// System.out.println("prob "+ prob +" and the plog(p)"+plog2p+"
			// where the sum: "+sum);
		}
		return sum;
	}
	
	public double getAttrInformation( Hashtable<Object, Hashtable<Object, Integer>> facts_of_attribute, int fact_size) {
		
		Collection<Object> attributeValues = facts_of_attribute.keySet();
		String attr_sum = Util.getSum();
		double sum = 0.0;
		for (Object attr : attributeValues) {
			int total_num_attr = facts_of_attribute.get(attr).get(attr_sum).intValue();
			//double sum_attr = 0.0;
			if (total_num_attr > 0) {
				sum += ((double) total_num_attr / (double) fact_size)* 
				getInformation(facts_of_attribute.get(attr), total_num_attr);
			}
		}
		return sum;
	}

	public double getInformation(Hashtable<Object, Integer> facts_in_class, int total_num_facts) {

		// List<?> targetValues = getPossibleValues(this.target);
		// Hashtable<Object, Integer> facts_in_class = getStatistics(facts,
		// getTarget()); //, targetValues);
		Collection<Object> targetValues = facts_in_class.keySet();
		double sum = 0;
		for (Object key : targetValues) {
			int num_in_class = facts_in_class.get(key).intValue();
			// System.out.println("num_in_class : "+ num_in_class + " key "+ key
			// + " and the total num "+ total_num_facts);
			double prob = (double) num_in_class / (double) total_num_facts;

			// double log2= Util.log2(prob);
			// double plog2p= prob*log2;
			sum += (prob == 0.0) ? 0.0 : -1 * prob * Util.log2(prob);
			// System.out.println("prob "+ prob +" and the plog(p)"+plog2p+"
			// where the sum: "+sum);
		}
		return sum;
	}

	public void setTarget(String targetField) {
		target = targetField;
		attrsToClassify.remove(target);
	}

	public void addDomain(Domain<?> domain) {
		domainSet.put(domain.getName(), domain);
		if (!domain.getName().equals(this.target))
			attrsToClassify.add(domain.getName());

	}

	public List<?> getPossibleValues(String fieldName) {
		return domainSet.get(fieldName).getValues();
	}

	public List<String> getAttributes() {
		return attrsToClassify;
	}

	public String getTarget() {
		return target;
	}

	public String getName() {
		return className;
	}

	public Domain<?> getDomain(String key) {
		return domainSet.get(key);
	}

	public TreeNode getRoot() {
		return (root);

	}

	public void setRoot(TreeNode root) {
		this.root = root;

	}

	public long getNumRead() {
		return FACTS_READ;
	}

	@Override
	public String toString() {
		return "Facts scanned " + FACTS_READ + "\n" + root.toString();
	}

	/*
	 * **OPT int getTotalSize(List<FactSet> facts) {
	 * 
	 * int num = 0; for(FactSet fs : facts) { num += fs.getSize(); }
	 * 
	 * return num; }
	 */

}
