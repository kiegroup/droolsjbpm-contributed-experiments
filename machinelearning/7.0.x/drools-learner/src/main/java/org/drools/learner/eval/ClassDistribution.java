package org.drools.learner.eval;

import java.util.Hashtable;

import org.drools.learner.Domain;
import org.drools.learner.tools.Util;

/* simple histogram keeps the number of instances in each class 
 * class: categories of the target attribute*/
public class ClassDistribution {

    //private static final Logger log = LoggerFactory.getSysOutLogger(LogLevel.ERROR);
    //protected static final Logger flog = LoggerFactory.getFileLogger(ClassDistribution.class, LogLevel.ERROR, Util.log_file); 

    protected Domain targetAttr;
    private Hashtable<Object, Double> quantityByClass;

    private String sumKey = Util.sum();
    private int numCategoryIdeas;
    private Object winnerCategory;

    public ClassDistribution( Domain targetDomain ) {
        this.targetAttr = targetDomain;
        //System.out.println("(ClassDistribution)target_attr "+ target_attr);
        this.quantityByClass = new Hashtable<Object, Double>( this.targetAttr.getCategoryCount() + 1 );
        for ( int c = 0; c < this.targetAttr.getCategoryCount(); c++ ) {
            Object category = this.targetAttr.getCategory( c );
            quantityByClass.put( category, 0.0d );
        }

        quantityByClass.put( sumKey, 0.0d );

        numCategoryIdeas = 0;
    }

    public ClassDistribution( ClassDistribution copyDist ) {
        this.targetAttr = copyDist.getClassDomain();
        this.quantityByClass = new Hashtable<Object, Double>( this.targetAttr.getCategoryCount() + 1 );
        this.setDistribution( copyDist );

        this.numCategoryIdeas = copyDist.getNumIdeas();
        this.winnerCategory = copyDist.getWinnerClass();

    }

    public void setSum( double sum ) {
        quantityByClass.put( sumKey, sum );
    }

    public double getSum() {
        return quantityByClass.get( sumKey );
    }

    public Domain getClassDomain() {
        return targetAttr;
    }

    public void change( Object targetCategory, double i ) {
        /*
         * TODO ???? if (target_category == sum_key) return;
         */
        double num1 = quantityByClass.get( targetCategory );
        num1 += i;
        quantityByClass.put( targetCategory, num1 );

        //quantity_by_class.put(target_category, quantity_by_class.get(target_category)+i);
    }

    public double getVoteFor( Object targetCategory ) {
        return quantityByClass.get( targetCategory );
    }

    public void evaluateMajority() {
        double winnerVote = 0.0d;
        int numIdeas = 0; // the number of target categories that the instances belong to

        Object winner = null;

        for ( int c = 0; c < this.targetAttr.getCategoryCount(); c++ ) {
            Object category = this.targetAttr.getCategory( c );

            double numInClass = this.getVoteFor( category );
            if ( numInClass > 0 ) {
                numIdeas++;
                if ( numInClass > winnerVote ) {
                    winnerVote = numInClass;
                    winner = category;
                }
            }
        }

        this.setNumIdeas( numIdeas );
        this.setWinnerClass( winner );
    }

    public void setNumIdeas( int numSuppertedTargetClasses ) {
        this.numCategoryIdeas = numSuppertedTargetClasses;
    }

    public void setWinnerClass( Object theWinnerTargetClass ) {
        this.winnerCategory = theWinnerTargetClass;
    }

    public int getNumIdeas() {
        return this.numCategoryIdeas;
    }

    public Object getWinnerClass() {
        return this.winnerCategory;
    }

    public String toString() {
        StringBuffer sbOut = new StringBuffer( "ClassDist: target:" + this.targetAttr.getFName() + " total: " + this.getSum() + " & categories:" );
        for ( int c = 0; c < this.targetAttr.getCategoryCount(); c++ ) {
            Object category = this.targetAttr.getCategory( c );
            sbOut.append( this.getVoteFor( category ) + " @" + category + ", " );
        }
        //		out +="\n";
        return sbOut.toString();
    }

    public void setDistribution( ClassDistribution targetDist ) {
        for ( int c = 0; c < this.targetAttr.getCategoryCount(); c++ ) {
            Object category = this.targetAttr.getCategory( c );
            this.quantityByClass.put( category, targetDist.getVoteFor( category ) );
        }
        this.quantityByClass.put( sumKey, targetDist.getSum() );
    }

}
