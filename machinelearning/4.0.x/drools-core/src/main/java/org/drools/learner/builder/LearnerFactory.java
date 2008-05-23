package org.drools.learner.builder;

import org.drools.WorkingMemory;
import org.drools.learner.Memory;
import org.drools.learner.tools.FeatureNotSupported;
import org.drools.learner.tools.Util;

public class LearnerFactory {
	
	//static int TREE_TYPE = ID3;
	
	public static Learner createID3(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		
		/*
		 * Quesitons:
		 * 1- which class to work with? : obj_class
		 * 2- what is its target attribute?
		 * 3- what are the objects
		 */ 
		
		/* create the memory */
		// process the annotations => create the 
		
		Memory mem = Memory.createFromWorkingMemory(wm, obj_class, Util.ID3);
		// set to memory type only the discrete domains

		ID3Learner id3 = new ID3Learner();
		id3.build(mem);//obj_class, target_attr, working_attr
		
		Tester.test(id3.getTree(), mem.getClassInstances());
		return id3;
	}
	
	
	public static Learner createC45(WorkingMemory wm, Class<? extends Object> obj_class) throws FeatureNotSupported {
		
		/* create the memory */
		Memory mem = Memory.createFromWorkingMemory(wm, obj_class, Util.C45);
		// set to memory type only the discrete domains

		C45Learner c45 = new C45Learner();
		c45.build(mem);//obj_class, target_attr, working_attr
		Tester.test(c45.getTree(), mem.getClassInstances());
		return c45;
	}
	
	public static Learner createID3(WorkingMemory wm) {
		
		/*
		 * Quesitons:
		 * 1- which class to work with?
		 * 2- what is its target attribute?
		 * 3- what are the objects
		 */ 
		//String target_attr = ObjectReader.getTargetAnnotation(arest.getClass());
		
		ID3Learner id3 = new ID3Learner();
		//id3.build(wm);
		return id3;
	}
}
