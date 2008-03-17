package id3;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

public class WorkingMemory {
	
	private Hashtable<String, FactSet> factsets;

	private Hashtable<String, Domain<?>> domainset;
	
	public WorkingMemory() {
		factsets = new Hashtable<String, FactSet>();
		domainset = new Hashtable<String, Domain<?>>();
	}

	public void insert(Object element) {
		String element_class = element.getClass().getName();
		//System.out.println("Get the keys:"+ factsets.keys());
		//System.out.println("WorkingMemory.get class "+ element_class + " exist? "+ factsets.containsKey(element_class));
		
		OOFactSet fs;
		if (!factsets.containsKey(element_class))
			fs = create_factset(element);
		else
			fs = (OOFactSet) factsets.get(element_class);//TODO should i cast
		
		fs.insert(element);
		System.out.println("WorkingMemory.insert(object) inserted element fs.size() "+ fs.getSize());
	}
	
	public void insert(String element, String name, String separator, List<Domain<?>> domains) {

		FSFactSet fs;
		if (!factsets.containsKey(name)) {
			fs =  new FSFactSet(name, domains);
			for (Domain<?> d: domains) {
				fs.addDomain(d.getName(), d);
				if (domainset.containsKey(d.getName()) || domainset.contains(d)) {
					System.out.println("WorkingMemory.insert Already exist domain bla????? name: "+name+ " domain: "+d.getName());
					System.exit(0);
				} else
					domainset.put(d.getName(), d);
			}
			factsets.put(name, fs);
		} else
			fs = (FSFactSet) factsets.get(name);//TODO should i cast
		
		fs.insert(element, domains, separator);
		//System.out.println("WorkingMemory.insert(string) inserted element fs.size() "+ fs.getSize());
	}
	
//	public void insert(FactSet fs) {
//		System.out.println("factset : "+ fs.getSize());
//		if (!factsets.containsKey(fs.getClassName())) {
//			for (Domain<?> d : fs.getDomains()) {
//				System.out.println("Domain"+ d.getName());
//				if (domainset.containsKey(d.getName()) || domainset.contains(d))
//					System.out.println("Already exist domain bla?????");
//				else
//					domainset.put(d.getName(), d);
//				
//				//System.out.println("WorkingMemory.create_factset field "+ field + " fielddomain name "+fieldDomain.getName()+" return_type_name: "+return_type_name+".");
//				
//				
//			}
//			factsets.put(fs.getClassName(), fs);
//		} else {
//			System.out.println("Already exist bla?????");
//		}
//	}

	
	/* factset workingmemory.createnew_factset(class) 
	 * 	=> instead of the class i have to pass the object itself because i am going to invoke the method
	 * 	=> no actually i will not invoke
	 * 		factset newfs = new newfactset(class) 
	 *			foreach field in class
	 *			domain d = domainset_hashtable[field]
	 *			if d == null
	 *				d = createnew_domain(field)
	 *			newfs.adddomain(d)=> why do you add this the factset? 
	 *								 we said that the domains should be independent from the factset
	 */
	private OOFactSet create_factset(Object element) {
		//System.out.println("WorkingMemory.create_factset element "+ element );
		
		Class<?> element_class = element.getClass();
		OOFactSet newfs = new OOFactSet(element_class);

		Method [] element_methods = element_class.getDeclaredMethods();
		for( Method m: element_methods) {
			
			
			String m_name = m.getName();
			String return_type_name = m.getReturnType().getName();
			//System.out.println("WorkingMemory.create_factset m "+ m + " method name "+m_name+" return_type_name: "+return_type_name+".");
			if (Util.isGetter(m_name) & Util.isSimpleType(return_type_name)) {
				String field = Util.getAttributeName(m_name);
				/*
				 * when u first read the element
				 * 	if the domain specifications are already given 
				 * 		then read from there and 
				 * 			 dont add each new value you read, just check if it is valid
				 * otherwise you create a new domain for that attribute
				 * Domain attributeSpec = dataSetSpec.getDomain(attr_name);
				 */
				Domain<?> fieldDomain;
				if (!domainset.containsKey(field))
					fieldDomain = DomainFactory.createDomainFromClass(m.getReturnType(), field);
				else
					fieldDomain = domainset.get(field);
				
				//System.out.println("WorkingMemory.create_factset field "+ field + " fielddomain name "+fieldDomain.getName()+" return_type_name: "+return_type_name+".");
				
				domainset.put(field, fieldDomain);
				newfs.addDomain(field, fieldDomain);
				
				//System.out.println("START: WorkingMemory.create_factset domainset size "+ domainset.size() + " newfs size "+newfs.getFacts().size()+".");
				
			}
		}
		
		factsets.put(element_class.getName(), newfs);
		return newfs;
	}
	
	/* TODO: iterator */ 
	public Collection<FactSet> getFactsets() {
		return factsets.values();
	}

	public Domain<?> getDomain(String field) {
		return domainset.get(field);
	}

	public boolean containsDomainKey(String field) {
		return domainset.containsKey(field);
	}

	public void putDomain(String field, Domain<?> fieldDomain) {
		this.domainset.put(field, fieldDomain);
		
	}

	public void putFactSet(String klass_name, FactSet newfs) {
		factsets.put(klass_name, newfs);	
	}

	public boolean containsFactSetKey(String field) {
		return factsets.containsKey(field);
	}
}


/*
workingmemory.insert(object)

	factset fs = factsets_hashtable[object.class]
	if fs == null
		fs = createnew_factset(object.class);
	fs.insert(object)
	
	
factset workingmemory.createnew_factset(class)

	factset newfs = new newfactset(class) 
	foreach field in class
		domain d = domainset_hashtable[field]
		if d == null
			d = createnew_domain(field)
		newfs.adddomain(d)


factset.insert(object)

	fact f;
	foreach field in object
		domain d = domainset_hashtable[field];
		attribute attr = d.createattribute(field.value)
		f.add(attr)
	addfact(f)


treebuilder.execute(workingmemory, classtoexecute, attributestoprocess)

	foreach factset in workingmemory
		if classtoexecute.isAssignableFrom( factset.class )
			internaladd(factset)

	internalprocess(attributestoprocess)


*/