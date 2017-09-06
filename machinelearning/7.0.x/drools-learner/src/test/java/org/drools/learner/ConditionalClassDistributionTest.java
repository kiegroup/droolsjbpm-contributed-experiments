package org.drools.learner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.drools.learner.builder.Learner;
import org.drools.learner.eval.CondClassDistribution;
import org.drools.learner.eval.InformationContainer;
import org.drools.learner.eval.InstDistribution;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConditionalClassDistributionTest {

    @Test
    public void testDistributionBasedOnSubCategory() {
        Domain size = new Domain(Car.class, "size", String.class);
        size.addCategory("s");
        size.addCategory("m");
        size.addCategory("l");

        Domain value = new Domain(Car.class, "value", String.class);
        value.addCategory("low");
        value.addCategory("medium");
        value.addCategory("high");


        Schema          schema      = Schema.createSchemaStructure(Car.class, Learner.DataType.STRUCTURED);
        InstanceFactory instFactory = new InstanceFactory(schema);
        List<Instance>       list        = new ArrayList<Instance>();
        list.add( instFactory.createInstance(new Car( "m1",2, "s", "p", "low")));
        list.add( instFactory.createInstance(new Car( "m2",2, "s", "d",  "medium")));
        list.add( instFactory.createInstance(new Car( "m3",2, "s", "e",  "high")));
        list.add( instFactory.createInstance(new Car( "m4",2, "m", "e",  "high")));
        list.add( instFactory.createInstance(new Car( "m5",4, "m", "e",  "low")));
        list.add( instFactory.createInstance(new Car( "m6",4, "l", "e",  "low")));

        CondClassDistribution condDist = new CondClassDistribution(size, value);

        for (Instance i : list) {
            condDist.change(i.getAttr(size.getFReferenceName()).getValue(), i.getAttr(value.getFReferenceName()).getValue(), 1);
        }

        assertEquals( 3, condDist.getDistributionOf("s").getSum(), 0 );
        assertEquals( 2, condDist.getDistributionOf("m").getSum(), 0 );
        assertEquals( 1, condDist.getDistributionOf("l").getSum(), 0 );

        assertEquals( 3, condDist.getNumCondClasses() );
    }

}
