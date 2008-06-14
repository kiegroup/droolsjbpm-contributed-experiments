package org.drools.learner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.drools.WorkingMemory;
import org.drools.common.InternalWorkingMemory;
import org.drools.spi.Extractor;
public class InstanceList {

	private Schema schema;
	private ArrayList<Instance> instances;
	private InstanceFactory factory;
	
	// old constructer
	public InstanceList(Schema _schema) {
		this.schema = _schema;
		this.instances = new ArrayList<Instance>(); 
	}
	public InstanceList(Schema _schema, WorkingMemory _session) {
		this.schema = _schema;
		this.instances = new ArrayList<Instance>(); 
		this.factory = new InstanceFactory(_session, _schema);
	}
	
	// will be used as copy constructer
	public InstanceList(Schema _schema, int size) {
		this.schema = _schema;
		this.instances = new ArrayList<Instance>(size);
	}

	public void addStructuredInstance(Object _obj) {
		// create instance and all attributes according to the schema		
		Instance inst = factory.createInstance(_obj);		
		System.out.println(inst);
		if (inst != null )
			instances.add(inst);
		else {
			System.out.println("Couldnot create the instance");
			System.exit(0);
		}
	}
	
	// old method
	public void addFromWorkingMemory(WorkingMemory _session, Object _obj) {
		// create instance and all attributes according to the schema
		Instance inst = new Instance();
		
		for (String f_name : schema.getAttrNames()) {
			Domain f_domain = schema.getAttrDomain(f_name);
			//ClassFieldExtractor f_extractor = schema.getAttrExtractor(f_name);
			Extractor f_extractor = schema.getAttrExtractor(f_name);//Label
			/* from WorkingMemoryLogger, private String extractDeclarations(final Activation activation,  final WorkingMemory workingMemory) {
			 * you can cast the WorkingMemory
			 * final Object value = declaration.getValue( (InternalWorkingMemory) workingMemory, handleImpl.getObject() );
			 */
			Object f_value = f_extractor.getValue( (InternalWorkingMemory) _session, _obj);
			/* TODO instanity checks 
			 * 1- is this value possible by type?
			 * 2- does this value exist in the possible set of values (if the domain type is categorical)?
			 * 
			 */
			try {
				if (f_domain.isPossible(f_value)) {
					f_domain.addCategory(f_value);
					inst.setAttr(f_name, f_value);
				}
			} catch (Exception e) {
				System.out.println("Domain: "+f_domain+ " could not add the value "+ f_value);
				e.printStackTrace();
			}
			
		}
		
		System.out.println("inst:"+ inst);
		instances.add(inst);
	}
	
	public void addAsInstance(Instance inst) {
		instances.add(inst);
	}
	
	public int getSize() {
		return instances.size();
	}
	
	public Instance getInstance(int index) {
		return instances.get(index);
	}
	
	public List<Instance> getInstances() {
		return instances;
	}
	
	public InstanceList getInstances(int[] bag) {
		if (bag.length > this.getSize()) {
			System.out.println("Exception: TOO BIG to get Memory.getClassInstancesOf");
			return null;
		}
		InstanceList toReturn = new InstanceList(this.getSchema(), bag.length);
		for (int j = 0; j< bag.length ; j ++) {
			toReturn.addAsInstance(this.getInstance(bag[j]));
		}
		return toReturn;
	}
	
	public Schema getSchema() {
		return schema;
	}
	public Collection<String> getTargets() {
		return schema.getTargets();
	}

	public Collection<String> getAttributes() {
		return schema.getAttrNames();
	}
	
	
/*	String getClassName();

	void assignTo(Collection<Instance> c);

	Domain getDomain(String attr);

 	public Collection<Domain> getDomains();

	public int getSize();

	public String toString();*/
}

/*
	Data set: 	A schema and a set of instances matching the schema. 
				Generally, no ordering on instances is assumed. 
				Most machine learning work uses a single fixed-format table.
	
	
				
	Instance: 	A single object of the world from which a model will be learned, 
														or on which a model will be used (e.g., for prediction). 
				In most machine learning work, instances are described by feature vectors; 
				some work uses more complex representations (e.g., containing relations between instances or between parts of instances).
		= (example, case, record)
	
	Schema: 	A description of a data set's attributes and their properties.
	
	Attribute:	A quantity describing an instance. 
				An attribute has a domain defined by the attribute type, which denotes the values that can be taken by an attribute. 
		= (field, variable, feature)
		
	The following domain types are common:
		- Categorical: 	A finite number of discrete values. 
						The type nominal denotes that there is no ordering between the values, such as last names and colors. 
						The type ordinal denotes that there is an ordering, 
						such as in an attribute taking on the values low, medium, or high.
						
		- Continuous (quantitative): Commonly, subset of real numbers, 
									where there is a measurable difference between the possible values. 
						Integers are usually treated as continuous in practical problems.
						
	Feature:	A feature is the specification of an attribute and its value. 
				For example, color is an attribute. ``Color is blue'' is a feature of an example. 
				Many transformations to the attribute set leave the feature set unchanged 
				(for example, regrouping attribute values or transforming multi-valued attributes to binary attributes). 
				Some authors use feature as a synonym for attribute (e.g., in feature-subset selection).	
	
	Feature vector:	A list of features describing an instance.
		= (record, tuple)
	
*/