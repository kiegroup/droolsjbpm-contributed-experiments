package org.drools.learner;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class Stats {
	
	public static final int INCORRECT = 0, CORRECT = 1, UNKNOWN = 2;
	
	DecimalFormat precision = new DecimalFormat("#.###");
	
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
	public void print2file(String fileSignature, boolean append) {
		
		//String dataFileName = "src/main/rules/"+_packageNames+"/"+ fileName; 
		
		if (!fileSignature.endsWith(".stats"))
			fileSignature += ".stats";
		System.out.println("printing stats:"+ fileSignature);
		
		try {
			StatsPrinter.print(this, new FileWriter(fileSignature, append));
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
	
	public String print4Latex() {
		
		double in = 100.0d *(double)getResult(Stats.INCORRECT)/(double) getTotal();
		double co = 100.0d *(double)getResult(Stats.CORRECT)/(double) getTotal();
		double un = 100.0d *(double)getResult(Stats.UNKNOWN)/(double) getTotal();
		double to = 100.0d;
		return "\t&\t"+ getResult(Stats.INCORRECT)+ "\t&\t"+ precision.format(in) + "\t&\t"+
				getResult(Stats.CORRECT)+"\t&\t"+ precision.format(co)+ "\t&\t"+ getTotal()+"\t&\t"+ precision.format(to);
	}
	
	public String print2string() {
		return getResult(Stats.INCORRECT)+ " "+getResult(Stats.CORRECT)+" "+getTotal();// getResult(Stats.UNKNOWN)+
	}
	public static String getErrors() {
		return "INCORRECT CORRECT TOTAL\n";//UNKNOWN
	}
}

