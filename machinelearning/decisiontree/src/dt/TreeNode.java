package dt;
import java.util.Collection;
import java.util.Hashtable;

import dt.memory.Domain;
import dt.memory.Fact;
import dt.tools.Util;


public class TreeNode {
	
	private Domain<?> domain;
	private Hashtable<Object, TreeNode> children;
	
	
	public TreeNode(Domain<?> domain)
	{
		this.domain = domain;
		this.children = new Hashtable<Object, TreeNode>();
	}
	
	
	public void addNode(Object attributeValue, TreeNode node) {
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
	
	public String toString() {
		return toString(1, new StringBuffer());
	}

	public String toString(int depth, StringBuffer buf) {
		if (domain != null) {
			buf.append(Util.ntimes("\t", depth));
			buf.append(Util.ntimes("***",1));
			buf.append( domain.getName() + " \n");
			for (Object attributeValue : children.keySet()) {
				buf.append(Util.ntimes("\t", depth + 1));
				buf.append("+" + attributeValue );
				buf.append("\n");
				TreeNode child = children.get(attributeValue);
				buf.append(child.toString(depth + 1, new StringBuffer()));
			}
		}
		return buf.toString();
	}
	
}
