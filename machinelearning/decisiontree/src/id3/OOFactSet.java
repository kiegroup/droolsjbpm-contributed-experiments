package id3;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

public class OOFactSet implements FactSet{

	private List<Fact> facts;

	/* set of attributes defining the type of the fact */
	//private Set<T> validDomains;
	private Hashtable<String, Domain<?>> validDomains;

	private Class<?> fs_class;
	
	public OOFactSet(Class<?> fact_class) {//Class<? extends Object>
		this.facts = new ArrayList<Fact>();
		this.validDomains = new Hashtable<String, Domain<?>>();
		this.fs_class = fact_class;
	}

	/*
	 	factset.insert(object)
			fact f;
			foreach field in object
				domain d = domainset_hashtable[field];
				attribute attr = d.createattribute(field.value)
				f.add(attr)
			addfact(f)
	 */
	public boolean insert(Object element) {
		Fact f = new Fact();

		Class<?> element_class = element.getClass();
		Method [] element_methods = element_class.getDeclaredMethods();
		for (Method m: element_methods) {
			String m_name = m.getName();
			String return_type_name = m.getReturnType().getName();
			if (Util.isGetter(m_name) & Util.isSimpleType(return_type_name) ) {
//				if (!Util.isSimpleType(return_type_name))
//					continue; // in the future we should support classes
				String field = Util.getAttributeName(m_name);

				/*
				 * when u first read the element
				 * 	if the domain specifications are already given 
				 * 		then read from there and 
				 * 			 dont add each new value you read, just check if it is valid
				 * otherwise you create a new domain for that attribute
				 * Domain attributeSpec = dataSetSpec.getDomain(attr_name);
				 */
				Domain fieldDomain = validDomains.get(field);
				
				//String
				Object field_value;
				try {
					field_value = m.invoke(element);
					
					//Object attribute =  fieldDomain.createAttribute(field_value);
					if (fieldDomain.isPossible(field_value))
						fieldDomain.addValue(field_value);
					f.add(fieldDomain, field_value);
					//System.out.println("FactSet.insert f "+ f + " fielddomain name "+fieldDomain.getName()+" value: "+field_value+".");
					
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				
			}
		}
		
		boolean result = facts.add(f);
		//System.out.println("FactSet.insert f "+ f + " result "+result+" facts.size(): "+facts.size()+".");
		return result;
		
		
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
	
	public Class<?> getFactClass() {
		return fs_class;
	}

	public String getClassName() {
		return fs_class.getName();
	}

	
}
