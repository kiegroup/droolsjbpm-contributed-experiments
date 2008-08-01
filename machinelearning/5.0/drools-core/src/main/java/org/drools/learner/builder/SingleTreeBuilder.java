package org.drools.learner.builder;


import org.drools.learner.DecisionTree;
import org.drools.learner.InstanceList;
import org.drools.learner.Memory;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;

public class SingleTreeBuilder implements DecisionTreeBuilder{
	
	private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(SingleTreeBuilder.class, SimpleLogger.DEFAULT_LEVEL);
	private static SimpleLogger slog = LoggerFactory.getSysOutLogger(SingleTreeBuilder.class, SimpleLogger.DEFAULT_LEVEL);

	private TreeAlgo algorithm = TreeAlgo.SINGLE; // default bagging, TODO boosting
	
	private DecisionTree one_tree;
	//private Learner trainer;
	
	public SingleTreeBuilder() {//Learner _trainer) {
		//this.trainer = _trainer;
		//dom_type = trainer.getDomainType();
	}
	
	/* 
	 * the memory has the information 
	 * the instances: the objects which the decision tree will work on
	 * the schema: the definition of the object instance 
	 * 			(Class<?>) klass, String targetField, List<String> workingAttributes
	 */
	public void build(Memory mem, Learner _trainer) {
		final InstanceList class_instances = mem.getClassInstances();
		_trainer.setInputData(class_instances);
		if (class_instances.getTargets().size()>1) {
			//throw new FeatureNotSupported("There is more than 1 target candidates");
			if (flog.error() !=null)
				flog.error().log("There is more than 1 target candidates");
			
			System.exit(0);
			// TODO put the feature not supported exception || implement it
		}
		_trainer.setTrainingDataSize(class_instances.getSize());
		_trainer.setTrainingDataSizePerTree(class_instances.getSize());
		one_tree = _trainer.train_tree(class_instances);
		_trainer.setBestTree(one_tree);
	}
	
	public TreeAlgo getTreeAlgo() {
		return this.algorithm; // default
	}

}
