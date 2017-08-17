package org.drools.examples.learner;

import java.util.List;

import org.drools.learner.DecisionTree;
import org.drools.learner.builder.DecisionTreeFactory;
import org.drools.learner.tools.ObjectFactory;
import org.drools.learner.tools.RulePrinter;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;

public class CarExample {

    public static final void main(final String[] args) throws Exception {
        // my rule base
//		final KieBase ruleBase = KnowledgeBaseFactory.newKnowledgeBase();

        //session.addEventListener( new DebugAgendaEventListener() );
        //session.addEventListener( new DebugWorkingMemoryEventListener() );

        //final WorkingMemoryFileLogger logger = new WorkingMemoryFileLogger( session );
        //logger.setFileName( "log/car" );

        String       inputFile = new String("data/car/car.data.txt");
        Class<?>     obj_class = Car.class;
        List<Object> objects   = ObjectFactory.getObjects(obj_class, inputFile);

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
            case 123:
                decision_tree = DecisionTreeFactory.createSingleC45EWorst(objects, obj_class);
                break;
            case 124:
                decision_tree = DecisionTreeFactory.createSingleC45Random(objects, obj_class);
                break;
            case 131:
                decision_tree = DecisionTreeFactory.createSingleC45EStop(objects, obj_class);
                break;
            case 132:
                decision_tree = DecisionTreeFactory.createSingleC45GStop(objects, obj_class);
                break;
            case 141:
                decision_tree = DecisionTreeFactory.createSingleC45EPrunStop(objects, obj_class);
                break;
            case 142:
                decision_tree = DecisionTreeFactory.createSingleC45GPrunStop(objects, obj_class);
                break;
            case 221:
                decision_tree = DecisionTreeFactory.createBagC45E(objects, obj_class);
                break;
            case 222:
                decision_tree = DecisionTreeFactory.createBagC45G(objects, obj_class);
                break;
            case 231:
                decision_tree = DecisionTreeFactory.createBagC45EStop(objects, obj_class);
                break;
            case 232:
                decision_tree = DecisionTreeFactory.createBagC45GStop(objects, obj_class);
                break;
            case 241:
                decision_tree = DecisionTreeFactory.createBagC45EPrunStop(objects, obj_class);
                break;
            case 242:
                decision_tree = DecisionTreeFactory.createBagC45GPrunStop(objects, obj_class);
                break;
            case 321:
                decision_tree = DecisionTreeFactory.createBoostC45E(objects, obj_class);
                break;
            case 322:
                decision_tree = DecisionTreeFactory.createBoostC45G(objects, obj_class);
                break;
            case 331:
                decision_tree = DecisionTreeFactory.createBoostC45EStop(objects, obj_class);
                break;
            case 332:
                decision_tree = DecisionTreeFactory.createBoostC45GStop(objects, obj_class);
                break;
            case 341:
                decision_tree = DecisionTreeFactory.createBoostC45EPrunStop(objects, obj_class);
                break;
            case 342:
                decision_tree = DecisionTreeFactory.createBoostC45GPrunStop(objects, obj_class);
                break;
        }

        String     drl      = RulePrinter.readRules(decision_tree);
        KieSession ksession = new KieHelper().addContent(drl, ResourceType.DRL).build().newKieSession();
        ksession.fireAllRules();

//        ksession.dispose();
//        KieBuilder kieBuilder = KieServices.get().newKieBuilder( kieHelper.kfs ).buildAll();
//        Results results = kieBuilder.getResults();


		/* 
			final Reader source = new InputStreamReader( HelloWorldExample.class.getResourceAsStream( "HelloWorld.drl" ) );
			//get the compiled package (which is serializable)
		    final Package pkg = builder.getPackage();
		    //add the package to a rulebase (deploy the rule package).
		    ruleBase.addPackage( pkg );
		 */

//        ksession.fireAllRules();
//        ReteStatistics stats = new ReteStatistics(ruleBase);
//        stats.calculateNumberOfNodes();
//        stats.print(Util.DRL_DIRECTORY +decision_tree.getSignature());
//        logger.writeToDisk();
//        ksession.dispose();
    }

//	public static class KieHelper2 extends KieHelper {
//
//    }
}
