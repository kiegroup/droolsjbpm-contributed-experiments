package dt.memory;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import dt.tools.Util;

public class FactDistribution {
	String attr_sum = Util.sum();
	
	Hashtable<Object, Hashtable<Object, Integer>> facts_at_attr;

	private int total_num;
	
	public FactDistribution(List<?> attributeValues, List<?>  targetValues) {
		facts_at_attr = new Hashtable<Object, Hashtable<Object, Integer>>(attributeValues.size());
		
		for (Object attr : attributeValues) {
			facts_at_attr.put(attr, new Hashtable<Object, Integer>(targetValues.size() + 1));
			for (Object t : targetValues) {
				facts_at_attr.get(attr).put(t, 0);
			}
			facts_at_attr.get(attr).put(attr_sum, 0);
		}
		
	}
	
	public FactDistribution clone() {
		return this.clone();
	}
	
	public void setTotal(int size) {
		this.total_num = size;	
	}
	public int getTotal() {
		return this.total_num;	
	}
	
	public Hashtable<Object, Integer> getAttrFor(Object attr_value) {
		return facts_at_attr.get(attr_value);
	}
	
	public int getSumForAttr(Object attr_value) {
		return facts_at_attr.get(attr_value).get(attr_sum).intValue();
	}
	
	public void setSumForAttr(Object attr_value, int total) {
		facts_at_attr.get(attr_value).put(attr_sum,total);
	}
	
	public void setTargetDistForAttr(Object attr_value, Hashtable<Object, Integer> targetDist) {
		for (Object target: targetDist.keySet())
			facts_at_attr.get(attr_value).put(target,targetDist.get(target));
	}
	
	public void setTargetDistForAttr(Object attr_value, FactTargetDistribution targetDist) {
		for (Object target: targetDist.getTargetClasses())
			facts_at_attr.get(attr_value).put(target,targetDist.getVoteFor(target));
	}

	public void change(Object attrValue, Object targetValue, int i) {
		int num_1 = facts_at_attr.get(attrValue).get(targetValue).intValue();
		num_1 += i;
		facts_at_attr.get(attrValue).put(targetValue, num_1);
		
		int total_num_1 = facts_at_attr.get(attrValue).get(attr_sum).intValue();
		total_num_1 += i;
		facts_at_attr.get(attrValue).put(attr_sum, total_num_1);
		
	}

	public Collection<Object> getAttributes() {
		return facts_at_attr.keySet();
	}
	

}
