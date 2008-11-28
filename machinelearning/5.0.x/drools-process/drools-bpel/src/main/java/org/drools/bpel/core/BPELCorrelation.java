package org.drools.bpel.core;

/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class BPELCorrelation {
	
	public static final int PATTERN_IN = 1;
	public static final int PATTERN_OUT = 2;
	public static final int PATTERN_OUT_IN = 3;
	
	private String set;
	private boolean initiate;
	private int pattern;
	
	public String getSet() {
		return set;
	}
	
	public void setSet(String set) {
		this.set = set;
	}
	
	public boolean isInitiate() {
		return initiate;
	}
	
	public void setInitiate(boolean initiate) {
		this.initiate = initiate;
	}
	
	public int getPattern() {
		return pattern;
	}
	
	public void setPattern(int pattern) {
		this.pattern = pattern;
	}

}
