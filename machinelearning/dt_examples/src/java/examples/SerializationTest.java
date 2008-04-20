package examples;

import java.io.InputStreamReader;
import java.io.Reader;
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
import dt.builder.C45TreeBuilder;
import dt.memory.WorkingMemory;
import dt.tools.DecisionTreeSerializer;
import dt.tools.FileProcessor;
import dt.tools.ObjectReader;
import dt.tools.RulePrinter;

public class SerializationTest {
	public static final void main(final String[] args) throws Exception {
		
		WorkingMemory simple = new WorkingMemory();
		String drlFile, inputFile, directory;
		Object obj;
		int test_size = 0;
		
		int obj_type= 0;
		switch (obj_type) {
		case 0:
			drlFile = new String("poker_hands" + ".drl");
			inputFile = new String("data/poker/poker-hand-training-true.data.txt");
			directory = new String("data/poker/");
			
			obj = new Poker();
			test_size = 2;
			break;
		default:
			drlFile = new String("golf" + ".drl");
			inputFile = new String("data/golf/golf.data.txt");
			directory = new String("data/golf/");
			obj = new Golf();
			test_size = 14;
			
		}

		int input_object = 1;	// process from file
		//int input_object = 2;	// read from file
		
		//int input_tree = 1;	// train a new tree
		int input_tree = 2;		// read from file
		//int input_tree = 3;	// retrain a tree
		
		boolean test_tree = true;
		boolean test_rules = false;
		boolean parse_w_drools = false;
		
		List<Object> my_objects = null;
		switch (input_object) {	
		case 1:
			my_objects = FileProcessor.test_process(simple, obj, inputFile, ",");
			break;
		case 2:
			break;
		}
		
		if (my_objects == null)	{
			System.out.println("No objects found to process returning  ");
			return;
		}
		
		C45TreeBuilder bocuk = null;
		DecisionTree bocukTree = null;
		switch (input_tree) {	
		case 1:
			bocuk = builder_test(simple, obj);
			bocukTree = train_test(bocuk, obj);
			write_test(bocuk, directory, "bocuks.build");
			write_test(bocukTree, directory, "bocuks.tree");
			break;
		case 2:
			bocuk = (C45TreeBuilder)read_test(directory, "bocuks.build");
			bocukTree = (DecisionTree)read_test(directory, "bocuks.tree");
			break;
		}
		
		if (bocukTree == null)	{
			System.out.println("No decision tree found to process returning  ");
			return;
		}
		
		List<Integer> test_result = null;
		if (test_tree)	{
			System.out.println("TEST"+ input_tree);
			//test_result = evaluation_test(bocuk, bocukTree, 0, bocuk.getNum_fact_trained());
			//test_result = evaluation_test(bocuk, bocukTree, (int)(bocuk.getNum_fact_trained()*3/5), (int)(bocuk.getNum_fact_trained()*4/5));
			
			test_result = evaluation_test(bocuk, bocukTree, 0, test_size);
			System.out.print("Test results: \tMistakes "+ test_result.get(0));
			System.out.print("\tCorrects "+ test_result.get(1));
			System.out.println("\t Unknown "+ test_result.get(2) +" OF "+ bocuk.getNum_fact_trained() + " facts" );
		}


		/* create the drl */
		if (test_rules) { 
			int max_rules = -1; // no limit print all
			rules_test(bocuk, bocukTree, drlFile, max_rules); 
		}
		
		
		if (parse_w_drools) {
			drools(drlFile, my_objects);
		}

	}
	
	public static C45TreeBuilder builder_test(WorkingMemory simple, Object emptyObject) {

		long st = System.currentTimeMillis();
		String target_attr = ObjectReader.getTargetAnnotation(emptyObject.getClass());
		
		List<String> workingAttributes= ObjectReader.getWorkingAttributes(emptyObject.getClass());
		
		C45TreeBuilder bocuk = new C45TreeBuilder(simple);
		bocuk.init(target_attr, workingAttributes);
		
		long build_time = System.currentTimeMillis();
		System.out.println("\nTime to builder " + (build_time-st));
		
		return bocuk;

		
	}
	
