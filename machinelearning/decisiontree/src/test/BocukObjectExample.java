package test;

import java.util.ArrayList;

import dt.DecisionTree;
import dt.builder.IDTreeBuilder;
import dt.memory.OOFactSet;
import dt.memory.WorkingMemory;
import dt.tools.RulePrinter;

public class BocukObjectExample {

	public static void main(String[] args) {
		RestaurantOld arest = new RestaurantOld(true, false,  false,  true, "Full", 1,   false,   false,  "Thai",    "30-60",  false);
		Class<?> k = arest.getClass();
		ArrayList<Object> facts = new ArrayList<Object>();
		facts.add(new RestaurantOld(true, false,  false,  true, "Full", 1,   false,   false,  "Thai",    "30-60",  false));
		facts.add(new RestaurantOld(false,  true, false,  false,  "Some", 1,   false,   false,  "Burger",  "0-10",   true));
		facts.add(new RestaurantOld(true, false,  true, true, "Full", 1,   true,   false,  "Thai",    "10-30",  true));
		facts.add(new RestaurantOld(true, false,  true, false,  "Full", 3, false,   true, "French",  ">60",    false)); 
		facts.add(new RestaurantOld(false,  true, false,  true, "Some", 2,  true,  true, "Italian", "0-10",   true));
		facts.add(new RestaurantOld(false,  true, false,  false,  "None", 1,   true,  false,  "Burger",  "0-10",   false));
		facts.add(new RestaurantOld(false,  false,  false,  true, "Some", 2,  true,  true, "Thai",    "0-10",   true));
		facts.add(new RestaurantOld(false,  true, true, false,  "Full", 1,   true,  false,  "Burger",  ">60",    false)); 
		facts.add(new RestaurantOld(true, true, true, true, "Full", 3, false,   true, "Italian", "10-30",  false)); 
		facts.add(new RestaurantOld(false,  false,  false,  false,  "None", 1,   false,   false,  "Thai",    "0-10",   false)); 
		facts.add(new RestaurantOld(true, true, true, true, "Full", 1,   false,   false,  "Burger",  "30-60",  true));

		WorkingMemory simple = new WorkingMemory();
		OOFactSet fs = simple.getFactSet(arest.getClass());

		for(Object r: facts) {
			try {
				//simple.insert(element)
				fs.insert(r);
			} catch (Exception e) {
				System.out.println("Inserting element "+ r + " and "+ e);
			}
		}
		
		IDTreeBuilder bocuk = new IDTreeBuilder();
		
		long dt = System.currentTimeMillis();
		DecisionTree bocuksTree = bocuk.build(simple, k, "will_wait", null);
		dt = System.currentTimeMillis() - dt;
		System.out.println("Time"+dt+"\n"+bocuksTree);
		
		RulePrinter my_printer = new RulePrinter(bocuk.getNum_fact_processed());
		boolean sort_via_rank = true;
		my_printer.printer(bocuksTree,"test" , new String("../dt_learning/src/test/rules"+".drl"), sort_via_rank);
	}
}
