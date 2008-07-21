package org.drools.examples.learner;

import java.io.InputStreamReader;
import java.io.Reader;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;
import org.drools.audit.WorkingMemoryFileLogger;
import org.drools.compiler.PackageBuilder;
import org.drools.event.DebugAgendaEventListener;
import org.drools.event.DebugWorkingMemoryEventListener;
import org.drools.examples.ShoppingExample.Purchase;
import org.drools.examples.ShoppingExample.Customer;
import org.drools.rule.Package;

public class ShoppingExmFromDrl {
	
	public static final void main(final String[] args) throws Exception {
        //read in the source
        //final Reader source = new InputStreamReader( HelloWorldExample.class.getResourceAsStream( "HelloWorld.drl" ) );
    	final Reader source = new InputStreamReader( Purchase.class.getResourceAsStream( "purchase_c45_one.drl" ) );

        final PackageBuilder builder = new PackageBuilder();

        //this wil parse and compile in one step
        builder.addPackageFromDrl( source );
        
        // Check the builder for errors
        if ( builder.hasErrors() ) {
            System.out.println( builder.getErrors().toString() );
            throw new RuntimeException( "Unable to compile \"purchase_c45_one.drl\".");
        }

        //get the compiled package (which is serializable)
        final Package pkg = builder.getPackage();

        //add the package to a rulebase (deploy the rule package).
        final RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        ruleBase.addPackage( pkg );

        final StatefulSession session = ruleBase.newStatefulSession();
        
        session.addEventListener( new DebugAgendaEventListener() );
        session.addEventListener( new DebugWorkingMemoryEventListener() );
        
        final WorkingMemoryFileLogger logger = new WorkingMemoryFileLogger( session );
        logger.setFileName( "log/purchase_c45_one_fromdrl" );        

		Object[] facts = ShoppingExm.getExamples1();
		
//		int i = 0;
		Class<?> obj_class = Purchase.class;
		for (Object r : facts) {
//			if (i ==0)
//				obj_class= r.getClass();
			session.insert(r);
//			i ++;
		}
        
        session.fireAllRules();
        
        logger.writeToDisk();
        
        session.dispose();
    }

}
