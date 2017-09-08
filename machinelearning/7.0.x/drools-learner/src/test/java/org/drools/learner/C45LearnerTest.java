package org.drools.learner;

import java.util.ArrayList;
import java.util.List;

import org.drools.learner.builder.C45Learner;
import org.drools.learner.eval.InstDistribution;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class C45LearnerTest {

    @Test
    public void testAllTargetLabelsTheSame() {
        Domain play = new Domain(Weather.class, "play", String.class);
        play.addCategory("no");
        play.addCategory("yes");

        // Check it when all target labels are "no"
        InstanceList instList = Weather.getDataPlayNo();
        InstDistribution instDistr = new InstDistribution(play, instList.getInstances());

        DecisionTree dt = new DecisionTree(instList.getSchema(), "play");

        List<Domain> attrDomains = new ArrayList<>();

        FixedHeuristic heuristic = new FixedHeuristic();
        C45Learner learner = new C45Learner(heuristic);
        learner.setTrainingDataSize(instList.size() * 2); // just double the size, so we can check rank was applied correctly
        learner.train(dt, null, null, instDistr, attrDomains, 0);

        LeafNode leafNode = (LeafNode) dt.getRoot();
        assertEquals( "no", leafNode.getCategory() );
        assertEquals( 5, leafNode.getNumClassification(), 0d );
        assertEquals( 0.5, leafNode.getRank(), 0d );


        // Now repeat this with "yes"
        instList = Weather.getDataPlayYes();
        instDistr = new InstDistribution(play, instList.getInstances());
        learner.train(dt, null, null, instDistr, attrDomains, 0);
        leafNode = (LeafNode) dt.getRoot();
        assertEquals( "yes", leafNode.getCategory() );
        assertEquals( 9, leafNode.getNumClassification(), 0d );
    }

    @Test
    public void testNoMoreAttributesLeftButStillMultipleTargetLabels() {
        Domain play = new Domain(Weather.class, "play", String.class);
        play.addCategory("no");
        play.addCategory("yes");

        InstanceList instList = Weather.getDataMoreYes();
        InstDistribution instDistr = new InstDistribution(play, instList.getInstances());

        DecisionTree dt = new DecisionTree(instList.getSchema(), "play");
        List<Domain> attrDomains = new ArrayList<>();

        FixedHeuristic heuristic = new FixedHeuristic();
        C45Learner learner = new C45Learner(heuristic);
        learner.setTrainingDataSize(instList.size() * 2); // just double the size, so we can check rank was applied correctly
        learner.train(dt, null, null, instDistr, attrDomains, 0);

        LeafNode leafNode = (LeafNode) dt.getRoot();
        assertEquals( "yes", leafNode.getCategory() );
        assertEquals( 9, leafNode.getNumMatch(), 0d );
        assertEquals( 9, leafNode.getNumClassification(), 0d );
        assertEquals( 0.5, leafNode.getRank(), 0.001 );
        assertEquals( 0, leafNode.getMissClassified(), 0d );

        instList = Weather.getDataMoreNo();
        instDistr = new InstDistribution(play, instList.getInstances());
        learner.train(dt, null, null, instDistr, attrDomains, 0);

        leafNode = (LeafNode) dt.getRoot();
        assertEquals( "no", leafNode.getCategory() );
        assertEquals( 14, leafNode.getNumMatch(), 0d );
        assertEquals( 8, leafNode.getNumClassification(), 0d );
        assertEquals( 0.444, leafNode.getRank(), 0.001 );
        assertEquals( 6, leafNode.getMissClassified(), 0d );
    }

    @Test
    public void testCategoryWithNo() {
        Domain play = new Domain(Weather.class, "play", String.class);
        play.addCategory("no");
        play.addCategory("yes");

        Domain outlook = new Domain(Weather.class, "outlook", String.class);
        outlook.addCategory("sun");
        outlook.addCategory("overcast");
        outlook.addCategory("rain");

        InstanceList instList = Weather.getDataNoOvercast();
        InstDistribution instDistr = new InstDistribution(play, instList.getInstances());

        DecisionTree dt = new DecisionTree(instList.getSchema(), play.getFReferenceName());
        List<Domain> attrDomains = new ArrayList<>();
        attrDomains.add(outlook);
        //attrDomains.add(wind);

        FixedHeuristic heuristic = new FixedHeuristic();
        C45Learner learner = new C45Learner(heuristic);
        heuristic.values.put(outlook, 0.3);
        learner.setTrainingDataSize(instList.size() * 2); // just double the size, so we can check rank was applied correctly
        learner.train(dt, null, null, instDistr, attrDomains, 0);
        System.out.println(dt.getRoot());

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


//
//        LeafNode leafNode = (LeafNode) dt.getRoot();
//        assertEquals( "yes", leafNode.getCategory() );
//        assertEquals( 14, leafNode.getNumMatch(), 0d );
//        assertEquals( 9, leafNode.getNumClassification(), 0d );
//        assertEquals( 0.321, leafNode.getRank(), 0.001 );
//        assertEquals( 5, leafNode.getMissClassified(), 0d );
//
//        instList = Weather.getDataMoreNo();
//        instDistr = new InstDistribution(play, instList.getInstances());
//        learner.train(dt, null, null, instDistr, attrDomains, 0);
//
//        leafNode = (LeafNode) dt.getRoot();
//        assertEquals( "no", leafNode.getCategory() );
//        assertEquals( 14, leafNode.getNumMatch(), 0d );
//        assertEquals( 8, leafNode.getNumClassification(), 0d );
//        assertEquals( 0.286, leafNode.getRank(), 0.001 );
//        assertEquals( 6, leafNode.getMissClassified(), 0d );
    }
}
