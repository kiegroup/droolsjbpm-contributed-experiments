package dt;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;

import dt.memory.Domain;
import dt.memory.Fact;
import dt.tools.Util;


public class TreeNode implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Domain<?> domain;
	private Hashtable<Object, TreeNode> children;
	private int id;
	
	
	public TreeNode(Domain<?> domain)
	{
		this.domain = domain;
		this.children = new Hashtable<Object, TreeNode>();
	}
	public void setID(int bocuk_id) {
		this.id = bocuk_id;
	}
	
	public int getID() {
		return this.id;
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if (obj instanceof TreeNode)
			return this.hashCode() == ((TreeNode) obj).hashCode();
		else 
			return false;
	}
	
	public int hashCode() {
		return id ; //domain.hashCode() ^ children.hashCode() ^ id;//should i add the children
	}
	
	
	public void putNode(Object attributeValue, TreeNode node) {
		children.put(attributeValue, node);
	}

	public Domain<?> getDomain() {
		return domain;
	}

	public void setDomain(Domain<?> domain) {
		this.domain = domain;
	}

	public Collection<Object> getChildrenKeys() {
		return children.keySet();
	}
	public boolean containChildKey(Object attr_key) {
		return children.keySet().contains(attr_key);
	}
	
	public TreeNode getChild(Object attr_key) {
		return children.get(attr_key);
	}

	public void setChildren(Hashtable<Object, TreeNode> children) {
		this.children = children;
	}
	
	public Integer evaluate(Fact f) {
		
		Domain node_domain = this.getDomain();
		Object attr_value = f.getFieldValue(node_domain.getName());
		//
		try {
			if (node_domain.isPossible(attr_value)) {
				
				TreeNode my_node = this.getChild(node_domain.getClass(attr_value));
				
				if (Util.DEBUG_TEST) {
					String out = "\nDomain:"+node_domain.getName()+"->";
					for (Object value: node_domain.getValues()) {
						out += value+"-";
					}
					out =  Util.ntimes("$", 5) + out + " SEARCHING for = "+ attr_value + " in "+ node_domain.getName();
					
					out += "\n KEYS:";
					for (Object key: this.getChildrenKeys()) {
						
						out += " "+key +"%"+this.getChild(key).getDomain() + " :";
					}
					System.out.print(out);
					System.out.print(" @myclass:"+node_domain.getClass(attr_value));
					
					if (my_node instanceof LeafNode)
						System.out.print(" --> leaf node");
					else
						System.out.print(" --> not a leaf node");
					
					
				}
				Integer x = my_node.evaluate(f);
				if (Util.DEBUG_TEST) {
					System.out.println(" <> TEST RESULT: "+ x);
				}
				return my_node.evaluate(f);
				//return this.getChild(node_domain.getClass(attr_value)).evaluate(f);
			} else {
//			throw new RuntimeException("no child exists for attribute value "
//					+ attr_value);
				// Unknown situation
				System.out.println(Util.ntimes("\n", 1)+"Notpossible situation at treenode: " + attr_value + " @ "+ node_domain);
				return (Integer.valueOf(2));
			}
		} catch (Exception e) {
			System.out.println(Util.ntimes("\n", 1)+"Exception situation at treenode: " + attr_value + " @ "+ node_domain);
			e.printStackTrace();
			System.exit(0);
			return (Integer.valueOf(2));
		}
	}
	public String toString(int depth) {
		return toString(1, 1, new StringBuffer());
	}
	public String toString() {
		return toString(1, 5, new StringBuffer());
	}

	public String toString(int tab, int depth, StringBuffer buf) {
		//if (depth > 0 && domain != null) {
		if (domain != null) {
			buf.append(Util.ntimes("\t", tab));
			buf.append(Util.ntimes("***",1));
			buf.append( domain.getName() + " n.h:"+this.hashCode()+ " \n");
			for (Object attributeValue : children.keySet()) {
				buf.append(Util.ntimes("\t", tab + 1));
				buf.append("+" + attributeValue );
				buf.append("\n");
				TreeNode child = children.get(attributeValue);
				buf.append(child.toString(tab + 1, --depth, new StringBuffer()));
			}
		}
		return buf.toString();
	}
	
	
	public String toString(HashMap <TreeNode, ArrayList<Fact>> matched_facts) {
		return toString(1, 1, new StringBuffer(), matched_facts);
	}

	public String toString(int tab, int depth, StringBuffer buf, HashMap <TreeNode, ArrayList<Fact>> matched_facts) {
	//	System.out.println("fact keys" + _facts.keySet());
		if (domain != null) {
			
//			for (TreeNode obj_node : matched_facts.keySet())
//				System.out.println("* o.id:"+obj_node.getID()+ " o.d:"+obj_node.getDomain()+ " o.h: "+ obj_node.hashCode()+ " => "+matched_facts.get(obj_node) );
//			System.out.println();
			buf.append(Util.ntimes("\t", tab));
			buf.append(Util.ntimes("***",1));
			buf.append( domain.getName() + " n.h:"+this.hashCode()+ " \t");
			//buf.append( domain.getName() + " \t");
			if (matched_facts != null) {
				if (matched_facts.containsKey(this)) {
					ArrayList<Fact> currentFacts = matched_facts.get(this);
					for (Fact f: currentFacts) {
						//buf.append( f.hashCode() + ", ");
						buf.append( f+ ", ");
					}
				}
				else 
					buf.append("n.id: "+ this.getID() + " no facts");
			}
			buf.append(" \n");
			for (Object attributeValue : children.keySet()) {
				buf.append(Util.ntimes("\t", tab + 1));
				buf.append("+" + attributeValue );
				buf.append("\n");
				TreeNode child = children.get(attributeValue);
				buf.append(child.toString(tab + 1, --depth, new StringBuffer(), matched_facts));
			}
		}
		return buf.toString();
	}
}
