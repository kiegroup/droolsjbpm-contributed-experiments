package examples;


import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;
import org.drools.audit.WorkingMemoryFileLogger;
import org.drools.compiler.PackageBuilder;
import org.drools.event.DebugAgendaEventListener;
import org.drools.event.DebugWorkingMemoryEventListener;
import org.drools.rule.Package;

import dt.DecisionTree;
import dt.builder.IDTreeBuilder;
import dt.memory.WorkingMemory;
import dt.tools.RulePrinter;
import dt.tools.Util;

public class RestaurantExample {
	public static final void main(final String[] args) throws Exception {

		String drlFile = new String("restaurants" + ".drl");
		WorkingMemory simple = new WorkingMemory();
		/* create the drl */
		List<Object> my_objects = produceRestaurants(simple, drlFile);

		/* parse the drl */
		// read in the source
		final Reader source = new InputStreamReader(Restaurant.class
				.getResourceAsStream(drlFile));

		final PackageBuilder builder = new PackageBuilder();

		// this will parse and compile in one step
		builder.addPackageFromDrl(source);

		// Check the builder for errors
		if (builder.hasErrors()) {
			System.out.println(builder.getErrors().toString());
			throw new RuntimeException("Unable to compile \"" + drlFile + "\".");
		}
		// get the compiled package (which is serializable)
		final Package pkg = builder.getPackage();

		// add the package to a rulebase (deploy the rule package).
		final RuleBase ruleBase = RuleBaseFactory.newRuleBase();
		ruleBase.addPackage(pkg);

		boolean load_to_drools = false;
		if (load_to_drools) {
			/* feeding the object to Drools working memory */
			final StatefulSession session = ruleBase.newStatefulSession();
			session.addEventListener(new DebugAgendaEventListener());
			session.addEventListener(new DebugWorkingMemoryEventListener());

			final WorkingMemoryFileLogger logger = new WorkingMemoryFileLogger(
					session);
			logger.setFileName("log/restaurants");

			Iterator<Object> it_obj = my_objects.iterator();
			while (it_obj.hasNext()) {
				Object obj = it_obj.next();

				System.out.println("Object " + obj);

				try {
					session.insert(obj);

				} catch (Exception e) {
					System.out
							.println("Inserting element " + obj + " and " + e);
				}
			}

			session.fireAllRules();

			Iterator<Object> my_it = session.iterateObjects();

			while (my_it.hasNext()) {
				Object o = my_it.next();
				System.out.println("Object " + o);
			}
			logger.writeToDisk();

			session.dispose();
		}

	}

	public static List<Object> produceRestaurants(WorkingMemory wm, String file) {

		Restaurant arest = new Restaurant(true, false, false, true, "Full", 1,
				false, false, "Thai", "30-60", false);
		Class<?> k = arest.getClass();
		ArrayList<Object> facts = new ArrayList<Object>();
		facts.add(new Restaurant(true, false, false, true, "Full", 1, false,
				false, "Thai", "30-60", false));
		facts.add(new Restaurant(false, true, false, false, "Some", 1, false,
				false, "Burger", "0-10", true));
		facts.add(new Restaurant(true, false, true, true, "Full", 1, true,
				false, "Thai", "10-30", true));
		facts.add(new Restaurant(true, false, true, false, "Full", 3, false,
				true, "French", ">60", false));
		facts.add(new Restaurant(false, true, false, true, "Some", 2, true,
				true, "Italian", "0-10", true));
		facts.add(new Restaurant(false, true, false, false, "None", 1, true,
				false, "Burger", "0-10", false));
		facts.add(new Restaurant(false, false, false, true, "Some", 2, true,
				true, "Thai", "0-10", true));
		facts.add(new Restaurant(false, true, true, false, "Full", 1, true,
				false, "Burger", ">60", false));
		facts.add(new Restaurant(true, true, true, true, "Full", 3, false,
				true, "Italian", "10-30", false));
		facts.add(new Restaurant(false, false, false, false, "None", 1, false,
				false, "Thai", "0-10", false));
		facts.add(new Restaurant(true, true, true, true, "Full", 1, false,
				false, "Burger", "30-60", true));

		for (Object r : facts) {
			try {
				wm.insert(r);

			} catch (Exception e) {
				System.out.println("Inserting element " + r + " and " + e);
			}
		}

		IDTreeBuilder bocuk = new IDTreeBuilder();

		long dt = System.currentTimeMillis();
		String target_attr = Util.getTargetAnnotation(arest.getClass());
		DecisionTree bocuksTree = bocuk.build(wm, k, target_attr, null);
		dt = System.currentTimeMillis() - dt;
		System.out.println("Time" + dt + "\n" + bocuksTree);

		RulePrinter my_printer = new RulePrinter();
		my_printer
				.printer(bocuksTree, "examples", "src/rules/examples/" + file);

		return facts;
	}
}
