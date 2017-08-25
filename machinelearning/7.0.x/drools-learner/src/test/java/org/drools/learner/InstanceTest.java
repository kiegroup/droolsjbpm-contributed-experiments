package org.drools.learner;

import org.drools.learner.builder.Learner;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InstanceTest {

    @Test
    public void testInstanceCreationAndReadAccessor() {
        Schema schema  = Schema.createSchemaStructure(Car.class, Learner.DataType.STRUCTURED);
        InstanceFactory instFactory = new InstanceFactory(schema);
        Instance m1 = instFactory.createInstance(new Car( "m1",2, "s", "p", "low"));
        Instance m2 = instFactory.createInstance(new Car( "m2",2, "s", "d", "medium"));
        Instance m3 = instFactory.createInstance(new Car( "m3",2, "s", "e", "high"));


        assertEquals( "m1", m1.getAttr("org.drools.learner.Car@model").getValue() );
        assertEquals( "s", m2.getAttr("org.drools.learner.Car@size").getValue() );
        assertEquals( "e", m3.getAttr("org.drools.learner.Car@fuel").getValue() );


    }
}
