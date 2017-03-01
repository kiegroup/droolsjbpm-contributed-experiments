package org.drools.learner.eval;

public class EstimatedNodeSize implements StoppingCriterion {
	
	private double outlier_percentage;
	private int estimated_sum_branch;
	private int num_times_branched;
	
	
	public EstimatedNodeSize(double o_p) {
		outlier_percentage = o_p;
		num_times_branched = 0;
	}
	

	public boolean stop(InformationContainer best_attr_eval) {
		int d = best_attr_eval.getDepth();
		estimated_sum_branch += best_attr_eval.domain.getCategoryCount();
		num_times_branched ++;
		double estimated_branch = (double)estimated_sum_branch/(double)num_times_branched;
		// N/(b^d)
		double estimated_size = best_attr_eval.getTotalNumData()/Math.pow(estimated_branch, d);
		System.out.println("EstimatedNodeSize:stop: " +best_attr_eval.getNumData() + " <= " + ( Math.ceil(estimated_size*outlier_percentage)-1) +" / "+estimated_size);
		if (best_attr_eval.getNumData() <= Math.ceil(estimated_size*outlier_percentage)-1)
			return true;
		else 
			return false;
	}
}
