package org.drools.learner.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Stack;

import org.drools.learner.AttributeValueComparator;
import org.drools.learner.DecisionTree;
import org.drools.learner.LeafNode;
import org.drools.learner.TreeNode;

public class RulePrinter {
	
//	private static final Logger log = LoggerFactory.getSysOutLogger(RulePrinter.class, LogLevel.ERROR);
//	private static final Logger flog = LoggerFactory.getFileLogger(RulePrinter.class, LogLevel.ERROR, Util.log_file); 
//	
	
	public static Reader readRules(DecisionTree learned_dt) {
		
		RulePrinter my_printer = new RulePrinter();	//bocuk.getNum_fact_trained()
		my_printer.setBoundOnNumRules(Util.MAX_NUM_RULES);
		my_printer.printer(learned_dt, Util.SORT_RULES_BY_RANK);
		
		String all_rules = my_printer.write2string();
		if (Util.PRINT_RULES) {
			//my_printer.write2file("examples", "src/rules/examples/" + file);
			my_printer.write2File(all_rules, false, Util.DRL_DIRECTORY+learned_dt.getSignature());
		}
		
		return new StringReader(all_rules);
	}

	private Class<?> rule_clazz;	
	
	private Stack<NodeValue> nodes;
	
	private ArrayList<Rule> rules;
	//private ArrayList<String> ruleText;
	
	
	private int bound_on_num_rules;
	private double num_instances;
	
	//private NumberComparator nComparator;
	
	public RulePrinter() {
		/* most important */
		this.nodes = new Stack<NodeValue>();
		
		this.rules = new ArrayList<Rule>();
		//ruleText = new ArrayList<String>();
		
		this.bound_on_num_rules = -1;
		this.num_instances = -1.0d;
		
		//this.nComparator = new NumberComparator();
	}
	
	public Class<?> getRuleClass() {
		return rule_clazz;
	}
	
	public int getBoundOnNumRules() {
		return bound_on_num_rules;
	}

	public void setBoundOnNumRules(int max_num_rules) {
		this.bound_on_num_rules = max_num_rules;
	}
	
	public double getNumInstances() {
		return this.num_instances;
	}

	public void setNumInstances(int num) {
		this.num_instances = num;
	}
	
	public void printer(DecisionTree dt, boolean sort) {//, PrintStream object
		this.rule_clazz = dt.getObjClass();
		this.num_instances = dt.getRoot().getNumMatch();
		dfs(dt.getRoot());
		
		if (sort)
			Collections.sort(rules, Rule.getRankComparator());
	}
	
	private void dfs(TreeNode my_node) {
		//System.out.println("How many guys there of "+my_node.getDomain().getName() +"  : "+my_node.getDomain().getValues().size());
		
		NodeValue node_value = new NodeValue(my_node);
		nodes.push(node_value);
		
		if (my_node instanceof LeafNode) {
			node_value.setNodeValue(((LeafNode) my_node).getCategory()); //getValue(null));
			//ruleText.add(print(nodes));
			//rule_list.add(spit(nodes));
			// what if more than one condition (more than one leafNode)
			
			Rule newRule = spitRule(nodes);
			newRule.setId(rules.size());
			rules.add(newRule);
			return;
		}
		
		for (Object attributeValue : my_node.getChildrenKeys()) {
			//System.out.println("Domain: "+ my_node.getDomain().getName() + " the value:"+ attributeValue);
			node_value.setNodeValue(attributeValue);		
			TreeNode child = my_node.getChild(attributeValue);
			dfs(child);
			nodes.pop();
		}
		return;
	}
	
