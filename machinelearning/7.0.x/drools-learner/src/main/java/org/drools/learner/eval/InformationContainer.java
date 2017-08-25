package org.drools.learner.eval;

import java.util.ArrayList;

import org.drools.learner.Domain;
import org.drools.learner.Instance;

public class InformationContainer {

    private Domain              domain;
    private double              attributeEval;
    //public double gain_ratio;
    private ArrayList<Instance> sortedData;

    private InstDistribution stats;
    private int              depth;
    private int              totalNumData;

    public InformationContainer(Domain domain, double attributeEval, ArrayList<Instance> sortedData, InstDistribution stats, int depth, int totalNumData) {
        this.domain = domain;
        this.attributeEval = attributeEval;
        this.sortedData = sortedData;
        this.stats = stats;
        this.depth = depth;
        this.totalNumData = totalNumData;
    }

    public InformationContainer(InstDistribution stats, int totalNumData, int depth) {
        this.totalNumData = totalNumData;
        this.stats = stats;
        this.depth = depth;
    }

//        bestAttrEval.setStats(dataStats);
//        bestAttrEval.setDepth(depth);
//        bestAttrEval.setTotalNumData(getTrainingDataSizePerTree());

    public InformationContainer() {
        domain = null;
        attributeEval = 0.0;
        sortedData = null;

        depth = 0;
        stats = null;
        totalNumData = 0;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public double getAttributeEval() {
        return attributeEval;
    }

    public void setAttributeEval(double attributeEval) {
        this.attributeEval = attributeEval;
    }

    public ArrayList<Instance> getSortedData() {
        return sortedData;
    }

    public void setSortedData(ArrayList<Instance> sortedData) {
        this.sortedData = sortedData;
    }

    public void setStats(InstDistribution dataStats) {
        stats = dataStats;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public double getNumData() {
        return stats.getSum();
    }

    public int getTotalNumData() {
        return totalNumData;
    }

    // total num of data fed to per tree
    public void setTotalNumData(int num) {
        totalNumData = num;
    }

    //	public InformationContainer(Domain _domain, double _attribute_eval, double _gain_ratio) {
    //		this.domain = _domain;
    //		this.attribute_eval = _attribute_eval;
    //		this.gain_ratio = _gain_ratio;
    //	}
}
