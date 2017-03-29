package org.drools.learner.eval;

import java.util.ArrayList;

import org.drools.learner.Domain;
import org.drools.learner.Instance;

public class InformationContainer {

    public Domain domain;
    public double attributeEval;
    //public double gain_ratio;
    public ArrayList<Instance> sortedData;

    private InstDistribution stats;
    private int depth;
    private int totalNumData;

    public InformationContainer() {
        domain = null;
        attributeEval = 0.0;
        sortedData = null;

        depth = 0;
        stats = null;
        totalNumData = 0;
    }

    public void setStats( InstDistribution dataStats ) {
        stats = dataStats;
    }

    public void setDepth( int depth ) {
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    public double getNumData() {
        return stats.getSum();
    }

    // total num of data fed to per tree
    public void setTotalNumData( int num ) {
        totalNumData = num;
    }

    public int getTotalNumData() {
        return totalNumData;
    }

    //	public InformationContainer(Domain _domain, double _attribute_eval, double _gain_ratio) {
    //		this.domain = _domain;
    //		this.attribute_eval = _attribute_eval;
    //		this.gain_ratio = _gain_ratio;
    //	}

}
