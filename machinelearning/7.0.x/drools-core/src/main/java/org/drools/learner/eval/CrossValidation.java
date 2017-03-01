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

public class CrossValidation implements ErrorEstimate{

	private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(CrossValidation.class, SimpleLogger.DEFAULT_LEVEL);
	private static SimpleLogger slog = LoggerFactory.getSysOutLogger(CrossValidation.class, SimpleLogger.DEBUG);
	
	private int k_fold, num_instances;
	
	private final int MIN_NUM_FOLDS = 2;
	
	/* estimated values 
	 * validation_error_estimate is used to estimate the true misclassification rate
	 * */
	private double validation_error_estimate, training_error_estimate, num_leaves_estimate, alpha_estimate;
	private int [] crossed_set;
	private int [] [] fold_indices;
	private ArrayList<DecisionTree> forest;
	private InstanceList class_instances;
	
	private int VALID_SET_0 = 0, VALID_SET_1 = 1;
	
	private boolean WITH_REP = false;

	
	public CrossValidation(int _k) {
		if (_k <=1) {
			if (flog.warn() !=null)
				flog.warn().log("There is 1 or less number of folds specified, i am setting "+MIN_NUM_FOLDS+" folds\n");
			k_fold = MIN_NUM_FOLDS;
		} else {
			k_fold = _k;
		}
		forest = new ArrayList<DecisionTree> (k_fold);
		validation_error_estimate = 0.0d;
		training_error_estimate = 0.0d;
		num_leaves_estimate = 0.0d;
		alpha_estimate = 0.0d;
		
		fold_indices = new int [k_fold] [2];
	}
	public int [] cross_set(int N) {
		if (WITH_REP)
			return Util.bag_w_rep(N, N);
		else
			return Util.bag_wo_rep(N, N);
	}
	
	
	// for small samples
	public void validate(Learner _trainer, InstanceList _instances) {		
		class_instances = _instances;
		num_instances = class_instances.getSize();
		if (class_instances.getTargets().size()>1 ) {
			//throw new FeatureNotSupported("There is more than 1 target candidates");
			if (flog.error() !=null)
				flog.error().log("There is more than 1 target candidates\n");
			System.exit(0);
			// TODO put the feature not supported exception || implement it
		}
	
		crossed_set = cross_set(num_instances);
		
		// N-fold 
		calc_fold_indices(num_instances);
		
		int i = 0;
//			int[] bag;		
		while (i++ < k_fold ) {
			int fold_size = getTestDataSize(i);
			if (slog.debug() !=null)
				slog.debug().log("i "+(i-1)+"/"+k_fold);
			ArrayList<InstanceList> sets = getSets(i-1);
			InstanceList learning_set = sets.get(0);
			InstanceList validation_set = sets.get(1);
						
			DecisionTree dt = _trainer.train_tree(learning_set);
			dt.setID(i-1);
			forest.add(dt);
			
			int error = 0;
			SingleTreeTester t= new SingleTreeTester(dt);
			if (slog.debug() !=null)
				slog.debug().log("validation fold_size " +fold_size + "\n");
			for (int index_i = 0; index_i < fold_size; index_i++) {
				
				if (slog.warn() !=null)
					slog.warn().log(" validation index_i " +index_i + (index_i ==fold_size -1?"\n":""));
				Integer result = t.test(validation_set.getInstance(index_i));
				if (result == Stats.INCORRECT) {
					error ++;
				}
			}
			//TODO dt.setTrainError(Util.division(error, fold_size));
			dt.calc_num_node_leaves(dt.getRoot());
			
//			if (slog.error() !=null)
//				slog.error().log("The estimate of : "+(i-1)+" training=" +dt.getTrainingError() +" valid=" + dt.getValidationError() +" num_leaves=" + dt.getRoot().getNumLeaves()+"\n");
		
			/* moving averages */
			validation_error_estimate += ((double)error/(double) fold_size)/(double)k_fold;
			//TODO training_error_estimate += ((double)dt.getTrainingError())/(double)k_fold;//((double)dt.getTrainingError()/(double)(num_instances-fold_size))/(double)k_fold;
			num_leaves_estimate += (double)dt.getRoot().getNumLeaves()/(double)k_fold;


//			if (slog.stat() !=null)
//				slog.stat().stat("."+ (i == k_fold?"\n":""));

		}
		alpha_estimate = (validation_error_estimate - training_error_estimate) /num_leaves_estimate;
		if (slog.stat() !=null)
			slog.stat().log(" The estimates: training=" +training_error_estimate +" valid=" + validation_error_estimate +" num_leaves=" + num_leaves_estimate+ " the alpha"+ alpha_estimate+"\n");
		// TODO how to compute a best tree from the forest
	}
	
	private void calc_fold_indices(int num_instances2) {
		// TODO Auto-generated method stub
		System.out.println(num_instances2 + " by "+ k_fold);
		int excess = num_instances2 % k_fold;
		int fold_index = 0, divide_index = 0;
		while (fold_index < k_fold) {
			
			fold_indices[fold_index][VALID_SET_0] = divide_index;
			int fold_size = getTestDataSize(fold_index); 
			fold_indices[fold_index][VALID_SET_1] = fold_indices[fold_index][VALID_SET_0] +  fold_size -1;
			
			System.out.println(fold_indices[fold_index][VALID_SET_0] +" - "+ fold_indices[fold_index][VALID_SET_1]);
			divide_index+= fold_size; 
			fold_index++;
		}
		
	}
	public ArrayList<InstanceList> getSets(int i) {
//		// first part divide = 0; divide < fold_size*i
//		// the validation set divide = fold_size*i; divide < fold_size*(i+1)-1
//		// last part divide = fold_size*(i+1); divide < N
		int valid_fold_size = getTestDataSize(i);
		InstanceList learning_set = new InstanceList(class_instances, num_instances - valid_fold_size + 1);
		InstanceList validation_set = new InstanceList(class_instances, valid_fold_size);
		for (int divide_index = 0; divide_index < num_instances; divide_index++){
			
			if (slog.info() !=null)
				slog.info().log("index " +divide_index+ " fold_size" + valid_fold_size + " i "+i+" = from "+fold_indices[i][VALID_SET_0]+" to "+fold_indices[i][VALID_SET_1]+" num_instances "+num_instances+ "\n");
			if (divide_index >= fold_indices[i][VALID_SET_0] && divide_index <= fold_indices[i][VALID_SET_1]) { // validation
				// validation [fold_size*i, fold_size*(i+1))
				if (slog.info() !=null)
					slog.info().log("validation one " +divide_index+ "\n");
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
	
	
	public int getTrainingDataSize(int i) {
		return num_instances-getTestDataSize(i);
	}
	
	public double getAlphaEstimate() {
		return alpha_estimate;
	}
	public int getTestDataSize(int i) {
		int excess = num_instances % k_fold;
		return (int) num_instances/k_fold + (i < excess? 1:0);
	}
	public double getErrorEstimate() {
		return validation_error_estimate;
	}
	
	public DecisionTree getEstimator(int i) {
		return forest.get(i);
	}
	
	public int getEstimatorSize() {
		return k_fold;
	}
	public void setTrainingDataSize(int trainingDataSize) {
		// TODO Auto-generated method stub
		
	}
}
