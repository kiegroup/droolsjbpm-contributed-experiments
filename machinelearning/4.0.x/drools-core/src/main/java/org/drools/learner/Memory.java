package org.drools.learner;

import java.util.HashMap;
import java.util.Iterator;

import org.drools.WorkingMemory;
import org.drools.learner.tools.FeatureNotSupported;

public class Memory {
	
	
	// TODO pass a list of classes, and get all th eobject from that class
	public static Memory createFromWorkingMemory(WorkingMemory _session, Class<?> clazz, int domain_type) throws FeatureNotSupported {
		// if mem == null
		Memory mem = new Memory();

		mem.session = _session;

		mem.setClassToClassify(clazz);
		// create schema from clazz
		Schema inst_schema = Schema.createFromClass(clazz, domain_type);

		// create a instance list that can hold objects from our schema
		mem.instances.put(clazz, new InstanceList(inst_schema));

		/* 
		 * do they create an ObjectTypeNode for each new inserted object type?
		 * even if there is no rule exists.
		 * No probably they do not 
		 */
		Iterator<Object> it_object = _session.iterateObjects();	// how can i get the object type nodes
		while (it_object.hasNext()) {
			Object obj = it_object.next();
			if (clazz.isAssignableFrom(obj.getClass()))
				mem.instances.get(clazz).addFromWorkingMemory(_session, obj);
		}
		//dt.FACTS_READ += facts.size();

		return mem;
	}


	// Drools memory
	private WorkingMemory session;
	//// class specification
	//private Schema schema;
	Class<?> clazzToClassify;
	
	// instance list
	private HashMap<Class<?>,InstanceList> instances;
	
	
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
