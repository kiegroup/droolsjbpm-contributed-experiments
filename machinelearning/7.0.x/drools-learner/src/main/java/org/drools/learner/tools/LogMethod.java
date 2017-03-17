package org.drools.learner.tools;

import java.io.Writer;

public interface LogMethod {
    void log(Object o, Writer w);
}