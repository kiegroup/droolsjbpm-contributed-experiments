package org.drools.learner.eval;

public class SplitPoint {
	
	private int index;
	private Object cut_point;
	private double info_value;
	
	public SplitPoint(int _index, Object _point) {
		this.index = _index;
		this.cut_point = _point;
	}
	
	public Object getValue() {
		return cut_point;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setInformationValue(double info_sum) {
		this.info_value = info_sum;
	}

}
