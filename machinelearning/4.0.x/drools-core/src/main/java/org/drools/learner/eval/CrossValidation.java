package org.drools.learner.eval;


import java.util.ArrayList;

import org.drools.learner.DecisionTree;
import org.drools.learner.InstanceList;
import org.drools.learner.Stats;
import org.drools.learner.builder.Learner;
import org.drools.learner.builder.SingleTreeTester;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;
import org.drools.learner.tools.Util;

public class CrossValidation implements Estimator{

	private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(CrossValidation.class, SimpleLogger.DEFAULT_LEVEL);
	private static SimpleLogger slog = LoggerFactory.getSysOutLogger(CrossValidation.class, SimpleLogger.DEFAULT_LEVEL);
	
	private int k_fold, num_instances;
	
	/* estimated values 
	 * validation_error_estimate is used to estimate the true misclassification rate
	 * */
	private double validation_error_estimate, training_error_estimate, num_leaves_estimate, alpha_estimate;
	private int [] crossed_set;
	private ArrayList<DecisionTree> forest;
	private InstanceList class_instances;
	
	private boolean WITH_REP = false;
	public CrossValidation(int _k, InstanceList _instances) {
		k_fold = _k;
		forest = new ArrayList<DecisionTree> (k_fold);
		validation_error_estimate = 0.0d;
		training_error_estimate = 0.0d;
		num_leaves_estimate = 0.0d;
		alpha_estimate = 0.0d;
		class_instances = _instances;
		
		num_instances = class_instances.getSize();
	}
	public int [] cross_set(int N) {

		if (WITH_REP)
			return Util.bag_w_rep(N, N);
		else
			return Util.bag_wo_rep(N, N);
	}
	
	
	// for small samples
	public void validate(Learner _trainer) {		
		if (class_instances.getTargets().size()>1 ) {
			//throw new FeatureNotSupported("There is more than 1 target candidates");
			if (flog.error() !=null)
				flog.error().log("There is more than 1 target candidates\n");
			System.exit(0);
			// TODO put the feature not supported exception || implement it
		}

		crossed_set = cross_set(num_instances);
		
		int fold_size = getFoldSize();
		int i = 0;
//			int[] bag;		
		while (i++ < k_fold ) {
			ArrayList<InstanceList> sets = getFold(i);
			InstanceList learning_set = sets.get(0);
			InstanceList validation_set = sets.get(1);
						
			DecisionTree dt = _trainer.train_tree(learning_set);
			dt.setID(i);
			forest.add(dt);
			
			int error = 0;
			SingleTreeTester t= new SingleTreeTester(dt);
			for (int index_i = 0; index_i < fold_size; index_i++) {
				Integer result = t.test(validation_set.getInstance(index_i));
				if (result == Stats.INCORRECT) {
					error ++;
				}
			}
			dt.setValidationError(error);
			dt.calc_numleaves(dt.getRoot());
			validation_error_estimate += error/k_fold;
			training_error_estimate += dt.getTrainingError()/k_fold;
			num_leaves_estimate += dt.getRoot().getNumLeaves()/k_fold;


			if (slog.stat() !=null)
				slog.stat().stat(".");

		}
		alpha_estimate = (validation_error_estimate - training_error_estimate) /num_leaves_estimate;
		
		// TODO how to compute a best tree from the forest
	}
	
	public ArrayList<InstanceList> getFold(int i) {
//		// first part divide = 0; divide < fold_size*i
//		// the validation set divide = fold_size*i; divide < fold_size*(i+1)-1
//		// last part divide = fold_size*(i+1); divide < N
		int fold_size = getFoldSize();
		InstanceList learning_set = new InstanceList(class_instances.getSchema(), num_instances - fold_size +1);
		InstanceList validation_set = new InstanceList(class_instances.getSchema(), fold_size);
		for (int divide_index = 0; divide_index < num_instances; divide_index++){
			if (divide_index >= fold_size*i && divide_index < fold_size*(i+1)-1) { // validation
				validation_set.addAsInstance(class_instances.getInstance(crossed_set[divide_index]));
			} else { // learninf part 
				learning_set.addAsInstance(class_instances.getInstance(crossed_set[divide_index]));
			}
		}
		
		ArrayList<InstanceList> lists = new ArrayList<InstanceList>(2);
		lists.add(learning_set);
		lists.add(validation_set);
		return lists;
		
	}
	
	
	private int getFoldSize() {
		// TODO Auto-generated method stub
		return (int) num_instances/k_fold;
	}
	public double getValidationErrorEstimate() {
		return validation_error_estimate;
	}
	
	public ArrayList<DecisionTree> getEstimators() {
		return forest;
	}
	
	public int getEstimatorSize() {
		return k_fold;
	}
}
