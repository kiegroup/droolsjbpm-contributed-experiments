package org.drools.learner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.drools.learner.builder.Learner;
import org.drools.learner.eval.InformationContainer;
import org.drools.learner.eval.InstDistribution;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class InstDistributionTest {

    @Test
    public void testCategoricalSplits() {
        Domain size = new Domain(Car.class, "size", String.class);
        size.addCategory("s");
        size.addCategory("m");
        size.addCategory("l");


        Schema          schema      = Schema.createSchemaStructure(Car.class, Learner.DataType.STRUCTURED);
        InstanceFactory instFactory = new InstanceFactory(schema);
        List<Instance>       list        = new ArrayList<Instance>();
        list.add( instFactory.createInstance(new Car( "m1",2, "s", "p", "low")));
        list.add( instFactory.createInstance(new Car( "m2",2, "s", "d",  "medium")));
        list.add( instFactory.createInstance(new Car( "m3",2, "m", "e",  "high")));
        list.add( instFactory.createInstance(new Car( "m4",4, "l", "e",  "low")));


        InstDistribution distr = new InstDistribution(size, list);
        assertNull(distr.getSupportersFor("x"));

        List<Instance> supported = distr.getSupportersFor("s");
        assertEquals( 2, supported.size());

        supported = distr.getSupportersFor("m");
        assertEquals( 1, supported.size());

        supported = distr.getSupportersFor("l");
        assertEquals( 1, supported.size());


        Domain fuel = new Domain(Car.class, "fuel", String.class);
        fuel.addCategory("p");
        fuel.addCategory("d");
        fuel.addCategory("e");
        InformationContainer info = new InformationContainer();
        info.setDomain(fuel);
        HashMap<Object, InstDistribution> map =  distr.split(info);

        assertEquals(3, map.size() );
        assertEquals(1, map.get("p").getSum(), 0);
        assertEquals(1, map.get("d").getSum(), 0);
        assertEquals(2, map.get("e").getSum(), 0);

        Domain doors = new Domain(Car.class, "doors", String.class);
        doors.addCategory(2);
        doors.addCategory(4);
        info = new InformationContainer();
        info.setDomain(doors);
        map =  map.get("e").split(info);

        assertEquals(2, map.size() );
        assertEquals(1, map.get(2).getSum(), 0);
        assertEquals(1, map.get(4).getSum(), 0);


        assertEquals("high", map.get(2).getSupportersFor("m").get(0).getAttr("org.drools.learner.Car@value").getValue());
        assertEquals("low", map.get(4).getSupportersFor("l").get(0).getAttr("org.drools.learner.Car@value").getValue());
    }

}
