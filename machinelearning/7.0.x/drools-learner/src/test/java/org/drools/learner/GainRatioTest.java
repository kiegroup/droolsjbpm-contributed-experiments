package org.drools.learner;

import java.util.ArrayList;
import java.util.List;

import org.drools.learner.builder.Learner;
import org.drools.learner.eval.CondClassDistribution;
import org.drools.learner.eval.InstDistribution;
import org.drools.learner.eval.heuristic.GainRatio;
import org.drools.learner.eval.heuristic.InformationGain;
import org.junit.Test;

import static org.junit.Assert.*;

public class GainRatioTest {


    @Test
    public void testGainRatio(){
        // The Entropy of the entire set, against it's leaf classification.

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

        assertEquals(1.577, GainRatio.splitInfo(condDist), 0.001);
        //System.out.println(outlookEntropy);

        GainRatio gainRatio = new GainRatio();
        gainRatio.init(instDistr);
        assertEquals(0.157, gainRatio.calculateCategoricalGainRatio(outlook), 0.001);
    }

}
