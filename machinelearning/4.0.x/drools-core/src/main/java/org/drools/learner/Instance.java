package org.drools.learner;

import java.util.HashMap;


public class Instance {
	private HashMap<String, Attribute> attributes;
	private double weight = 1.0;
	
	public Instance() {
		this.attributes = new HashMap<String, Attribute>(); // TODO should i set a size, HOW?
	}
	
	public void setWeight(double w) {
		weight = w;
	}
	
	public double getWeight() {
		return weight;
	}
	
	// name : _obj_klass+"@"+attr_name
	// value : its value
	public void setAttr(String _name, Object _value) {
		Attribute f_attr = new Attribute();
		f_attr.setName(_name);
		f_attr.setValue(_value);
		this.attributes.put(_name, f_attr);
	}
	
	public Attribute getAttr(String field_name) {
		return attributes.get(field_name);
	}
	public Object getAttrValue(String field_name) {
		return attributes.get(field_name).getValue();
	}
	
	public int hashCode() {
		return attributes.hashCode();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(this.hashCode() + " ");
		for (String key: attributes.keySet()) {
			sb.append(key +"="+attributes.get(key)+", ");
		}
		return sb.toString();
	}
	

}
