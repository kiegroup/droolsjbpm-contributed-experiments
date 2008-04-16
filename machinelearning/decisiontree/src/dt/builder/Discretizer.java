package dt.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import dt.memory.Domain;
import dt.memory.Fact;
import dt.memory.FactAttrDistribution;
import dt.memory.FactTargetDistribution;
import dt.tools.Util;

public class Discretizer {
	private Domain<?> splitDomain;
	private Domain<?> targetDomain;
	
	private String splitAttr;
	private List<Fact> facts;
	private FactTargetDistribution distribution;
	
	private List<SplitPoint> split_indices;
	private int maxDepth = 1;
	private Domain binaryDomain;
	
	
	Discretizer(Domain<?> _targetDomain, List<Fact> _facts, FactTargetDistribution _facts_in_class) {
		this.facts = new ArrayList<Fact>(_facts.size());
		facts.addAll(_facts);
		// sort them
		
		this.targetDomain = _targetDomain;
		this.distribution = _facts_in_class;

	}
	public Fact getSortedFact(int i) {
		return facts.get(i);
	}
	
	public List<Fact> getSortedFacts() {
		return facts;
	}
	
	public void init_binary_domain() {
		Object key0 = Integer.valueOf(0);
		Object key1 = Integer.valueOf(1);
		
		binaryDomain = splitDomain.clone();
		binaryDomain.addPseudoValue(key0);
		binaryDomain.addPseudoValue(key1);
	}
	
	public List<Integer> findSplits(Domain attrDomain) {
		
		this.splitAttr = attrDomain.getName();
		this.splitDomain = attrDomain;
		init_binary_domain();
		Collections.sort(facts, facts.get(0).getDomain(attrDomain.getName()).factComparator());
		
		split_indices = new ArrayList<SplitPoint>();
		SplitPoint last_point = new SplitPoint(facts.size()-1, (Number) facts.get(facts.size()-1).getFieldValue(splitAttr));
		split_indices.add(last_point);
		
		SplitPoint foundPoint = find_a_split(0, facts.size(), getMaxDepth(), distribution, split_indices);
		if (foundPoint != null)
			Collections.sort(split_indices, Discretizer.getSplitComparator());
		
		List<Integer> splits = new ArrayList<Integer>(split_indices.size());
		for (SplitPoint sp: split_indices) {
			splits.add(Integer.valueOf(sp.getIndex()));
			attrDomain.addIndex(sp.getIndex());
			attrDomain.addPseudoValue(sp.getCut_point());
			
		}
		
		return splits;
		
	}
	
	
	private int getMaxDepth() {
		return this.maxDepth ;
	}


