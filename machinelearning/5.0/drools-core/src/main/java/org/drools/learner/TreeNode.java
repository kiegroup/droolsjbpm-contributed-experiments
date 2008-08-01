package org.drools.learner;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import org.drools.learner.tools.Util;

public class TreeNode {
	
	//private static final Logger log = LoggerFactory.getSysOutLogger(TreeNode.class, LogLevel.ERROR);
	//private static final Logger flog = LoggerFactory.getFileLogger(TreeNode.class);	

	private Domain domain;
	private TreeNode father;
	private Hashtable<Object, TreeNode> children;
	/* TODO explain
	 * rank:
	 * gain:
	 * gainRatio
	 */
	private double rank, infoMea;
	
	// Number of all instances matching at that node
	private double num_matching_instances;
	private Object label;
	private int label_size;
	private int leaves;
	
	private int depth;
	
	public TreeNode(Domain domain) {
		this.father = null;
		this.domain = domain;
		this.children = new Hashtable<Object, TreeNode>();
		
	}
	
	public void setDepth(int d) {
		depth = d;
	}
	
	public int getDepth() {
		return depth;
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
	
	public double getInfoMea() {
		return infoMea;
	}
	
	public void setInfoMea(double mea) {
		this.infoMea = mea;
	}
	public Object getLabel() {
		return label;
	}

	public void setLabel(Object get_winner_class) {
		label = get_winner_class;
	}
	
	public void setNumLabeled(int supportersFor) {
		label_size = supportersFor;
	}
	
	public int getNumLabeled() {
		return label_size;
	}
	
	public int getNumLeaves() {
		return leaves;
	}
	
	public void setNumLeaves(int leaves2) {
		leaves = leaves2;
	}
	
	public void setFather(TreeNode currentNode) {
		father = currentNode;
	}
	public TreeNode getFather() {
		return father;
	}
	public Object voteFor(Instance i) {
		final Object attr_value = i.getAttrValue(this.domain.getFReferenceName());
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
	
	public int getMissClassified() {
		int num_missclassified = 0;
		for(Object key: this.getChildrenKeys()) {
			TreeNode child = this.getChild(key);
			num_missclassified += child.getMissClassified();
		}
		return num_missclassified;
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
			buf.append( domain.getFName() + " n.hash:"+this.hashCode()+" match:" +num_matching_instances+" correct:"+label_size+ " \n");
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
