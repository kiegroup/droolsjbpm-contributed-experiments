package org.drools.examples.learner;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;
import org.drools.audit.WorkingMemoryFileLogger;
import org.drools.compiler.PackageBuilder;
import org.drools.event.DebugAgendaEventListener;
import org.drools.event.DebugWorkingMemoryEventListener;
import org.drools.learner.tools.ObjectFactory;
import org.drools.rule.Package;

public class CarExampleFromDrl {
	
	public static final void main(final String[] args) throws Exception {
        //read in the source
        //final Reader source = new InputStreamReader( HelloWorldExample.class.getResourceAsStream( "HelloWorld.drl" ) );
    	final Reader source = new InputStreamReader( Car.class.getResourceAsStream( "car_c45_boost2.drl" ) );

        final PackageBuilder builder = new PackageBuilder();

        //this wil parse and compile in one step
        builder.addPackageFromDrl( source );
        
        // Check the builder for errors
        if ( builder.hasErrors() ) {
            System.out.println( builder.getErrors().toString() );
            throw new RuntimeException( "Unable to compile \"car_c45_boost2.drl\".");
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
        logger.setFileName( "log/car_c45_boost2" );        

        String inputFile = new String("data/car/car.data.txt");
		Class<?> obj_class = Car.class;
		List<Object> facts = ObjectFactory.getObjects(obj_class, inputFile);
		for (Object r : facts) {
			session.insert(r);
		}
        
        session.fireAllRules();
        
       logger.writeToDisk();
        
        session.dispose();
    }

}
