package org.drools.learner;

import java.util.ArrayList;

import org.drools.learner.builder.SingleTreeTester;
import org.drools.learner.eval.ErrorEstimate;
import org.drools.learner.eval.TestSample;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;
import org.drools.learner.tools.Util;



public class DecisionTreePruner {
	
	private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(DecisionTreePruner.class, SimpleLogger.DEFAULT_LEVEL);
	private static SimpleLogger slog = LoggerFactory.getSysOutLogger(DecisionTreePruner.class, SimpleLogger.DEBUG);
	
	private ErrorEstimate procedure;
	
	private TreeStats best_stats;

	private int num_trees_to_grow;
	
	private double INIT_ALPHA = 0.5d;
	private static final double EPSILON = 0.0d;//0.0000000001;
	
	private int best_id = 0;;
	
	public DecisionTreePruner(ErrorEstimate proc) {
		procedure = proc;
		num_trees_to_grow = procedure.getEstimatorSize();
		//updates = new ArrayList<ArrayList<NodeUpdate>>(num_trees_to_grow);
		
		best_stats = new TreeStats(0.0);//proc.getAlphaEstimate());
	}
	
	public DecisionTreePruner() {
		procedure = null;
		num_trees_to_grow = 1;
		//num_trees_to_grow = procedure.getEstimatorSize();
		//updates = new ArrayList<ArrayList<NodeUpdate>>(num_trees_to_grow);
		procedure = new TestSample(0.2d);
		best_stats = new TreeStats(0.0);//proc.getAlphaEstimate());
	}
	
	public void prun_to_estimate(ArrayList<DecisionTree> dts) {	
		ArrayList<ArrayList<NodeUpdate>> updates = new ArrayList<ArrayList<NodeUpdate>>(num_trees_to_grow);
		ArrayList<ArrayList<TreeStats>> sequence_stats = new ArrayList<ArrayList<TreeStats>>(num_trees_to_grow);
		ArrayList<MinAlphaProc> alpha_procs = new ArrayList<MinAlphaProc>(num_trees_to_grow);
		
		/*
		 * The best tree is selected from this series of trees with the classification error not exceeding 
		 * 	an expected error rate on some test set (cross-validation error), 
		 * which is done at the second stage.
		 */
//		double value_to_select = procedure.getErrorEstimate();
		
		for (int dt_i = 0; dt_i<dts.size(); dt_i++) {
			
			DecisionTree dt= dts.get(dt_i);
			procedure.setTrainingDataSize(dt.getTrainingDataSize());
			dt.setID(dt_i);
			dt.calc_num_node_leaves(dt.getRoot());
			
			

			// dt.getId()
			// dt.calc_num_node_leaves(dt.getRoot()); // this is done in the estimator

			double epsilon = EPSILON * numExtraMisClassIfPrun(dt.getRoot());
			MinAlphaProc alpha_proc = new MinAlphaProc(INIT_ALPHA, epsilon);
			TreeSequenceProc search = new TreeSequenceProc(dt, alpha_proc);//INIT_ALPHA

			search.init_tree();	// alpha_1 = 0.0 
			search.iterate_trees(1);

			//updates.add(tree_sequence);
			updates.add(search.getTreeSequence());
			sequence_stats.add(search.getTreeSequenceStats());
			alpha_procs.add(alpha_proc);

			// sort the found candidates
			//Collections.sort(updates.get(dt.getId()), arg1)
			int sid = 0, best_st=0;
			best_id = 0;
			double best_error = 1.0; 
			System.out.println("Tree id\t Num_leaves\t Cross-validated\t Resubstitution\t Alpha\t");
			ArrayList<TreeStats> trees = sequence_stats.get(dt.getId());
			for (TreeStats st: trees ){
				if (st.getCostEstimation() <= best_error) {
					best_error = st.getCostEstimation();
					best_id = sid;
					best_st = st.iteration_id;
				}
				System.out.println(sid+ "\t " +st.getNum_terminal_nodes()+ "\t "+ st.getCostEstimation() + "\t "+  st.getResubstitution_cost() + "\t "+ st.getAlpha() );
				sid++;				
			}
			System.out.println("BEST "+best_id+ "\t " +trees.get(best_id).getNum_terminal_nodes()+ "\t "+ trees.get(best_id).getCostEstimation() + "\t "+  trees.get(best_id).getResubstitution_cost() + "\t "+ trees.get(best_id).getAlpha() );

			//			System.exit(0);
			//int N = procedure.getTestDataSize(dt_i);
			int N = dt.getTestingDataSize();// procedure.getTestDataSize(dt_i);
			double standart_error_estimate = Math.sqrt((trees.get(best_id).getCostEstimation() * (1- trees.get(best_id).getCostEstimation())/ (double)N));

			int update_id = search.tree_sequence.size()-1;
			int i = trees.get(trees.size()-1).iteration_id;
			int _sid = trees.size()-1;
			while(i > best_st-1) {
				NodeUpdate nu =search.tree_sequence.get(update_id);
				while (i == nu.iteration_id) {
					search.add_back(nu.node_update, nu.old_node);
					update_id --;
					nu = search.tree_sequence.get(update_id);
				}
				_sid--;
				TreeStats st = trees.get(_sid);
				i = st.iteration_id;
				System.out.println(_sid+ "\t " +st.getNum_terminal_nodes()+ "\t "+ st.getCostEstimation() + "\t "+  st.getResubstitution_cost() + "\t "+ st.getAlpha() );
				
			}
//			System.exit(0);
		}
		
	
	}
	
