package org.drools.learner.eval;

import java.util.ArrayList;

import org.drools.learner.Domain;
import org.drools.learner.Instance;

public class InformationContainer {
	
	public Domain domain;
	public double attribute_eval;
	//public double gain_ratio;
	public ArrayList<Instance> sorted_data;
	
	private InstDistribution stats;
	private int depth;
	private int total_num_data;
	
	public InformationContainer() {
		domain = null;
		attribute_eval = 0.0;
		sorted_data = null;

		depth = 0;
		stats = null;
		total_num_data = 0;
	}

	public void setStats(InstDistribution data_stats) {
		stats = data_stats;
	}

	public void setDepth(int _depth) {
		depth = _depth;
	}

	public int getDepth() {
		return depth;
	}
	public double getNumData() {
		return stats.getSum();
	}

	// total num of data fed to per tree
	public void setTotalNumData(int num) {
		total_num_data = num;
	}
	
	public int getTotalNumData() {
		return total_num_data ;
	}
	
//	public InformationContainer(Domain _domain, double _attribute_eval, double _gain_ratio) {
//		this.domain = _domain;
//		this.attribute_eval = _attribute_eval;
//		this.gain_ratio = _gain_ratio;
//	}

}
