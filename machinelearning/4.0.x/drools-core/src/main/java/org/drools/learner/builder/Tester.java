package org.drools.learner.builder;

import org.drools.learner.AttributeValueComparator;
import org.drools.learner.Domain;
import org.drools.learner.Instance;
import org.drools.learner.InstanceList;
import org.drools.learner.Stats;
import org.drools.learner.tools.Util;

public abstract class Tester{
	
	private int DOMAIN_TYPE = 0, TREE_SET =0;
	public abstract void test(DecisionTreeBuilder builder, InstanceList data);
	
	public static Integer evaluate (Domain targetDomain, Instance i, Object tree_decision) {	
		String targetFName = targetDomain.getFName();
		
		Object tattr_value = i.getAttrValue(targetFName);
		Object i_category = targetDomain.getCategoryOf(tattr_value);
		
		if (AttributeValueComparator.instance.compare(i_category, tree_decision) == 0) {
			return Integer.valueOf(1); 	//correct
		} else {
			return Integer.valueOf(0);	// mistake
		} 
	}
	
	public void printStats(Stats evaluation) {
		if (Util.PRINT_STATS) {
			if (Util.DEBUG_TEST) {
				evaluation.print2out();
			}
			evaluation.print2file("", this.DOMAIN_TYPE, this.TREE_SET);
		}
	}

}
