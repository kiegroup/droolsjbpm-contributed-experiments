package id3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class FSFactSet implements FactSet{

	private List<Fact> facts;

	/* set of attributes defining the type of the fact */
	//private Set<T> validDomains;
	private Hashtable<String, Domain<?>> validDomains;

	private String fs_class; 
	
	
	public FSFactSet(String element_class) {
		this.facts = new ArrayList<Fact>();
		this.validDomains = new Hashtable<String, Domain<?>>();
		this.fs_class = element_class;
	}


	public FSFactSet(String element_class, List<Domain<?>> domains) {
		this.facts = new ArrayList<Fact>();
		this.validDomains = new Hashtable<String, Domain<?>>(domains.size());
		this.fs_class = element_class;
		
		for (Domain<?> d: domains) {
			//d.setConstant();
			validDomains.put(d.getName(), d);
		}
		
	}
	public boolean insert(String data, List<Domain<?>> domains, String separator){
		// assume the domains are in the same order with value
		Fact newfact = new Fact();
		//Hashtable<String,Object> attributes = new Hashtable<String,Object>();
		if (data.endsWith("."))
			data = data.substring(0, data.length()-1);
		List<String> attributeValues = Arrays.asList(data.split(separator));
		
		if (domains.size()== attributeValues.size()){

			Iterator<Domain<?>> domain_it = domains.iterator();
			Iterator<String> value_it = attributeValues.iterator();
			while(domain_it.hasNext() && value_it.hasNext()){
				Domain attr_domain = domain_it.next();
				//String name = attr_domain.getName();

				Object value =  attr_domain.readString(value_it.next());
				
				//System.out.println("Domain "+ name+ " and the value"+value);
				try {
					if (value == null) {
						value = new Double(-1);
					} else {
						if (attr_domain.isPossible(value))
							attr_domain.addValue(value);
					}
					newfact.add(attr_domain, value);
				} catch (Exception e) {
					System.out.println(e+ " the domain: "+attr_domain.getName()+ " does not accept "+ value);
					//e.printStackTrace();
				}
			}
			//String targetAttributeName = dataSetSpec.getTarget();
			//AttributeSpecification attributeSpec =dataSetSpec.getAttributeSpecFor(targetAttributeName );
			//System.out.println("Fact: "+newfact);
			boolean result = facts.add(newfact);
			return result;
		}
		else{
			throw new RuntimeException("Unable to construct Example from " + data);
		}
	}
	


	public void add(Fact newFact) {
		facts.add(newFact);
	}


	public Fact getFact(int index) {
		return facts.get(index);
	}
	
	public void assignTo(Collection<Fact> c) {
		c.addAll(facts);
	}
	
	public int getSize() {
		return facts.size();
	}
	
	/* TODO iterator */ 
	public Collection<Domain<?>> getDomains() {
		return validDomains.values();
	} 
	
	/* TODO iterator */ 
	public Collection<String> getDomainKeys() {
		return validDomains.keySet();
	} 
	
	public Domain<?> getDomain(String field) {
		return validDomains.get(field);	
	}

	public void addDomain(String field, Domain<?> fieldDomain) {
		validDomains.put(field, fieldDomain);	
	}
	

	public String getClassName() {
		return fs_class;
	}
	
	public String toString() {
		String out = "";
		for (Fact f: facts) {
			out += f.toString() +"\n";
		}
		return out;
	}
	

}
