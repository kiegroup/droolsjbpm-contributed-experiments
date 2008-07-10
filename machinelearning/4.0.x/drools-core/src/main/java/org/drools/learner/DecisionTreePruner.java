package org.drools.learner;

import java.util.ArrayList;

import org.drools.learner.eval.Estimator;



public class DecisionTreePruner {
	
	private Estimator procedure;
	
	private ArrayList<ArrayList<NodeUpdate>> updates;
	private int num_trees_to_grow;
	public DecisionTreePruner(Estimator proc) {
		procedure = proc;
		num_trees_to_grow = procedure.getEstimatorSize();
		updates = new ArrayList<ArrayList<NodeUpdate>>(num_trees_to_grow);
	} 	
	
	
	public void global_prun() {
		
		for (DecisionTree dt: procedure.getEstimators()) {
			// dt.getId()
			updates.add(new ArrayList<NodeUpdate>());
			prun(dt);
		}
	
	}
	
	
	
	public void prun(DecisionTree dt_0) {
		
		calc_numleaves(dt_0.getRoot());
		
		// for each non-leaf subtree
		ArrayList<TreeNode> candidate_nodes = new ArrayList<TreeNode>();
		search_alphas(dt_0, dt_0.getRoot(), candidate_nodes);
		System.out.println("alpha: "+min_alpha);
		
		if (candidate_nodes.size() >0) {
			TreeNode best_node = candidate_nodes.get(0);
			LeafNode best_clone = new LeafNode(dt_0.getTargetDomain(), best_node.getLabel());
			best_clone.setRank(	best_node.getRank());
			best_clone.setNumMatch(best_node.getNumMatch());						//num of matching instances to the leaf node
			best_clone.setNumClassification(best_node.getNumLabeled());				//num of (correctly) classified instances at the leaf node

			NodeUpdate update = new NodeUpdate(best_node, best_clone);
			//update.set

			updates.get(dt_0.getId()).add(update);

			TreeNode father_node = best_node.getFather();
			for(Object key: father_node.getChildrenKeys()) {
				if (father_node.getChild(key).equals(best_node)) {
					father_node.putNode(key, best_clone);
					break;
				}
			}
			prun(dt_0);
		}
		
	}

	private double min_alpha = 10.0;
	// memory optimized
	private void search_alphas(DecisionTree dt, TreeNode my_node, ArrayList<TreeNode> nodes) {
		
		if (my_node instanceof LeafNode) {
			
			//leaves.add((LeafNode) my_node);
			return;
		} else {
			// if you prune that one k more instances are misclassified
			int num_misclassified = (int) my_node.getNumMatch() - my_node.getNumLabeled(); // needs to be cast because of
			int k = dt.getValidationError() - num_misclassified;
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
	
	private int calc_numleaves(TreeNode my_node) {
		if (my_node instanceof LeafNode) {

			return 1;
		}
		int leaves = 0;
		for (Object child_key: my_node.getChildrenKeys()) {
			/* split the last two class at the same time */
			
			TreeNode child = my_node.getChild(child_key);
			int leaf = calc_numleaves(child);
			
			leaves += leaf;
			
		}
		my_node.setNumLeaves(leaves);
		return leaves;
	}
	
	public class NodeUpdate{
		
		public NodeUpdate(TreeNode old_n, LeafNode new_n) {
			
		}
		
	}

}