	public DecisionTree prun_to_estimate() {	
		ArrayList<ArrayList<NodeUpdate>> updates = new ArrayList<ArrayList<NodeUpdate>>(num_trees_to_grow);
		ArrayList<ArrayList<TreeStats>> sequence_stats = new ArrayList<ArrayList<TreeStats>>(num_trees_to_grow);
		ArrayList<MinAlphaProc> alpha_procs = new ArrayList<MinAlphaProc>(num_trees_to_grow);
		
		/*
		 * The best tree is selected from this series of trees with the classification error not exceeding 
		 * 	an expected error rate on some test set (cross-validation error), 
		 * which is done at the second stage.
		 */
		double value_to_select = procedure.getErrorEstimate();

		for (int dt_i = 0; dt_i<procedure.getEstimatorSize(); dt_i++) {
			DecisionTree dt= procedure.getEstimator(dt_i);
			
			
			// dt.getId()
			// dt.calc_num_node_leaves(dt.getRoot()); // this is done in the estimator
			
			double epsilon = EPSILON * numExtraMisClassIfPrun(dt.getRoot());
			MinAlphaProc alpha_proc = new MinAlphaProc(INIT_ALPHA, epsilon);
			TreeSequenceProc search = new TreeSequenceProc(dt, alpha_proc);//INIT_ALPHA
			 
			search.init_tree();	// alpha_1 = 0.0 
			search.iterate_trees(1);
			
			//updates.add(tree_sequence);
			updates.add(search.getTreeSequence());
			sequence_stats.add(search.getTreeSequenceStats());
			alpha_procs.add(alpha_proc);
			
			// sort the found candidates
			//Collections.sort(updates.get(dt.getId()), arg1)
			int sid = 0, best_st = 0;
			best_id = 0;
			double best_error = 1.0; 
			System.out.println("Tree id\t Num_leaves\t Cross-validated\t Resubstitution\t Alpha\t");
			ArrayList<TreeStats> trees = sequence_stats.get(dt.getId());
			for (TreeStats st: trees ){
				if (st.getCostEstimation() <= best_error) {
					best_error = st.getCostEstimation();
					best_id = sid;
					best_st = st.iteration_id;
				}
				System.out.println(sid+ "\t " +st.getNum_terminal_nodes()+ "\t "+ st.getCostEstimation() + "\t "+  st.getResubstitution_cost() + "\t "+ st.getAlpha() );
				sid++;				
			}
			System.out.println("BEST "+best_id+ "\t " +trees.get(best_id).getNum_terminal_nodes()+ "\t "+ trees.get(best_id).getCostEstimation() + "\t "+  trees.get(best_id).getResubstitution_cost() + "\t "+ trees.get(best_id).getAlpha() );

			//			System.exit(0);
			int N = procedure.getTestDataSize(dt_i);
			double standart_error_estimate = Math.sqrt((trees.get(best_id).getCostEstimation() * (1- trees.get(best_id).getCostEstimation())/ (double)N));
			
			int update_id = search.tree_sequence.size()-1;
			int i = trees.get(trees.size()-1).iteration_id;
			int _sid = trees.size()-1;
			while(i > best_st) {
				NodeUpdate nu =search.tree_sequence.get(update_id);
				while (i == nu.iteration_id) {
					search.add_back(nu.node_update, nu.old_node);
					update_id --;
					nu = search.tree_sequence.get(update_id);
				}
				_sid--;
				TreeStats st = trees.get(_sid);
				i = st.iteration_id;
				System.out.println(_sid+ "\t " +st.getNum_terminal_nodes()+ "\t "+ st.getCostEstimation() + "\t "+  st.getResubstitution_cost() + "\t "+ st.getAlpha() );
				
			}
//			System.exit(0);
			
			return dt;
		}
		return null;
		
	
	
	}
	
	
//	public DecisionTree prun_to_estimate(DecisionTree dt) {	
//		ArrayList<ArrayList<NodeUpdate>> updates = new ArrayList<ArrayList<NodeUpdate>>(num_trees_to_grow);
//		ArrayList<ArrayList<TreeStats>> sequence_stats = new ArrayList<ArrayList<TreeStats>>(num_trees_to_grow);
//		ArrayList<MinAlphaProc> alpha_procs = new ArrayList<MinAlphaProc>(num_trees_to_grow);
//		
//		/*
//		 * The best tree is selected from this series of trees with the classification error not exceeding 
//		 * 	an expected error rate on some test set (cross-validation error), 
//		 * which is done at the second stage.
//		 */
//		double value_to_select = procedure.getErrorEstimate();
//
//		for (int dt_i = 0; dt_i<procedure.getEstimatorSize(); dt_i++) {
//			DecisionTree dt= procedure.getEstimator(dt_i);
//			
//			
//			// dt.getId()
//			// dt.calc_num_node_leaves(dt.getRoot()); // this is done in the estimator
//			
//			double epsilon = EPSILON * numExtraMisClassIfPrun(dt.getRoot());
//			MinAlphaProc alpha_proc = new MinAlphaProc(INIT_ALPHA, epsilon);
//			TreeSequenceProc search = new TreeSequenceProc(dt, alpha_proc);//INIT_ALPHA
//			 
//			search.init_tree();	// alpha_1 = 0.0 
//			search.iterate_trees(1);
//			
//			//updates.add(tree_sequence);
//			updates.add(search.getTreeSequence());
//			sequence_stats.add(search.getTreeSequenceStats());
//			alpha_procs.add(alpha_proc);
//			
//			// sort the found candidates
//			//Collections.sort(updates.get(dt.getId()), arg1)
//			int sid = 0;
//			best_id = 0;
//			double best_error = 1.0; 
//			System.out.println("Tree id\t Num_leaves\t Cross-validated\t Resubstitution\t Alpha\t");
//			ArrayList<TreeStats> trees = sequence_stats.get(dt.getId());
//			for (TreeStats st: trees ){
//				if (st.getCostEstimation() <= best_error) {
//					best_error = st.getCostEstimation();
//					best_id = sid;
//				}
//				System.out.println(sid+ "\t " +st.getNum_terminal_nodes()+ "\t "+ st.getCostEstimation() + "\t "+  st.getResubstitution_cost() + "\t "+ st.getAlpha() );
//				sid++;				
//			}
//			System.out.println("BEST "+best_id+ "\t " +trees.get(best_id).getNum_terminal_nodes()+ "\t "+ trees.get(best_id).getCostEstimation() + "\t "+  trees.get(best_id).getResubstitution_cost() + "\t "+ trees.get(best_id).getAlpha() );
//
//			//			System.exit(0);
//			int N = procedure.getTestDataSize(dt_i);
//			double standart_error_estimate = Math.sqrt((trees.get(best_id).getCostEstimation() * (1- trees.get(best_id).getCostEstimation())/ (double)N));
//			
//			int update_id = search.tree_sequence.size()-1;
//			for (int i = trees.size()-1; i>=best_id; i--){
//				NodeUpdate nu =search.tree_sequence.get(update_id);
//				while (i == nu.iteration_id) {
//					search.add_back(nu.node_update, nu.old_node);
//					update_id ++;
//					nu = search.tree_sequence.get(update_id);
//				}
//				TreeStats st = trees.get(i);
//				System.out.println(sid+ "\t " +st.getNum_terminal_nodes()+ "\t "+ st.getCostEstimation() + "\t "+  st.getResubstitution_cost() + "\t "+ st.getAlpha() );
//				sid++;				
//			}
//			
//			return dt;
//		}
//		return null;
//		
//	
//	
//	}
	public void select_tree () {
		/*
		 * The best tree is selected from this series of trees with the classification error not exceeding 
		 * an expected error rate on some test set (cross-validation error), 
		 * which is done at the second stage.
		 */
		double value_to_select = procedure.getErrorEstimate();
		

	
	}
	
