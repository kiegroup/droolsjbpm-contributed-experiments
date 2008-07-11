package org.drools.learner;

import java.util.ArrayList;

import org.drools.learner.builder.SingleTreeTester;
import org.drools.learner.eval.Estimator;



public class DecisionTreePruner {
	
	private Estimator procedure;
	
	private ArrayList<ArrayList<NodeUpdate>> updates;
	private int num_trees_to_grow;
	
	private NodeUpdate best_update;
	public DecisionTreePruner(Estimator proc) {
		procedure = proc;
		num_trees_to_grow = procedure.getEstimatorSize();
		updates = new ArrayList<ArrayList<NodeUpdate>>(num_trees_to_grow);
		
		best_update = new NodeUpdate(10000000.0d);
	} 	
	
	
	public void prun_to_estimate() {	
		for (DecisionTree dt: procedure.getEstimators()) {
			// dt.getId()
			updates.add(new ArrayList<NodeUpdate>());
			//calc_numleaves(dt.getRoot()); // this is done in the estimator
			prun(dt);
		}
	
	}
	
	private void prun(DecisionTree dt_0) {
		
		// for each non-leaf subtree
		ArrayList<TreeNode> candidate_nodes = new ArrayList<TreeNode>();
		TreeCandidate search = new TreeCandidate();
		search.search_alphas(dt_0, dt_0.getRoot(), candidate_nodes);
		double min_alpha = search.getMinAlpha();
		System.out.println("alpha: "+min_alpha);
		
		if (candidate_nodes.size() >0) {
			TreeNode best_node = candidate_nodes.get(0);
			LeafNode best_clone = new LeafNode(dt_0.getTargetDomain(), best_node.getLabel());
			best_clone.setRank(	best_node.getRank());
			best_clone.setNumMatch(best_node.getNumMatch());						//num of matching instances to the leaf node
			best_clone.setNumClassification(best_node.getNumLabeled());				//num of (correctly) classified instances at the leaf node

			NodeUpdate update = new NodeUpdate(best_node, best_clone);
			//update.set
			update.setAlpha(min_alpha);
			update.setDecisionTree(dt_0);
			int k = numExtraMisClassIfPrun(best_node); // extra misclassified guys
			int num_leaves = best_node.getNumLeaves();
			int new_num_leaves = dt_0.getRoot().getNumLeaves() - num_leaves +1;

			TreeNode father_node = best_node.getFather();
			for(Object key: father_node.getChildrenKeys()) {
				if (father_node.getChild(key).equals(best_node)) {
					father_node.putNode(key, best_clone);
					break;
				}
			}
			updateLeaves(father_node, -num_leaves+1);
			
			ArrayList<InstanceList> sets = procedure.getFold(dt_0.getId());
			//InstanceList learning_set = sets.get(0);
			InstanceList validation_set = sets.get(1);
			
			int error = 0;
			SingleTreeTester t= new SingleTreeTester(dt_0);
			for (int index_i = 0; index_i < validation_set.getSize(); index_i++) {
				Integer result = t.test(validation_set.getInstance(index_i));
				if (result == Stats.INCORRECT) {
					error ++;
				}
			}
			
			
			update.setCross_validated_cost(error);
			int new_resubstitution_cost = dt_0.getTrainingError() + k;
			double cost_complexity = new_resubstitution_cost + min_alpha * (new_num_leaves);

			update.setResubstitution_cost(new_resubstitution_cost);
			// Cost Complexity = Resubstitution Misclassification Cost + \alpha . Number of terminal nodes
			update.setCost_complexity(cost_complexity);
			update.setNum_terminal_nodes(new_num_leaves);

			updates.get(dt_0.getId()).add(update);
			
			
			if (error < procedure.getValidationErrorEstimate() * 1.3) {
				// if the error of the tree is not that bad
				
				if (error < best_update.getCross_validated_cost()) {
					best_update = update;
				}
					
				prun(dt_0);
			} else {
				update.setStopTree();
			}
		}
		
	}

	private void updateLeaves(TreeNode my_node, int i) {
		my_node.setNumLeaves(my_node.getNumLeaves() + i);
		TreeNode father_node = my_node.getFather();
		if (father_node !=null)
			updateLeaves(father_node, i);
		
	}
	
	
	private int numExtraMisClassIfPrun(TreeNode my_node) {
		int num_misclassified = (int) my_node.getNumMatch() - my_node.getNumLabeled(); // needs to be cast because of
		
		int kids_misclassified = 0;
		for(Object key: my_node.getChildrenKeys()) {
			TreeNode child = my_node.getChild(key);
			kids_misclassified += (int) child.getNumMatch() - child.getNumLabeled();
		}
	
		return num_misclassified - kids_misclassified;
	}
	
	public class TreeCandidate {
		private double min_alpha;
		public TreeCandidate() {
			min_alpha = 100000000.0d;
		}
		
		// memory optimized
		private void search_alphas(DecisionTree dt, TreeNode my_node, ArrayList<TreeNode> nodes) {
			
			if (my_node instanceof LeafNode) {
				
				//leaves.add((LeafNode) my_node);
				return;
			} else {
				// if you prune that one k more instances are misclassified
				
				int k = numExtraMisClassIfPrun(my_node);
				int num_leaves = my_node.getNumLeaves();
				
				double alpha = k/(dt.FACTS_READ * (num_leaves-1));
				if (alpha == min_alpha) {
					// add this one to the set
					nodes.add(my_node);
				} else if (alpha < min_alpha) {
					min_alpha = alpha;
					
					nodes = new ArrayList<TreeNode>();
					// remove the ones you found and replace with that one
					//tree_sequence.get(dt_id).put(my_node), alpha
					
					nodes.add(my_node);
					
				} else {
					
				}
				
				for (Object attributeValue : my_node.getChildrenKeys()) {
					TreeNode child = my_node.getChild(attributeValue);
					search_alphas(dt, child, nodes);
					//nodes.pop();
				}
				
			}
			

			return;
		}

		public double getMinAlpha() {
			return min_alpha;
		}
	}
	
	public class NodeUpdate{
		
		private boolean stopTree;
		
		private DecisionTree tree;
		private int num_terminal_nodes;
		private double cross_validated_cost;
		private double resubstitution_cost;
		private double cost_complexity;
		private double alpha;
		
		public NodeUpdate(double error) {
			cross_validated_cost = error;
		}
		public void setDecisionTree(DecisionTree dt_0) {
			tree = dt_0;
		}
		public NodeUpdate(TreeNode old_n, LeafNode new_n) {
			stopTree = false;
		}

		public void setStopTree() {
			stopTree = true;
		}

		public int getNum_terminal_nodes() {
			return num_terminal_nodes;
		}

		public void setNum_terminal_nodes(int num_terminal_nodes) {
			this.num_terminal_nodes = num_terminal_nodes;
		}

		public double getCross_validated_cost() {
			return cross_validated_cost;
		}

		public void setCross_validated_cost(double cross_validated_cost) {
			this.cross_validated_cost = cross_validated_cost;
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
		
		
	}

}
