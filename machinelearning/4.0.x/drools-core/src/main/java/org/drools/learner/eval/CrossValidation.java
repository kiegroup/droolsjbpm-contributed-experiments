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
	
	private int k_fold;
	private double error_estimate;
	private ArrayList<DecisionTree> forest;
	
	private boolean WITH_REP = false;
	public CrossValidation(int _k) {
		k_fold = _k;
		forest = new ArrayList<DecisionTree> (k_fold);
		error_estimate = 0.0d;
	}
	
	// for small samples
	public void validate(InstanceList class_instances, Learner _trainer) {		
		if (class_instances.getTargets().size()>1 ) {
			//throw new FeatureNotSupported("There is more than 1 target candidates");
			if (flog.error() !=null)
				flog.error().log("There is more than 1 target candidates\n");
			System.exit(0);
			// TODO put the feature not supported exception || implement it
		}
	
		int N = class_instances.getSize();
		int[] bag;
		if (WITH_REP)
			bag = Util.bag_w_rep(N, N);
		else
			bag = Util.bag_wo_rep(N, N);	

		
		int fold_size = (int)N/k_fold;
		int i = 0;
//			int[] bag;		
		while (i++ < k_fold ) {
//			// first part divide = 0; divide < fold_size*i
//			// the validation set divide = fold_size*i; divide < fold_size*(i+1)-1
//			// last part divide = fold_size*(i+1); divide < N

			InstanceList learning_set = new InstanceList(class_instances.getSchema(), N- fold_size);
			InstanceList validation_set = new InstanceList(class_instances.getSchema(), fold_size);
			for (int divide_index = 0; divide_index <N; divide_index++){
				if (divide_index >= fold_size*i && divide_index < fold_size*(i+1)-1) { // validation
					validation_set.addAsInstance(class_instances.getInstance(bag[divide_index]));
				} else { // learninf part 
					learning_set.addAsInstance(class_instances.getInstance(bag[divide_index]));
				}
			}
						
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
			error_estimate += error/k_fold;


			if (slog.stat() !=null)
				slog.stat().stat(".");

		}
		// TODO how to compute a best tree from the forest
	}
	
	public double getErrorEstimate() {
		return error_estimate;
	}
	
	public ArrayList<DecisionTree> getEstimators() {
		return forest;
	}
	
	public int getEstimatorSize() {
		return k_fold;
	}
}
