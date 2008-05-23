package org.drools.learner.eval;

import java.util.Hashtable;

import org.drools.learner.Domain;
import org.drools.learner.tools.Util;

public class CondClassDistribution {
	
	private String sum_key = Util.sum();
	
	private Domain cond_attr; // domain of the attr we distribute the instances conditionally
	private Hashtable<Object, ClassDistribution> cond_quantity_by_class;
	
	private int total_num;
	
	public CondClassDistribution(Domain attributeDomain, Domain targetDomain) {
		this.cond_attr = attributeDomain;
		//List<?> attributeValues = this.domain.getValues();
		cond_quantity_by_class = new Hashtable<Object, ClassDistribution>(attributeDomain.getCategoryCount());
		
		for (int c_idx = 0; c_idx<attributeDomain.getCategoryCount(); c_idx++) {
			Object attr_category = attributeDomain.getCategory(c_idx);
			cond_quantity_by_class.put(attr_category, new ClassDistribution(targetDomain));
		}
		
	}
	
	public CondClassDistribution(CondClassDistribution copy) {
		this.cond_attr = copy.getAttrDomain();
		this.cond_quantity_by_class = new Hashtable<Object, ClassDistribution>(copy.getNumCondClasses());
		
		for (int c_idx = 0; c_idx< this.cond_attr.getCategoryCount(); c_idx++) {
			Object attr_category = this.cond_attr.getCategory(c_idx);
			this.cond_quantity_by_class.put(attr_category, new ClassDistribution(copy.getDistributionOf(attr_category)));
		}
		this.total_num = copy.getTotal();
		
	}
	
	public int getTotal() {
		return this.total_num;	
	}
	
	public void setTotal(int size) {
		this.total_num = size;	
	}
	
	public Domain getAttrDomain() {
		return cond_attr;
	}
//	public String getTargetName() {
//		return target_attr.getFName();
//	}
	
	public Object getCondClass(int idx) {
		return cond_attr.getCategory(idx);
	}
	
	public int getNumCondClasses() {
		return cond_attr.getCategoryCount();
	}
	
	public int getTotal_AttrCategory(Object attr_value) {
		return cond_quantity_by_class.get(attr_value).getSum();
	}
	
	public void change(Object attr_category, Object target_class, int i) {
//		System.out.print("The cond_dist: a_cat:"+ attr_category+" t_cat:"+ target_class);
//		System.out.println(" the nums: "+cond_quantity_by_class.get(attr_category));
		
		cond_quantity_by_class.get(attr_category).change(target_class, i);		
		cond_quantity_by_class.get(attr_category).change(sum_key, i);
		
	}
	
	public ClassDistribution getDistributionOf(Object attr_value) {
		return cond_quantity_by_class.get(attr_value);
	}
	public void clear() {
		this.cond_quantity_by_class.clear();
		// all for each?
	}
	
	public void setDistForAttrValue(Object attr_value, ClassDistribution facts_in_class) {
		//cond_quantity_by_class.put(attr_value, targetDist);
		/* TODO should i make a clone */	
		cond_quantity_by_class.get(attr_value).setDistribution(facts_in_class);
		
	}
	public String toString() {
		String out = "\nCondClassDist: attr: "+cond_attr.getFName()+" total num: "+ this.getTotal() + "\n" ;
		for (int c_idx = 0; c_idx<cond_attr.getCategoryCount(); c_idx++) {
			Object attr_category = cond_attr.getCategory(c_idx);
			ClassDistribution td = cond_quantity_by_class.get(attr_category);
			out += "(ATTR:"+attr_category+ "=> "+ td +")";
		}
		return out;
	}	

}
