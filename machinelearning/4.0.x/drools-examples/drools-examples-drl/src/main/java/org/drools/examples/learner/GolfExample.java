package org.drools.examples.learner;

import java.util.List;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;
import org.drools.audit.WorkingMemoryFileLogger;
import org.drools.compiler.PackageBuilder;
import org.drools.event.DebugAgendaEventListener;
import org.drools.event.DebugWorkingMemoryEventListener;
import org.drools.learner.builder.Learner;
import org.drools.learner.builder.LearnerFactory;
import org.drools.learner.tools.ObjectFactory;

public class GolfExample {
	
	public static final void main(final String[] args) throws Exception {
		// my rule base 
		final RuleBase ruleBase = RuleBaseFactory.newRuleBase();
		//ruleBase.addPackage( pkg );

		final StatefulSession session = ruleBase.newStatefulSession();
		// LearningSession

		// what are these listeners???
		session.addEventListener( new DebugAgendaEventListener() );
		session.addEventListener( new DebugWorkingMemoryEventListener() );

		final WorkingMemoryFileLogger logger = new WorkingMemoryFileLogger( session );
		logger.setFileName( "log/golf" );   
		
		String inputFile = new String("data/golf/golf.data.txt");
		Class<?> obj_class = Golf.class;
		List<Object> facts = ObjectFactory.getObjects(obj_class, inputFile);
		for (Object r : facts) {
			session.insert(r);
		}

		// instantiate a learner for a specific object class and pass session to train
		Learner learner = LearnerFactory.createID3(session, obj_class);

		final PackageBuilder builder = new PackageBuilder();
		//this wil generate the rules, then parse and compile in one step
		builder.addPackageFromLearner( learner );
		ruleBase.addPackage( builder.getPackage() );
		/* 
			final Reader source = new InputStreamReader( HelloWorldExample.class.getResourceAsStream( "HelloWorld.drl" ) );
			//get the compiled package (which is serializable)
		    final Package pkg = builder.getPackage();
		    //add the package to a rulebase (deploy the rule package).
		    ruleBase.addPackage( pkg );
		 */

		session.fireAllRules();

		logger.writeToDisk();

		session.dispose();
	}

}