	public void prun_tree(DecisionTree tree) {
		double epsilon = 0.0000001 * numExtraMisClassIfPrun(tree.getRoot());
		TreeSequenceProc search = new TreeSequenceProc(tree, new AnAlphaProc(best_stats.getAlpha(), epsilon));
		search.iterate_trees(0);
		//search.getTreeSequence()// to go back
		
	}

	private void updateLeaves(TreeNode my_node, int i) {
		my_node.setNumLeaves(my_node.getNumLeaves() + i);
		TreeNode father_node = my_node.getFather();
		if (father_node !=null)
			updateLeaves(father_node, i);
		
	}
	
	// returns the node missclassification cost
	private int R(TreeNode t) {
		return (int) t.getNumMatch() - t.getNumLabeled();
	}
	
	private int numExtraMisClassIfPrun(TreeNode my_node) {
		int num_misclassified = R(my_node); // needs to be cast because of

		if (slog.debug() !=null)
			slog.debug().log(":numExtraMisClassIfPrun:num_misclassified "+ num_misclassified);
		
		int kids_misclassified = 0;
		for(Object key: my_node.getChildrenKeys()) {
			TreeNode child = my_node.getChild(key);
			kids_misclassified += child.getMissClassified();
			if (slog.debug() !=null)
				slog.debug().log(" kid="+ kids_misclassified );
		}
		if (slog.debug() !=null)
			slog.debug().log("\n");
		if (num_misclassified < kids_misclassified) {
			System.out.println("Problem ++++++");
			System.exit(0);
		}
		return num_misclassified - kids_misclassified;
	}
	
	
	public class TreeSequenceProc {
		