	public SplitPoint find_a_split(int begin_index, int end_index, int depth, 
			FactTargetDistribution facts_in_class, 
			List<SplitPoint> split_points) {
		
		if (facts.size() <= 1) {
			if (Util.DEBUG) System.out.println("fact.size <=1 returning 0.0....");
			return null;
		}
		facts_in_class.evaluateMajority();
		if (facts_in_class.getNum_supported_target_classes()==1) {
			if (Util.DEBUG) System.out.println("getNum_supported_target_classes=1 returning 0.0....");
			return null; //?
		}

		if (depth == 0) {
			if (Util.DEBUG) System.out.println("depth == 0  returning 0.0....");
			return null;
		}
		
		String targetAttr = targetDomain.getName();
		List<?> targetValues = targetDomain.getValues();

		if (Util.DEBUG)	System.out.println("Discretizer.find_a_split() attributeToSplit? " + splitAttr);
		int num_split_points = 0;
		
		Fact fact_ = facts.get(begin_index);
		Comparator<Fact> targetComp_ = fact_.getDomain(targetAttr).factComparator();
		Comparator<Fact> attrComp_ = fact_.getDomain(splitAttr).factComparator();
		if (Util.DEBUG) System.out.println("Discretizer.find_a_split() SORTING: "+0+" attr "+splitAttr+ " "+ fact_ );
		for(int index =begin_index+1; index < end_index; index ++) {
			Fact fact_2= facts.get(index);
			//System.out.println("test != " + attrComp_.compare(fact_, fact_2) +" of "+ fact_.getFieldValue(splitAttr)+ " and "+ fact_2.getFieldValue(splitAttr));

			if ( targetComp_.compare(fact_, fact_2)!=0 && attrComp_.compare(fact_, fact_2)!=0) {
				num_split_points++;
				
				if (Util.DEBUG) System.out.println("Discretizer.find_a_split() SORTING: "+index+" attr "+splitAttr+ " "+ fact_2 );
				//break; //you can check if there is at least one  
			}
			fact_ = fact_2;
		}
		if (num_split_points ==0) {
			return null; //there is no possible split point
		}
		
		/* initialize the distribution */
		Object key0 = Integer.valueOf(0);
		Object key1 = Integer.valueOf(1);
//		List<Object> keys = new ArrayList<Object>(2);
//		keys.add(key0);
//		keys.add(key1);
		
		FactAttrDistribution facts_at_attribute = new FactAttrDistribution(getBinaryDomain(), targetDomain);
		facts_at_attribute.setTotal(facts.size());
		facts_at_attribute.setTargetDistForAttr(key1, facts_in_class);
		facts_at_attribute.setSumForAttr(key1, facts.size());
		
		double best_sum = 100000.0;
		SplitPoint bestPoint = split_points.get(split_points.size()-1);

		int split_index =begin_index+1, index = begin_index+1;
		FactAttrDistribution best_distribution = null;
		//Iterator<Fact> f_ite = facts.iterator();
		Iterator<Fact> f_ite = facts.listIterator(begin_index);
		
		Fact f1 = f_ite.next();
		Comparator<Fact> targetComp = f1.getDomain(targetAttr).factComparator();
		Comparator<Fact> attrComp = f1.getDomain(splitAttr).factComparator();
		
		if (Util.DEBUG)	System.out.println("\nentropy.info_cont() SEARCHING: "+begin_index+" attr "+splitAttr+ " "+ f1 );
		while (f_ite.hasNext() && index<end_index) {/* 2. Look for potential cut-points. */

			Fact f2 = f_ite.next();
			
			Object targetKey = f1.getFieldValue(targetAttr);
			
			// System.out.println("My key: "+ targetKey.toString());
			//for (Object attr_key : attr_values)
			
			/* every time it change the place in the distribution */
			facts_at_attribute.change(key0, targetKey, +1);
			facts_at_attribute.change(key1, targetKey, -1);
	
			/*
			 * 2.1 Cut points are points in the sorted list above where the class labels change. 
			 * Eg. if I had five instances with values for the attribute of interest and labels 
			 * (1.0,A), (1.4,A), (1.7, A), (2.0,B), (3.0, B), (7.0, A), then there are only
			 * two cutpoints of interest: 1.85 and 5 (mid-way between the points
			 * where the classes change from A to B or vice versa).
			 */
			
			if ( targetComp.compare(f1, f2)!=0 && attrComp.compare(f1, f2)!=0) {
				
				if (Util.DEBUG) System.out.println("entropy.info_cont() SEARCHING: "+(index)+" attr "+splitAttr+ " "+ f2 );
				
				// the cut point
				Number cp_i = (Number) f1.getFieldValue(splitAttr);
				Number cp_i_next = (Number) f2.getFieldValue(splitAttr);

				Number cut_point = (Double)(cp_i.doubleValue() + cp_i_next.doubleValue()) / 2;
				
				/*
				 * 3. Evaluate your favourite disparity measure 
				 * (info gain, gain ratio, gini coefficient, chi-squared test) on the cut point
				 * and calculate its gain 
				 */
				double sum = Entropy.calc_info_attr(facts_at_attribute);
				//System.out.println("**entropy.info_contattr() FOUND: "+ sum + " best sum "+best_sum + 
				if (Util.DEBUG) System.out.println("  **Try @"+ (index)+" sum "+sum+" best sum "+best_sum + 
				" value ("+ f1.getFieldValue(splitAttr) +"-|"+ cut_point+"|-"+ f2.getFieldValue(splitAttr)+")");
				
				if (sum < best_sum) {
					best_sum = sum;
					
					if (Util.DEBUG) System.out.println(Util.ntimes("?", 10)+"** FOUND: @"+(index)+" target ("+ f1.getFieldValue(targetAttr) +"-|T|-"+ f2.getFieldValue(targetAttr)+")");
					split_index = index;

					bestPoint = new SplitPoint(index-1, cut_point);
					bestPoint.setValue(best_sum);
					
					if (best_distribution != null)	best_distribution.clear();
					best_distribution = new FactAttrDistribution(facts_at_attribute);
				}
			} else {}		
			f1 = f2;
			index++;
		}
		split_points.add(bestPoint);

		double sum1 = 0.0;

		find_a_split(begin_index, split_index, depth-1,
				best_distribution.getAttrFor(key0), split_points);

		double sum2 = 0.0;
		find_a_split(split_index, end_index, depth-1,
				best_distribution.getAttrFor(key1), split_points);
		
		
		if (Util.DEBUG) {
			System.out.println("entropy.info_contattr(BOK_last) split_indices.size "+split_indices.size());
			for(SplitPoint i : split_points)
				System.out.println("all split_indices "+i.getIndex() + " the value "+ i.getCut_point());
		}
		return bestPoint;
	}
	
	private Domain<?> getBinaryDomain() {
		return binaryDomain;
	}
	public static Comparator<SplitPoint> getSplitComparator() {
		return new SplitComparator();
	}
	
	private static class SplitComparator implements Comparator<SplitPoint>{
		public int compare(SplitPoint sp1, SplitPoint sp2) {
			return sp1.getIndex() - sp2.getIndex();
			
		}	
	}

	public class SplitPoint {
		
		private int index;
		private Number cut_point;
		private double value;
		
		SplitPoint(int _index, Number _point) {
			this.index = _index;
			this.cut_point = _point;
		}
		public void setValue(double info_sum) {
			this.value = info_sum;
		}
		public int getIndex() {
			return index;
		}
		public void setIndex(int index) {
			this.index = index;
		}
		public Number getCut_point() {
			return cut_point;
		}
		public void setCut_point(Number cut_point) {
			this.cut_point = cut_point;
		}
		public double getValue() {
			return value;
		}		
		
	}
}
