package dt.memory;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import dt.tools.Util;

public class FactAttrDistribution {
	String attr_sum = Util.sum();
	
	Hashtable<Object, FactTargetDistribution> facts_at_attr;

	private int total_num;
	
	public FactAttrDistribution(List<?> attributeValues, Domain<?>  targetDomain) {
		facts_at_attr = new Hashtable<Object, FactTargetDistribution>(attributeValues.size());
		
		for (Object attr : attributeValues) {
			facts_at_attr.put(attr, new FactTargetDistribution(targetDomain));
//			for (Object t : targetDomain.getValues()) {
//				facts_at_attr.get(attr).put(t, 0);
//			}
//			facts_at_attr.get(attr).put(attr_sum, 0);
		}
		
	}
	
	public FactAttrDistribution clone() {
		return this.clone();
	}
	
	public void setTotal(int size) {
		this.total_num = size;	
	}
	public int getTotal() {
		return this.total_num;	
	}
	
	public FactTargetDistribution getAttrFor(Object attr_value) {
		return facts_at_attr.get(attr_value);
	}
	
	public int getSumForAttr(Object attr_value) {
		return facts_at_attr.get(attr_value).getSum();
	}
	
	public void setSumForAttr(Object attr_value, int total) {
		facts_at_attr.get(attr_value).setSum(total);
	}
	
//	public void setTargetDistForAttr(Object attr_value, Hashtable<Object, Integer> targetDist) {
//		for (Object target: targetDist.keySet())
//			facts_at_attr.get(attr_value).put(target,targetDist.get(target));
//	}
	
	public void setTargetDistForAttr(Object attr_value, FactTargetDistribution targetDist) {
		
		//facts_at_attr.put(attr_value, targetDist);
		/* TODO should i make a close */
		FactTargetDistribution old = facts_at_attr.get(attr_value);		
		old.setDistribution(targetDist);
	
	}

	public void change(Object attrValue, Object targetValue, int i) {
		facts_at_attr.get(attrValue).change(targetValue, i);
		
		facts_at_attr.get(attrValue).change(attr_sum, i);
/*		int num_1 = facts_at_attr.get(attrValue).get(targetValue).intValue();
		num_1 += i;
		facts_at_attr.get(attrValue).put(targetValue, num_1);
		
		int total_num_1 = facts_at_attr.get(attrValue).get(attr_sum).intValue();
		total_num_1 += i;
		facts_at_attr.get(attrValue).put(attr_sum, total_num_1);
		*/
		
	}

	public Collection<Object> getAttributes() {
		return facts_at_attr.keySet();
	}
	

}
