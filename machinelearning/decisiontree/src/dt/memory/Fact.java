package dt.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;


public class Fact implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Hashtable<String, Domain<?>> fields;
	private HashMap<String, Object> values;

	public Fact() {
		this.values = new HashMap<String, Object>();
		this.fields = new Hashtable<String, Domain<?>>();
		/* while creating the fact i should add the possible keys, the valid domains */
	}
	
	public Fact(Set<Domain<?>> domains) {
		this.fields = new Hashtable<String, Domain<?>>();
		for (Domain<?> d: domains)
			this.fields.put(d.getName(), d);
		this.values = new HashMap<String, Object>();
		//this.attributes. of the keys are only these domains
		/* while creating the fact i should add the possible keys, the valid domains */
	}

	/*public Fact(Hashtable<Domain<?>, Attribute<?>> attributes) {
		this.attributes = attributes;
	}*/
	
	/* 
	 * TODO do i need to check anything before adding 
	 * maybe i should check if the domain specifications are written somewhere
	 * 
	 */
	public void add(Domain<?> its_domain, Object value) throws Exception {
		if (!its_domain.isPossible(value))
			throw new Exception("The value "+value +" is not possible what is going on in domain: "+ its_domain.getName());
		//System.out.println("Bocuk wants to see the names of the domains "+ its_domain.getName());
		fields.put(its_domain.getName(), its_domain);
		values.put(its_domain.getName(), value);
	}

	public Domain<?> getDomain(String field_name) {
		return fields.get(field_name);
	}
	
	public Object getFieldValue(String field_name) {
		return values.get(field_name);
	}

	public String getAttributeValueAsString(String name) {
		Object attr = getFieldValue(name);
		return (attr != null) ? attr.toString() : null;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if ((o == null) || (this.getClass() != o.getClass())) {
			return false;
		}
		Fact other = (Fact) o;
		return fields.equals(other.fields); //TODO work on the equals() fnc
	}
	
	public int hashCode() {
		return values.hashCode();
	}

	public String toString() {
		String out = this.hashCode() + "";
		
//		String out = "";
//		for (String key: fields.keySet())
//		{
//			out += "-"+values.get(key);
//		}
//		for (String key: fields.keySet())
//		{
//			out += fields.get(key) +"="+values.get(key)+",";
//		}
		return out;
	}
	
}
