package org.drools.learner.builder;

import java.util.ArrayList;
import java.util.Collection;

import org.drools.learner.DecisionTree;
import org.drools.learner.InstanceList;
import org.drools.learner.Memory;
import org.drools.learner.Stats;

public class SolutionSet {

    private Memory              mem;
    private ArrayList<Solution> solTrees;
    private InstanceList        globalTrainSet, globalTestSet;
    private Stats globalTrainStats, globalTestStats;
    private int bestSolutionId;

    public SolutionSet(Memory mem) {
        this.mem = mem;
        bestSolutionId = 0;
        solTrees = new ArrayList<Solution>(1);
    }

    public Collection<String> getTargets() {
        return mem.getInstances().getTargets();
    }

    public InstanceList getInputData() {
        return mem.getInstances();
    }

    //	public InstanceList getValidationSet

    public InstanceList getTrainSet() {
        return mem.getTrainSet();
    }

    public InstanceList getTestSet() {
        return mem.getTestSet();
    }

    public void addSolution(Solution s) {
        solTrees.add(s);
    }

    public void addSolution(DecisionTree tree, InstanceList set, InstanceList testSet) {
        Solution x = new Solution(tree, set);
        x.setTestList(testSet);
        addSolution(x);
    }

    public ArrayList<Solution> getSolutions() {
        return solTrees;
    }

    public Stats getGlobalTrainStats() {
        return globalTrainStats;
    }

    public void setGlobalTrainStats(Stats train) {
        globalTrainStats = train;
    }

    public Stats getGlobalTestStats() {
        return globalTestStats;
    }

    public void setGlobalTestStats(Stats test) {
        globalTestStats = test;
    }

    public int getBestSolutionId() {
        return bestSolutionId;
    }

    public void setBestSolutionId(int bestId) {
        bestSolutionId = bestId;
    }

    public Solution getBestSolution() {
        return solTrees.get(bestSolutionId);
    }

    public int getMinTestId() {
        double min = 1.0;
        int    id  = -1;
        for (int i = 0; i < solTrees.size(); i++) {
            double testError  = solTrees.get(i).getTestError();
            double trainError = solTrees.get(i).getTrainError();
            if (testError < min) {
                min = testError;
                id = i;
            } else if (testError == min) {
                double trainOld = solTrees.get(id).getTrainError();
                if (trainError < trainOld) {
                    min = testError;
                    id = i;
                }
            }
        }
        return id;
    }
}
