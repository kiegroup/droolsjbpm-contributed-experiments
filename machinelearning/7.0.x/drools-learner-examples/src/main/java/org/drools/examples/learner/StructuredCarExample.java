package org.drools.examples.learner;

import java.util.List;

import org.drools.examples.learner.structured_car.Car;
import org.drools.learner.DecisionTree;
import org.drools.learner.builder.DecisionTreeFactory;
import org.drools.learner.tools.ObjectFactory;
import org.drools.learner.tools.RulePrinter;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;

public class StructuredCarExample {

    public static final void main(final String[] args) throws Exception {
        // my rule base
//		final RuleBase ruleBase = RuleBaseFactory.newRuleBase();
//
//		final StatefulSession session = ruleBase.newStatefulSession();	// LearningSession
//
//		//session.addEventListener( new DebugAgendaEventListener() );
//		//session.addEventListener( new DebugWorkingMemoryEventListener() );
//
//		//final WorkingMemoryFileLogger logger = new WorkingMemoryFileLogger( session );
//		//logger.setFileName( "log/car" );
//
//		String inputFile = new String("data/car/car.data.txt");
//		Class<?> obj_class = Car.class;
//		List<Object> facts = ObjectFactory.insertStructuredObjects(obj_class, session, inputFile);
//		for (Object r : facts) {
//			session.insert(r);
//		}

        String       inputFile = new String("data/car/car.data.txt");
        Class<?>     obj_class = Car.class;
        List<Object> objects   = ObjectFactory.getStructuredObjects(obj_class, inputFile);

        // instantiate a learner for a specific object class and pass session to train
        DecisionTree decision_tree = null;
        int          ALGO = 121;
        /*
		 * Single	1xx, Bag 	2xx, Boost 3xx
		 * ID3 		x1x, C45 	x2x
		 * Entropy	xx1, Gain	xx2
		 */
        switch (ALGO) {
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
        }

        String     drl      = RulePrinter.readRules(decision_tree);
        KieSession ksession = new KieHelper().addContent(drl, ResourceType.DRL).build().newKieSession();
        ksession.fireAllRules();

//		final PackageBuilder builder = new PackageBuilder();
//		//this wil generate the rules, then parse and compile in one step
//		builder.addPackageFromTree( decision_tree );
//		/*
//		 * get the compiled package (which is serializable) from the builder
//		 * add the package to a rulebase (deploy the rule package).
//		 */
////		ruleBase.addPackage( builder.getPackage() );
////
////		session.fireAllRules();
////
////		//session.fireAllRules();
////		ReteStatistics stats = new ReteStatistics(ruleBase);
////        stats.calculateNumberOfNodes();
////        stats.print(Util.DRL_DIRECTORY + decision_tree.getSignature());
//		//logger.writeToDisk();
//
//		session.dispose();
    }
}
