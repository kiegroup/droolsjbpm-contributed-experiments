package org.drools.learner;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class StatsPrinter {


	//public static void print(Stats mystats, OutputStream os) {
	public static void print(Stats mystats, Writer wr) throws IOException {
		//PrintWriter pwr = new PrintWriter(os);
		
		// print the statistics of the results to a file	
		wr.write("TESTING results: incorrect "+ mystats.getResult(Stats.INCORRECT)+"\n");
		wr.write("TESTING results: correct "+ mystats.getResult(Stats.CORRECT)+"\n");
		wr.write("TESTING results: unknown "+ mystats.getResult(Stats.UNKNOWN)+"\n");
		wr.write("TESTING results: Total Number "+ mystats.getTotal());
		
		//wr.flush();
		wr.close();
	}
	
	public static void print(StringBuffer sb, Writer wr) throws IOException {
		//PrintWriter pwr = new PrintWriter(os);
		
		// print the statistics of the results to a file	
		wr.write(sb.toString()+ "\n");
		
		wr.close();
	}
	
	public static void print2file(StringBuffer sb, String fileSignature, boolean append) {
		
		//String dataFileName = "src/main/rules/"+_packageNames+"/"+ fileName; 
		
		if (!fileSignature.endsWith(".stats"))
			fileSignature += ".stats";
		System.out.println("printing stats:"+ fileSignature);
		
		try {
			StatsPrinter.print(sb, new FileWriter(fileSignature, append));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}