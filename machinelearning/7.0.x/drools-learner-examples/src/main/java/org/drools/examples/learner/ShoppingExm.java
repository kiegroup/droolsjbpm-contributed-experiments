package org.drools.examples.learner;

import java.util.Arrays;
import java.util.List;

import org.drools.examples.learner.ShoppingClasses.Customer;
import org.drools.examples.learner.ShoppingClasses.Product;
import org.drools.examples.learner.ShoppingClasses.Purchase;
import org.drools.learner.DecisionTree;
import org.drools.learner.builder.DecisionTreeFactory;
import org.drools.learner.tools.RulePrinter;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;

public class ShoppingExm {

    public static final void main(final String[] args) throws Exception {
        // my rule base
//		final RuleBase ruleBase = RuleBaseFactory.newRuleBase();
//		//ruleBase.addPackage( pkg );
//
//		final StatefulSession session = ruleBase.newStatefulSession();
//		// LearningSession
//
//		// what are these listeners???
////		session.addEventListener( new DebugAgendaEventListener() );
////		session.addEventListener( new DebugWorkingMemoryEventListener() );
//
////		final WorkingMemoryFileLogger logger = new WorkingMemoryFileLogger( session );
////		logger.setFileName( "log/restaurants" );
//
//		Object[] facts = getExamples1();
//
////		int i = 0;
//		Class<?> obj_class = Purchase.class;
//		for (Object r : facts) {
////			if (i ==0)
////				obj_class= r.getClass();
//			session.insert(r);
////			i ++;
//		}

        Class<?>     obj_class = Purchase.class;
        List<Object> objects   = Arrays.asList(getExamples1());

        // instantiate a learner for a specific object class and pass session to train
        DecisionTree decision_tree;
        int          ALGO = 1;
        switch (ALGO) {
            case 1:
                decision_tree = DecisionTreeFactory.createSingleC45E(objects, obj_class);
                break;
            case 2:
                decision_tree = DecisionTreeFactory.createBagC45E(objects, obj_class);
                break;

            default:
                decision_tree = DecisionTreeFactory.createBagC45G(objects, obj_class);
        }

        String     drl      = RulePrinter.readRules(decision_tree);
        KieSession ksession = new KieHelper().addContent(drl, ResourceType.DRL).build().newKieSession();
        ksession.fireAllRules();

//		boolean BUILD_TREE = true;
//		if (BUILD_TREE) {
//			final PackageBuilder builder = new PackageBuilder();
//			//this wil generate the rules, then parse and compile in one step
//			builder.addPackageFromTree( decision_tree );
//			ruleBase.addPackage( builder.getPackage() );
//			session.fireAllRules();
//		}
//
////		logger.writeToDisk();
//
//		session.dispose();
    }

    public static Object[] getExamples() {
        Customer mark = new Customer("mark", 0);
        mark.setEligible(false);
        Product  shoes      = new Product("shoes", 60);
        Product  hat        = new Product("hat", 60);
        Purchase purchase_x = new Purchase(mark, shoes);
        Purchase purchase_y = new Purchase(mark, hat);
        Object[] facts = {
                mark,
                shoes,
                hat,
                purchase_x,
                purchase_y};
        return facts;
    }

    public static Object[] getExamples1() {
        Customer mark = new Customer("mark", 0);
        mark.setEligible(true);
        Product  shoes      = new Product("shoes", 60);
        Product  hat        = new Product("hat", 60);
        Purchase purchase_x = new Purchase(mark, shoes);
        Purchase purchase_y = new Purchase(mark, hat);

        Customer gizil = new Customer("gizil", 10);
        gizil.setEligible(true);
        Customer daniel = new Customer("daniel", 10);
        daniel.setEligible(true);
        Customer krem = new Customer("krem", 10);
        krem.setEligible(true);

        Product skirt = new Product("skirt", 60);
        Product robe  = new Product("robe", 60);
        Product hat2  = new Product("hat2", 60);

        Customer pervin = new Customer("pervin", 20);
        pervin.setEligible(false);

        Customer ertug = new Customer("ertug", 30);
        ertug.setEligible(false);

        Object[] facts = {
                mark,
                shoes,
                hat,
                purchase_x,
                purchase_y,
                new Purchase(gizil, skirt),
                new Purchase(gizil, robe),
                new Purchase(daniel, shoes),
                new Purchase(daniel, hat2),
                new Purchase(pervin, hat),
                new Purchase(pervin, hat2),
                new Purchase(pervin, shoes),
                new Purchase(ertug, skirt),
                new Purchase(ertug, hat),
                new Purchase(krem, hat2)};
        return facts;
    }
}
