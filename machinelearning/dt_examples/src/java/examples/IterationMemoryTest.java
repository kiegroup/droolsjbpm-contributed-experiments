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
import dt.TreeNode;
import dt.builder.C45TreeBuilder;
import dt.builder.C45TreeIterator;
import dt.memory.Fact;
import dt.memory.WorkingMemory;
import dt.tools.DecisionTreeSerializer;
import dt.tools.FileProcessor;
import dt.tools.ObjectReader;
import dt.tools.RulePrinter;
import dt.tools.Util;

public class IterationMemoryTest {
	public static final void main(final String[] args) throws Exception {
		int obj_type= 0;
		WorkingMemory simple = new WorkingMemory();
		
		ArrayList<Fact> _facts = null;
		String drlFile, inputFile, directory;
		String build_file = "bocuks.iterator", tree_file = "bocuks_new.tree";
		Object obj;
		
		//obj_type= 1;
		switch (obj_type) {
		case 1:
			drlFile = new String("poker_hands_" + ".drl");
			inputFile = new String("data/poker/poker-hand-training-true.data.txt");
			directory = new String("data/poker/");	
			obj = new Poker();
			
			break;
		default:
			drlFile = new String("golf_" + ".drl");
			inputFile = new String("data/golf/golf.data.txt");
			directory = new String("data/golf/");
			obj = new Golf();
		}
		
		int input_object = 1;	// process text from file
		//int input_object = 2;	// read from file
		
		List<Object> my_objects = null;
		switch (input_object) {	
		case 1:	// process from file
			my_objects = FileProcessor.test_process(simple, obj, inputFile, ",");
			_facts = simple.getFacts(obj.getClass());
			break;
		case 2:	// read from file
			break;
		}
		
		if (my_objects == null)	{
			System.out.println("No objects found to process returning  ");
			return;
		}
		
		
		int input_tree = 0;	// read from file
		//int input_tree = 1;		// train a new tree with some part of the facts
		int[] test_size = {0, _facts.size()}, train_size = {0, _facts.size()};
		int[] retrain_size = new int[3];
		boolean retrain_tree = true, test_tree = true, print_tree = true, write_tree = true, test_rules = false, parse_w_drools = false;
		switch (obj_type) {
		case 1:
			test_size[1] = 2;
			train_size[1] = 2;
			retrain_size [1] = 2;
			break;
		default:
			test_size[1] = 14;
			train_size[1] = 4;
			retrain_size[0] = train_size[1];
			retrain_size[1] = retrain_size[0]+4;
			retrain_size[2] = _facts.size();
		}
		
		C45TreeIterator bocuk = null;
		DecisionTree bocukTree = null;
		boolean tree_read = true;
		if (input_tree == 0) {	
			// read the matching facts from file
			try {
				bocuk = (C45TreeIterator)read(directory, build_file);
				bocukTree = (DecisionTree)read(directory, tree_file);
			} catch (Exception e) {
				System.out.println("EXCEPTION:  Could not read the tree "+ e);
				e.printStackTrace();
				tree_read = false;
			}
//			catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (ClassNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}	
		if (input_tree > 0 || !tree_read || bocukTree==null || bocuk==null) {
			if (!tree_read)	System.out.println("EXCEPTION:  Could not read the tree so training ");
			
			bocuk = builder(simple, obj);
			ArrayList<Fact> training_list = new ArrayList<Fact>(train_size[1]-train_size[0]+1);
			for (int i= train_size[0]; i<train_size[1]; i++)
				training_list.add(_facts.get(i));
			/* find the matching facts */
			bocukTree = train(bocuk, obj, training_list);
			
			write_tree = true;
		}
		
		
		if (bocukTree == null)	{
			System.out.println("No decision tree found to process returning  ");
			return;
		} else {
			System.out.println("!!My matching facts: \n"+ Util.ntimes("\n", 3));
			for (TreeNode obj_node : bocuk.getMatchingFacts().keySet())
				System.out.println("* "+obj_node.toString(1)+ " => "+bocuk.getMatchingFacts().get(obj_node) );
			
			if (print_tree) 
				System.out.println("INPUT TREE: "+ bocukTree.toString(bocuk.getMatchingFacts()));
		}
		
		if (retrain_tree) {
			// retrain a tree
			
			ArrayList<Fact> re_training_list = new ArrayList<Fact>(retrain_size[1]-retrain_size[0]);
			for (int i= retrain_size[0]; i<retrain_size[1]; i++)
				re_training_list.add(_facts.get(i));
			bocukTree = retrain(bocuk, bocukTree, re_training_list);
			
			write_tree = false;
		}
		
		List<Integer> test_result = null;
		if (test_tree)	{
			System.out.println("TEST"+ input_tree);
			//test_result = evaluation_test(bocuk, bocukTree, 0, bocuk.getNum_fact_trained());
			//test_result = evaluation_test(bocuk, bocukTree, (int)(bocuk.getNum_fact_trained()*3/5), (int)(bocuk.getNum_fact_trained()*4/5));
			
			test_result = evaluation(bocuk, bocukTree, _facts.subList(test_size[0], test_size[1]));
			System.out.print("Test results: \tMistakes "+ test_result.get(0));
			System.out.print("\tCorrects "+ test_result.get(1));
			System.out.println("\t Unknown "+ test_result.get(2) +" OF "+ (test_size[1]-test_size[0]) + " facts" );
			if (print_tree)
				System.out.println("TESTED TREE: "+ bocukTree.toString(bocuk.getMatchingFacts()));
		}
		
		if (retrain_tree) {
			// retrain a tree
			
			ArrayList<Fact> re_training_list = new ArrayList<Fact>(retrain_size[2]-retrain_size[1]);
			for (int i= retrain_size[1]; i<retrain_size[2]; i++)
				re_training_list.add(_facts.get(i));
			bocukTree = retrain(bocuk, bocukTree, re_training_list);
			
			write_tree = false;
		}
		
		if (test_tree)	{
			System.out.println("TEST2"+ input_tree);
			//test_result = evaluation_test(bocuk, bocukTree, 0, bocuk.getNum_fact_trained());
			//test_result = evaluation_test(bocuk, bocukTree, (int)(bocuk.getNum_fact_trained()*3/5), (int)(bocuk.getNum_fact_trained()*4/5));
			
			test_result = evaluation(bocuk, bocukTree, _facts.subList(test_size[0], test_size[1]));
			System.out.print("Test results: \tMistakes "+ test_result.get(0));
			System.out.print("\tCorrects "+ test_result.get(1));
			System.out.println("\t Unknown "+ test_result.get(2) +" OF "+ (test_size[1]-test_size[0]) + " facts" );
			if (print_tree)
				System.out.println("TESTED TREE: "+ bocukTree.toString(bocuk.getMatchingFacts()));
		}
		
		if (write_tree) {
			write(bocuk, directory, build_file);
			write(bocukTree, directory, tree_file);
		}


		/* create the drl */
		if (test_rules) { 
			int max_rules = -1; // no limit print all
			rules(bocuk, bocukTree, drlFile, max_rules); 
		}
		
		
		if (parse_w_drools) {
			drools(drlFile, my_objects);
		}

	}
	
	public static C45TreeIterator builder(WorkingMemory simple, Object emptyObject) {

		long st = System.currentTimeMillis();
		String target_attr = ObjectReader.getTargetAnnotation(emptyObject.getClass());
		
		List<String> workingAttributes= ObjectReader.getWorkingAttributes(emptyObject.getClass());
		
		C45TreeBuilder c45_build = new C45TreeBuilder();
		c45_build.setTarget(target_attr);
		for (String attr : workingAttributes) {
			c45_build.addAttribute(attr);
			c45_build.addDomain(simple.getDomain(attr));
		}
		C45TreeIterator bocuk = new C45TreeIterator(c45_build);
		
		//bocuk.init(target_attr, workingAttributes);
		
		long build_time = System.currentTimeMillis();
		System.out.println("\nTime to builder " + (build_time-st));
		
		return bocuk;	
	}
	
	public static DecisionTree train(C45TreeIterator builder, Object emptyObject, ArrayList<Fact> facts) {
								 // String drlfile, , int max_rules
		long st = System.currentTimeMillis();
		DecisionTree bocuksTree = builder.build_to_iterate(emptyObject.getClass(), facts);
		
		long train_time = System.currentTimeMillis();
		System.out.println("\nTime to train_decision_tree " + (train_time-st));
	
		return bocuksTree;
	}
	
	public static DecisionTree retrain(C45TreeIterator builder, DecisionTree dt, ArrayList<Fact> facts) {
		 // String drlfile, , int max_rules
		long st = System.currentTimeMillis();
		DecisionTree bocuksTree = builder.re_build(dt, facts);

		long retrain_time = System.currentTimeMillis();
		System.out.println("\nTime to train_decision_tree " + (retrain_time-st));

		return bocuksTree;
	}
	
	public static void write(Object tree, String outputdirectory, String file) {
		 // String drlfile, , int max_rules

		long st = System.currentTimeMillis();

		DecisionTreeSerializer.write(tree, outputdirectory+file);
		
		long write_time = System.currentTimeMillis();
		System.out.println("Time to write_decision_tree " + (write_time-st) + "\n" );
	
		
	}
	
	public static Object read(String directory, String file) throws Exception {
		long st = System.currentTimeMillis();
		
		Object obj = DecisionTreeSerializer.read(directory+file);
		
		long read_time = System.currentTimeMillis();
		System.out.println("Time to read_" + file+" "+(read_time-st) + "\n" );
		
		return obj;
//		try {
//			
//		} catch (Exception e) {
//			// train from scratch
//			e.printStackTrace();
//		}
//return null;
		
		
	}
	

	public static List<Integer> evaluation(C45TreeIterator builder, DecisionTree tree, List<Fact> facts) {
		long st = System.currentTimeMillis();
		
		List<Integer> evaluation = builder.test(tree, facts); //builder.getFacts().subList(first_f, last_f));
		long test_time = System.currentTimeMillis();
		System.out.println("Time to test_decision_tree " + (test_time-st) + "\n" );
		
		return evaluation;

	}
	
	
	public static void rules (C45TreeIterator builder, DecisionTree tree, String drlfile, int max_rules) {
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


