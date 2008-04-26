package dt.builder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import dt.DecisionTree;
import dt.LeafNode;
import dt.TreeNode;

import dt.memory.Domain;
import dt.memory.Fact;
import dt.memory.FactDistribution;
import dt.tools.FactProcessor;
import dt.tools.Util;

public class C45TreeIterator implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private C45TreeBuilder builder;
	private HashMap <TreeNode, ArrayList<Fact>> matching_facts;
	private int NUM_NODES;
	
	public C45TreeIterator(C45TreeBuilder my_builder) {
		builder = my_builder;
		matching_facts = new HashMap<TreeNode, ArrayList<Fact>>();
		NUM_NODES = 0;
	}
	
	/* building with the training set (some part of the facts) */
	public DecisionTree build_to_iterate(Class<?> klass, ArrayList<Fact> first_facts) {
		/* gets the facts which the decision tree is eligible */
		//setKlass(klass);
		
		DecisionTree dt = new DecisionTree(klass.getName());
		builder.init_dt(dt, builder.getTarget()); // initialize the decision tree with the target and all domains
		
		ArrayList<String> attrs = new ArrayList<String>(dt.getAttributes());
		Collections.sort(attrs);
		
		builder.add_to_training(first_facts);	/* you must set this when the training called the first time */
		dt.FACTS_READ += first_facts.size();

		//while ()
		TreeNode root = train(dt, first_facts, attrs);
		dt.setRoot(root);
		
		
		return dt;
	}
	
	public TreeNode train(DecisionTree dt, ArrayList<Fact> facts, List<String> attributeNames) {

		builder.FUNC_CALL++;
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
			classifiedNode.setRank((double) facts.size()/(double) builder.getNum_fact_trained());
			classifiedNode.setNumSupporter(facts.size());
			this.NUM_NODES ++;
			classifiedNode.setID(this.NUM_NODES);
			matching_facts.put(classifiedNode, facts);
			
			return classifiedNode;
		}

		/* if there is no attribute left in order to continue */
		if (attributeNames.size() == 0) {
			/* an heuristic of the leaf classification */
			Object winner = stats.getThe_winner_target_class();
			LeafNode noAttributeLeftNode = new LeafNode(dt.getDomain(dt.getTarget()), winner);
			noAttributeLeftNode.setRank((double) stats.getVoteFor(winner)/ (double) builder.getNum_fact_trained());
			noAttributeLeftNode.setNumSupporter(stats.getVoteFor(winner));
			this.NUM_NODES ++;
			noAttributeLeftNode.setID(this.NUM_NODES);
			matching_facts.put(noAttributeLeftNode, facts);
			
			/* we need to know how many guys cannot be classified and who these guys are */
			FactProcessor.splitUnclassifiedFacts(builder.getUnClassifiedFacts(), stats);
			
			return noAttributeLeftNode;
		}

		/* choosing the attribute for the branching starts */
