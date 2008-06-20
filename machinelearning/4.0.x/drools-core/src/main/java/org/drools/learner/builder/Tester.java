package org.drools.learner.builder;

import org.drools.learner.AttributeValueComparator;
import org.drools.learner.Domain;
import org.drools.learner.Instance;
import org.drools.learner.InstanceList;
import org.drools.learner.Stats;
import org.drools.learner.tools.Util;

public abstract class Tester{
	
	//private static final Logger log = LoggerFactory.getSysOutLogger(LogLevel.ERROR); 
	//private static final Logger flog = LoggerFactory.getFileLogger(Tester.class, LogLevel.ERROR, Util.log_file);
	
	public abstract Stats test(InstanceList data);// String executionSignature
	
	public static Integer evaluate (Domain targetDomain, Instance i, Object tree_decision) {	
		String targetFName = targetDomain.getFReferenceName();
		
		Object tattr_value = i.getAttrValue(targetFName);
		Object i_category = targetDomain.getCategoryOf(tattr_value);
		
		if (AttributeValueComparator.instance.compare(i_category, tree_decision) == 0) {
			return Integer.valueOf(1); 	//correct
		} else {
			return Integer.valueOf(0);	// mistake
		} 
	}
	
	protected void printStats(final Stats evaluation, String executionSignature) {
		if (Util.PRINT_STATS) {
//			if (flog.debug() !=null)
//				flog.debug().log(evaluation.print2string());
			
			evaluation.print2file(executionSignature);
		}
	}

}
