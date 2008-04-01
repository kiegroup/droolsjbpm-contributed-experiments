package dt.tools;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;

import dt.DecisionTree;
import dt.LeafNode;
import dt.TreeNode;
import dt.memory.LiteralDomain;

public class RulePrinter {
	
	private ArrayList<String> ruleText;
	//private ArrayList<ArrayList<NodeValue>> rule_list;
	private ArrayList<Rule> rules;
	
	private Stack<NodeValue> nodes;
	
	private Object ruleObject;
	
	private boolean ONLY_ACTIVE = true;
	private int num_facts; 
	//private RuleComparator rule_comp = new RuleComparator();
	
	
	public RulePrinter(int num_facts) {
		ruleText = new ArrayList<String>();
		//rule_list = new ArrayList<ArrayList<NodeValue>>();
		rules = new ArrayList<Rule>();
		
		/* most important */
		nodes = new Stack<NodeValue>();
		
		this.num_facts = num_facts;
	}
	
	public RulePrinter() {
		ruleText = new ArrayList<String>();
		//rule_list = new ArrayList<ArrayList<NodeValue>>();
		rules = new ArrayList<Rule>();
		
		/* most important */
		nodes = new Stack<NodeValue>();
	}
	
	public void printer(DecisionTree dt, String packageName, String outputFile, boolean sort) {//, PrintStream object
		ruleObject = dt.getName();
		dfs(dt.getRoot());
	
		if (outputFile!=null) {
			if (packageName != null)
				write("package " + packageName +";\n\n", false, outputFile);
			else
				try {
					throw new Exception("The package is not specified");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		if (sort)
			Collections.sort(rules, Rule.getRankComparator());
		
		int total_num_facts=0;
		int i = 0;
		for( Rule rule: rules) {
			i++;
			if (ONLY_ACTIVE) {
				if (rule.getRank() >= 0) {
					System.out.println("//Active rules " +i + " write to drl \n"+ rule +"\n");
					if (outputFile!=null) {
						write(rule.toString(), true, outputFile);
						write("\n", true, outputFile);
					}
				}

			} else {
				System.out.println("//rule " +i + " write to drl \n"+ rule +"\n");
				if (outputFile!=null) {
					write(rule.toString(), true, outputFile);
					write("\n", true, outputFile);
				}
			}
			total_num_facts += rule.getPopularity();
		}
		if (outputFile!=null) {
			write("//THE END: Total number of facts correctly classified= "+ total_num_facts, true, outputFile);
			write("\n", true, outputFile); // EOF
		}
	}
	public Object getRuleObject() {
		return ruleObject;
	}
	
	private void dfs(TreeNode my_node) {
		System.out.println("How many guys there of "+my_node.getDomain().getName() +"  : "+my_node.getDomain().getValues().size());
		
		NodeValue node_value = new NodeValue(my_node);
		nodes.push(node_value);
		
		if (my_node instanceof LeafNode) {
			node_value.setNodeValue(((LeafNode) my_node).getValue());
			ruleText.add(print(nodes));
			//rule_list.add(spit(nodes));
			// what if more than one condition (more than one leafNode)
			
			Rule newRule = spitRule(nodes);
			newRule.setId(rules.size());
			rules.add(newRule);
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
	
	private Rule spitRule(Stack<NodeValue> nodes) {
						//, Stack<NodeValue> leaves // if more than one leaf
		Rule newRule = new Rule(nodes.size());// (nodes, leaves) //if more than one leaf
		newRule.setObject(getRuleObject().toString());
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
	
	//	--------------------------------------------------------------------------------
	// Saves the string
	//--------------------------------------------------------------------------------
	public void write(String toWrite, boolean append, String data)	{  
		//String data = new String("data/results_"+System.currentTimeMillis()+".m");
		File file =new File(data);
		if (append)
		{
			if(!file.exists())
				System.out.println("File doesnot exit, creating...");
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(data, true));
				out.write(toWrite);
				out.close();
				//System.out.println("I wrote "+ toWrite);
			} catch (IOException e) {
				System.out.println("No I cannot write to the file (appending) e:"+ e);
				/* TODO */
			}

		} else {
			if(file.exists()&& (file.length()>0))
				file.delete();
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(data));
				out.write(toWrite);
				out.close();
				System.out.println("I wrote "+ toWrite);
			} catch (IOException e) {
				System.out.println("No I cannot write to the file (creating new file) e:"+ e);
				/* TODO */
			}
		}
	}
}

class Rule {
	private int id;
	private String attr_obj;
	private double rank;
	private double popularity;
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
		popularity = ((LeafNode)current.getNode()).getNum_facts_classified();
	}
	public void setObject(String obj) {
		attr_obj= obj;
	}
	
	public String getObject() {
		return attr_obj;
	}
	
	private int getId() {
		// TODO Auto-generated method stub
		return id;
	}
	
	public void setId(int id) {
		this.id= id;
	}
	
	public double getPopularity() {
		return popularity;
	}

	public void setPopularity(double popularity) {
		this.popularity = popularity;
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
			
		String out = ""; //"rule \"#"+getId()+" "+decision+" rank:"+rank+"\" \n";

		out += "\t when";
		out += "\n\t\t "+getObject() +"("+ "";
		for (NodeValue cond: conditions) {
			out += cond + ", ";
		}
		
		String action = "";
		String decision = "";
		for (NodeValue act: actions) {
			out += act.getDomain() + " : "+act.getDomain()+" , ";
			action += act.getNodeValue() + " , ";
			decision += act.getDomain() + " ";
		}
		action = action.substring(0, action.length()-3);
		out = out.substring(0, out.length()-3) + ")\n";
		
		out += "\t then ";
		out += "\n\t\t System.out.println(\"Decision on "+decision+"= \"+" + decision + "+\": ("+action+")\");\n";
		if (getRank() <0)
			out += "\n\t\t System.out.println(\"But no matching fact found = DOES not fire on\");\n";
		out = "rule \"#"+getId()+" "+decision+ "= "+action+" classifying "+getPopularity()+" num of facts with rank:"+getRank() +"\" \n" + out;
		
		out += "end\n";

		return out;
	}

	public static Comparator<Rule> getRankComparator() {
		return new RuleComparator();
	}
	
	private static class RuleComparator implements Comparator<Rule>{
		// this will sort from best rank to least rank
		public int compare(Rule r1, Rule r2) {
			if (r1.getRank() < r2.getRank())
				return 1; // normally -1
			else if (r1.getRank() > r2.getRank())
				return -1; // normally 1
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
		String value;
		if (node.getDomain() instanceof LiteralDomain)
			value = "\""+nodeValue+ "\""; 
		else
			value = nodeValue + "";
		
		if (node.getDomain().isDiscrete())
			return node.getDomain() + " == "+ value; 
		else {
			int size = node.getDomain().getValues().size();
			System.out.println("How many guys there of "+node.getDomain().getName() +" and the value "+nodeValue+" : "+size);
			if (node.getDomain().getValues().lastIndexOf(nodeValue) == size-1)
				return node.getDomain() + " > "+ node.getDomain().getValues().get(size-2);
			else
				return node.getDomain() + " <= "+ value;
		}
	}
		
}

