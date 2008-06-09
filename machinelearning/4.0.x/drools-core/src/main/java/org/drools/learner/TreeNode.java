package org.drools.learner;

import java.util.Collection;
import java.util.Hashtable;

import org.drools.learner.tools.Util;

public class TreeNode {
	
	//private static final Logger log = LoggerFactory.getSysOutLogger(TreeNode.class, LogLevel.ERROR);
	//private static final Logger flog = LoggerFactory.getFileLogger(TreeNode.class);	
	
	
	private Domain domain;
	private Hashtable<Object, TreeNode> children;
	/* TODO explain
	 * rank:
	 * gain:
	 * gainRatio
	 */
	private double rank, gain, gainRatio;
	
	// Number of all instances matching at that node
	private double num_matching_instances;
	
	public TreeNode(Domain domain) {
		this.domain = domain;
		this.children = new Hashtable<Object, TreeNode>();
		
	}
	
	public double getRank() {
		return rank;
	}

	public void setRank(double _rank) {
		this.rank = _rank;
	}
	
	public void setNumMatch(double size) {
		this.num_matching_instances= size;
	}
	
	public double getNumMatch() {
		return this.num_matching_instances;
	}
	
	public Domain getDomain() {
		return domain;
	}
	
	public void putNode(Object attributeCategory, TreeNode node) {
		children.put(attributeCategory, node);
	}
	
	public Collection<Object> getChildrenKeys() {
		return children.keySet();
	}
	
	public TreeNode getChild(Object attr_key) {
		return children.get(attr_key);
	}
	
	public Object voteFor(Instance i) {
		final Object attr_value = i.getAttrValue(this.domain.getFName());
		final Object category = domain.getCategoryOf(attr_value);
		
		final TreeNode my_node = this.getChild(category);
		
		
//		flog.debug(new Object() {
//			public String toString() {
//				
//				StringBuffer sb = new StringBuffer(Util.ntimes("$", 5)+"\nDomain:"+domain.getFName()+"->");
//				for (int idx = 0; idx < domain.getCategoryCount(); idx++) {
//					sb.append(domain.getCategory(idx)+"-");
//				}
//				sb.append(" SEARCHING for = "+ attr_value + " in "+ domain.getFName());
//				sb.append("\n KEYS:");
//				for (Object key: getChildrenKeys()) {
//					sb.append(" "+key +"% "+ getChild(key).getDomain() + " :");
//				}
//				return sb.toString();
//			}
//		});
		return my_node.voteFor(i);
	}
	
	public int hashCode() {
		return domain.hashCode(); // ^ id;//should i add the children
	}
	
	public String toString() {
		return toString(1, 5, new StringBuffer());
	}
	
	public String toString(int tab, int depth, StringBuffer buf) {
		//if (depth > 0 && domain != null) {
		if (domain != null) {
			buf.append(Util.ntimes("\t", tab));
			buf.append(Util.ntimes("***",1));
			buf.append( domain.getFName() + " n.hash:"+this.hashCode()+ " \n");
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
	

}
