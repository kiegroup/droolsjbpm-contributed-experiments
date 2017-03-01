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

public class TestSample implements ErrorEstimate{
	
	private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(TestSample.class, SimpleLogger.DEFAULT_LEVEL);
	private static SimpleLogger slog = LoggerFactory.getSysOutLogger(TestSample.class, SimpleLogger.DEBUG);
	
	private int TEST_SET_0 = 0, TEST_SET_1 = 0;
	
	private InstanceList class_instances;
	private double error_estimate, training_error_estimate, num_leaves_estimate, alpha_estimate;
	private boolean WITH_REP = false;
	private int num_instances, training_data_size;
	private int[] crossed_set;
	private double test_ratio;
	
	private DecisionTree dt;
	
	public TestSample(double _ratio) {
		test_ratio = _ratio;
		error_estimate = 0.0d;
		training_error_estimate = 0.0d;
		num_leaves_estimate = 0.0d;
		alpha_estimate = 0.0d;
		
	}
	
	public int [] cross_set(int N) {
		if (WITH_REP )
			return Util.bag_w_rep(N, N);
		else
			return Util.bag_wo_rep(N, N);
	}
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
		
		TEST_SET_1 = (int)(test_ratio * num_instances)+1;
		System.out.println(test_ratio +"*"+ num_instances+" "+TEST_SET_1);
		
		crossed_set = cross_set(num_instances);
		
		ArrayList<InstanceList> sets = getSets(0);
		InstanceList learning_set = sets.get(0);
		InstanceList test_set = sets.get(1);
					
		dt = _trainer.train_tree(learning_set);
		dt.setID(0);

		
		int error = 0;
		SingleTreeTester t= new SingleTreeTester(dt);
		if (slog.debug() !=null)
			slog.debug().log("validation fold_size " +test_set.getSize() + "\n");
		for (int index_i = 0; index_i < test_set.getSize(); index_i++) {
			
			if (slog.warn() !=null)
				slog.warn().log(" validation index_i " +index_i + (index_i ==test_set.getSize() -1?"\n":""));
			Integer result = t.test(test_set.getInstance(index_i));
			if (result == Stats.INCORRECT) {
				error ++;
			}
		}
		//TODO dt.setValidationError(Util.division(error, test_set.getSize()));
		dt.calc_num_node_leaves(dt.getRoot());
		
//		if (slog.error() !=null)
//			slog.error().log("The estimate of : "+(0)+" training=" +dt.getTrainingError() +" valid=" + dt.getValidationError() +" num_leaves=" + dt.getRoot().getNumLeaves()+"\n");
	
		/* moving averages */
		//TODO error_estimate = dt.getValidationError();
		//TODO training_error_estimate = (double)dt.getTrainingError();
		num_leaves_estimate = (double)dt.getRoot().getNumLeaves();


//		if (slog.stat() !=null)
//			slog.stat().stat("."+ (i == k_fold?"\n":""));
		alpha_estimate = (error_estimate - training_error_estimate) /num_leaves_estimate;
		if (slog.stat() !=null)
			slog.stat().log(" The estimates: training=" +training_error_estimate +" valid=" + error_estimate +" num_leaves=" + num_leaves_estimate+ " the alpha"+ alpha_estimate+"\n");
		// TODO how to compute a best tree from the forest
	}
	
	public ArrayList<InstanceList> getSets(int i) {
//		// first part divide = 0; divide < fold_size*i
//		// the validation set divide = fold_size*i; divide < fold_size*(i+1)-1
//		// last part divide = fold_size*(i+1); divide < N
		InstanceList learning_set = new InstanceList(class_instances, num_instances - TEST_SET_1 +1);
		InstanceList validation_set = new InstanceList(class_instances, TEST_SET_1);
		for (int divide_index = 0; divide_index < num_instances; divide_index++){
			
			if (slog.info() !=null)
				slog.info().log("index " +divide_index+ " fold_size" + TEST_SET_1 + " = from "+TEST_SET_0+" to "+TEST_SET_1+" num_instances "+num_instances+ "\n");
			if (divide_index >= TEST_SET_0 && divide_index <= TEST_SET_1) { // validation
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

	public double getAlphaEstimate() {
		return alpha_estimate;
	}

	public double getErrorEstimate() {
		return error_estimate;
	}

	public int getEstimatorSize() {
		return 1;
	}

	public DecisionTree getEstimator(int i) {
		return dt;
	}

	public int getTrainingDataSize(int i) {
		return training_data_size; //num_instances - TEST_SET_1;
	}
	public int getTestDataSize(int i) {
		return TEST_SET_1;
	}

	public void setTrainingDataSize(int _trainingDataSize) {
		training_data_size = _trainingDataSize;
	}

}
