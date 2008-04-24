package dt.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import dt.memory.Domain;
import dt.memory.Fact;
import dt.memory.FactDistribution;

public class FactProcessor {

	public static Hashtable<Object, ArrayList<Fact>> splitFacts(ArrayList<Fact> facts, Domain<?> choosenDomain) {
		if (choosenDomain.isDiscrete()) {	
			return FactProcessor.splitFacts_disc(facts, choosenDomain);
		} else {
			Collections.sort(facts, choosenDomain.factComparator()); /* hack*/
			return FactProcessor.splitFacts_cont_opt(facts, choosenDomain);
		}
	}
	
	public static Hashtable<Object, ArrayList<Fact>> splitFacts_disc(ArrayList<Fact> facts, Domain<?> choosenDomain) {
		String attributeName = choosenDomain.getName();
		List<?> attributeValues = choosenDomain.getValues();
		Hashtable<Object, ArrayList<Fact>> factLists = new Hashtable<Object, ArrayList<Fact>>(attributeValues.size());
		for (Object v : attributeValues) {
			factLists.put(v, new ArrayList<Fact>());
		}
		for (Fact f : facts) {
			factLists.get(f.getFieldValue(attributeName)).add(f);
		}
		return factLists;
	}
	
	
	/* it must work */
	private static Hashtable<Object, ArrayList<Fact>> splitFacts_cont_opt(ArrayList<Fact> facts, Domain<?> attributeDomain) {
		
		String attributeName = attributeDomain.getName();
		
		if (Util.DEBUG) System.out.println("FactProcessor.splitFacts_cont() attr_split "+ attributeName);
		
		List<?> splitValues = attributeDomain.getValues();
		List<Integer> splitIndices = attributeDomain.getIndices();
		if (Util.DEBUG) {
			System.out.println("FactProcessor.splitFacts_cont() haniymis benim repsentativelerim: "+ splitValues.size() + " and the split points "+ splitIndices.size());
			
			System.out.println("FactProcessor.splitFacts_cont() before splitting "+ facts.size());
			
			int index = 0;
			int split_index = 0;
			Object attr_key = splitValues.get(split_index);
			for (Fact f : facts) {
				
				if (index == splitIndices.get(split_index).intValue()+1 ) {
					System.out.print("PRINT* (");
					attr_key = splitValues.get(split_index+1);
					split_index++;	
				} else {
					System.out.print("PRINT (");
				}
				System.out.println(split_index+"): fact "+f);
				index++;
			}
		
		}
		
		Hashtable<Object, ArrayList<Fact>> factLists = new Hashtable<Object, ArrayList<Fact>>(splitValues.size());
		for (Object v: attributeDomain.getValues()) {
			factLists.put(v, new ArrayList<Fact>());
		}
		
		//Comparator<Fact> cont_comp = attributeDomain.factComparator();
		Iterator<Integer> splits_it = splitIndices.iterator();
		int start_point = 0;
		int index = 0;
		while (splits_it.hasNext()) {// || index < attributeDomain.getValues().size()
			int integer_index = splits_it.next().intValue();
//			if (splits_it.hasNext())
//				integer_index = splits_it.next().intValue();
//			else
//				integer_index = facts.size();
			
			Object category = splitValues.get(index);
			//System.out.println("FactProcessor.splitFacts_cont() new category: "+ category);
			Fact pseudo = new Fact();
			try {
				pseudo.add(attributeDomain, category);
				if (Util.DEBUG) {
					System.out.println("FactProcessor.splitFacts_cont() new category: "+ category );
					System.out.println(" ("+start_point+","+integer_index+")");
				}
				ArrayList<Fact> temp = new ArrayList<Fact>(integer_index+1-start_point+1);
				temp.addAll(facts.subList(start_point, integer_index+1));
				factLists.put(category, temp);
				start_point = integer_index+1;

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			index++;
			
		}
		
		
		return factLists;
	}
	public static Hashtable<Object, List<Fact>> splitFacts(List<Fact> facts, Domain<?> choosenDomain) {
		if (choosenDomain.isDiscrete()) {	
			return FactProcessor.splitFacts_disc(facts, choosenDomain);
		} else {
			Collections.sort(facts, choosenDomain.factComparator()); /* hack*/
			return FactProcessor.splitFacts_cont_opt(facts, choosenDomain);
		}
	}
	
	
	public static Hashtable<Object, List<Fact>> splitFacts_disc(List<Fact> facts, Domain<?> choosenDomain) {
		String attributeName = choosenDomain.getName();
		List<?> attributeValues = choosenDomain.getValues();
		Hashtable<Object, List<Fact>> factLists = new Hashtable<Object, List<Fact>>(attributeValues.size());
		for (Object v : attributeValues) {
			factLists.put(v, new ArrayList<Fact>());
		}
		for (Fact f : facts) {
			factLists.get(f.getFieldValue(attributeName)).add(f);
		}
		return factLists;
	}
	public static Hashtable<Object, List<Fact>> splitFacts_cont(List<Fact> facts, Domain<?> attributeDomain) {

		List<?> splitValues = attributeDomain.getValues();
		List<Integer> splitIndices = attributeDomain.getIndices();
		
		System.out.println("Numof classes in domain "+ attributeDomain.getValues().size());
		System.out.println("Numof splits in domain "+ attributeDomain.getIndices().size());
		
		System.out.println("Numof splits in indices "+ splitValues.size());
		Hashtable<Object, List<Fact>> factLists = new Hashtable<Object, List<Fact>>(splitValues.size());
		for (Object v: attributeDomain.getValues()) {
			factLists.put(v, new ArrayList<Fact>());
		}
		
		
		int index = 0;
		int split_index = 0;
		Object attr_key = splitValues.get(split_index);
		List<Fact> subList = factLists.get(attr_key);
		for (Fact f : facts) {
			
			if (index == splitIndices.get(split_index).intValue()+1 ) {
				attr_key = splitValues.get(split_index+1);
				subList = factLists.get(attr_key);
				split_index++;	
			}
			subList.add(f);
			index++;
		}

		return factLists;
		
	}
	/* it must work */
	private static Hashtable<Object, List<Fact>> splitFacts_cont_opt(
			List<Fact> facts, Domain<?> attributeDomain) {
		
		String attributeName = attributeDomain.getName();
		
		if (Util.DEBUG) System.out.println("FactProcessor.splitFacts_cont() attr_split "+ attributeName);
		
		List<?> splitValues = attributeDomain.getValues();
		List<Integer> splitIndices = attributeDomain.getIndices();
		if (Util.DEBUG) {
			System.out.println("FactProcessor.splitFacts_cont() haniymis benim repsentativelerim: "+ splitValues.size() + " and the split points "+ splitIndices.size());
			
			System.out.println("FactProcessor.splitFacts_cont() before splitting "+ facts.size());
			
			int index = 0;
			int split_index = 0;
			Object attr_key = splitValues.get(split_index);
			for (Fact f : facts) {
				
				if (index == splitIndices.get(split_index).intValue()+1 ) {
					System.out.print("PRINT* (");
					attr_key = splitValues.get(split_index+1);
					split_index++;	
				} else {
					System.out.print("PRINT (");
				}
				System.out.println(split_index+"): fact "+f);
				index++;
			}
		
		}
		
		Hashtable<Object, List<Fact>> factLists = new Hashtable<Object, List<Fact>>(splitValues.size());
		for (Object v: attributeDomain.getValues()) {
			factLists.put(v, new ArrayList<Fact>());
		}
		
		//Comparator<Fact> cont_comp = attributeDomain.factComparator();
		Iterator<Integer> splits_it = splitIndices.iterator();
		int start_point = 0;
		int index = 0;
		while (splits_it.hasNext()) {// || index < attributeDomain.getValues().size()
			int integer_index = splits_it.next().intValue();
//			if (splits_it.hasNext())
//				integer_index = splits_it.next().intValue();
//			else
//				integer_index = facts.size();
			
			Object category = splitValues.get(index);
			//System.out.println("FactProcessor.splitFacts_cont() new category: "+ category);
			Fact pseudo = new Fact();
			try {
				pseudo.add(attributeDomain, category);
				if (Util.DEBUG) {
					System.out.println("FactProcessor.splitFacts_cont() new category: "+ category );
					System.out.println(" ("+start_point+","+integer_index+")");
				}
				factLists.put(category, facts.subList(start_point, integer_index+1));
				start_point = integer_index+1;

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			index++;
			
		}
		
		
		return factLists;
	}

	public static void splitUnclassifiedFacts(List<Fact> unclassified_facts, FactDistribution stats) {
		
		Object winner = stats.getThe_winner_target_class();
		//System.out.println(Util.ntimes("DANIEL", 2)+ " lets get unclassified daniel winner "+winner +" num of sup "  +stats.getVoteFor(winner));
		for (Object looser: stats.getTargetClasses()) {
			int num_supp = stats.getVoteFor(looser);
			
			if ((num_supp > 0) && !winner.equals(looser)) {
				
				//System.out.println(Util.ntimes("DANIEL", 2)+ " one looser ? "+looser + " num of sup="+num_supp);
				//System.out.println(" the num of supporters = "+ stats.getVoteFor(looser));
				//System.out.println(" but the guys "+ stats.getSupportersFor(looser));
				//System.out.println("How many bok: "+stats.getSupportersFor(looser).size());
				unclassified_facts.addAll(stats.getSupportersFor(looser));
			} else {
				//System.out.println(Util.ntimes("DANIEL", 5)+ "how many times matching?? not a looser "+ looser );
			}
		}
		
		@SuppressWarnings("unused")
		int bok = 1;
	}
}
