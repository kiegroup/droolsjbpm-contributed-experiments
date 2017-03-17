package org.drools.learner.eval.heuristic;

public class MinEntropy extends Entropy implements Heuristic {

	public MinEntropy() {
		super();
		super.multiplier = -1.0;
		
	}
	
}
