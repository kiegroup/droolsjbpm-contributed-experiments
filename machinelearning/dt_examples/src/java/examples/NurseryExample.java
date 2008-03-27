package examples;

import id3.BocukFileExample;

import java.io.InputStreamReader;
import java.io.Reader;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.compiler.PackageBuilder;
import org.drools.rule.Package;

public class NurseryExample {
	
	
	public static final void main(final String[] args) throws Exception {
		

		int action = 1; /* create the drl */
		//int action = 2; /* parse the drl */

		String drlFile = new String("nurseries"+".drl");
		switch (action) {
		case 1:
			Object nurse = new Nursery();
			BocukFileExample.processFileExample(nurse, drlFile, "../data/nursery/nursery.data.txt", ",", "classnursery");
			
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
			
			System.out.println("Happy ending");
			break;
			

			

		}

	}	
	
	


}


