package org.drools.learner.eval;

import java.util.ArrayList;

import org.drools.learner.DecisionTree;
import org.drools.learner.InstanceList;
import org.drools.learner.Stats;
import org.drools.learner.builder.Learner;
import org.drools.learner.builder.SingleTreeTester;
import org.drools.learner.tools.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrossValidation implements ErrorEstimate {

    protected static final transient Logger   log = LoggerFactory.getLogger(CrossValidation.class);
    private final                int    MIN_NUM_FOLDS = 2;
    private int kFold;
    private int numInstances;
    /*
     * estimated values validation_error_estimate is used to estimate the true
     * misclassification rate
     */
    private double validationErrorEstimate, trainingErrorEstimate, numLeavesEstimate, alphaEstimate;
    private int[]                   crossedSet;
    private int[][]                 foldIndices;
    private ArrayList<DecisionTree> forest;
    private InstanceList            classInstances;

    private int VALID_SET_0 = 0, VALID_SET_1 = 1;

    private boolean WITH_REP = false;

    public CrossValidation(int k) {
        if (k <= 1) {
            if (log.isWarnEnabled()) {
                log.warn("There is 1 or less number of folds specified, i am setting " + MIN_NUM_FOLDS + " folds\n");
            }
            kFold = MIN_NUM_FOLDS;
        } else {
            kFold = k;
        }
        forest = new ArrayList<DecisionTree>(kFold);
        validationErrorEstimate = 0.0d;
        trainingErrorEstimate = 0.0d;
        numLeavesEstimate = 0.0d;
        alphaEstimate = 0.0d;

        foldIndices = new int[kFold][2];
    }

    public int[] crossSet(int N) {
        if (WITH_REP) {
            return Util.bagWRep(N, N);
        } else {
            return Util.bagWoRep(N, N);
        }
    }

    // for small samples
    public void validate(Learner trainer, InstanceList instances) {
        classInstances = instances;
        numInstances = classInstances.getSize();
        if (classInstances.getTargets().size() > 1) {
            //throw new FeatureNotSupported("There is more than 1 target candidates");
            if (log.isErrorEnabled()) {
                log.error("There is more than 1 target candidates\n");
            }
            System.exit(0);
            // TODO put the feature not supported exception || implement it
        }

        crossedSet = crossSet(numInstances);

        // N-fold 
        calcFoldIndices(numInstances);

        int i = 0;
        //			int[] bag;		
        while (i++ < kFold) {
            int foldSize = getTestDataSize(i);
            if (log.isDebugEnabled()) {
                log.debug("i " + (i - 1) + "/" + kFold);
            }
            ArrayList<InstanceList> sets          = getSets(i - 1);
            InstanceList            learningSset  = sets.get(0);
            InstanceList            validationSet = sets.get(1);

            DecisionTree dt = trainer.trainTree(learningSset);
            dt.setID(i - 1);
            forest.add(dt);

            int              error = 0;
            SingleTreeTester t     = new SingleTreeTester(dt);
            if (log.isDebugEnabled()) {
                log.debug("validation fold_size " + foldSize + "\n");
            }
            for (int indexI = 0; indexI < foldSize; indexI++) {

                if (log.isDebugEnabled()) {
                    log.warn(" validation index_i " + indexI + (indexI == foldSize - 1 ? "\n" : ""));
                }
                Integer result = t.test(validationSet.getInstance(indexI));
                if (result == Stats.INCORRECT) {
                    error++;
                }
            }
            //TODO dt.setTrainError(Util.division(error, fold_size));
            dt.calcNumNodeLeaves(dt.getRoot());

            //			if (log.error() !=null)
            //				log.error().log("The estimate of : "+(i-1)+" training=" +dt.getTrainingError() +" valid=" + dt.getValidationError() +" num_leaves=" + dt.getRoot().getNumLeaves()+"\n");

            /* moving averages */
            validationErrorEstimate += ((double) error / (double) foldSize) / (double) kFold;
            //TODO training_error_estimate += ((double)dt.getTrainingError())/(double)k_fold;//((double)dt.getTrainingError()/(double)(num_instances-fold_size))/(double)k_fold;
            numLeavesEstimate += (double) dt.getRoot().getNumLeaves() / (double) kFold;

            //			if (log.stat() !=null)
            //				log.stat().stat("."+ (i == k_fold?"\n":""));

        }
        alphaEstimate = (validationErrorEstimate - trainingErrorEstimate) / numLeavesEstimate;
        if (log.isInfoEnabled()) {
            log.info(" The estimates: training=" + trainingErrorEstimate + " valid=" + validationErrorEstimate + " num_leaves=" + numLeavesEstimate + " the alpha" + alphaEstimate + "\n");
        }
        // TODO how to compute a best tree from the forest
    }

    private void calcFoldIndices(int numInstances2) {
        // TODO Auto-generated method stub
        System.out.println(numInstances2 + " by " + kFold);
        int excess    = numInstances2 % kFold;
        int foldIndex = 0, divideIndex = 0;
        while (foldIndex < kFold) {

            foldIndices[foldIndex][VALID_SET_0] = divideIndex;
            int foldSize = getTestDataSize(foldIndex);
            foldIndices[foldIndex][VALID_SET_1] = foldIndices[foldIndex][VALID_SET_0] + foldSize - 1;

            System.out.println(foldIndices[foldIndex][VALID_SET_0] + " - " + foldIndices[foldIndex][VALID_SET_1]);
            divideIndex += foldSize;
            foldIndex++;
        }
    }

    public ArrayList<InstanceList> getSets(int i) {
        //		// first part divide = 0; divide < fold_size*i
        //		// the validation set divide = fold_size*i; divide < fold_size*(i+1)-1
        //		// last part divide = fold_size*(i+1); divide < N
        int          validFoldSize = getTestDataSize(i);
        InstanceList learningSet   = new InstanceList(classInstances, numInstances - validFoldSize + 1);
        InstanceList validationSet = new InstanceList(classInstances, validFoldSize);
        for (int divideIndex = 0; divideIndex < numInstances; divideIndex++) {

            if (log.isInfoEnabled()) {
                log.info("index " + divideIndex + " fold_size" + validFoldSize + " i " + i + " = from " + foldIndices[i][VALID_SET_0] + " to " + foldIndices[i][VALID_SET_1] + " num_instances "
                                    + numInstances + "\n");
            }
            if (divideIndex >= foldIndices[i][VALID_SET_0] && divideIndex <= foldIndices[i][VALID_SET_1]) { // validation
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

    public int getTrainingDataSize(int i) {
        return numInstances - getTestDataSize(i);
    }

    public double getAlphaEstimate() {
        return alphaEstimate;
    }

    public int getTestDataSize(int i) {
        int excess = numInstances % kFold;
        return (int) numInstances / kFold + (i < excess ? 1 : 0);
    }

    public double getErrorEstimate() {
        return validationErrorEstimate;
    }

    public DecisionTree getEstimator(int i) {
        return forest.get(i);
    }

    public int getEstimatorSize() {
        return kFold;
    }

    public void setTrainingDataSize(int trainingDataSize) {
        // TODO Auto-generated method stub

    }
}
