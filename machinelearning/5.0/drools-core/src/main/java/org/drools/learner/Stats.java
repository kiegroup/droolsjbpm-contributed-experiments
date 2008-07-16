package org.drools.learner;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

public class Stats {
	
	public static final int INCORRECT = 0, CORRECT = 1, UNKNOWN = 2;
	
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
	/*
	 * fileSignature must contain the folder location
	 * by default the folder = "src/main/rules/"
	 */
	public void print2file(String fileSignature) {
		
		//String dataFileName = "src/main/rules/"+_packageNames+"/"+ fileName; 
		
		if (!fileSignature.endsWith(".stats"))
			fileSignature += ".stats";
		System.out.println("printing stats:"+ fileSignature);
		
		try {
			StatsPrinter.print(this, new FileWriter(fileSignature));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void print2out() {
		try {
			StatsPrinter.print(this, new PrintWriter(System.out));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public String print2string() {
		StringWriter swr = new StringWriter();
		try {
			StatsPrinter.print(this, swr);
			return swr.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
}

class StatsPrinter {
	//public static void print(Stats mystats, OutputStream os) {
	public static void print(Stats mystats, Writer wr) throws IOException {
		//PrintWriter pwr = new PrintWriter(os);
		
		// print the statistics of the results to a file	
		wr.write("TESTING results: incorrect "+ mystats.getResult(Stats.INCORRECT)+"\n");
		wr.write("TESTING results: correct "+ mystats.getResult(Stats.CORRECT)+"\n");
		wr.write("TESTING results: unknown "+ mystats.getResult(Stats.UNKNOWN)+"\n");
		wr.write("TESTING results: Total Number "+ mystats.getTotal());
		
		wr.flush();
	}
}
