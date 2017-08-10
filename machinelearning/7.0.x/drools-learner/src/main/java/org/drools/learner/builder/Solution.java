package org.drools.learner.builder;

import org.drools.learner.DecisionTree;
import org.drools.learner.InstanceList;
import org.drools.learner.Stats;

public class Solution {

    private DecisionTree dt;
    private InstanceList trainingList;

    private InstanceList testList;
    private Stats        trainStats, testStats;

    public Solution(DecisionTree dt, InstanceList list) {
        this.dt = dt;
        trainingList = list;
    }

    public DecisionTree getTree() {
        return dt;
    }

    public InstanceList getList() {
        return trainingList;
    }

    public InstanceList getTestList() {
        return testList;
    }

    public void setTestList(InstanceList test) {
        testList = test;
    }

    public Stats getTrainStats() {
        return trainStats;
    }

    public void setTrainStats(Stats train) {
        trainStats = train;
    }

    public Stats getTestStats() {
        return testStats;
    }

    public void setTestStats(Stats test) {
        testStats = test;
    }

    public double getTrainError() {
        System.out.println("Total Train" + trainStats.getTotal() + ", size " + trainingList.getSize());
        return (double) trainStats.getResult(Stats.INCORRECT) / (double) trainStats.getTotal();
    }

    public double getTestError() {
        System.out.println("Total Test" + testStats.getTotal() + ", size " + testList.getSize());
        return (double) testStats.getResult(Stats.INCORRECT) / (double) testStats.getTotal();
    }

    public void changeTrainError(int change) {
        trainStats.change(Stats.INCORRECT, change);
        trainStats.change(Stats.CORRECT, -1 * change);
    }

    public void setError(int change) {
        // TODO test_stats.	
        System.out.println("Solution setError() doing nothing");
        System.exit(0);
    }
}
