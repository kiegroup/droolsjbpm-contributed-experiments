package org.drools.learner.builder;

import java.util.ArrayList;

import org.drools.learner.AttributeValueComparator;
import org.drools.learner.DecisionTree;
import org.drools.learner.Instance;
import org.drools.learner.InstanceList;
import org.drools.learner.Memory;
import org.drools.learner.Stats;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;
import org.drools.learner.tools.Util;

public class AdaBoostKBuilder implements DecisionTreeBuilder{

	private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(AdaBoostKBuilder.class, SimpleLogger.DEFAULT_LEVEL);
	private static SimpleLogger slog = LoggerFactory.getSysOutLogger(AdaBoostKBuilder.class, SimpleLogger.DEBUG);
	
	private TreeAlgo algorithm = TreeAlgo.BOOST_K; // default bagging, TODO boosting
	
	private static int FOREST_SIZE = 10;
	private static final double TREE_SIZE_RATIO = 0.9;
	private static final boolean WITH_REP = false;
	
	private ArrayList<DecisionTree> forest;
	private ArrayList<Double> classifier_accuracy;
	//private Learner trainer;
	
	public AdaBoostKBuilder() {
		//this.trainer = _trainer;
	}
	public void build(Memory mem, Learner _trainer) {
		
		final InstanceList class_instances = mem.getClassInstances();
		_trainer.setInputData(class_instances);
		
		
		if (class_instances.getTargets().size()>1 ) {
			//throw new FeatureNotSupported("There is more than 1 target candidates");
			if (flog.error() !=null)
				flog.error().log("There is more than 1 target candidates");
			System.exit(0);
			// TODO put the feature not supported exception || implement it
		} else if (_trainer.getTargetDomain().getCategoryCount() <=2) {
			if (flog.warn() !=null)
				flog.warn().log("The target domain is binary!!! Do u really need that one");
		}
	
		int N = class_instances.getSize();
		int K = _trainer.getTargetDomain().getCategoryCount();
		int M = (int)(TREE_SIZE_RATIO * N);
		_trainer.setDataSizePerTree(M);

		
		forest = new ArrayList<DecisionTree> (FOREST_SIZE);
		classifier_accuracy = new ArrayList<Double>(FOREST_SIZE);
		//Initialize the weight wij  (i = 1,...,m (number of instances) and j = 1,...,K (number of classes))
		double[][] weight = new double[M][K];
		for (int index_i=0; index_i<M; index_i++) {
			for (int index_j=0; index_j<K; index_j++) {
				Instance inst_i = class_instances.getInstance(index_i);
				
				
				Object instance_target = inst_i.getAttrValue(_trainer.getTargetDomain().getFName());
				Object instance_target_category = _trainer.getTargetDomain().getCategoryOf(instance_target);
				Object target_category= _trainer.getTargetDomain().getCategory(index_j);
				
				if (AttributeValueComparator.instance.compare(instance_target_category, target_category) == 0) {
					//if y_i == u_j => the instance i is in class n.
					weight[index_i][index_j]= 0;
				} else {
					// mistake
					weight[index_i][index_j]= 1;
				} 
				
			}
		}
		
		int i = 0;
		int[] bag;		
		while (i++ < FOREST_SIZE ) {
			// a.  Normalize wij
			if (WITH_REP)
				bag = Util.bag_w_rep(M, N);
			else
				bag = Util.bag_wo_rep(M, N);
			

			// b. Train h_t(x) by minimizing loss function
			
			InstanceList working_instances = class_instances.getInstances(bag);			
			DecisionTree dt = _trainer.train_tree(working_instances);
			dt.setID(i);
			
			double error = 0.0;
			SingleTreeTester t= new SingleTreeTester(dt);
			for (int index_i = 0; index_i < M; index_i++) {
				Integer result = t.test(class_instances.getInstance(index_i));
				if (result == Stats.INCORRECT) {
				
					//error += distribution.get(index_i);
				}
			}
			
			if (error > 0.0f) {
				double alpha = Util.ln( (1.0d-error)/error ) / 2.0d;
				
				if (error < 0.5d) {
					// The classification accuracy of the weak classifier
					classifier_accuracy.add(alpha);
					
					double norm_fact= 0.0d;
					// Update the weight matrix wij:
					for (int index_i = 0; index_i < M; index_i++) {
						Integer result = t.test(class_instances.getInstance(index_i));//TODO dont need to test two times
						switch (result) {
						case Stats.INCORRECT:
							//distribution.set(index_i, distribution.get(index_i) * Util.exp(alpha));
							break;
						case Stats.CORRECT:
							//distribution.set(index_i, distribution.get(index_i) * Util.exp(-1.0d * alpha));
							break;
						case Stats.UNKNOWN:
							if (slog.error() !=null)
								slog.error().log("Unknown situation bok");
							System.exit(0);
							break;
						}
						//norm_fact += distribution.get(index_i);
					}
					// Normalization of the weights
					for (int index_i = 0; index_i < M; index_i++) {
						//distribution.set(index_i, distribution.get(index_i) / norm_fact);
					}
				} else {
					if (slog.debug() != null)
						slog.debug().log("The error="+error+" alpha:"+alpha+ "\n");
					if (slog.error() != null)
						slog.error().log("error:"+error + " alpha will be negative and the weights of the training samples will be updated in the wrong direction"+"\n");
					FOREST_SIZE = i-1;//ignore the current tree
					break;
				}
			}
			
			
			else {
				if (slog.stat() != null)
					slog.stat().log("All instances classified correctly TERMINATE, forest size:"+i+ "\n");
				// What to do here??
				FOREST_SIZE = i;
				classifier_accuracy.add(10.0); // TODO add a very big number
				
				
			}
			
			
			forest.add(dt);

			if (slog.stat() !=null)
				slog.stat().stat(".");

		}
		// TODO how to compute a best tree from the forest
		_trainer.setBestTree(forest.get(0));
		
		//this.c45 = dt;
	}

	public ArrayList<DecisionTree> getTrees() {
		return forest;
	}
	public ArrayList<Double> getAccuracies() {
		return classifier_accuracy;
	}
	public TreeAlgo getTreeAlgo() {
		return algorithm; //TreeAlgo.BAG; // default
	}
	public void clearForest(int j) {
		//forest = null;
		for (int i = forest.size()-1; i >j; i--)
			forest.remove(i);
		
	}
}
