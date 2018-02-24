package org.drools.learner;

import java.util.ArrayList;
import java.util.List;

import org.drools.learner.builder.C45Learner;
import org.drools.learner.builder.Learner;
import org.drools.learner.eval.InstDistribution;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class C45LearnerTest {
    @Test
    public void testAllTargetLabelsTheSame() {
        Domain play = Weather.playDomain;

        // Check it when all target labels are "no"
        InstanceList instList = Weather.getDataPlayAllNo();
        InstDistribution instDistr = new InstDistribution(play, instList.getInstances());

        DecisionTree dt = new DecisionTree(instList.getSchema(), "play");

        List<Domain> attrDomains = new ArrayList<>();

        FixedHeuristic heuristic = new FixedHeuristic();
        C45Learner learner = new C45Learner(heuristic);
        learner.setTrainingDataSize(instList.size());
        learner.train(dt, null, null, instDistr, attrDomains, 0);

        LeafNode leafNode = (LeafNode) dt.getRoot();
        assertEquals( "no", leafNode.getCategory() );
        assertEquals( 5, leafNode.getNumClassification(), 0d );
        assertEquals( 1, leafNode.getRank(), 0d );


        // Now repeat this with "yes"
        instList = Weather.getDataPlayAllYes();
        instDistr = new InstDistribution(play, instList.getInstances());
        learner.setTrainingDataSize(instList.size());
        dt = new DecisionTree(instList.getSchema(), "play");
        learner.train(dt, null, null, instDistr, attrDomains, 0);
        leafNode = (LeafNode) dt.getRoot();
        assertEquals( "yes", leafNode.getCategory() );
        assertEquals( 9, leafNode.getNumClassification(), 0d );
        assertEquals( 1, leafNode.getRank(), 0d );
    }

    @Test
    public void testNoMoreAttributesLeftButStillMultipleTargetLabels() {
        Domain play = Weather.playDomain;

        InstanceList instList = Weather.getDataMoreYes();
        InstDistribution instDistr = new InstDistribution(play, instList.getInstances());
        DecisionTree dt = new DecisionTree(instList.getSchema(), "play");

        List<Domain> attrDomains = new ArrayList<>();

        FixedHeuristic heuristic = new FixedHeuristic();
        C45Learner learner = new C45Learner(heuristic);
        learner.setTrainingDataSize(instList.size());
        learner.train(dt, null, null, instDistr, attrDomains, 0);

        LeafNode leafNode = (LeafNode) dt.getRoot();
        assertEquals( "yes", leafNode.getCategory() );
        assertEquals( 14, leafNode.getNumMatch(), 0d );
        assertEquals( 9, leafNode.getNumClassification(), 0d );
        assertEquals( 0.642, leafNode.getRank(), 0.001 );
        assertEquals( 5, leafNode.getMissClassified(), 0d );

        instList = Weather.getDataMoreNo();
        instDistr = new InstDistribution(play, instList.getInstances());
        dt = new DecisionTree(instList.getSchema(), "play");
        learner.setTrainingDataSize(instList.size());
        learner.train(dt, null, null, instDistr, attrDomains, 0);

        leafNode = (LeafNode) dt.getRoot();
        assertEquals( "no", leafNode.getCategory() );
        assertEquals( 14, leafNode.getNumMatch(), 0d );
        assertEquals( 8, leafNode.getNumClassification(), 0d );
        assertEquals( 0.571, leafNode.getRank(), 0.001 );
        assertEquals( 6, leafNode.getMissClassified(), 0d );
    }

    @Test
    public void testCategoryWithNo() {
        Domain play = Weather.playDomain;
        Domain outlook = Weather.outlookDomain;

        InstanceList instList = Weather.getDataNoOvercast();
        InstDistribution instDistr = new InstDistribution(play, instList.getInstances());

        DecisionTree dt = new DecisionTree(instList.getSchema(), play.getFReferenceName());
        List<Domain> attrDomains = new ArrayList<>();
        attrDomains.add(outlook);
        //attrDomains.add(wind);

        FixedHeuristic heuristic = new FixedHeuristic();
        C45Learner learner = new C45Learner(heuristic);
        heuristic.values.put(outlook, 0.3);
        learner.setTrainingDataSize(instList.size());
        learner.train(dt, null, null, instDistr, attrDomains, 0);

        TreeNode node = dt.getRoot();
        assertSame(outlook, node.getDomain());
        assertEquals(3, node.getChildrenKeys().size());
        assertEquals(10, node.getNumMatch(), 0);

        LeafNode sunLeaf = (LeafNode) node.getChild("sun");
        assertEquals("yes", sunLeaf.getCategory());
        assertEquals(4, sunLeaf.getNumClassification(), 0);
        assertEquals(6, sunLeaf.getNumMatch(), 0);
        assertEquals(2, sunLeaf.getMissClassified(), 0);

        LeafNode rainLeaf = (LeafNode) node.getChild("rain");
        assertEquals("no", rainLeaf.getCategory());
        assertEquals(4, rainLeaf.getNumClassification(), 0);
        assertEquals(4, rainLeaf.getNumMatch(), 0);
        assertEquals(0, rainLeaf.getMissClassified(), 0);

        LeafNode overcast = (LeafNode) node.getChild("overcast");
        assertEquals("no", overcast.getCategory());
        assertEquals(0, overcast.getNumClassification(), 0);
        assertEquals(0, overcast.getNumMatch(), 0);
        assertEquals(0, overcast.getMissClassified(), 0);
    }


    public static InstanceList getDataTest1() {
        Schema schema  = Schema.createSchemaStructure(Weather.class, Learner.DataType.STRUCTURED);

        List<Weather> list = new ArrayList<Weather>();
        list.add(new Weather("sun", "hot", "high", "low", "yes"));
        list.add(new Weather("sun", "cold", "high", "high", "yes"));
        list.add(new Weather("overcast", "hot", "high", "low", "yes"));
        list.add(new Weather("overcast", "cold", "normal", "high", "no"));
        list.add(new Weather("rain", "mild", "high", "low", "yes"));
        list.add(new Weather("rain", "cold", "normal", "low", "no"));
        InstanceList instList = new InstanceList(schema);
        list.stream().forEach(v->instList.addStructuredInstance(v));
        return instList;
    }

    @Test
    public void testCorrectTree() {
        Domain play = Weather.playDomain;
        Domain outlook = Weather.outlookDomain;
        Domain temperature = Weather.temperatureDomain;


        InstanceList instList = getDataTest1();
        InstDistribution instDistr = new InstDistribution(play, instList.getInstances());

        DecisionTree dt = new DecisionTree(instList.getSchema(), play.getFReferenceName());
        List<Domain> attrDomains = new ArrayList<>();
        attrDomains.add(outlook);
        attrDomains.add(temperature);

        FixedHeuristic heuristic = new FixedHeuristic();
        C45Learner     learner   = new C45Learner(heuristic);
        heuristic.values.put(outlook, 0.3);
        heuristic.values.put(temperature, 0.2);
        learner.setTrainingDataSize(instList.size());
        learner.train(dt, null, null, instDistr, attrDomains, 0);

        //Tdt.getRoot()
    }
}
