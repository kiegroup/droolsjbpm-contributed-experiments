package org.drools.examples.learner;

import java.util.List;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;
import org.drools.audit.WorkingMemoryFileLogger;
import org.drools.compiler.PackageBuilder;
import org.drools.event.DebugAgendaEventListener;
import org.drools.event.DebugWorkingMemoryEventListener;
import org.drools.learner.DecisionTree;
import org.drools.learner.builder.DecisionTreeFactory;
import org.drools.learner.tools.ObjectFactory;

public class GolfExample {
	
	public static final void main(final String[] args) throws Exception {
		long start_time = System.currentTimeMillis();
		// my rule base 
		final RuleBase ruleBase = RuleBaseFactory.newRuleBase();
		//ruleBase.addPackage( pkg );

		final StatefulSession session = ruleBase.newStatefulSession();
		// LearningSession

		// what are these listeners???
		session.addEventListener( new DebugAgendaEventListener() );
		session.addEventListener( new DebugWorkingMemoryEventListener() );
		
		

		final WorkingMemoryFileLogger logger = new WorkingMemoryFileLogger( session );
		logger.setFileName( "log/golf_c45" );   
		
		String inputFile = new String("data/golf/golf.data.txt");
		Class<?> obj_class = Golf.class;
		List<Object> facts = ObjectFactory.getObjects(obj_class, inputFile);
		for (Object r : facts) {
			session.insert(r);
		}

		// instantiate a learner for a specific object class and pass session to train
		DecisionTree decision_tree; int ALGO = 7;
		switch (ALGO) {
		case 1:
			decision_tree  = DecisionTreeFactory.createBaggedC45(session, obj_class);
			break;
		case 2:
			decision_tree  = DecisionTreeFactory.createBoostedC45(session, obj_class);
			break;
		case 3:
			decision_tree  = DecisionTreeFactory.createGlobal2(session, obj_class);
				break;
		case 4:
			decision_tree  = DecisionTreeFactory.createBagC45Entropy(session, obj_class);
				break;		
		case 5:
			decision_tree  = DecisionTreeFactory.createBagC45GainRatio(session, obj_class);
				break;	
		case 6:
			decision_tree  = DecisionTreeFactory.createSingleID3(session, obj_class);
				break;
		case 7: 
			decision_tree  = DecisionTreeFactory.createSingleC45Entropy(session, obj_class);
			break;
		case 8: 
			decision_tree  = DecisionTreeFactory.createSingleC45GainRatio(session, obj_class);
			break;
		default:
			decision_tree  = DecisionTreeFactory.createSingleC45(session, obj_class);
		
		}
		
		final PackageBuilder builder = new PackageBuilder();
		//this wil generate the rules, then parse and compile in one step
		builder.addPackageFromTree( decision_tree );
		/* 
		 * get the compiled package (which is serializable) from the builder
		 * add the package to a rulebase (deploy the rule package). 
		 */
		ruleBase.addPackage( builder.getPackage() );

		session.fireAllRules();
		
		long end_time = System.currentTimeMillis();
		System.out.println("Total time="+ (end_time-start_time));
		
		logger.writeToDisk();

		session.dispose();
	}

}
