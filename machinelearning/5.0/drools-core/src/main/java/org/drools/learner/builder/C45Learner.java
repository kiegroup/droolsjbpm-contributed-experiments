package org.drools.learner.builder;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.drools.learner.DecisionTree;
import org.drools.learner.Domain;
import org.drools.learner.LeafNode;
import org.drools.learner.TreeNode;
import org.drools.learner.eval.AttributeChooser;
import org.drools.learner.eval.InformationContainer;
import org.drools.learner.eval.InstDistribution;
import org.drools.learner.eval.heuristic.Heuristic;
import org.drools.learner.eval.stopping.StoppingCriterion;
import org.drools.learner.tools.FeatureNotSupported;
import org.drools.learner.tools.Util;

public class C45Learner extends Learner{
	
	private AttributeChooser chooser;
	
	
	public C45Learner(Heuristic hf) {
		super();
		super.setDomainAlgo(DomainAlgo.QUANTITATIVE);
		chooser = new AttributeChooser(hf);
	}
	
	
	protected TreeNode train(DecisionTree dt, InstDistribution data_stats,  int depth) {//List<Instance> data) {
		
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
			classifiedNode.setRank(	data_stats.getSum()/
									(double)this.getTrainingDataSize()/* total size of data fed to dt*/);
			classifiedNode.setNumMatch(data_stats.getSum());						//num of matching instances to the leaf node
			classifiedNode.setNumClassification(data_stats.getSum());				//num of classified instances at the leaf node
			
			//classifiedNode.setInfoMea(mea)
			return classifiedNode;
		}
		
		List<Domain> attribute_domains = dt.getAttrDomains();
		/* if  there is no attribute left in order to continue */
		if (attribute_domains.size() == 0) {
			/* an heuristic of the leaf classification*/
			Object winner = data_stats.get_winner_class();								/*winner target category*/
			LeafNode noAttributeLeftNode = new LeafNode(dt.getTargetDomain()			/* target domain*/, 
														winner);
			noAttributeLeftNode.setRank(data_stats.getVoteFor(winner)/
										(double)this.getTrainingDataSize()						/* total size of data fed to dt*/);
			noAttributeLeftNode.setNumMatch(data_stats.getSum());						//num of matching instances to the leaf node
			noAttributeLeftNode.setNumClassification(data_stats.getVoteFor(winner));	//num of classified instances at the leaf node
			//noAttributeLeftNode.setInfoMea(best_attr_eval.attribute_eval);
			
			
			/* we need to know how many guys cannot be classified and who these guys are */
			data_stats.missClassifiedInstances(missclassified_data);
			dt.changeTrainError((data_stats.getSum() - data_stats.getVoteFor(winner))/(double)getTrainingDataSize());
			return noAttributeLeftNode;
		}
	
		InformationContainer best_attr_eval = new InformationContainer();
		best_attr_eval.setStats(data_stats);
		best_attr_eval.setDepth(depth);
		best_attr_eval.setTotalNumData(getTrainingDataSizePerTree());

		/* choosing the best attribute in order to branch at the current node*/
		chooser.chooseAttribute(best_attr_eval, data_stats, attribute_domains);
		
		if (super.criteria != null && criteria.size()>0) {
			for (StoppingCriterion sc: criteria) 
				if (sc.stop(best_attr_eval)) {
					Object winner = data_stats.get_winner_class();
					LeafNode majorityNode = new LeafNode(dt.getTargetDomain(), winner);
					majorityNode.setRank((double)data_stats.getVoteFor(winner)/
										 (double)this.getTrainingDataSize()						/* total size of data fed to trainer*/);
					majorityNode.setNumMatch(data_stats.getSum());
					majorityNode.setNumClassification(data_stats.getVoteFor(winner));
					
					/* we need to know how many guys cannot be classified and who these guys are */
					data_stats.missClassifiedInstances(missclassified_data);
					dt.changeTrainError((data_stats.getSum() - data_stats.getVoteFor(winner))/(double)getTrainingDataSize());
					return majorityNode;
				}
		}
		Domain node_domain = best_attr_eval.domain;
		if (slog.debug() != null)
			slog.debug().log("\n"+Util.ntimes("*", 20)+" 1st best attr: "+ node_domain);

		TreeNode currentNode = new TreeNode(node_domain);
		currentNode.setNumMatch(data_stats.getSum());									//num of matching instances to the leaf node
		currentNode.setRank(data_stats.getSum()/
							(double)this.getTrainingDataSize()									/* total size of data fed to trainer*/);
		currentNode.setInfoMea(best_attr_eval.attribute_eval);
		//what the highest represented class is and what proportion of items at that node actually are that class
		currentNode.setLabel(data_stats.get_winner_class());
		currentNode.setNumLabeled(data_stats.getSupportersFor(data_stats.get_winner_class()).size());

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
			if (slog.debug() != null)
				slog.debug().log("{"+ node_domain +":"+category+ "}"+(c==node_domain.getCategoryCount()-1?"\n":""));
			
			/* list of domains except the choosen one (&target domain)*/
			DecisionTree child_dt = new DecisionTree(dt, node_domain);	
			child_dt.FACTS_READ = dt.FACTS_READ;
			
			if (filtered_stats == null || filtered_stats.get(category) == null || filtered_stats.get(category).getSum() ==0) {
				/* majority !!!! */
				LeafNode majorityNode = new LeafNode(dt.getTargetDomain(), data_stats.get_winner_class());
				majorityNode.setRank(-1.0);		//it does not classify any instance
				majorityNode.setNumMatch(0);
				majorityNode.setNumClassification(0);
				//currentNode.setInfoMea(best_attr_eval.attribute_eval);
				//dt.setTrainingError((int) (dt.getTrainingError() + data_stats.getSum()));
				
				majorityNode.setFather(currentNode);
				currentNode.putNode(category, majorityNode);
			} else {
				TreeNode newNode = train(child_dt, filtered_stats.get(category), depth+1);//, attributeNames_copy
				newNode.setFather(currentNode);
				currentNode.putNode(category, newNode);
			}
		}

		return currentNode;
		
	}
	

}
