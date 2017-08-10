package org.drools.learner.eval;

public class SplitPoint {

    private int    index;
    private Object cutPoint;
    private double infoValue;

    public SplitPoint(int index, Object point) {
        this.index = index;
        this.cutPoint = point;
    }

    public Object getValue() {
        return cutPoint;
    }

    public int getIndex() {
        return index;
    }

    public void setInformationValue(double infoSum) {
        this.infoValue = infoSum;
    }
}
