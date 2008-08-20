package org.drools.learner;

import java.util.HashMap;
import java.util.Iterator;

import org.drools.WorkingMemory;
import org.drools.learner.builder.Learner.DataType;
import org.drools.learner.builder.Learner.DomainAlgo;
import org.drools.learner.tools.FeatureNotSupported;

public class Memory {

	// TODO pass a list of classes, and get all the object from that class
	// by default structured
	public static Memory createFromWorkingMemory(WorkingMemory _session, Class<?> clazz, DomainAlgo domain, DataType data) throws FeatureNotSupported {
		// if mem == null
		Memory mem = new Memory();

		mem.session = _session;

		mem.setClassToClassify(clazz);
		// create schema from clazz
		Schema inst_schema = null;
		try {
			inst_schema = Schema.createSchemaStructure(clazz, domain, data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}

		// create a instance list that can hold objects from our schema
		mem.instances.put(clazz, new InstanceList(inst_schema, _session));
		
//		mem.test_instances.put(clazz, new InstanceList(inst_schema, _session));

		/* 
		 * do they create an ObjectTypeNode for each new inserted object type?
		 * even if there is no rule exists.
		 * No probably they do not 
		 */
		Iterator<Object> it_object = _session.iterateObjects();	// how can i get the object type nodes
		while (it_object.hasNext()) {
			Object obj = it_object.next();
			// validating in the the factory during instantiation
			//if (clazz.isAssignableFrom(obj.getClass()))
			mem.instances.get(clazz).addStructuredInstance(obj);
		}
		//dt.FACTS_READ += facts.size();

		return mem;
	}
	
	public static Memory createTestFromWorkingMemory(Memory old_memory, WorkingMemory _session, Class<?> clazz, DomainAlgo domain, DataType data) throws FeatureNotSupported {
		// if mem == null
		Memory mem = new Memory();

		mem.session = _session;

		mem.setClassToClassify(clazz);
		
		// create schema from clazz
		Schema inst_schema = old_memory.getClassInstances().getSchema();
//		try {
//			inst_schema = Schema.createSchemaStructure(clazz, domain, data);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			System.exit(0);
//		}

		// create a instance list that can hold objects from our schema
		mem.instances.put(clazz, new InstanceList(inst_schema, _session));

		/* 
		 * do they create an ObjectTypeNode for each new inserted object type?
		 * even if there is no rule exists.
		 * No probably they do not 
		 */
		Iterator<Object> it_object = _session.iterateObjects();	// how can i get the object type nodes
		while (it_object.hasNext()) {
			Object obj = it_object.next();
			// validating in the the factory during instantiation
			//if (clazz.isAssignableFrom(obj.getClass()))
			mem.instances.get(clazz).addStructuredInstance(obj);
		}
		//dt.FACTS_READ += facts.size();

		return mem;
	}


	// Drools memory
	private WorkingMemory session;
	//// class specification
	//private Schema schema;
	Class<?> clazzToClassify;
	
	// instance list used to train
	private HashMap<Class<?>,InstanceList> instances;
	
//	// instance list used to test
//	private HashMap<Class<?>,InstanceList> test_instances;
	
	
	private Memory() {
		this.instances = new HashMap<Class<?>, InstanceList>();
	}
	
	private void setClassToClassify(Class<?> clazz) {
		this.clazzToClassify = clazz;
	}
	
	public InstanceList getClassInstances() {
		return instances.get(this.clazzToClassify);
	}

	
}
