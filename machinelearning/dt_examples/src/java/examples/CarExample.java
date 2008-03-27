package examples;

import id3.DecisionTree;
import id3.DecisionTreeBuilder;
import id3.FactSetFactory;
import id3.RulePrinter;
import id3.WorkingMemory;

import java.io.InputStreamReader;
import java.io.Reader;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.compiler.PackageBuilder;
import org.drools.rule.Package;

public class CarExample {
	
	
	public static final void main(final String[] args) throws Exception {

		int action = 1; /* create the drl */
		//int action = 2; /* parse the drl */

		String drlFile = new String("cars"+".drl");
		switch (action) {
		case 1:
			readCars(drlFile);
			
			break;
		case 2:

			//read in the source 
			// TODO give an exception of the file does not exist
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
	
	public static void readCars(String file) {
		
		/*
		 * | class values 
		 * 		unacc, acc, good, vgood
		 * | attributes
		 * 		buying: vhigh, high, med, low. 
		 * 		maint: vhigh, high, med, low. 
		 * 		doors: 2, 3, 4, 5, more. 
		 * 		persons: 2, 4, more. 
		 * 		lug_boot: small, med, big.
		 * 		safety: low, med, high.
		 */

		Car emptyCar = new Car();
		String filename = "../data/car/car.data.txt";
		String separator = ",";	
		WorkingMemory simple = new WorkingMemory();
		
		//FactSetFactory.readObjectData(simple, filename, separator, emptyCar);
		
		try {
			FactSetFactory.fromFileAsObject(simple, emptyCar.getClass(), filename, separator);
			DecisionTreeBuilder bocuk = new DecisionTreeBuilder();

			long dt = System.currentTimeMillis();
			DecisionTree bocuksTree = bocuk.build(simple, emptyCar.getClass().getName(), "target", null);
			dt = System.currentTimeMillis() - dt;
			System.out.println("Time" + dt + "\n" + bocuksTree);

			RulePrinter my_printer = new RulePrinter();
			my_printer.printer(bocuksTree, "examples", "src/rules/examples/"+file);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}


}


