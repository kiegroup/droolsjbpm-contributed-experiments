package org.drools.learner;

import java.util.ArrayList;
import java.util.List;

import org.drools.learner.eval.AttributeChooser;
import org.drools.learner.eval.InformationContainer;
import org.drools.learner.eval.InstDistribution;
import org.junit.Test;

import static org.junit.Assert.*;

public class AttributeChooserTest {


    @Test
    public void testChooseAttribute(){
        // The Entropy of the entire set, against it's leaf classification.

        Domain play = new Domain(Weather.class, "play", String.class);
        play.addCategory("no");
        play.addCategory("yes");

        Domain outlook = new Domain(Weather.class, "outlook", String.class);
        outlook.addCategory("sun");
        outlook.addCategory("overcast");
        outlook.addCategory("rain");

        Domain humidity = new Domain(Weather.class, "humidity", String.class);
        humidity.addCategory("high");
        humidity.addCategory("normal");

        List<Domain> attrDomains = new ArrayList<>();
        attrDomains.add(outlook);
        attrDomains.add(humidity);

        InstanceList instList = Weather.getData();
        InstDistribution instDistr = new InstDistribution(play, instList.getInstances());

        FixedHeuristic heuristic = new FixedHeuristic();
        heuristic.values.put(outlook, 0.6);
        heuristic.values.put(humidity, 0.3);
        AttributeChooser attrChooser = new AttributeChooser(heuristic);

        InformationContainer bestAttrEval = new InformationContainer(null, 0, 0 );
        assertNull(bestAttrEval.getDomain());
        attrChooser.chooseAttribute(bestAttrEval, instDistr, attrDomains);
        assertEquals( outlook, bestAttrEval.getDomain());
        assertSame(outlook, bestAttrEval.getDomain());

        // reverse the values, make sure the choice is different
        heuristic.values.put(outlook, 0.3);
        heuristic.values.put(humidity, 0.6);

        bestAttrEval = new InformationContainer(null, 0, 0 );
        assertNull(bestAttrEval.getDomain());
        attrChooser.chooseAttribute(bestAttrEval, instDistr, attrDomains);
        assertEquals( humidity, bestAttrEval.getDomain());
        assertSame(humidity, bestAttrEval.getDomain());
    }
}
