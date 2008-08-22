package org.drools.learner.builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.drools.learner.DecisionTree;
import org.drools.learner.Domain;
import org.drools.learner.Instance;
import org.drools.learner.InstanceList;
import org.drools.learner.TreeNode;
import org.drools.learner.eval.InstDistribution;
import org.drools.learner.eval.stopping.StoppingCriterion;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;

public abstract class Learner {
	
	protected static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(Learner.class, SimpleLogger.DEFAULT_LEVEL);
	protected static SimpleLogger slog = LoggerFactory.getSysOutLogger(Learner.class, SimpleLogger.DEFAULT_LEVEL);
	
	public static enum DomainAlgo { CATEGORICAL, QUANTITATIVE }
	public static DomainAlgo DEFAULT_DOMAIN = DomainAlgo.QUANTITATIVE;
	
	public static enum DataType {PRIMITIVE, STRUCTURED, COLLECTION}
	public static DataType DEFAULT_DATA = DataType.PRIMITIVE;
	private int data_size, data_size_per_tree;
	
	// must be deleted, goes to builder	
//	private DecisionTree best_tree;
	private InstanceList input_data;
	protected HashSet<Instance> missclassified_data;
	

	private DomainAlgo algorithm;
	
	protected ArrayList<StoppingCriterion> criteria; 
	
	
	protected abstract TreeNode train(DecisionTree dt, InstDistribution data_stats, int depth);
	
	public Learner() {
		this.data_size = 0;
		this.data_size_per_tree = 0;
		
		criteria = new ArrayList<StoppingCriterion>(4);
		missclassified_data = new HashSet<Instance>();
	}

	
	public DecisionTree instantiate_tree() {
		String target_reference = this.getTargetDomain().getFReferenceName();
		//System.out.println("(Learner) target   "+ target_reference);
		DecisionTree dt = new DecisionTree(input_data.getSchema(), target_reference);

		//flog.debug("Num of attributes: "+ dt.getAttrDomains().size());
		return dt;
	}	
	public void train_tree(DecisionTree dt, InstanceList working_instances) {
		InstDistribution stats_by_class = new InstDistribution(dt.getTargetDomain());
		stats_by_class.calculateDistribution(working_instances.getInstances());

		dt.FACTS_READ += working_instances.getSize();

		TreeNode root = train(dt, stats_by_class, 0);
		dt.setRoot(root);
		//flog.debug("Result tree\n" + dt);
//		return dt;
	}
	
	public DecisionTree train_tree(InstanceList working_instances) {
		String target_reference = this.getTargetDomain().getFReferenceName();
		//System.out.println("(Learner) target   "+ target_reference);
		DecisionTree dt = new DecisionTree(input_data.getSchema(), target_reference);

		//flog.debug("Num of attributes: "+ dt.getAttrDomains().size());
		
		InstDistribution stats_by_class = new InstDistribution(dt.getTargetDomain());
		stats_by_class.calculateDistribution(working_instances.getInstances());
		
		
		dt.FACTS_READ += working_instances.getSize();

		TreeNode root = train(dt, stats_by_class, 0);
		dt.setRoot(root);
		//flog.debug("Result tree\n" + dt);
		return dt;
	}
	
	public Domain getTargetDomain() {
		Iterator<String> it_target= input_data.getTargets().iterator();
		// TODO check if there is a target candidate
		String target = it_target.next();
		//System.out.println("(Learner) What is target?? "+ target +" and the domain "+ input_data.getSchema().getAttrDomain(target));
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
			
			TreeNode root = train(dt, stats_by_class, 0);
			dt.setRoot(root);
			//flog.debug("Result tree\n" + dt);
		}
		return dt;
	}
	public ArrayList<StoppingCriterion> getStoppingCriteria() {
		return criteria;
	}
	
	public void addStoppingCriteria(StoppingCriterion _criteria) {
		criteria.add(_criteria);
	}
	
	public void setTrainingDataSizePerTree(int num) {
		this.data_size_per_tree = num;
	}
	
	public int getTrainingDataSizePerTree() {
		return this.data_size_per_tree;
	}
	
	public void setTrainingDataSize(int num) {
		this.data_size = num;
	}
	public int getTrainingDataSize() {
		return this.data_size;
	}
// must be deleted, goes to builder	
//	public DecisionTree getTree() {
//		return best_tree;
//	}

	public DomainAlgo getDomainAlgo() {
		return this.algorithm;
	}
	
	public void setDomainAlgo(DomainAlgo type) {
		this.algorithm = type;
	}

	public void setInputSpec(InstanceList class_instances) {
		this.input_data = class_instances;	
	}
	
//	public InstanceList getInputData() {
//		return input_data;
//	}

	// must be deleted, goes to builder	
//	public void setBestTree(DecisionTree dt) {
//		this.best_tree = dt;
//	}

}
