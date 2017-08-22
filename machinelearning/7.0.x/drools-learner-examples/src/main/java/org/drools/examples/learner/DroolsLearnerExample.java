package org.drools.examples.learner;

import java.util.List;

import org.drools.learner.DecisionTree;
import org.drools.learner.builder.DecisionTreeFactory;
import org.drools.learner.tools.RulePrinter;
import org.kie.api.KieBase;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroolsLearnerExample {

    private static final Logger LOG = LoggerFactory.getLogger( DroolsLearnerExample.class );

    public static final int SINGLE_C45E = 121;
    public static final int SINGLE_C45G = 122;
    public static final int SINGLE_C45E_WORST = 123;
    public static final int SINGLE_C45_RANDOM = 124;
    public static final int SINGLE_C45E_STOP = 131;
    public static final int SINGLE_C45G_STOP = 132;
    public static final int SINGLE_C45E_PRUN_STOP = 141;
    public static final int SINGLE_C45G_PRUN_STOP = 142;
    public static final int BAG_C45E = 221;
    public static final int BAG_C45G = 222;
    public static final int BAG_C45E_STOP = 231;
    public static final int BAG_C45G_STOP = 232;
    public static final int BAG_C45E_PRUN_STOP = 241;
    public static final int BAG_C45G_PRUN_STOP = 242;
    public static final int BOOST_C45E = 321;
    public static final int BOOST_C45G = 322;
    public static final int BOOST_C45E_STOP = 331;
    public static final int BOOST_C45G_STOP = 332;
    public static final int BOOST_C45E_PRUN_STOP = 341;
    public static final int BOOST_C45G_PRUN_STOP = 342;

    private String drl;
    private KieBase kbase;
    private int algorithm;

    public DroolsLearnerExample( int algorithm ) {
        this.algorithm = algorithm;
    }

    /**
     * 
     * Trains a learner model of the speciied type against the domain model data provided
     * 
     * @param type
     * @param data
     * @throws Exception
     */
    public void runTraningExample( Class<?> type, List<Object> data ) throws Exception {
        // instantiate a learner for a specific object class and pass session to train
        DecisionTree decisionTree = null;
        /*
         * Single   1xx, Bag    2xx, Boost 3xx
         * ID3      x1x, C45    x2x
         * Entropy  xx1, Gain   xx2
         */
        switch ( algorithm ) {
            case SINGLE_C45E:
                decisionTree = DecisionTreeFactory.createSingleC45E( data, type );
                break;
            case SINGLE_C45G:
                decisionTree = DecisionTreeFactory.createSingleC45G( data, type );
                break;
            case SINGLE_C45E_WORST:
                decisionTree = DecisionTreeFactory.createSingleC45EWorst( data, type );
                break;
            case SINGLE_C45_RANDOM:
                decisionTree = DecisionTreeFactory.createSingleC45Random( data, type );
                break;
            case SINGLE_C45E_STOP:
                decisionTree = DecisionTreeFactory.createSingleC45EStop( data, type );
                break;
            case SINGLE_C45G_STOP:
                decisionTree = DecisionTreeFactory.createSingleC45GStop( data, type );
                break;
            case SINGLE_C45E_PRUN_STOP:
                decisionTree = DecisionTreeFactory.createSingleC45EPrunStop( data, type );
                break;
            case SINGLE_C45G_PRUN_STOP:
                decisionTree = DecisionTreeFactory.createSingleC45GPrunStop( data, type );
                break;
            case BAG_C45E:
                decisionTree = DecisionTreeFactory.createBagC45E( data, type );
                break;
            case BAG_C45G:
                decisionTree = DecisionTreeFactory.createBagC45G( data, type );
                break;
            case BAG_C45E_STOP:
                decisionTree = DecisionTreeFactory.createBagC45EStop( data, type );
                break;
            case BAG_C45G_STOP:
                decisionTree = DecisionTreeFactory.createBagC45GStop( data, type );
                break;
            case BAG_C45E_PRUN_STOP:
                decisionTree = DecisionTreeFactory.createBagC45EPrunStop( data, type );
                break;
            case BAG_C45G_PRUN_STOP:
                decisionTree = DecisionTreeFactory.createBagC45GPrunStop( data, type );
                break;
            case BOOST_C45E:
                decisionTree = DecisionTreeFactory.createBoostC45E( data, type );
                break;
            case BOOST_C45G:
                decisionTree = DecisionTreeFactory.createBoostC45G( data, type );
                break;
            case BOOST_C45E_STOP:
                decisionTree = DecisionTreeFactory.createBoostC45EStop( data, type );
                break;
            case BOOST_C45G_STOP:
                decisionTree = DecisionTreeFactory.createBoostC45GStop( data, type );
                break;
            case BOOST_C45E_PRUN_STOP:
                decisionTree = DecisionTreeFactory.createBoostC45EPrunStop( data, type );
                break;
            case BOOST_C45G_PRUN_STOP:
                decisionTree = DecisionTreeFactory.createBoostC45GPrunStop( data, type );
                break;
        }

        drl = RulePrinter.readRules( decisionTree );
        kbase = new KieHelper().addContent( drl, ResourceType.DRL ).build();
    }

    /**
     * Executes the generated rules against the data provided.
     * 
     * You must run trainingExample first...
     * 
     * @param data
     */
    public void runGeneratedRules( List<Object> data ) {

        if ( kbase == null ) {
            throw new UnsupportedOperationException( "The learner been trained yet..." );
        }

        KieSession ksession = kbase.newKieSession();
        data.stream().forEach( ksession::insert );
        ksession.fireAllRules();
        ksession.dispose();
    }

    public String getDrl() {
        return this.drl;
    }
}