package org.drools.learner.builder;

import java.util.Hashtable;
import java.util.List;

import org.drools.learner.DecisionTree;
import org.drools.learner.Domain;
import org.drools.learner.LeafNode;
import org.drools.learner.TreeNode;
import org.drools.learner.eval.AttributeChooser;
import org.drools.learner.eval.InstDistribution;
import org.drools.learner.eval.heuristic.Heuristic;
import org.drools.learner.tools.Util;

public class ID3Learner extends Learner {
	
	private AttributeChooser chooser;
	
	public ID3Learner(Heuristic hf) {
		super();
		super.setDomainAlgo(DomainAlgo.CATEGORICAL);
		chooser = new AttributeChooser(hf);
	}
	
	protected TreeNode train(DecisionTree dt, InstDistribution data_stats, int depth) {//List<Instance> data) {
		
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
									(double)this.getTrainingDataSize()/* total size of data fed to dt*/);
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
										(double)this.getTrainingDataSize()						/* total size of data fed to dt*/);
			noAttributeLeftNode.setNumMatch(data_stats.getSum());						//num of matching instances to the leaf node
			noAttributeLeftNode.setNumClassification(data_stats.getVoteFor(winner));	//num of classified instances at the leaf node
			return noAttributeLeftNode;
		}
		
		
		/* id3 starts: choose the attribute according to the entropy function 
		 * entrophy function: data, info of the data wrt target domain, info for each attribute 
		 * */
		Domain node_domain = chooser.chooseAttributeAsCategorical(data_stats, attribute_domains);
		
		
		if (flog.stat() !=null)
			flog.stat().log(Util.ntimes("*", 20)+" 1st best attr: "+ node_domain);

		TreeNode currentNode = new TreeNode(node_domain);
		currentNode.setNumMatch(data_stats.getSum());									//num of matching instances to the leaf node
		currentNode.setRank((double)data_stats.getSum()/
							(double)this.getTrainingDataSize()									/* total size of data fed to trainer*/);
		//currentNode.setInfoMea(best_attr_eval.attribute_eval);
		//what the highest represented class is and what proportion of items at that node actually are that class
		currentNode.setLabel(data_stats.get_winner_class());
		currentNode.setNumLabeled(data_stats.getSupportersFor(data_stats.get_winner_class()).size());
		
		
		Hashtable<Object, InstDistribution> filtered_stats = data_stats.splitFromCategorical(node_domain, null);
		dt.FACTS_READ += data_stats.getSum();
		
		for (int c=0; c<node_domain.getCategoryCount(); c++) {
			/* split the last two class at the same time */
			Object category = node_domain.getCategory(c);
			
			/* list of domains except the choosen one (&target domain)*/
			DecisionTree child_dt = new DecisionTree(dt, node_domain);	
			
			if (filtered_stats.get(category) == null || filtered_stats.get(category).getSum() ==0) {
				/* majority !!!! */
				LeafNode majorityNode = new LeafNode(dt.getTargetDomain(), data_stats.get_winner_class());
				majorityNode.setRank(-1.0);		//it does not classify any instance
				majorityNode.setNumMatch(0);
				majorityNode.setNumClassification(0);
				currentNode.putNode(category, majorityNode);
			} else {
				TreeNode newNode = train(child_dt, filtered_stats.get(category), depth+1);//, attributeNames_copy
				currentNode.putNode(category, newNode);
			}
		}
		return currentNode;
		
	}
}
