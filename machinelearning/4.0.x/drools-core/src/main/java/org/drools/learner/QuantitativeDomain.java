package org.drools.learner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.drools.learner.eval.SplitPoint;

public class QuantitativeDomain extends Domain{
	
	public static QuantitativeDomain createFromDomain(Domain father) {
		QuantitativeDomain quantitative = new QuantitativeDomain(father.getObjKlass(), father.getFName(), father.getFType());
		//quantitative.setCategories(father.getCategories());
		
		return quantitative;
	}
	
	public QuantitativeDomain cheapClone() {
		QuantitativeDomain qdom = new QuantitativeDomain(super.getObjKlass(), super.getFName(), super.getFType());
		if (super.isCategorical()) {
			System.out.println("QuantitativeDomain.cheapClone() fuck categorical how come QuantitativeDomain cloned " );
			System.exit(0);
		}
		qdom.setCategorical(super.isCategorical());

		//for (int i = 0; i<super.getCategoryCount(); i++)
		qdom.fCategories = this.fCategories;
		qdom.splits = this.splits;
		
		return qdom;
	}
	

	private ArrayList<SplitPoint> splits;
	
	private QuantitativeDomain(Class<?> _obj_klass, String _fname, Class<?> _ftype) {
		super(_obj_klass, _fname, _ftype);
		super.setCategorical(false);	 // by deafault it is true, must set it to false
		this.splits = new ArrayList<SplitPoint>();
		

	}
	public void setFName(String fname) {
		super.setFName(fname);
	}

	
	public boolean isCategorical() {
		return false;
	}
	
//	public int getIndex(int index) {
//		return splits.get(index).getIndex();
//	}
	public SplitPoint getSplit(int index) {
		return splits.get(index);
	}

	public int getNumIndices() {
		return splits.size();
	}
	
	public boolean addSplitPoint(SplitPoint pair) { //int index, Number value) {
		
		int insertion_point = Collections.binarySearch(this.splits, pair, getSplitComparator());
		if (insertion_point >= 0) {
			return false;	/* the pair exists in the list and the return is index of the search key*/
		} else {			/* (-(insertion point) - 1)
							* 	the point at which the key would be inserted into the list
							* 	the index of the first element greater than the key, or list.size()
							*/
			int unfound_insertion_point = -(insertion_point) -1;
			this.splits.add(unfound_insertion_point, pair);
			super.fCategories.add(unfound_insertion_point, pair.getValue());
			return true;
		}
		/* otherwise *
		if (!this.containsIndex(index)) {
			super.splits.add(pair);
		}
		Collections.sort(super.fCategories, nComparator); */
	}
	
	public boolean containsIndex(int value) {
		for (Object op: super.fCategories) {
			SplitPoint sp = (SplitPoint) op;
			if (sp.getIndex() == value)
				return true;
			
		}
		return false;

	}
	
	public static Comparator<SplitPoint> getSplitComparator() {
		return new SplitComparator();
	}
	
	private static class SplitComparator implements Comparator<SplitPoint>{
		public int compare(SplitPoint sp1, SplitPoint sp2) {
			return sp1.getIndex() - sp2.getIndex();
			
		}	
	}

}