	public static DecisionTree train_test(C45TreeBuilder builder, Object emptyObject) {
								 // String drlfile, , int max_rules
		long st = System.currentTimeMillis();
		DecisionTree bocuksTree = builder.build(emptyObject.getClass());
		
		long train_time = System.currentTimeMillis();
		
		System.out.println("\nTime to train_decision_tree " + (train_time-st));
	
		return bocuksTree;
	}
	
	public static void write_test(Object tree, String outputdirectory, String file) {
		 // String drlfile, , int max_rules

		long st = System.currentTimeMillis();

		DecisionTreeSerializer.write(tree, outputdirectory+file);
		
		long write_time = System.currentTimeMillis();
		System.out.println("Time to write_decision_tree " + (write_time-st) + "\n" );
	
		
	}
	
	public static Object read_test(String directory, String file) {
		long st = System.currentTimeMillis();
		try {
			Object obj = DecisionTreeSerializer.read(directory+file);
			
			long read_time = System.currentTimeMillis();
			System.out.println("Time to read_" + file+" "+(read_time-st) + "\n" );
			
			return obj;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	

	public static List<Integer> evaluation_test(C45TreeBuilder builder, DecisionTree tree, int first_f, int last_f) {
		long st = System.currentTimeMillis();
		
		List<Integer> evaluation = builder.test(tree, builder.getFacts().subList(first_f, last_f));
		long test_time = System.currentTimeMillis();
		System.out.println("Time to test_decision_tree " + (test_time-st) + "\n" );
		
		return evaluation;

	}
	
	
	public static void rules_test (C45TreeBuilder builder, DecisionTree tree, String drlfile, int max_rules) {
		long st = System.currentTimeMillis();

		RulePrinter my_printer  = new RulePrinter(builder.getNum_fact_trained());
		if (max_rules >0)
			my_printer.setMax_num_rules(max_rules);
		boolean sort_via_rank = true;
		boolean print = true;
		my_printer.printer(tree, sort_via_rank, print);
		my_printer.write2file("examples", "src/rules/examples/" + drlfile);
		
		long print_time = System.currentTimeMillis();
		System.out.println("Time to print_rules " + (print_time-st) + "\n" );
	}
	public static void drools(String drlFile, List<Object> my_objects) throws Exception{
		/* parse the drl */
		
		
		//read in the source 
		// TODO give an exception of the file does not exist
		final Reader source = new InputStreamReader(Golf.class
				.getResourceAsStream(drlFile));

		final PackageBuilder builder = new PackageBuilder();

		//this will parse and compile in one step
		builder.addPackageFromDrl(source);

		// Check the builder for errors
		if (builder.hasErrors()) {
			System.out.println(builder.getErrors().toString());
			throw new RuntimeException("Unable to compile \"" + drlFile + "\".");
		}
		//get the compiled package (which is serializable)
		final Package pkg = builder.getPackage();

		//add the package to a rulebase (deploy the rule package).
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
			logger.setFileName("log/golf");

			Iterator<Object> it_obj = my_objects.iterator();
			while (it_obj.hasNext()) {
				Object obj = it_obj.next();

				//System.out.println("Object " + obj);

				try {
					session.insert(obj);

				} catch (Exception e) {
					System.out
							.println("Inserting element " + obj + " and " + e);
				}
			}

			session.fireAllRules();

			//        Iterator<Object> my_it = session.iterateObjects();
			//        
			//        while(my_it.hasNext()) {
			//        	Object o = my_it.next();
			//        	//System.out.println("Object " + o);
			//        }
			logger.writeToDisk();

			session.dispose();
		}
		System.out.println("Happy ending");
	}
}