		private static final double MAX_ERROR_RATIO = 0.99;
	
		private DecisionTree focus_tree;
		//private double the_alpha;
		private AlphaSelectionProc alpha_proc;
		private ArrayList<NodeUpdate> tree_sequence;
		private ArrayList<TreeStats> tree_sequence_stats;
		
		private TreeStats best_tree_stats;
		public TreeSequenceProc(DecisionTree dt, AlphaSelectionProc cond) { //, double init_alpha
			focus_tree = dt;
			
			
			alpha_proc = cond;
			tree_sequence = new ArrayList<NodeUpdate>();
			tree_sequence_stats = new ArrayList<TreeStats>();
			
			best_tree_stats = new TreeStats(10000000.0d);
			
			NodeUpdate init_tree = new NodeUpdate(dt.getValidationError());
			tree_sequence.add(init_tree);
			
			TreeStats init_tree_stats = new TreeStats(dt.getValidationError());
			init_tree_stats.setResubstitution_cost(dt.getTrainingError());
			init_tree_stats.setAlpha(0.0d);	// dont know
			init_tree_stats.setCost_complexity(-1);	// dont known
//			init_tree_stats.setDecisionTree(dt);
			init_tree_stats.setNum_terminal_nodes(dt.getRoot().getNumLeaves());		
			tree_sequence_stats.add(init_tree_stats);
			
		}
		public DecisionTree getFocusTree() {
			return focus_tree;
		}
		
		public ArrayList<TreeStats> getTreeSequenceStats() {
			return tree_sequence_stats;
		}

		public ArrayList<NodeUpdate> getTreeSequence() {
			return tree_sequence;
		}

		private void init_tree() {
			// initialize the tree to be prunned
			// T_1 is the smallest subtree of T_max satisfying R(T_1) = R(T_max)
			
			ArrayList<TreeNode> last_nonterminals = focus_tree.getAnchestor_of_Leaves(focus_tree.getRoot());
			
			// R(t) <= sum R(t_children) by the proposition 2.14, R(t) node miss-classification cost
			// if R(t) = sum R(t_children) then prune off t_children
			
			boolean tree_changed = false; // (k)
			for (TreeNode t: last_nonterminals) {
//				int R_children = 0;
//				for (Object child_key: t.getChildrenKeys()) {
//					TreeNode child = t.getChild(child_key);
//					R_children += child.getMissClassified();
//				}
//				if (t.getMissClassified() == R_children) {
				if (numExtraMisClassIfPrun(t) == 0) {
					// prune off the candidate node
					tree_changed = true;
					prune_off(t, 0);
				} 
				
			}
			if (tree_changed) {
				TreeStats stats = new TreeStats();
				stats.iteration_id(0);
				update_tree_stats(stats, 0.0d, 0); // error_estimation = stats.getCostEstimation() for the set (cross_validation or test error)
//			tree_sequence_stats.add(stats);
			}
		}
		
