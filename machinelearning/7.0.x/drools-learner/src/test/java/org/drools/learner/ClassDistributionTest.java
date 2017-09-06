package org.drools.learner;

import org.drools.learner.eval.ClassDistribution;
import org.junit.Test;
import static  org.junit.Assert.*;

public class ClassDistributionTest {

    @Test
    public void testSingleCategory(){
        Domain domain  = new Domain(Car.class, "size", String.class);
        domain.addCategory("s");
        domain.addCategory("m");
        domain.addCategory("l");
        ClassDistribution classDistribution = new ClassDistribution(domain);

        classDistribution.change("s", 1);
        classDistribution.change("s", 2);
        classDistribution.change("s", 3);
        assertEquals(6.0d, classDistribution.getSum(), 0);
        assertEquals( 6,  classDistribution.getVoteFor("s"), 0);
        assertEquals( 0,  classDistribution.getVoteFor("m"), 0);
        assertEquals( 0,  classDistribution.getVoteFor("l"), 0);

        assertEquals( 0, classDistribution.getNumberOfSupportedCategories() );


        classDistribution.evaluateMajority();

        assertEquals( 1, classDistribution.getNumberOfSupportedCategories() );
        assertEquals( "s", classDistribution.getWinnerClass() );
    }

    @Test
    public void testMultipleCategories(){
        Domain domain = new Domain(Car.class, "size", String.class);
        domain.addCategory("s");
        domain.addCategory("m");
        domain.addCategory("l");
        ClassDistribution classDistribution = new ClassDistribution(domain);

        classDistribution.change("s", 1);
        classDistribution.change("s", 2);
        classDistribution.change("s", 3);
        classDistribution.change("m", 10);
        assertEquals(16.0d, classDistribution.getSum(), 0);
        assertEquals( 6,  classDistribution.getVoteFor("s"), 0);
        assertEquals( 10,  classDistribution.getVoteFor("m"), 0);

        assertEquals( 0, classDistribution.getNumberOfSupportedCategories() );


        classDistribution.evaluateMajority();

        assertEquals( 2, classDistribution.getNumberOfSupportedCategories() );
        assertEquals( "m", classDistribution.getWinnerClass() );
    }
}
