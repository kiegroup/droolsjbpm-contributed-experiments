package org.drools.learner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

public class DecisionTreeVisitor {

    private Stack<NodeValue> nodes;

    private HashMap<Integer, Path> paths;

    private int numPathsFound;

    public DecisionTreeVisitor() {
        /* most important */
        this.nodes = new Stack<NodeValue>();
        this.paths = new HashMap<Integer, Path>();
        numPathsFound = 0;
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

    private void dfs(TreeNode mynode, int treeId) {
        //System.out.println("How many guys there of "+my_node.getDomain().getName() +"  : "+my_node.getDomain().getValues().size());

        if (mynode.isLeaf()) {
            NodeValue leafValue = new NodeValue(mynode);
            leafValue.setValue(((LeafNode) mynode).getCategory()); //getValue(null));
            nodes.push(leafValue);
            Path p = spitPath(nodes);
            p.setTreeId(treeId);
            numPathsFound++;
            if (!paths.containsKey(p.hashCode())) {
                paths.put(p.hashCode(), p);
            }
            nodes.pop();
            return;
        }

        for (Object attributeValue : mynode.getChildrenKeys()) {
            //System.out.println("Domain: "+ my_node.getDomain().getName() + " the value:"+ attributeValue);
            NodeValue nodeValue = new NodeValue(mynode);
            nodeValue.setValue(attributeValue);
            nodes.push(nodeValue);
            TreeNode child = mynode.getChild(attributeValue);
            dfs(child, treeId);
            nodes.pop();
        }
        return;
    }

    // memory optimized
    private void dfs2(TreeNode myNode, int treeId) {
        //System.out.println("How many guys there of "+my_node.getDomain().getName() +"  : "+my_node.getDomain().getValues().size());
        NodeValue nodeValue = new NodeValue(myNode);
        nodes.push(nodeValue);
        if (myNode.isLeaf()) {
            //NodeValue leaf_value = new NodeValue(my_node);
            nodeValue.setValue(((LeafNode) myNode).getCategory()); //getValue(null));
            //nodes.push(leaf_value);
            //paths.add(getPath(nodes)); // if i can spit the rule here it would work
            Path p = spitPath(nodes);
            p.setTreeId(treeId);

            if (!paths.containsKey(p.hashCode())) {
                paths.put(p.hashCode(), p);
            }
            return;
        }

        for (Object attributeValue : myNode.getChildrenKeys()) {
            //System.out.println("Domain: "+ my_node.getDomain().getName() + " the value:"+ attributeValue);
            nodeValue.setValue(attributeValue);
            TreeNode child = myNode.getChild(attributeValue);
            dfs2(child, treeId);
            nodes.pop();
        }
        return;
    }

    public int getNumPaths() {
        return paths.size();
    }

    public int getNumPathsFound() {
        return numPathsFound;
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
