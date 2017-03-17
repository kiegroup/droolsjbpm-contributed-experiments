package org.drools.learner.tools;

public interface LoggerInterface {
	void log(String s);
	void stat(String s);
	
	void setMsg(String s) ;
	
	LoggerInterface clone();
}