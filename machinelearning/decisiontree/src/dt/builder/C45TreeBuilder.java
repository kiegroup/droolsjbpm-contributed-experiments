package dt.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import dt.DecisionTree;
import dt.LeafNode;
import dt.TreeNode;

import dt.memory.FactDistribution;
import dt.memory.FactTargetDistribution;
import dt.memory.WorkingMemory;
import dt.memory.Fact;
import dt.memory.FactSet;
import dt.memory.OOFactSet;
import dt.memory.Domain;
import dt.tools.FactProcessor;
import dt.tools.Util;

public class C45TreeBuilder implements DecisionTreeBuilder {

	class MyThread extends Thread {
		C45TreeBuilder builder;
		DecisionTree dt;
		List<Fact> facts;
		List<String> attributeNames;
		TreeNode currentNode = null;
		Object value = null;
		TreeNode result = null;

		@Override
		public void run() {
			result = builder.c45(dt, facts, attributeNames);
			currentNode.addNode(value, result);
		}
	}

	MyThread helper;
	private int FUNC_CALL = 0;
	protected int num_fact_processed = 0;
	private ArrayList<Fact> unclassified_facts;
	
	/*
	 * treebuilder.execute(workingmemory, classtoexecute, attributestoprocess)
	 * 
	 * foreach factset in workingmemory if classtoexecute.isAssignableFrom(
	 * factset.class ) internaladd(factset)
	 * 
	 * internalprocess(attributestoprocess)
	 */

	public int getNum_fact_processed() {
		return num_fact_processed;
	}

	public void setNum_fact_processed(int num_fact_processed) {
		this.num_fact_processed = num_fact_processed;
	}

	public DecisionTree build(WorkingMemory wm, Class<?> klass,
			String targetField, List<String> workingAttributes) {
		
		unclassified_facts = new ArrayList<Fact>();
		DecisionTree dt = new DecisionTree(klass.getName());
		// **OPT List<FactSet> facts = new ArrayList<FactSet>();
		ArrayList<Fact> facts = new ArrayList<Fact>();
		FactSet klass_fs = null;
		Iterator<FactSet> it_fs = wm.getFactsets();
		while (it_fs.hasNext()) {
			FactSet fs = it_fs.next();
			if (fs instanceof OOFactSet) {
				if (klass.isAssignableFrom(((OOFactSet) fs).getFactClass())) {
					// **OPT facts.add(fs);
					fs.assignTo(facts); // adding all facts of fs to "facts
				}
			}
			if (klass.getName() == fs.getClassName()) {
				klass_fs = fs;
			}
		}
		dt.FACTS_READ += facts.size();

		setNum_fact_processed(facts.size());

		if (workingAttributes != null)
			for (String attr : workingAttributes) {
				dt.addDomain(klass_fs.getDomain(attr));
			}
		else
			for (Domain<?> d : klass_fs.getDomains())
				dt.addDomain(d);

		dt.setTarget(targetField);

		ArrayList<String> attrs = new ArrayList<String>(dt.getAttributes());
		Collections.sort(attrs);

		TreeNode root = c45(dt, facts, attrs);
		dt.setRoot(root);

		return dt;
	}

	public DecisionTree build(WorkingMemory wm, String klass,
			String targetField, List<String> workingAttributes) {
		unclassified_facts = new ArrayList<Fact>();
		DecisionTree dt = new DecisionTree(klass);
		// **OPT List<FactSet> facts = new ArrayList<FactSet>();
		ArrayList<Fact> facts = new ArrayList<Fact>();
		FactSet klass_fs = null;
		Iterator<FactSet> it_fs = wm.getFactsets();
		while (it_fs.hasNext()) {
			FactSet fs = it_fs.next();
			if (klass == fs.getClassName()) {
				// **OPT facts.add(fs);
				fs.assignTo(facts); // adding all facts of fs to "facts"

				klass_fs = fs;
				break;
			}
		}
		dt.FACTS_READ += facts.size();
		setNum_fact_processed(facts.size());

		if (workingAttributes != null)
			for (String attr : workingAttributes) {
				//System.out.println("Bok degil " + attr);
				dt.addDomain(klass_fs.getDomain(attr));
			}
		else
			for (Domain<?> d : klass_fs.getDomains())
				dt.addDomain(d);

		dt.setTarget(targetField);

		ArrayList<String> attrs = new ArrayList<String>(dt.getAttributes());
		Collections.sort(attrs);

		TreeNode root = c45(dt, facts, attrs);
		dt.setRoot(root);

		return dt;
	}

