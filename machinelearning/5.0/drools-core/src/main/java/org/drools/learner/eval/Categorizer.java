package org.drools.learner.eval;

import java.util.ArrayList;
import java.util.Collections;

import org.drools.learner.Domain;
import org.drools.learner.Instance;
import org.drools.learner.InstanceComparator;
import org.drools.learner.QuantitativeDomain;
import org.drools.learner.eval.heuristic.Entropy;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;
import org.drools.learner.tools.Util;


public class Categorizer {
	
	private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(Categorizer.class, SimpleLogger.DEFAULT_LEVEL);
	
	private ArrayList<Instance> data;
	private QuantitativeDomain splitDomain;
	private Domain targetDomain;
	
	private InstanceComparator targetComp, attrComp;
	
	private ClassDistribution distribution;
	
	
	/* TODO make it singleton */	
	private static final Object key0 = Integer.valueOf(0);
	private static final Object key1 = Integer.valueOf(1);
	private static final Domain binaryDomain = init_binary_domain();

	
	
	/* TODO put this into globals file */
	private int maxDepth = 1;
	
	public Categorizer(InstDistribution _data_in_class) {
		//List<Instance> _instancese
		this.data = new ArrayList<Instance>((int)_data_in_class.getSum());
		this.distribution = _data_in_class;
		this.targetDomain = _data_in_class.getClassDomain();
		
		for (int category = 0; category<targetDomain.getCategoryCount(); category++) {
			Object targetCategory = targetDomain.getCategory(category); 
			data.addAll(_data_in_class.getSupportersFor(targetCategory));
		}
		
		this.targetComp = new InstanceComparator(targetDomain.getFReferenceName());
	}
	
	public void findSplits(QuantitativeDomain attrDomain) {
		this.splitDomain = attrDomain;
		this.attrComp = new InstanceComparator(attrDomain.getFReferenceName());
		
		init_binary_domain();
		
		// CATEGORIZATION 1. sort the values 
		Collections.sort(this.data, this.attrComp);
		
		int last_index = this.data.size()-1;
		Object last_value = data.get(last_index).getAttrValue(this.splitDomain.getFReferenceName());
		this.splitDomain.addSplitPoint(new SplitPoint(last_index, last_value));	
		
		find_a_split(0, this.data.size(), this.maxDepth, this.distribution);
		return;
	}
	
	public static Domain init_binary_domain() {
		//if (binaryDomain == null) {
			Domain bD = new Domain(Object.class, "binary", Integer.class); //splitDomain.cheapClone();
			bD.addCategory(key0);	//addSplitPoint(new SplitPoint(0, key0));
			bD.addCategory(key1); //addSplitPoint(new SplitPoint(1, key1)); 
		//}
		return bD;
	}

	public ArrayList<Instance> getSortedInstances() {
		return data;
	}
	
	public QuantitativeDomain getSplitDomain() {
		return this.splitDomain;
	}
	
