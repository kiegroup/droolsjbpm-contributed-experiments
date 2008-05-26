package org.drools.learner;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.drools.learner.tools.Util;

public class Stats {
	
	public static int INCORRECT = 0, CORRECT = 1, UNKNOWN = 2;
	
	private Class<?> stat_class;
	private ArrayList<Integer> histogram;
	
	private int total_data;
	
	public Stats( Class<?> _stat_class) {
		stat_class = _stat_class;
		
		/*
		 * INCORRECT | CORRECT | UNKNOWN
		 *   |		 	  |  	    |
		 *   0       	  1       	2
		 */
		histogram = new ArrayList<Integer>(3);
		for (int i=0; i < 3; i ++) {
			histogram.add(new Integer(0));
		}
		total_data = 0;
	}
	public int getResult(int classification) {
		return histogram.get(classification);
	}

	public void change(Integer result, int i) {
		histogram.set(result, Integer.valueOf(histogram.get(result) + i));
		total_data += i;
	}
	
	public int getTotal() {
		return total_data;
	}
	
	public void print2file(String dataFile, int domain_type, int tree_set) {
		
		String packageFolders = this.stat_class.getPackage().getName();

		String _packageNames = packageFolders.replace('.', '/');
		
		String fileName = (dataFile == null || dataFile == "") ? this.stat_class.getSimpleName().toLowerCase() : dataFile; 		
		
		
		String suffix = Util.getFileSuffix(domain_type, tree_set);
		fileName += "_"+suffix + ".stats";
		

		
		String dataFileName = "src/main/rules/"+_packageNames+"/"+ fileName; 
		try {
			StatsPrinter.print(this, new FileOutputStream(dataFileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void print2out() {
		StatsPrinter.print(this, System.out);
		
	}
}

class StatsPrinter {
	public static void print(Stats mystats, OutputStream os) {
		PrintWriter pwr = new PrintWriter(os);
		
		// print the statistics of the results to a file	
		pwr.println("TESTING results: incorrect "+ mystats.getResult(Stats.INCORRECT));
		pwr.println("TESTING results: correct "+ mystats.getResult(Stats.CORRECT));
		pwr.println("TESTING results: unknown "+ mystats.getResult(Stats.UNKNOWN));
		pwr.println("TESTING results: Total Number "+ mystats.getTotal());
		
		pwr.flush();
	}
}
