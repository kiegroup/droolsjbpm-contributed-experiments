package org.drools.learner;

public class Attribute {
	
	private String name;
	private Object value;
	
	//final ClassFieldExtractor extractor = cache.getExtractor( class_obj, f_name, getClass().getClassLoader() );
	
	public String getName() {
		return name;
	}
	
	public void setName(String _name) {
		this.name = _name;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object _value) {
		this.value = _value;
	}
	
	/* we assume that "another" points to the same attribute type as this */
	public int compareValue(Attribute another) {
		/* Boolean, Double, Integer, String...*/
		return AttributeValueComparator.instance.compare(getValue(), another.getValue());
	}
	
	public int hashCode() {
		return this.value.hashCode();
	}
	
	public String toString() {
		return value.toString();
	}
	

}
