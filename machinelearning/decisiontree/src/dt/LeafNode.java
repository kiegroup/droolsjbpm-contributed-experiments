package dt;

import java.util.Comparator;

import dt.memory.Domain;
import dt.memory.Fact;
import dt.tools.Util;


public class LeafNode extends TreeNode {
	//represents leaf nodes with the target value
	private Object targetValue;
	private double rank;
	private int num_facts_classified;

	public LeafNode(Domain<?> targetDomain, Object value){
		super(targetDomain);
		this.targetValue = value;
		num_facts_classified = 0;
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
	
	public Integer evaluate(Fact f) {
		
		Domain<?> target_domain = this.getDomain();
		Fact pseudo_f = new Fact();
		try {
			pseudo_f.add(target_domain, this.getValue());
			Comparator<Fact> targetComp = target_domain.factComparator();
			if (targetComp.compare(f, pseudo_f) == 0 ) {
				return Integer.valueOf(1);
			} else {
				return Integer.valueOf(0);
			}
		} catch (Exception e) {
			
			System.out.println(Util.ntimes("\n", 10)+"Unknown situation at leafnode: " + this.getValue() + " @ "+ target_domain);
			e.printStackTrace();
			// Unknown
			System.exit(0);
			return Integer.valueOf(2);
		}
		
		
		
	}
	
	public String toString(){
		return "DECISION -> " + targetValue.toString();
	}
	
	public String toString(int depth, StringBuffer buf) {
		buf.append(Util.ntimes("\t",depth+1));
		buf.append("DECISION -> " +targetValue.toString()+"\n");
		return buf.toString();
	}

	public void setNumSupporter(int size) {
		this.num_facts_classified= size;
		
	}
	
	public int getNum_facts_classified() {
		return this.num_facts_classified;
	}
}
