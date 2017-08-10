package org.drools.examples.learner;

import java.util.ArrayList;
import java.util.List;

import org.drools.learner.DecisionTree;
import org.drools.learner.builder.DecisionTreeFactory;
import org.drools.learner.tools.RulePrinter;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;

// Hello World for Learner
public class RestaurantExample {

    public static final void main(final String[] args) throws Exception {
        // my rule base
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
//		final WorkingMemoryFileLogger logger = new WorkingMemoryFileLogger( session );
//		logger.setFileName( "log/restaurants" );
//
        Class<?>     obj_class = Restaurant.class;
        List<Object> objects   = getRestaurants();
//		for (Object r : facts) {
//			session.insert(r);
//		}

        // instantiate a learner for a specific object class and pass session to train
        DecisionTree decision_tree;
        int          ALGO = 111;
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
//		ruleBase.addPackage( builder.getPackage() );
//		/*
//			final Reader source = new InputStreamReader( HelloWorldExample.class.getResourceAsStream( "HelloWorld.drl" ) );
//			//get the compiled package (which is serializable)
//		    final Package pkg = builder.getPackage();
//		    //add the package to a rulebase (deploy the rule package).
//		    ruleBase.addPackage( pkg );
//		 */
//
//		session.fireAllRules();
//
//        ReteStatistics stats = new ReteStatistics(ruleBase);
//        stats.calculateNumberOfNodes();
//        stats.print(Util.DRL_DIRECTORY + decision_tree.getSignature());
//		logger.writeToDisk();
//
//		session.dispose();

        String     drl      = RulePrinter.readRules(decision_tree);
        KieSession ksession = new KieHelper().addContent(drl, ResourceType.DRL).build().newKieSession();
        ksession.fireAllRules();
        ksession.dispose();
    }

    public static List<Object> getRestaurants() {
        ArrayList<Object> rests = new ArrayList<Object>();
        rests.add(new Restaurant(true, false, false, true, "Full", 1, false,
                                 false, "Thai", "30-60", false));
        rests.add(new Restaurant(false, true, false, false, "Some", 1, false,
                                 false, "Burger", "0-10", true));
        rests.add(new Restaurant(true, false, true, true, "Full", 1, true,
                                 false, "Thai", "10-30", true));
        rests.add(new Restaurant(true, false, true, false, "Full", 3, false,
                                 true, "French", ">60", false));
        rests.add(new Restaurant(false, true, false, true, "Some", 2, true,
                                 true, "Italian", "0-10", true));
        rests.add(new Restaurant(false, true, false, false, "None", 1, true,
                                 false, "Burger", "0-10", false));
        rests.add(new Restaurant(false, false, false, true, "Some", 2, true,
                                 true, "Thai", "0-10", true));
        rests.add(new Restaurant(false, true, true, false, "Full", 1, true,
                                 false, "Burger", ">60", false));
        rests.add(new Restaurant(true, true, true, true, "Full", 3, false,
                                 true, "Italian", "10-30", false));
        rests.add(new Restaurant(false, false, false, false, "None", 1, false,
                                 false, "Thai", "0-10", false));
        rests.add(new Restaurant(true, true, true, true, "Full", 1, false,
                                 false, "Burger", "30-60", true));

        return rests;
    }

    public static List<Object> getVVs() {
        ArrayList<Object> rests = new ArrayList<Object>();
        rests.add(new VV(true, false, false, true, "Full", 1, false,
                         false, "Thai", "30-60", false));
//		rests.add(new Restaurant(false, true, false, false, "Some", 1, false,
//				false, "Burger", "0-10", true));
//		rests.add(new Restaurant(true, false, true, true, "Full", 1, true,
//				false, "Thai", "10-30", true));
//		rests.add(new Restaurant(true, false, true, false, "Full", 3, false,
//				true, "French", ">60", false));
//		rests.add(new Restaurant(false, true, false, true, "Some", 2, true,
//				true, "Italian", "0-10", true));
//		rests.add(new Restaurant(false, true, false, false, "None", 1, true,
//				false, "Burger", "0-10", false));
//		rests.add(new Restaurant(false, false, false, true, "Some", 2, true,
//				true, "Thai", "0-10", true));
//		rests.add(new Restaurant(false, true, true, false, "Full", 1, true,
//				false, "Burger", ">60", false));
//		rests.add(new Restaurant(true, true, true, true, "Full", 3, false,
//				true, "Italian", "10-30", false));
//		rests.add(new Restaurant(false, false, false, false, "None", 1, false,
//				false, "Thai", "0-10", false));
//		rests.add(new Restaurant(true, true, true, true, "Full", 1, false,
//				false, "Burger", "30-60", true));

        return rests;
    }
}
