package org.drools.learner.eval;

public class TreeStats{

	//private int iteration_id;
	private int num_terminal_nodes;
	private double test_error;
	private double train_error;

	
	public TreeStats() {

	}
	public TreeStats(double error1, double error2) {
		train_error = error1;
		test_error = error2;
	}

	// to set an node update with the worst cross validated error
	public TreeStats(double error) {
//		iteration_id = 0;
		test_error = error;
		train_error = 1.0d - error;
	}
	
	public int getNum_terminal_nodes() {
		return num_terminal_nodes;
	}

	public void setNum_terminal_nodes(int num_terminal_nodes) {
		this.num_terminal_nodes = num_terminal_nodes;
	}

	public double getErrorEstimation() {
		return test_error;
	}

	public void setErrorEstimation(double valid_cost) {
		this.test_error = valid_cost;
	}

	public double getTrainError() {
		return train_error;
	}

	public void setTrainError(double resubstitution_cost) {
		this.train_error = resubstitution_cost;
	}
	
}