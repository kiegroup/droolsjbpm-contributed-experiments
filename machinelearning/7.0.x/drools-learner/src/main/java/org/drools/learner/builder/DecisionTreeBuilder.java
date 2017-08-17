package org.drools.learner.builder;

import org.drools.learner.DecisionTree;
import org.drools.learner.Memory;

public interface DecisionTreeBuilder {

    SolutionSet build(Memory wm, Learner trainer);

    TreeAlgo getTreeAlgo();

    //public static final int SINGLE = 1, BAG = 2, BOOST = 3;
    public static enum TreeAlgo {
        SINGLE, BAG, BOOST, BOOST_K
    }

    static Tester getTester(DecisionTree dt) {
        return new SingleTreeTester(dt);
    }
}
