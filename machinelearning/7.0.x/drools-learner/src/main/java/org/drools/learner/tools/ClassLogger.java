package org.drools.learner.tools;

import java.io.Writer;

public class ClassLogger {

    private static final LogLevel DEFAULT_LEVEL = LogLevel.ERROR;
    /*
     * 4 - info 3 - debug 2 - warning 1 - error 0 - nothing
     */
    public LogLevel level = DEFAULT_LEVEL;
    private Class<?> klass;
    private Writer   w;
    public ClassLogger(Class<?> klass, Writer w) {
        this.klass = klass;
        this.w = w;
    }

    public ClassLogger(Class<?> klass, Writer w, LogLevel level) {
        this.klass = klass;
        this.level = level;
        this.w = w;
    }

    public void stat(String s) {
        //if (level.compareTo(LogLevel.STAT) >= 0) {
        if (level.ordinal() >= LogLevel.STAT.ordinal()) {
            try {
                w.write(s);
                //w.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void info(String s) {
        //if (level.compareTo(LogLevel.INF0) >= 0) {
        if (level.ordinal() >= LogLevel.INF0.ordinal()) {
            try {
                w.write(("[info](" + klass.getSimpleName() + ") " + s));
                //w.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void debug(String s) {
        //if (level.compareTo(LogLevel.DEBUG) >= 0) {
        if (level.ordinal() >= LogLevel.DEBUG.ordinal()) {
            try {
                w.write(("[debug](" + klass.getSimpleName() + ") " + s));
                //w.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void warn(String s) {
        //if (level.compareTo(LogLevel.WARN) >= 0) {
        if (level.ordinal() >= LogLevel.WARN.ordinal()) {
            try {
                w.write(("[warn](" + klass.getSimpleName() + ") " + s));
                //w.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void error(String s) {
        //if (level.compareTo(LogLevel.ERROR) >= 0) {
        if (level.ordinal() >= LogLevel.ERROR.ordinal()) {
            try {
                w.write(("[error](" + klass.getSimpleName() + ") " + s));
                //w.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static enum LogLevel {
        NO, STAT, ERROR, WARN, DEBUG, INF0
    }
}