	private Rule spitRule(Stack<NodeValue> nodes) {
		//, Stack<NodeValue> leaves // if more than one leaf
		Rule newRule = new Rule(nodes.size());// (nodes, leaves) //if more than one leaf
		newRule.setObjectClass(this.getRuleClass());
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
	
	public void write2File(String toWrite, boolean append, String fileSignature) {//DomainType domain_type, int tree_set  
	
		//String drlFileName = 
		if (!fileSignature.endsWith(".drl"))
			fileSignature += ".drl";
		System.out.println("file:"+ fileSignature);
		File file =new File(fileSignature);
		if (append)
		{
			if(!file.exists())
				//flog.warn("File doesnot exit, creating...");
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(fileSignature, true));
				out.write(toWrite);
				out.close();
				//System.out.println("I wrote "+ toWrite);
			} catch (IOException e) {
				//flog.error("No I couldnot append to the file e:"+ e);
				/* TODO */
			}

		} else {
			if(file.exists()&& (file.length()>0)) {
				file.delete();
				//flog.warn("File exits, deleting...");
			}
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(fileSignature));
				out.write(toWrite);
				out.close();
			} catch (IOException e) {
				//flog.error("No I couldnot create the file e:"+ e);
				/* TODO */
			}
		}
	}
	
	public String write2string(){//String packageName) {
		StringBuffer outputBuffer = new StringBuffer();
		
		String packageName = this.getRuleClass().getPackage().getName();
		
		//log.debug("Package name: "+ packageName);
		
		if (packageName != null)
			outputBuffer.append("package " + packageName +";\n\n");
		
		else {
			//TODO throw exception
			//flog.error("RulePrinter write2string packageName="+packageName);
		}
		
//		flog.debug(new Object() {
//			public String toString() {	
//				String out = "//Num of rules " +rules.size()+"\n //this.getBoundOnNumRules() "+ getBoundOnNumRules();
//				return out;	
//			}
//		});
	
		int total_num_facts=0;
		int i = 0, active_i = 0;
		for( Rule rule: rules) {
			i++;
			//flog.debug("Rule: "+ i);
			if (Util.ONLY_ACTIVE_RULES) {
				if (rule.getRank() >= 0) {
					active_i++;
//					if (Util.DEBUG_RULE_PRINTER) {
//						System.out.println("//Active rules " +i + " write to drl \n"+ rule +"\n");
//					}
					outputBuffer.append(rule.toString());
					outputBuffer.append("\n");
				}

			} else {
				if (rule.getRank() >= 0) {
					active_i++;
				}
//				if (Util.DEBUG_RULE_PRINTER) {
//					System.out.println("//rule " +i + " write to drl \n"+ rule +"\n");
//				}
				outputBuffer.append(rule.toString());
				outputBuffer.append("\n");
			}
			total_num_facts += rule.getNumClassifiedInstances();	
			if (this.getBoundOnNumRules()>0 && i >= this.getBoundOnNumRules())
				break;
		}
		outputBuffer.append("//THE END: Total number of facts correctly classified= "+ total_num_facts + " over "+ this.getNumInstances());
		outputBuffer.append("\n//with " + active_i + " number of rules over "+i+" total number of rules ");
		outputBuffer.append("\n"); // EOF
		
		return outputBuffer.toString();
	}
}

class Rule {
	
	private Class<?> attr_obj;	// object class name
	private ArrayList<NodeValue> conditions;
	private ArrayList<NodeValue>  actions;
	
	private double rank;				 // matching ratio
	private double num_classified_instances;// number of instances matching that rule
	
	private int id;						 // unique id, need a unique name in the drl file
	
	
	Rule(int numCond) {
		conditions = new ArrayList<NodeValue>(numCond);
		actions = new ArrayList<NodeValue>(1);
	}
	
	public void addCondition(NodeValue current) {
		NodeValue nv = new NodeValue(current.getNode());
		nv.setNodeValue(current.getNodeValue());
		conditions.add(nv);
	}
	public void addAction(NodeValue current) {
		NodeValue nv = new NodeValue(current.getNode());
		nv.setNodeValue(current.getNodeValue());
		actions.add(nv);
		this.setRank(((LeafNode)current.getNode()).getRank());
		this.setNumClassifiedInstances(((LeafNode)current.getNode()).getNumClassification());
	}
	public void setObjectClass(Class<?> obj) {
		attr_obj= obj;
	}
	public String getObjectClassName() {
		return attr_obj.getSimpleName();
	}
	