		private void iterate_trees(int i) {
			if (slog.debug() !=null)
				slog.debug().log(focus_tree.toString() +"\n");
			// if number of non-terminal nodes in the tree is more than 1 
			// = if there exists at least one non-terminal node different than root
			if (focus_tree.getNumNonTerminalNodes() < 1) {
				if (slog.debug() !=null)
					slog.debug().log(":sequence_trees:TERMINATE-There is no non-terminal nodes? " + focus_tree.getNumNonTerminalNodes() +"\n");
				return; 
			} else if (focus_tree.getNumNonTerminalNodes() == 1 && focus_tree.getRoot().getNumLeaves()<=1) {
				if (slog.debug() !=null)
					slog.debug().log(":sequence_trees:TERMINATE-There is only one node left which is root node " + focus_tree.getNumNonTerminalNodes()+ " and it has only one leaf (pruned)" +focus_tree.getRoot().getNumLeaves()+"\n");
				return; 
			}
			// for each non-leaf subtree
			ArrayList<TreeNode> candidate_nodes = new ArrayList<TreeNode>();
			// to find all candidates with min_alpha value
			//TreeSequenceProc search = new TreeSequenceProc(the_alpha, alpha_proc);//100000.0d, new MinAlphaProc()); 
			
			
			find_candidate_nodes(focus_tree.getRoot(), candidate_nodes);
			//double min_alpha = search.getTheAlpha();
			double min_alpha = getTheAlpha();
			System.out.println("!!!!!!!!!!! dt:"+focus_tree.getId()+" ite "+i+" alpha: "+min_alpha + " num_nodes_found "+candidate_nodes.size());
			
			if (candidate_nodes.size() >0) {
				// The one or more subtrees with that value of will be replaced by leaves
				// instead of getting the first node, have to process all nodes and prune all
				// write a method to prune all
				//TreeNode best_node = candidate_nodes.get(0);
				
				
				int change_in_training_misclass = 0; // (k)
				for (TreeNode candidate_node:candidate_nodes) {
					// prune off the candidate node
					prune_off(candidate_node, i);
					change_in_training_misclass += numExtraMisClassIfPrun(candidate_node); // extra misclassified guys
				
				}
				TreeStats stats = new TreeStats();
				stats.iteration_id(i); 
				update_tree_stats(stats, min_alpha, change_in_training_misclass); // error_estimation = stats.getCostEstimation() for the set (cross_validation or test error)
				
				if (slog.debug() !=null)
					slog.debug().log(":sequence_trees:error "+ stats.getCostEstimation() +"<?"+ procedure.getErrorEstimate() * 1.6 +"\n");

				if (stats.getCostEstimation() < MAX_ERROR_RATIO) { //procedure.getValidationErrorEstimate() * 1.6) {
					// if the error of the tree is not that bad
					
					if (stats.getCostEstimation() < best_tree_stats.getCostEstimation()) {
						best_tree_stats = stats;
						if (slog.debug() !=null)
							slog.debug().log(":sequence_trees:best node updated \n");
			
					}
					//	TODO update alpha_proc by increasing the min_alpha				the_alpha += 10;	
					alpha_proc.init_proc(alpha_proc.getAlpha() + INIT_ALPHA);
					iterate_trees(i+1);
				} else {
					//TODO update.setStopTree();
					return;
				}
			} else {
				if (slog.debug() !=null)
					slog.debug().log(":sequence_trees:no candidate node is found ???? \n");
			}
			
		}

		
		
		private void prune_off(TreeNode candidate_node, int i) {	
			
			LeafNode best_clone = new LeafNode(focus_tree.getTargetDomain(), candidate_node.getLabel());
			best_clone.setRank(	candidate_node.getRank());
			best_clone.setNumMatch(candidate_node.getNumMatch());				//num of matching instances to the leaf node
			best_clone.setNumClassification(candidate_node.getNumLabeled());	//num of (correctly) classified instances at the leaf node

			NodeUpdate update = new NodeUpdate(candidate_node, best_clone);
			//TODO 
			update.iteration_id(i); 
			
			update.setDecisionTree(focus_tree);
			//change_in_training_misclass += numExtraMisClassIfPrun(candidate_node); // extra misclassified guys
			int num_leaves = candidate_node.getNumLeaves();

			TreeNode father_node = candidate_node.getFather();
			if (father_node != null) {
				for(Object key: father_node.getChildrenKeys()) {
					if (father_node.getChild(key).equals(candidate_node)) {
						father_node.putNode(key, best_clone);
						break;
					}
				}
				updateLeaves(father_node, -num_leaves+1);
			} else {
				// this node does not have any father node it is the root node of the tree
				focus_tree.setRoot(best_clone);
			}
			
			//updates.get(dt_0.getId()).add(update);
			tree_sequence.add(update);
			
		}
		public void add_back(TreeNode leaf_node, TreeNode original_node) {
			TreeNode father = leaf_node.getFather();
			if (father == null) {
				focus_tree.setRoot(original_node);
				return;
			}
			else
			for(Object key: father.getChildrenKeys()) {
				if (father.getChild(key).equals(leaf_node)) {
					father.putNode(key, original_node);
					break;
				}
			}
		}
		
		
		private void update_tree_stats(TreeStats stats, double computed_alpha, int change_in_training_error) {
				//TODO put back
		//			ArrayList<InstanceList> sets = procedure.getSets(focus_tree.getId());
//			InstanceList learning_set = sets.get(0);
//			InstanceList validation_set = sets.get(1);

			InstanceList learning_set = focus_tree.getTrain();
			InstanceList validation_set = focus_tree.getTest();
			int num_error = 0;
			SingleTreeTester t= new SingleTreeTester(focus_tree);
			
			//System.out.println(validation_set.getSize());
			for (int index_i = 0; index_i < validation_set.getSize(); index_i++) {
				Integer result = t.test(validation_set.getInstance(index_i));
				if (result == Stats.INCORRECT) {
					num_error ++;
				}
			}
			double percent_error = Util.division(num_error, validation_set.getSize());
//			int _num_error = 0;
//			for (int index_i = 0; index_i < learning_set.getSize(); index_i++) {
//				Integer result = t.test(learning_set.getInstance(index_i));
//				if (result == Stats.INCORRECT) {
//					_num_error ++;
//				}
//			}
//			double _percent_error = Util.division(_num_error, learning_set.getSize());

			int new_num_leaves = focus_tree.getRoot().getNumLeaves();
			
			double new_resubstitution_cost = focus_tree.getTrainingError() + Util.division(change_in_training_error, procedure.getTrainingDataSize(focus_tree.getId()));
			focus_tree.setTrainingError(new_resubstitution_cost);
			
			double cost_complexity = new_resubstitution_cost + computed_alpha * (new_num_leaves);
			
			if (slog.debug() !=null)
				slog.debug().log(":sequence_trees:cost_complexity of selected tree "+ cost_complexity +"\n");
			
			
			stats.setAlpha(computed_alpha);
			stats.setCostEstimation(percent_error);
			//stats.setTrainingCost(_percent_error);
			stats.setResubstitution_cost(new_resubstitution_cost);
			// Cost Complexity = Resubstitution Misclassification Cost + \alpha . Number of terminal nodes
			stats.setCost_complexity(cost_complexity);
			stats.setNum_terminal_nodes(new_num_leaves);
			tree_sequence_stats.add(stats);
		}

