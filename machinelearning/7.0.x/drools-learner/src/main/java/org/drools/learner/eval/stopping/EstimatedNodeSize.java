package org.drools.learner.eval.stopping;

import org.drools.learner.eval.InformationContainer;

public class EstimatedNodeSize implements StoppingCriterion {

    private double outlierPercentage;
    private int    estimatedSumBranch;
    private int    numTimesBranched;
    private int    numPrunned;

    public EstimatedNodeSize(double oP) {
        outlierPercentage = oP;
        numTimesBranched = 0;
        numPrunned = 0;
    }

    public boolean stop(InformationContainer bestAttrEval) {
        int d = bestAttrEval.getDepth();
        estimatedSumBranch += bestAttrEval.getDomain().getCategoryCount();
        numTimesBranched++;
        double estimatedBranch = (double) estimatedSumBranch / (double) numTimesBranched;
        // N/(b^d)
        double estimatedSize = bestAttrEval.getTotalNumData() / Math.pow(estimatedBranch, d);
        //System.out.println("EstimatedNodeSize:stop: " +best_attr_eval.getNumData() + " <= " + ( Math.ceil(estimated_size*outlier_percentage)-1) +" / "+estimated_size);
        if (bestAttrEval.getNumData() <= Math.ceil(estimatedSize * outlierPercentage) - 1) {
            numPrunned++;
            return true;
        } else {
            return false;
        }
    }

    public int getNumPruned() {
        return numPrunned;
    }
}
