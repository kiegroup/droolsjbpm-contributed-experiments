package org.drools.learner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.drools.core.WorkingMemory;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;
public class InstanceList {

	private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(InstanceList.class, SimpleLogger.WARN);
	private static SimpleLogger slog = LoggerFactory.getSysOutLogger(InstanceList.class, SimpleLogger.DEFAULT_LEVEL);
	
	private Schema schema;
	private ArrayList<Instance> instances;
	private InstanceFactory factory = null;
	
	public InstanceList(Schema _schema, WorkingMemory _session) {
		this.schema = _schema;
		this.instances = new ArrayList<Instance>();
		this.factory = new InstanceFactory(_session, _schema);
	}
	
	// copy ctor
	public InstanceList(InstanceList _il) {
		this.schema = _il.schema;
		this.instances = _il.instances;
		this.factory = _il.factory;
	}
	
	// another copy constructor
	public InstanceList(InstanceList _il, int size) {
		this.schema = _il.schema;
		this.instances = new ArrayList<Instance>(size);
		this.factory = _il.factory;
	}

	public void addStructuredInstance(Object _obj) {
		//the factory will validate the object class during the execution
		// create instance and all attributes according to the schema		
		Instance inst = factory.createInstance(_obj);		
		
		if (inst != null ) {
			// the object is validated and the instance is created
			//System.out.println(inst);
			instances.add(inst);
		} else {
			if (slog.warn() != null)
				slog.warn().log("The object "+_obj.getClass()+" is not related to the structure, couldnot create the instance\n");
			//System.exit(0);
		}
	}

	public void shuffle() {
		long seed = 10101001;
		Random rnd = new Random(seed);
		Collections.shuffle(this.instances, rnd);
	}
	
	public InstanceList subList(int from, int until) {
		InstanceList il = new InstanceList(this);
		il.instances = new ArrayList<Instance>(instances.subList(from, until));
		return il;
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
		InstanceList toReturn = new InstanceList(this, bag.length);
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