		// memory optimized
		public void find_candidate_nodes(TreeNode my_node, ArrayList<TreeNode> nodes) {
			
			if (my_node instanceof LeafNode) {
				
				//leaves.add((LeafNode) my_node);
				return;
			} else {
				// if you prune that one k more instances are misclassified
				
				int k = numExtraMisClassIfPrun(my_node);
				int num_leaves = my_node.getNumLeaves();
				
				if (k==0) {
					if (slog.debug() !=null)
						slog.debug().log(":search_alphas:k == 0\n" );
					
				}
				double num_training_data = (double)procedure.getTrainingDataSize(focus_tree.getId());
				double alpha = ( (double)k / num_training_data) /((double)(num_leaves-1));
				if (slog.debug() !=null)
					slog.debug().log(":search_alphas:alpha "+ alpha+ "/"+alpha_proc.getAlpha()+ " k "+k+" num_leaves "+num_leaves+" all "+ procedure.getTrainingDataSize(focus_tree.getId()) +  "\n");
				
				//the_alpha = alpha_proc.check_node(alpha, the_alpha, my_node, nodes);
				alpha_proc.check_node(alpha, my_node, nodes);
				
				for (Object attributeValue : my_node.getChildrenKeys()) {
					TreeNode child = my_node.getChild(attributeValue);
					find_candidate_nodes(child, nodes);
					//nodes.pop();
				}				
			}
			return;
		}
		
