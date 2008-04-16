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
			result = builder.train(dt, facts, attributeNames);
			currentNode.addNode(value, result);
		}
	}

	MyThread helper;
	private int FUNC_CALL = 0;
	protected int num_fact_trained = 0;
	private ArrayList<Fact> unclassified_facts;
	private ArrayList<Fact> training_facts;
	private WorkingMemory global_wm;
	private List<Domain<?>> domains;
	
	/*
	 * treebuilder.execute(workingmemory, classtoexecute, attributestoprocess)
	 * 
	 * foreach factset in workingmemory if classtoexecute.isAssignableFrom(
	 * factset.class ) internaladd(factset)
	 * 
	 * internalprocess(attributestoprocess)
	 */
	public C45TreeBuilder(WorkingMemory wm) {
		
		unclassified_facts = new ArrayList<Fact>();
		training_facts = new ArrayList<Fact>();
		global_wm = wm;
		domains = new ArrayList<Domain<?>>();
	
	}
	
	public C45TreeBuilder() {
		
		unclassified_facts = new ArrayList<Fact>();
		training_facts = new ArrayList<Fact>();
		domains = new ArrayList<Domain<?>>();
	
	}
	
	
	private void setKlass(Class<?> klass) {
		Iterator<FactSet> it_fs = global_wm.getFactsets();
		FactSet klass_fs = null;
		while (it_fs.hasNext()) {
			FactSet fs = it_fs.next();
			if (fs instanceof OOFactSet) {
				if (klass.isAssignableFrom(((OOFactSet) fs).getFactClass())) {
					// **OPT facts.add(fs);
					fs.assignTo(training_facts); // adding all facts of fs to "facts
				}
			} else if (klass.getName().equalsIgnoreCase(fs.getClassName())) {
				fs.assignTo(training_facts); // adding all facts of fs to "facts"

				klass_fs = fs;
				break;
			}
			if (klass.getName() == fs.getClassName()) {
				klass_fs = fs;
			}
		}
		
		for (Domain<?> d : klass_fs.getDomains())
			domains.add(d);
	}
	
	private void init(DecisionTree dt, String targetField, List<String> workingAttributes) {
		dt.setTarget(targetField);
		if (workingAttributes != null)
			for (String attr : workingAttributes) {
				dt.addDomain(global_wm.getDomain(attr));
			}
		else {
			for (Domain<?> d : domains) {
				dt.addDomain(d);	
			}
		}
		
	}
	
	/* building with a training and test */
	public DecisionTree build(Class<?> klass, String targetField, List<String> workingAttributes) {
		/* gets the facts the decision tree is eligible */
		setKlass(klass);
		
		DecisionTree dt = new DecisionTree(klass.getName());
		init(dt, targetField, workingAttributes);
		
		
		DecisionTree best_dt = new DecisionTree(klass.getName());
		init(dt, targetField, workingAttributes);
		
		ArrayList<String> attrs = new ArrayList<String>(dt.getAttributes());
		Collections.sort(attrs);
		
		
		dt.FACTS_READ += training_facts.size();
		/* you must set this when the training called the first time */
		setNum_fact_trained(training_facts.size());

		//while ()
		TreeNode root = train(dt, training_facts, attrs);
		dt.setRoot(root);
		
		System.out.println(Util.ntimes("\n", 2)+Util.ntimes("$", 5)+" TESTING "+Util.ntimes("\n", 2));
		List<Integer> evaluation = test(dt, training_facts.subList(339, 340));

		System.out.println("TESTING results: Mistakes "+ evaluation.get(0));
		System.out.println("TESTING results: Corrects "+ evaluation.get(1));
		System.out.println("TESTING results: Unknown "+ evaluation.get(2));
		if (evaluation.get(1) == training_facts.size()) {
			best_dt.setRoot(root);
		}
		
		return dt;
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
			} else if (klass.getName().equalsIgnoreCase(fs.getClassName())) {
				fs.assignTo(facts); // adding all facts of fs to "facts"

				klass_fs = fs;
				break;
			}
			if (klass.getName() == fs.getClassName()) {
				klass_fs = fs;
			}
		}
		dt.FACTS_READ += facts.size();

		setNum_fact_trained(facts.size());

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

		TreeNode root = train(dt, facts, attrs);
		dt.setRoot(root);

		return dt;
	}

/*	public DecisionTree build(WorkingMemory wm, String klass,
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
	}*/

	public TreeNode train(DecisionTree dt, List<Fact> facts,
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
			classifiedNode.setRank((double) facts.size()/(double) getNum_fact_trained());
			classifiedNode.setNumSupporter(facts.size());
			
			return classifiedNode;
		}

		/* if there is no attribute left in order to continue */
		if (attributeNames.size() == 0) {
			/* an heuristic of the leaf classification */
			Object winner = stats.getThe_winner_target_class();
			LeafNode noAttributeLeftNode = new LeafNode(dt.getDomain(dt.getTarget()), winner);
			noAttributeLeftNode.setRank((double) stats.getVoteFor(winner)/ (double) num_fact_trained);
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
				TreeNode newNode = train(dt, filtered_facts.get(value), attributeNames_copy);
				currentNode.addNode(value, newNode);
			}
		}

		return currentNode;
	}

	
	
	public List<Integer> test(DecisionTree dt, List<Fact> facts) {
		/*
		 * false | true | unknown
		 *   |		 |       |
		 *   0       1       2
		 */
		List<Integer> results = new ArrayList<Integer>(3);
		for (int i=0; i < 3; i ++) {
			results.add(new Integer(0));
		}
		
		int i = 0;
		for (Fact f : facts) {
			if (Util.DEBUG_TEST) {
				System.out.println(Util.ntimes("#\n", 5)+i+ " <START> TEST: f="+ f);
				//System.exit(0);
			}
			Integer result = dt.test(f);
			
			results.set(result, Integer.valueOf(results.get(result) + 1));
			i ++;
		}
		return results;
		
	}

	public int getNumCall() {
		return FUNC_CALL;
	}

	public int getNum_fact_trained() {
		return num_fact_trained;
	}

	public void setNum_fact_trained(int num_fact_processed) {
		this.num_fact_trained = num_fact_processed;
	}
}