	private int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id= id;
	}
	
	private void setNumClassifiedInstances(double dataSize) {
		this.num_classified_instances = dataSize;	
	}
	public double getNumClassifiedInstances() {
		return this.num_classified_instances;	
	}

	public void setRank(double r) {
		this.rank = r;
	}
	public double getRank() {
		return this.rank;
	}
	
	public String toString() {
		/*		 
		rule "Good Bye"
    		dialect "java"
			when
				$m:Message( status == Message.GOODBYE)
			then
				System.out.println( "[getLabel()] Expected value (" + $c.getLabel() + "), Classified as (False)"); 
		end
		 */
		//"rule \"#"+getId()+" "+decision+" rank:"+rank+"\" \n";
		StringBuffer sb_out = new StringBuffer("");
		String obj_ref = "$"+this.getObjectClassName().substring(0, 1).toLowerCase();

		sb_out.append("\t when");
		sb_out.append("\n\t\t "+obj_ref+":"+this.getObjectClassName() +"("+ "");
		for (NodeValue cond: conditions) {
			sb_out.append(cond.toString() + ", ");
		}
		
		StringBuffer sb_action = new StringBuffer("");
		StringBuffer sb_field = new StringBuffer("");
		StringBuffer sb_expected_value = new StringBuffer("");
		for (NodeValue act: actions) {
			// if the query is on a field then i have to get its value during in the rule 'cause it might be private
			if (!act.getNode().getDomain().isArtificial())
				sb_out.append(obj_ref+ "_"+act.getFName() + " : "+act.getFName()+", ");
			
			sb_action.append(act.getNodeValue() + " , ");
			if (!act.getNode().getDomain().isArtificial())
				sb_field.append(act.getFName() + "");
			else
				sb_field.append(act.getFName() + "()");
			
			
			if (!act.getNode().getDomain().isArtificial())
				sb_expected_value.append(obj_ref+ "_"+act.getFName());//reading the value by the reference of $o_fieldname
			else
				sb_expected_value.append(obj_ref+ "."+act.getFName() + "()");// reading the value from the object $o.function()
			
		}
		sb_action.delete(sb_action.length()-3, sb_action.length()-1);
		sb_out.delete(sb_out.length()-2, sb_out.length()-1);
		sb_out.append(")\n");
		sb_out.append("\t then ");
		sb_out.append("\n\t\t System.out.println(\"["+sb_field.toString()+ "] Expected value (\" + "+  sb_expected_value.toString()+ " + \"), Classified as ("+sb_action.toString()+")\");\n"); 
		if (getRank() <0)
			sb_out.append("\n\t\t System.out.println(\"But no matching fact found = DOES not fire on\");\n");
		
		sb_out.insert(0, "rule \"#"+getId()+" "+sb_field.toString()+ "= "+sb_action.toString()+" classifying "+this.getNumClassifiedInstances()+" num of facts with rank:"+getRank() +"\" \n");
		sb_out.append("end\n");

		return sb_out.toString();
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
	
	//private static final Logger flog = LoggerFactory.getFileLogger(NodeValue.class, LogLevel.ERROR, Util.log_file); 
	
	private TreeNode node;
	private Object nodeValue;	// should it be Attribute???
	
	public NodeValue(TreeNode n) {
		this.node = n;
	}
	
	public String getFName() {
		return node.getDomain().getFName();
	}
	
	public Object getNodeValue() {
		return nodeValue;
	}
	public void setNodeValue(Object category) {
		this.nodeValue = category;
		
	}
	
	public TreeNode getNode() {
		return node;
	}
	
	@SuppressWarnings("unchecked")
	public String toString() {
		String fName = node.getDomain().getFName();
		String value;
		if (node.getDomain().getFType() == String.class)
			value = "\""+nodeValue+ "\""; 
		else
			value = nodeValue + "";
		
		if (node.getDomain().isCategorical())
			return fName + " == "+ value; 
		else {
			
			int size = node.getDomain().getCategoryCount()-1;
			//System.out.println("How many guys there of "+node.getDomain().getName() +" and the value "+nodeValue+" : "+size);
			
			int idx = size;
			if (nodeValue instanceof Number) {
				for (; idx>=0; idx--) {
					Object categoryValue = node.getDomain().getCategory(idx);
					if (nodeValue instanceof Comparable && categoryValue instanceof Comparable) {
						// TODO ask this to daniel???
//						if (Util.DEBUG_RULE_PRINTER) {
//							System.out.println("NodeValue:"+ nodeValue+ " c-"+nodeValue.getClass() +" & category:"+ categoryValue+ " c-"+categoryValue.getClass());
//						}
						if ( AttributeValueComparator.instance.compare(nodeValue, categoryValue) == 0 ) {
							break;
						}
					} else {
						System.out.println("Fuck not comparable NodeValue:"+ nodeValue+ " c-"+nodeValue.getClass() +" & category:"+ categoryValue+ " c-"+categoryValue.getClass());
						System.exit(0);
					}

				}
			} else {
				/* TODO implement the String setting */
				System.out.println("Fuck not number:"+ nodeValue+ " c-"+nodeValue.getClass());
				System.exit(0);

			}
			
			if (idx == 0)
				return fName + " <= "+ value;
			else if (idx == size)
				// if the category is the last one that the rule is domain.name > category(last-1)
				return fName+ " > "+ node.getDomain().getCategory(size-1);
			else {
				//return node.getDomain().getCategory(idx) + " < " + fName+ " <= "+ node.getDomain().getCategory(idx+1);
				// Why drools does not support category(idx) < domain.name <= category(idx+1)
				//flog.debug("value "+ value + "=====?????"+   node.getDomain().getCategory(idx+1));
				
				return fName+ " <= "+ value; // node.getDomain().getCategory(idx+1);
			}
		}
	}
	
}
