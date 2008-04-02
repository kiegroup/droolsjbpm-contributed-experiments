package dt.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import dt.memory.Domain;
import dt.memory.Fact;
import dt.memory.FactDistribution;
import dt.memory.FactTargetDistribution;

public class FactProcessor {

	public static Hashtable<Object, List<Fact>> splitFacts(
			List<Fact> facts, Domain<?> choosenDomain) {
		if (choosenDomain.isDiscrete()) {	
			return FactProcessor.splitFacts_disc(facts, choosenDomain);
		} else {
			Collections.sort(facts, choosenDomain.factComparator()); /* hack*/
			return FactProcessor.splitFacts_cont(facts, choosenDomain);
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
	
	/* it must work */
	private static Hashtable<Object, List<Fact>> splitFacts_cont(
			List<Fact> facts, Domain<?> attributeDomain) {
		
		String attributeName = attributeDomain.getName();
		
		if (Util.DEBUG) System.out.println("FactProcessor.splitFacts_cont() attr_split "+ attributeName);
		
		List<?> categorization = attributeDomain.getValues();
		List<Integer> split_indices = attributeDomain.getIndices();
		if (Util.DEBUG) {
			System.out.println("FactProcessor.splitFacts_cont() haniymis benim repsentativelerim: "+ categorization.size() + " and the split points "+ split_indices.size());
			
			System.out.println("FactProcessor.splitFacts_cont() before splitting "+ facts.size());
			int split_i =0;
			for(int i=0; i<facts.size(); i++) {
				if (split_i<split_indices.size() && split_indices.get(split_i).intValue()== i) {
					System.out.println("PRINT*: FactProcessor.splitFacts_cont() will split at "+i + " the fact "+facts.get(i));
					split_i ++;
				} else {
					System.out.println("PRINT: FactProcessor.splitFacts_cont() at "+i + " the fact "+facts.get(i));
				}
			}
		}
		
		Hashtable<Object, List<Fact>> factLists = new Hashtable<Object, List<Fact>>(categorization.size());
		for (Object v: attributeDomain.getValues()) {
			factLists.put(v, new ArrayList<Fact>());
		}
		
		//Comparator<Fact> cont_comp = attributeDomain.factComparator();
		Iterator<Integer> splits_it = split_indices.iterator();
		int start_point = 0;
		int index = 0;
		
		while (splits_it.hasNext() || index < attributeDomain.getValues().size()) {
			int integer_index;
			if (splits_it.hasNext())
				integer_index = splits_it.next().intValue();
			else
				integer_index = facts.size();
			
			Object category = attributeDomain.getValues().get(index);
			//System.out.println("FactProcessor.splitFacts_cont() new category: "+ category);
			Fact pseudo = new Fact();
			try {
				pseudo.add(attributeDomain, category);
				if (Util.DEBUG) {
					System.out.println("FactProcessor.splitFacts_cont() new category: "+ category );
					System.out.println(" ("+start_point+","+integer_index+")");
				}
				factLists.put(category, facts.subList(start_point, integer_index));
				start_point = integer_index;

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			index++;
			
		}
		
		
		return factLists;
	}

	public static void splitUnclassifiedFacts(
			List<Fact> unclassified_facts, FactDistribution stats) {
		
		Object winner = stats.getThe_winner_target_class();
		System.out.println(Util.ntimes("DANIEL", 2)+ " lets get unclassified daniel winner "+winner +" num of sup "  +stats.getVoteFor(winner));
		for (Object looser: stats.getTargetClasses()) {
			int num_supp = stats.getVoteFor(looser);
			
			if ((num_supp > 0) && !winner.equals(looser)) {
				
				System.out.println(Util.ntimes("DANIEL", 2)+ " one looser ? "+looser + " num of sup="+num_supp);
				//System.out.println(" the num of supporters = "+ stats.getVoteFor(looser));
				//System.out.println(" but the guys "+ stats.getSupportersFor(looser));
				//System.out.println("How many bok: "+stats.getSupportersFor(looser).size());
				unclassified_facts.addAll(stats.getSupportersFor(looser));
			} else
				System.out.println(Util.ntimes("DANIEL", 5)+ "how many times matching?? not a looser "+ looser );
		}
		
		@SuppressWarnings("unused")
		int bok = 1;
	}
}
