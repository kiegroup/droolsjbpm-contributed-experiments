package org.drools.learner.builder;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.drools.learner.DecisionTree;
import org.drools.learner.Domain;
import org.drools.learner.Instance;
import org.drools.learner.InstanceList;
import org.drools.learner.LeafNode;
import org.drools.learner.Memory;
import org.drools.learner.Stats;
import org.drools.learner.TreeNode;
import org.drools.learner.eval.Entropy;
import org.drools.learner.eval.InformationContainer;
import org.drools.learner.eval.InstDistribution;
import org.drools.learner.tools.FeatureNotSupported;
import org.drools.learner.tools.RulePrinter;
import org.drools.learner.tools.Util;

public class C45Learner implements Learner{
	
	
	private int data_size;
	private DecisionTree c45;
	private InstanceList input_data;
	protected ArrayList<Instance> missclassified_data;
	public C45Learner() {
		this.data_size = 0;
	}
	
	public void setDataSize(int num) {
		this.data_size = num;
		
		missclassified_data = new ArrayList<Instance>();
	}
	
	public int getDataSize() {
		return this.data_size;
	}
	
	public void build(Memory wm) {
		
		InstanceList class_instances = wm.getClassInstances();
		input_data = class_instances;
		if (class_instances.getTargets().size()>1) {
			//throw new FeatureNotSupported("There is more than 1 target candidates");
			System.out.println("There is more than 1 target candidates");
			System.exit(0);
			// TODO put the feature not supported exception || implement it
		}
		
		if (Util.DEBUG_LEARNER)
			for (Instance inst: class_instances.getInstances()) {
				System.out.println("Inst: "+ inst);
			}
		
		this.setDataSize(class_instances.getSize());
		DecisionTree dt = null; 
		for (String target: class_instances.getTargets()) {
			dt = new DecisionTree(class_instances.getSchema(), target);;
			if (Util.DEBUG_LEARNER) {
				System.out.println("Num of attributes: "+ dt.getAttrDomains().size());
			}
			//System.exit(0);
			InstDistribution stats_by_class = new InstDistribution(dt.getTargetDomain());
			stats_by_class.calculateDistribution(class_instances.getInstances());
			dt.FACTS_READ += class_instances.getSize();
			
			TreeNode root = train(dt, stats_by_class);
			dt.setRoot(root);
			if (Util.DEBUG_LEARNER) {
				System.out.println("Result tree\n" + dt);
			}
		}
		this.c45 = dt;
	}
	
	public TreeNode train(DecisionTree dt, InstDistribution data_stats) {//List<Instance> data) {
		
		if (data_stats.getSum() == 0) {
			throw new RuntimeException("Nothing to classify, factlist is empty");
		}
		
		/* let's get the statistics of the results */
		//InstDistribution stats_by_class = new InstDistribution(dt.getTargetDomain());///* the target domain*/);
		data_stats.evaluateMajority();
		
		
		/* if all elements are classified to the same value */
		if (data_stats.get_num_ideas(/* number of supported target categories*/) == 1) {

			LeafNode classifiedNode = new LeafNode(dt.getTargetDomain()				/* target domain*/, 
												   data_stats.get_winner_class() 	/*winner target category*/);
			classifiedNode.setRank(	(double)data_stats.getSum()/
									(double)this.getDataSize()/* total size of data fed to dt*/);
			classifiedNode.setNumMatch(data_stats.getSum());						//num of matching instances to the leaf node
			classifiedNode.setNumClassification(data_stats.getSum());				//num of classified instances at the leaf node
			return classifiedNode;
		}
		
		List<Domain> attribute_domains = dt.getAttrDomains();
		/* if  there is no attribute left in order to continue */
		if (attribute_domains.size() == 0) {
			/* an heuristic of the leaf classification*/
			Object winner = data_stats.get_winner_class();								/*winner target category*/
			LeafNode noAttributeLeftNode = new LeafNode(dt.getTargetDomain()			/* target domain*/, 
														winner);
			noAttributeLeftNode.setRank((double)data_stats.getVoteFor(winner)/
										(double)this.getDataSize()						/* total size of data fed to dt*/);
			noAttributeLeftNode.setNumMatch(data_stats.getSum());						//num of matching instances to the leaf node
			noAttributeLeftNode.setNumClassification(data_stats.getVoteFor(winner));	//num of classified instances at the leaf node
			
			/* we need to know how many guys cannot be classified and who these guys are */
			data_stats.missClassifiedInstances(missclassified_data);
			
			return noAttributeLeftNode;
		}
		
	
		InformationContainer best_attr_eval = new InformationContainer();
		/* choosing the best attribute in order to branch at the current node*/
		Entropy.chooseAttribute(best_attr_eval, data_stats, attribute_domains);
		Domain node_domain = best_attr_eval.domain;
		
		if (Util.DEBUG_LEARNER) {
			System.out.println(Util.ntimes("*", 20)+" 1st best attr: "+ node_domain);
		}

		TreeNode currentNode = new TreeNode(node_domain);
		currentNode.setNumMatch(data_stats.getSum());									//num of matching instances to the leaf node
		currentNode.setRank((double)data_stats.getSum()/
							(double)this.getDataSize()									/* total size of data fed to dt*/);
		
		
		Hashtable<Object, InstDistribution> filtered_stats = null;
		try {
			filtered_stats = data_stats.split(best_attr_eval);
		} catch (FeatureNotSupported e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// node_domain);
		
		dt.FACTS_READ += data_stats.getSum();
		
		for (int c = 0; c<node_domain.getCategoryCount(); c++) {
			/* split the last two class at the same time */
			Object category = node_domain.getCategory(c);
			
			/* list of domains except the choosen one (&target domain)*/
			DecisionTree child_dt = new DecisionTree(dt, node_domain);	
			
			if (filtered_stats == null || filtered_stats.get(category) == null || filtered_stats.get(category).getSum() ==0) {
				/* majority !!!! */
				LeafNode majorityNode = new LeafNode(dt.getTargetDomain(), data_stats.get_winner_class());
				majorityNode.setRank(-1.0);		//it does not classify any instance
				majorityNode.setNumMatch(0);
				majorityNode.setNumClassification(0);
				currentNode.putNode(category, majorityNode);
			} else {
				TreeNode newNode = train(child_dt, filtered_stats.get(category));//, attributeNames_copy
				currentNode.putNode(category, newNode);
			}
		}
		return currentNode;
		
	}
	
	public DecisionTree getTree() {
		return c45;
	}

	public int getTreeType() {
		return Util.C45;
	}
	

}
