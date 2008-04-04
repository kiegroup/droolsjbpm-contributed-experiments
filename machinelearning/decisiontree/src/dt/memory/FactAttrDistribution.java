package dt.memory;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import dt.tools.Util;

public class FactAttrDistribution {
	
	private String attr_sum = Util.sum();
	private Domain<?> domain; // domain of the attr 
	private Hashtable<Object, FactTargetDistribution> facts_at_attr;

	private int total_num;
	
	public FactAttrDistribution(Domain<?> attributeDomain, Domain<?>  targetDomain) {
		this.domain = attributeDomain;
		List<?> attributeValues = this.domain.getValues();
		facts_at_attr = new Hashtable<Object, FactTargetDistribution>(attributeValues.size());
		
		for (Object attr : attributeValues) {
			facts_at_attr.put(attr, new FactTargetDistribution(targetDomain));
		}
		
	}
	
	public FactAttrDistribution(FactAttrDistribution copy) {
		this.domain = copy.getDomain();
		this.facts_at_attr = new Hashtable<Object, FactTargetDistribution>(copy.getNumAttributes());
		
		for (Object attr : copy.getAttributes()) {
			FactTargetDistribution attr_x = new FactTargetDistribution(copy.getAttrFor(attr));
			facts_at_attr.put(attr, attr_x);
		}
		this.total_num = copy.getTotal();
		
	}
	
//	public FactAttrDistribution clone() {
//		FactAttrDistribution temp = new FactAttrDistribution(this);
//		return this.clone();
//	}
	
	private Domain<?> getDomain() {
		return this.domain;
	}

	public void clear() {
		this.facts_at_attr.clear();
		// all for each?
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
	
	
	public void setTargetDistForAttr(Object attr_value, FactTargetDistribution targetDist) {
		
		//facts_at_attr.put(attr_value, targetDist);
		/* TODO should i make a clone */
		FactTargetDistribution old = facts_at_attr.get(attr_value);		
		old.setDistribution(targetDist);
	
	}

	public void change(Object attrValue, Object targetValue, int i) {
		facts_at_attr.get(attrValue).change(targetValue, i);
		
		facts_at_attr.get(attrValue).change(attr_sum, i);
		
	}

	public Collection<Object> getAttributes() {
		return facts_at_attr.keySet();
	}
	
	public int getNumAttributes() {
		return facts_at_attr.keySet().size();
	}
	
	public String toString() {
		String out = "FAD: attr: "+domain.getName()+" total num: "+ this.getTotal() + "\n" ;
		for (Object attr : this.getAttributes()) {
			FactTargetDistribution ftd = facts_at_attr.get(attr);
			out += ftd ;
		}
		return out;
	}
	

}
