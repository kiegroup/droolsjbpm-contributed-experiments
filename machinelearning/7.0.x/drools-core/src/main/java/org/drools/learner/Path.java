package org.drools.learner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

public class Path {
	
	private int code;
	private Class<?> attr_obj;	// object class name
	private ArrayList<NodeValue> conditions;
	
	private NodeValue action;
/* 	action nodeValue has the information of 	
	private double rank;				 // matching ratio
	private double num_classified_instances;// number of instances matching that rule
	private double info_mea;
	*/
	private int treeId; 				// id of the tree that the path belongs 
	
//	private int id;						 // unique id, need a unique name in the drl file
//	
	
	public Path(int numCond) {
		conditions = new ArrayList<NodeValue>(numCond);
		code = 0;
	}

	
	public void addStep(NodeValue current) {
		NodeValue nv = new NodeValue(current.getNode());
		nv.setValue(current.getValue());
		updateHashCode(nv);
		conditions.add(nv);
	}
	
	public void setStats(NodeValue current) {
		action = new NodeValue(current.getNode());
		action.setValue(current.getValue());
		
		updateHashCode(action);
		//this.setNumClassifiedInstances(((LeafNode)current.getNode()).getNumClassification()); // only the leaf node case
	}
	
	public Iterator<NodeValue> getConditionIterator() {
		return conditions.iterator();
	}
	
	public NodeValue getAction() {
		return action;
	}
	
	public void updateHashCode(NodeValue nv) {
		code = code + nv.hashCode() << 6;
	}
	public void setObjectClass(Class<?> obj) {
		attr_obj= obj;
	}
	public String getObjectClassName() {
		return attr_obj.getSimpleName();
	}
	
	public int getTreeId() {
		return treeId;
	}
	
	public void setTreeId(int id) {
		treeId = id;
	}
//	private int getId() {
//		return id;
//	}
//	
//	public void setId(int id) {
//		this.id= id;
//	}
	
//	private void setNumClassifiedInstances(double dataSize) {
//		this.num_classified_instances = dataSize;	
//	}
	public double getNumClassified() {
		return ((LeafNode)this.action.getNode()).getNumMatch();	
	}

//	public void setRank(double r) {
//		this.rank = r;
//	}
	public double getRank() {
		return ((LeafNode)this.action.getNode()).getRank(); //this.rank;
	}
	
	public double getInfoMea() {
		return ((LeafNode)this.action.getNode()).getInfoMea();
	}
	
	public static Comparator<Path> getPathRankComparator() {
		return new PathComparator();
	}
	
	private static class PathComparator implements Comparator<Path>{
		// this will sort from best rank to least rank
		public int compare(Path r1, Path r2) {
			if (r1.getRank() < r2.getRank())
				return 1; // normally -1
			else if (r1.getRank() > r2.getRank())
				return -1; // normally 1
			else
				return 0;
		}	
	}
/* 	WHAT TODO ???	
	public static Comparator<Path> getInfoComparator() {
		return new PathInfoComparator();
	}
	
	private static class PathInfoComparator implements Comparator<Path>{
		// this will sort from best rank to least rank
		public int compare(Path p1, Path p2) {
//			COMPLEXITY++;
//			if (n1.hashCode() == n2.hashCode())
//				return 0;
			double p1_mea = p1.getInfoMea();
			double p2_mea = p2.getInfoMea();
//			switch (INFO_MEA) {
//			case 4:	/* 4 - ranked gain ration/
//				p1_mea = p1.getRank()*p1_mea;
//				p2_mea = p2.getRank()*p2_mea;
//				break;
//			}
			
			//if a node with the same domain exist at the same depth
			//COMPLEXITY++;
			if (p1_mea < p2_mea)
				return 1;	// inverted=>must be -1
			//COMPLEXITY++;
			if (p1_mea > p2_mea)
				return -1;	// inverted=>must be 1
			//COMPLEXITY++;	//else {
			return 0;
		}	
	}
*/
	@Override
	public int hashCode() {
		return code;
	}

	public String toString() {
		StringBuffer out_bf = new StringBuffer();
		
		for (NodeValue c:conditions) {
			//out_bf.append(c.stringCode() +" - ");
			out_bf.append(c +", ");
		}
		//out_bf.append(" => "+action.stringCode());
		out_bf.append(" => "+action);
		return out_bf.toString();
		
	}
}