	private TreeNode c45(DecisionTree dt, List<Fact> facts,
			List<String> attributeNames) {

		FUNC_CALL++;
		if (facts.size() == 0) {
			throw new RuntimeException("Nothing to classify, factlist is empty");
		}
		/* let's get the statistics of the results */
		// List<?> targetValues = dt.getPossibleValues(dt.getTarget());
		//Hashtable<Object, Integer> stats_ = dt.getStatistics(facts, dt.getTarget());// targetValues
		
		//FactTargetDistribution stats = dt.getDistribution(facts);
		
		FactDistribution stats = new FactDistribution(dt.getDomain(dt.getTarget()));
		stats.calculateDistribution(facts);
		stats.evaluateMajority();

		/* if all elements are classified to the same value */
		if (stats.getNum_supported_target_classes() == 1) {

			LeafNode classifiedNode = new LeafNode(dt.getDomain(dt.getTarget()), stats.getThe_winner_target_class());
			classifiedNode.setRank((double) facts.size()/(double) getNum_fact_processed());
			classifiedNode.setNumSupporter(facts.size());
			
			return classifiedNode;
		}

		/* if there is no attribute left in order to continue */
		if (attributeNames.size() == 0) {
			/* an heuristic of the leaf classification */
			Object winner = stats.getThe_winner_target_class();
			LeafNode noAttributeLeftNode = new LeafNode(dt.getDomain(dt.getTarget()), winner);
			noAttributeLeftNode.setRank((double) stats.getVoteFor(winner)/ (double) num_fact_processed);
			noAttributeLeftNode.setNumSupporter(stats.getVoteFor(winner));
			
			/* we need to know how many guys cannot be classified and who these guys are */
			FactProcessor.splitUnclassifiedFacts(unclassified_facts, stats);
			
			return noAttributeLeftNode;
		}

		/* choosing the attribute for the branching starts */
//		String chosenAttribute = Entropy.chooseContAttribute(dt, facts, stats, attributeNames);
//		List<?> categorization = dt.getPossibleValues(chosenAttribute);
		Domain<?> choosenDomain = Entropy.chooseContAttribute(dt, facts, stats, attributeNames);
		System.out.println(Util.ntimes("*", 20) + " 1st best attr: "+ choosenDomain.getName());

		TreeNode currentNode = new TreeNode(choosenDomain);
			
		Hashtable<Object, List<Fact>> filtered_facts = FactProcessor.splitFacts(facts, choosenDomain);

		for (Object value : filtered_facts.keySet()) {
			if (filtered_facts.get(value).isEmpty()){
				@SuppressWarnings("unused")
				boolean bok = true;
			}
		}
		dt.FACTS_READ += facts.size();

		for (Object value : filtered_facts.keySet()) {
			/* split the last two class at the same time */

			ArrayList<String> attributeNames_copy = new ArrayList<String>(
					attributeNames);
			attributeNames_copy.remove(choosenDomain.getName());

			if (filtered_facts.get(value).isEmpty()) {
				/* majority !!!! */
				LeafNode majorityNode = new LeafNode(dt.getDomain(dt.getTarget()), stats.getThe_winner_target_class());
				majorityNode.setRank(-1.0); // classifying nothing
				majorityNode.setNumSupporter(filtered_facts.get(value).size());
				currentNode.addNode(value, majorityNode);
			} else {
				TreeNode newNode = c45(dt, filtered_facts.get(value), attributeNames_copy);
				currentNode.addNode(value, newNode);
			}
		}

		return currentNode;
	}
	public int getNumCall() {
		return FUNC_CALL;
	}


}
