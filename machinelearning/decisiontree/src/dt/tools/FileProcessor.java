package dt.tools;

import java.util.List;

import dt.DecisionTree;
import dt.builder.C45TreeBuilder;
import dt.builder.IDTreeBuilder;
import dt.memory.FactSetFactory;
import dt.memory.WorkingMemory;

public class FileProcessor {
	public static List<Object> processFileExmID3(WorkingMemory simple, Object emptyObject, String drlfile, String datafile, String separator) {

		try {
			boolean only_discrete = true;
			List<Object> obj_read=FactSetFactory.fromFileAsObject(simple, emptyObject.getClass(), datafile, separator, only_discrete);
			IDTreeBuilder bocuk = new IDTreeBuilder();

			long dt = System.currentTimeMillis();
			String target_attr = ObjectReader.getTargetAnnotation(emptyObject.getClass());
			
			DecisionTree bocuksTree = bocuk.build(simple, emptyObject.getClass().getName(), target_attr, null);
			dt = System.currentTimeMillis() - dt;
			System.out.println("Time" + dt + "\n" + bocuksTree);

			RulePrinter my_printer = new RulePrinter(bocuk.getNum_fact_trained());
			boolean sort_via_rank = true;
			boolean print = true;
			my_printer.printer(bocuksTree, sort_via_rank, print);
			my_printer.write2file("examples", "src/rules/examples/" + drlfile);
			
			return obj_read;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

		
	}

	
	public static List<Object> processFileExmC45(WorkingMemory simple, Object emptyObject, String drlfile, String datafile, String separator, int max_rules) {

		try {
			boolean all_discrete = false;
			List<Object> obj_read=FactSetFactory.fromFileAsObject(simple, emptyObject.getClass(), datafile, separator, all_discrete);
			C45TreeBuilder bocuk = new C45TreeBuilder();

			long dt = System.currentTimeMillis();
			String target_attr = ObjectReader.getTargetAnnotation(emptyObject.getClass());
			
			List<String> workingAttributes= ObjectReader.getWorkingAttributes(emptyObject.getClass());
			
			DecisionTree bocuksTree = bocuk.build(simple, emptyObject.getClass(), target_attr, workingAttributes);
			dt = System.currentTimeMillis() - dt;
			System.out.println("Time" + dt + "\n" + bocuksTree);

			RulePrinter my_printer  = new RulePrinter(bocuk.getNum_fact_trained());
			if (max_rules >0)
				my_printer.setMax_num_rules(max_rules);
			boolean sort_via_rank = true;
			boolean print = true;
			my_printer.printer(bocuksTree, sort_via_rank, print);
			my_printer.write2file("examples", "src/rules/examples/" + drlfile);
			
			return obj_read;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

		
	}
	
	public static List<Object> test_dt_training(WorkingMemory simple, Object emptyObject, String drlfile, String datafile, String separator, int max_rules) {

		try {
			boolean all_discrete = false;
			List<Object> obj_read=FactSetFactory.fromFileAsObject(simple, emptyObject.getClass(), datafile, separator, all_discrete);
			

			long st = System.currentTimeMillis();
			String target_attr = ObjectReader.getTargetAnnotation(emptyObject.getClass());
			
			List<String> workingAttributes= ObjectReader.getWorkingAttributes(emptyObject.getClass());
			
			C45TreeBuilder bocuk = new C45TreeBuilder(simple);
			bocuk.init(target_attr, workingAttributes);
			DecisionTree bocuksTree = bocuk.build(emptyObject.getClass());
			long train_time = System.currentTimeMillis();
			
			System.out.println("\nTime to build" + (train_time-st));
			
			System.out.println(Util.ntimes("\n", 1)+Util.ntimes("$", 5)+" TESTING "+Util.ntimes("\n", 1));
			List<Integer> evaluation = bocuk.test(bocuksTree, bocuk.getFacts());//.subList(339, 340));
			long test_time = System.currentTimeMillis();
			System.out.println("Time to test" + (test_time-train_time) + "\n" );
			System.out.println("TESTING results: Mistakes "+ evaluation.get(0));
			System.out.println("TESTING results: Corrects "+ evaluation.get(1));
			System.out.println("TESTING results: Unknown "+ evaluation.get(2));
			
//			RulePrinter my_printer  = new RulePrinter(bocuk.getNum_fact_trained());
//			if (max_rules >0)
//				my_printer.setMax_num_rules(max_rules);
//			boolean sort_via_rank = true;
//			boolean print = true;
//			my_printer.printer(bocuksTree, sort_via_rank, print);
//			my_printer.write2file("examples", "src/rules/examples/" + drlfile);
//			
			return obj_read;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

		
	}
	
	
	public static List<Object> test_process(WorkingMemory simple, Object emptyObject, String datafile, String separator) {

		try {
			long st = System.currentTimeMillis();
			boolean all_discrete = false;
			List<Object> obj_read=FactSetFactory.fromFileAsObject(simple, emptyObject.getClass(), datafile, separator, all_discrete);
			long process_time = System.currentTimeMillis();
			
			System.out.println("\nTime to process_objects " + (process_time-st));
//			
			return obj_read;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

		
	}
	
}
