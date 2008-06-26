package org.drools.learner.eval;

import java.util.ArrayList;

import org.drools.learner.Domain;
import org.drools.learner.Instance;

public class InformationContainer {
	
	public Domain domain;
	public double attribute_eval;
	//public double gain_ratio;
	public ArrayList<Instance> sorted_data;
	
	public InformationContainer() {
		domain = null;
		attribute_eval = 0.0;
		sorted_data = null;

	}
	
//	public InformationContainer(Domain _domain, double _attribute_eval, double _gain_ratio) {
//		this.domain = _domain;
//		this.attribute_eval = _attribute_eval;
//		this.gain_ratio = _gain_ratio;
//	}

}
