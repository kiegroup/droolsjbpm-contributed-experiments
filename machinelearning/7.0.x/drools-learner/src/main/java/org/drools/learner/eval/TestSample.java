package org.drools.learner.eval;

import java.util.ArrayList;

import org.drools.learner.DecisionTree;
import org.drools.learner.InstanceList;
import org.drools.learner.Stats;
import org.drools.learner.builder.Learner;
import org.drools.learner.builder.SingleTreeBuilder;
import org.drools.learner.builder.SingleTreeTester;
import org.drools.learner.tools.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSample implements ErrorEstimate {

    protected static final transient Logger log = LoggerFactory.getLogger(SingleTreeBuilder.class);

    private int TEST_SET_0 = 0, TEST_SET_1 = 0;

    private InstanceList classInstances;
    private double       errorEstimate, trainingErrorEstimate, numLeavesEstimate, alphaEstimate;
    private boolean WITH_REP = false;
    private int numInstances, trainingDataSize;
    private int[]  crossedSet;
    private double testRatio;

    private DecisionTree dt;

    public TestSample(double ratio) {
        testRatio = ratio;
        errorEstimate = 0.0d;
        trainingErrorEstimate = 0.0d;
        numLeavesEstimate = 0.0d;
        alphaEstimate = 0.0d;
    }

    public int[] crossSet(int N) {
        if (WITH_REP) {
            return Util.bagWRep(N, N);
        } else {
            return Util.bagWoRep(N, N);
        }
    }

    public void validate(Learner trainer, InstanceList instances) {
        classInstances = instances;
        numInstances = classInstances.size();
        if (classInstances.getTargets().size() > 1) {
            //throw new FeatureNotSupported("There is more than 1 target candidates");
            if (log.isErrorEnabled()) {
                log.error("There is more than 1 target candidates\n");
            }
            System.exit(0);
            // TODO put the feature not supported exception || implement it
        }

        TEST_SET_1 = (int) (testRatio * numInstances) + 1;
        System.out.println(testRatio + "*" + numInstances + " " + TEST_SET_1);

        crossedSet = crossSet(numInstances);

        ArrayList<InstanceList> sets        = getSets(0);
        InstanceList            learningSet = sets.get(0);
        InstanceList            testSet     = sets.get(1);

        dt = trainer.trainTree(learningSet);
        dt.setID(0);

        int              error = 0;
        SingleTreeTester t     = new SingleTreeTester(dt);
        if (log.isDebugEnabled()) {
            log.debug("validation fold_size " + testSet.size() + "\n");
        }
        for (int indexI = 0; indexI < testSet.size(); indexI++) {

            if (log.isWarnEnabled()) {
                log.warn(" validation index_i " + indexI + (indexI == testSet.size() - 1 ? "\n" : ""));
            }
            Integer result = t.test(testSet.getInstance(indexI));
            if (result == Stats.INCORRECT) {
                error++;
            }
        }
        //TODO dt.setValidationError(Util.division(error, test_set.size()));
        dt.calcNumNodeLeaves(dt.getRoot());

        //		if (log.error() !=null)
        //			log.error().log("The estimate of : "+(0)+" training=" +dt.getTrainingError() +" valid=" + dt.getValidationError() +" num_leaves=" + dt.getRoot().getNumLeaves()+"\n");

        /* moving averages */
        //TODO error_estimate = dt.getValidationError();
        //TODO training_error_estimate = (double)dt.getTrainingError();
        numLeavesEstimate = (double) dt.getRoot().getNumLeaves();

        //		if (log.stat() !=null)
        //			log.stat().stat("."+ (i == k_fold?"\n":""));
        alphaEstimate = (errorEstimate - trainingErrorEstimate) / numLeavesEstimate;
        if (log.isInfoEnabled()) {
            log.info(" The estimates: training=" + trainingErrorEstimate + " valid=" + errorEstimate + " num_leaves=" + numLeavesEstimate + " the alpha" + alphaEstimate + "\n");
        }
        // TODO how to compute a best tree from the forest
    }

    public ArrayList<InstanceList> getSets(int i) {
        //		// first part divide = 0; divide < fold_size*i
        //		// the validation set divide = fold_size*i; divide < fold_size*(i+1)-1
        //		// last part divide = fold_size*(i+1); divide < N
        InstanceList learningSet   = new InstanceList(classInstances, numInstances - TEST_SET_1 + 1);
        InstanceList validationSet = new InstanceList(classInstances, TEST_SET_1);
        for (int divideIndex = 0; divideIndex < numInstances; divideIndex++) {

            if (log.isInfoEnabled()) {
                log.info("index " + divideIndex + " fold_size" + TEST_SET_1 + " = from " + TEST_SET_0 + " to " + TEST_SET_1 + " num_instances " + numInstances + "\n");
            }
            if (divideIndex >= TEST_SET_0 && divideIndex <= TEST_SET_1) { // validation
                // validation [fold_size*i, fold_size*(i+1))
                if (log.isInfoEnabled()) {
                    log.info("validation one " + divideIndex + "\n");
                }
                validationSet.addAsInstance(classInstances.getInstance(crossedSet[divideIndex]));
            } else { // learninf part 
                learningSet.addAsInstance(classInstances.getInstance(crossedSet[divideIndex]));
            }
        }

        ArrayList<InstanceList> lists = new ArrayList<InstanceList>(2);
        lists.add(learningSet);
        lists.add(validationSet);
        return lists;
    }

    public double getAlphaEstimate() {
        return alphaEstimate;
    }

    public double getErrorEstimate() {
        return errorEstimate;
    }

    public int getEstimatorSize() {
        return 1;
    }

    public DecisionTree getEstimator(int i) {
        return dt;
    }

    public int getTrainingDataSize(int i) {
        return trainingDataSize; //num_instances - TEST_SET_1;
    }

    public int getTestDataSize(int i) {
        return TEST_SET_1;
    }

    public void setTrainingDataSize(int trainingDataSize) {
        this.trainingDataSize = trainingDataSize;
    }
}
