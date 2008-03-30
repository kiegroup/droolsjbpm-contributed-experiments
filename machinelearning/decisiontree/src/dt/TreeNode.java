package dt;
import java.util.Hashtable;

import dt.memory.Domain;
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

	public Hashtable<Object, TreeNode> getChildren() {
		return children;
	}

	public void setChildren(Hashtable<Object, TreeNode> children) {
		this.children = children;
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
