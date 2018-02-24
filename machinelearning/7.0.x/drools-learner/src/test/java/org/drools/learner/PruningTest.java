package org.drools.learner;

import java.util.ArrayList;
import java.util.List;

import org.drools.learner.builder.C45Learner;
import org.drools.learner.builder.DecisionTreeBuilder;
import org.drools.learner.builder.Learner;
import org.drools.learner.builder.Solution;
import org.drools.learner.builder.Tester;
import org.drools.learner.eval.InstDistribution;
import org.drools.learner.tools.Util;
import org.drools.learner.DecisionTreePruner;
import org.drools.learner.DecisionTreePruner.TreeSequenceProc;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class PruningTest {

    protected static final transient Logger log = LoggerFactory.getLogger(PruningTest.class);

    public static InstanceList getDataTest1() {
        Schema schema  = Schema.createSchemaStructure(Weather.class, Learner.DataType.STRUCTURED);

        List<Weather> list = new ArrayList<Weather>();
        list.add(new Weather("sun", "hot", "high", "low", "yes"));
        list.add(new Weather("sun", "cold", "high", "high", "yes"));
        list.add(new Weather("overcast", "hot", "high", "low", "yes"));
        list.add(new Weather("overcast", "cold", "normal", "high", "yes"));
        list.add(new Weather("overcast", "cold", "normal", "high", "no"));
        list.add(new Weather("overcast", "cold", "normal", "high", "no"));
        list.add(new Weather("rain", "mild", "high", "low", "yes"));
        list.add(new Weather("rain", "cold", "normal", "low", "no"));
        InstanceList instList = new InstanceList(schema);
        list.stream().forEach(v->instList.addStructuredInstance(v));
        return instList;
    }

    @Test
    public void test0() {
        Domain play        = Weather.playDomain;
        Domain outlook     = Weather.outlookDomain;
        Domain temperature = Weather.temperatureDomain;
        Domain humidity = Weather.humidityDomain;

        InstanceList     instList  = getDataTest1();
        InstDistribution instDistr = new InstDistribution(play, instList.getInstances());

        DecisionTree dt          = new DecisionTree(instList.getSchema(), play.getFReferenceName());
        List<Domain> attrDomains = new ArrayList<>();
        attrDomains.add(outlook);
        attrDomains.add(temperature);
        attrDomains.add(humidity);

        MemoryImpl mem = new MemoryImpl(Weather.class, instList);
        mem.processTestSet(1);

        FixedHeuristic heuristic = new FixedHeuristic();
        C45Learner     learner   = new C45Learner(heuristic);
        heuristic.values.put(outlook, 0.3);
        heuristic.values.put(temperature, 0.2);
        heuristic.values.put(humidity, 0.1);
        learner.setTrainingDataSize(mem.getTrainSet().size());
        learner.train(dt, null, null, instDistr, attrDomains, 0);

        Solution sol = createSolution(dt, mem);

        TreeNode root = dt.getRoot();
        assertEquals("outlook", root.getDomain().getFName());
        assertEquals(3, root.getChildrenKeys().size());

        TreeNode sunNode      = root.getChild("sun");
        TreeNode overcastNode = root.getChild("overcast");
        TreeNode rainNode     = root.getChild("rain");
        assertNotNull(sunNode);
        assertNotNull(overcastNode);
        assertNotNull(rainNode);

        assertEquals(0, sunNode.getChildrenKeys().size());
        assertEquals(3, overcastNode.getChildrenKeys().size());
        assertEquals(3, rainNode.getChildrenKeys().size());


        TreeNode node     = overcastNode.getChild("cold");
        log.debug( "domain: " + node.getDomain() );
        assertEquals(3, node.getChildrenKeys().size());
    }

    @Test
    @Ignore // still working on this one (mdp)
    public void test1() {
        Domain play        = Weather.playDomain;
        Domain outlook     = Weather.outlookDomain;
        Domain temperature = Weather.temperatureDomain;
        Domain humidity = Weather.humidityDomain;

        InstanceList     instList  = getDataTest1();
        InstDistribution instDistr = new InstDistribution(play, instList.getInstances());

        DecisionTree dt          = new DecisionTree(instList.getSchema(), play.getFReferenceName());
        List<Domain> attrDomains = new ArrayList<>();
        attrDomains.add(outlook);
        attrDomains.add(temperature);
        attrDomains.add(humidity);

        MemoryImpl mem = new MemoryImpl(Weather.class, instList);
        mem.processTestSet(1);

        FixedHeuristic heuristic = new FixedHeuristic();
        C45Learner     learner   = new C45Learner(heuristic);
        heuristic.values.put(outlook, 0.3);
        heuristic.values.put(temperature, 0.2);
        heuristic.values.put(humidity, 0.1);
        learner.setTrainingDataSize(mem.getTrainSet().size());
        learner.train(dt, null, null, instDistr, attrDomains, 0);

        Solution sol = createSolution(dt, mem);
        DecisionTreePruner                  pruner  = new DecisionTreePruner();
        DecisionTreePruner.TreeSequenceProc treeSeq = pruner.createSearchTree(sol);

        TreeNode root = dt.getRoot();
        assertEquals( "outlook", root.getDomain().getFName() );
        assertEquals(3, root.getChildrenKeys().size());

        TreeNode sunNode = root.getChild("sun");
        TreeNode overcastNode = root.getChild("overcast");
        TreeNode rainNode = root.getChild("rain");
        assertNotNull(sunNode);
        assertNotNull(overcastNode);
        assertNotNull(rainNode);

        assertEquals( 0, sunNode.getChildrenKeys().size());
        assertEquals( 3, overcastNode.getChildrenKeys().size());
        assertEquals( 3, rainNode.getChildrenKeys().size());

        TreeNode node     = overcastNode.getChild("cold");
        log.debug( "domain: " + node.getDomain() );
        assertEquals(3, node.getChildrenKeys().size());
    }

    private Solution createSolution(DecisionTree dt, MemoryImpl mem) {
        Tester   t          = DecisionTreeBuilder.getTester(dt);
        Stats    trainStats = t.test(mem.getTrainSet());
        Stats    testStats  = t.test(mem.getTestSet());
        Solution best       = new Solution(dt, mem.getTrainSet());
        best.setTestList(mem.getTestSet());
        best.setTrainStats(trainStats);
        best.setTestStats(testStats);

        return best;
    }
}
