package org.drools.learner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;


public class DecisionTreeVisitor {
	
	private Stack<NodeValue> nodes;
	
	private HashMap<Integer, Path> paths;
	
	private int num_paths_found;
	
	public DecisionTreeVisitor() {
		/* most important */
		this.nodes = new Stack<NodeValue>();
		this.paths = new HashMap<Integer, Path>();
		num_paths_found = 0;
	}
	
	public void visit(DecisionTree dt) {
		dfs(dt.getRoot(), dt.getId());
//		if (!nodes.empty())
//			nodes.pop(); // POP the root
		
	}
	// memory optimized
	public void visit2(DecisionTree dt) {
		dfs2(dt.getRoot(), dt.getId());
		//if (!nodes.empty())
			nodes.pop(); // POP the root
		
	}
	
	private void dfs(TreeNode my_node, int tree_id) {
		//System.out.println("How many guys there of "+my_node.getDomain().getName() +"  : "+my_node.getDomain().getValues().size());

		if (my_node instanceof LeafNode) {
			NodeValue leaf_value = new NodeValue(my_node);
			leaf_value.setValue(((LeafNode) my_node).getCategory()); //getValue(null));
			nodes.push(leaf_value);
			Path p = spitPath(nodes);
			p.setTreeId(tree_id);
			num_paths_found ++;
			if (!paths.containsKey(p.hashCode())) {
				paths.put(p.hashCode(), p);
			}
			nodes.pop();
			return;
		}
		
		for (Object attributeValue : my_node.getChildrenKeys()) {
			//System.out.println("Domain: "+ my_node.getDomain().getName() + " the value:"+ attributeValue);
			NodeValue node_value = new NodeValue(my_node);
			node_value.setValue(attributeValue);		
			nodes.push(node_value);
			TreeNode child = my_node.getChild(attributeValue);
			dfs(child, tree_id);
			nodes.pop();
		}
		return;
	}
	// memory optimized
	private void dfs2(TreeNode my_node, int tree_id) {
		//System.out.println("How many guys there of "+my_node.getDomain().getName() +"  : "+my_node.getDomain().getValues().size());
		NodeValue node_value = new NodeValue(my_node);
		nodes.push(node_value);
		if (my_node instanceof LeafNode) {
			//NodeValue leaf_value = new NodeValue(my_node);
			node_value.setValue(((LeafNode) my_node).getCategory()); //getValue(null));
			//nodes.push(leaf_value);
			//paths.add(getPath(nodes)); // if i can spit the rule here it would work
			Path p = spitPath(nodes);
			p.setTreeId(tree_id);

			if (!paths.containsKey(p.hashCode())) {
				paths.put(p.hashCode(), p);
			}
			return;
		}
		
		for (Object attributeValue : my_node.getChildrenKeys()) {
			//System.out.println("Domain: "+ my_node.getDomain().getName() + " the value:"+ attributeValue);
			node_value.setValue(attributeValue);			
			TreeNode child = my_node.getChild(attributeValue);
			dfs2(child, tree_id);
			nodes.pop();
		}
		return;
	}

	
	public int getNumPaths() {
		return paths.size();
	}
	
	public int getNumPathsFound() {
		return num_paths_found;
	}

	public Collection<Path> getPathList() {
		return paths.values();
	}
	
	private Path spitPath(Stack<NodeValue> nodes) {
		//, Stack<NodeValue> leaves // if more than one leaf
		Path newPath = new Path(nodes.size());// (nodes, leaves) //if more than one leaf
		//newRule.setObjectClass(this.getRuleClass());
		Iterator<NodeValue> it = nodes.iterator();
		while (it.hasNext()) {

			NodeValue current = it.next();
			if (it.hasNext()) { 
				newPath.addStep(current);
			} else {
				newPath.setStats(current);
			}
		}
		
//		if (slog.debug() != null) {
//			slog.debug().log("\n"+newPath.hashCode()+ " : "+ newPath + "\n");	
//		}
		return newPath;	
	}


	
//	private ArrayList<NodeValue> getPath(Stack<NodeValue> nodes) {
//		//, Stack<NodeValue> leaves // if more than one leaf
//		ArrayList<NodeValue> path = new ArrayList<NodeValue> (nodes.size());
//		Iterator<NodeValue> it = nodes.iterator();
//		while (it.hasNext()) {
//			NodeValue current = it.next();
//			path.add(current);
//		}
//		return path;	
//	}

}
