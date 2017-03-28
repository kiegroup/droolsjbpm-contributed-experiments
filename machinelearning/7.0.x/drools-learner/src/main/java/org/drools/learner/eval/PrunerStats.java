package org.drools.learner.eval;

public class PrunerStats extends TreeStats {

    private int iterationId;

    private double costComplexity;
    private double alpha;
    //	private double training_error;

    public PrunerStats() {
        iterationId = 0;
    }

    public PrunerStats( TreeStats ts ) {
        super( ts.getTrainError(), ts.getErrorEstimation() );
        iterationId = 0;
    }

    // to set an node update with the worst cross validated error
    public PrunerStats( double error1 ) {
        super( error1 );
        iterationId = 0;
        //test_cost = error;

    }

    public PrunerStats( double error1, double error2 ) {
        super( error1, error2 );
        iterationId = 0;
        //test_cost = error;

    }

    public void iterationId( int i ) {
        iterationId = i;
    }

    public int iterationId() {
        return iterationId;
    }

    public double getCostComplexity() {
        return costComplexity;
    }

    public void setCostComplexity( double costComplexity ) {
        this.costComplexity = costComplexity;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha( double alpha ) {
        this.alpha = alpha;
    }

}
