package org.drools.learner.builder;

import java.io.Reader;

import org.drools.learner.DecisionTree;
import org.drools.learner.Memory;

public interface Learner {
	/* 
	 * the memory has the information 
	 * the instances: the objects which the decision tree will work on
	 * the schema: the definition of the object instance 
	 * 			(Class<?>) klass, String targetField, List<String> workingAttributes
	 */
	void build(Memory wm);
	
	DecisionTree getTree();
	
	int getTreeType();

}
