package org.drools.learner;

import java.util.HashMap;


public class Instance {
	private HashMap<String, Attribute> attributes;
	
	public Instance() {
		this.attributes = new HashMap<String, Attribute>(); // TODO should i set a size, HOW?
	}
	
	
	
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
		String out = this.hashCode() + " ";
		for (String key: attributes.keySet())
		{
			out += key +"="+attributes.get(key)+", ";
		}
		return out;
	}
	

}
