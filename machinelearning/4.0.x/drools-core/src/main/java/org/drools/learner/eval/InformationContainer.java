package org.drools.learner.eval;

import java.util.ArrayList;

import org.drools.learner.Domain;
import org.drools.learner.Instance;

public class InformationContainer {
	
	public Domain domain;
	public double gain;
	public double gain_ratio;
	public ArrayList<Instance> sorted_data;
	
	public InformationContainer() {

	}
	public InformationContainer(Domain _domain, double _gain, double _gain_ratio) {
		this.domain = _domain;
		this.gain = _gain;
		this.gain_ratio = _gain_ratio;
	}

}
