package org.drools.learner;

import java.util.ArrayList;

import org.drools.learner.builder.Solution;
import org.drools.learner.builder.SolutionSet;
import org.drools.learner.builder.SingleTreeTester;
//import org.drools.learner.eval.ErrorEstimate;
//import org.drools.learner.eval.TestSample;
import org.drools.learner.eval.PrunerStats;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;
import org.drools.learner.tools.Util;



public class DecisionTreePruner {
	
	private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(DecisionTreePruner.class, SimpleLogger.DEFAULT_LEVEL);
	private static SimpleLogger slog = LoggerFactory.getSysOutLogger(DecisionTreePruner.class, SimpleLogger.DEBUG);
	
//	private PrunerStats best_stats;
	
	private double INIT_ALPHA = 0.5d;
	private static final double EPSILON = 0.0d;//0.0000000001;
	
	
//	private double best_error;
	private Solution best_solution;
	private ArrayList<Solution> pruned_sol;
	
	
	private PrunerStats best_stats_everfound;

	public DecisionTreePruner() {
//		best_error = 1.0;

		best_stats_everfound = new PrunerStats(1.0);//proc.getAlphaEstimate());
		pruned_sol = new ArrayList<Solution>();
	}
	public Solution getBestSolution() {
		return best_solution;
		
	}
	public void prun_to_estimate(SolutionSet sol_set) {			
		/*
		 * The best tree is selected from this series of trees with the classification error not exceeding 
		 * 	an expected error rate on some test set (cross-validation error), 
		 * which is done at the second stage.
		 */
		int i =0;
		for (Solution sol: sol_set.getSolutions()) {
			boolean updated = this.prun_to_estimate(sol);
			if (updated) {
				sol_set.setBestSolutionId(i);
			}
		}
		
		

		
	}
	