//		String chosenAttribute = Entropy.chooseContAttribute(dt, facts, stats, attributeNames);
//		List<?> categorization = dt.getPossibleValues(chosenAttribute);
		Domain<?> choosenDomain = Entropy.chooseBothAttribute(dt, facts, stats, attributeNames);
		if (Util.RUN)	System.out.println(Util.ntimes("*", 20) + " 1st best attr: "+ choosenDomain.getName());

		TreeNode currentNode = new TreeNode(choosenDomain);
		this.NUM_NODES ++;
		currentNode.setID(this.NUM_NODES);
		matching_facts.put(currentNode, facts);
		
		
		Hashtable<Object, ArrayList<Fact>> filtered_facts = FactProcessor.splitFacts(facts, choosenDomain);

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
				majorityNode.setNumSupporter(0);
				this.NUM_NODES ++;
				majorityNode.setID(this.NUM_NODES);
				matching_facts.put(majorityNode, new ArrayList<Fact>());
				currentNode.putNode(value, majorityNode);
			} else {
				TreeNode newNode = train(dt, filtered_facts.get(value), attributeNames_copy);
//				this.NUM_NODES ++;
//				newNode.setID(this.NUM_NODES);
				currentNode.putNode(value, newNode);
			}
		}

		return currentNode;
	}
	
	/* building with the training set (some part of the facts) */
	public DecisionTree re_build(DecisionTree dt, ArrayList<Fact> new_facts) {
		
		ArrayList<String> attrs = new ArrayList<String>(dt.getAttributes());
		Collections.sort(attrs);
		
		builder.add_to_training(new_facts);
		dt.FACTS_READ += new_facts.size();
		
		//System.out.println(Util.ntimes("\n", 10)+"How facts are u training? "+ training_facts.size());
		//while ()
		TreeNode root = re_train(dt, dt.getRoot(), new_facts, attrs);
		dt.setRoot(root);
		
		return dt;
	}
	
	public TreeNode re_train(DecisionTree dt, TreeNode currentNode, ArrayList<Fact> new_facts, List<String> attributeNames) {

		builder.FUNC_CALL++;
		if (new_facts.size() == 0) {
			throw new RuntimeException("Nothing new to classify, new fact list is empty");
		}
		/* let's get the statistics of the results */
		// List<?> targetValues = dt.getPossibleValues(dt.getTarget());
		//Hashtable<Object, Integer> stats_ = dt.getStatistics(facts, dt.getTarget());// targetValues
		
		//FactTargetDistribution stats = dt.getDistribution(facts);
		ArrayList<Fact> currentFacts = matching_facts.get(currentNode);
		currentFacts.addAll(new_facts);
		FactDistribution stats = new FactDistribution(dt.getDomain(dt.getTarget()));
		stats.calculateDistribution(currentFacts);
		stats.evaluateMajority();

		/* if all elements are classified to the same value */
		if (stats.getNum_supported_target_classes() == 1) {
			LeafNode classifiedNode;
			if (currentNode instanceof LeafNode) {
				classifiedNode = (LeafNode)currentNode;
			} else {
				
				classifiedNode = new LeafNode(dt.getDomain(dt.getTarget()), stats.getThe_winner_target_class());
				classifiedNode.setID(currentNode.getID());
			}
			classifiedNode.setRank((double) currentFacts.size()/(double) builder.getNum_fact_trained());
			classifiedNode.setNumSupporter(currentFacts.size());
			matching_facts.put(classifiedNode, currentFacts);//?
			return classifiedNode;
		}

		/* if there is no attribute left in order to continue */
		if (attributeNames.size() == 0) {
			/* an heuristic of the leaf classification */
			LeafNode noAttributeLeftNode;
			Object winner = stats.getThe_winner_target_class();
			if (currentNode instanceof LeafNode) {
				noAttributeLeftNode = (LeafNode)currentNode;
			} else {
				noAttributeLeftNode = new LeafNode(dt.getDomain(dt.getTarget()), winner);
				noAttributeLeftNode.setID(currentNode.getID());
			}
			noAttributeLeftNode.setRank((double) stats.getVoteFor(winner)/ (double) builder.num_fact_trained);
			noAttributeLeftNode.setNumSupporter(stats.getVoteFor(winner));
			
			/* we need to know how many guys cannot be classified and who these guys are */
			FactProcessor.splitUnclassifiedFacts(builder.getUnClassifiedFacts(), stats);
			matching_facts.put(noAttributeLeftNode, currentFacts);
			return noAttributeLeftNode;
		}

		/* choosing the attribute for the branching starts */
		Domain<?> choosenDomain = Entropy.chooseBothAttribute(dt, currentFacts, stats, attributeNames);
		if (Util.RUN)	System.out.println(Util.ntimes("*", 20) + " 1st best attr: "+ choosenDomain.getName());
		else if (builder.FUNC_CALL % 100 ==0){
			System.out.print(".");
		}
		
		dt.FACTS_READ += new_facts.size();
		System.out.println(Util.ntimes("\n", 2)+"RETRAINING_DOMAINS COMP: current: "+ currentNode.getDomain() + " and choosen "+ choosenDomain + " == " + (currentNode.getDomain().equals(choosenDomain)) );

		if (currentNode.getDomain().equals(choosenDomain)) {
			
			Hashtable<Object, ArrayList<Fact>> filtered_facts = FactProcessor.splitNewFacts(new_facts, choosenDomain);
			/* split the last two class at the same time */
			for (Object value : filtered_facts.keySet()) {
				
				TreeNode childNode = currentNode.getChild(value);
				List<Fact> matching_split = matching_facts.get(childNode);

				ArrayList<String> attributeNames_copy = new ArrayList<String>(
						attributeNames);
				attributeNames_copy.remove(choosenDomain.getName());
				if (childNode == null) {
					System.out.println("the child node is null how come? ");
					/* 
					 * there was no node assigned for that object value
					 * so you need to re_train for the filtered fact set
					 */
					/* ???????
					childNode = train(dt, filtered_facts.get(value), attributeNames_copy);
					currentNode.addNode(value, childNode); */
					System.exit(1);
				}
					
				if (filtered_facts.get(value).isEmpty()) {
					/* there is no new matching guy to that branch 
					 * everything should stay the same 
					 * what can change???
					 */	
					System.out.println("there is no new matching guy to that branch? No change in "+childNode); 
					
				} else {					
					childNode = re_train(dt, childNode, filtered_facts.get(value), attributeNames_copy);
					currentNode.putNode(value, childNode);					
				}				
			}
			
		} else {
			/* there are two ways 
			 * 1. i can call the train function for that set of facts and 
			 * 		add the root to the current place
			 */
			/*
			ArrayList<String> attributeNames_ = new ArrayList<String>(attributeNames);
			//attributeNames_copy.remove(choosenDomain.getName());
			currentNode = train(dt, currentFacts, attributeNames_);
			*/
			
			/* 2. i can split the all facts according to the places i found and continue
			 * 
			 */
			
			/* before re_training on this guy you have to remove the existing fact
			 * lists from the matching fact lists hashmap 
			 */
			
			
			int old_id = currentNode.getID();
			remove_matching_facts(currentNode);
			Hashtable<Object, ArrayList<Fact>> filtered_facts = FactProcessor.splitFacts(currentFacts, choosenDomain);
			currentNode = new TreeNode(choosenDomain);
			currentNode.setID(old_id);
			matching_facts.put(currentNode, currentFacts);
			
			for (Object value : filtered_facts.keySet()) {
				/* split the last two class at the same time */

				ArrayList<String> attributeNames_copy = new ArrayList<String>(attributeNames);
				attributeNames_copy.remove(choosenDomain.getName());

				if (filtered_facts.get(value).isEmpty()) {
					/* majority !!!! */
					LeafNode majorityNode = new LeafNode(dt.getDomain(dt.getTarget()), stats.getThe_winner_target_class());
					majorityNode.setRank(-1.0); // classifying nothing
					majorityNode.setNumSupporter(0);
					this.NUM_NODES ++;
					majorityNode.setID(this.NUM_NODES);
					matching_facts.put(majorityNode, new ArrayList<Fact>());
					currentNode.putNode(value, majorityNode);
				} else {
					TreeNode newNode = train(dt, filtered_facts.get(value), attributeNames_copy);
					currentNode.putNode(value, newNode);
				}
			}
		}

		return currentNode;
	}
	
	private void remove_matching_facts(TreeNode current) {
		matching_facts.remove(current);
		//this.NUM_NODES --; /* SHOULD I */
		for (Object childKey: current.getChildrenKeys()) {
			remove_matching_facts(current.getChild(childKey));
			
		}
	}

	public int getNum_fact_trained() {
		// TODO Auto-generated method stub
		return builder.getNum_fact_trained();
	}


	public List<Integer> test(DecisionTree tree, List<Fact> facts) {
		return builder.test( tree, facts);
	}

	public HashMap<TreeNode, ArrayList<Fact>> getMatchingFacts() {		
		return matching_facts;
		// TODO hide this information better
	}

}
