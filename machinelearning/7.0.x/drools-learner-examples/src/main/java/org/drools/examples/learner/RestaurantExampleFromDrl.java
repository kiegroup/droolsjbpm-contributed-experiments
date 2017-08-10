package org.drools.examples.learner;

import java.util.List;

import org.drools.learner.tools.ObjectFactory;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;

public class RestaurantExampleFromDrl {

    public static final void main(final String[] args) throws Exception {
//        //read in the source
//        //final Reader source = new InputStreamReader( HelloWorldExample.class.getResourceAsStream( "HelloWorld.drl" ) );
//    	final Reader source = new InputStreamReader( Restaurant.class.getResourceAsStream( "restaurant_id3_one.drl" ) );
//
//        final PackageBuilder builder = new PackageBuilder();
//
//        //this wil parse and compile in one step
//        builder.addPackageFromDrl( source );
//
//        // Check the builder for errors
//        if ( builder.hasErrors() ) {
//            System.out.println( builder.getErrors().toString() );
//            throw new RuntimeException( "Unable to compile \"HelloWorld.drl\".");
//        }
//
//        //get the compiled package (which is serializable)
//        final Package pkg = builder.getPackage();
//
//        //add the package to a rulebase (deploy the rule package).
//        final RuleBase ruleBase = RuleBaseFactory.newRuleBase();
//        ruleBase.addPackage( pkg );
//
//        final StatefulSession session = ruleBase.newStatefulSession();
//
//        session.addEventListener( new DebugAgendaEventListener() );
//        session.addEventListener( new DebugWorkingMemoryEventListener() );
//
//        final WorkingMemoryFileLogger logger = new WorkingMemoryFileLogger( session );
//        logger.setFileName( "log/restaurant_fromdrl" );
//
//		List<Object> facts = RestaurantExample.getRestaurants();
//		for (Object r : facts) {
//			session.insert(r);
//		}
//
//        session.fireAllRules();
//
//       logger.writeToDisk();
//
//        session.dispose();

        KieSession   ksession  = new KieHelper().addResource(ResourceFactory.newInputStreamResource(Restaurant.class.getResourceAsStream("restaurant_id3_one.drl"))).build().newKieSession();
        String       inputFile = new String("data/car/car.data.txt");
        Class<?>     obj_class = Restaurant.class;
        List<Object> facts     = ObjectFactory.getObjects(obj_class, inputFile);
        for (Object r : facts) {
            ksession.insert(r);
        }

        ksession.fireAllRules();

        //logger.writeToDisk();

        ksession.dispose();
    }
}
