package id3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

public class DecisionTree {
	
	public long FACTS_READ = 0;
	
	/* set of the attributes, their types*/
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
		Hashtable<Object, Integer> facts_in_class = getStatistics(facts, target, targetValues);
		
		int winner_vote = 0;
		Object winner = null;
		for (Object key: targetValues) {

			int num_in_class = facts_in_class.get(key).intValue();
			if (num_in_class > winner_vote) {
				winner_vote = num_in_class;
				winner = key;
			}
		}
		return winner;
	}


//*OPT*	public double calculateGain(List<FactSet> facts, String attributeName) {	
	public double calculateGain(List<Fact> facts, String attributeName) {
		return getInformation(facts) - getGain(facts, attributeName);
	}

//*OPT*	public double getGain(List<FactSet> facts, String attributeToSplit) {
	public double getGain(List<Fact> facts, String attributeToSplit) {
		System.out.println("What is the attributeToSplit? "+attributeToSplit);
		List<?> attributeValues = getPossibleValues(attributeToSplit);

		String attr_sum = "sum";

		List<?> targetValues = getPossibleValues(target);
		//Hashtable<Object, Integer> facts_in_class = new Hashtable<Object, Integer>(targetValues.size());

		/* initialize the hashtable */
		Hashtable<Object, Hashtable<Object, Integer>> facts_of_attribute = new Hashtable<Object, Hashtable<Object, Integer>>(attributeValues.size());
		for (Object attr: attributeValues) {
			facts_of_attribute.put(attr, new Hashtable<Object, Integer>(targetValues.size()+1));
			for (Object t: targetValues) {
				facts_of_attribute.get(attr).put(t, 0);
			}
			facts_of_attribute.get(attr).put(attr_sum, 0);
		}


		int total_num_facts= 0;
//*OPT*		for (FactSet fs: facts) {
//*OPT*			for (Fact f: fs.getFacts()) {
		for (Fact f: facts) {
				total_num_facts ++;
				Object targetKey = f.getFieldValue(target);
				//System.out.println("My key: "+ targetKey.toString());

				Object attr_key = f.getFieldValue(attributeToSplit);
				int num = facts_of_attribute.get(attr_key).get(targetKey).intValue();
				num ++;
				facts_of_attribute.get(attr_key).put(targetKey, num);

				int total_num = facts_of_attribute.get(attr_key).get(attr_sum).intValue();
				total_num ++;
				facts_of_attribute.get(attr_key).put(attr_sum, total_num);

//				System.out.println("getGain of "+attributeToSplit+
//				": total_num "+ facts_of_attribute.get(attr_key).get(attr_sum) +
//				" and "+facts_of_attribute.get(attr_key).get(targetKey) +
//				" at attr=" + attr_key + " of t:"+targetKey);
		}
		FACTS_READ += facts.size();
//*OPT*			}
//*OPT*		}

		double sum = 0.0;
		for (Object attr: attributeValues) {
			int total_num_attr = facts_of_attribute.get(attr).get(attr_sum).intValue();
			
			double sum_attr = 0.0;
			if (total_num_attr > 0)
				for (Object t: targetValues) {
					int num_attr_target = facts_of_attribute.get(attr).get(t).intValue();

					double prob = (double)num_attr_target/total_num_attr;
					//System.out.println("prob "+ prob);
					sum_attr += (prob == 0.0) ? 0.0 : (-1* prob * Util.log2(prob));
				}
			sum += ((double)total_num_attr/(double)total_num_facts) * sum_attr;
		}
		return sum;
	}
	
	
	private class FactNumericAttributeComparator implements Comparator<Fact> {
		private String attr_name;
		public FactNumericAttributeComparator(String _attr_name) {
			attr_name = _attr_name;
		}

		public int compare(Fact f0, Fact f1) {
			Number n0 = (Number)f0.getFieldValue(attr_name);
			Number n1 = (Number)f1.getFieldValue(attr_name);
			if (n0.doubleValue() < n1.doubleValue())
				return -1;
			else if (n0.doubleValue() > n1.doubleValue())
				return 1;
			else
				return 0;
		}
	}

	/* GLOBAL DISCRETIZATION
	 a a b a b b b b b   (target)
	 1 2 3 4 5 6 7 8 9   (attr c)
	 0 0 0 0 1 1 1 1 1
	    "<5", ">=5"
	  "true"  "false"
	*/

	//*OPT*	public double getGain(List<FactSet> facts, String attributeToSplit) {
	public double getContinuousGain(List<Fact> facts, String attributeToSplit) {
		System.out.println("What is the attributeToSplit? "+attributeToSplit);

		List<?> boundaries = getPossibleValues(attributeToSplit);

		/* sort the values */
		Collections.sort(facts, new FactNumericAttributeComparator(attributeToSplit));

		//Fact split_point = facts.get(facts.size() / 2);
		//		a b a a b
		// 		1 2 3 4 5
		//       1.5
		//			2.5
		//			 3.5
		//    0.00001 0.00002 1 100
		//		0.000015    
		
		//      <  50   >
		//      25     75
		//HashTable<Boolean>
		
		String attr_sum = "sum";

		List<?> targetValues = getPossibleValues(target);
		//Hashtable<Object, Integer> facts_in_class = new Hashtable<Object, Integer>(targetValues.size());

		return 1.0;
	}
	
	
//*OPT*		public double getInformation(List<FactSet> facts) {	
	Hashtable<Object, Integer> getStatistics(List<Fact> facts, String target, List<?> targetValues) {
		Hashtable<Object, Integer> facts_in_class = new Hashtable<Object, Integer>(targetValues.size());

		for (Object t: targetValues) {
			facts_in_class.put(t, 0);
		}

		int total_num_facts= 0;
//*OPT*		for (FactSet fs: facts) {
//*OPT*			for (Fact f: fs.getFacts()) {
		for (Fact f: facts) {
				total_num_facts++;
				Object key = f.getFieldValue(target);
				//System.out.println("My key: "+ key.toString());
				facts_in_class.put(key, facts_in_class.get(key).intValue() + 1); // bocuk kafa :P
		}
		FACTS_READ += facts.size();
//*OPT*			}
//*OPT*		}
		return facts_in_class;
	}


//*OPT*	public double getInformation(List<FactSet> facts) {
	/** it returns the information value of facts
	 *  entropy that characterizes the (im)purity of an arbitrary collection of examples
	 *  @param facts list of facts
	 */ 
	public double getInformation(List<Fact> facts) {

		List<?> targetValues = getPossibleValues(this.target);
		
		Hashtable<Object, Integer> facts_in_class = getStatistics(facts, target, targetValues);
		int total_num_facts = facts.size();
		double sum = 0;
		for (Object key: targetValues) {
			int num_in_class = facts_in_class.get(key).intValue();
			//System.out.println("num_in_class : "+ num_in_class + " key "+ key + " and the total num "+ total_num_facts);
			double prob = (double) num_in_class / (double) total_num_facts;
			
			//double log2= Util.log2(prob);
			//double plog2p= prob*log2;
			sum += (prob == 0.0) ? 0.0 :-1* prob * Util.log2(prob);
			//System.out.println("prob "+ prob +" and the plog(p)"+plog2p+" where the sum: "+sum);
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
		return(root);
		
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
	
	
	/* **OPT
		int getTotalSize(List<FactSet> facts) {

			int num = 0;
			for(FactSet fs : facts) {
				num += fs.getSize();
			}

			return num;
		}
	*/

}
