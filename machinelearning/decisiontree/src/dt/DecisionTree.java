package dt;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import dt.memory.Domain;
import dt.memory.Fact;
import dt.tools.Util;

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

	public DecisionTree(String klass) {
		this.className = klass;
		this.domainSet = new Hashtable<String, Domain<?>>();
		this.attrsToClassify = new ArrayList<String>();
	}

	public Object getMajority(List<Fact> facts) {
		List<?> targetValues = getPossibleValues(this.target);
		Hashtable<Object, Integer> facts_in_class = getStatistics(facts, target);

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

	// *OPT* public double getInformation(List<FactSet> facts) {
	public Hashtable<Object, Integer> getStatistics(List<Fact> facts, String target) {

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
