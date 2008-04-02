package test;


import dt.DecisionTree;
import dt.builder.IDTreeBuilder;
import dt.memory.FactSetFactory;
import dt.memory.WorkingMemory;
import dt.tools.RulePrinter;

public class BocukFileExample {

	public static void main(String[] args) {
		

		WorkingMemory simple = new WorkingMemory();


		/* insert the guys */
		//String klassCar =  FactSetFactory.insertCarSet(simple);
		String klassNursery =  FactSetFactory.insertNurserySet(simple);
		
		//String klassAdvertisement = FactSetFactory.insertAdvertisementSet(simple);

		
		boolean buildTree = true;
		if (buildTree) {

			IDTreeBuilder bocuk = new IDTreeBuilder();
			//DecisionTreeBuilderMT bocuk = new DecisionTreeBuilderMT();

			long dt = System.currentTimeMillis();
			//DecisionTree bocuksTree = bocuk.build(simple, klassCar, "classCar", null);
			DecisionTree bocuksTree = bocuk.build(simple, klassNursery, "classnursery", null);
			
			//DecisionTree bocuksTree = bocuk.build(simple, klassAdvertisement, "classAdvertisement", FactSetFactory.attributesOfAdvertisement);
			
			
			dt = System.currentTimeMillis() - dt;
			System.out.println("Time"+dt + " facts read: "+bocuksTree.getNumRead() + " num call: "+ bocuk.getNumCall() );
			//System.out.println(bocuksTree);

			RulePrinter my_printer = new RulePrinter(bocuk.getNum_fact_processed());
			boolean sort_via_rank = true;
			my_printer.printer(bocuksTree, null, null, sort_via_rank);
		}
	}


}

