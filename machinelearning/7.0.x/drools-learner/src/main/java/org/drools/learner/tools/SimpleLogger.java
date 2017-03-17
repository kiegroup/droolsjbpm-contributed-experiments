package org.drools.learner.tools;


public class SimpleLogger {

	public static final int STAT  = 0;
	public static final int ERROR = 1;
	public static final int WARN  = 2;
	public static final int DEBUG = 3;
	public static final int INFO  = 4;
	
	public static final int DEFAULT_LEVEL  = ERROR;
	
	public static String[] msgs = {"stat","error", "warn", "debug", "info"};

	public static final int LAST  = INFO;

	public LoggerInterface[] loggers = new LoggerInterface[LAST+1];

	public SimpleLogger() {
	
		for (int i = 0; i <= LAST; ++i) {
			loggers[i] = null;
		}
	}

	public SimpleLogger(int _lvl, LoggerInterface _logint) {
		for (int i = 0; i <= LAST; ++i) {
			if (i > _lvl)
				loggers[i] = null;
			else {
				loggers[i] = _logint.clone();
				loggers[i].setMsg(msgs[i]);
			}
		}
	}

	public final LoggerInterface get(int _index) {
		return loggers[_index];
	}

	public final LoggerInterface stat() {
		return loggers[STAT];
	}

	public final LoggerInterface error() {
		return loggers[ERROR];
	}

	public final LoggerInterface warn() {
		return loggers[WARN];
	}

	public final LoggerInterface debug() {
		return loggers[DEBUG];
	}

	public final LoggerInterface info() {
		return loggers[INFO];
	}

}
