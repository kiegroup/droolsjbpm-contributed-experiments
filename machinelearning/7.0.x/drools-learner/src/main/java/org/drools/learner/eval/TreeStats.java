package org.drools.learner.eval;

public class TreeStats {

    //private int iteration_id;
    private int    numTerminalNodes;
    private double testError;
    private double trainError;

    public TreeStats() {

    }

    public TreeStats(double error1, double error2) {
        trainError = error1;
        testError = error2;
    }

    // to set an node update with the worst cross validated error
    public TreeStats(double error) {
        //		iteration_id = 0;
        testError = error;
        trainError = 1.0d - error;
    }

    public int getNumTerminalNodes() {
        return numTerminalNodes;
    }

    public void setNumTerminalNodes(int numTerminalNodes) {
        this.numTerminalNodes = numTerminalNodes;
    }

    public double getErrorEstimation() {
        return testError;
    }

    public void setErrorEstimation(double validCost) {
        this.testError = validCost;
    }

    public double getTrainError() {
        return trainError;
    }

    public void setTrainError(double resubstitutionCost) {
        this.trainError = resubstitutionCost;
    }
}