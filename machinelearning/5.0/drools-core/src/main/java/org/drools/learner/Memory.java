package org.drools.learner;

import java.util.HashMap;
import java.util.Iterator;

import org.drools.WorkingMemory;
import org.drools.learner.builder.Learner.DataType;
import org.drools.learner.builder.Learner.DomainAlgo;
import org.drools.learner.tools.FeatureNotSupported;
import org.drools.learner.tools.Util;

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

		return mem;
	}
	


	// Drools memory
	private WorkingMemory session;
	//// class specification
	//private Schema schema;
	Class<?> clazzToClassify;
	
	// instance list used to train
	private HashMap<Class<?>,InstanceList> instances;
	
	private InstanceList train_instances, test_instances;
	
//	// instance list used to test
//	private HashMap<Class<?>,InstanceList> test_instances;
	private double trainRatio = Util.TRAINING_RATIO;
	private double testRatio = Util.TESTING_RATIO;
	
	
	private Memory() {
		this.instances = new HashMap<Class<?>, InstanceList>();
	}
	
	public void setTrainRatio(double ratio) {
		trainRatio = ratio;
	}
	
	public void setTestRatio(double ratio) {
		testRatio = ratio;
	}
	
	public InstanceList getTrainSet() {
		return train_instances;
	}
	public InstanceList getTestSet() {
		return test_instances;
	}
	
	public void processTestSet() {
		int split_idx =  (int)(trainRatio * instances.get(this.clazzToClassify).getSize());
		//int split_idx2 =  split_idx + (int)(testRatio * instances.get(this.clazzToClassify).getSize());
		int split_idx2 = instances.get(this.clazzToClassify).getSize();
		
		train_instances = instances.get(this.clazzToClassify).subList(0, split_idx);
		test_instances = instances.get(this.clazzToClassify).subList(split_idx, split_idx2);//class_instances.getSize());
		return;
	}
	
	private void setClassToClassify(Class<?> clazz) {
		this.clazzToClassify = clazz;
	}
	// target class instances
	public InstanceList getClassInstances() {
		return instances.get(this.clazzToClassify);
	}

	
}
