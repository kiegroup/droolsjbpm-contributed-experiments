package org.drools.learner;

import java.util.Comparator;


public class DomainComparator implements Comparator<Domain> {

	public int compare(Domain d0, Domain d1) {
		
		return d0.getFName().compareTo(d1.getFName());
//		int fNameComp = d0.getFName().compareTo(d1.getFName());
//		if (fNameComp!=0)
//			return fNameComp;
//		else if ()	// do other stuff
//			return 1;
//		else
//			return 0;
	}
}
