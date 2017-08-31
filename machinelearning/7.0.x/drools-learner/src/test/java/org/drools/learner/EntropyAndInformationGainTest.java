package org.drools.learner;

import org.drools.learner.builder.Learner;
import org.drools.learner.eval.CondClassDistribution;
import org.drools.learner.eval.InstDistribution;
import org.drools.learner.eval.heuristic.InformationGain;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EntropyAndInformationGainTest {

    @Test
    public void testSetEntropy(){
        // The Entropy of the entire set, against it's leaf classification.
        Schema schema  = Schema.createSchemaStructure(Weather.class, Learner.DataType.STRUCTURED);

        Domain play = new Domain(Weather.class, "play", String.class);
        play.addCategory("no");
        play.addCategory("yes");

        InstanceList instList = Weather.getData();

        InstDistribution instDistr = new InstDistribution(play, instList.getInstances());
        InformationGain  informationGain   = new InformationGain();
        informationGain.init(instDistr);
        assertEquals( 0.94d, informationGain.getDataEval(), 0.01);
    }

    @Test
    public void testInformationGain(){
        // The Entropy of the entire set, against it's leaf classification.
        Schema schema  = Schema.createSchemaStructure(Weather.class, Learner.DataType.STRUCTURED);

        Domain play = new Domain(Weather.class, "play", String.class);
        play.addCategory("no");
        play.addCategory("yes");

        Domain outlook = new Domain(Weather.class, "outlook", String.class);
        outlook.addCategory("sun");
        outlook.addCategory("overcast");
        outlook.addCategory("rain");

        InstanceList instList = Weather.getData();

        InstDistribution instDistr = new InstDistribution(play, instList.getInstances());

        CondClassDistribution condDist = instDistr.categoricalGroupBy(outlook);

        assertEquals(0.694, InformationGain.calculateInformation(condDist), 0.001);
        //System.out.println(outlookEntropy);

        InformationGain entropy = new InformationGain();
        entropy.init(instDistr);
        assertEquals(0.247, entropy.calculateInformationGain(outlook), 0.001);
    }

}
