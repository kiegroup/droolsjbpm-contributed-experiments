package examples;

import id3.DecisionTree;
import id3.DecisionTreeBuilder;
import id3.RulePrinter;
import id3.WorkingMemory;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.compiler.PackageBuilder;
import org.drools.rule.Package;

public class RestaurantExample {
	public static final void main(final String[] args) throws Exception {

		//int action = 1; /* create the drl */
		int action = 2; /* parse the drl */

		String drlFile = new String("restaurants"+".drl");
		switch (action) {
		case 1:
			produceRestaurants(drlFile);
			
			break;
		case 2:

			//read in the source
			final Reader source = new InputStreamReader(Restaurant.class
					.getResourceAsStream(drlFile));

			final PackageBuilder builder = new PackageBuilder();

			//this will parse and compile in one step
			builder.addPackageFromDrl(source);

			// Check the builder for errors
			if (builder.hasErrors()) {
				System.out.println(builder.getErrors().toString());
				throw new RuntimeException(
						"Unable to compile \""+drlFile+"\".");
			}
			//get the compiled package (which is serializable)
			final Package pkg = builder.getPackage();

			//add the package to a rulebase (deploy the rule package).
			final RuleBase ruleBase = RuleBaseFactory.newRuleBase();
			ruleBase.addPackage(pkg);
			break;
			

			

		}

	}

	public static void produceRestaurants(String file) {

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

		WorkingMemory simple = new WorkingMemory();

		for (Object r : facts) {
			try {
				simple.insert(r);

			} catch (Exception e) {
				System.out.println("Inserting element " + r + " and " + e);
			}
		}

		DecisionTreeBuilder bocuk = new DecisionTreeBuilder();

		long dt = System.currentTimeMillis();
		DecisionTree bocuksTree = bocuk.build(simple, k, "will_wait", null);
		dt = System.currentTimeMillis() - dt;
		System.out.println("Time" + dt + "\n" + bocuksTree);

		RulePrinter my_printer = new RulePrinter();
		my_printer.printer(bocuksTree, "examples", "src/rules/examples/"+file);
	}
}
