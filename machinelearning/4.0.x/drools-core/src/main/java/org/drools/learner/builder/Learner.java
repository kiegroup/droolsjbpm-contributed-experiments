package org.drools.learner.builder;

import java.util.HashSet;
import java.util.Iterator;

import org.drools.learner.DecisionTree;
import org.drools.learner.Domain;
import org.drools.learner.Instance;
import org.drools.learner.InstanceList;
import org.drools.learner.TreeNode;
import org.drools.learner.eval.InstDistribution;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;

public abstract class Learner {
	
	protected static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(Learner.class, SimpleLogger.DEFAULT_LEVEL);
	
	public static enum DomainAlgo { ID3, C45, WEIGHT }
	private int data_size;
	private DecisionTree best_tree;
	private InstanceList input_data;
	protected HashSet<Instance> missclassified_data;
	

	private DomainAlgo algorithm;
	
	
	protected abstract TreeNode train(DecisionTree dt, InstDistribution data_stats);
	
	public Learner() {
		this.data_size = 0;
	}

	
	public DecisionTree train_tree(InstanceList working_instances) {
		String target = this.getTargetDomain().getFName();
		DecisionTree dt = new DecisionTree(input_data.getSchema(), target);

		//flog.debug("Num of attributes: "+ dt.getAttrDomains().size());
		
		InstDistribution stats_by_class = new InstDistribution(dt.getTargetDomain());
		stats_by_class.calculateDistribution(working_instances.getInstances());
		dt.FACTS_READ += working_instances.getSize();

		TreeNode root = train(dt, stats_by_class);
		dt.setRoot(root);
		//flog.debug("Result tree\n" + dt);
		return dt;
	}
	
	public Domain getTargetDomain() {
		Iterator<String> it_target= input_data.getTargets().iterator();
		// TODO check if there is a target candidate
		String target = it_target.next();
		return input_data.getSchema().getAttrDomain(target);
		
	}
	
	// TODO how are we going to select the target domain if there is more than one candidate
	private DecisionTree select_target(InstanceList working_instances) {
		DecisionTree dt = null; 
		for (String target: input_data.getTargets()) {
			dt = new DecisionTree(input_data.getSchema(), target);
				
			//flog.debug("Num of attributes: "+ dt.getAttrDomains().size());
			
			InstDistribution stats_by_class = new InstDistribution(dt.getTargetDomain());
			stats_by_class.calculateDistribution(working_instances.getInstances());
			dt.FACTS_READ += working_instances.getSize();
			
			TreeNode root = train(dt, stats_by_class);
			dt.setRoot(root);
			//flog.debug("Result tree\n" + dt);
		}
		return dt;
	}
	
	
	public void setDataSizePerTree(int num) {
		this.data_size = num;
		
		missclassified_data = new HashSet<Instance>();
	}
	
	public int getDataSize() {
		return this.data_size;
	}
	
	public DecisionTree getTree() {
		return best_tree;
	}

	public DomainAlgo getDomainAlgo() {
		return this.algorithm;
	}
	
	public void setDomainAlgo(DomainAlgo type) {
		this.algorithm = type;
	}

	public void setInputData(InstanceList class_instances) {
		this.input_data = class_instances;	
	}
	
	public InstanceList getInputData() {
		return input_data;
	}

	public void setBestTree(DecisionTree dt) {
		this.best_tree = dt;
	}

}
