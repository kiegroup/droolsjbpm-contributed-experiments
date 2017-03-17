package org.drools.learner.tools;

import java.io.Writer;


public class Logger {
	public static enum LogLevel_ { NULL, STAT, ERROR, WARN, DEBUG, INFO; }

	private static NullLogMethod null_log = new NullLogMethod();

	private Writer writer;
	private Class<?> klass;
	private LogMethod stat_m = null_log, error_m = null_log, warn_m = null_log, 
										debug_m = null_log, info_m  = null_log ;

	public Logger(Class<?> _class, Writer w) {
		klass = _class;
		writer = w;
	}
	public void setVerbosity(LogLevel_ lvl) {
//		switch (lvl) {
//		case INFO:
//			info_m = new SimpleLogMethod();;
//		case DEBUG:
//			debug_m = new SimpleLogMethod();
//		case WARN:
//			warn_m = new SimpleLogMethod();
//		case ERROR:
//			error_m = new SimpleLogMethod();
//		case STAT:
//			stat_m = new SimpleLogMethod();
//		case NULL:
//		}
	}
	
	public void stat(Object o) {
		stat_m.log(o, writer);//"[stat](" + klass.getSimpleName() + ") " + 
	}
	
	public void error(Object o) {
		error_m.log("[error](" + klass.getSimpleName() + ") " + o, writer);
	}

	public void warn(Object o) {
		warn_m.log("[warn](" + klass.getSimpleName() + ") " + o, writer);
	}

	public void debug(Object o) {
		debug_m.log("[debug](" + klass.getSimpleName() + ") " + o, writer);
	}

	public void info(Object o) {
		info_m.log("[info](" + klass.getSimpleName() + ") " + o, writer);
	}
}


class NullLogMethod implements LogMethod {
	public void log(Object o, Writer w) {}
}

//class SimpleLogMethod implements LogMethod {
//	public void log(Object o, Writer w) {
//		try {
//			w.write(o.toString());
//			w.flush();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//}