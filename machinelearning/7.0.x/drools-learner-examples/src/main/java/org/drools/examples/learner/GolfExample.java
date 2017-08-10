package org.drools.examples.learner;

import java.util.List;

import org.drools.learner.DecisionTree;
import org.drools.learner.builder.DecisionTreeFactory;
import org.drools.learner.tools.ObjectFactory;
import org.drools.learner.tools.RulePrinter;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;

public class GolfExample {

    public static final void main(final String[] args) throws Exception {
        long start_time = System.currentTimeMillis();
//		// my rule base
//		final RuleBase ruleBase = RuleBaseFactory.newRuleBase();
//		//ruleBase.addPackage( pkg );
//
//		final StatefulSession session = ruleBase.newStatefulSession();
//		// LearningSession
//
//		// what are these listeners???
//		session.addEventListener( new DebugAgendaEventListener() );
//		session.addEventListener( new DebugWorkingMemoryEventListener() );
//
//
//
//		final WorkingMemoryFileLogger logger = new WorkingMemoryFileLogger( session );
//		logger.setFileName( "log/golf_c45" );
//
//		String inputFile = new String("data/golf/golf.data.txt");
//		Class<?> obj_class = Golf.class;
//		List<Object> facts = ObjectFactory.getObjects(obj_class, inputFile);
//		for (Object r : facts) {
//			session.insert(r);
//		}

        String       inputFile = new String("data/golf/golf.data.txt");
        Class<?>     obj_class = Golf.class;
        List<Object> objects   = ObjectFactory.getObjects(obj_class, inputFile);

        DecisionTree decision_tree;
        int          ALGO = 121;
        /*
		 * Single	1xx, Bag 	2xx, Boost 3xx
		 * ID3 		x1x, C45 	x2x
		 * Entropy	xx1, Gain	xx2
		 */
        switch (ALGO) {
            case 111:
                decision_tree = DecisionTreeFactory.createSingleID3E(objects, obj_class);
                break;
            case 112:
                decision_tree = DecisionTreeFactory.createSingleID3G(objects, obj_class);
                break;
            case 121:
                decision_tree = DecisionTreeFactory.createSingleC45E(objects, obj_class);
                break;
            case 122:
                decision_tree = DecisionTreeFactory.createSingleC45G(objects, obj_class);
                break;
            case 221:
                decision_tree = DecisionTreeFactory.createBagC45E(objects, obj_class);
                break;
            case 222:
                decision_tree = DecisionTreeFactory.createBagC45G(objects, obj_class);
                break;

            default:
                decision_tree = DecisionTreeFactory.createSingleID3E(objects, obj_class);
        }

//		final PackageBuilder builder = new PackageBuilder();
//		//this wil generate the rules, then parse and compile in one step
//		builder.addPackageFromTree( decision_tree );
//		/*
//		 * get the compiled package (which is serializable) from the builder
//		 * add the package to a rulebase (deploy the rule package).
//		 */
//		ruleBase.addPackage( builder.getPackage() );
//
//		session.fireAllRules();
//
//		long end_time = System.currentTimeMillis();
//		System.out.println("Total time="+ (end_time-start_time));
//
//		ReteStatistics stats = new ReteStatistics(ruleBase);
//	    stats.calculateNumberOfNodes();
//	    stats.print(Util.DRL_DIRECTORY + decision_tree.getSignature());
//		logger.writeToDisk();
//
//		session.dispose();

        String     drl      = RulePrinter.readRules(decision_tree);
        KieSession ksession = new KieHelper().addContent(drl, ResourceType.DRL).build().newKieSession();
        ksession.fireAllRules();
    }
}