		public double getTheAlpha() {
			return alpha_proc.getAlpha();
		}
		
		
		
		
		private void iterate_trees_(int i) {
			if (slog.debug() !=null)
				slog.debug().log(focus_tree.toString() +"\n");
			// if number of non-terminal nodes in the tree is more than 1 
			// = if there exists at least one non-terminal node different than root
			if (focus_tree.getNumNonTerminalNodes() < 1) {
				if (slog.debug() !=null)
					slog.debug().log(":sequence_trees:TERMINATE-There is no non-terminal nodes? " + focus_tree.getNumNonTerminalNodes() +"\n");
				return; 
			} else if (focus_tree.getNumNonTerminalNodes() == 1 && focus_tree.getRoot().getNumLeaves()<=1) {
				if (slog.debug() !=null)
					slog.debug().log(":sequence_trees:TERMINATE-There is only one node left which is root node " + focus_tree.getNumNonTerminalNodes()+ " and it has only one leaf (pruned)" +focus_tree.getRoot().getNumLeaves()+"\n");
				return; 
			}
			// for each non-leaf subtree
			ArrayList<TreeNode> candidate_nodes = new ArrayList<TreeNode>();
			// to find all candidates with min_alpha value
			//TreeSequenceProc search = new TreeSequenceProc(the_alpha, alpha_proc);//100000.0d, new MinAlphaProc()); 
			
			
			find_candidate_nodes(focus_tree.getRoot(), candidate_nodes);
			//double min_alpha = search.getTheAlpha();
			double min_alpha = getTheAlpha();
			System.out.println("!!!!!!!!!!! dt:"+focus_tree.getId()+" ite "+i+" alpha: "+min_alpha + " num_nodes_found "+candidate_nodes.size());
			
			if (candidate_nodes.size() >0) {
				// The one or more subtrees with that value of will be replaced by leaves
				// instead of getting the first node, have to process all nodes and prune all
				// write a method to prune all
				//TreeNode best_node = candidate_nodes.get(0);
				TreeStats stats = new TreeStats();
				stats.iteration_id(i); 
				
				int change_in_training_misclass = 0; // (k)
				for (TreeNode candidate_node:candidate_nodes) {
					// prune off the candidate node
					//prune_off(candidate_node, i);
					//change_in_training_misclass += numExtraMisClassIfPrun(candidate_node); // extra misclassified guys
/*	*/				
					LeafNode best_clone = new LeafNode(focus_tree.getTargetDomain(), candidate_node.getLabel());
					best_clone.setRank(	candidate_node.getRank());
					best_clone.setNumMatch(candidate_node.getNumMatch());				//num of matching instances to the leaf node
					best_clone.setNumClassification(candidate_node.getNumLabeled());	//num of (correctly) classified instances at the leaf node

					NodeUpdate update = new NodeUpdate(candidate_node, best_clone);
					update.iteration_id(i); 
					
					update.setDecisionTree(focus_tree);
					
					int num_leaves = candidate_node.getNumLeaves();
					TreeNode father_node = candidate_node.getFather();
					if (father_node != null) {
						for(Object key: father_node.getChildrenKeys()) {
							if (father_node.getChild(key).equals(candidate_node)) {
								father_node.putNode(key, best_clone);
								break;
							}
						}
						updateLeaves(father_node, -num_leaves+1);
					} else {
						// this node does not have any father node it is the root node of the tree
						focus_tree.setRoot(best_clone);
					}
					
					//updates.get(dt_0.getId()).add(update);
					tree_sequence.add(update);
/**/					
				}
/**/				
				ArrayList<InstanceList> sets = procedure.getSets(focus_tree.getId());
				InstanceList learning_set = sets.get(0);
				InstanceList validation_set = sets.get(1);
				
				int num_error = 0;
				SingleTreeTester t= new SingleTreeTester(focus_tree);
				
				System.out.println(validation_set.getSize());
				for (int index_i = 0; index_i < validation_set.getSize(); index_i++) {
					Integer result = t.test(validation_set.getInstance(index_i));
					if (result == Stats.INCORRECT) {
						num_error ++;
					}
				}
				double percent_error = Util.division(num_error, validation_set.getSize());
//				int _num_error = 0;
//				for (int index_i = 0; index_i < learning_set.getSize(); index_i++) {
//					Integer result = t.test(learning_set.getInstance(index_i));
//					if (result == Stats.INCORRECT) {
//						_num_error ++;
//					}
//				}
//				double _percent_error = Util.division(_num_error, learning_set.getSize());

				int new_num_leaves = focus_tree.getRoot().getNumLeaves();
				
				double new_resubstitution_cost = focus_tree.getTrainingError() + Util.division(change_in_training_misclass, procedure.getTrainingDataSize(focus_tree.getId()));
				double cost_complexity = new_resubstitution_cost + min_alpha * (new_num_leaves);
				
				
				if (slog.debug() !=null)
					slog.debug().log(":sequence_trees:cost_complexity of selected tree "+ cost_complexity +"\n");
				
				
				stats.setAlpha(min_alpha);
				stats.setCostEstimation(percent_error);
//				stats.setTrainingCost(_percent_error);
				stats.setResubstitution_cost(new_resubstitution_cost);
				// Cost Complexity = Resubstitution Misclassification Cost + \alpha . Number of terminal nodes
				stats.setCost_complexity(cost_complexity);
				stats.setNum_terminal_nodes(new_num_leaves);
				
/**/
				tree_sequence_stats.add(stats);
				if (slog.debug() !=null)
					slog.debug().log(":sequence_trees:error "+ percent_error +"<?"+ procedure.getErrorEstimate() * 1.6 +"\n");

				if (percent_error < MAX_ERROR_RATIO) { //procedure.getValidationErrorEstimate() * 1.6) {
					// if the error of the tree is not that bad
					
					if (percent_error < best_tree_stats.getCostEstimation()) {
						best_tree_stats = stats;
						if (slog.debug() !=null)
							slog.debug().log(":sequence_trees:best node updated \n");
			
					}
					//	TODO update alpha_proc by increasing the min_alpha				the_alpha += 10;	
					alpha_proc.init_proc(alpha_proc.getAlpha() + INIT_ALPHA);
					iterate_trees_(i+1);
				} else {
					//TODO update.setStopTree();
					return;
				}
			} else {
				if (slog.debug() !=null)
					slog.debug().log(":sequence_trees:no candidate node is found ???? \n");
			}
			
		}
	}
	
	
	public interface AlphaSelectionProc {
		public double check_node(double cur_alpha, TreeNode cur_node, ArrayList<TreeNode> nodes);
		public void init_proc(double value);
		public double getAlpha();
	}
	
	public class AnAlphaProc implements AlphaSelectionProc{
		private double an_alpha;
		private double CART_EPSILON;
		public AnAlphaProc(double value, double epsilon) {
			an_alpha = value;
			CART_EPSILON = epsilon;
		}
//		public AnAlphaProc(double value) {
//			an_alpha = value;
//		}
		public void init_proc(double value) {
			// TODO ????
		}
		
		public double check_node(double cur_alpha, TreeNode cur_node, ArrayList<TreeNode> nodes) {
			if (Util.epsilon(cur_alpha - an_alpha, CART_EPSILON)) {
				for(TreeNode parent:nodes) {
					if (isChildOf(cur_node, parent))
						return cur_alpha;// it is not added
				}
				// add this one to the set
				nodes.add(cur_node);
			}
			return cur_alpha;
		}