	private double find_a_split(int begin_index, int size, int depth, 
								ClassDistribution facts_in_class) {

		if (flog.debug() !=null)
			flog.debug().log("./n");
		if (data.size() <= 1 || (size-begin_index)<2) {
			if (flog.warn() !=null)
				flog.warn().log("fact.size <=1 returning 0.0....");
			return 0.0;
		}
//		if (facts_in_class.getSum() == 0) {
//			return 0.0;	// there is no one in it
//		}
		facts_in_class.evaluateMajority();		
		if (facts_in_class.get_num_ideas()==1) {
			if (flog.warn() !=null)
				flog.warn().log("getNum_supported_target_classes=1 returning 0.0....");
			return 0.0; //?
		}

		if (depth == 0) {
			if (flog.warn() !=null)
				flog.warn().log("depth == 0  returning 0.0....");
			return 0.0;
		}
		
		/* initialize the distribution */		
		CondClassDistribution instances_by_attr = new CondClassDistribution(binaryDomain, this.targetDomain);
		instances_by_attr.setDistForAttrValue(key1, facts_in_class);
		instances_by_attr.setTotal(facts_in_class.getSum());
		
		double best_sum = 100000.0;

		int num_split_points = 0, split_index = begin_index+1;	
		int last_index = size-1;
		Object last_value = data.get(last_index).getAttrValue(this.splitDomain.getFReferenceName());
		SplitPoint bestPoint = new SplitPoint(last_index, last_value);	
		CondClassDistribution best_distribution = null;//instances_by_attr;
		Instance i1 = data.get(begin_index), i2; 
		
		if (flog.debug() !=null)
			flog.debug().log("\nentropy.info_cont() SEARCHING: "+begin_index+ " until "+size+" attr "+this.splitDomain.getFName()+ " "+ i1);
		for(int index =begin_index+1; index < size; index ++) {
			i2= data.get(index);
			
			/* every time i read a new instance and change the place in the distribution */
			instances_by_attr.change(key0, i1.getAttrValue(this.targetDomain.getFReferenceName()), +1.0d * i1.getWeight());	//+1
			instances_by_attr.change(key1, i1.getAttrValue(this.targetDomain.getFReferenceName()), -1.0d * i1.getWeight());	//-1
		
			if (flog.debug() !=null)
				flog.debug().log("Instances " +i1+" vs "+i2);
			/*
			 * CATEGORIZATION 2.1. Cut points are points in the sorted list above where the class labels change. 
			 * Eg. if I had five instances with values for the attribute of interest and labels 
			 * (1.0,A), (1.4,A), (1.7, A), (2.0,B), (3.0, B), (7.0, A), then there are only
			 * two cutpoints of interest: 1.85 and 5 (mid-way between the points
			 * where the classes change from A to B or vice versa).
			 */
			if ( targetComp.compare(i1, i2)!=0 && attrComp.compare(i1, i2)!=0) {
				num_split_points++;
				
				if (flog.debug() !=null)
					flog.debug().log("entropy.info_cont() SEARCHING: "+(index)+" attr "+this.splitDomain.getFName()+ " "+ i2);
				// the cut point
				Object cp_i = i1.getAttrValue(this.splitDomain.getFReferenceName());
				Object cp_i_next = i2.getAttrValue(this.splitDomain.getFReferenceName());

				Class<?> fClass = this.splitDomain.getFType();
				
				Object cut_point = Util.calculateMidPoint(fClass, cp_i, cp_i_next);
							
				/*
				 * CATEGORIZATION 3. Evaluate your favourite disparity measure 
				 * (info gain, gain ratio, gini coefficient, chi-squared test) on the cut point
				 * and calculate its gain 
				 */
				double sum = Entropy.calc_info_attr(instances_by_attr);
				if (sum < best_sum) {
					best_sum = sum;
					split_index = index;
				
					if (flog.debug() !=null)
						flog.debug().log(Util.ntimes("?", 10)+"** FOUND: @"+(index)+" target ("+ i1.getAttrValue(this.targetDomain.getFName()) 
								+"-|T|-"+ i2.getAttrValue(this.targetDomain.getFName())+")");
					bestPoint = new SplitPoint(index-1, cut_point);
					bestPoint.setInformationValue(best_sum);
					
//					if (best_distribution != null)
//						best_distribution.clear();
					best_distribution = new CondClassDistribution(instances_by_attr);
				}
			}
			i1 = i2;

		}
		if (best_distribution != null) {
			if (flog.debug() !=null)
				flog.debug().log("bp:"+ bestPoint);
		
			this.splitDomain.addSplitPoint(bestPoint);
		
			/* 
			 * TODO : can we put the conditional class distribution to its correct place instead of the split
			 */

			if (flog.debug() !=null)
				flog.debug().log("bd:"+ best_distribution.getNumCondClasses());
			double sum1 = find_a_split(begin_index, split_index, depth-1, best_distribution.getDistributionOf(key0));

			double sum2 = find_a_split(split_index, size, depth-1, best_distribution.getDistributionOf(key1));
		
			return sum1 + sum2; 
		} else
			return 0.0;
		
		/* return best_sum;
		 * TODO find the correct formula of the sumi's for the return
		 */
		
		
		
	}


}
