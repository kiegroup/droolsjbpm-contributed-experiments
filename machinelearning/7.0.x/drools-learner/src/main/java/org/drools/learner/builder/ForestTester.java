package org.drools.learner.builder;

import java.util.ArrayList;

import org.drools.learner.DecisionTree;
import org.drools.learner.Domain;
import org.drools.learner.Instance;
import org.drools.learner.InstanceList;
import org.drools.learner.Stats;
import org.drools.learner.eval.ClassDistribution;
import org.drools.learner.tools.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForestTester extends Tester {

    protected static final transient Logger log = LoggerFactory.getLogger(ForestTester.class);

    private ArrayList<DecisionTree> trees;
    private Domain                  targetDomain;

    public ForestTester(ArrayList<DecisionTree> forest) {
        trees = forest;
        targetDomain = forest.get(0).getTargetDomain();
    }

    public Stats test(InstanceList data) {

        Stats evaluation = new Stats(data.getSchema().getObjectClass()); //represent.getObjClass());

        int i = 0;
        for (Instance instance : data.getInstances()) {
            Object  forestDecision = this.voteOn(instance);
            Integer result         = evaluate(targetDomain, instance, forestDecision);

            //log.debug(Util.ntimes("#\n", 1)+i+ " <START> TEST: instant="+ instance + " = target "+ result);			
            if (i % 1000 == 0 && log.isInfoEnabled()) {
                log.info(".");
            }

            evaluation.change(result, 1);
            i++;
        }

        return evaluation;
    }

    public Object voteOn(Instance i) {
        ClassDistribution classification = new ClassDistribution(targetDomain);

        for (int j = 0; j < trees.size(); j++) {
            Object vote = trees.get(j).vote(i);
            if (vote != null) {
                classification.change(vote, 1);
                //classification.change(Util.sum(), 1);
            } else {
                // TODO add an unknown value
                //classification.change(-1, 1);
                if (log.isErrorEnabled()) {
                    log.error(Util.ntimes("\n", 10) + "Unknown situation at tree: " + j + " for fact " + i);
                }
                System.exit(0);
            }
        }
        classification.evaluateMajority();
        Object winner = classification.getWinnerClass();

        double ratio = 0.0;
        if (classification.getNumIdeas() == 1) {
            //100 %
            ratio = 1;
            return winner;
        } else {
            double numVotes = classification.getVoteFor(winner);
            ratio = (numVotes / (double) trees.size());
            // TODO if the ratio is smaller than some number => reject
        }
        return winner;
    }

    public void printStats(final Stats evaluation, String executionSignature, boolean append) {
        super.printStats(evaluation, executionSignature, append);
    }
}