		public double getAlpha() {
			return an_alpha;
		}
	}
	
	public class MinAlphaProc implements AlphaSelectionProc{
		private double sum_min_alpha, init_min;
		private int num_minimum;
		
		private double CART_EPSILON;
		public MinAlphaProc(double value, double epsilon) {
			init_min = value;
			sum_min_alpha = 0;
			num_minimum = 0;
			CART_EPSILON = epsilon;
		}
		
		public void init_proc(double value) {
			init_min = value;
			sum_min_alpha = 0;
			num_minimum = 0;
		}
		
		public double check_node(double cur_alpha, TreeNode cur_node, ArrayList<TreeNode> nodes) {
			double average_of_min_alphas = getAlpha();
			if (slog.debug() !=null)
				slog.debug().log(":search_alphas:alpha "+ cur_alpha+ "/"+average_of_min_alphas+ " diff "+(cur_alpha - average_of_min_alphas)+"\n");
			
			if (Util.epsilon(cur_alpha - average_of_min_alphas, CART_EPSILON)) {
				// check if the cur_node is a child of any node that has been added to the list before.
				for(TreeNode parent:nodes) {
					if (isChildOf(cur_node, parent))
						return cur_alpha;// if it is the case do not add the node
				}
				// else add this one to the set
				nodes.add(cur_node);
				sum_min_alpha += cur_alpha;
				num_minimum ++;
				return cur_alpha;
			} else if (cur_alpha < average_of_min_alphas) {
				
				nodes.clear(); // can not put a new 'cause then it does not update the global one = new ArrayList<TreeNode>();
				// remove the ones you found and replace with that one
				//tree_sequence.get(dt_id).put(my_node), alpha
				num_minimum = 1;
				sum_min_alpha = cur_alpha;
				nodes.add(cur_node);
				return cur_alpha;
				
			} else {
				
			}
			return sum_min_alpha/num_minimum;
		}

		public double getAlpha() {
			if (num_minimum == 0)
				return init_min;
			else
				return sum_min_alpha/num_minimum;
		}
	}
	public boolean isChildOf(TreeNode cur_node, TreeNode found_node) {
		TreeNode parent = cur_node.getFather();
		if (parent == null)
			return false;
		if (parent.equals(found_node))
			return true;
		else 
			return isChildOf(parent, found_node);
	}
	
	
	
	public class NodeUpdate{
		
		private boolean stopTree;
		
		private DecisionTree tree;
		
		private TreeNode old_node, node_update;
		
		private int iteration_id;
		
		// to set an node update with the worst cross validated error
		public NodeUpdate(double error) {
			//stats = new TreeStats(error);
			//cross_validated_cost = error;
			
			stopTree = false;
			old_node = null;
			node_update = null;
		}
		public NodeUpdate(TreeNode old_n, LeafNode new_n) {
			stopTree = false;
			old_node = old_n;
			node_update = new_n;
		}
		public void iteration_id(int i) {
			iteration_id = i;
		}
		
		public void setDecisionTree(DecisionTree dt_0) {
			tree = dt_0;
		}

		public void setStopTree() {
			stopTree = true;
		}

			
	}
	public class TreeStats{

		private int iteration_id;
		private int num_terminal_nodes;
		private double test_cost;
		private double resubstitution_cost;
		private double cost_complexity;
		private double alpha;
//		private double training_error;
		
		public TreeStats() {
			iteration_id = 0;
		}

		// to set an node update with the worst cross validated error
		public TreeStats(double error) {
			iteration_id = 0;
			test_cost = error;
		}
		
		public void iteration_id(int i) {
			iteration_id = i;
		}		
		public int getNum_terminal_nodes() {
			return num_terminal_nodes;
		}

		public void setNum_terminal_nodes(int num_terminal_nodes) {
			this.num_terminal_nodes = num_terminal_nodes;
		}

		public double getCostEstimation() {
			return test_cost;
		}

		public void setCostEstimation(double valid_cost) {
			this.test_cost = valid_cost;
		}

		public double getResubstitution_cost() {
			return resubstitution_cost;
		}

		public void setResubstitution_cost(double resubstitution_cost) {
			this.resubstitution_cost = resubstitution_cost;
		}

		public double getCost_complexity() {
			return cost_complexity;
		}

		public void setCost_complexity(double cost_complexity) {
			this.cost_complexity = cost_complexity;
		}

		public double getAlpha() {
			return alpha;
		}

		public void setAlpha(double alpha) {
			this.alpha = alpha;
		}
		
//		public void setTrainingCost(double _percent_error) {
//			training_error = _percent_error;
//		}
//		
//		public double getTrainingCost() {
//			return training_error;
//		}
		
	}
	

}
