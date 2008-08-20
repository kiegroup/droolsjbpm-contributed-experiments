package org.drools.learner.builder;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.drools.learner.AttributeValueComparator;
import org.drools.learner.Domain;
import org.drools.learner.Instance;
import org.drools.learner.InstanceList;
import org.drools.learner.Stats;
import org.drools.learner.StatsPrinter;
import org.drools.learner.eval.stopping.StoppingCriterion;
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
	
//	protected void printStats(final Stats evaluation, String executionSignature, boolean append) {
//		if (Util.PRINT_STATS) {
////			if (flog.debug() !=null)
////				flog.debug().log(evaluation.print2string());
//			
//			evaluation.print2file(executionSignature, append);//correct
//		}
//	}
	
	protected void printStats(Stats evaluation, String executionSignature, boolean append) {
		//PrintWriter pwr = new PrintWriter(os);
		
		// print the statistics of the results to a file	
		StringBuffer sb = new StringBuffer();
		sb.append("TESTING results: incorrect "+ evaluation.getResult(Stats.INCORRECT)+"\n");
		sb.append("TESTING results: correct "+ evaluation.getResult(Stats.CORRECT)+"\n");
		sb.append("TESTING results: unknown "+ evaluation.getResult(Stats.UNKNOWN)+"\n");
		sb.append("TESTING results: Total Number "+ evaluation.getTotal());
		
		StatsPrinter.print2file(sb, executionSignature, append);
	}
	
	public static void printStopping(ArrayList<StoppingCriterion> stoppingCriteria,
			String executionSignature) {
		StringBuffer sb = new StringBuffer();
		for (StoppingCriterion sc:stoppingCriteria) {
			sb.append("\n"+"\t&\t"+ sc.getClass().getSimpleName() );
		}
		sb.append("\n");
		for (StoppingCriterion sc:stoppingCriteria) {
			sb.append("\t&\t"+sc.getNumPruned() + "");
		}
		sb.append("\\\\\n");
		StatsPrinter.print2file(sb, executionSignature, true);
		
	}

	

}
