package org.drools.learner.eval;

public class EstimatedNodeSize implements StoppingCriterion {

    private double outlierPercentage;
    private int    estimatedSumBranch;
    private int    numTimesBranched;

    public EstimatedNodeSize(double outlierPercentage) {
        this.outlierPercentage = outlierPercentage;
        numTimesBranched = 0;
    }

    public boolean stop(InformationContainer bestAttrEval) {
        int d = bestAttrEval.getDepth();
        estimatedSumBranch += bestAttrEval.domain.getCategoryCount();
        numTimesBranched++;
        double estimatedBranch = (double) estimatedSumBranch / (double) numTimesBranched;
        // N/(b^d)
        double estimatedSize = bestAttrEval.getTotalNumData() / Math.pow(estimatedBranch, d);
        System.out.println("EstimatedNodeSize:stop: " + bestAttrEval.getNumData() + " <= " + (Math.ceil(estimatedSize * outlierPercentage) - 1) + " / " + estimatedSize);
        if (bestAttrEval.getNumData() <= Math.ceil(estimatedSize * outlierPercentage) - 1) {
            return true;
        } else {
            return false;
        }
    }
}
