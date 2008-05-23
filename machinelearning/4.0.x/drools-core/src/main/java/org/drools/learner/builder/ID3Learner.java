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
import org.drools.learner.TreeNode;
import org.drools.learner.eval.Entropy;
import org.drools.learner.eval.InstDistribution;


import org.drools.learner.tools.RulePrinter;
import org.drools.learner.tools.Util;

public class ID3Learner implements Learner {
	
	
	private int data_size;
	private DecisionTree id3;
	private InstanceList input_data;
	
	public ID3Learner() {
		this.data_size = 0;
	}
	
	/* TODO updating the working memory
	 * (non-Javadoc)
	 * @see org.drools.learner.Learner#build(org.drools.WorkingMemory)
	 * 
		
		ObjectSet fs = wm.getSet(klass, all_discrete);
		Collection<Domain<?>> domains = fs.getDomains();
		Object element = ObjectReader.read(klass, domains, line, separator);
		fs.insert(element);
	 */
	
	public void setDataSize(int num) {
		this.data_size = num;
	}
	
	public int getDataSize() {
		return this.data_size;
	}

	/* 
	 * the memory has the information 
	 * the instances: the objects which the decision tree will work on
	 * the schema: the definition of the object instance 
	 * 			(Class<?>) klass, String targetField, List<String> workingAttributes
	 */
	public void build(Memory wm) {
	
		//ArrayList<String> attrs = new ArrayList<String>(wm.getAttributes());
		//Collections.sort(attrs);
		// foreach instanceList
		this.input_data = wm.getClassInstances();
		for (Instance inst: input_data.getInstances()) {
			System.out.println("Inst: "+ inst);
		}
		
		this.setDataSize(input_data.getSize());
		DecisionTree dt = null; 
		for (String target: input_data.getTargets()) {
			dt = new DecisionTree(input_data.getSchema(), target);
			//dt.setTarget(target);
			
			if (Util.DEBUG_LEARNER) {
				System.out.println("Num of attributes: "+ dt.getAttrDomains().size());
			}
			//System.exit(0);
			InstDistribution stats_by_class = new InstDistribution(dt.getTargetDomain());
			stats_by_class.calculateDistribution(input_data.getInstances());
			dt.FACTS_READ += input_data.getSize();
			
			TreeNode root = train(dt, stats_by_class);
			dt.setRoot(root);
			if (Util.DEBUG_LEARNER) {
				System.out.println("Result tree\n" + dt);
			}
		}
		id3 = dt;
	}
	
	public Reader readRules() {
		if (this.id3 == null) {
			System.out.println("There is tree/rule to process");
			return null;
		}
		
		RulePrinter my_printer = new RulePrinter();	//bocuk.getNum_fact_trained()
		boolean sort_via_rank = true;
		boolean print = true;
		my_printer.printer(this.id3, sort_via_rank);
		
		String all_rules = my_printer.write2string();
		if (print) {
			//my_printer.write2file("examples", "src/rules/examples/" + file);
			if (Util.DEBUG_LEARNER) {
				System.out.println(all_rules);
			}
			my_printer.write2File(all_rules, false, "", Util.ID3 );
		}
		
		return new StringReader(all_rules);
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
			return noAttributeLeftNode;
		}
		
		
		/* id3 starts: choose the attribute according to the entropy function 
		 * entrophy function: data, info of the data wrt target domain, info for each attribute 
		 * */
		Domain node_domain = Entropy.chooseAttributeAsCategorical(data_stats, attribute_domains);
		
		//System.out.println(Util.ntimes("*", 20)+" 1st best attr: "+ node_domain);

		TreeNode currentNode = new TreeNode(node_domain);
		currentNode.setNumMatch(data_stats.getSum());									//num of matching instances to the leaf node
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
				TreeNode newNode = train(child_dt, filtered_stats.get(category));//, attributeNames_copy
				currentNode.putNode(category, newNode);
			}
		}
		return currentNode;
		
	}

	public DecisionTree getTree() {
		return id3;
	}
	
	public int getTreeType() {
		return Util.ID3;
	}
}
