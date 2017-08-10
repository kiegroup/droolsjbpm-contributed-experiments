package org.drools.learner.builder;

import java.util.ArrayList;
import java.util.Collections;

import org.drools.learner.DecisionTree;
import org.drools.learner.DecisionTreeVisitor;
import org.drools.learner.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecisionTreeMerger {

    protected static final transient Logger log = LoggerFactory.getLogger(DecisionTreeMerger.class);

    private DecisionTreeVisitor visitor;

    private ArrayList<Path> sortedPaths;

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
        sortedPaths = new ArrayList<Path>(visitor.getPathList());
        Collections.sort(sortedPaths, Path.getPathRankComparator());
    }

    public int getNumPaths() {
        return visitor.getNumPaths();
    }

    public int getNumPathsfound() {
        return visitor.getNumPathsFound();
    }

    public void printPaths() {
        if (log.isWarnEnabled()) {
            for (Path p : visitor.getPathList()) {
                log.warn(p.hashCode() + "-" + p.getRank() + " : " + p + "\n");
            }
        }
    }

    public void printSortedPaths() {

        if (log.isWarnEnabled()) {
            log.warn("Sorted paths: Total num of paths " + getNumPathsfound() + " num paths different" + getNumPaths() + "\n");
            for (Path p : sortedPaths) {
                log.warn(p.hashCode() + "-" + p.getTreeId() + "-" + p.getRank() + " : " + p + "\n");
            }
        }
    }
}
