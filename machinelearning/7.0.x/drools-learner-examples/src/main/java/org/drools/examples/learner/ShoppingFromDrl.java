package org.drools.examples.learner;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import org.drools.examples.learner.ShoppingClasses.Customer;
import org.drools.examples.learner.ShoppingClasses.Purchase;
import org.drools.examples.learner.ShoppingClasses.Product;
import org.drools.learner.tools.ObjectFactory;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;

public class ShoppingFromDrl {
	
	public static final void main(final String[] args) throws Exception {
//        //read in the source
//        //final Reader source = new InputStreamReader( HelloWorldExample.class.getResourceAsStream( "HelloWorld.drl" ) );
//    	final Reader source = new InputStreamReader( Purchase.class.getResourceAsStream( "purchase_c45_one.drl" ) );
//
//        final PackageBuilder builder = new PackageBuilder();
//
//        //this wil parse and compile in one step
//        builder.addPackageFromDrl( source );
//
//        // Check the builder for errors
//        if ( builder.hasErrors() ) {
//            System.out.println( builder.getErrors().toString() );
//            throw new RuntimeException( "Unable to compile \"purchase_c45_one.drl\".");
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
//        logger.setFileName( "log/purchase_c45_one_fromdrl" );
//
//		Object[] facts = getExamples();
//
////		int i = 0;
//		Class<?> obj_class = Purchase.class;
//		for (Object r : facts) {
////			if (i ==0)
////				obj_class= r.getClass();
//			session.insert(r);
////			i ++;
//		}
//
//        session.fireAllRules();
//
//        logger.writeToDisk();
//
//        session.dispose();

		KieSession ksession = new KieHelper( ).addResource( ResourceFactory.newInputStreamResource( Car.class.getResourceAsStream( "purchase_c45_one.drl" ) ) ).build().newKieSession();
		Object[] facts = getExamples();
		for (Object r : facts) {
			ksession.insert(r);
		}

		ksession.fireAllRules();

		ksession.dispose();
    }
	
	public static Object[] getExamples()
	{
		 Customer mark = new Customer( "mark", 0 );
		 mark.setEligible(false);
		 Product shoes = new Product( "shoes", 60 );
		 Product hat = new Product( "hat", 60 );
		 Purchase purchase_x = new Purchase( mark, shoes );
		 Purchase purchase_y = new Purchase( mark, hat );
		 
		 Customer gizil = new Customer( "gizil", 10 );
		 mark.setEligible(true);
		 Customer daniel = new Customer( "daniel", 10 );
		 mark.setEligible(false);
		 Customer krem = new Customer( "krem", 10 );
		 mark.setEligible(true);
		 
		 Product skirt = new Product( "skirt", 60 );
		 Product robe = new Product( "robe", 60 );
		 Product hat2 = new Product( "hat2", 60 );
		 
		 Object[] facts = {
//				 mark, 
//				 shoes,
//				 hat,
				 purchase_x,
				 purchase_y,
//				 gizil,
//				 skirt,
//				 robe,
//				 daniel,
//				 krem,
//				 hat2,
				 new Purchase( gizil, skirt ),
				 new Purchase( gizil, robe ),
				 new Purchase( daniel, shoes ),
				 new Purchase( daniel, hat2 ),
				 new Purchase( krem, hat2 )};
		 
		 Object[] facts2 = {
				 mark, 
				 shoes,
				 hat,
				 purchase_x,
				 purchase_y,
				 gizil,
				 skirt,
				 robe,
				 daniel,
				 krem,
				 hat2,
				 new Purchase( gizil, skirt ),
				 new Purchase( gizil, robe ),
				 new Purchase( daniel, shoes ),
				 new Purchase( daniel, hat2 ),
				 new Purchase( krem, hat2 )};
		 return facts2;
	}

}
