package org.drools.learner;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.drools.learner.tools.Util;

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
	public static void print(Stats train, Stats test) {
		StringBuffer sb = new StringBuffer();
		sb.append("#"+ Stats.getErrors());
		sb.append( "\n");
		sb.append(train.print4Latex() +test.print4Latex()+"\\\\"+ "\n");
		sb.append( "\n");
		
		System.out.println(sb.toString());
	}
	
	public static void printLatexComment(String comment, String executionSignature, boolean append) {
		StringBuffer sb = new StringBuffer();
		sb.append("#"+ comment+"\n");		
		StatsPrinter.print2file(sb, Util.DRL_DIRECTORY +executionSignature, append);
	}
	public static void printLatexLine(String executionSignature, boolean append) {
		StringBuffer sb = new StringBuffer();
		sb.append( "\n");		
		StatsPrinter.print2file(sb, Util.DRL_DIRECTORY +executionSignature, append);
	}
	
	public static void printLatex(Stats train, Stats test, String executionSignature, boolean append) {
		StringBuffer sb = new StringBuffer();
		sb.append(train.print4Latex() +test.print4Latex()+"\\\\"+ "\n");		
		StatsPrinter.print2file(sb, Util.DRL_DIRECTORY +executionSignature, append);
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