package org.drools.learner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.drools.learner.eval.TreeStats;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;


public class DecisionTree {
	
	protected static SimpleLogger slog = LoggerFactory.getSysOutLogger(DecisionTree.class, SimpleLogger.DEFAULT_LEVEL);
	
	//private Class<?> obj_clazz;
	private Schema obj_schema;
	
	/* the target attribute */
	private Domain target;
	
	///* all attributes that will be used during classification */
	private ArrayList<Domain> attrsToClassify;

	private TreeNode root;
	
	// The id of the tree in the forest
	private int id;
	
	private String execution_signature;
	public long FACTS_READ = 0;

//	private double validation_error, training_error;

	private TreeStats error_stats;
	private int num_nonterminal_nodes;
	private int trainingDataSize, testDataSize;
	//private InstanceList train, test;

	public DecisionTree(Schema inst_schema, String _target) {
		this.obj_schema = inst_schema; //inst_schema.getObjectClass();
		this.num_nonterminal_nodes = 0;

		if (slog.debug() != null)
			slog.debug().log("The target attribute: "+ _target+ "\n");
		
		this.target = inst_schema.getAttrDomain(_target);
		
		if (slog.debug() != null)
			slog.debug().log("The target domain: "+ target+ "\n");
		this.attrsToClassify = new ArrayList<Domain>(inst_schema.getAttrNames().size()-1);
		for (String attr_name : inst_schema.getAttrNames()) {
			if (!attr_name.equals(_target)) {
				//flog.debug("Adding the attribute: "+ attr_name);
				if (slog.debug() != null)
					slog.debug().log("Adding the attribute: "+ attr_name+ "\n");
				this.attrsToClassify.add(inst_schema.getAttrDomain(attr_name));
			}
		}
		Collections.sort(this.attrsToClassify, new DomainComparator()); // compare the domains by the name
		error_stats = new TreeStats(0.0d, 0.0d);
	}
	
	public DecisionTree(DecisionTree parentTree, Domain exceptDomain) {
		//this.domainSet = new Hashtable<String, Domain<?>>();
		this.obj_schema = parentTree.getSchema();
		this.target = parentTree.getTargetDomain();
		this.error_stats = parentTree.error_stats;
		
		this.attrsToClassify = new ArrayList<Domain>(parentTree.getAttrDomains().size()-1);
		for (Domain attr_domain : parentTree.getAttrDomains()) {
			if (attr_domain.isNotJustSelected(exceptDomain))
				this.attrsToClassify.add(attr_domain);
		}		
	}
	
	private Schema getSchema() {
		return obj_schema;
	}

	public Class<?> getObjClass() {
		return obj_schema.getObjectClass();
	}
	
	public HashMap<String, ArrayList<Field>> getAttrRelationMap() {
		return obj_schema.getAttrRelationMap();
	}
	
	public int getId() {
		return id;
	}
	
	public void setID(int i) {
		this.id = i;
	}
	
	public Domain getTargetDomain() {
		return target;
	}
	
	public ArrayList<Domain> getAttrDomains() {
		return attrsToClassify;
	}
	
	public TreeNode getRoot() {
		return (root);
	}

	public void setRoot(TreeNode root) {
		this.root = root;
	}
	
	public Object vote(Instance i) {
		return this.getRoot().voteFor(i);
	}
	
	public TreeStats getStats() {
		return error_stats;
	}
	public void changeTestError(double error) {
		error_stats.setErrorEstimation(error_stats.getErrorEstimation() + error);
	}
	
	public void changeTrainError(double error) {
		error_stats.setTrainError(error_stats.getTrainError() + error);
	}
	
	public double getTestError() {
		return error_stats.getErrorEstimation();
	}
	
	public double getTrainError() {
		return error_stats.getTrainError();
	}

//	public void setValidationError(double error) {
//		validation_error = error;
//	}	
//	public double getValidationError() {
//		return validation_error;
//	}
//	
//	public void setTrainingError(double error) {
//		training_error = error;
//	}
//	public double getTrainingError() {
//		return training_error;
//	}
	
	public int calc_num_node_leaves(TreeNode my_node) {
		if (my_node instanceof LeafNode) {

			return 1;
		} else {
			num_nonterminal_nodes ++; // TODO does this really work?
		}
		
		int leaves = 0;
		for (Object child_key: my_node.getChildrenKeys()) {
			/* split the last two class at the same time */
			
			TreeNode child = my_node.getChild(child_key);
			leaves += calc_num_node_leaves(child);
			
		}
		my_node.setNumLeaves(leaves);
		return leaves;
	}
	
	//private ArrayList<LeafNode> leaf_nodes;
	public ArrayList<LeafNode> getLeaves(TreeNode start_node) {
		ArrayList<LeafNode> terminal_nodes = new ArrayList<LeafNode>();
		
		find_leaves(terminal_nodes, start_node);

		return terminal_nodes;
	}
	
	private int find_leaves(ArrayList<LeafNode> terminals, TreeNode my_node) {
		if (my_node instanceof LeafNode) {
			terminals.add((LeafNode)my_node);
			return 1;
		} else {
			num_nonterminal_nodes ++; // TODO does this really work?
		}
		
		int leaves = 0;
		for (Object child_key: my_node.getChildrenKeys()) {
			/* split the last two class at the same time */
			
			TreeNode child = my_node.getChild(child_key);
			leaves += find_leaves(terminals, child);
			
		}
		//my_node.setNumLeaves(leaves);
		return leaves;
	}
	
	public ArrayList<TreeNode> getAnchestor_of_Leaves(TreeNode start_node) {
		ArrayList<LeafNode> terminal_nodes = new ArrayList<LeafNode>();
		
		ArrayList<TreeNode> anc_terminal_nodes = new ArrayList<TreeNode>();
		
		find_leaves(terminal_nodes, anc_terminal_nodes, start_node);

		return anc_terminal_nodes;
	}
	
	private int find_leaves(ArrayList<LeafNode> terminals, ArrayList<TreeNode> anchestors, TreeNode my_node) {
		
		int leaves = 0;
		boolean anchestor_added = false;
		for (Object child_key: my_node.getChildrenKeys()) {
			/* split the last two class at the same time */
			
			TreeNode child = my_node.getChild(child_key);
			if (child instanceof LeafNode) {
				terminals.add((LeafNode)child);
				if (!anchestor_added) {
					num_nonterminal_nodes ++; // TODO does this really work?
					anchestors.add(my_node);
					anchestor_added = true;
				}
				return 1;
			} else {
				leaves += find_leaves(terminals, anchestors, child);
			}
		}
		//my_node.setNumLeaves(leaves);
		return leaves;
	}
	
	public int getNumNonTerminalNodes() {
		return num_nonterminal_nodes;
	}
	public void setSignature(String executionSignature) {
		execution_signature = executionSignature;
	}
	
	public String getSignature() {
		return execution_signature;
	}
	
	@Override
	public String toString() {
		String out = "Facts scanned " + FACTS_READ + "\n";
		return out + root.toString();
	}

	public void setTrainingDataSize(int size) {
		trainingDataSize = size;
	}
	
	public void setTestingDataSize(int size) {
		testDataSize = size;
	}
	
	public int getTrainingDataSize() {
		return trainingDataSize;
	}
	
	public int getTestingDataSize() {
		return testDataSize;
	}
	
//	public void setTrain(InstanceList x) {
//		train = x;
//	}
//	
//	public void setTest(InstanceList x) {
//		 test = x;
//	}
//
//	public InstanceList getTrain() {
//		return train;
//	}
//	
//	public InstanceList getTest() {
//		return test;
//	}


	
}
