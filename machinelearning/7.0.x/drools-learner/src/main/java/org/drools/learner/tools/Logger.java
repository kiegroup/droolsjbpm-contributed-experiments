package org.drools.learner.tools;

import java.io.Writer;

public class Logger {

    private static NullLogMethod nullLog = new NullLogMethod();
    private Writer   writer;
    private Class<?> klass;
    private LogMethod statM = nullLog, errorM = nullLog, warnM = nullLog, debugM = nullLog, infoM = nullLog;
    public Logger(Class<?> klass, Writer w) {
        this.klass = klass;
        writer = w;
    }

    public void setVerbosity(LogLevel lvl) {
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
        statM.log(o, writer);//"[stat](" + klass.getSimpleName() + ") " +
    }

    public void error(Object o) {
        errorM.log("[error](" + klass.getSimpleName() + ") " + o, writer);
    }

    public void warn(Object o) {
        warnM.log("[warn](" + klass.getSimpleName() + ") " + o, writer);
    }

    public void debug(Object o) {
        debugM.log("[debug](" + klass.getSimpleName() + ") " + o, writer);
    }

    public void info(Object o) {
        infoM.log("[info](" + klass.getSimpleName() + ") " + o, writer);
    }

    public static enum LogLevel {
        NULL, STAT, ERROR, WARN, DEBUG, INFO;
    }
}

class NullLogMethod implements LogMethod {

    public void log(Object o, Writer w) {
    }
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