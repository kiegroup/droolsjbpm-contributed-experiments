package dt.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import dt.DecisionTree;
import dt.LeafNode;
import dt.TreeNode;
import dt.memory.Domain;
import dt.memory.Fact;
import dt.memory.FactDistribution;
import dt.memory.FactSet;
import dt.memory.OOFactSet;
import dt.memory.WorkingMemory;
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
	protected int FUNC_CALL = 0;
	protected int num_fact_trained = 0;
	private ArrayList<Fact> facts;
	private ArrayList<Fact> training_facts;
	protected ArrayList<Fact> unclassified_facts;
	
	private WorkingMemory global_wm;
	private List<Domain<?>> domains;
	private String target;
	private List<String> attributes;
	
	/*
	 * treebuilder.execute(workingmemory, classtoexecute, attributestoprocess)
	 * 
	 * foreach factset in workingmemory if classtoexecute.isAssignableFrom(
	 * factset.class ) internaladd(factset)
	 * 
	 * internalprocess(attributestoprocess)
	 */
	public C45TreeBuilder(WorkingMemory wm) {
	
		global_wm = wm;
		
		facts = new ArrayList<Fact>();
		training_facts = new ArrayList<Fact>();
		unclassified_facts = new ArrayList<Fact>();
	
		target = null;
		attributes = new ArrayList<String>();
		domains = new ArrayList<Domain<?>>();
	}
	
	public C45TreeBuilder() {
		
		facts = new ArrayList<Fact>();
		training_facts = new ArrayList<Fact>();
		unclassified_facts = new ArrayList<Fact>();

		target = null;
		attributes = new ArrayList<String>();
		domains = new ArrayList<Domain<?>>();
		
	}
	/* set the builder's 
	 *  domains
	 */
	public void setDomains(Class<?> klass) {
		FactSet klass_fs = null;
		
		for (Domain<?> d : klass_fs.getDomains())
			domains.add(d);
	}
	
	/* set the builder's 
	 * 	facts
	 *  domains
	 */
	private void setKlass(Class<?> klass) {
		Iterator<FactSet> it_fs = global_wm.getFactsets();
		FactSet klass_fs = null;
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
		
		for (Domain<?> d : klass_fs.getDomains())
			domains.add(d);
	}
	
	/* initialize the builder's 
	 * 	targetField
	 * 	the attribute list (workingAttributes != null ? workingAttributes : domains )
	 */
	public void init(String targetField, List<String> workingAttributes) {
		this.setTarget(targetField);
		if (workingAttributes != null)
			for (String attr : workingAttributes) {
				this.addAttribute(attr);
			}
		else {
			for (Domain<?> d : domains) {
				this.addAttribute(d.getName());	
			}
		}
		
	}
	
	public String getTarget() {
		return this.target;
	}
	
	public void setTarget(String targetField) {
		this.target = targetField;
		//attrsToClassify.remove(target);
	}
	
	public void addDomain(Domain<?> d) {
		//if (!attribute.equals(this.target))
			//attributes.add(d.getName());
			domains.add(d);
	}
	public void addAttribute(String attribute) {
		//if (!attribute.equals(this.target))
			attributes.add(attribute);
	}
	public void init_dt(DecisionTree dt, String targetField) {
		dt.setTarget(targetField);
		for (Domain<?> d : domains) {
			dt.addDomain(d);	
		}
		
	}
	private void init_dt(DecisionTree dt, String targetField, List<String> workingAttributes) {
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
	/* building with the training set (all relative facts from wm) from scratch*/
	public DecisionTree build(Class<?> klass) {
		/* gets the facts the decision tree is eligible */
		setKlass(klass);
		
		DecisionTree dt = new DecisionTree(klass.getName());
		init_dt(dt, this.target, this.attributes);
		
		ArrayList<String> attrs = new ArrayList<String>(dt.getAttributes());
		Collections.sort(attrs);
		
		training_facts.addAll(facts);
		dt.FACTS_READ += training_facts.size();

		//while ()
		TreeNode root = train(dt, training_facts, attrs);
		dt.setRoot(root);
	
		
		return dt;
	}
		
	
	/* building with a training and test */
	public DecisionTree build_test(Class<?> klass, String targetField, List<String> workingAttributes) {
		if (this.target == null) {
			System.out.println("Target is not set");
			System.exit(0);
		}
		/* gets the facts the decision tree is eligible */
		DecisionTree tree = build(klass);
		
		System.out.println(Util.ntimes("\n", 2)+Util.ntimes("$", 5)+" TESTING "+Util.ntimes("\n", 2));
		List<Integer> evaluation = test(tree, training_facts);//.subList(339, 340));

		System.out.println("TESTING results: Mistakes "+ evaluation.get(0));
		System.out.println("TESTING results: Corrects "+ evaluation.get(1));
		System.out.println("TESTING results: Unknown "+ evaluation.get(2) +" OF "+  training_facts.size() + " facts");
		return tree;
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
		this.add_to_training(facts);

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

	/* building with the training set (some part of the facts) */
	public DecisionTree build(Class<?> klass, List<Fact> first_facts) {
		/* gets the facts which the decision tree is eligible */
		//setKlass(klass);
		
		DecisionTree dt = new DecisionTree(klass.getName());
		init_dt(dt, this.target); // initialize the decision tree with the target and all domains
		
		ArrayList<String> attrs = new ArrayList<String>(dt.getAttributes());
		Collections.sort(attrs);
		
		this.add_to_training(first_facts);	/* you must set this when the training called the first time */
		dt.FACTS_READ += first_facts.size();

		//while ()
		TreeNode root = train(dt, training_facts, attrs);
		dt.setRoot(root);
		
		
		return dt;
	}
	
	public TreeNode train(DecisionTree dt, List<Fact> facts, List<String> attributeNames) {

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
		if (Util.RUN)	System.out.println(Util.ntimes("*", 20) + " 1st best attr: "+ choosenDomain.getName());

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

			ArrayList<String> attributeNames_copy = new ArrayList<String>(attributeNames);
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

	/* building with the training set (some part of the facts) */
	public DecisionTree re_build(DecisionTree dt, List<Fact> new_facts) {
		
		ArrayList<String> attrs = new ArrayList<String>(dt.getAttributes());
		Collections.sort(attrs);
		
		
		this.add_to_training(new_facts);	/* you must set this when the training called the first time */
		dt.FACTS_READ += new_facts.size();

		System.out.println(Util.ntimes("\n", 10)+"How facts are u training? "+ training_facts.size());
		//while ()
		TreeNode root = re_train(dt, dt.getRoot(), training_facts, attrs);
		dt.setRoot(root);
		
		return dt;
	}
	
	public TreeNode re_train(DecisionTree dt, TreeNode currentNode, List<Fact> facts, List<String> attributeNames) {

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
		if (Util.RUN)	System.out.println(Util.ntimes("*", 20) + " 1st best attr: "+ choosenDomain.getName());
		else if (FUNC_CALL % 100 ==0){
			System.out.print(".");
		}
		
		Hashtable<Object, List<Fact>> filtered_facts = FactProcessor.splitFacts(facts, choosenDomain);
		for (Object value : filtered_facts.keySet()) {
			if (filtered_facts.get(value).isEmpty()){
				@SuppressWarnings("unused")
				boolean bok = true;
			}
		}
		dt.FACTS_READ += facts.size();
		
		if (currentNode.getDomain() == choosenDomain) {
			
			
			for (Object value : filtered_facts.keySet()) {
				
				TreeNode childNode = currentNode.getChild(value);
				/* split the last two class at the same time */

				ArrayList<String> attributeNames_copy = new ArrayList<String>(
						attributeNames);
				attributeNames_copy.remove(choosenDomain.getName());

				if (filtered_facts.get(value).isEmpty()) {
					/* majority !!!! */
					//Comparator<Fact> targetComp = dt.getDomain(dt.getTarget()).factComparator(); 
					
					if (childNode == null || !(childNode instanceof LeafNode)) {
						LeafNode majorityNode = new LeafNode(dt.getDomain(dt.getTarget()), stats.getThe_winner_target_class());
						majorityNode.setRank(-1.0); // classifying nothing
						majorityNode.setNumSupporter(filtered_facts.get(value).size());
						
						childNode = majorityNode; // How to set this guy
						if (childNode == null)
							currentNode.addNode(value, childNode);
					}
					
					else {
						/* have to remove the leafnode from the children list with key value*/
						((LeafNode)childNode).setRank(-1.0); // classifying nothing
						((LeafNode)childNode).setNumSupporter(filtered_facts.get(value).size());
						
						if (dt.getDomain(dt.getTarget()).compare(((LeafNode)childNode).getValue(), value)!=0) {
							((LeafNode)childNode).setTargetValue(value);
							//currentNode.
						}
					} 	
				} else {
					 
					if (childNode == null) { // there was no node assigned for that object value
						childNode = train(dt, filtered_facts.get(value), attributeNames_copy);
						currentNode.addNode(value, childNode);
					}
					else {
						TreeNode newNode = re_train(dt, childNode, filtered_facts.get(value), attributeNames_copy);
					//currentNode.addNode(value, newNode);
					}
				}
			}
			
		} else {
			currentNode = new TreeNode(choosenDomain);
			
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

			Integer result = dt.test(f);
			if (Util.DEBUG_TEST) {
				System.out.println(Util.ntimes("#\n", 1)+i+ " <START> TEST: f="+ f + " = target "+ result);
			} else
				if (i%1000 ==0)	System.out.print(".");
			results.set(result, Integer.valueOf(results.get(result) + 1));
			i ++;
		}
		return results;
		
	}
	
	public List<Fact> getFacts(int fromIndex, int toIndex) {
		return facts.subList(fromIndex, toIndex); //.iterator();
	}
		
	public List<Fact> getFacts() {
		return facts; //.iterator();
	}
	
	public List<Fact> getTrainingFacts() {
		return training_facts; //.iterator();
	}
	
	public List<Fact> getUnClassifiedFacts() {
		return unclassified_facts; //.iterator();
	}
	
	
	public int getNumUnClassifiedFacts() {
		return unclassified_facts.size(); //.iterator();
	}

	public int getNumCall() {
		return FUNC_CALL;
	}
	public int getNum_fact_trained() {
		return training_facts.size();
	}
//	public int getNum_fact_trained() {
//		return num_fact_trained;
//	}
//
//	public void setNum_fact_trained(int num_fact_processed) {
//		this.num_fact_trained = num_fact_processed;
//	}

	public void add_to_training(List<Fact> new_facts) {
		this.training_facts.addAll(new_facts);
	}
}
