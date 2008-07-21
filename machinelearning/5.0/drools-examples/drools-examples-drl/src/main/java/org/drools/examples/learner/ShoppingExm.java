package org.drools.examples.learner;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;
import org.drools.compiler.PackageBuilder;
import org.drools.examples.learner.ShoppingClasses.*;

import org.drools.learner.DecisionTree;
import org.drools.learner.builder.DecisionTreeFactory;

public class ShoppingExm {

	public static final void main(final String[] args) throws Exception {
		// my rule base 
		final RuleBase ruleBase = RuleBaseFactory.newRuleBase();
		//ruleBase.addPackage( pkg );

		final StatefulSession session = ruleBase.newStatefulSession();
		// LearningSession

		// what are these listeners???
//		session.addEventListener( new DebugAgendaEventListener() );
//		session.addEventListener( new DebugWorkingMemoryEventListener() );

//		final WorkingMemoryFileLogger logger = new WorkingMemoryFileLogger( session );
//		logger.setFileName( "log/restaurants" );        

		Object[] facts = getExamples1();
		
//		int i = 0;
		Class<?> obj_class = Purchase.class;
		for (Object r : facts) {
//			if (i ==0)
//				obj_class= r.getClass();
			session.insert(r);
//			i ++;
		}

		// instantiate a learner for a specific object class and pass session to train
		DecisionTree decision_tree; int ALGO = 1;
		switch (ALGO) {
		case 1: 
			decision_tree  = DecisionTreeFactory.createSingleC45E(session, obj_class);
			break;
		case 2:
			decision_tree  = DecisionTreeFactory.createBagC45E(session, obj_class);
			break;
		case 3: 
			decision_tree  = DecisionTreeFactory.createBoostedC45G(session, obj_class);
			break;
		default:
			decision_tree  = DecisionTreeFactory.createBagC45G(session, obj_class);
		
		}

		boolean BUILD_TREE = true;
		if (BUILD_TREE) {
			final PackageBuilder builder = new PackageBuilder();
			//this wil generate the rules, then parse and compile in one step
			builder.addPackageFromTree( decision_tree );
			ruleBase.addPackage( builder.getPackage() );
			session.fireAllRules();
		}

//		logger.writeToDisk();

		session.dispose();
	}
	
	public static Object[] getExamples()
	{
		 Customer mark = new Customer( "mark", 0 );
		 mark.setEligible(false);
		 Product shoes = new Product( "shoes", 60 );
		 Product hat = new Product( "hat", 60 );
		 Purchase purchase_x = new Purchase( mark, shoes );
		 Purchase purchase_y = new Purchase( mark, hat );
		 Object[] facts = {
				 mark, 
				 shoes,
				 hat,
				 purchase_x,
				 purchase_y};
		 return facts;
	}
	
	public static Object[] getExamples1()
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
				 mark, 
				 shoes,
				 hat,
				 purchase_x,
				 purchase_y,
				 new Purchase( gizil, skirt ),
				 new Purchase( gizil, robe ),
				 new Purchase( daniel, shoes ),
				 new Purchase( daniel, hat2 ),
				 new Purchase( krem, hat2 )};
		 return facts;
	}
}
