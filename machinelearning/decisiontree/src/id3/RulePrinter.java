package id3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;

public class RulePrinter {
	
	private ArrayList<String> ruleText;
	//private ArrayList<ArrayList<NodeValue>> rule_list;
	private ArrayList<Rule> rules;
	
	private Stack<NodeValue> nodes;
	
	private Object ruleObject;
	//private RuleComparator rule_comp = new RuleComparator();
	
	public RulePrinter() {
		ruleText = new ArrayList<String>();
		//rule_list = new ArrayList<ArrayList<NodeValue>>();
		rules = new ArrayList<Rule>();
		
		/* most important */
		nodes = new Stack<NodeValue>();
	}
	
	public void printer(DecisionTree dt) {//, PrintStream object
		ruleObject = dt.getName();
		dfs(dt.getRoot());
		
//		int j = 0;
//		for( String rule: ruleText) {
//			j++;
//			System.out.println("Rule " +j + " suggests that \n"+ rule +".\n");
//		}
		
		int i = 0;
		Collections.sort(rules, Rule.getRankComparator());
		for( Rule rule: rules) {
			i++;
			System.out.println("//rule " +i + " write to drl \n"+ rule +"\n");
		}
	}
	
	private void dfs(TreeNode my_node) {
		NodeValue node_value = new NodeValue(my_node);
		nodes.push(node_value);
		
		if (my_node instanceof LeafNode) {
			node_value.setNodeValue(((LeafNode) my_node).getValue());
			ruleText.add(print(nodes));
			//rule_list.add(spit(nodes));
			// what if more than one condition (more than one leafNode)
			
			rules.add(spitRule(nodes));
			return;
		}
		
		Hashtable<Object,TreeNode> children = my_node.getChildren();
		for (Object attributeValue : children.keySet()) {
			//System.out.println("Domain: "+ my_node.getDomain().getName() + " the value:"+ attributeValue);
			node_value.setNodeValue(attributeValue);		
			TreeNode child = children.get(attributeValue);
			dfs(child);
			nodes.pop();
		}
		return;
		
		
			
		
	}
	private ArrayList<NodeValue> spit(Stack<NodeValue> nodes) {
		ArrayList<NodeValue> list_nodes = new ArrayList<NodeValue>(nodes.size());
		Iterator<NodeValue> it = nodes.iterator();

		while (it.hasNext()) {
			
			NodeValue current = it.next();
			list_nodes.add(current);
		}
		return list_nodes;	
	}
	
	private Rule spitRule(Stack<NodeValue> nodes) {
						//, Stack<NodeValue> leaves // if more than one leaf
		Rule newRule = new Rule(nodes.size());// (nodes, leaves) //if more than one leaf
		Iterator<NodeValue> it = nodes.iterator();

		while (it.hasNext()) {
			
			NodeValue current = it.next();
			if (it.hasNext()) { 
				newRule.addCondition(current);
			} else {
				newRule.addAction(current);
			}
		}
		return newRule;	
	}
	
	private String print(Stack<NodeValue> nodes) {
		Iterator<NodeValue> it = nodes.iterator();
		
		String out = "rule \"1 rank:\" \n";
		out += "\t when";
		out += "\t\t "+ruleObject+"Object("+ "";
		while (it.hasNext()) {
			
			NodeValue current = it.next();
			if (it.hasNext()) { 
				out += "" + current.getDomain() + " == "+ current.getNodeValue() +" & " ;
			} else {
				out = out.substring(0, out.length()-2) + ")\n";
				out += "\n\t then ";
				out += "\n\t\t System.out.println(\"Decision (\"" + current.getDomain() + "\") = \""+ current.getNodeValue()+");";
			}
		}
		
		/*
		 
		rule "Good Bye"
    		dialect "java"
			when
				Message( status == Message.GOODBYE, message : message )
			then
				System.out.println( "Goodbye: " + message ); 
		end
		 */
		return out;	
	}
	
}




class Rule {

	private double rank;
	private ArrayList<NodeValue> conditions;
	private ArrayList<NodeValue>  actions;
	
	Rule(int numCond) {
		conditions = new ArrayList<NodeValue>(numCond);
		actions = new ArrayList<NodeValue>(1);
	}

	public double getRank() {
		return rank;
	}

	public void addCondition(NodeValue current) {
		conditions.add(new NodeValue(current.getNode(), current.getNodeValue()));
	}
	public void addAction(NodeValue current) {
		actions.add(new NodeValue(current.getNode(), current.getNodeValue()));
		rank = ((LeafNode)current.getNode()).getRank();
	}
	
	
	public String toString() {
		/*
		 
		rule "Good Bye"
    		dialect "java"
			when
				Message( status == Message.GOODBYE, message : message )
			then
				System.out.println( "Goodbye: " + message ); 
		end
		 */

		String out = "rule \"#x rank:"+rank+"\" \n";
		out += "\t when";
		out += "\n\t\t Object("+ "";
		for (NodeValue cond: conditions) {
			out += cond + " & ";
		}
	
		out = out.substring(0, out.length()-3) + ")\n";
		
		
		String action = "";
		for (NodeValue act: actions) {
			action += act.getNodeValue() + " & ";
		}
		action = action.substring(0, action.length()-3);
		
		out += "\n\t then ";
		out += "\n\t\t System.out.println(\"Decision (\"+" + action + "+\")\");";

		return out;
	}
	

	public static Comparator<Rule> getRankComparator() {
		return new RuleComparator();
	}
	
	private static class RuleComparator implements Comparator<Rule>{
		public int compare(Rule r1, Rule r2) {
			if (r1.getRank() < r2.getRank())
				return -1;
			else if (r1.getRank() > r2.getRank())
				return 1;
			else
				return 0;
		}	
	}
}


class NodeValue {
	
	private TreeNode node;
	private Object nodeValue;
	
	
	NodeValue(TreeNode n) {
		this.node = n;
	}
	
	NodeValue(TreeNode n, Object value) {
		this.node = n;
		this.nodeValue = value;
	}
	public String getDomain() {
		return node.getDomain().getName();
	}
	
	public TreeNode getNode() {
		return node;
	}
	public void setNode(TreeNode node) {
		this.node = node;
	}
	public Object getNodeValue() {
		return nodeValue;
	}
	public void setNodeValue(Object nodeValue) {
		this.nodeValue = nodeValue;
	}
	public String toString() {
		return node.getDomain() + " == "+ nodeValue; 
	}
		
}

