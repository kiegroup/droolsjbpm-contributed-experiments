package org.drools.learner;

import java.util.Comparator;


public class InstanceComparator implements Comparator<Instance> {
	
	private String attr_name;

	public InstanceComparator(String _attr_name) {
		this.attr_name = _attr_name;
	}
	public int compare(Instance i0, Instance i1) {
		return i0.getAttr(this.attr_name).compareValue(i1.getAttr(this.attr_name));
	}

}
