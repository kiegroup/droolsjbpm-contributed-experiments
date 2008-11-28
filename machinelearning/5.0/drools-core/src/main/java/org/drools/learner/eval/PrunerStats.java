package org.drools.learner.eval;

public class PrunerStats extends TreeStats{


	private int iteration_id;

	private double cost_complexity;
	private double alpha;
//	private double training_error;

	public PrunerStats() {
		iteration_id = 0;
	}
	
	public PrunerStats(TreeStats ts) {
		super(ts.getTrainError(), ts.getErrorEstimation());
		iteration_id = 0;
	}

	// to set an node update with the worst cross validated error
	public PrunerStats(double error1) {
		super(error1);
		iteration_id = 0;
		//test_cost = error;
		
	}
	public PrunerStats(double error1, double error2) {
		super(error1, error2);
		iteration_id = 0;
		//test_cost = error;
		
	}

	public void iteration_id(int i) {
		iteration_id = i;
	}

	public int iteration_id() {
		return iteration_id;
	}


	public double getCost_complexity() {
		return cost_complexity;
	}

	public void setCost_complexity(double cost_complexity) {
		this.cost_complexity = cost_complexity;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}


}