	public boolean prun_to_estimate(Solution sol) {			
		/*
		 * The best tree is selected from this series of trees with the classification error not exceeding 
		 * 	an expected error rate on some test set (cross-validation error), 
		 * which is done at the second stage.
		 */
		
		DecisionTree dt = sol.getTree();
		dt.calc_num_node_leaves(dt.getRoot());
		

		double epsilon = EPSILON * numExtraMisClassIfPrun(dt.getRoot());
		MinAlphaProc alpha_proc = new MinAlphaProc(INIT_ALPHA, epsilon);
		TreeSequenceProc search = new TreeSequenceProc(sol, alpha_proc);//INIT_ALPHA

		search.init_tree();	// alpha_1 = 0.0 
		search.iterate_trees(1);
		
//		updates.add(search.getTreeSequence());
//		equence_stats.add(search.getTreeSequenceStats());
//		alpha_procs.add(alpha_proc);

		boolean better_found = false;

		int sid = 0;
		int best_id = 0;
//		double best_error = 1.0d;
		System.out.println("Tree id\t Num_leaves\t Cross-validated\t Resubstitution\t Alpha\t");
		ArrayList<PrunerStats> trees = search._getTreeSequenceStats();
		for (PrunerStats st: trees ){
			if (st.getErrorEstimation() < best_stats_everfound.getErrorEstimation() ||
			   (st.getErrorEstimation() == best_stats_everfound.getErrorEstimation() && 
				st.getNum_terminal_nodes() < best_stats_everfound.getNum_terminal_nodes())) {
				best_stats_everfound.iteration_id(st.iteration_id());
				best_stats_everfound.setErrorEstimation(st.getErrorEstimation());
				best_stats_everfound.setTrainError(st.getTrainError());
				best_stats_everfound.setNum_terminal_nodes(st.getNum_terminal_nodes());
				best_stats_everfound.setAlpha(st.getAlpha());
				best_id = sid;
//				best_st = st.iteration_id();
				better_found = true;
			} else {
				
			}
			System.out.println(sid+ "\t " +st.getNum_terminal_nodes()+ "\t "+ 100.0d*st.getErrorEstimation() + "\t "+  100.0d*st.getTrainError()+ "\t "+ st.getAlpha() );
			sid++;				
		}
		System.out.println("BEST "+best_id+ "\t " +trees.get(best_id).getNum_terminal_nodes()+ "\t "+ trees.get(best_id).getErrorEstimation() + "\t "+  trees.get(best_id).getTrainError() + "\t "+ trees.get(best_id).getAlpha() );
		
		//System.exit(0);
		
		int N = dt.getTestingDataSize();// procedure.getTestDataSize(dt_i);
		double standart_error_estimate = Math.sqrt((trees.get(best_id).getErrorEstimation() * (1- trees.get(best_id).getErrorEstimation())/ (double)N));

		if (better_found) {
			for (int i = search.tree_sequence.size()-1; i>0; i--) {

				NodeUpdate nu = search.tree_sequence.get(i);
				System.out.println(nu.iteration_id +">"+ best_stats_everfound.iteration_id());
				if (nu.iteration_id > best_stats_everfound.iteration_id() ) {
					search.add_back(nu.node_update, nu.old_node);
					SingleTreeTester t = new SingleTreeTester(search.tree_sol.getTree());
					Stats train = t.test(search.tree_sol.getList());
					Stats test = t.test(search.tree_sol.getTestList());
					int num_leaves = search.tree_sol.getTree().getRoot().getNumLeaves();

					System.out.println("Back "+ i+ "\t " +num_leaves+ "\t "+ test.getResult(Stats.INCORRECT)/(double)test.getTotal() + "\t "+  train.getResult(Stats.INCORRECT)/(double)train.getTotal()+ "\t ");
				} else {
					break;
				}
			}
			
			best_solution = search.tree_sol;
			return true;
		}
		return false;

		
	}

	
	public void prun_tree(Solution sol) {
		double epsilon = 0.0000001 * numExtraMisClassIfPrun(sol.getTree().getRoot());
		TreeSequenceProc search = new TreeSequenceProc(sol, new AnAlphaProc(best_stats_everfound.getAlpha(), epsilon));
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
		if (slog.debug() !=null)
			slog.debug().log(":R:num_misclassified "+ t.getNumMatch() + " "+ t.getNumLabeled());
		return (int) (t.getNumMatch() - t.getNumLabeled());
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
			//System.exit(0);
			return 0;
		}
		return num_misclassified - kids_misclassified;
	}
	
	
	public class TreeSequenceProc {
		
		private static final double MAX_ERROR_RATIO = 0.99;
	
		private Solution tree_sol;
		//private double the_alpha;
		private AlphaSelectionProc alpha_proc;
		private ArrayList<NodeUpdate> tree_sequence;
//		private ArrayList<PrunerStats> tree_sequence_stats;
		private ArrayList<PrunerStats> _tree_sequence_stats;
		
		private PrunerStats best_tree_stats;
		public TreeSequenceProc(Solution sol, AlphaSelectionProc cond) { //, double init_alpha
			tree_sol = sol;
			
			
			alpha_proc = cond;
			tree_sequence = new ArrayList<NodeUpdate>();
//			tree_sequence_stats = new ArrayList<PrunerStats>();
			_tree_sequence_stats = new ArrayList<PrunerStats>();
			
			best_tree_stats = new PrunerStats(10000000.0d);
			
			
			System.out.println("From solution:"+tree_sol.getTestError()+ " "+ tree_sol.getTrainError() + " " +tree_sol.getTree().getRoot().getNumLeaves());
			System.out.println("From tree:"+tree_sol.getTree().getTestError()+ " "+ tree_sol.getTree().getTrainError() + " " +tree_sol.getTree().getRoot().getNumLeaves());
			
			
			NodeUpdate init_tree = new NodeUpdate(tree_sol.getTestError());
			tree_sequence.add(init_tree);
			
//			PrunerStats init_tree_stats = new PrunerStats(tree_sol.getTree().getStats());
//			init_tree_stats.setNum_terminal_nodes(tree_sol.getTree().getRoot().getNumLeaves());		
//			init_tree_stats.setAlpha(0.0d);	// dont know
//			init_tree_stats.setCost_complexity(-1);	// dont known
//			tree_sequence_stats.add(init_tree_stats);
			
			PrunerStats _init_tree_stats = new PrunerStats(tree_sol.getTrainError(), tree_sol.getTestError());
			_init_tree_stats.setNum_terminal_nodes(tree_sol.getTree().getRoot().getNumLeaves());		
			_init_tree_stats.setAlpha(0.0d);	// dont know
			_init_tree_stats.setCost_complexity(-1);	// dont known
			_tree_sequence_stats.add(_init_tree_stats);
			
		}
		public DecisionTree getFocusTree() {
			return tree_sol.getTree();
		}
		
//		public ArrayList<PrunerStats> getTreeSequenceStats() {
//			return tree_sequence_stats;
//		}
		
		public ArrayList<PrunerStats> _getTreeSequenceStats() {
			return _tree_sequence_stats;
		}

		public ArrayList<NodeUpdate> getTreeSequence() {
			return tree_sequence;
		}

		private void init_tree() {
			// initialize the tree to be prunned
			// T_1 is the smallest subtree of T_max satisfying R(T_1) = R(T_max)
			
			ArrayList<TreeNode> last_nonterminals = tree_sol.getTree().getAnchestor_of_Leaves(tree_sol.getTree().getRoot());
			
			// R(t) <= sum R(t_children) by the proposition 2.14, R(t) node miss-classification cost
			// if R(t) = sum R(t_children) then prune off t_children
			
			boolean tree_changed = false; // (k)
			for (TreeNode t: last_nonterminals) {
				if (numExtraMisClassIfPrun(t) == 0) {
					// prune off the candidate node
					tree_changed = true;
					prune_off(t, 0);
				} 
				
			}
			if (tree_changed) {
				PrunerStats stats = new PrunerStats();
				stats.iteration_id(0);
				//update_tree_stats(stats, 0.0d, 0); // error_estimation = stats.getCostEstimation() for the set (cross_validation or test error)
				_update_tree_stats(stats, 0.0d, 0);
//			tree_sequence_stats.add(stats);
			}
		}
		
		private void iterate_trees(int i) {
			if (slog.debug() !=null)
				slog.debug().log(tree_sol.getTree().toString() +"\n");
			// if number of non-terminal nodes in the tree is more than 1 
			// = if there exists at least one non-terminal node different than root
			if (tree_sol.getTree().getNumNonTerminalNodes() < 1) {
				if (slog.debug() !=null)
					slog.debug().log(":sequence_trees:TERMINATE-There is no non-terminal nodes? " + tree_sol.getTree().getNumNonTerminalNodes() +"\n");
				return; 
			} else if (tree_sol.getTree().getNumNonTerminalNodes() == 1 && tree_sol.getTree().getRoot().getNumLeaves()<=1) {
				if (slog.debug() !=null)
					slog.debug().log(":sequence_trees:TERMINATE-There is only one node left which is root node " + tree_sol.getTree().getNumNonTerminalNodes()+ " and it has only one leaf (pruned)" +tree_sol.getTree().getRoot().getNumLeaves()+"\n");
				return; 
			}
			// for each non-leaf subtree
			ArrayList<TreeNode> candidate_nodes = new ArrayList<TreeNode>();
			// to find all candidates with min_alpha value
			//TreeSequenceProc search = new TreeSequenceProc(the_alpha, alpha_proc);//100000.0d, new MinAlphaProc()); 
			
			
			find_candidate_nodes(tree_sol.getTree().getRoot(), candidate_nodes);
			//double min_alpha = search.getTheAlpha();
			double min_alpha = getTheAlpha();
			System.out.println("!!!!!!!!!!! dt:"+tree_sol.getTree().getId()+" ite "+i+" alpha: "+min_alpha + " num_nodes_found "+candidate_nodes.size());
			
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
//				PrunerStats stats = new PrunerStats();
//				stats.iteration_id(i); 
//				update_tree_stats(stats, min_alpha, change_in_training_misclass); // error_estimation = stats.getCostEstimation() for the set (cross_validation or test error)
				PrunerStats stats = new PrunerStats();
				stats.iteration_id(i); 
				_update_tree_stats(stats, min_alpha, change_in_training_misclass);
//				if (slog.debug() !=null)
//					slog.debug().log(":sequence_trees:error "+ stats.getCostEstimation() +"<?"+ procedure.getErrorEstimate() * 1.6 +"\n");

				if (stats.getErrorEstimation() < MAX_ERROR_RATIO) { //procedure.getValidationErrorEstimate() * 1.6) {
					// if the error of the tree is not that bad
					
					if (stats.getErrorEstimation() < best_tree_stats.getErrorEstimation()) {
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
			System.out.println(tree_sol.getTree().getTargetDomain() + " " + candidate_node.getLabel());
			LeafNode best_clone = new LeafNode(tree_sol.getTree().getTargetDomain(), candidate_node.getLabel());
			best_clone.setRank(	candidate_node.getRank());
			best_clone.setNumMatch(candidate_node.getNumMatch());				//num of matching instances to the leaf node
			best_clone.setNumClassification(candidate_node.getNumLabeled());	//num of (correctly) classified instances at the leaf node

			NodeUpdate update = new NodeUpdate(candidate_node, best_clone);
			//TODO 
			update.iteration_id(i); 
			
			update.setDecisionTree(tree_sol.getTree());
			//change_in_training_misclass += numExtraMisClassIfPrun(candidate_node); // extra misclassified guys
			int num_leaves = candidate_node.getNumLeaves();

			TreeNode father_node = candidate_node.getFather();
			if (father_node != null) {
				for(Object key: father_node.getChildrenKeys()) {
					if (father_node.getChild(key).equals(candidate_node)) {
						father_node.putNode(key, best_clone);
						best_clone.setFather(father_node);
						break;
					}
				}
				updateLeaves(father_node, -num_leaves+1);
			} else {
				// this node does not have any father node it is the root node of the tree
				tree_sol.getTree().setRoot(best_clone);
			}
			
			//updates.get(dt_0.getId()).add(update);
			tree_sequence.add(update);
			
		}
		public void add_back(TreeNode leaf_node, TreeNode original_node) {
			TreeNode father = leaf_node.getFather();
			if (father == null) {
				tree_sol.getTree().setRoot(original_node);
				return;
			}
			else {
				int num_leaves = original_node.getNumLeaves();
				for(Object key: father.getChildrenKeys()) {
					if (father.getChild(key).equals(leaf_node)) {
						father.putNode(key, original_node);
						original_node.setFather(father);
						updateLeaves(father, num_leaves-1);
						break;
					}
				}
			}
		}
		
		
		
		private void _update_tree_stats(PrunerStats stats, double computed_alpha, int change_in_training_error) {

			InstanceList learning_set = tree_sol.getList();
			InstanceList validation_set = tree_sol.getTestList();
			int num_error = 0;
			SingleTreeTester t= new SingleTreeTester(tree_sol.getTree());
			Stats test_stats = t.test(tree_sol.getTestList());
			
			//System.out.println(validation_set.getSize());
			for (int index_i = 0; index_i < validation_set.getSize(); index_i++) {
				Integer result = t.test(validation_set.getInstance(index_i));
				if (result == Stats.INCORRECT) {
					num_error ++;
				}
			}
			double percent_error = Util.division(num_error, validation_set.getSize());
			System.out.println("From test: "+ test_stats.getResult(Stats.INCORRECT)/(double)test_stats.getTotal() + " x "+ percent_error);
			int new_num_leaves = tree_sol.getTree().getRoot().getNumLeaves();
			
			//double new_resubstitution_cost = tree_sol.getTree().getTrainError() + Util.division(change_in_training_error, learning_set.getSize());
			tree_sol.changeTrainError(change_in_training_error);//setTrainingError(new_resubstitution_cost);
			//tree_sol.setError();
			double cost_complexity = tree_sol.getTrainError() + computed_alpha * (new_num_leaves);
			
			if (slog.debug() !=null)
				slog.debug().log(":sequence_trees:cost_complexity of selected tree "+ cost_complexity +"\n");
			
			
			stats.setAlpha(computed_alpha);
			stats.setErrorEstimation(percent_error);
			stats.setTrainError(tree_sol.getTrainError());
			// Cost Complexity = Resubstitution Misclassification Cost + \alpha . Number of terminal nodes
			stats.setCost_complexity(cost_complexity);
			stats.setNum_terminal_nodes(new_num_leaves);
			_tree_sequence_stats.add(stats);
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
				double num_training_data = (double)tree_sol.getList().getSize();
				double alpha = ( (double)k / num_training_data) /((double)(num_leaves-1));
				if (slog.debug() !=null)
					slog.debug().log(":search_alphas:alpha "+ alpha+ "/"+alpha_proc.getAlpha()+ " k "+k+" num_leaves "+num_leaves+" all "+ tree_sol.getList().getSize() +  "\n");
				
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
	
	

}
