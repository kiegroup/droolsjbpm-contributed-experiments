package dt;

import dt.memory.Domain;
import dt.tools.Util;


public class LeafNode extends TreeNode {
	//represents leaf nodes with the target value
	private Object targetValue;
	private double rank;

	public LeafNode(Domain<?> targetDomain, Object value){
		super(targetDomain);
		this.targetValue = value;
	}
	
	public void addNode(Object attributeValue, TreeNode node) {
		throw new RuntimeException("cannot add Node to a leaf node");
	}
	
	public void addLeaf(Object attributeValue, String target, Boolean targetValue) {
		throw new RuntimeException("cannot add Leaf to a final node");
	}
	
	public Object getValue() {
		return targetValue;
	}
	
	public double getRank() {
		return rank;
	}

	public void setRank(double rank) {
		this.rank = rank;
	}
	
	public String toString(){
		return "DECISION -> " + targetValue.toString();
	}
	
	public String toString(int depth, StringBuffer buf) {
		buf.append(Util.ntimes("\t",depth+1));
		buf.append("DECISION -> " +targetValue.toString()+"\n");
		return buf.toString();
	}
}
