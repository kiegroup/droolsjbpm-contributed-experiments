package org.drools.learner.builder;

import java.util.ArrayList;
import java.util.Collections;

import org.drools.learner.DecisionTree;
import org.drools.learner.DecisionTreeVisitor;
import org.drools.learner.Path;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;

public class DecisionTreeMerger {
	
	//private static SimpleLogger flog = LoggerFactory.getFileLogger(DecisionTreeMerger.class, SimpleLogger.WARN, "rules_gizil2");
	private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(DecisionTreeMerger.class, SimpleLogger.WARN);
	private static SimpleLogger slog = LoggerFactory.getSysOutLogger(DecisionTreeMerger.class, SimpleLogger.WARN);
	
	
	private DecisionTreeVisitor visitor;
	
	private ArrayList<Path> sorted_paths;
	
	public DecisionTreeMerger() {
		visitor = new DecisionTreeVisitor();
		
	}
	public void add(DecisionTree dt) {
		visitor.visit(dt);
	}
	
	public void add2(DecisionTree dt) {
		visitor.visit2(dt);
	}
	
	public DecisionTree getBest() {
		sortPaths();
		printSortedPaths();
		//System.exit(0);
		return null;
	}
	
	public void sortPaths() {
		sorted_paths = new ArrayList<Path>(visitor.getPathList());
		Collections.sort(sorted_paths, Path.getPathRankComparator());	
	}
	
	public int getNumPaths() {
		return visitor.getNumPaths();
	}
	
	public int getNumPathsfound() {
		return visitor.getNumPathsFound();
	}
	public void printPaths() {
		if (flog.warn() != null) {
			for (Path p : visitor.getPathList()) {
				flog.warn().log(p.hashCode()+ "-"+p.getRank()+" : "+ p + "\n");
			}
			
		}
		
	}
	
	
	public void printSortedPaths() {
		
		if (flog.warn() != null) {
			flog.warn().log("Sorted paths: Total num of paths "+getNumPathsfound()+" num paths different"+getNumPaths()+ "\n");
			slog.warn().log("Total num of paths "+getNumPathsfound()+" num paths different"+getNumPaths()+ "\n");
			for (Path p: sorted_paths) {
				flog.warn().log(p.hashCode()+ "-"+p.getTreeId()+"-"+p.getRank()+" : "+ p + "\n");
				slog.warn().log(p.hashCode()+ "-"+p.getTreeId()+"-"+p.getRank()+" : "+ p + "\n");
			}
			
		}
		
		
		
	}
}
