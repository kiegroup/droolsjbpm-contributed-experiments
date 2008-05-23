package org.drools.learner.builder;

import org.drools.learner.DecisionTree;
import org.drools.learner.Instance;
import org.drools.learner.InstanceList;
import org.drools.learner.Stats;
import org.drools.learner.tools.Util;

public class Tester {
	
	/* test the entire set*/
	public static void test(DecisionTree dt, InstanceList data) {
		if (dt == null) {
			System.out.println("The tree is not created");
			System.exit(0);
		}
		
		if (Util.DEBUG_TEST) {
			System.out.println(Util.ntimes("\n", 2)+Util.ntimes("$", 5)+" TESTING "+Util.ntimes("\n", 2));
		}
		
		Stats evaluation = new Stats(dt.getObjClass());
		int i = 0;
		for (Instance instant : data.getInstances()) {

			Integer result = dt.test(instant);
			if (Util.DEBUG_TEST) {
				System.out.println(Util.ntimes("#\n", 1)+i+ " <START> TEST: instant="+ instant + " = target "+ result);
			} else {
				if (i%1000 ==0)	System.out.print(".");
			}
			evaluation.change(result, 1);
			i ++;
		}
		
		if (Util.PRINT_STATS) {
			if (Util.DEBUG_TEST) {
				evaluation.print2out();
			}
			evaluation.print2file("");
		}
		return;
	}

}
